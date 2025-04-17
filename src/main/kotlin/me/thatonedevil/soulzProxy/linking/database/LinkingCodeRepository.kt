package me.thatonedevil.soulzProxy.linking.database

import java.sql.Connection
import java.util.*
import java.util.concurrent.CompletableFuture

object LinkingCodeRepository {

    fun createTable(connection: Connection) {
        connection.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS linking_codes (
                    code VARCHAR(10) NOT NULL,
                    uuid CHAR(36) PRIMARY KEY
                );
            """)
        }
    }

    fun store(code: String, uuid: UUID): CompletableFuture<Void> = CompletableFuture.runAsync {
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("""
                INSERT INTO linking_codes (code, uuid)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE code = VALUES(code)
            """).use {
                it.setString(1, code)
                it.setString(2, uuid.toString())
                it.executeUpdate()
            }
        }
    }

    fun getUUID(code: String): UUID? =
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("SELECT uuid FROM linking_codes WHERE code = ?").use {
                it.setString(1, code)
                it.executeQuery().use { rs ->
                    if (rs.next()) UUID.fromString(rs.getString("uuid")) else null
                }
            }
        }

    fun remove(code: String): CompletableFuture<Void> = CompletableFuture.runAsync {
        Database.getConnection()?.use { conn ->
            conn.prepareStatement("DELETE FROM linking_codes WHERE code = ?").use {
                it.setString(1, code)
                it.executeUpdate()
            }
        }
    }
}
