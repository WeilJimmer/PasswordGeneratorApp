package org.wbftw.weil.passwordgenerator.utils

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

object SecureCharArrayConverter {
    /**
     * 安全地將 CharArray 轉換為 ByteArray，直接使用 NIO 的 CharBuffer 和 ByteBuffer
     * 避免創建中間的 String 物件
     */
    fun toByteArray(chars: CharArray): ByteArray {
        val charset = Charset.forName("UTF-8")
        val encoder = charset.newEncoder()

        // 分配 CharBuffer，使用 wrap 避免複製數據
        val charBuffer = CharBuffer.wrap(chars)

        try {
            // 預估所需的 byte 大小
            val maxBytes = (chars.size * encoder.maxBytesPerChar()).toInt()
            // 分配 ByteBuffer
            val byteBuffer = ByteBuffer.allocate(maxBytes)

            // 編碼
            encoder.encode(charBuffer, byteBuffer, true)
            encoder.flush(byteBuffer)

            // 創建正確大小的 byte 數組
            val result = ByteArray(byteBuffer.position())
            byteBuffer.flip()
            byteBuffer.get(result)

            // 清理 ByteBuffer
            clearBuffer(byteBuffer)

            return result

        } finally {
            // 清理 CharBuffer
            charBuffer.clear()
            // 注意：這裡不清理原始的 chars，因為這應該由調用者決定
        }
    }

    /**
     * 安全地清理 ByteBuffer
     */
    private fun clearBuffer(buffer: ByteBuffer) {
        try {
            // 用零覆蓋所有位置
            buffer.position(0)
            buffer.limit(buffer.capacity())
            while (buffer.hasRemaining()) {
                buffer.put(0.toByte())
            }
        } finally {
            buffer.clear()
        }
    }

    /**
     * 安全地清理 CharArray
     */
    fun clearChars(chars: CharArray) {
        java.util.Arrays.fill(chars, '\u0000')
    }

    /**
     * 安全地清理 ByteArray
     */
    fun clearBytes(bytes: ByteArray) {
        java.util.Arrays.fill(bytes, 0.toByte())
    }
}