package net.fabricmc.example.server.utils.replay

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet

internal class ReplaySender : ChannelDuplexHandler() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {

        if (msg is Packet<*>) {
            // pass packets directly
            super.channelRead(ctx, msg)
        }

        if (msg is Array<*> && msg.isArrayOf<Byte>()) {
            super.channelRead(ctx, msg)

            // whether this is running on the same thread as the game
//            Minecraft.getInstance().isSameThread

            val lightProvider = Minecraft.getInstance().level!!.chunkSource.lightEngine
            while (lightProvider.hasLightWork()) {
                lightProvider.runUpdates(Integer.MAX_VALUE, true, true)
            }
        }
    }

}