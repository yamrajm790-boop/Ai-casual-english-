const rateLimit = require('express-rate-limit');

const translateLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute
  max: 60, // Limit each IP to 60 requests per windowMs
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    translated: null,
    error: 'Too many translation requests, please try again in a moment.'
  }
});

module.exports = { translateLimiter };
