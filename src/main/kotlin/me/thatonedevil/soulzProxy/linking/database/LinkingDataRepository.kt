package me.thatonedevil.soulzProxy.linking.database

import me.thatonedevil.soulzProxy.linking.LinkingData
import java.sql.Connection
import java.util.*
import java.util.concurrent.CompletableFuture

object LinkingDataRepository {

    fun createTable(connection: Connection) {
        connection.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS linking_data (
                    uuid CHAR(36) PRIMARY KEY,
                    name CHAR(50) NOT NULL,
                    linked BOOLEAN NOT NULL DEFAULT FALSE,
                    user_id VARCHAR(50) NOT NULL
                );
            """
            )
        }
    }

    fun save(data: LinkingData): CompletableFuture<Void> = CompletableFuture.runAsync {
        Database.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO linking_data (uuid, name, linked, user_id)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), linked = VALUES(linked), user_id = VALUES(user_id)
            """
            ).use {
                it.setString(1, data.uuid.toString())
                it.setString(2, data.name)
                it.setBoolean(3, data.linked)
                it.setString(4, data.userId)
                it.executeUpdate()
            }
        }
    }

    fun delete(uuid: UUID): CompletableFuture<Void> = CompletableFuture.runAsync {
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("DELETE FROM linking_data WHERE uuid = ?").use {
                it.setString(1, uuid.toString())
                it.executeUpdate()
            }
        }
    }

    fun load(uuid: UUID): LinkingData? =
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT * FROM linking_data WHERE uuid = ?").use {
                it.setString(1, uuid.toString())
                it.executeQuery().use { rs ->
                    if (rs.next()) {
                        LinkingData(
                            uuid = UUID.fromString(rs.getString("uuid")),
                            name = rs.getString("name"),
                            linked = rs.getBoolean("linked"),
                            userId = rs.getString("user_id")
                        )
                    } else null
                }
            }
        }

    fun getUUIDFromDiscord(discordId: String): UUID? =
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT uuid FROM linking_data WHERE user_id = ?").use {
                it.setString(1, discordId)
                it.executeQuery().use { rs ->
                    if (rs.next()) UUID.fromString(rs.getString("uuid")) else null
                }
            }
        }

    fun isLinked(userId: String): Boolean =
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT 1 FROM linking_data WHERE user_id = ?").use {
                it.setString(1, userId)
                it.executeQuery().use { rs -> rs.next() }
            }
        } ?: false

    fun clearAll(): CompletableFuture<Void> = CompletableFuture.runAsync {
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("DELETE FROM linking_data").executeUpdate()
        }
    }
}
