package net.fabricmc.example.server

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.example.client.extensions.sendStringMessage
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.slf4j.LoggerFactory


class ExampleModServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        LOGGER.info("Starting server")

        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, _sender, _server ->
            // This code runs when the player joins a world
            val playerId = handler.player.uuid
            LOGGER.info("Player connected to dedicated server! Player uuid is $playerId")

            handler.player.sendStringMessage("Hello from the server $playerId!")
        })

        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, _server ->
            // This code runs when the player joins a world
            val playerId = handler.player.uuid
            LOGGER.info("Player disconnected to dedicated server! Player uuid is $playerId")
        })

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            val registerCommand = { command: String, context: Command<CommandSourceStack> ->
                LiteralArgumentBuilder.literal<CommandSourceStack>(command)
                    .executes(context)
            }

            registerCommand("say-hi") { context ->
                val player = context.source.player as Player?
                player?.sendStringMessage(">> Hi!")

                Command.SINGLE_SUCCESS
            }

            registerCommand("toggle-diamond-everything") { context ->
                val player = context.source.player as Player?
                player?.sendStringMessage(">> Here you go")

                // equip diamond everything lambda

                if (player?.getItemBySlot(EquipmentSlot.HEAD)?.item == Items.DIAMOND_HELMET) {
                    for (slot in EquipmentSlot.values()) {
                        player?.setItemSlot(slot, ItemStack.EMPTY)
                    }
                } else {
                    val setItem = { slot: EquipmentSlot, what: ItemLike ->
                        val itemStack = ItemStack(what)
                        player?.setItemSlot(slot, itemStack)
                    }

                    setItem(EquipmentSlot.HEAD, Items.DIAMOND_HELMET)
                    setItem(EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE)
                    setItem(EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS)
                    setItem(EquipmentSlot.FEET, Items.DIAMOND_BOOTS)

                    setItem(EquipmentSlot.MAINHAND, Items.DIAMOND_SWORD)
                    setItem(EquipmentSlot.OFFHAND, Items.SHIELD)
                }

                Command.SINGLE_SUCCESS
            }

            registerCommand("toggle-fly") { context ->
                val player = context.source.player as Player?

                // toggle fly
                player?.abilities?.mayfly = !player?.abilities?.mayfly!!
                player.onUpdateAbilities()

                player.sendStringMessage(">> Fly is now ${player.abilities?.mayfly}")

                Command.SINGLE_SUCCESS
            }
        })
    }

    companion object {
        // This logger is used to write text to the console and the log file.
        // It is considered best practice to use your mod id as the logger's name.
        // That way, it's clear which mod wrote info, warnings, and errors.
        val LOGGER = LoggerFactory.getLogger("modid")
    }
}