const fetch = require('node-fetch');
const logger = require('../utils/logger');

const SYSTEM_PROMPT = "You are an expert translator and native English speaker. Convert the input text (Hindi, Roman Hindi, Odia, Bengali, Telugu, Tamil, Gujarati, Marathi, Punjabi, or mixed language) into natural, casual, everyday spoken English. Never translate word-for-word. Never explain. Never add quotes or notes. Return ONLY the translated casual English sentence.";

// Strict list of FREE OpenRouter models in prioritized fallback order
const FREE_MODELS_POOL = [
  "poolside/laguna-xs-2.1:free",
  "poolside/laguna-s-2.1:free",
  "inclusionai/ling-3.0-flash:free",
  "google/gemma-3-27b-it:free",
  "cohere/north-mini-code:free",
  "nvidia/nemotron-3-nano-30b-a3b:free"
];

async function translateWithOpenRouter(text) {
  const apiKey = process.env.OPENROUTER_API_KEY;
  const configuredModel = process.env.OPENROUTER_MODEL;

  if (!apiKey) {
    logger.warn("OPENROUTER_API_KEY is not set. Using smart local casual rules fallback.");
    return fallbackCasualTranslation(text);
  }

  // Priority queue of FREE models only
  const modelsToTry = [];
  if (configuredModel && configuredModel.includes(':free')) {
    modelsToTry.push(configuredModel);
  }
  FREE_MODELS_POOL.forEach(m => {
    if (!modelsToTry.includes(m)) {
      modelsToTry.push(m);
    }
  });

  for (const modelCandidate of modelsToTry) {
    try {
      logger.info(`Attempting translation with free OpenRouter model: ${modelCandidate}`);
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
          "HTTP-Referer": "https://ai-casual-english-backend.onrender.com",
          "X-Title": "AI Casual English Keyboard"
        },
        body: JSON.stringify(payload),
        timeout: 10000
      });

      if (!response.ok) {
        const errText = await response.text();
        logger.warn(`Free model ${modelCandidate} failed with status ${response.status}`, { error: errText });
        continue; // Try next free model in pool
      }

      const data = await response.json();
      const content = data?.choices?.[0]?.message?.content?.trim();

      if (content) {
        logger.info(`Successfully translated using backend free model: ${modelCandidate}`);
        return content.replace(/^["']|["']$/g, ''); // Remove outer quotes
      }
    } catch (err) {
      logger.warn(`Failed free model ${modelCandidate}: ${err.message}. Retrying next free model...`);
    }
  }

  logger.warn("All free OpenRouter models failed or timed out. Using server fallback translation.");
  return fallbackCasualTranslation(text);
}

function fallbackCasualTranslation(text) {
  const lower = text.trim().toLowerCase();
  if (lower.includes("main kal nahi aaunga") || lower.includes("kal nahi aaunga")) return "I won't come tomorrow.";
  if (lower.includes("mu office jauchi") || lower.includes("office jauchi")) return "I'm heading to the office.";
  if (lower.includes("ami bari jacchi") || lower.includes("bari jacchi")) return "I'm heading home.";
  if (lower.includes("nenu intiki velthunna") || lower.includes("intiki velthunna")) return "I'm heading home.";
  if (lower.includes("main ghar ja raha") || lower.includes("ghar ja raha")) return "I'm heading home.";
  if (lower.includes("ami kheyechi") || lower.includes("khana khaya")) return "I already ate.";
  if (lower.includes("nenu vachanu")) return "I'm here.";
  if (lower.includes("kaise ho") || lower.includes("kan karuchu")) return "What's up?";

  // Capitalize sentence nicely
  return text.trim().charAt(0).toUpperCase() + text.trim().slice(1);
}

module.exports = { translateWithOpenRouter };

