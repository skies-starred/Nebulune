package xyz.aerii.nebulune.utils

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerGamePacketListener
import xyz.aerii.athen.handlers.Smoothie.client

fun Packet<ServerGamePacketListener>.send() {
    client.connection?.send(this)
}