package net.fabricmc.example.mixin

import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.Connection
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Mutable
import org.spongepowered.asm.mixin.Shadow

@Mixin(ClientPacketListener::class)
abstract class ClientPacketListenerAccessor {

    @Mutable
    @Shadow
    var connection: Connection? = null;
}