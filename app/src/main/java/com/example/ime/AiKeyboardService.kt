package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.TranslationRepository
import com.example.ui.theme.AICasualEnglishKeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var audioManager: AudioManager? = null

    private var currentText by mutableStateOf("")
    private var translatedText by mutableStateOf<String?>(null)
    private var isLoading by mutableStateOf(false)
    private var selectedTone by mutableStateOf("Casual & Natural")
    private var selectedSourceLanguage by mutableStateOf("Auto-detect")
    private var autoTranslateEnabled by mutableStateOf(true)
    private var realtimePreviewEnabled by mutableStateOf(true)
    private var hapticEnabled by mutableStateOf(true)
    private var soundEnabled by mutableStateOf(true)
    private var debounceDelayMs by mutableStateOf(350)
    private var clipboardClips = mutableListOf<String>()

    private var translationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dataStoreManager = DataStoreManager(this)
        val database = AppDatabase.getDatabase(this)
        repository = TranslationRepository(database.translationHistoryDao())
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        observeSettings()
    }

    private fun observeSettings() {
        scope.launch {
            selectedTone = dataStoreManager.selectedTone.first()
            selectedSourceLanguage = dataStoreManager.selectedSourceLanguage.first()
            autoTranslateEnabled = dataStoreManager.autoTranslateEnabled.first()
            realtimePreviewEnabled = dataStoreManager.realtimePreviewEnabled.first()
            hapticEnabled = dataStoreManager.hapticFeedback.first()
            soundEnabled = dataStoreManager.soundFeedback.first()
            debounceDelayMs = dataStoreManager.debounceDelayMs.first()
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
                    selectedSourceLanguage = selectedSourceLanguage,
                    autoTranslateEnabled = autoTranslateEnabled,
                    realtimePreviewEnabled = realtimePreviewEnabled,
                    clipboardClips = clipboardClips,
                    onKeyPress = { char -> handleKeyPress(char) },
                    onBackspace = { handleBackspace() },
                    onSpace = { handleKeyPress(" ") },
                    onEnter = { handleEnter() },
                    onTranslateClick = { triggerManualTranslation() },
                    onApplyTranslation = { applyTranslatedText(it) },
                    onToneSelect = { tone ->
                        selectedTone = tone
                        scope.launch { dataStoreManager.saveSelectedTone(tone) }
                        if (currentText.isNotBlank()) {
                            triggerDebouncedTranslation()
                        }
                    },
                    onLanguageSelect = { lang ->
                        selectedSourceLanguage = lang
                        scope.launch { dataStoreManager.setSelectedSourceLanguage(lang) }
                        if (currentText.isNotBlank()) {
                            triggerDebouncedTranslation()
                        }
                    },
                    onToggleAutoTranslate = { enabled ->
                        autoTranslateEnabled = enabled
                        scope.launch { dataStoreManager.setAutoTranslateEnabled(enabled) }
                    },
                    onSelectClip = { clip ->
                        currentInputConnection?.commitText(clip, 1)
                        triggerFeedback()
                        syncCurrentTextFromConnection()
                        triggerDebouncedTranslation()
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
        translationJob?.cancel()
    }

    private fun syncCurrentTextFromConnection() {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
        currentText = extracted?.text?.toString() ?: ""
    }

    private fun handleKeyPress(text: String) {
        triggerFeedback()
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
        syncCurrentTextFromConnection()
        triggerDebouncedTranslation()
    }

    private fun handleBackspace() {
        triggerFeedback()
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        syncCurrentTextFromConnection()
        if (currentText.isBlank()) {
            translationJob?.cancel()
            translatedText = null
            isLoading = false
        } else {
            triggerDebouncedTranslation()
        }
    }

    private fun handleEnter() {
        triggerFeedback()
        val ic = currentInputConnection ?: return

        // If auto-translate is enabled or a translation preview exists, replace original with English translation
        val translationToApply = translatedText
        if ((autoTranslateEnabled || !translationToApply.isNullOrBlank()) && !translationToApply.isNullOrBlank()) {
            applyTranslatedText(translationToApply)
        }

        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        syncCurrentTextFromConnection()
        translatedText = null
    }

    private fun triggerDebouncedTranslation() {
        if (!realtimePreviewEnabled) return
        val textToTranslate = currentText.trim()
        if (textToTranslate.isBlank() || textToTranslate.length < 2) {
            translationJob?.cancel()
            translatedText = null
            isLoading = false
            return
        }

        translationJob?.cancel()
        translationJob = scope.launch {
            delay(debounceDelayMs.toLong())
            isLoading = true
            val result = repository.translateToCasualEnglish(
                originalText = textToTranslate,
                sourceLanguage = selectedSourceLanguage,
                tone = selectedTone
            )
            isLoading = false
            result.onSuccess { casual ->
                translatedText = casual
            }.onFailure {
                // If translation fails, leave preview as null
            }
        }
    }

    private fun triggerManualTranslation() {
        triggerFeedback()
        syncCurrentTextFromConnection()
        val textToTranslate = currentText.ifBlank {
            currentInputConnection?.getSelectedText(0)?.toString() ?: ""
        }

        if (textToTranslate.isBlank()) return

        isLoading = true
        scope.launch {
            val result = repository.translateToCasualEnglish(
                originalText = textToTranslate,
                sourceLanguage = selectedSourceLanguage,
                tone = selectedTone
            )
            isLoading = false
            result.onSuccess { casual ->
                translatedText = casual
            }.onFailure {
                translatedText = textToTranslate
            }
        }
    }

    private fun applyTranslatedText(replacementText: String) {
        triggerFeedback()
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
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
                        if (!clipboardClips.contains(clipText)) {
                            clipboardClips.add(clipText)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun triggerFeedback() {
        if (soundEnabled) {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f)
            } catch (_: Exception) {}
        }

        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(12)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
    }
}
