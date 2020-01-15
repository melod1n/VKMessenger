package ru.melod1n.vk.io

import java.io.ByteArrayOutputStream

class BytesOutputStream : ByteArrayOutputStream {
    constructor() : super(8192)
    constructor(size: Int) : super(size)

    val byteArray: ByteArray
        get() = buf
}