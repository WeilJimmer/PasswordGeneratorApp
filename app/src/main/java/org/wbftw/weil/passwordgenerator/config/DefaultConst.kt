package org.wbftw.weil.passwordgenerator.config

class DefaultConst {

    companion object {

        val DEFAULT_PASSWORD_LENGTH: Int = 40
        val DEFAULT_PASSWORD_TABLE_NUMBER: CharArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9') //0123456789
        val DEFAULT_PASSWORD_TABLE_UPPERCASE: CharArray = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z') //ABCDEFGHIJKLMNOPQRSTUVWXYZ
        val DEFAULT_PASSWORD_TABLE_LOWERCASE: CharArray = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z') //abcdefghijklmnopqrstuvwxyz
        val DEFAULT_PASSWORD_TABLE_SYMBOLS: CharArray = charArrayOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '{', '}', '[', ']', '=', ',', '.') //!@#$%^&*(){}[]=,.

    }

}