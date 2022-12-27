package net.fabricmc.example.mixin

import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.Connection
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ClientPacketListener::class)
interface ClientPacketListenerAccessor {
    @Accessor
    fun setConnection(connection: Connection)
}