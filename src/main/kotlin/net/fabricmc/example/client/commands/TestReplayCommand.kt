package net.fabricmc.example.client.commands

import com.mojang.authlib.minecraft.client.ObjectMapper
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.ByteBuf
import net.fabricmc.example.client.extensions.into
import net.fabricmc.example.client.extensions.sendStringMessage
import net.fabricmc.example.client.utils.replay.PacketReplayer
import net.fabricmc.example.mixin.ConnectionAccessor
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.world.entity.player.Player
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class TestReplayCommand : BaseCommand("testreplay") {
    override fun execute(command: CommandContext<FabricClientCommandSource>): Int {
        val player = command.source.entity as Player?
        player?.sendStringMessage(">> starting test replay")

        // load saved packets
        val packetsFile = File(FileUtils.getUserDirectoryPath() + "/Downloads/packet_output/small_exmp.txt")
        if (!packetsFile.exists()) {
            player?.sendStringMessage(">> packets file not found")
            return Command.SINGLE_SUCCESS
        }

        // start up replayer thingy
        val packetReplayer = PacketReplayer()

        val objectMapper = ObjectMapper.create()
        val connectionProtocol = (packetReplayer.connection as ConnectionAccessor)
            .getChannel()
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
            packetReplayer.send(packet!!)
        }

        fileScanner.close()

        return Command.SINGLE_SUCCESS
    }
}