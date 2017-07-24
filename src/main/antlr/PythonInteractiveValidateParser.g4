parser grammar PythonInteractiveValidateParser;

import PythonParser;

options {
  tokenVocab = PythonLexer;
}

// single_input: NEWLINE | simple_stmt | compound_stmt NEWLINE
single_input
 : NEWLINE
 | simple_stmt
 | compound_stmt NEWLINE? EOF
 ;

// suite: simple_stmt | NEWLINE INDENT stmt+ DEDENT
suite
 : simple_stmt
 | NEWLINE (EOF | DEDENT+ EOF | INDENT stmt+ (DEDENT | EOF))
 ;

// simple_stmt: small_stmt (';' small_stmt)* [';'] NEWLINE
simple_stmt
 : small_stmt (SEMI_COLON small_stmt)* SEMI_COLON? (NEWLINE | EOF)
 ;
