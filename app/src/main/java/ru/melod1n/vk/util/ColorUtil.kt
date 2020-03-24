package ru.melod1n.vk.util

import kotlin.random.Random

object ColorUtil {

    fun generateHEXColor(): String {
        return String.format("#%06x", Random.nextInt(0xffffff + 1))
    }

}