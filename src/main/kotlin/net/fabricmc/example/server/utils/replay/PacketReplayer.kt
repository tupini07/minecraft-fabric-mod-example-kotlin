package net.fabricmc.example.server.utils.replay

import net.fabricmc.example.mixin.ConnectionAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerHandshakePacketListenerImpl

class PacketReplayer {
    val replaySpeed = 1.0
    val connection: Connection

    init {
        ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick { clientLevel ->
            this.onWorldTick(
                clientLevel
            )
        })

        val networkManager = Connection(PacketFlow.CLIENTBOUND)

        networkManager.setListener(ServerHandshakePacketListenerImpl(
            Minecraft.getInstance().singleplayerServer!!,
            networkManager
        ))

//        val channel = EmbeddedChannel()
        val conn = Minecraft
            .getInstance()
            .connection!!
            .connection!! as ConnectionAccessor

        val channel = conn.getChannel().pipeline().channel()!!

        // TODO we also have the option to 'replace'
        channel.pipeline().remove("packet_handler")

        channel.pipeline().addFirst("packet_handler", networkManager)
        channel.pipeline().addFirst("custom_replay_sender", ReplaySender())

        channel.pipeline().fireChannelActive()

        connection = Minecraft
            .getInstance()
            .connection!!
            .connection

        // ChunkManager -> ChunkSource in Mojang mappings
//        net.minecraft.world.level.chunk.ChunkSource

//        Minecraft.getInstance().cameraEntity!!.setpos
//        net.minecraft.network.protocol.game.

        //        Minecraft.getInstance().connection!!.send()
    }

    fun send(packet: Packet<*>) {
        connection.send(packet)
    }

    private fun onWorldTick(level: ClientLevel) {
        // TODO this should happen on server world tick,
        /*
             if (mc.world != null) {
                for (PlayerEntity playerEntity : mc.world.getPlayers()) {
                    if (!playerEntity.updateNeeded && playerEntity instanceof OtherClientPlayerEntity) {
                        playerEntity.tickMovement();
                    }
                }
            }
        * */
    }
}