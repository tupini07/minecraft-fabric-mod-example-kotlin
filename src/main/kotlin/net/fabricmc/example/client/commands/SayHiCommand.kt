package net.fabricmc.example.client.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.example.client.extensions.sendStringMessage
import net.fabricmc.example.common.commands.BaseCommand
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.entity.player.Player


class SayHiCommand : BaseCommand<FabricClientCommandSource>("sayhi") {
    override fun execute(command: CommandContext<FabricClientCommandSource>): Int {
        val player = command.source.entity as Player?
        player?.sendStringMessage(">> Hi!")

        return Command.SINGLE_SUCCESS
    }
}
