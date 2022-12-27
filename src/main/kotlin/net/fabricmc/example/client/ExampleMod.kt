package net.fabricmc.example.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.example.client.commands.SayHiCommand
import net.fabricmc.example.client.utils.packets.ExPacketListener
import net.fabricmc.example.mixin.ConnectionAccessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.slf4j.LoggerFactory


class ExampleMod : ClientModInitializer {
    override fun onInitializeClient() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Hello Fabric world!")


        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            // Register your commands here
            SayHiCommand().register(dispatcher)
        })

        ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler, _, _ ->
            // This code runs when the player joins a world
            LOGGER.info("Player connected to logical server")

            val connection = handler.connection!! as ConnectionAccessor
            val pipeline = connection.getChannel().pipeline()

            pipeline.addBefore(
                "decoder",
                "custom_packet_handler_before_decoder",
                ExPacketListener("custom_packet_handler_before_decoder")
            )
            // pipeline.addBefore("packet_handler", "8_custom_packet_handler", ExPacketListener())
        })

//        Minecraft.getInstance().options!!.renderDistance().set(64)
//        Minecraft.getInstance().options!!.overrideWidth = 512
//        Minecraft.getInstance().options!!.overrideHeight = 512
    }

    companion object {
        // This logger is used to write text to the console and the log file.
        // It is considered best practice to use your mod id as the logger's name.
        // That way, it's clear which mod wrote info, warnings, and errors.
        val LOGGER = LoggerFactory.getLogger("modid")
    }
}