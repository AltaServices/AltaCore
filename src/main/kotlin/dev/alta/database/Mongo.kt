package dev.alta.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import dev.alta.AltaCore
import org.bson.Document

/**
 * @author TastyCake
 * @date 9/21/2024
 */
object Mongo {
    lateinit var database: MongoDatabase
    lateinit var client: MongoClient

    init {
        val plugin = AltaCore.instance
        try {
            val host = plugin.config.getString("database.host")
            val port = plugin.config.getInt("database.port")
            val databaseName = plugin.config.getString("database.name")

            client = MongoClients.create("mongodb://$host:$port")
            database = client.getDatabase(databaseName)
        } catch (e: Exception) {
            plugin.logger.severe("Couldn't connect to Mongo Database!")
        }
    }

    fun getOrCreateCollection(name: String): MongoCollection<Document> {
        val collectionNames = database.listCollectionNames().into(mutableListOf())
        if (!collectionNames.contains(name)) {
            database.createCollection(name)
        }
        return database.getCollection(name)
    }
}