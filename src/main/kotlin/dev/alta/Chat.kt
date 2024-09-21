package dev.alta

import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @author TastyCake
 * @date 9/21/2024
 */
object Chat {
    fun color(s: String): String {
        return ChatColor.translateAlternateColorCodes('&', s)
    }
}

fun Player.sendColored(s: String) {
    this.sendMessage(Chat.color(s))
}