package dev.alta

import org.bukkit.plugin.java.JavaPlugin

class AltaCore : JavaPlugin() {
    companion object {
        @JvmStatic
        lateinit var instance: AltaCore
    }

    override fun onEnable() {
        instance = this

        RegisterCommand()
    }

    override fun onDisable() {

    }
}
