package com.spinoza.messenger_tfs.data.network.attachment

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.data.network.model.UploadFileResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

class AttachmentHandlerImpl @Inject constructor(
    private val authKeeper: AppAuthKeeper,
    private val apiService: ZulipApiService,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : AttachmentHandler {

    override suspend fun saveAttachments(urls: List<String>): Map<String, Boolean> =
        withContext(ioDispatcher) {
            val result = mutableMapOf<String, Boolean>()
            val downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            for (url in urls) {
                val fileName = url.substringAfterLast("/")
                val file = File(downloadsDirectory, fileName)
                val uniqueFile = generateUniqueFileName(file)
                runCatching {
                    downloadFile(url, uniqueFile)
                    result[uniqueFile.name] = true
                }.onFailure {
                    result[uniqueFile.name] = false
                }
            }
            result
        }

    override suspend fun uploadFile(context: Context, uri: Uri): Result<Pair<String, String>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val contentType = context.contentResolver.getType(uri)
                    ?: throw RepositoryError(context.getString(R.string.error_unknown_file_type))
                val tempFile = uri.getTempFile(context)
                val requestFile = tempFile.asRequestBody(contentType.toMediaTypeOrNull())
                val fileName = uri.getFileName(context)
                val filePart = MultipartBody.Part.createFormData(fileName, fileName, requestFile)
                val response =
                    apiRequest<UploadFileResponse> { apiService.uploadFile(filePart) }
                Pair(fileName, response.uri)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun Uri.getTempFile(context: Context): File {
        val inputStream = context.contentResolver.openInputStream(this)
            ?: throw RepositoryError(context.getString(R.string.error_uri))
        val buffer = ByteArray(TEMP_FILE_BUFFER_SIZE)
        val filesDir = context.filesDir
        val tempFile = File(filesDir, TEMP_FILE_NAME).also { it.deleteOnExit() }
        var fileSize = 0L
        val bufferedOutputStream = withContext(ioDispatcher) {
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

    private fun Uri.getFileName(context: Context): String {
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