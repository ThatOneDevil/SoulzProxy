package me.thatonedevil.soulzProxy.linking

import com.google.common.io.ByteStreams
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import me.thatonedevil.soulzProxy.JdaManager.guild
import me.thatonedevil.soulzProxy.JdaManager.jdaEnabled
import me.thatonedevil.soulzProxy.JdaManager.verifiedRole
import me.thatonedevil.soulzProxy.linking.database.DataManager
import me.thatonedevil.soulzProxy.linking.database.DataManager.isLinked
import me.thatonedevil.soulzProxy.utils.Config.getServerSpecificMessage
import me.thatonedevil.soulzProxy.utils.Utils
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import java.awt.Color


class LinkEmbed(var proxy: ProxyServer) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!jdaEnabled) return

        if (event.name == "linkembed") {
            if (!event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("❌ You do not have permission to use this command!").setEphemeral(true).queue()
                return
            }

            val embed = EmbedBuilder()
                .setTitle("🔗 Link Your Minecraft Account")
                .setColor(Color(46, 204, 113)) // Nice green color for linking success
                .setThumbnail("https://cdn.discordapp.com/avatars/1237115078038524015/dc6e4719b07fe06cffdc6ca68ead806f.webp?size=100")
                .setDescription(
                    "⚡ **Easily connect your Discord & Minecraft accounts!**\n\n" +
                            "Follow the steps below to complete the linking process."
                )
                .addField("📌 **Step 1:**", "Use `/link` in Minecraft to generate a unique code.", false)
                .addField("📌 **Step 2:**", "Click the button below and enter your 6-digit code.", false)
                .addField("📌 **Step 3:**", "If the code is correct, your accounts will be linked automatically!", false)
                .setFooter("SoulzSteal Linking System • ThatOneDevil", null)
                .build()

            val button: Button = Button.primary("link", "Click to link")

            event.channel.sendMessageEmbeds(embed).addActionRow(button).queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!jdaEnabled) return

        if (event.componentId == "link") {
            val codeInput = TextInput.create("code_input", "Put code here", TextInputStyle.SHORT)
                .setPlaceholder("Enter your 6-digit code")
                .setRequired(true)
                .setMinLength(6)
                .setMaxLength(6)
                .build()

            val modal = Modal.create("link_modal", "Link Your Minecraft Account")
                .addActionRow(codeInput)
                .build()

            event.replyModal(modal).queue()
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (!jdaEnabled) return

        if (event.modalId == "link_modal") {
            val code = event.getValue("code_input")?.asString ?: return

            val uuid = LinkCommand.getUUIDFromCode(code) // Retrieve UUID from code

            if (isLinked(event.user.id)) {
                event.reply("❌ User is already linked to an account!").setEphemeral(true).queue()
                return
            }

            if (uuid == null) {
                event.reply("❌ Invalid or expired code!").setEphemeral(true).queue()
                return
            }
            val player = proxy.getPlayer(uuid).get()

            val playerName = player.username

            val embed = EmbedBuilder()
                .setTitle("✅ Account Linked Successfully!")
                .setColor(Color.GREEN)
                .setDescription("Your Discord account has been linked to your Minecraft profile.")
                .addField("🔗 Discord: ", "**${event.user.name}**", false)
                .addField("🎮 Minecraft: ", "**${playerName}**", false)
                .setThumbnail("https://cravatar.eu/head/${uuid}.png")
                .setFooter("Linking system by ThatOneDevil", null)
                .setTimestamp(java.time.Instant.now())
                .build()

            event.replyEmbeds(embed).setEphemeral(true).queue()

            val serverConnection = player.currentServer.get()
            val message = getServerSpecificMessage("messages.linkCommand.linkedBroadcast", serverConnection)
            val formattedMessage = message
                .replace("<player>", playerName)

            val miniMessageFormatted = convertLegacyToMiniMessage(formattedMessage)
            serverConnection.server.sendMessage(miniMessageFormatted)

            val data = DataManager.getPlayerData(player)
            data.linked = true
            data.userId = event.user.id

            player.uniqueId
            guild?.addRoleToMember(event.user, verifiedRole!!)?.queue()

            val out = ByteStreams.newDataOutput()
            out.writeUTF(player.username)
            out.writeUTF(event.user.id)
            out.writeBoolean(true)

            Utils.sendPluginMessageToBackendUsingPlayer(player, MinecraftChannelIdentifier.from("soulzproxy:main"), out.toByteArray());

            DataManager.savePlayerData(data)
            LinkCommand.removeCode(code)

        }
    }

}