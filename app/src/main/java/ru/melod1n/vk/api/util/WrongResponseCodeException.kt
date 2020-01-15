package ru.melod1n.vk.api.util

import java.io.IOException

class WrongResponseCodeException(message: String?) : IOException(message) {
    companion object {
        private const val serialVersionUID = 1L
    }
}