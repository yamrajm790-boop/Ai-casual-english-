const express = require('express');
const { handleTranslate } = require('../controllers/translateController');
const { translateLimiter } = require('../middleware/rateLimiter');

const router = express.Router();

router.post('/translate', translateLimiter, handleTranslate);

module.exports = router;
