package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.TranslationRepository
import com.example.ui.theme.AICasualEnglishKeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiKeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var repository: TranslationRepository
    private var vibrator: Vibrator? = null

    private var currentText by mutableStateOf("")
    private var translatedText by mutableStateOf<String?>(null)
    private var isLoading by mutableStateOf(false)
    private var selectedTone by mutableStateOf("Casual & Natural")
    private var hapticEnabled = true
    private var clipboardClips = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dataStoreManager = DataStoreManager(this)
        val database = AppDatabase.getDatabase(this)
        repository = TranslationRepository(database.translationHistoryDao())
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        scope.launch {
            selectedTone = dataStoreManager.selectedTone.first()
            hapticEnabled = dataStoreManager.hapticFeedback.first()
        }
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val composeView = ComposeView(this)
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)

        updateClipboardItems()

        composeView.setContent {
            AICasualEnglishKeyboardTheme {
                KeyboardView(
                    currentInputText = currentText,
                    translatedText = translatedText,
                    isLoading = isLoading,
                    selectedTone = selectedTone,
                    clipboardClips = clipboardClips,
                    onKeyPress = { char -> handleKeyPress(char) },
                    onBackspace = { handleBackspace() },
                    onSpace = { handleKeyPress(" ") },
                    onEnter = { handleEnter() },
                    onTranslateClick = { performCasualTranslation() },
                    onApplyTranslation = { applyTranslatedText(it) },
                    onToneSelect = { tone ->
                        selectedTone = tone
                        scope.launch { dataStoreManager.saveSelectedTone(tone) }
                        if (currentText.isNotBlank()) {
                            performCasualTranslation()
                        }
                    },
                    onSelectClip = { clip ->
                        currentInputConnection?.commitText(clip, 1)
                        triggerHaptic()
                    }
                )
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun syncCurrentTextFromConnection() {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)
        currentText = extracted?.text?.toString() ?: ""
    }

    private fun handleKeyPress(text: String) {
        triggerHaptic()
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun handleBackspace() {
        triggerHaptic()
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNull_or_empty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun handleEnter() {
        triggerHaptic()
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun performCasualTranslation() {
        triggerHaptic()
        syncCurrentTextFromConnection()
        val textToTranslate = currentText.ifBlank {
            currentInputConnection?.getSelectedText(0)?.toString() ?: ""
        }

        if (textToTranslate.isBlank()) return

        isLoading = true
        scope.launch {
            val customKey = dataStoreManager.customApiKey.first()
            val result = repository.translateToCasualEnglish(textToTranslate, selectedTone, customKey)
            isLoading = false
            result.onSuccess { casual ->
                translatedText = casual
            }.onFailure {
                translatedText = textToTranslate
            }
        }
    }

    private fun applyTranslatedText(replacementText: String) {
        triggerHaptic()
        val ic = currentInputConnection ?: return
        // Select all text in the field and replace with translated casual English
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)
        val fullLength = extracted?.text?.length ?: 0
        if (fullLength > 0) {
            ic.setSelection(0, fullLength)
            ic.commitText(replacementText, 1)
        } else {
            ic.commitText(replacementText, 1)
        }
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun updateClipboardItems() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val primaryClip = clipboard?.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                clipboardClips.clear()
                for (i in 0 until primaryClip.itemCount) {
                    val clipText = primaryClip.getItemAt(i).text?.toString()
                    if (!clipText.isNullOrBlank()) {
                        val validText = clipText
                        if (!clipboardClips.contains(validText)) {
                            clipboardClips.add(validText)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun triggerHaptic() {
        if (!hapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {
        }
    }

    private fun CharSequence?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
    private fun CharSequence?.isNull_or_blank(): Boolean = this == null || this.isBlank()

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
    }
}
