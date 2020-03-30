package ru.melod1n.vk.api.model

import org.json.JSONObject
import java.io.Serializable
import java.util.*

abstract class VKModel : Serializable {

    protected constructor()
    protected constructor(o: JSONObject?)

    open fun asList(): ArrayList<out VKModel> {
        return ArrayList(listOf(this))
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}