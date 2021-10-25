package no.nav.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.execution.GraphQLServer
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import graphql.GraphQL
import io.ktor.request.*

class KtorGraphQLServer(
    requestParser: KtorGraphQLRequestParser,
    contextFactory: KtorGraphQLContextFactory,
    requestHandler: GraphQLRequestHandler
) : GraphQLServer<ApplicationRequest>(requestParser, contextFactory, requestHandler)

fun ktorGraphQLServer(): KtorGraphQLServer {
    val schemaGeneratorConfig = SchemaGeneratorConfig(supportedPackages = listOf("no.nav.graphql"))
    val schema = toSchema(
        schemaGeneratorConfig,
        queries = listOf(
            TopLevelObject(ProductsQuery())
        ),
        mutations = listOf(
            TopLevelObject(ProductsMutation())
        )

    )
    val graphQL = GraphQL.newGraphQL(schema).build()
    return KtorGraphQLServer(
        KtorGraphQLRequestParser,
        KtorGraphQLContextFactory,
        GraphQLRequestHandler(graphQL)
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
