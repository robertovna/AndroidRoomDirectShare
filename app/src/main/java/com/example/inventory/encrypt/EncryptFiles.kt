package com.example.inventory.encrypt

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.security.crypto.EncryptedFile
import com.example.inventory.data.Item
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import androidx.security.crypto.MasterKeys
import java.nio.charset.StandardCharsets

class EncryptFile {
    companion object {
        private fun getEncryptedFile(file: File, context: Context): EncryptedFile {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

            return EncryptedFile.Builder(
                file,
                context,
                masterKeyAlias,
                fileEncryptionScheme
            ).build()
        }

        fun encryptItemIntoFile(context: Context, uri: Uri, item: Item) {
            val tmpFile = File(context.cacheDir, "some.json")

            if (tmpFile.exists()) {
                tmpFile.delete()
            }

            val encryptedFile = getEncryptedFile(tmpFile, context)
            var bytes = Json.encodeToString(item).toByteArray(StandardCharsets.UTF_8)

            encryptedFile.openFileOutput().apply {
                write(bytes)
                flush()
                close()
            }

            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outputStream ->
                    outputStream.write(tmpFile.readBytes())
                    outputStream.flush()
                }
            }
        }

        fun getEncryptedItemPathInCache(context: Context, item: Item) : File {
            val tmpFile = File(context.cacheDir, "some.json")

            if (tmpFile.exists()) {
                tmpFile.delete()
            }

            val encryptedFile = getEncryptedFile(tmpFile, context)
            var bytes = Json.encodeToString(item).toByteArray(StandardCharsets.UTF_8)

            encryptedFile.openFileOutput().apply {
                write(bytes)
                flush()
                close()
            }
            val newFile = File(context.cacheDir, "${item.itemName}.json")
            if (newFile.exists())
                newFile.delete()
            tmpFile.copyTo(newFile)
            return newFile
        }

        fun decryptItemFromFile(context: Context, uri: Uri): Item {
            val tmpFile = File(context.cacheDir, "some.json")

            if (tmpFile.exists()) {
                tmpFile.delete()
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                context.contentResolver.openOutputStream(tmpFile.toUri())?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val fileBytes = getEncryptedFile(tmpFile, context).openFileInput().readBytes()
            return Json.decodeFromString(fileBytes.decodeToString())
        }
    }
}