// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package vector

import io.ktor.client.*
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
import org.eclipse.lmos.router.vector.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter

fun main() {
    val embeddingClient = DefaultEmbeddingClient(HttpClient())
    val ping = embeddingClient.embed("Hello").getOrNull()
    require(ping != null) {
        "Ollama is not running. Please start Ollama to run this test. Also, make sure you have 'all-minilm' installed in your Ollama."
    }

    val samplesToAnnotate = 5000
    val inputFilePath = ClassLoader.getSystemResource("test.csv").file
    val outputFilePath = "benchmarks/vector_prediction.csv"
    val jsonFilePath = ClassLoader.getSystemResource("agentRoutingSpecs.json").file

    val agentSpecsProvider = JsonAgentRoutingSpecsProvider(jsonFilePath = jsonFilePath)
    val vectorSearchClient = DefaultVectorClient(DefaultVectorClientProperties(seedJsonFilePath = ""), embeddingClient)
    vectorSearchClient.seed(seedVectors(-1))

    val agentSpecResolver =
        VectorAgentRoutingSpecsResolver(
            agentRoutingSpecsProvider = agentSpecsProvider,
            vectorSearchClient = vectorSearchClient,
        )

    runBlocking {
        processCsvInParallel(inputFilePath, outputFilePath, samplesToAnnotate, agentSpecResolver)
    }
}

suspend fun processCsvInParallel(
    inputFilePath: String,
    outputFilePath: String,
    samplesToAnnotate: Int,
    agentSpecResolver: VectorAgentRoutingSpecsResolver,
) = coroutineScope {
    val inputFile = File(inputFilePath)
    val outputFile = File(outputFilePath)
    val semaphore = Semaphore(10) // Limit the number of concurrent coroutines

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

fun seedVectors(samples: Int): List<VectorSeedRequest> {
    val trainFilePath = ClassLoader.getSystemResource("train.csv").file
    val trainFile = File(trainFilePath)
    val seedRequests = mutableListOf<VectorSeedRequest>()

    FileReader(trainFile).use { reader ->
        // Parse the CSV file
        val csvFormat =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
        val csvParser = CSVParser(reader, csvFormat)

        for (record: CSVRecord in csvParser) {
            if (samples > 0 && seedRequests.size >= samples) {
                break
            }
            val instruction = record.get("instruction")
            val agentName = record.get("agent")
            seedRequests.add(VectorSeedRequest(agentName, instruction))
        }
    }
    return seedRequests
}

fun getAgentName(
    inputString: String,
    agentSpecResolver: VectorAgentRoutingSpecsResolver,
): String {
    val context = Context(listOf())
    val input = UserMessage(inputString)
    val result = agentSpecResolver.resolve(context, input)
    return result.getOrNull()?.name ?: "No agent found"
}
