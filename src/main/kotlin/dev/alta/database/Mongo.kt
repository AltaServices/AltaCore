package dev.alta.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import dev.alta.AltaCore
import org.bson.Document

object Mongo {
    private lateinit var database: MongoDatabase
    private lateinit var client: MongoClient

    fun initialize(plugin: AltaCore) {
        val uri = plugin.config.getString("database.uri")
            ?: throw IllegalStateException("Database URI is not set in the config")
        val databaseName = plugin.config.getString("database.name")
            ?: throw IllegalStateException("Database name is not set in the config")

        client = MongoClients.create(uri)
        database = client.getDatabase(databaseName)
        plugin.logger.info("Successfully connected to MongoDB database")
    }

    fun getOrCreateCollection(name: String): MongoCollection<Document> {
        val collectionNames = database.listCollectionNames().into(mutableListOf())
        if (!collectionNames.contains(name)) {
            database.createCollection(name)
        }
        return database.getCollection(name)
    }

    fun close() {
        if (::client.isInitialized) {
            client.close()
        }
    }
}