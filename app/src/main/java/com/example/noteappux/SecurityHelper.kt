package com.example.noteappux

import java.security.MessageDigest

object SecurityHelper {

    fun hashPasscode(passcode: String): String {
        val cleanPasscode = passcode.trim()

        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(cleanPasscode.toByteArray())

        return bytes.joinToString("") { byte ->
            "%02x".format(byte)
        }
    }

    fun verifyPasscode(inputPasscode: String, savedHash: String): Boolean {
        val inputHash = hashPasscode(inputPasscode)
        return inputHash == savedHash
    }
}