package com.wk.ti.util;

public class StringUtil {

    private StringUtil() {}

    public static String generateChatName(String question) {
        String trimmed = question.trim();
        // Remove trailing punctuation if question is short
        if (trimmed.length() <= 50) {
            return cutLastPunctuationSymbol(trimmed);
        }
        // Find last sentence end
        int sentenceEnd = Math.max(
                Math.max(trimmed.lastIndexOf('.'), trimmed.lastIndexOf('?')),
                Math.max(trimmed.lastIndexOf('!'), trimmed.lastIndexOf(';'))
        );
        if (sentenceEnd > 0 && sentenceEnd <= 50) {
            return cutLastPunctuationSymbol(trimmed.substring(0, sentenceEnd + 1).trim());
        }
        // Otherwise, cut by last whitespace within 50 chars
        int lastSpace = trimmed.lastIndexOf(' ', 50);
        if (lastSpace > 0) {
            return trimmed.substring(0, lastSpace).trim();
        }
        // Fallback: hard cut at 50 chars
        return trimmed.substring(0, 50).trim();
    }

    private static String cutLastPunctuationSymbol(String trimmed) {
        if (trimmed.endsWith(".") || trimmed.endsWith("?") || trimmed.endsWith("!") || trimmed.endsWith(";")) {
            return trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

}
