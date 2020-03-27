package ru.melod1n.vk.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ru.melod1n.vk.common.AppGlobal
import java.io.File


object FileUtils {

    fun getFileUri(context: Context, file: File): Uri? {
        return FileProvider.getUriForFile(context, "${AppGlobal.packageNameString}.provider", file)
    }

}