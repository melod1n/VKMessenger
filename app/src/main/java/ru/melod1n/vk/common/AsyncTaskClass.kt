package ru.melod1n.vk.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import java.net.URL
import javax.net.ssl.HttpsURLConnection

open class AsyncTaskClass(var imageView: ImageView) : AsyncTask<String, Int, Bitmap?>() {

    override fun doInBackground(vararg params: String?): Bitmap? {
        for (param in params) {
            try {
                val url = URL(param)
                val connection = url.openConnection() as HttpsURLConnection

                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        imageView.setImageBitmap(result)
    }

}