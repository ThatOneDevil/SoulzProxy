package me.thatonedevil.soulzProxy.utils

import com.velocitypowered.api.proxy.ServerConnection
import me.thatonedevil.soulzProxy.SoulzProxy
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.CompletableFuture
import kotlin.io.path.Path
import kotlin.io.path.pathString

object Config {
    private val configPath: Path = Path("${SoulzProxy.instance.dataDirectory.pathString}/config.yml")
    private var messages: Map<String, Any>? = null

    fun loadConfigAsync(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (!Files.exists(configPath)) {
                saveDefaultConfigAsync().join()
            }
            try {
                Files.newInputStream(configPath).use { input ->
                    val yaml = Yaml()
                    messages = yaml.load(input)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getMessage(path: String): String {
        val keys = path.split(".")
        var current: Any? = messages
        for (key in keys) {
            if (current is Map<*, *>) {
                current = current[key]
            } else {
                return "Message not found"
            }
        }
        return current as? String ?: "Message not found"
    }

    fun getServerSpecificMessage(message: String, serverConnection: ServerConnection): String {
        val serverName = serverConnection.serverInfo.name
        val serverMessage = getMessage(message)
            .replace("<primary>", getMessage("messages.colour.${serverName}.primary"))
            .replace("<secondary>", getMessage("messages.colour.${serverName}.secondary"))

        return serverMessage

    }

    private fun saveDefaultConfigAsync(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val input: InputStream? = javaClass.classLoader.getResourceAsStream("config.yml")
            if (input == null) {
                System.err.println("Default config.yml not found in resources!")
                return@runAsync
            }
            try {
                Files.createDirectories(configPath.parent)
                Files.copy(input, configPath, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
