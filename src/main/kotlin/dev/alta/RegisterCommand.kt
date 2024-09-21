package dev.alta

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

/**
 * @author TastyCake
 * @date 9/21/2024
 */
class RegisterCommand : CommandExecutor {
    companion object {
        val EMAIL_VALIDATE_PATTERN
                : Pattern = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    }

    init {
        AltaCore.instance.getCommand("register").executor = this
    }

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return true
        }

        val player = sender as Player

        val mail = args.pop(0)?: run {
            player.sendColored("&cMissing argument.")
            player.sendColored("&cCorrect usage: &e/register <mail>")
            return true
        }

        if (!EMAIL_VALIDATE_PATTERN.matcher(mail).matches()) {
            player.sendColored("&cThat is not a valid email address.")
            player.sendColored("&cPlease enter a valid email address!")
            return true
        }

        Services.registerPlayer(player.uniqueId, mail)

        return true
    }
}

fun Array<out String>.pop(index: Int): String? {
    if (index < size - 1) {
        return null
    }

    return this[index]
}