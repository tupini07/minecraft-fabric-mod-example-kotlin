package net.fabricmc.example.server

import net.fabricmc.api.ModInitializer
import net.fabricmc.example.server.commands.TestReplayCommand
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory

class ExampleModServer : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("Hello Fabric world (server)!")

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            // Register your commands here
            TestReplayCommand().register(dispatcher)
        })

        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            dispatcher.register(
                Commands.literal("ppp").executes { ctx ->
                    ctx.source.sendSystemMessage(Component.literal("asdsad"))

                    1
                })

            if (environment.includeIntegrated) {
                // Register your commands here
                TestReplayCommand().register(dispatcher)
            }


        }

    }

    companion object {
        val LOGGER = LoggerFactory.getLogger("modid")
    }
}