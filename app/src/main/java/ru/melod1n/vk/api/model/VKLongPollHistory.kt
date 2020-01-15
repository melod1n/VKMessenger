package ru.melod1n.vk.api.model

import java.util.*

class VKLongPollHistory : VKModel() {
    private val lpMessages: ArrayList<VKMessage>? = null
    private val messages: ArrayList<VKMessage>? = null
    private val profiles: ArrayList<VKUser>? = null
    private val groups: ArrayList<VKGroup>? = null //TODO: использовать

    companion object {
        private const val serialVersionUID = 1L
    }
}