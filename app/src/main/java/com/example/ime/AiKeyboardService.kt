package com.example.ime

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import kotlinx.coroutines.withContext
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
import com.example.MainActivity
import com.example.data.local.DataStoreManager
import com.example.data.repository.TranslationRepository
import com.example.data.repository.TranslationResult
import com.example.ime.components.getLongPressOptionsForKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiKeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var autoTranslationJob: Job? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var repository: TranslationRepository

    private var uiState by mutableStateOf(KeyboardUiState())

    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        dataStoreManager = DataStoreManager(applicationContext)
        repository = TranslationRepository(applicationContext)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Observe DataStore preferences
        serviceScope.launch {
            launch {
                dataStoreManager.themeMode.collect { mode ->
                    uiState = uiState.copy(darkTheme = mode != "LIGHT")
                }
            }
            launch {
                dataStoreManager.vibrationEnabled.collect { enabled ->
                    uiState = uiState.copy(vibrationEnabled = enabled)
                }
            }
            launch {
                dataStoreManager.soundEnabled.collect { enabled ->
                    uiState = uiState.copy(soundEnabled = enabled)
                }
            }
            launch {
                dataStoreManager.backendUrl.collect { url ->
                    uiState = uiState.copy(backendUrl = url)
                }
            }
            launch {
                dataStoreManager.geminiApiKey.collect { key ->
                    uiState = uiState.copy(geminiKey = key)
                }
            }
        }
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
        }

        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewTreeLifecycleOwner(this@AiKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@AiKeyboardService)
            setViewTreeViewModelStoreOwner(this@AiKeyboardService)

            setContent {
                KeyboardView(
                    uiState = uiState,
                    onKeyTap = ::handleKeyTap,
                    onKeyLongPress = ::handleKeyLongPress,
                    onLongPressOptionSelected = ::handleLongPressOptionSelected,
                    onDismissLongPress = {
                        uiState = uiState.copy(longPressKey = null, longPressOptions = emptyList())
                    },
                    onShiftTap = ::handleShiftTap,
                    onDeleteTap = ::handleDeleteTap,
                    onEnterTap = ::handleEnterTap,
                    onSpaceSwipe = ::handleSpaceSwipe,
                    onGlobeTap = ::handleGlobeTap,
                    onGlobeLongPress = ::handleGlobeLongPress,
                    onAiTranslateClick = ::performAiTranslation,
                    onSendEnglishPreview = ::handleSendEnglishPreview,
                    onClearInput = ::handleClearInput,
                    onSuggestionClick = ::handleSuggestionClick,
                    onOpenSettingsClick = ::openAppSettings,
                    onOpenClipboardClick = {
                        uiState = uiState.copy(keyMode = KeyMode.CLIPBOARD)
                    },
                    onSwitchMode = { newMode ->
                        playKeyEffects()
                        uiState = uiState.copy(keyMode = newMode)
                    }
                )
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        updateCurrentComposingText()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        updateCurrentComposingText()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    private fun handleKeyTap(char: String) {
        playKeyEffects()
        currentInputConnection?.commitText(char, 1)

        // Reset shift if it was single uppercase shift
        if (uiState.keyMode == KeyMode.QWERTY_UPPER) {
            uiState = uiState.copy(keyMode = KeyMode.QWERTY_LOWER)
        }
        updateCurrentComposingText()
    }

    private fun handleKeyLongPress(char: String) {
        playKeyEffects()
        val options = getLongPressOptionsForKey(char)
        if (options.isNotEmpty()) {
            uiState = uiState.copy(
                longPressKey = char,
                longPressOptions = options
            )
        }
    }

    private fun handleLongPressOptionSelected(option: String) {
        playKeyEffects()
        currentInputConnection?.commitText(option, 1)
        uiState = uiState.copy(longPressKey = null, longPressOptions = emptyList())
        updateCurrentComposingText()
    }

    private fun handleShiftTap() {
        playKeyEffects()
        val nextMode = when (uiState.keyMode) {
            KeyMode.QWERTY_LOWER -> KeyMode.QWERTY_UPPER
            KeyMode.QWERTY_UPPER -> KeyMode.QWERTY_CAPS_LOCK
            KeyMode.QWERTY_CAPS_LOCK -> KeyMode.QWERTY_LOWER
            else -> KeyMode.QWERTY_UPPER
        }
        uiState = uiState.copy(keyMode = nextMode)
    }

    private fun handleDeleteTap() {
        playKeyEffects()
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        updateCurrentComposingText()
    }

    private fun handleEnterTap() {
        playKeyEffects()
        val ic = currentInputConnection ?: return

        if (uiState.englishPreviewText.isNotBlank()) {
            handleSendEnglishPreview()
            return
        }

        val typed = uiState.currentComposingText
        if (typed.isNotBlank()) {
            autoTranslationJob?.cancel()
            uiState = uiState.copy(isTranslating = true)

            serviceScope.launch {
                val backendUrl = dataStoreManager.backendUrl.first()
                val geminiKey = dataStoreManager.geminiApiKey.first()

                val result = repository.translateText(
                    text = typed,
                    backendUrl = backendUrl,
                    customGeminiKey = geminiKey
                )

                uiState = uiState.copy(isTranslating = false)

                when (result) {
                    is TranslationResult.Success -> {
                        val translatedText = result.translatedText
                        ic.deleteSurroundingText(typed.length, 0)
                        ic.commitText(translatedText, 1)
                        uiState = uiState.copy(
                            currentComposingText = "",
                            englishPreviewText = ""
                        )
                    }
                    is TranslationResult.Error -> {
                        sendDefaultEnterOrAction(ic)
                    }
                }
            }
            return
        }

        sendDefaultEnterOrAction(ic)
    }

    private fun sendDefaultEnterOrAction(ic: InputConnection) {
        val editorInfo = currentInputEditorInfo
        val action = if (editorInfo != null) {
            editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        } else {
            EditorInfo.IME_ACTION_NONE
        }

        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun handleSendEnglishPreview() {
        playKeyEffects()
        val ic = currentInputConnection ?: return
        val preview = uiState.englishPreviewText.trim()
        val typed = uiState.currentComposingText

        if (preview.isNotBlank()) {
            // Delete original typed text before cursor from target application
            if (typed.isNotBlank()) {
                ic.deleteSurroundingText(typed.length, 0)
            }
            // Commit ONLY the English Preview
            ic.commitText(preview, 1)
            autoTranslationJob?.cancel()
            uiState = uiState.copy(
                currentComposingText = "",
                englishPreviewText = "",
                isTranslating = false
            )
        } else if (typed.isNotBlank()) {
            performAiTranslation()
        }
    }

    private fun handleClearInput() {
        playKeyEffects()
        val ic = currentInputConnection ?: return
        val typed = uiState.currentComposingText
        if (typed.isNotBlank()) {
            ic.deleteSurroundingText(typed.length, 0)
        }
        autoTranslationJob?.cancel()
        uiState = uiState.copy(
            currentComposingText = "",
            englishPreviewText = "",
            isTranslating = false
        )
    }

    private fun handleSpaceSwipe(direction: Int) {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(100, 0) ?: ""
        val textAfter = ic.getTextAfterCursor(100, 0) ?: ""
        val currentPos = textBefore.length

        val newPos = (currentPos + direction).coerceIn(0, textBefore.length + textAfter.length)
        ic.setSelection(newPos, newPos)
    }

    private fun handleGlobeTap() {
        playKeyEffects()
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            val token = window?.window?.attributes?.token
            if (imm != null && token != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    switchToNextInputMethod(false)
                } else {
                    @Suppress("DEPRECATION")
                    imm.switchToNextInputMethod(token, false)
                }
            }
        } catch (e: Exception) {
            Log.e("AiKeyboardService", "Error switching input method", e)
        }
    }

    private fun handleGlobeLongPress() {
        playKeyEffects()
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.showInputMethodPicker()
        } catch (e: Exception) {
            Log.e("AiKeyboardService", "Error showing input method picker", e)
        }
    }

    private fun handleSuggestionClick(suggestion: String) {
        playKeyEffects()
        currentInputConnection?.commitText("$suggestion ", 1)
        updateCurrentComposingText()
    }

    private fun updateCurrentComposingText() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString() ?: ""
        val before = ic.getTextBeforeCursor(300, 0)?.toString() ?: ""

        val activeText = if (selected.isNotBlank()) selected else before
        val previousText = uiState.currentComposingText

        uiState = uiState.copy(
            currentComposingText = activeText,
            activeSuggestions = generateWordSuggestions(before)
        )

        if (activeText.isBlank()) {
            autoTranslationJob?.cancel()
            uiState = uiState.copy(englishPreviewText = "", isTranslating = false)
        } else if (activeText != previousText) {
            triggerAutoTranslation(activeText)
        }
    }

    private fun triggerAutoTranslation(textToTranslate: String) {
        autoTranslationJob?.cancel()
        if (textToTranslate.isBlank()) return

        autoTranslationJob = serviceScope.launch {
            delay(300) // 300ms typing debounce
            uiState = uiState.copy(isTranslating = true)

            val backendUrl = dataStoreManager.backendUrl.first()
            val geminiKey = dataStoreManager.geminiApiKey.first()

            val result = repository.translateText(
                text = textToTranslate,
                backendUrl = backendUrl,
                customGeminiKey = geminiKey
            )

            when (result) {
                is TranslationResult.Success -> {
                    uiState = uiState.copy(
                        englishPreviewText = result.translatedText,
                        isTranslating = false
                    )
                }
                is TranslationResult.Error -> {
                    uiState = uiState.copy(isTranslating = false)
                    withContext(Dispatchers.Main) {
                        try {
                            Toast.makeText(this@AiKeyboardService, "Translation: ${result.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("AiKeyboardService", "Could not show error toast: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun generateWordSuggestions(contextText: String): List<String> {
        val lastWord = contextText.split(" ", "\n").lastOrNull()?.lowercase() ?: ""
        if (lastWord.isBlank()) return emptyList()

        val dictionary = listOf(
            "heading", "going", "office", "already", "eating", "thanks", "awesome",
            "working", "reaching", "soon", "later", "tonight", "tomorrow", "talking"
        )
        return dictionary.filter { it.startsWith(lastWord) && it != lastWord }.take(3)
    }

    private fun performAiTranslation() {
        val ic = currentInputConnection ?: return
        playKeyEffects()

        val selectedText = ic.getSelectedText(0)?.toString()
        val beforeText = ic.getTextBeforeCursor(500, 0)?.toString()

        val targetText = if (!selectedText.isNullOrBlank()) {
            selectedText.trim()
        } else if (!beforeText.isNullOrBlank()) {
            beforeText.trim()
        } else {
            ""
        }

        if (targetText.isBlank()) return

        uiState = uiState.copy(isTranslating = true)

        serviceScope.launch {
            val backendUrl = dataStoreManager.backendUrl.first()
            val geminiKey = dataStoreManager.geminiApiKey.first()

            val result = repository.translateText(
                text = targetText,
                backendUrl = backendUrl,
                customGeminiKey = geminiKey
            )

            uiState = uiState.copy(isTranslating = false)

            when (result) {
                is TranslationResult.Success -> {
                    val translatedText = result.translatedText
                    if (!selectedText.isNullOrBlank()) {
                        ic.commitText(translatedText, 1)
                    } else if (!beforeText.isNullOrBlank()) {
                        // Clear previous text before cursor
                        ic.deleteSurroundingText(beforeText.length, 0)
                        ic.commitText(translatedText, 1)
                    }
                    updateCurrentComposingText()
                }
                is TranslationResult.Error -> {
                    Log.e("AiKeyboardService", "Translation error: ${result.message}")
                    withContext(Dispatchers.Main) {
                        try {
                            Toast.makeText(this@AiKeyboardService, "Translation: ${result.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("AiKeyboardService", "Could not show error toast: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun playKeyEffects() {
        if (uiState.vibrationEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(18)
                }
            } catch (e: Exception) {
                // Ignore vibration errors
            }
        }

        if (uiState.soundEnabled) {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        super.onDestroy()
    }
}
