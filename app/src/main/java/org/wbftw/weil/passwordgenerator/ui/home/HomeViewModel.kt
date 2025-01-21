package org.wbftw.weil.passwordgenerator.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wbftw.weil.passwordgenerator.config.DefaultConst
import org.wbftw.weil.passwordgenerator.obj.FormState
import org.wbftw.weil.passwordgenerator.utils.SecureCharArrayConverter
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HomeViewModel : ViewModel() {

    val TAG = "HomeViewModel"
    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()
    var outputPassword: MutableLiveData<CharArray> = MutableLiveData<CharArray>().apply { value = charArrayOf() }
    var outputMasterPasswordHash: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

    fun copyPassword(context: Context, password: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", password)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Password Copied!", Toast.LENGTH_SHORT).show()
    }

    fun generatePassword() {
        // check if the form is valid
        val form = formState.value
        val charset = buildString {
            if (form.checkedNumber) append(DefaultConst.DEFAULT_PASSWORD_TABLE_NUMBER)
            if (form.checkedUppercase) append(DefaultConst.DEFAULT_PASSWORD_TABLE_UPPERCASE)
            if (form.checkedLowercase) append(DefaultConst.DEFAULT_PASSWORD_TABLE_LOWERCASE)
            if (form.checkedSpecialChars) append(form.specialChars)
        }
        if (charset.isEmpty()) {
            Log.d(TAG, "charset is empty")
            return
        }
        if (form.method == 1 && form.masterPassword.isEmpty()) {
            Log.d(TAG, "master password is empty")
            return
        }
        viewModelScope.launch {
            var password: CharArray = CharArray(form.passwordLength) {
                charset[SecureRandom().nextInt(charset.length)]
            }
            var masterPasswordHash = ""

            if (form.method == 1) {
                val masterPassword = form.masterPassword
                val version = form.version
                val salt = form.salt

                val passwordData = "$version:$salt"

                val key = generateHmacKey(masterPassword.toByteArray())
                val signature = hmacSha512(key, passwordData.toByteArray())

                val password1 = signature.toBinaryString()
                val signature2 = hmacSha512(key, (passwordData + password1).toByteArray())

                val password2 = signature2.toBinaryString()
                val signatureOfMaster = hmacSha512(key, masterPassword.toByteArray())

                val combineBinaryPassword = password1 + password2

                password = CharArray(form.passwordLength) {
                    val binaryChunk = combineBinaryPassword.substring(it * 7, it * 7 + 7)
                    val pos = binaryChunk.toInt(2) % charset.length
                    charset[pos]
                }
                masterPasswordHash = signatureOfMaster.toHexString().take(8)
            }

            outputPassword.value = password
            outputMasterPasswordHash.value = masterPasswordHash
            Log.d(TAG, "(generate) password: ${password.joinToString("")}")
            Log.d(TAG, "(generate) masterPasswordHash: $masterPasswordHash")
        }
    }

    fun updateFormState(formState: FormState) {
        _formState.value = formState
    }

    private fun generateHmacKey(keyBytes: ByteArray): SecretKeySpec {
        return SecretKeySpec(keyBytes, "HmacSHA512")
    }

    private fun hmacSha512(key: SecretKeySpec, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(key)
        return mac.doFinal(data)
    }

    private fun ByteArray.toBinaryString(): String {
        return joinToString("") { byte ->
            byte.toUByte().toString(2).padStart(8, '0')
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { byte ->
            byte.toUByte().toString(16).padStart(2, '0').uppercase()
        }
    }

    private fun CharArray.toByteArray(): ByteArray {
        return SecureCharArrayConverter.toByteArray(this)
    }

}
