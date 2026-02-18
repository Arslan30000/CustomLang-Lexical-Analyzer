/* 1. User Code Section */
package src;
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
%}

/* 3. Macros (Regex Definitions) */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Digit          = [0-9]

/* Identifier: Uppercase followed by lowercase/digits */
Identifier     = [A-Z][a-z0-9]{0,30}

/* Numbers: Integers and Floats with optional signs and exponents */
IntegerLiteral = [+-]? {Digit}+
FloatLiteral   = [+-]? {Digit}+ \. {Digit}{1,6} ([eE] [+-]? {Digit}+)?

/* Comments */
SingleLineComment = "##" [^\n]*

%%

/* 4. Lexical Rules */

<YYINITIAL> {
  /* Boolean Literals */
  "true" | "false" { return token(TokenType.BOOLEAN_LITERAL); }

  /* Arithmetic Operators */
  "+" | "-" | "*" | "/" | "%"  { return token(TokenType.ARITHMETIC_OP); }
  
  /* Punctuators */
  "(" | ")" | "{" | "}" | "[" | "]" | "," | ";" | ":" { return token(TokenType.PUNCTUATOR); }

  /* Literals & Identifiers (Macros) */
  {Identifier}      { return token(TokenType.IDENTIFIER); }
  {FloatLiteral}    { return token(TokenType.FLOAT_LITERAL); }
  {IntegerLiteral}  { return token(TokenType.INTEGER_LITERAL); }

  /* Ignore Whitespace and Comments */
  {WhiteSpace}          { /* ignore */ }
  {SingleLineComment}   { /* ignore */ }
}

/* Error Fallback: Matches any single char not matched above */
[^]  { 
    System.err.println("Error: Illegal character <" + yytext() + "> at line " + (yyline+1));
    return token(TokenType.ERROR);
}