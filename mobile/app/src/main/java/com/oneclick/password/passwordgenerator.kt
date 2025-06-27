package com.oneclick.password

import java.security.SecureRandom

object PasswordGenerator {
    private val secureRandom = SecureRandom()

    fun generatePassword(length: Int = 12): String {

        val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val specialCharacters = "!@#$%^&*"

        val allCharacters = lowerCaseLetters + upperCaseLetters + digits + specialCharacters

        val password = StringBuilder()

        password.append(lowerCaseLetters.randomSecure())
        password.append(upperCaseLetters.randomSecure())
        password.append(digits.randomSecure())
        password.append(specialCharacters.randomSecure())

        for (i in 4 until length) {
            password.append(allCharacters.randomSecure())
        }

        return password.toList().shuffled(secureRandom).joinToString("")
    }

    private fun String.randomSecure(): Char {
        return this[secureRandom.nextInt(this.length)]
    }
}

