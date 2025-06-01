package org.abr.memearenabot.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    private InputValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new InputValidator();
    }

    @Test
    public void testValidateAiDescription_Valid() {
        String description = "This is a valid description for AI meme generation.";
        InputValidator.ValidationResult result = validator.validateAiDescription(description);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidateAiDescription_Empty() {
        String description = "";
        InputValidator.ValidationResult result = validator.validateAiDescription(description);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateAiDescription_Null() {
        InputValidator.ValidationResult result = validator.validateAiDescription(null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateAiDescription_TooLong() {
        // Create a description that is too long (over 500 characters)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append("a");
        }
        String description = sb.toString();
        
        InputValidator.ValidationResult result = validator.validateAiDescription(description);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("too long"));
    }

    @Test
    public void testValidateTemplateId_Valid() {
        String templateId = "drake";
        InputValidator.ValidationResult result = validator.validateTemplateId(templateId);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidateTemplateId_Empty() {
        String templateId = "";
        InputValidator.ValidationResult result = validator.validateTemplateId(templateId);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateTemplateId_Null() {
        InputValidator.ValidationResult result = validator.validateTemplateId(null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateTemplateId_InvalidFormat() {
        // Template ID with invalid characters
        String templateId = "drake@!#$";
        InputValidator.ValidationResult result = validator.validateTemplateId(templateId);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Invalid template ID format"));
    }

    @Test
    public void testValidateTemplateTextLines_Valid() {
        List<String> textLines = Arrays.asList("Line 1", "Line 2");
        InputValidator.ValidationResult result = validator.validateTemplateTextLines(textLines);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidateTemplateTextLines_Empty() {
        List<String> textLines = Collections.emptyList();
        InputValidator.ValidationResult result = validator.validateTemplateTextLines(textLines);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateTemplateTextLines_Null() {
        InputValidator.ValidationResult result = validator.validateTemplateTextLines(null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateTemplateTextLines_TooManyLines() {
        // Create a list with too many text lines (over 10)
        List<String> textLines = Arrays.asList(
            "Line 1", "Line 2", "Line 3", "Line 4", "Line 5",
            "Line 6", "Line 7", "Line 8", "Line 9", "Line 10", "Line 11"
        );
        
        InputValidator.ValidationResult result = validator.validateTemplateTextLines(textLines);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Too many text lines"));
    }

    @Test
    public void testValidateTemplateTextLines_LineTooLong() {
        // Create a line that is too long (over 100 characters)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("a");
        }
        List<String> textLines = Arrays.asList("Line 1", sb.toString());
        
        InputValidator.ValidationResult result = validator.validateTemplateTextLines(textLines);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Line is too long"));
    }

    @Test
    public void testValidateVoiceData_Valid() {
        byte[] voiceData = "This is test voice data".getBytes();
        InputValidator.ValidationResult result = validator.validateVoiceData(voiceData);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidateVoiceData_Empty() {
        byte[] voiceData = new byte[0];
        InputValidator.ValidationResult result = validator.validateVoiceData(voiceData);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateVoiceData_Null() {
        InputValidator.ValidationResult result = validator.validateVoiceData(null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateUrl_Valid() {
        String url = "https://example.com/image.jpg";
        InputValidator.ValidationResult result = validator.validateUrl(url);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    public void testValidateUrl_Empty() {
        String url = "";
        InputValidator.ValidationResult result = validator.validateUrl(url);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateUrl_Null() {
        InputValidator.ValidationResult result = validator.validateUrl(null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("empty"));
    }

    @Test
    public void testValidateUrl_InvalidFormat() {
        String url = "not-a-valid-url";
        InputValidator.ValidationResult result = validator.validateUrl(url);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Invalid URL format"));
    }

    @Test
    public void testValidationResultStaticMethods() {
        InputValidator.ValidationResult success = InputValidator.ValidationResult.success();
        InputValidator.ValidationResult error = InputValidator.ValidationResult.error("Test error");
        
        assertTrue(success.isValid());
        assertNull(success.getErrorMessage());
        
        assertFalse(error.isValid());
        assertEquals("Test error", error.getErrorMessage());
    }
} 