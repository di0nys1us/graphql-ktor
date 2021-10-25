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

fun Route.graphql(server: GraphQLServer<ApplicationRequest>) {
    post("/graphql") {
        when (val response = server.execute(call.request)) {
            is GraphQLResponse<*> -> {
                call.respond(response)
            }
            else -> call.response.status(HttpStatusCode.NotImplemented)
        }
    }
}

fun Route.graphqlPlayground(playgroundHtml: String) {
    get("/graphql") {
        call.respondText(playgroundHtml, ContentType.Text.Html)
    }
}

fun Application.graphQLModule() {
    install(Routing)
    install(ContentNegotiation) {
        jackson()
    }

    val server = ktorGraphQLServer()
    val playgroundHtml = javaClass.getResource("/playground.html")!!.readText()

    routing {
        graphql(server)
        graphqlPlayground(playgroundHtml)
    }
}
