package com.example.nfctag

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class TagService : HostApduService() {

    // NDEF-сообщение: текстовая запись "PAY"
    private val ndefMsg = byteArrayOf(
        0xD1.toByte(), 0x01, 0x06, 0x54, 0x02, 0x65, 0x6E, 0x50, 0x41, 0x59
    )
    private val ndefFile = byteArrayOf(0x00, ndefMsg.size.toByte()) + ndefMsg
    private val ccFile = byteArrayOf(
        0x00, 0x0F, 0x20, 0x00, 0x3B, 0x00, 0x34, 0x04, 0x06,
        0xE1.toByte(), 0x04, 0x00, 0xFF.toByte(), 0x00, 0xFF.toByte()
    )

    private var selected: ByteArray = ccFile
    private val OK = byteArrayOf(0x90.toByte(), 0x00)
    private val FAIL = byteArrayOf(0x6A, 0x82.toByte())

    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {
        if (apdu.size < 4) return FAIL
        val ins = apdu[1].toInt() and 0xFF

        return when (ins) {
            0xA4 -> { // SELECT
                val p1 = apdu[2].toInt() and 0xFF
                if (p1 == 0x04) {
                    selected = ccFile
                    OK
                } else {
                    val fid = ((apdu[5].toInt() and 0xFF) shl 8) or (apdu[6].toInt() and 0xFF)
                    when (fid) {
                        0xE103 -> { selected = ccFile; OK }
                        0xE104 -> { selected = ndefFile; OK }
                        else -> FAIL
                    }
                }
            }
            0xB0 -> { // READ BINARY
                val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)
                var le = apdu[4].toInt() and 0xFF
                if (le == 0) le = 256
                if (offset > selected.size) {
                    FAIL
                } else {
                    val end = minOf(offset + le, selected.size)
                    selected.copyOfRange(offset, end) + OK
                }
            }
            else -> FAIL
        }
    }

    override fun onDeactivated(reason: Int) {}
}
