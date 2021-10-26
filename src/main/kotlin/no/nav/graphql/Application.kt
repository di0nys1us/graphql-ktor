package no.nav.graphql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val hikariConfig = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://localhost:5432/graphql-ktor"
        username = "graphql-ktor"
        password = "graphql-ktor"
    }

    Database.connect(HikariDataSource(hikariConfig))

    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(ProductTable, TechnicalDataTable)
    }
}
