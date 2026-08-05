# AI Casual English Keyboard ⌨️✨

A production-ready Android Input Method Editor (IME) Keyboard that translates any language into natural, casual spoken English directly while typing inside any app.

---

## 🌟 Key Features

- **In-Keyboard Instant AI Translation**: Press the **✨ AI Casual** button on the suggestion strip to convert typed text or selection directly into spoken casual English without leaving the active app.
- **Natural Spoken English Style**: Outputs natural, friendly spoken expressions (e.g., *"Main ghar ja raha hoon"* ➔ *"I'm heading home."*), avoiding robotic translations.
- **Zero API Key Leak Security**: All OpenRouter API keys and Model IDs remain safely on the Node.js/Express backend service hosted on Render.
- **Full QWERTY Layout**: Features Shift, Caps Lock, Number/Symbol keypads (`?123`, `=\<`), Emoji picker panel with categories, and Clipboard manager.
- **Haptic & Sound Feedback**: Customizable vibration feedback and subtle key tap sound effects.
- **Data Persistence & History**: Local Room database keeps track of past translations with search, copy to clipboard, and delete capabilities.
- **DataStore Preferences**: Theme customization (Dark/Light), backend endpoint configuration, and fallback options.

---

## 🚀 Backend Deployment (Render & OpenRouter)

### Prerequisites
- [Render Account](https://render.com)
- [OpenRouter API Key](https://openrouter.ai/keys)

### Step-by-Step Render Deployment
1. Push this repository to GitHub.
2. In the Render Dashboard, create a **New Web Service**.
3. Connect your GitHub repository and set the **Root Directory** to `backend`.
4. Configure Build & Start Commands:
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
5. Add Environment Variables in Render:
   - `OPENROUTER_API_KEY`: Your secret OpenRouter API Key
   - `OPENROUTER_MODEL`: `google/gemini-2.5-flash` (or preferred model)
   - `PORT`: `10000`
6. Deploy the web service. Once deployed, note your service URL (e.g. `https://ai-casual-english-keyboard.onrender.com`).

---

## 📲 Installing & Enabling Keyboard on Android

1. Install the APK on your Android device.
2. Launch the **AI Casual English Keyboard** app.
3. Follow the 2-step setup wizard:
   - **Step 1**: Tap **Enable** to enable "AI Casual English Keyboard" under Android System Settings ➔ On-screen Keyboard.
   - **Step 2**: Tap **Switch** to select "AI Casual English Keyboard" as your current default input method.
4. Test typing in any text input field or using the live test playground inside the app!

---

## ⚙️ Project Architecture

```
├── app/                  # Android Kotlin Jetpack Compose IME Application
│   ├── src/main/java/com/example/
│   │   ├── data/        # Room Database, DataStore Preferences, Retrofit APIs, Repository
│   │   ├── ime/         # InputMethodService, KeyboardView, Layouts, SuggestionStrip
│   │   ├── ui/          # Home Wizard, Settings, History, About screens
│   │   └── MainActivity.kt
│   └── build.gradle.kts
├── backend/              # Node.js Express Backend Service
│   ├── controllers/     # Translate Controller
│   ├── middleware/      # Rate Limiter & Security
│   ├── routes/          # /api/translate
│   ├── services/        # OpenRouter Service & Fallbacks
│   ├── server.js        # Express Application Entry
│   ├── Dockerfile
│   └── render.yaml
└── .github/workflows/   # GitHub Actions CI/CD Pipeline
    └── build-apk.yml
```

---

## 🔒 Security & Privacy

- **No Hardcoded Secrets**: No OpenRouter keys or authorization tokens are stored in the APK.
- **Input Sanitization**: All text input is sanitized and truncated on the server before processing.
- **HTTPS Only**: All communications between the keyboard client and the translation backend use encrypted TLS/HTTPS.
