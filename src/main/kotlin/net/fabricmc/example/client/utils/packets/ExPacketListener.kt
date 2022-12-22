package net.fabricmc.example.client.utils.packets


import com.mojang.authlib.minecraft.client.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.fabricmc.example.client.extensions.copyToByteBufferSafe
import net.minecraft.network.Connection
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.ByteBuffer

@ChannelHandler.Sharable
class ExPacketListener(name: String) : ChannelInboundHandlerAdapter() {
    private val bufferedWriter: BufferedWriter
    private var numWrites = 0

    init {
        FileUtils.getUserDirectoryPath()
        val outputFile = File(FileUtils.getUserDirectoryPath() + "/Downloads/packet_output/$name.txt")

        if (outputFile.exists()) {
            outputFile.delete()
            outputFile.createNewFile()
        }

        // NOTE: writes happen on the network thread, so it might cause the game to lag. Better to move this
        // to a separate thread
        bufferedWriter = BufferedWriter(FileWriter(outputFile, Charsets.UTF_8), 512_000)
    }


    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        LOGGER.info("${this.javaClass.name} handler added")
        super.handlerAdded(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        LOGGER.info("${this.javaClass.name} handler removed")
        super.handlerRemoved(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // TODO: How do we know which `tick` number the current event happened in? Is it enough to assume average
        //       tick speed is constant?
        //  ^ Consider that this code here runs on the network thread

        val byteBuffer: ByteBuffer = if (msg is ByteBuf) {
            msg.copyToByteBufferSafe()
        } else {
            encodePacket(ctx, msg as Packet<*>)!!.copyToByteBufferSafe()
        }

        // NOTE: it's a bit weird but the `serialized` output is enclosed in double quotes, and these are
        // necessary for the deserializer to work properly!
        val serializedBuffer = serializer.writeValueAsString(byteBuffer)

        bufferedWriter.write(serializedBuffer)

        // to decode packets
        // val deserializedPacket = serializer
        //     .readValue(serializedBuffer, ByteBuffer::class.java)
        //     .let {
        //         val byteBuf = it.into<ByteBuf>()
        //         decodePacket(ctx, byteBuf)
        //     }

        // TODO: this might be unnecessary and inneficient to read. Better to have a stream parser that ueses
        // double quotes (") to know when a packet starts and ends
        bufferedWriter.write("\n")

        this.numWrites += 1
        if (this.numWrites % 1000 == 0) {
            bufferedWriter.flush()
            LOGGER.info("Flushing recorded packets. Total=$numWrites")
        }

        ctx.fireChannelRead(msg)
    }

    private fun decodePacket(ctx: ChannelHandlerContext, msg: ByteBuf): Packet<*>? {
        val i: Int = msg.readableBytes()
        if (i != 0) {
            val friendlybytebuf = FriendlyByteBuf(msg)
            val j = friendlybytebuf.readVarInt()

            val packet: Packet<*>? = ctx
                .channel()
                .attr<ConnectionProtocol>(Connection.ATTRIBUTE_PROTOCOL)
                .get()
                .createPacket(PacketFlow.CLIENTBOUND, j, friendlybytebuf)

            if (packet == null) {
                throw IOException("Bad packet id $j")
            } else {
                if (friendlybytebuf.readableBytes() > 0) {
                    throw IOException(
                        "Packet " + ctx.channel().attr<ConnectionProtocol>(Connection.ATTRIBUTE_PROTOCOL).get()
                            .getId() + "/" + j + " (" + packet.javaClass.simpleName + ") was larger than I expected, found " + friendlybytebuf.readableBytes() + " bytes extra whilst reading packet " + j
                    )
                } else {
                    return packet
                }
            }
        }

        return null
    }

    private fun encodePacket(ctx: ChannelHandlerContext, msg: Packet<*>): FriendlyByteBuf? {
        val byteBuffer = ByteBufAllocator.DEFAULT.buffer()
        val connectionProtocol: ConnectionProtocol = ctx
            .channel()
            .attr<ConnectionProtocol>(Connection.ATTRIBUTE_PROTOCOL)
            .get()

        if (connectionProtocol == null) {
            throw RuntimeException("ConnectionProtocol unknown: $msg")
        } else {
            val integer = connectionProtocol.getPacketId(PacketFlow.CLIENTBOUND, msg)
            if (integer == null) {
                throw IOException("Can't serialize unregistered packet")
            } else {
                val friendlybytebuf = FriendlyByteBuf(byteBuffer)
                friendlybytebuf.writeVarInt(integer)
                try {
                    val i = friendlybytebuf.writerIndex()

                    msg.write(friendlybytebuf)
                    val j = friendlybytebuf.writerIndex() - i
                    require(j <= 8388608) { "Packet too big (is $j, should be less than 8388608): $msg" }

                    return friendlybytebuf
                } catch (throwable: Throwable) {
                    if (!msg.isSkippable()) {
                        throw throwable
                    }
                }
            }
        }

        return null
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger("modid")
        private val serializer = ObjectMapper.create()
    }
}