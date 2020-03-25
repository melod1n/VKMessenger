package ru.melod1n.vk.api.util

import ru.melod1n.vk.activity.MessagesActivity
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.util.Util
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object VKUtil {
    fun extractPattern(string: String, pattern: String): String? {
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

    fun parseProfileId(text: String): String? {
        val m = pattern_profile_id.matcher(text)
        return if (!m.find()) null else m.group(2)
    }

    fun sortMessagesByDate(values: ArrayList<VKMessage>, reverse: Boolean): ArrayList<VKMessage> {
        values.sortWith(Comparator { m1, m2 ->

            if (reverse) {
                m2.date - m1.date
            } else {
                m1.date - m2.date
            }
        })

        return values
    }

    fun prepareList(messages: ArrayList<VKMessage>) {
        var toSkip = -1

        for (i in messages.size - 1 downTo 1) {
            if (toSkip == i) {
                continue
            }

            val m1 = messages[i]
            val m2 = messages[i - 1]

            val d1 = Util.removeTime(Date(m1.date * 1000L))
            val d2 = Util.removeTime(Date(m2.date * 1000L))

            if (d1 > d2) {
                messages.add(i - 1, MessagesActivity.TimeStamp(SimpleDateFormat("dd MMM", Locale.getDefault()).format(d1)))
                toSkip = i - 1
            }
        }
    }
}