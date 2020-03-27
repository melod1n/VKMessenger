package ru.melod1n.vk.api.util

import ru.melod1n.vk.R
import ru.melod1n.vk.adapter.MessageAdapter
import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
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
        for (i in messages.size - 1 downTo 1) {
            val m1 = messages[i]
            val m2 = messages[i - 1]

            val d1 = Util.removeTime(Date(m1.date * 1000L))
            val d2 = Util.removeTime(Date(m2.date * 1000L))

            if (d1 > d2) {
                messages.add(i, MessageAdapter.TimeStamp(SimpleDateFormat("dd MMM", Locale.getDefault()).format(d1)))
            }
        }
    }

    fun getUserOnline(user: VKUser): String {
        val r = AppGlobal.resources
        return if (user.isOnline) {
            if (user.isOnlineMobile) {
                r.getString(R.string.user_online_mobile)
            } else {
                r.getString(R.string.user_online)
            }
        } else {
            val lastSeen = user.lastSeen
            if (lastSeen != null) {
                r.getString(R.string.user_last_seen_at, getLastSeenTime(lastSeen.time * 1000L))
            } else {
                r.getString(R.string.user_offline)
            }
        }
    }

    //TODO: нормальное время
    fun getLastSeenTime(date: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}