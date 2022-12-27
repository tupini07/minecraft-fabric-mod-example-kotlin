package net.fabricmc.example.common.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext

abstract class BaseCommand<T>(val command: String) {

    open fun register(dispatcher: CommandDispatcher<T>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<T>(command)
                .executes { context ->
                    execute(context!!)
                }
        )
    }

    abstract fun execute(command: CommandContext<T>): Int
}