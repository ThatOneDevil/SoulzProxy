package me.thatonedevil.soulzProxy.linking

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.thatonedevil.soulzProxy.JdaManager.guild
import me.thatonedevil.soulzProxy.JdaManager.verifiedRole
import me.thatonedevil.soulzProxy.linking.database.DataManager
import me.thatonedevil.soulzProxy.linking.database.DataManager.isLinked
import me.thatonedevil.soulzProxy.utils.Config.getServerSpecificMessage
import me.thatonedevil.soulzProxy.utils.MessagingUtils
import me.thatonedevil.soulzProxy.utils.Utils.convertLegacyToMiniMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import java.awt.Color

class LinkEmbed(private val proxy: ProxyServer) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "linkembed") return
        if (!event.member!!.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("❌ You do not have permission to use this command!").setEphemeral(true).queue()
            return
        }

        event.channel.sendMessageEmbeds(buildLinkEmbed())
            .addActionRow(Button.primary("link", "Click to link"))
            .queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != "link") return

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

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId != "link_modal") return

        val code = event.getValue("code_input")?.asString ?: return
        handleLinking(event, code)
    }

    private fun buildLinkEmbed() = EmbedBuilder().apply {
        setTitle("🔗 Link Your Minecraft Account")
        setColor(Color(46, 204, 113))
        setThumbnail("https://github.com/ThatOneDevil/SoulzProxy/blob/42fafdf2bf6666fabfb6d7ce79a7a11c7a70506d/images/soulz-logo.png?raw=true")
        setDescription("⚡ **Easily connect your Discord & Minecraft accounts!**\n\nFollow the steps below to complete the linking process.")
        addField("📌 **Step 1:**", "Use `/link` in **Minecraft** to generate a unique code.", false)
        addField("📌 **Step 2:**", "Click the button below and enter your **6-digit code**.", false)
        addField("📌 **Step 3:**", "If the code is correct, your accounts will be linked **automatically**!", false)
        addField("📌 **Step 4:**", "Claim your reward using `/linkclaim` in **Minecraft**", false)
        setFooter("SoulzSteal Linking System • ThatOneDevil", null)
    }.build()

    private fun handleLinking(event: ModalInteractionEvent, code: String) {
        if (isLinked(event.user.id)) {
            event.reply("❌ User is already linked to an account!").setEphemeral(true).queue()
            return
        }

        val uuid = LinkCommand.getUUIDFromCode(code)
        if (uuid == null) {
            event.reply("❌ Invalid or expired code!").setEphemeral(true).queue()
            return
        }

        val player = proxy.getPlayer(uuid).orElse(null) ?: run {
            event.reply("❌ Player not found!").setEphemeral(true).queue()
            return
        }

        linkPlayerToDiscord(event, player, code)
    }

    private fun linkPlayerToDiscord(event: ModalInteractionEvent, player: Player, code: String) {
        val playerName = player.username

        val embed = EmbedBuilder().apply {
            setTitle("✅ Account Linked Successfully!")
            setColor(Color.GREEN)
            setDescription("Your Discord account has been linked to your Minecraft profile.")
            addField("🔗 Discord: ", "**${event.user.name}**", false)
            addField("🎮 Minecraft: ", "**${playerName}**", false)
            setThumbnail("https://cravatar.eu/head/${player.uniqueId}.png")
            setFooter("Linking system by ThatOneDevil", null)
            setTimestamp(java.time.Instant.now())
        }.build()

        event.replyEmbeds(embed).setEphemeral(true).queue()

        broadcastLinkMessage(player, playerName)
        updatePlayerData(player, event.user.id)
        MessagingUtils.notifyBackend(player)
        assignVerifiedRole(event.user)
        LinkCommand.removeCode(code)
    }

    private fun broadcastLinkMessage(player: Player, playerName: String) {
        val serverConnection = player.currentServer.get()
        val message = getServerSpecificMessage("messages.linkCommand.linkedBroadcast", serverConnection)
        val formatted = convertLegacyToMiniMessage(message.replace("<player>", playerName))
        serverConnection.server.sendMessage(formatted)
    }

    private fun updatePlayerData(player: Player, discordId: String) {
        val data = DataManager.getPlayerData(player)
        data.linked = true
        data.userId = discordId
        DataManager.savePlayerData(data)
    }

    private fun assignVerifiedRole(user: User) {
        guild?.addRoleToMember(user, verifiedRole!!)?.queue()
    }
}
