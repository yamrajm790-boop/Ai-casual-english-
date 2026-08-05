const fetch = require('node-fetch');
const logger = require('../utils/logger');

const SYSTEM_PROMPT = "You are an expert translator. Convert the user's text into natural casual spoken English. Never explain. Never add notes. Return only translated sentence.";

const FREE_MODELS_POOL = [
  "inclusionai/ling-3.0-flash:free",
  "poolside/laguna-s-2.1:free",
  "poolside/laguna-xs-2.1:free",
  "google/gemma-4-26b-a4b-it:free",
  "google/gemma-4-31b-it:free",
  "cohere/north-mini-code:free",
  "nvidia/nemotron-3-nano-30b-a3b:free",
  "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
  "google/gemini-2.5-flash"
];

async function translateWithOpenRouter(text) {
  const apiKey = process.env.OPENROUTER_API_KEY;
  const configuredModel = process.env.OPENROUTER_MODEL;

  if (!apiKey) {
    logger.warn("OPENROUTER_API_KEY is not set. Using smart local casual rules fallback.");
    return fallbackCasualTranslation(text);
  }

  // Priority queue of models: process.env.OPENROUTER_MODEL first, followed by free models pool
  const modelsToTry = [];
  if (configuredModel) {
    modelsToTry.push(configuredModel);
  }
  FREE_MODELS_POOL.forEach(m => {
    if (!modelsToTry.includes(m)) {
      modelsToTry.push(m);
    }
  });

  for (const modelCandidate of modelsToTry) {
    try {
      logger.info(`Attempting translation with OpenRouter model: ${modelCandidate}`);
      const payload = {
        model: modelCandidate,
        messages: [
          { role: "system", content: SYSTEM_PROMPT },
          { role: "user", content: text }
        ],
        temperature: 0.3,
        max_tokens: 200
      };

      const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${apiKey}`,
          "Content-Type": "application/json",
          "HTTP-Referer": "https://ai-casual-english-keyboard.onrender.com",
          "X-Title": "AI Casual English Keyboard"
        },
        body: JSON.stringify(payload),
        timeout: 8000
      });

      if (!response.ok) {
        const errText = await response.text();
        logger.warn(`Model ${modelCandidate} failed with status ${response.status}`, { error: errText });
        continue; // Try next model candidate in pool
      }

      const data = await response.json();
      const content = data?.choices?.[0]?.message?.content?.trim();

      if (content) {
        logger.info(`Successfully translated using model: ${modelCandidate}`);
        return content.replace(/^["']|["']$/g, ''); // Remove outer quotes
      }
    } catch (err) {
      logger.warn(`Failed model ${modelCandidate}: ${err.message}. Trying next candidate.`);
    }
  }

  logger.warn("All OpenRouter models failed or timed out. Using local fallback translation.");
  return fallbackCasualTranslation(text);
}

function fallbackCasualTranslation(text) {
  const lower = text.trim().toLowerCase();
  if (lower.includes("main ghar ja raha") || lower.includes("mu office jauchi")) return "I'm heading home.";
  if (lower.includes("ami kheyechi")) return "I already ate.";
  if (lower.includes("nenu vachanu")) return "I'm here.";
  if (lower.includes("kaise ho") || lower.includes("kan karuchu")) return "What's up?";

  // Capitalize sentence nicely
  return text.trim().charAt(0).toUpperCase() + text.trim().slice(1);
}

module.exports = { translateWithOpenRouter };
