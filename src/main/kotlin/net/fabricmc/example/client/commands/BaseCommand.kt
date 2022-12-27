package net.fabricmc.example.client.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

abstract class BaseCommand(val command: String) {

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<FabricClientCommandSource>(command)
                .executes { context ->
                    execute(context!!)
                }
        )
    }

    abstract fun execute(command: CommandContext<FabricClientCommandSource>): Int
}