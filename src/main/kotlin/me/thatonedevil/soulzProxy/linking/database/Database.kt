package me.thatonedevil.soulzProxy.linking.database

import me.thatonedevil.soulzProxy.utils.Config.getMessage
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val dbUrl = getMessage("database.jdbcString")

    fun getConnection(): Connection? =
        runCatching {
            Class.forName("com.mysql.cj.jdbc.Driver")
            DriverManager.getConnection(dbUrl)
        }.onFailure {
            println("Database connection error: ${it.message}")
        }.getOrNull()
}
