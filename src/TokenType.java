package src;
public enum TokenType {
    IDENTIFIER,         // e.g., Count, Val1
    INTEGER_LITERAL,    // e.g., 10, -5
    FLOAT_LITERAL,      // e.g., 3.14, 1.5e-10
    BOOLEAN_LITERAL,    // true or false
    ARITHMETIC_OP,      // +, -, *, /, %
    PUNCTUATOR,         // (, ), {, }, [, ], ,, ;, :
    EOF,                // End of file marker
    ERROR               // Used when lexical errors occur
}