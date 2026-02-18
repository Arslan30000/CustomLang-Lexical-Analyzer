package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ManualScanner {
    private String source;
    private int current = 0;
    private int line = 1;
    private int col = 1;
    private int start = 0;
    
    // Statistics
    private int totalTokens = 0;
    private int commentsRemoved = 0;
    private int[] tokenCounts = new int[TokenType.values().length];

    private SymbolTable symbolTable;
    private ErrorHandler errorHandler; 

    public ManualScanner(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        this.source = new String(bytes).replace("\r\n", "\n").replace("\r", "\n");
        this.symbolTable = new SymbolTable();
        this.errorHandler = new ErrorHandler();
    }

    public List<Token> scanTokens() {
        List<Token> tokens = new ArrayList<>();
        
        while (!isAtEnd()) {
            start = current;
            char c = peek();

            // 1. Whitespace
            if (Character.isWhitespace(c)) {
                handleWhitespace();
                continue;
            }

            // 2. Single Line Comments (##)
            if (c == '#' && peekNext() == '#') {
                scanSingleLineComment(); 
                continue;
            }

            Token token = null;

            // 3. Logic Dispatch for the 7 Token Types
            if (isDigit(c) || ((c == '+' || c == '-') && isDigit(peekNext()))) {
                token = scanNumber(); // Handles Integer and Float
            }
            else if (isUpper(c)) {
                token = scanIdentifier(); // Handles Identifiers
            }
            else if (isLower(c)) {
                token = scanBoolean(); // Handles Booleans (true/false)
            }
            else if (isOperatorOrPunctuator(c)) {
                token = scanOperatorOrPunctuator(); // Handles Arith Ops & Punctuators
            }
            else {
                // ERROR RECOVERY: Invalid Character
                String invalidChar = String.valueOf(advance());
                errorHandler.addError("Invalid Char", line, col - 1, invalidChar, "Character not in alphabet");
                continue; 
            }

            if (token != null && token.getType() != TokenType.ERROR) {
                tokens.add(token);
                totalTokens++;
                tokenCounts[token.getType().ordinal()]++;
                System.out.println(token); 
            }
        }

        printStatistics();
        errorHandler.printErrors(); 
        symbolTable.printTable();
        return tokens;
    }

    // 1. IDENTIFIER: [A-Z][a-z0-9]{0,30}
    private Token scanIdentifier() {
        advance(); // consume the Uppercase letter
        
        while (isLower(peek()) || isDigit(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        
        // Error: Length > 31
        if (text.length() > 31) {
            errorHandler.addError("Invalid ID", line, col - text.length(), text.substring(0, 10) + "...", "Identifier exceeds 31 characters");
            return new Token(TokenType.ERROR, text, line, col - text.length());
        }

        symbolTable.addIdentifier(text, line);
        return new Token(TokenType.IDENTIFIER, text, line, col - text.length());
    }

    // 2. BOOLEAN LITERAL: (true|false)
    private Token scanBoolean() {
        advance();
        while (isLower(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        if (text.equals("true") || text.equals("false")) {
            return new Token(TokenType.BOOLEAN_LITERAL, text, line, col - text.length());
        }

        // If it starts with lowercase but isn't true/false, it's an invalid identifier
        errorHandler.addError("Invalid ID", line, col - text.length(), text, "Identifiers must start with Uppercase");
        return new Token(TokenType.ERROR, text, line, col - text.length());
    }

    // 3 & 4. INTEGER LITERAL AND FLOATING POINT LITERAL
    private Token scanNumber() {
        if (peek() == '+' || peek() == '-') advance();
        
        while (isDigit(peek())) advance();

        if (peek() == '.') {
            if (isDigit(peekNext())) {
                advance(); 
                while (isDigit(peek())) advance();

                if (peek() == '.') {
                    String malformed = ".";
                    advance(); 
                    while(isDigit(peek())) { malformed += advance(); }
                    errorHandler.addError("Malformed Literal", line, col, malformed, "Float cannot have multiple decimal points");
                }
            } else {
                errorHandler.addError("Malformed Literal", line, col, ".", "Float must have digits after decimal");
            }
        }
        
        if (peek() == 'e' || peek() == 'E') {
            advance(); 
            if (peek() == '+' || peek() == '-') advance(); 
            while (isDigit(peek())) advance(); 
        }
        
        String text = source.substring(start, current);
        boolean isFloat = text.contains(".") || text.contains("e") || text.contains("E");
        
        return isFloat ? new Token(TokenType.FLOAT_LITERAL, text, line, col - text.length()) 
                       : new Token(TokenType.INTEGER_LITERAL, text, line, col - text.length());
    }

    // 5. SINGLE LINE COMMENT: ##[^\n]*
    private void scanSingleLineComment() {
        commentsRemoved++;
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }
    
    private boolean isOperatorOrPunctuator(char c) {
        return "()[]{};:,+-*/%".indexOf(c) != -1;
    }

    // 6 & 7. ARITHMETIC OPERATORS AND PUNCTUATORS
    private Token scanOperatorOrPunctuator() {
        char c = advance();
        String text = String.valueOf(c);
        
        switch (c) {
            case '(': case ')': case '{': case '}': case '[': case ']': 
            case ',': case ';': case ':': 
                return new Token(TokenType.PUNCTUATOR, text, line, col - 1);
            
            case '+': case '-': case '*': case '/': case '%':
                return new Token(TokenType.ARITHMETIC_OP, text, line, col - 1);
        }
        
        return new Token(TokenType.ERROR, "Unknown: " + text, line, col - 1);
    }

    // --- UTILITY HELPERS ---
    private void handleWhitespace() { 
        char c = advance(); 
        if (c == '\n') { line++; col = 1; } 
    }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isUpper(char c) { return c >= 'A' && c <= 'Z'; }
    private boolean isLower(char c) { return c >= 'a' && c <= 'z'; }
    private char advance() { col++; return source.charAt(current++); }
    private char peek() { return isAtEnd() ? '\0' : source.charAt(current); }
    private char peekNext() { return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1); }
    private boolean isAtEnd() { return current >= source.length(); }
    
    private void printStatistics() {
        System.out.println("\n--- Scanner Statistics ---");
        System.out.println("Total Tokens: " + totalTokens);
        System.out.println("Lines Processed: " + line);
        System.out.println("Comments Removed: " + commentsRemoved);
        for (TokenType t : TokenType.values()) {
            if (t != TokenType.EOF && t != TokenType.ERROR && tokenCounts[t.ordinal()] > 0) {
                System.out.println(String.format("%-15s : %d", t, tokenCounts[t.ordinal()]));
            }
        }
        System.out.println("--------------------------\n");
    }

    public static void main(String[] args) {
        try {
            String file = "tests/test5.lang"; 
            if (args.length > 0) file = args[0];
            
            ManualScanner scanner = new ManualScanner(file);
            scanner.scanTokens();
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}