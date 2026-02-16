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
code written in CustomLang. It supports variable declarations, control structures,
complex literals, and error handling.

### 1.1 Keywords
The following words are reserved and cannot be used as identifiers:
- start / finish     : Defines the beginning and end of the program block.
- declare            : Used to declare a new variable.
- input / output     : Handles standard input and output operations.
- if / else          : Conditional control structures.
- loop               : Initiates a loop structure.
- break / continue   : Loop control statements.
- function / return  : Defines functions and return values.
- condition          : Reserved for defining condition blocks.

### 1.2 Identifiers
Identifiers are used for variable and function names.
- Rule: Must start with an Uppercase Letter (A-Z).
- Allowed Characters: Letters (a-z, A-Z), Digits (0-9), and Underscores (_).
- Length Limit: Maximum 31 characters.
- Examples:
    - Valid: Count, Total_Sum, Var1
    - Invalid: count (lowercase start), 1stVar (digit start), _temp (underscore start).

### 1.3 Literals
The scanner supports five types of literals:
- Integer: Sequence of digits, optional sign.
  Example: 10, -5, +100
- Float: Decimal point required. Supports Scientific Notation.
  Example: 3.14, -0.01, 1.5e-10
- Boolean: Reserved keywords.
  Example: true, false
- String: Enclosed in double quotes (").
  Example: "Hello World", ""
- Char: Enclosed in single quotes ('). Supports escapes.
  Example: 'A', '\n', '\t'

### 1.4 Operators & Precedence
Operators are listed in descending order of precedence:
1. Unary / Update: ++, --, !
2. Power: **
3. Multiplicative: *, /, %
4. Additive: +, -
5. Relational: <, >, <=, >=, ==, !=
6. Logical: &&, ||
7. Assignment: =, +=, -=, *=, /=

### 1.5 Comments
- Single-Line: Starts with ##. Everything after is ignored until the newline.
- Multi-Line: Starts with #| and ends with |#. Can span multiple lines.

--------------------------------------------------------------------------------

## 2. Sample Programs

### Sample 1: Basic Arithmetic
start
  declare Count = 10;
  declare Ratio = 3.14;
  
  ## Simple calculation
  declare Result = Count * 2;
  output Result;
finish

### Sample 2: Control Structures
start
  declare Index = 0;
  
  loop (Index < 5) {
      if (Index == 3) {
          continue;
      }
      output "Current Index: ";
      output Index;
      
      Index = Index + 1;
  }
finish

### Sample 3: Complex Literals
start
  #| 
     Testing scientific notation
     and string handling
  |#
  
  declare SciVal = 1.0e-5;
  declare Message = "Status: OK";
  declare CharCheck = '\n';
  
  if (SciVal > 0.0) {
      output Message;
  }
finish

--------------------------------------------------------------------------------

## 3. Compilation & Execution

### Prerequisites
- Java Development Kit (JDK) installed.
- Terminal or Command Prompt access.

### Step 1: Compilation
Navigate to the root project folder (where src and tests are located) and run:
javac src/*.java

### Step 2: Running the Manual Scanner
To scan a specific file using the hand-coded scanner:
java -cp src ManualScanner tests/test1.lang

(Note: If no argument is provided, the code defaults to tests/test4.lang).

### Step 3: Running the JFlex Scanner
To scan using the JFlex-generated scanner:
java -cp src JFlexTest tests/test2.lang

--------------------------------------------------------------------------------

## 4. Error Handling

The scanner includes a robust ErrorHandler that reports:
1. Lexical Errors: Invalid characters (e.g., @, $).
2. Malformed Literals: Floats with multiple dots (1.2.3).
3. Identifier Constraints: Identifiers starting with lowercase or exceeding 31 chars.
4. Unclosed Structures: Unterminated strings or multi-line comments.

Example Error Report:
=== ERROR REPORT ===
ERROR: [Invalid ID     ] Line: 2   Col: 11  Lexeme: "validVar  " -> Identifiers must start with Uppercase
ERROR: [Invalid Char   ] Line: 6   Col: 18  Lexeme: "$         " -> Character not in alphabet
====================