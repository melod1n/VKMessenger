package ru.melod1n.vk.io

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.max

object EasyStreams {
    const val BUFFER_SIZE = 8192
    const val CHAR_BUFFER_SIZE = 4096
    @JvmOverloads
    @Throws(IOException::class)
    fun read(from: InputStream, encoding: Charset = StandardCharsets.UTF_8): String {
        return read(InputStreamReader(from, encoding))
    }

    @Throws(IOException::class)
    fun read(from: Reader): String {
        val builder = StringWriter(CHAR_BUFFER_SIZE)
        return try {
            copy(from, builder)
            builder.toString()
        } finally {
            close(from)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readBytes(from: InputStream): ByteArray {
        val output = ByteArrayOutputStream(max(from.available(), BUFFER_SIZE))
        try {
            copy(from, output)
        } finally {
            close(from)
        }
        return output.toByteArray()
    }

    @Throws(IOException::class)
    fun write(from: ByteArray, to: OutputStream) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @Throws(IOException::class)
    fun write(from: String, to: OutputStream) {
        write(from, OutputStreamWriter(to, StandardCharsets.UTF_8))
    }

    @Throws(IOException::class)
    fun write(from: CharArray, to: Writer) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @Throws(IOException::class)
    fun write(from: String, to: Writer) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @Throws(IOException::class)
    fun copy(from: Reader, to: Writer): Long {
        val buffer = CharArray(CHAR_BUFFER_SIZE)
        var read: Int
        var total: Long = 0
        while (from.read(buffer).also { read = it } != -1) {
            to.write(buffer, 0, read)
            total += read.toLong()
        }
        return total
    }

    @Throws(IOException::class)
    fun copy(from: InputStream, to: OutputStream): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var read: Int
        var total: Long = 0
        while (from.read(buffer).also { read = it } != -1) {
            to.write(buffer, 0, read)
            total += read.toLong()
        }
        return total
    }

    @JvmOverloads
    fun buffer(input: InputStream, size: Int = BUFFER_SIZE): BufferedInputStream {
        return if (input is BufferedInputStream) input else BufferedInputStream(input, size)
    }

    @JvmOverloads
    fun buffer(output: OutputStream, size: Int = BUFFER_SIZE): BufferedOutputStream {
        return if (output is BufferedOutputStream) output else BufferedOutputStream(output, size)
    }

    @JvmOverloads
    fun buffer(input: Reader, size: Int = CHAR_BUFFER_SIZE): BufferedReader {
        return if (input is BufferedReader) input else BufferedReader(input, size)
    }

    @JvmOverloads
    fun buffer(output: Writer, size: Int = CHAR_BUFFER_SIZE): BufferedWriter {
        return if (output is BufferedWriter) output else BufferedWriter(output, size)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun gzip(input: InputStream, size: Int = BUFFER_SIZE): GZIPInputStream {
        return if (input is GZIPInputStream) input else GZIPInputStream(input, size)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun gzip(input: OutputStream, size: Int = BUFFER_SIZE): GZIPOutputStream {
        return if (input is GZIPOutputStream) input else GZIPOutputStream(input, size)
    }

    fun close(c: Closeable?): Boolean {
        if (c != null) {
            try {
                c.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }
}