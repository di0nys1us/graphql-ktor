package no.nav.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.*
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import graphql.GraphQL
import io.ktor.request.*
import java.util.*

class KtorGraphQLServer(
    requestParser: KtorGraphQLRequestParser,
    contextFactory: KtorGraphQLContextFactory,
    requestHandler: GraphQLRequestHandler,
) : GraphQLServer<ApplicationRequest>(requestParser, contextFactory, requestHandler)

fun ktorGraphQLServer(): KtorGraphQLServer {
    val queries = ServiceLoader.load(Query::class.java)
    val mutations = ServiceLoader.load(Mutation::class.java)
    val schemaGeneratorConfig = SchemaGeneratorConfig(supportedPackages = listOf("no.nav.graphql"))
    val schema = toSchema(
        schemaGeneratorConfig,
        queries = queries.map { TopLevelObject(it) },
        mutations = mutations.map { TopLevelObject(it) }
    )
    val graphQL = GraphQL.newGraphQL(schema).build()
    val dataLoaderRegistryFactory = DefaultDataLoaderRegistryFactory(listOf(
        TechnicalDataDataLoader,
    ))
    return KtorGraphQLServer(
        KtorGraphQLRequestParser,
        KtorGraphQLContextFactory,
        GraphQLRequestHandler(graphQL, dataLoaderRegistryFactory)
    )
}

object KtorGraphQLRequestParser : GraphQLRequestParser<ApplicationRequest> {
    override suspend fun parseRequest(request: ApplicationRequest): GraphQLServerRequest {
        return request.call.receive()
    }
}

data class KtorGraphQLContext(val uid: String) : GraphQLContext

object KtorGraphQLContextFactory : GraphQLContextFactory<KtorGraphQLContext, ApplicationRequest> {
    override suspend fun generateContext(request: ApplicationRequest): KtorGraphQLContext {
        return KtorGraphQLContext("graphql-ktor")
    }
}
