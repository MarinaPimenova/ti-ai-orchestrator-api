package com.wk.ti.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Slf4j
public class ParserUtil {
    private static final String START = "[";
    private static final String END = "]";
    private static final String JSON_START = "{";
    private static final String JSON_END = "}";
    private static final String DELIMITER = ": ";
    private static final String WRAPPER = "\"";
    private static final String TERMINATOR = ", ";
    private static final ObjectMapper mapper = new ObjectMapper();

    private ParserUtil() {
    }

    public static String listToString(List<String> input) {
        return START +
                String.join(",", fallback(input)) +
                END;
    }

    public static String listListToString(List<List<String>> input) {
        StringBuilder sb = new StringBuilder();
        sb.append(START);
        for (List<String> row : fallback(input)) {
            sb.append(listToString(row));
        }
        sb.append(END);
        return sb.toString();
    }

    private static <T> List<T> fallback(List<T> input) {
        return input == null ? List.of() : input;
    }

    public static String listToJson(List<String> headers, List<List<String>> input, String objectName) {
        StringBuilder sb = new StringBuilder();

        sb.append(JSON_START);
        for (int k = 0; k < input.size(); k++) {
            List<String> row = input.get(k);
            sb.append(WRAPPER).append(objectName).append(WRAPPER).append(DELIMITER).append(JSON_START);
            for (int i = 0; i < headers.size() && i < row.size(); i++) {
                sb.append(WRAPPER).append(headers.get(i)).append(WRAPPER).append(DELIMITER)
                        .append(WRAPPER).append(row.get(i)).append(WRAPPER)
                        .append(i == (headers.size() - 1) ? "" : TERMINATOR);
            }
            sb.append(JSON_END).append(k == (input.size() - 1) ? "" : TERMINATOR);
        }
        sb.append(JSON_END);
        return sb.toString();
    }

    public static boolean containsAnyWord(String sentence, List<String> words) {
        if (sentence == null || sentence.isBlank() || words == null || words.isEmpty()) {
            return false;
        }

        // Normalize the sentence (optional: lowercasing for case-insensitive matching)
        String normalized = sentence.toLowerCase();

        return words.stream()
                .filter(w -> w != null && !w.isBlank())
                .map(String::toLowerCase)
                .map(Pattern::quote) // escape regex meta chars
                .map(word -> Pattern.compile("\\b" + word + "\\b"))
                .anyMatch(p -> p.matcher(normalized).find());
    }

    public static String getContentType(File file) {
        // Detect MIME type based on file extension
        String contentType = null;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.warn("Unable to determine content type automatically for file {}: {}", file.getName(), e.getMessage());
        }

        return contentType == null ? getFallbackContentType(file.getName()) : contentType;
    }

    private static String getFallbackContentType(String fileName) {
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("csv", "text/csv");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("xml", "application/xml");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String ext = fileName.substring(dotIndex + 1).toLowerCase();
            return mimeTypes.getOrDefault(ext, "application/octet-stream");
        }
        return "application/octet-stream"; // default binary fallback
    }


    public static <T> T parseJsonPayload(String jsonString, Class<T> valueType) {
        try {
            return mapper.readValue(jsonString, valueType);
        } catch (JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            // to not fail conversation history request due to incorrect written JSON in DB
            return null;
        }
    }
}
