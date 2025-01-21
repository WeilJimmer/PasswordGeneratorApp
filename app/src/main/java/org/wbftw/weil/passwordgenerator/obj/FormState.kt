package org.wbftw.weil.passwordgenerator.obj

import org.wbftw.weil.passwordgenerator.config.DefaultConst

data class FormState(
    val method: Int = 0, // 0 = random, 1 = fixed password
    val passwordLength: Int = 40,
    val specialChars: CharArray = DefaultConst.DEFAULT_PASSWORD_TABLE_SYMBOLS,
    val checkedNumber: Boolean = true,
    val checkedUppercase: Boolean = true,
    val checkedLowercase: Boolean = true,
    val checkedSpecialChars: Boolean = true,
    val masterPassword: CharArray = CharArray(0),
    val version: String = "1",
    val salt: String = "",
)