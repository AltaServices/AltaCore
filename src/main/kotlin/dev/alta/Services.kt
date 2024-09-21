package dev.alta

import org.bukkit.configuration.file.FileConfiguration
import java.util.Properties
import java.util.UUID
import javax.mail.*
import javax.mail.internet.*

/**
 * @author TastyCake
 * @date 9/21/2024
 */
object Services {
    fun registerPlayer(uuid: UUID, email: String) {
        val plugin = AltaCore.instance
        val token = generateToken()
        val config: FileConfiguration = plugin.config
        config.set("pendingRegistrations.$token", uuid.toString())
        plugin.saveConfig()
        sendRegistrationEmail(email, token)
    }

    private fun generateToken(): String = UUID.randomUUID().toString()

    private fun sendRegistrationEmail(email: String, token: String) {
        val plugin = AltaCore.instance
        val host = plugin.config.getString("smtp.host")
        val port = plugin.config.getString("smtp.port")
        val username = plugin.config.getString("smtp.username")
        val password = plugin.config.getString("smtp.password")
        val from = plugin.config.getString("smtp.from")

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(from))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                subject = "Complete Your Registration"
                val registrationLink = "${plugin.config.getString("website.url")}/register?token=$token"
                val emailContent = Chat.color("&aClick the following link to complete your registration: &e$registrationLink")
                setText(emailContent.toString())
            }

            Transport.send(message)
            plugin.logger.info("Registration email sent to $email")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to send email to $email: ${e.message}")
        }
    }
}