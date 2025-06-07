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