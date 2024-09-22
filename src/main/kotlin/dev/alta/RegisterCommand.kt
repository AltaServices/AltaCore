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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }

        val player = sender

        if (args.isEmpty()) {
            player.sendColored("&cMissing argument.")
            player.sendColored("&cCorrect usage: &e/register <email>")
            return true
        }

        val mail = args[0]

        if (!EMAIL_VALIDATE_PATTERN.matcher(mail).matches()) {
            player.sendColored("&cThat is not a valid email address.")
            player.sendColored("&cPlease enter a valid email address!")
            return true
        }

        try {
            Services.registerPlayer(player.uniqueId, mail)
            player.sendColored("&aRegistration process started. Please check your email.")
        } catch (e: Exception) {
            player.sendColored("&cAn error occurred during registration. Please try again later.")
            e.printStackTrace()
        }

        return true
    }
}