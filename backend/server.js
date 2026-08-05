require('dotenv').config();
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const translateRoutes = require('./routes/translateRoutes');
const logger = require('./utils/logger');

const app = express();
const PORT = process.env.PORT || 10000;

// Security & Middleware
app.use(helmet());
app.use(cors({ origin: '*' }));
app.use(express.json({ limit: '100kb' }));

// Health Check Endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'ok',
    service: 'AI Casual English Keyboard Backend',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.use('/api', translateRoutes);

// Global Error Handler
app.use((err, req, res, next) => {
  logger.error('Unhandled server error', err);
  res.status(500).json({
    translated: null,
    error: 'Internal server error.'
  });
});

app.listen(PORT, () => {
  logger.info(`Server running on port ${PORT}`);
});
