package me.grabsky.tweaks

import me.grabsky.indigo.KotlinPlugin
import me.grabsky.indigo.api.logger.ConsoleLogger

class Tweaks(override val consoleLogger: ConsoleLogger) : KotlinPlugin() {


    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    override fun onReload(): Boolean {
        TODO("Not yet implemented")
    }
}