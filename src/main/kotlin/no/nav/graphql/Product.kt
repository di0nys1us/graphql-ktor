package no.nav.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Products : Table() {
    val id = integer("id").autoIncrement()
    val name = text("name")

    override val primaryKey = PrimaryKey(id)
}

@GraphQLDescription("A product")
data class Product(
    @GraphQLDescription("Product ID") val id: Int,
    @GraphQLDescription("Product name") val name: String
)

class ProductsQuery : Query {
    @GraphQLDescription("Fetch all products")
    fun products(limit: Int?, offset: Int?) = transaction {
        Products.selectAll().limit(limit ?: 20, (offset ?: 0).toLong()).map {
            Product(it[Products.id], it[Products.name])
        }
    }
}

class ProductsMutation : Mutation {
    @GraphQLDescription("Save a single product")
    fun saveProduct(productName: String) = transaction {
        val productId = Products.insert {
            it[name] = productName
        } get Products.id
        Product(productId, productName)
    }
}
