// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections
import kotlinx.serialization.json.Json
import org.eclipse.lmos.router.core.*
import org.eclipse.lmos.router.vector.VectorAgentRoutingSpecsResolver
import org.eclipse.lmos.router.vector.VectorSeedClient
import org.eclipse.lmos.router.vector.VectorSeedRequest
import org.eclipse.lmos.router.vector.starter.VectorAgentRoutingSpecsResolverAutoConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
import org.springframework.ai.autoconfigure.vectorstore.qdrant.QdrantVectorStoreAutoConfiguration
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationListener
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.qdrant.QdrantContainer
import java.io.File
import java.util.Properties

@SpringBootTest
@ContextConfiguration(
    classes = [
        VectorAgentRoutingSpecsResolverAutoConfiguration::class,
        OpenAiAutoConfiguration::class,
        QdrantVectorStoreAutoConfiguration::class, QdrantVectorConfigurationListener::class,
    ],
)
class SpringVectorAgentRoutingSpecsResolverFlow(
    @Autowired
    private val vectorAgentRoutingSpecsResolver: VectorAgentRoutingSpecsResolver,
    @Autowired
    private val vectorSeedClient: VectorSeedClient,
    @Autowired
    private val qdrantClient: QdrantClient,
) {
    @Test
    fun `test sample flow`() {
        require(System.getenv("OPENAI_API_KEY") != null) {
            "Please set the OPENAI_API_KEY environment variable to run the tests"
        }

        val context = Context(listOf(AssistantMessage("Hello")))

        val jsonSeedFileContent = File("src/test/resources/seed.json").readText()
        val vectorSeedRequests = Json.decodeFromString<List<VectorSeedRequest>>(jsonSeedFileContent)
        vectorSeedClient.seed(vectorSeedRequests).getOrThrow()

        // The input to test whether offer-agent is resolved
        val input = UserMessage("Can you help me find a new phone?")
        val result = vectorAgentRoutingSpecsResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "offer-agent")

        // The input to test whether order-agent is resolved
        val input2 = UserMessage("I would like to cancel my pizza order")
        val result2 = vectorAgentRoutingSpecsResolver.resolve(context, input2)
        assert(result2 is Success)
        assert((result2 as Success).getOrNull()?.name == "order-agent")
    }

    @BeforeEach
    fun setUp() {
        qdrantClient
            .createCollectionAsync(
                "test",
                Collections.VectorParams
                    .newBuilder()
                    .setDistance(Collections.Distance.Cosine)
                    .setSize(1536)
                    .build(),
            ).get()
    }

    @AfterEach
    fun tearDown() {
        qdrantClient.deleteCollectionAsync("test").get()
    }
}

@Component
class QdrantVectorConfigurationListener :
    ApplicationListener<ApplicationEnvironmentPreparedEvent>,
    DisposableBean {
    private lateinit var container: QdrantContainer

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        container = QdrantContainer("qdrant/qdrant:v1.7.4")
        container.start()
        val qdrantVectorStoreProperties = Properties()
        qdrantVectorStoreProperties.put("spring.ai.vectorstore.qdrant.host", container.host)
        qdrantVectorStoreProperties.put("spring.ai.vectorstore.qdrant.port", container.grpcPort)
        event.environment.propertySources.addFirst(
            PropertiesPropertySource(
                "qdrantVectorStoreProperties",
                qdrantVectorStoreProperties,
            ),
        )
    }

    override fun destroy() {
        container.stop()
    }
}
