package net.fabricmc.example.client.utils.replay

import io.netty.channel.embedded.EmbeddedChannel
import net.fabricmc.example.mixin.ClientGamePacketListenerAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow

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

        networkManager.setListener(ClientHandshakePacketListenerImpl(networkManager,
            Minecraft.getInstance(),
            null,
            null,
            false,
            null,
            { _ -> }
        ))

        val channel = EmbeddedChannel()

        channel.pipeline().addLast("custom_replay_sender", ReplaySender())
        channel.pipeline().addLast("packet_handler", networkManager)
        channel.pipeline().fireChannelActive()

        val clientPacketListener =
            Minecraft.getInstance().player!!.connection!!.connection as ClientGamePacketListenerAccessor

        clientPacketListener.setConnection(networkManager)

        connection = networkManager

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