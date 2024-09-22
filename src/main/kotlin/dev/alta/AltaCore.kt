package dev.alta

import dev.alta.database.Mongo
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

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
            logger.severe("Failed to initialize database connection: ${e.message}")
            server.pluginManager.disablePlugin(this)
            return
        }

        RegisterCommand()

        logger.info("AltaCore has been enabled!")
    }

    override fun onDisable() {
        Mongo.close()
        logger.info("AltaCore has been disabled!")
    }
}
