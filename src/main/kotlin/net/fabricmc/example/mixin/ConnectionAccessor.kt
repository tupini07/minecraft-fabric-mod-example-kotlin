package net.fabricmc.example.mixin

import io.netty.channel.Channel
import net.minecraft.network.Connection
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Connection::class)
interface ConnectionAccessor {

    @Accessor("channel")
    fun getChannel(): Channel
}