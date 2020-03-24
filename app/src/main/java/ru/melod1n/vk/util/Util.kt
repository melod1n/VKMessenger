package ru.melod1n.vk.util

import ru.melod1n.vk.io.BytesOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object Util {

    fun serialize(source: Any?): ByteArray? {
        try {
            val bos = BytesOutputStream()
            val out = ObjectOutputStream(bos)
            out.writeObject(source)
            out.close()
            return bos.byteArray
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deserialize(source: ByteArray?): Any? {
        if (ArrayUtil.isEmpty(source)) {
            return null
        }
        try {
            val bis = ByteArrayInputStream(source)
            val `in` = ObjectInputStream(bis)
            val o = `in`.readObject()
            `in`.close()
            return o
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}