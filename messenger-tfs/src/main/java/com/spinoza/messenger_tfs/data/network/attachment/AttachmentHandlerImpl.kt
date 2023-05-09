package com.spinoza.messenger_tfs.data.network.attachment

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.UploadFileResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.UploadedFileInfo
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class AttachmentHandlerImpl @Inject constructor(
    private val authorizationStorage: AuthorizationStorage,
    private val apiService: ZulipApiService,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : AttachmentHandler {

    override suspend fun saveAttachments(
        context: Context,
        urls: List<String>,
    ): Map<String, Boolean> = withContext(ioDispatcher) {
        val result = mutableMapOf<String, Boolean>()
        for (url in urls) {
            runCatching {
                result[downloadFile(context, url)] = true
            }.onFailure {
                result[url.getFileNameFromUrl()] = false
            }
        }
        result
    }

    override suspend fun uploadFile(context: Context, uri: Uri): Result<UploadedFileInfo> =
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
                UploadedFileInfo(fileName, response.uri)
            }
        }

    private suspend fun downloadFile(context: Context, url: String): String {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val fileName = url.getFileNameFromUrl()
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationUri(Uri.fromFile(file))
            .addRequestHeader(
                authorizationStorage.getAuthHeaderTitle(),
                authorizationStorage.getAuthHeaderValue()
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(file.name)
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().apply {
            setFilterById(downloadId)
        }
        var downloading = true
        var downloadedFileName: String? = null
        while (downloading) {
            delay(DELAY_BEFORE_CHECK_DOWNLOAD_STATUS)
            downloadManager.query(query).use { cursor ->
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            downloadedFileName = file.name
                        }
                    }
                }
            }
        }
        return downloadedFileName ?: DEFAULT_FILE_NAME
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

    private fun String.getFileNameFromUrl(): String {
        return this.substringAfterLast("/").ifBlank { DEFAULT_FILE_NAME }
    }

    private companion object {

        const val DELAY_BEFORE_CHECK_DOWNLOAD_STATUS = 500L
        const val TEMP_FILE_BUFFER_SIZE = 512 * 1024
        const val END_OF_FILE = -1
        const val NO_OFFSET = 0
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L
        const val MIN_FILE_SIZE = 0L
        const val DEFAULT_FILE_NAME = "file"
        const val TEMP_FILE_NAME = "temp_file"
    }
}