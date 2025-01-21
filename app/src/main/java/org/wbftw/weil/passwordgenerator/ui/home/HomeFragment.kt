package org.wbftw.weil.passwordgenerator.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.wbftw.weil.passwordgenerator.R
import org.wbftw.weil.passwordgenerator.databinding.FragmentHomeBinding
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    private fun generatePassword(binding: FragmentHomeBinding) {
        val checkboxNumber: CheckBox = binding.checkboxNumber
        val uppercaseCheckbox: CheckBox = binding.checkboxUppercase
        val lowercaseCheckbox: CheckBox = binding.checkboxLowercase
        val symbolsCheckbox: CheckBox = binding.checkboxSpecialCharacters
        val fixedPasswordCheckBox: CheckBox = binding.checkboxMethodFixed
        val masterPassword: TextInputEditText = binding.editTextMasterPassword
        if (!checkboxNumber.isChecked && !uppercaseCheckbox.isChecked && !lowercaseCheckbox.isChecked && !symbolsCheckbox.isChecked) {
            Toast.makeText(context, getString(R.string.code_please_check_at_least_1_type), Toast.LENGTH_SHORT).show()
            return
        }
        if (fixedPasswordCheckBox.isChecked && masterPassword.text.toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.code_please_enter_master_password), Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.generatePassword()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateEvent(binding)
        setEvent(binding)

        return root
    }

    private fun updateEvent(binding: FragmentHomeBinding){
        viewModel.outputPassword.observe(viewLifecycleOwner) { password ->
            //binding.editTextGeneratedPassword.setText(password,0,password.size)w
            Log.d(TAG, "password: ${password.joinToString("")}")
            binding.editTextGeneratedPassword.setText(password.joinToString(""))
        }
        viewModel.outputMasterPasswordHash.observe(viewLifecycleOwner) { hash ->
            val hint = getString(R.string.code_master_password) + " [$hash]"
            binding.textInputLayoutMasterPassword.hint = hint
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formState.collect {
                    viewModel.generatePassword()
                }
            }
        }
    }

    private fun setEvent(binding: FragmentHomeBinding){
        val generateButton: Button = binding.buttonGenerate
        val copyButton: Button = binding.buttonCopy
        val slider: Slider = binding.sliderLength
        val lengthText: EditText = binding.editTextLength
        val checkBoxNumber: CheckBox = binding.checkboxNumber
        val checkBoxUppercase: CheckBox = binding.checkboxUppercase
        val checkBoxLowercase: CheckBox = binding.checkboxLowercase
        val checkBoxSpecialChars: CheckBox = binding.checkboxSpecialCharacters
        val checkBoxMethodFixed: CheckBox = binding.checkboxMethodFixed
        val editTextMasterPassword: TextInputEditText = binding.editTextMasterPassword
        val editTextVersion: TextInputEditText = binding.editTextVersion
        val editTextSalt: TextInputEditText = binding.editTextSalt
        val editTextSpecial: EditText = binding.editTextCustomCharacters

        // Region [password length]
        slider.addOnChangeListener { _, value, _ ->
            lengthText.setText(value.toInt().toString())
            viewModel.updateFormState(viewModel.formState.value.copy(passwordLength=value.toInt()))
        }
        lengthText.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                val value = lengthText.text.toString().toIntOrNull() ?: 0
                slider.value = value.toFloat()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        // Region [button]
        generateButton.setOnClickListener {
            generatePassword(binding)
        }
        copyButton.setOnClickListener {
            viewModel.copyPassword(requireContext(), binding.editTextGeneratedPassword.text.toString())
        }

        // Region [checkbox]
        checkBoxNumber.setOnCheckedChangeListener { _, c ->
            viewModel.updateFormState(viewModel.formState.value.copy(checkedNumber=c))
        }
        checkBoxUppercase.setOnCheckedChangeListener { _, c ->
            viewModel.updateFormState(viewModel.formState.value.copy(checkedUppercase=c))
        }
        checkBoxLowercase.setOnCheckedChangeListener { _, c ->
            viewModel.updateFormState(viewModel.formState.value.copy(checkedLowercase=c))
        }
        checkBoxSpecialChars.setOnCheckedChangeListener { _, c ->
            viewModel.updateFormState(viewModel.formState.value.copy(checkedSpecialChars=c))
        }
        checkBoxMethodFixed.setOnCheckedChangeListener { _, c ->
            viewModel.updateFormState(viewModel.formState.value.copy(method=if(c) 1 else 0))
            val masterPasswordLayout = binding.textInputLayoutMasterPassword
            val versionSaltLayout = binding.linearLayoutSaltVersion
            if (c) {
                masterPasswordLayout.visibility = View.VISIBLE
                versionSaltLayout.visibility = View.VISIBLE
            } else {
                masterPasswordLayout.visibility = View.GONE
                versionSaltLayout.visibility = View.GONE
            }
        }
        editTextSpecial.addTextChangedListener{
            viewModel.updateFormState(viewModel.formState.value.copy(specialChars=it.toString().toCharArray()))
        }

        // Region [fixed password method]
        editTextMasterPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                val currentChars = CharArray(length)
                s?.forEachIndexed { index, char ->
                    currentChars[index] = char
                }
                viewModel.updateFormState(viewModel.formState.value.copy(masterPassword=currentChars))
            }
        })
        editTextSalt.addTextChangedListener{
            viewModel.updateFormState(viewModel.formState.value.copy(salt=it.toString()))
        }
        editTextVersion.addTextChangedListener{
            viewModel.updateFormState(viewModel.formState.value.copy(version=it.toString()))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun resetDefault(){
        Log.d(TAG, "reset default")
        binding.checkboxNumber.isChecked = true
        binding.checkboxUppercase.isChecked = true
        binding.checkboxLowercase.isChecked = true
        binding.checkboxSpecialCharacters.isChecked = true
        binding.checkboxMethodFixed.isChecked = false
        binding.editTextMasterPassword.setText("")
        binding.editTextVersion.setText("1")
        binding.editTextSalt.setText("")
        binding.sliderLength.value = 40f
        binding.editTextLength.setText("40")
    }

}