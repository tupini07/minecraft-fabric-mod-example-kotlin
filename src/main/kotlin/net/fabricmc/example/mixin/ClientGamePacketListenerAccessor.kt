package net.fabricmc.example.mixin

import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientGamePacketListener
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ClientGamePacketListener::class)
interface ClientGamePacketListenerAccessor {
    @Accessor
    fun setConnection(connection: Connection)
}