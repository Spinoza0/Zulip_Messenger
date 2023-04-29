package com.spinoza.messenger_tfs.data.network

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.utils.getBodyOrThrow
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.domain.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.attachment.AttachmentHandler
import com.spinoza.messenger_tfs.domain.attachment.AttachmentNotificator
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

class AttachmentHandlerImpl @Inject constructor(
    private val context: Context,
    private val authKeeper: AppAuthKeeper,
    private val apiService: ZulipApiService,
    private val notificator: AttachmentNotificator,
) : AttachmentHandler {

    override fun saveAttachments(urls: List<String>) {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        CoroutineScope(Dispatchers.IO).launch {
            for (url in urls) {
                val fileName = url.substringAfterLast("/")
                val file = File(downloadsDirectory, fileName)
                val uniqueFile = generateUniqueFileName(file)
                runCatching {
                    downloadFile(url, uniqueFile)
                    withContext(Dispatchers.Main) {
                        notificator.showNotification(
                            "${uniqueFile.name} - ${context.getString(R.string.downloaded)}"
                        )
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        notificator.showNotification(
                            "${uniqueFile.name} - ${context.getString(R.string.error_downloading)}"
                        )
                    }
                }
            }
        }
    }

    override suspend fun uploadFile(oldMessageText: String, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val contentType = context.contentResolver.getType(uri)
                    ?: throw RepositoryError(context.getString(R.string.error_unknown_file_type))
                val tempFile = getTempFile(uri)
                val requestFile = RequestBody.create(MediaType.parse(contentType), tempFile)
                val fileName = uri.getFileName()
                val filePart = MultipartBody.Part.createFormData(fileName, fileName, requestFile)
                val response = apiService.uploadFile(filePart)
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val responseBody = response.getBodyOrThrow()
                if (responseBody.result != ZulipApiService.RESULT_SUCCESS) {
                    throw RepositoryError(responseBody.msg)
                }
                "$oldMessageText\n[$fileName](${responseBody.uri})\n"
            }
        }

    private fun downloadFile(url: String, destination: File) {
        val connection = URL(url).openConnection()
        connection.setRequestProperty(authKeeper.getKey(), authKeeper.getValue())
        connection.connect()

        val inputStream = BufferedInputStream(connection.getInputStream())
        val outputStream = FileOutputStream(destination)

        inputStream.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != END_OF_FILE) {
                    output.write(buffer, NO_OFFSET, bytesRead)
                }
                output.flush()
            }
        }
    }

    private fun generateUniqueFileName(file: File): File {
        var uniqueFile = file
        var counter = FILENAME_INDEX
        while (uniqueFile.exists()) {
            val fileName = file.nameWithoutExtension
            val extension = file.extension
            val newFileName = "$fileName ($counter).$extension"
            uniqueFile = File(file.parent, newFileName)
            counter++
        }
        return uniqueFile
    }

    private suspend fun getTempFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw RepositoryError(context.getString(R.string.error_uri))
        val buffer = ByteArray(TEMP_FILE_BUFFER_SIZE)
        val filesDir = context.filesDir
        val tempFile = File(filesDir, TEMP_FILE_NAME).also { it.deleteOnExit() }
        var fileSize = 0L
        val bufferedOutputStream = withContext(Dispatchers.IO) {
            BufferedOutputStream(FileOutputStream(tempFile), TEMP_FILE_BUFFER_SIZE)
        }
        inputStream.use { input ->
            bufferedOutputStream.use { output ->
                var len = input.read(buffer)
                while (len != END_OF_FILE) {
                    fileSize += len
                    output.write(buffer, NO_OFFSET, len)
                    len = inputStream.read(buffer)
                }
                output.flush()
            }
        }
        if (fileSize <= MIN_FILE_SIZE) {
            throw RepositoryError(context.getString(R.string.error_uri))
        }
        if (fileSize >= MAX_FILE_SIZE) {
            throw RepositoryError(context.getString(R.string.error_file_size))
        }
        return tempFile
    }

    private fun Uri.getFileName(): String {
        var fileName: String = DEFAULT_FILE_NAME
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName.ifBlank { DEFAULT_FILE_NAME }
    }

    private companion object {

        const val DOWNLOAD_BUFFER_SIZE = 1024
        const val TEMP_FILE_BUFFER_SIZE = 512 * 1024
        const val END_OF_FILE = -1
        const val NO_OFFSET = 0
        const val FILENAME_INDEX = 1
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L
        const val MIN_FILE_SIZE = 0L
        const val DEFAULT_FILE_NAME = "file"
        const val TEMP_FILE_NAME = "temp_file"
    }
}