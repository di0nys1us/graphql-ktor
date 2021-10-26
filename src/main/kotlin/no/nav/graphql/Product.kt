package no.nav.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.extensions.getValueFromDataLoader
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CompletableFuture

object ProductTable : Table("product") {
    val id = integer("id").autoIncrement()
    val name = text("name")

    override val primaryKey = PrimaryKey(id)
}

@GraphQLDescription("A product")
data class Product(
    @GraphQLDescription("Product ID") val id: Int,
    @GraphQLDescription("Product name") val name: String,
) {
    fun technicalData(dataFetchingEnvironment: DataFetchingEnvironment): CompletableFuture<List<TechnicalData>> =
        dataFetchingEnvironment.getValueFromDataLoader(TechnicalDataDataLoader.dataLoaderName, this@Product.id)
}

class ProductsQuery : Query {
    @GraphQLDescription("Fetch all products")
    fun products(limit: Int?, offset: Int?): List<Product> = transaction {
        ProductTable
            .selectAll().limit(limit ?: 20, (offset ?: 0).toLong())
            .map {
                Product(it[ProductTable.id], it[ProductTable.name])
            }
    }
}

class ProductsMutation : Mutation {
    @GraphQLDescription("Save a single product")
    fun saveProduct(productName: String) = transaction {
        val productId = ProductTable.insert {
            it[name] = productName
        } get ProductTable.id
        Product(productId, productName)
    }
}
