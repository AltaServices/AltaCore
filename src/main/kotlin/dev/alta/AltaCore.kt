package dev.alta

import dev.alta.database.Mongo
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import com.mongodb.MongoSecurityException
import com.mongodb.MongoTimeoutException

/**
 * @author TastyCake
 * @date 9/21/2024
 */

class AltaCore : JavaPlugin() {
    companion object {
        @JvmStatic
        lateinit var instance: AltaCore
    }

    override fun onEnable() {
        instance = this

        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            saveDefaultConfig()
        }

        reloadConfig()

        try {
            Mongo.initialize(this)
        } catch (e: Exception) {
            when (e) {
                is com.mongodb.MongoSecurityException -> {
                    logger.severe("Failed to authenticate with MongoDB. Please check your username and password in the config.yml file.")
                }
                is com.mongodb.MongoTimeoutException -> {
                    logger.severe("Timed out while connecting to MongoDB. Please check your database URI and ensure the server is running.")
                }
                else -> {
                    logger.severe("Failed to initialize database connection: ${e.message}")
                }
            }
            logger.severe("Disabling plugin due to database connection failure.")
            server.pluginManager.disablePlugin(this)
            return
        }

        getCommand("register")?.setExecutor(RegisterCommand())

        logger.info("AltaCore has been enabled!")
    }

    override fun onDisable() {
        Mongo.close()
        logger.info("AltaCore has been disabled!")
    }
}
