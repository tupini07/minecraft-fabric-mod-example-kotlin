package net.fabricmc.example.server.commands

import com.mojang.authlib.minecraft.client.ObjectMapper
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.ByteBuf
import net.fabricmc.example.client.extensions.into
import net.fabricmc.example.client.extensions.sendStringMessage
import net.fabricmc.example.common.commands.BaseCommand
import net.fabricmc.example.mixin.ConnectionAccessor
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.world.entity.player.Player
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class TestReplayCommand : BaseCommand<CommandSourceStack>("testreplay") {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal(command)
                .executes { context ->
                    execute(context!!)
                }
        )
    }

    override fun execute(command: CommandContext<CommandSourceStack>): Int {
        val player = command.source.entity as Player?
        player?.sendStringMessage(">> starting test replay")

        // load saved packets
        val packetsFile = File(FileUtils.getUserDirectoryPath() + "/Downloads/packet_output/small_exmp.txt")
        if (!packetsFile.exists()) {
            player?.sendStringMessage(">> packets file not found")
            return Command.SINGLE_SUCCESS
        }

        // start up replayer thingy
//        val packetReplayer = PacketReplayer()

        val objectMapper = ObjectMapper.create()

        val conn = Minecraft
            .getInstance()
            .connection!!
            .connection!! as ConnectionAccessor

        val connectionProtocol = conn.getChannel().pipeline().channel()!!
            .attr(Connection.ATTRIBUTE_PROTOCOL)
            .get()!!

        val fileScanner = Scanner(packetsFile)
        while (fileScanner.hasNextLine()) {
            val line = fileScanner.nextLine()

            // decode packet
            val byteBuf = objectMapper
                .readValue(line, ByteBuffer::class.java)
                .into<ByteBuf>()

            val friendlyByteBuf = FriendlyByteBuf(byteBuf)
            val j = friendlyByteBuf.readVarInt()

            val packet = connectionProtocol.createPacket(PacketFlow.CLIENTBOUND, j, friendlyByteBuf)

            //send packet
//            packetReplayer.send(packet!!)
            Minecraft.getInstance().connection!!.connection.send(packet!!)
        }

        fileScanner.close()

        return Command.SINGLE_SUCCESS
    }
}