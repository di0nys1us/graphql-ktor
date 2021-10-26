package no.nav.graphql

import com.expediagroup.graphql.server.execution.GraphQLServer
import com.expediagroup.graphql.server.types.GraphQLResponse
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File

suspend fun ApplicationCall.respondResource(name: String) {
    when (val resource = javaClass.getResource(name)) {
        null -> {
            respond(HttpStatusCode.NotFound, "$name not found")
        }
        else -> {
            respondFile(File(resource.toURI()))
        }
    }
}

fun Route.graphql(server: GraphQLServer<ApplicationRequest>) {
    post("/graphql") {
        when (val response = server.execute(call.request)) {
            is GraphQLResponse<*> -> {
                call.respond(response)
            }
            else -> call.respond(HttpStatusCode.NotImplemented, "GraphQLBatchResponse not implemented")
        }
    }
}

fun Route.graphqlPlayground() {
    get("/graphql") {
        call.respondResource("/playground.html")
    }
}

fun Route.graphqlVoyager() {
    get("/voyager") {
        call.respondResource("/voyager.html")
    }
}

fun Application.graphQLModule() {
    install(Routing)
    install(ContentNegotiation) {
        jackson()
    }

    val server = ktorGraphQLServer()

    routing {
        graphql(server)
        graphqlPlayground()
        graphqlVoyager()
    }
}
