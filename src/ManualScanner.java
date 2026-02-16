
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ErrorHandler errorHandler; // New Error Handler
    private static final Set<String> keywords;

    static {
        keywords = new HashSet<>();
        keywords.add("start"); keywords.add("finish"); keywords.add("loop");
        keywords.add("condition"); keywords.add("declare"); keywords.add("output");
        keywords.add("input"); keywords.add("function"); keywords.add("return");
        keywords.add("break"); keywords.add("continue"); keywords.add("else");
        keywords.add("if"); 
    }

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

            // 2. Comments
            if (c == '#') {
                if (peekNext() == '#') {
                    scanSingleLineComment(); 
                    continue;
                } else if (peekNext() == '|') {
                    scanMultiLineComment(); 
                    continue;
                }
                // Fallthrough if it's just '#'
            }

            Token token = null;

            // 3. Logic Dispatch
            if (isDigit(c)) {
                token = scanNumber();
            } 
            else if ((c == '+' || c == '-') && isDigit(peekNext())) {
                token = scanNumber();
            }
            else if (isUpper(c)) {
                token = scanIdentifier();
            }
            else if (isLower(c)) {
                token = scanKeywordOrBool();
            }
            else if (c == '"') {
                token = scanString();
            }
            else if (c == '\'') {
                token = scanChar();
            }
            else if (isOperatorOrPunctuator(c)) {
                token = scanOperatorOrPunctuator();
            }
            else {
                // ERROR RECOVERY: Invalid Character
                String invalidChar = String.valueOf(advance());
                errorHandler.addError("Invalid Char", line, col - 1, invalidChar, "Character not in alphabet");
                continue; // Skip and continue scanning
            }

            if (token != null) {
                if (token.getType() != TokenType.ERROR) {
                    tokens.add(token);
                    totalTokens++;
                    tokenCounts[token.getType().ordinal()]++;
                    System.out.println(token); 
                }
            }
        }

        printStatistics();
        errorHandler.printErrors(); // Report all errors at the end
        symbolTable.printTable();
        return tokens;
    }

    private Token scanIdentifier() {
        advance(); 
        while (isAlphaNumeric(peek())) {
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

    private Token scanKeywordOrBool() {
        advance();
        while (isLower(peek()) || isUpper(peek()) || isDigit(peek()) || peek() == '_') {
            advance();
        }

        String text = source.substring(start, current);

        if (keywords.contains(text)) return new Token(TokenType.KEYWORD, text, line, col - text.length());
        if (text.equals("true") || text.equals("false")) return new Token(TokenType.BOOLEAN_LITERAL, text, line, col - text.length());

        // Error: Wrong starting character (Lowercase start but not a keyword)
        errorHandler.addError("Invalid ID", line, col - text.length(), text, "Identifiers must start with Uppercase");
        return new Token(TokenType.ERROR, text, line, col - text.length());
    }

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

    private Token scanString() {
        advance(); 
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') { line++; col = 1; }
            advance();
        }

        if (isAtEnd()) {
            errorHandler.addError("Unterminated", line, col, "\"", "String literal not closed");
            return new Token(TokenType.ERROR, "Unterminated", line, col);
        }

        advance(); 
        String value = source.substring(start, current);
        return new Token(TokenType.STRING_LITERAL, value, line, col - value.length());
    }

    private void scanMultiLineComment() {
        commentsRemoved++;
        advance(); advance(); 
        while (!isAtEnd()) {
            if (peek() == '\n') { line++; col = 1; }
            if (peek() == '|' && peekNext() == '#') {
                advance(); advance(); 
                return;
            }
            advance();
        }
        errorHandler.addError("Unclosed Comment", line, col, "#|", "Multi-line comment not closed");
    }
    
    private boolean isOperatorOrPunctuator(char c) {
        return "()[]{};:+-*/%!=<>&|".indexOf(c) != -1;
    }

    private Token scanChar() {
        advance(); 
        if (peek() == '\\') { advance(); advance(); } 
        else { advance(); }
        if (peek() == '\'') {
            advance(); 
            String val = source.substring(start, current);
            return new Token(TokenType.CHAR_LITERAL, val, line, col - val.length());
        }
        return new Token(TokenType.ERROR, "Invalid Char", line, col);
    }

    private Token scanOperatorOrPunctuator() {
        char c = advance();
        String text = String.valueOf(c);
        switch (c) {
            case '(': case ')': case '{': case '}': case '[': case ']': case ',': case ';': case ':': return new Token(TokenType.PUNCTUATOR, text, line, col - 1);
            case '+': if (match('+')) return makeToken(TokenType.INC_DEC_OP, "++"); if (match('=')) return makeToken(TokenType.ASSIGNMENT_OP, "+="); return makeToken(TokenType.ARITHMETIC_OP, "+");
            case '-': if (match('-')) return makeToken(TokenType.INC_DEC_OP, "--"); if (match('=')) return makeToken(TokenType.ASSIGNMENT_OP, "-="); return makeToken(TokenType.ARITHMETIC_OP, "-");
            case '*': if (match('*')) return makeToken(TokenType.ARITHMETIC_OP, "**"); if (match('=')) return makeToken(TokenType.ASSIGNMENT_OP, "*="); return makeToken(TokenType.ARITHMETIC_OP, "*");
            case '/': if (match('=')) return makeToken(TokenType.ASSIGNMENT_OP, "/="); return makeToken(TokenType.ARITHMETIC_OP, "/");
            case '%': return makeToken(TokenType.ARITHMETIC_OP, "%");
            case '=': if (match('=')) return makeToken(TokenType.RELATIONAL_OP, "=="); return makeToken(TokenType.ASSIGNMENT_OP, "=");
            case '!': if (match('=')) return makeToken(TokenType.RELATIONAL_OP, "!="); return makeToken(TokenType.LOGICAL_OP, "!");
            case '<': if (match('=')) return makeToken(TokenType.RELATIONAL_OP, "<="); return makeToken(TokenType.RELATIONAL_OP, "<");
            case '>': if (match('=')) return makeToken(TokenType.RELATIONAL_OP, ">="); return makeToken(TokenType.RELATIONAL_OP, ">");
            case '&': if (match('&')) return makeToken(TokenType.LOGICAL_OP, "&&"); break;
            case '|': if (match('|')) return makeToken(TokenType.LOGICAL_OP, "||"); break;
        }
        return new Token(TokenType.ERROR, "Unknown: " + text, line, col - 1);
    }

    private void handleWhitespace() { char c = advance(); if (c == '\n') { line++; col = 1; } }
    private void scanSingleLineComment() { commentsRemoved++; while (peek() != '\n' && !isAtEnd()) { advance(); } }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isUpper(char c) { return c >= 'A' && c <= 'Z'; }
    private boolean isLower(char c) { return c >= 'a' && c <= 'z'; }
    private boolean isAlphaNumeric(char c) { return isLower(c) || isUpper(c) || isDigit(c) || c == '_'; }
    private char advance() { col++; return source.charAt(current++); }
    private char peek() { return isAtEnd() ? '\0' : source.charAt(current); }
    private char peekNext() { return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1); }
    private boolean match(char expected) { if (isAtEnd() || source.charAt(current) != expected) return false; current++; col++; return true; }
    private boolean isAtEnd() { return current >= source.length(); }
    private Token makeToken(TokenType type, String lexeme) { return new Token(type, lexeme, line, col - lexeme.length()); }
    
    private void printStatistics() {
        System.out.println("\n--- Scanner Statistics ---");
        System.out.println("Total Tokens: " + totalTokens);
        System.out.println("Lines Processed: " + line);
        System.out.println("Comments Removed: " + commentsRemoved);
        for (TokenType t : TokenType.values()) {
            if (t != TokenType.EOF && t != TokenType.ERROR) {
                System.out.println(String.format("%-15s : %d", t, tokenCounts[t.ordinal()]));
            }
        }
        System.out.println("--------------------------\n");
    }

    public static void main(String[] args) {
        try {
            String file = "tests/test3.lang"; 
            if (args.length > 0) file = args[0];
            
            ManualScanner scanner = new ManualScanner(file);
            scanner.scanTokens();
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}