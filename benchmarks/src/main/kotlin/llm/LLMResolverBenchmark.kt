// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package llm

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.eclipse.lmos.router.core.Context
import org.eclipse.lmos.router.core.JsonAgentRoutingSpecsProvider
import org.eclipse.lmos.router.core.UserMessage
import org.eclipse.lmos.router.core.getOrNull
import org.eclipse.lmos.router.llm.DefaultModelClient
import org.eclipse.lmos.router.llm.DefaultModelClientProperties
import org.eclipse.lmos.router.llm.LLMAgentRoutingSpecsResolver
import java.io.File
import java.io.FileReader
import java.io.FileWriter

fun main() {
    require(System.getenv("OPENAI_API_KEY") != null) { "Please set the OPENAI_API_KEY environment variable to run the tests" }

    val samplesToAnnotate = 2000
    // read from src/main/resources/test.csv
    val inputFilePath = ClassLoader.getSystemResource("test.csv").file
    val outputFilePath = "benchmarks/llm_prediction.csv"
    val jsonFilePath = ClassLoader.getSystemResource("agentRoutingSpecs.json").file

    val agentSpecsProvider = JsonAgentRoutingSpecsProvider(jsonFilePath = jsonFilePath)
    val agentSpecResolver =
        LLMAgentRoutingSpecsResolver(
            agentRoutingSpecsProvider = agentSpecsProvider,
            modelClient =
                DefaultModelClient(
                    defaultModelClientProperties =
                        DefaultModelClientProperties(
                            openAiApiKey = System.getenv("OPENAI_API_KEY"),
                        ),
                ),
        )

    runBlocking {
        processCsvInParallel(inputFilePath, outputFilePath, samplesToAnnotate, agentSpecResolver)
    }
}

suspend fun processCsvInParallel(
    inputFilePath: String,
    outputFilePath: String,
    samplesToAnnotate: Int,
    agentSpecResolver: LLMAgentRoutingSpecsResolver,
) = coroutineScope {
    val inputFile = File(inputFilePath)
    val outputFile = File(outputFilePath)
    val semaphore = Semaphore(4) // Limit the number of concurrent coroutines

    FileReader(inputFile).use { reader ->
        FileWriter(outputFile).use { writer ->
            // Parse the CSV file
            val csvFormat =
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
            val csvParser = CSVParser(reader, csvFormat)

            // Get the headers and add the "prediction" column
            val headers = csvParser.headerNames.plus("prediction").toTypedArray()

            // Create the CSV printer with the updated headers
            val csvPrinter =
                CSVPrinter(
                    writer,
                    CSVFormat.DEFAULT
                        .builder()
                        .setHeader(*headers)
                        .build(),
                )

            val jobs = mutableListOf<Job>()
            var count = 0

            for (record: CSVRecord in csvParser) {
                if (count >= samplesToAnnotate) {
                    break
                }
                count++

                val job =
                    launch(Dispatchers.IO) {
                        semaphore.withPermit {
                            val instruction = record.get("instruction")
                            val prediction = getAgentName(instruction, agentSpecResolver)
                            val recordWithPrediction = record.toMap().plus("prediction" to prediction)
                            synchronized(csvPrinter) {
                                csvPrinter.printRecord(recordWithPrediction.values)
                            }
                        }
                    }
                jobs.add(job)
            }

            jobs.forEach { it.join() }
            csvPrinter.flush()
        }
    }
}

fun getAgentName(
    inputString: String,
    agentSpecResolver: LLMAgentRoutingSpecsResolver,
): String {
    val context = Context(listOf())
    val input = UserMessage(inputString)
    val result = agentSpecResolver.resolve(context, input)
    return result.getOrNull()?.name ?: "No agent found"
}
