package net.fabricmc.example.client.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.example.client.extensions.sendStringMessage
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.entity.player.Player


class SayHiCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
            dispatcher.register(
                LiteralArgumentBuilder.literal<FabricClientCommandSource>("sayhi")
                    .executes { context ->
                        execute(context!!)
                    }
            )
        }

        private fun execute(command: CommandContext<FabricClientCommandSource>): Int {
            val player = command.source.entity as Player?
            player?.sendStringMessage(">> Hi!")

            return Command.SINGLE_SUCCESS
        }
    }
}
