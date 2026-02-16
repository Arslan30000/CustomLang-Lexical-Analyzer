/* 1. User Code Section */
import java.io.IOException;

%%

/* 2. Options and Declarations */
%class Yylex
%public
%unicode
%line
%column
%type Token

%{
  // Helper to create tokens with line/col info
  private Token token(TokenType type) {
      return new Token(type, yytext(), yyline + 1, yycolumn + 1);
  }
  
  // Helper for when we need to force specific text (optional)
  private Token token(TokenType type, String text) {
      return new Token(type, text, yyline + 1, yycolumn + 1);
  }
%}

/* 3. Macros (Regex Definitions) */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Digits and Letters */
Digit          = [0-9]
Identifier     = [A-Z][a-z0-9]{0,30}

/* Integer: Optional sign, then digits */
IntegerLiteral = [+-]? {Digit}+

/* Float: Optional sign, digits, dot, 1-6 digits, optional exponent */
FloatLiteral   = [+-]? {Digit}+ \. {Digit}{1,6} ([eE] [+-]? {Digit}+)?

/* String: " anything " */
StringLiteral  = \" [^\"\n]* \"

/* Char: 'x' or escape sequence */
CharLiteral    = \' ( [^'\\\n] | \\. ) \'

/* Comments */
SingleLineComment = "##" [^\n]*
MultiLineComment  = "#|" ~"|#"

%%

/* 4. Lexical Rules */

<YYINITIAL> {

  /* Keywords */
  "start"       { return token(TokenType.KEYWORD); }
  "finish"      { return token(TokenType.KEYWORD); }
  "loop"        { return token(TokenType.KEYWORD); }
  "condition"   { return token(TokenType.KEYWORD); }
  "declare"     { return token(TokenType.KEYWORD); }
  "output"      { return token(TokenType.KEYWORD); }
  "input"       { return token(TokenType.KEYWORD); }
  "function"    { return token(TokenType.KEYWORD); }
  "return"      { return token(TokenType.KEYWORD); }
  "break"       { return token(TokenType.KEYWORD); }
  "continue"    { return token(TokenType.KEYWORD); }
  "else"        { return token(TokenType.KEYWORD); }
  "if"          { return token(TokenType.KEYWORD); }

  /* Boolean Literals */
  "true" | "false" { return token(TokenType.BOOLEAN_LITERAL); }

  /* Multi-Character Operators */
  "++"          { return token(TokenType.INC_DEC_OP); }
  "--"          { return token(TokenType.INC_DEC_OP); }
  "**"          { return token(TokenType.ARITHMETIC_OP); }
  "=="          { return token(TokenType.RELATIONAL_OP); }
  "!="          { return token(TokenType.RELATIONAL_OP); }
  "<="          { return token(TokenType.RELATIONAL_OP); }
  ">="          { return token(TokenType.RELATIONAL_OP); }
  "&&"          { return token(TokenType.LOGICAL_OP); }
  "||"          { return token(TokenType.LOGICAL_OP); }
  "+="          { return token(TokenType.ASSIGNMENT_OP); }
  "-="          { return token(TokenType.ASSIGNMENT_OP); }
  "*="          { return token(TokenType.ASSIGNMENT_OP); }
  "/="          { return token(TokenType.ASSIGNMENT_OP); }

  /* Single Character Operators */
  "+" | "-" | "*" | "/" | "%"  { return token(TokenType.ARITHMETIC_OP); }
  "<" | ">"                    { return token(TokenType.RELATIONAL_OP); }
  "!"                          { return token(TokenType.LOGICAL_OP); }
  "="                          { return token(TokenType.ASSIGNMENT_OP); }
  
  /* Punctuators */
  "(" | ")" | "{" | "}" | "[" | "]" | "," | ";" | ":" { return token(TokenType.PUNCTUATOR); }

  /* Literals & Identifiers (Macros) */
  {Identifier}      { return token(TokenType.IDENTIFIER); }
  {FloatLiteral}    { return token(TokenType.FLOAT_LITERAL); }
  {IntegerLiteral}  { return token(TokenType.INTEGER_LITERAL); }
  {StringLiteral}   { return token(TokenType.STRING_LITERAL); }
  {CharLiteral}     { return token(TokenType.CHAR_LITERAL); }

  /* Ignore Whitespace and Comments */
  {WhiteSpace}          { /* ignore */ }
  {SingleLineComment}   { /* ignore */ }
  {MultiLineComment}    { /* ignore */ }

}

/* Error Fallback: Matches any single char not matched above */
[^]  { 
    System.err.println("Error: Illegal character <" + yytext() + "> at line " + (yyline+1)); 
    return token(TokenType.ERROR);
}