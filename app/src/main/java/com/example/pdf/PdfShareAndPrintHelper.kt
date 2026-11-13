package com.example.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfShareAndPrintHelper {

    fun sharePdf(context: Context, filePath: String, title: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun copyPdfToUri(context: Context, sourceFilePath: String, destinationUri: Uri): Boolean {
        val src = File(sourceFilePath)
        if (!src.exists()) return false
        return runCatching {
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                FileInputStream(src).use { input ->
                    input.copyTo(out)
                }
            }
            true
        }.getOrDefault(false)
    }

    fun printPdf(context: Context, filePath: String, jobName: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(file.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                    } else {
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    }
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName.ifBlank { "IDVANTA_Document" }, adapter, null)
    }
}
