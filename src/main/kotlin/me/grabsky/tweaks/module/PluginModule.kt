package me.grabsky.tweaks.module

import me.grabsky.indigo.KotlinPlugin
import me.grabsky.tweaks.Tweaks

abstract class PluginModule(protected val tweaks: Tweaks) {
    abstract fun onModuleEnable()
    abstract fun onModuleDisable()
}