package no.nav.graphql

import com.expediagroup.graphql.server.execution.KotlinDataLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object TechnicalDataTable : Table("technical_data") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val value = text("value")
    val productId = integer("product_id") references ProductTable.id

    override val primaryKey = PrimaryKey(id)

    fun findByProductId(productId: Int): List<TechnicalData> =
        TechnicalDataTable
            .select { TechnicalDataTable.productId eq productId }
            .map {
                TechnicalData(
                    it[id],
                    it[name],
                    it[value]
                )
            }
}

data class TechnicalData(
    val id: Int,
    val name: String,
    val value: String,
)

object TechnicalDataDataLoader : KotlinDataLoader<Int, List<TechnicalData>> {
    override val dataLoaderName: String = "TechnicalDataDataLoader"

    override fun getDataLoader(): DataLoader<Int, List<TechnicalData>> = DataLoaderFactory.newMappedDataLoader { ids ->
        GlobalScope.future {
            transaction {
                val technicalDataByProductId = TechnicalDataTable
                    .select { TechnicalDataTable.id inList ids }
                    .groupBy({ it[TechnicalDataTable.productId] }, {
                        TechnicalData(
                            it[TechnicalDataTable.id],
                            it[TechnicalDataTable.name],
                            it[TechnicalDataTable.value]
                        )
                    })
                    .toMutableMap()
                ids.forEach {
                    technicalDataByProductId.computeIfAbsent(it) { emptyList() }
                }
                technicalDataByProductId
            }
        }
    }
}
