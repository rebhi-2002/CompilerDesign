package projectcompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this license header, choose License Headers in Project
 Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rebhe Ibrahim
 */
/**
 * A class to analyze tokens from a given content.
 */
public class TokenAnalyzer {

    // List of keywords in Pascal | Declare a String array for keywords
    private static final String[] KEYWORDS_ARRAY = {"program", "integer", "boolean", "begin", "end", "procedure", "if", "then", "else", "for", "true", "false", "do", "write", "mod", "div", "read", "and", "or", "not", "of", "array", ">=", "<=", "var", "*", "<>", ":=", ";", ",", "..", ">", "<", "(", ")", "[", "/", "+", "-", ".", "]", "=", ":"};
    // Method to convert the array to a List when needed
    private static final List<String> KEYWORDS = Arrays.asList(KEYWORDS_ARRAY);

    private static final List<String> OPERATORS = Arrays.asList(
            "+", "-", "*", "/", "=", "<", ">", "&&", "||", "&", "|", "!", ";", "(", ")", "{", "}", "[", "]", ",", ":", "@",
            "**", "^^", "%", "$", "#", "_", "?!", "<<", ">>", "+=", "-=", "*=", "/=", "%=", "==", "!=", "<=", ">=", "=>", "??",
            "++", "--", "===", "!==", "??=", "?.", "~", "^", ">>>", "::", "<=>", "->");

    private List<String> errors = new ArrayList<>(); // To track errors
    private List<Token> tokens = new ArrayList<>(); // Store tokens as Token objects

    /* // Pascal 
    private static final String[] KEYWORDS_ARRAY = {
        "program", "integer", "boolean", "begin", "end", "procedure", "function", "if", "then", 
        "else", "for", "true", "false", "do", "write", "mod", "div", "read", "and", "or", "not", 
        "of", "array", "var", "type", "record", "case", "while", "repeat", "until", "exit", "with", 
        "set", "file", "label", "goto", "const", "type", "implementation", "interface"
    };
    private static final List<String> KEYWORDS = Arrays.asList(KEYWORDS_ARRAY);
    private static final List<String> OPERATORS = Arrays.asList(
        "+", "-", "*", "/", "=", "<", ">", "<>", ":=", "and", "or", "not", "div", "mod", 
        ">>", "<<", "++", "--", "**", "~", ":=", "<=", ">=", "==", "<=>", "!", "&", "|"
    ); */


    // Helper function to validate identifiers (no numbers at the beginning)
    private boolean isValidIdentifier(String identifier) {
        // Check if the identifier starts with a digit
        return !Character.isDigit(identifier.charAt(0)); // Invalid if it starts with a digit
    }
    // Helper function to validate decimal numbers
    private boolean isValidDecimal(String decimal) {
        // return decimal.matches("\\-?\\d*\\.\\d+([eE][+-]?\\d+)?");
        try {
            Double.parseDouble(decimal); // Check if it can be parsed as a decimal
            return true;
        } catch (NumberFormatException e) {
            return false; // Invalid if it can't be parsed
        }
    }
    // Function to validate integer constants
    private boolean isValidInteger(String integer) {
        try {
            Integer.parseInt(integer); // Check if it can be parsed as an integer
            return true;
        } catch (NumberFormatException e) {
            return false; // Invalid if it can't be parsed
        }
    }

    public List<Token> analyze(String content) {
        tokens.clear(); // Clear previous tokens before starting new analysis
        String[] lines = content.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim().toUpperCase(); // Trim whitespace and make case-insensitive
            line = handleComments(line); // Handle comments
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }
            
            // تعديل النمط ليكون غير حساس لحالة الأحرف
            String keywordRegex = "(?i)" + String.join("|", KEYWORDS.stream().map(Pattern::quote).toArray(String[]::new));
            // جمع المعاملات من القائمة OPERATORS في تعبير Regex
            String operatorRegex = String.join("|", OPERATORS.stream().map(Pattern::quote).toArray(String[]::new));

            // إنشاء النمط (Pattern) باستخدام التعبير النمطي المبني على قوائم الكلمات المفتاحية والمعاملات
            Matcher matcher = Pattern.compile(
                "(" + keywordRegex + ")|" // Keywords
                + "([a-zA-Z_$][a-zA-Z0-9_$]*)|" // Identifiers
                + "(\\d+\\.\\d+)|" // + "(\\d+\\.\\d*([eE][+-]?\\d+)?)|" // Decimal numbers (including scientific notation)
                + "(\\d+)|" // + "(\\-?\\d+\\.\\d*|\\-?\\d+)|" // Integers (including negative and decimal)
                + "(" + operatorRegex + ")|" // Operators
                // + "(//.*|/\\*(?:.|\\n)*?\\*/)|" // Comments (single-line and multi-line)
                // + "\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'|" // Strings
                + "([^\\s]+)" // Unknown tokens
            ).matcher(line);

            // Process matches based on token type
            while (matcher.find()) {
                // Retrieve possible matches for each type of token in the current line
                String keyword = matcher.group(1);      // Match for keywords
                String identifier = matcher.group(2);   // Match for identifiers
                String decimal = matcher.group(3);      // Match for decimal numbers
                String integer = matcher.group(4);      // Match for integer constants
                String operator = matcher.group(5);     // Match for operators
                // String comments = matcher.group(6);      // Match for commentsRegex
                // String string = matcher.group(7);      // Match for stringRegex
                String unknown = matcher.group(6);      // Match for unknown symbols

                // Check for non-null matches and add them to the tokens list with the appropriate type
                if (keyword != null) {
                    tokens.add(new Token(keyword, "keyword", i + 1)); // Add keyword
                } else if (identifier != null) {
                    // Validate identifiers to prevent invalid ones starting with a digit
                    if (isValidIdentifier(identifier)) { // if (!Character.isDigit(identifier.charAt(0))) {...}
                        tokens.add(new Token(identifier, "ident", i + 1)); // Add valid identifier
                    } else {
                        tokens.add(new Token(identifier, "Invalid Identifier", (i + 1))); // Mark as invalid identifier
                    }
                } else if (decimal != null) {
                    // Validate decimal numbers
                    if (isValidDecimal(decimal)) {
                        tokens.add(new Token(decimal, "Decimal", i + 1)); // Add valid decimal number
                    } else {
                        tokens.add(new Token(decimal, "Invalid Decimal", i + 1)); // Mark as invalid decimal
                    }
                } else if (integer != null) {
                    // Validate integer constants
                    if (isValidInteger(integer)) {
                        tokens.add(new Token(integer, "Num Const", i + 1)); // Add valid integer constant
                    } else {
                        tokens.add(new Token(integer, "Invalid Integer", i + 1)); // Mark as invalid integer
                    }
                } else if (operator != null) {
                    tokens.add(new Token(operator, "Operator", i + 1)); // Add operator
                // } else if (comments != null) {
                //     tokens.add(new Token(comments, "Comment", i + 1)); // Add Comments Regex
                // } else if (string != null) {
                //     tokens.add(new Token(string, "String", i + 1)); // Add String Regex
                } else if (unknown != null) {
                    tokens.add(new Token(unknown, "Unknown", i + 1)); // Add unknown symbol
                }
            }

            // Check for unclosed brackets
            checkForUnclosedBrackets(line, i + 1);
        }
        return tokens;
    }

    public void clearTokens() {
        tokens.clear(); // Extra method to clear tokens when needed
    }

    private String handleComments(String line) {
        // Ignore multi-line comments
        line = line.replaceAll("/\\*.*?\\*/", ""); // Remove multi-line comments

        String[] parts = line.split("//"); // if (line.contains("//"))
        if (parts.length > 0) {
            line = parts[0]; // Handle single-line comments safely
        } else {
            line = ""; // If there's nothing before the comment, make line empty
        }

        // Remove multi-line comments
        line = line.replaceAll("/\\*.*?\\*/", "");
        return line;
        // return line.trim(); // إرجاع السطر بعد إزالة التعليقات
    }

    private void checkForUnclosedBrackets(String line, int lineNumber) {
        int openBraces = 0, openBrackets = 0, openParentheses = 0;
        for (char ch : line.toCharArray()) {
            switch (ch) {
                case '{':
                    openBraces++;
                    break;
                case '}':
                    openBraces--;
                    break;
                case '[':
                    openBrackets++;
                    break;
                case ']':
                    openBrackets--;
                    break;
                case '(':
                    openParentheses++;
                    break;
                case ')':
                    openParentheses--;
                    break;
            }
        }

        if (openBraces > 0) {
            errors.add("Unclosed '{' at line " + lineNumber);
        }
        if (openBrackets > 0) {
            errors.add("Unclosed '[' at line " + lineNumber);
        }
        if (openParentheses > 0) {
            errors.add("Unclosed '(' at line " + lineNumber);
        }

    }

    public List<String> getErrors() {
        return errors;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    // Token class to represent each token
    class Token {
        private String value;
        private String type;
        private int lineNumber;
        int length;

        public Token(String value, String type, int lineNumber) {
            this.value = value;
            this.type = type;
            this.lineNumber = lineNumber;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return "Token: " + value + ", Type: " + type + ", Line: " + lineNumber;
            // System.out.println("Token: " + token.getValue() + ", Type: " + token.getType() + ", Line: " + token.getLineNumber());
        }
    }
}


