package ru.melod1n.vk.api.util

import java.io.*
import java.util.regex.Pattern

object VKUtil {
    fun extractPattern(string: String?, pattern: String?): String? {
        val p = Pattern.compile(pattern)
        val m = p.matcher(string)
        return if (!m.find()) null else m.toMatchResult().group(1)
    }

    @Throws(IOException::class)
    fun convertStreamToString(`is`: InputStream): String {
        val r = InputStreamReader(`is`)
        val sw = StringWriter()
        val buffer = CharArray(1024)
        try {
            var n: Int
            while (r.read(buffer).also { n = it } != -1) {
                sw.write(buffer, 0, n)
            }
        } finally {
            try {
                `is`.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return sw.toString()
    }

    fun closeStream(oin: Any?) {
        if (oin != null) try {
            if (oin is InputStream) oin.close()
            if (oin is OutputStream) oin.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private const val pattern_string_profile_id = "^(id)?(\\d{1,10})$"
    private val pattern_profile_id = Pattern.compile(pattern_string_profile_id)
    fun parseProfileId(text: String?): String? {
        val m = pattern_profile_id.matcher(text)
        return if (!m.find()) null else m.group(2)
    }
}