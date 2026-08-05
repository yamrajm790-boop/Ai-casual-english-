const { translateWithOpenRouter } = require('../services/openRouterService');
const logger = require('../utils/logger');

async function handleTranslate(req, res) {
  try {
    const { text } = req.body;

    if (!text || typeof text !== 'string' || text.trim().length === 0) {
      return res.status(400).json({
        translated: null,
        error: "Invalid input. 'text' field is required and must be a non-empty string."
      });
    }

    const cleanInput = text.trim().slice(0, 1000); // Limit length to prevent abuse
    logger.info("Processing translation request", { inputLength: cleanInput.length });

    const translatedText = await translateWithOpenRouter(cleanInput);

    return res.status(200).json({
      translated: translatedText
    });
  } catch (error) {
    logger.error("Controller translation error", error);
    return res.status(500).json({
      translated: null,
      error: "Internal server error while translating."
    });
  }
}

module.exports = { handleTranslate };
