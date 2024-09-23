package dev.alta

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Filters
import dev.alta.database.Mongo
import org.bson.Document
import org.bukkit.configuration.file.FileConfiguration
import java.util.Properties
import java.util.UUID
import java.time.Instant
import java.time.temporal.ChronoUnit
import jakarta.mail.*
import jakarta.mail.internet.*
import org.bukkit.entity.Player

/**
 * @author Cupoftea
 * @date 9/21/2024
 */

object Services {
    private const val REGISTRATION_COOLDOWN_MINUTES = 15

    fun registerPlayer(player: Player, email: String) {
        val registrationStatus = checkRegistrationStatus(player.uniqueId)
        
        when (registrationStatus) {
            RegistrationStatus.FULLY_REGISTERED -> {
                player.sendColored("&cYou are already registered.")
                return
            }
            RegistrationStatus.PENDING_REGISTRATION -> {
                val lastRegistrationAttempt = getLastRegistrationAttempt(player.uniqueId)
                if (lastRegistrationAttempt != null && 
                    Instant.now().isBefore(lastRegistrationAttempt.plus(REGISTRATION_COOLDOWN_MINUTES.toLong(), ChronoUnit.MINUTES))) {
                    player.sendColored("&cPlease wait before requesting a new registration email.")
                    return
                }
            }
            RegistrationStatus.NOT_REGISTERED -> {
            }
        }

        val token = generateToken()

        Mongo.storeUserInfo(player.uniqueId, player.name, email)

        val collection: MongoCollection<Document> = Mongo.getOrCreateCollection("altaCore")
        val document = Document()
            .append("pendingRegistrations.$token", Document()
                .append("uuid", player.uniqueId.toString())
                .append("timestamp", Instant.now().toString())
            )

        collection.updateOne(
            Filters.eq("_id", "pendingRegistrations"),
            Document("\$set", document),
            UpdateOptions().upsert(true)
        )

        sendRegistrationEmail(email, token)
        player.sendColored("&aRegistration email sent. Please check your inbox.")
    }

    fun checkRegistrationStatus(uuid: UUID): RegistrationStatus {
        val usersCollection = Mongo.getOrCreateCollection("users")
        val pendingRegistrationsCollection = Mongo.getOrCreateCollection("altaCore")

        val userDocument = usersCollection.find(Filters.eq("uuid", uuid.toString())).first()
        if (userDocument != null && userDocument.containsKey("password")) {
            return RegistrationStatus.FULLY_REGISTERED
        }

        val pendingRegistration = pendingRegistrationsCollection
            .find(Filters.eq("_id", "pendingRegistrations"))
            .first()
            ?.get("pendingRegistrations", Document::class.java)
            ?.entries
            ?.find { (_, value) -> (value as Document)["uuid"] == uuid.toString() }

        return if (pendingRegistration != null) {
            RegistrationStatus.PENDING_REGISTRATION
        } else {
            RegistrationStatus.NOT_REGISTERED
        }
    }

    private fun getLastRegistrationAttempt(uuid: UUID): Instant? {
        val pendingRegistrationsCollection = Mongo.getOrCreateCollection("altaCore")
        val pendingRegistration = pendingRegistrationsCollection
            .find(Filters.eq("_id", "pendingRegistrations"))
            .first()
            ?.get("pendingRegistrations", Document::class.java)
            ?.entries
            ?.find { (_, value) -> (value as Document)["uuid"] == uuid.toString() }

        return pendingRegistration?.let { (_, value) ->
            Instant.parse((value as Document)["timestamp"].toString())
        }
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
                val websiteUrl = plugin.config.getString("website.url") ?: "http://localhost"
                val websitePort = plugin.config.getInt("website.port", -1)
                val registrationLink = if (websitePort > 0) {
                    "$websiteUrl:$websitePort/register?token=$token"
                } else {
                    "$websiteUrl/register?token=$token"
                }
                val emailContent = Chat.color("&aClick the following link to complete your registration: &e$registrationLink")
                setText(emailContent)
            }

            Transport.send(message)
            plugin.logger.info("Registration email sent to $email")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to send email to $email: ${e.message}")
        }
    }
}

enum class RegistrationStatus {
    FULLY_REGISTERED,
    PENDING_REGISTRATION,
    NOT_REGISTERED
}