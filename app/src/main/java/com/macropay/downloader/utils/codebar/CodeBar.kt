package com.macropay.downloader.utils.codebar

import com.macropay.data.logs.ErrorMgr.guardar
import kotlin.Throws
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.common.BitMatrix
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.*

class CodeBar {
    var TAG = "CodeBar"
    @Throws(WriterException::class, IOException::class)
    fun QRCodeImage(text: String?, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val qrCodeWriter = QRCodeWriter()
            val hints: MutableMap<EncodeHintType, Any?> = HashMap()
            hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8 // .ISO_8859_1 "utf-8");
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            hints[EncodeHintType.MARGIN] = 0
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.CODE_39, width, height, hints)
            bitmap = createBitmap(bitMatrix)
        } catch (ex: Exception) {
            guardar(TAG, "generateQRCodeImage", ex.message)
        }
        return bitmap
    }

    fun codebar(text: String?, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val hints: MutableMap<EncodeHintType, Any?> = HashMap()
            hints[EncodeHintType.CHARACTER_SET] = StandardCharsets.UTF_8 // .ISO_8859_1 "utf-8");
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            hints[EncodeHintType.MARGIN] = 0
            val bitMatrix = Code128Writer().encode(
                text,
                BarcodeFormat.CODE_39,
                width,
                height,
                hints
            )
            bitmap = createBitmap(bitMatrix)
        } catch (ex: Exception) {
            guardar(TAG, "generateQRCodeImage", ex.message)
        }
        return bitmap
    }

    // Color.BLACK : Color.WHITE
    fun createBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    companion object {
        private const val QR_CODE_IMAGE_PATH = "images/MyQRCode.png"
        private fun getImageStr(inputStream: InputStream): String {
            var data: ByteArray? = null
            try {
                data = ByteArray(inputStream.available())
                inputStream.read(data)
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // cifrado
            return Base64.getEncoder().encodeToString(data)
        }
    }
}