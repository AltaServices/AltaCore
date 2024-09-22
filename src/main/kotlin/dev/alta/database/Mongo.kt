package dev.alta.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import dev.alta.AltaCore
import org.bson.Document
import java.util.UUID

object Mongo {
    private lateinit var database: MongoDatabase
    private lateinit var client: MongoClient

    fun initialize(plugin: AltaCore) {
        val uri = plugin.config.getString("database.uri")
            ?: throw IllegalStateException("Database URI is not set in the config")
        val databaseName = plugin.config.getString("database.name")
            ?: throw IllegalStateException("Database name is not set in the config")
        val username = plugin.config.getString("database.username")
        val password = plugin.config.getString("database.password")

        val connectionString = if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            uri
        } else {
            "mongodb://$username:$password@${uri.removePrefix("mongodb://")}"
        }

        client = MongoClients.create(connectionString)
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

    fun storeUserInfo(uuid: UUID, username: String, email: String) {
        val collection = getOrCreateCollection("users")
        val document = Document()
            .append("uuid", uuid.toString())
            .append("username", username)
            .append("email", email)

        collection.insertOne(document)
    }
}