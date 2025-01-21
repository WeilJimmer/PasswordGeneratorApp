package org.wbftw.weil.passwordgenerator.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Password Generator Android App by Weil Jimmer @ White Birch Forum Team (c) https://www.wbftw.org/"
    }
    val text: LiveData<String> = _text
}