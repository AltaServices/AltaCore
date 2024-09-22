package dev.alta

import dev.alta.database.Mongo
import org.bukkit.plugin.java.JavaPlugin

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

        Mongo
        RegisterCommand()
    }

    override fun onDisable() {

    }
}
