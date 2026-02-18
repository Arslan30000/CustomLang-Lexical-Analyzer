# Compiler Construction - Assignment 01: Lexical Analyzer

Project: Custom Language Scanner Implementation
Course: Compiler Construction
Team Members:
- Muhammad Arslan - 23i-0572
- Masab Tahir - 23i-0006

--------------------------------------------------------------------------------

## 1. Language Specifications

Language Name: CustomLang
File Extension: .lang

This project implements a lexical analyzer (scanner) capable of tokenizing source
code written in CustomLang. As per assignment instructions and TA clarification, 
the scanner focuses strictly on 7 specific token types using both Manual DFA 
and JFlex implementations.

### 1.1 Token Types Implemented (The 7 Selected Tokens)

1. Identifiers
   - Rule: Must start with an Uppercase Letter (A-Z).
   - Allowed Characters: Lowercase letters (a-z) and Digits (0-9).
   - Length Limit: Maximum 31 characters.
   - Examples: Valid: Count, Val1 | Invalid: count, 1stVar

2. Integer Literals
   - Format: Sequence of digits, optional sign.
   - Examples: 10, -5, +100

3. Floating Point Literals
   - Format: Decimal point required. Supports Scientific Notation.
   - Examples: 3.14, -0.01, 1.5e-10

4. Boolean Literals
   - Format: Exact match keywords.
   - Examples: true, false

5. Arithmetic Operators
   - Supported: +, -, *, /, %

6. Punctuators
   - Supported: ( ) { } [ ] , ; :

7. Single-Line Comments
   - Format: Starts with ##. Everything after is ignored until the newline.

--------------------------------------------------------------------------------

## 2. Compilation & Execution

### Prerequisites
- Java Development Kit (JDK) installed.
- JFlex library (jflex-full-1.9.1.jar).

### Step 1: Generating and Compiling
Navigate to the root project folder and run:
`java -jar jflex-full-1.9.1.jar src/Scanner.flex`
`javac src/*.java`

### Step 2: Running the Manual Scanner
`java src.ManualScanner`

### Step 3: Running the JFlex Scanner
`java src.JFlexTest`

--------------------------------------------------------------------------------

## 3. Error Handling

The scanner includes a robust ErrorHandler that reports:
1. Lexical Errors: Invalid characters (e.g., @, $).
2. Malformed Literals: Floats with multiple dots (1.2.3).
3. Identifier Constraints: Identifiers starting with lowercase or exceeding 31 chars.

Example Error Report:
=== ERROR REPORT ===
ERROR: [Invalid ID     ] Line: 2   Col: 11  Lexeme: "invalid" -> Identifiers must start with Uppercase
ERROR: [Invalid Char   ] Line: 6   Col: 18  Lexeme: "@"       -> Character not in alphabet
====================