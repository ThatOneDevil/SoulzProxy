package me.thatonedevil.soulzProxy.linking

import com.velocitypowered.api.proxy.Player
import me.thatonedevil.soulzProxy.utils.Config.getMessage
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.CompletableFuture

object DataManager {

    private val dbUrl = getMessage("database.jdbcString")

    private fun getConnection(): Connection? =
        runCatching {
            Class.forName("com.mysql.cj.jdbc.Driver")
            DriverManager.getConnection(dbUrl) }
            .onFailure { println("Database connection error: ${it.message}") }
            .getOrNull()

    fun createTable() {
        getConnection()?.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS linking_data (
                        uuid CHAR(36) PRIMARY KEY,
                        name CHAR(50) NOT NULL,
                        linked BOOLEAN NOT NULL DEFAULT FALSE,
                        user_id VARCHAR(50) NOT NULL
                    );
                    """
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS linking_codes (
                        code VARCHAR(10) PRIMARY KEY,
                        uuid CHAR(36) NOT NULL
                    );
                    """
                )
            }
        }
    }

    fun storeLinkingCode(code: String, uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            getConnection()?.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO linking_codes (code, uuid) VALUES (?, ?)"
                ).use { statement ->
                    statement.setString(1, code)
                    statement.setString(2, uuid.toString())
                    statement.executeUpdate()
                }
            }
        }
    }

    fun getUUIDFromCode(code: String): UUID? {
        return getConnection()?.use { connection ->
            connection.prepareStatement(
                "SELECT uuid FROM linking_codes WHERE code = ?"
            ).use { statement ->
                statement.setString(1, code)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) UUID.fromString(resultSet.getString("uuid")) else null
                }
            }
        }
    }

    fun removeLinkingCode(code: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            getConnection()?.use { connection ->
                connection.prepareStatement("DELETE FROM linking_codes WHERE code = ?").use { statement ->
                    statement.setString(1, code)
                    statement.executeUpdate()
                }
            }
        }
    }

    fun savePlayerData(playerData: LinkingData): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            getConnection()?.use { connection ->
                connection.prepareStatement(
                    """
                    INSERT INTO linking_data (uuid, name, linked, user_id)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE name = VALUES(name), linked = VALUES(linked), user_id = VALUES(user_id)
                    """
                ).use { statement ->
                    statement.setString(1, playerData.uuid.toString())
                    statement.setString(2, playerData.name)
                    statement.setBoolean(3, playerData.linked)
                    statement.setString(4, playerData.userId)
                    statement.executeUpdate()
                }
            }
        }
    }

    fun deletePlayerData(uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            getConnection()?.use { connection ->
                connection.prepareStatement("DELETE FROM linking_data WHERE uuid = ?").use { statement ->
                    statement.setString(1, uuid.toString())
                    statement.executeUpdate()
                }
            }
        }
    }

    fun loadPlayerData(uuid: UUID): LinkingData? {
        return getConnection()?.use { connection ->
            connection.prepareStatement("SELECT * FROM linking_data WHERE uuid = ?").use { statement ->
                statement.setString(1, uuid.toString())
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        LinkingData(
                            uuid = UUID.fromString(resultSet.getString("uuid")),
                            name = resultSet.getString("name"),
                            linked = resultSet.getBoolean("linked"),
                            userId = resultSet.getString("user_id")
                        )
                    } else null
                }
            }
        }
    }

    fun getUUIDFromDiscordId(discordId: String): UUID? {
        return getConnection()?.use { connection ->
            connection.prepareStatement(
                "SELECT uuid FROM linking_data WHERE user_id = ?"
            ).use { statement ->
                statement.setString(1, discordId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) UUID.fromString(resultSet.getString("uuid")) else null
                }
            }
        }
    }

    fun clearAllData(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            getConnection()?.use { connection ->
                connection.prepareStatement("DELETE FROM linking_data").executeUpdate()
            }
        }
    }

    fun getPlayerData(player: Player): LinkingData {
        return loadPlayerData(player.uniqueId) ?: LinkingData(player.uniqueId, player.username)
    }

    fun isLinked(id: String): Boolean {
        return getConnection()?.use { connection ->
            connection.prepareStatement(
                "SELECT 1 FROM linking_data WHERE user_id = ?"
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    resultSet.next() // Returns true if there's at least one row, meaning the ID exists
                }
            }
        } ?: false // If the connection fails or is not `useMySQL`, return false
    }

}