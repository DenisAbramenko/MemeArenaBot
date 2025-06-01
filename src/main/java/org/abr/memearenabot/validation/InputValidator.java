package org.abr.memearenabot.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for user input
 */
@Component
public class InputValidator {
    private static final Logger logger = LoggerFactory.getLogger(InputValidator.class);

    // Maximum length for text descriptions
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    // Maximum number of text lines for templates
    private static final int MAX_TEMPLATE_LINES = 10;

    // Maximum length for each line in template
    private static final int MAX_LINE_LENGTH = 100;

    // Regex for validating template IDs
    private static final Pattern TEMPLATE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,50}$");

    // Regex for validating URLs
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    /**
     * Validate AI description
     */
    public ValidationResult validateAiDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            logger.debug("AI description validation failed: empty description");
            return ValidationResult.error("Description cannot be empty");
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            logger.debug("AI description validation failed: description too long ({} characters)",
                    description.length());
            return ValidationResult.error("Description is too long (max " + MAX_DESCRIPTION_LENGTH + " characters)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate template ID
     */
    public ValidationResult validateTemplateId(String templateId) {
        if (templateId == null || templateId.trim().isEmpty()) {
            logger.debug("Template ID validation failed: empty ID");
            return ValidationResult.error("Template ID cannot be empty");
        }

        if (!TEMPLATE_ID_PATTERN.matcher(templateId).matches()) {
            logger.debug("Template ID validation failed: invalid format");
            return ValidationResult.error("Invalid template ID format");
        }

        return ValidationResult.success();
    }

    /**
     * Validate template text lines
     */
    public ValidationResult validateTemplateTextLines(List<String> textLines) {
        if (textLines == null || textLines.isEmpty()) {
            logger.debug("Template text validation failed: empty lines");
            return ValidationResult.error("Text lines cannot be empty");
        }

        if (textLines.size() > MAX_TEMPLATE_LINES) {
            logger.debug("Template text validation failed: too many lines ({} lines)", textLines.size());
            return ValidationResult.error("Too many text lines (max " + MAX_TEMPLATE_LINES + " lines)");
        }

        for (String line : textLines) {
            if (line.length() > MAX_LINE_LENGTH) {
                logger.debug("Template text validation failed: line too long ({} characters)", line.length());
                return ValidationResult.error("Line is too long (max " + MAX_LINE_LENGTH + " characters)");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validate voice data
     */
    public ValidationResult validateVoiceData(byte[] voiceData) {
        if (voiceData == null || voiceData.length == 0) {
            logger.debug("Voice data validation failed: empty data");
            return ValidationResult.error("Voice data cannot be empty");
        }

        // Maximum voice message size (10MB)
        if (voiceData.length > 10 * 1024 * 1024) {
            logger.debug("Voice data validation failed: data too large ({} bytes)", voiceData.length);
            return ValidationResult.error("Voice message is too large (max 10MB)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate URL
     */
    public ValidationResult validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.debug("URL validation failed: empty URL");
            return ValidationResult.error("URL cannot be empty");
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            logger.debug("URL validation failed: invalid format");
            return ValidationResult.error("Invalid URL format");
        }

        return ValidationResult.success();
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
} 