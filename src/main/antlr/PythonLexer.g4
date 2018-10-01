lexer grammar PythonLexer;

tokens {
  INDENT, DEDENT, NEWLINE
}

DOT : '.';
ELLIPSIS : '...';
STAR : '*';
OPEN_PAREN : '(';
CLOSE_PAREN : ')';
COMMA : ',';
COLON : ':';
SEMI_COLON : ';';
POWER : '**';
ASSIGN : '=';
OPEN_BRACK : '[';
CLOSE_BRACK : ']';
OR_OP : '|';
XOR : '^';
AND_OP : '&';
LEFT_SHIFT : '<<';
RIGHT_SHIFT : '>>';
ADD : '+';
MINUS : '-';
DIV : '/';
MOD : '%';
IDIV : '//';
NOT_OP : '~';
OPEN_BRACE : '{';
CLOSE_BRACE : '}';
LESS_THAN : '<';
GREATER_THAN : '>';
EQUALS : '==';
GT_EQ : '>=';
LT_EQ : '<=';
NOT_EQ_1 : '<>';
NOT_EQ_2 : '!=';
AT : '@';
ARROW : '->';
ADD_ASSIGN : '+=';
SUB_ASSIGN : '-=';
MULT_ASSIGN : '*=';
AT_ASSIGN : '@=';
DIV_ASSIGN : '/=';
MOD_ASSIGN : '%=';
AND_ASSIGN : '&=';
OR_ASSIGN : '|=';
XOR_ASSIGN : '^=';
LEFT_SHIFT_ASSIGN : '<<=';
RIGHT_SHIFT_ASSIGN : '>>=';
POWER_ASSIGN : '**=';
IDIV_ASSIGN : '//=';

DEF : 'def';
RETURN : 'return';
RAISE : 'raise';
FROM : 'from';
IMPORT : 'import';
AS : 'as';
GLOBAL : 'global';
NONLOCAL : 'nonlocal';
ASSERT : 'assert';
IF : 'if';
ELIF : 'elif';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';
IN : 'in';
TRY : 'try';
FINALLY : 'finally';
WITH : 'with';
EXCEPT : 'except';
LAMBDA : 'lambda';
OR : 'or';
AND : 'and';
NOT : 'not';
IS : 'is';
NONE : 'None';
TRUE : 'True';
FALSE : 'False';
CLASS : 'class';
YIELD : 'yield';
DEL : 'del';
PASS : 'pass';
CONTINUE : 'continue';
BREAK : 'break';
ASYNC : 'async';
AWAIT : 'await';

STRING_LITERAL
 : ( [rR] | [uU] | [fF] | ( [fF] [rR] ) | ( [rR] [fF] ) )? ( SHORT_STRING | LONG_STRING )
 ;

BYTES_LITERAL
 : ( [bB] | ( [bB] [rR] ) | ( [rR] [bB] ) ) ( SHORT_BYTES | LONG_BYTES )
 ;

/// decimalinteger ::=  nonzerodigit digit* | "0"+
DECIMAL_INTEGER
 : NON_ZERO_DIGIT DIGIT*
 | '0'+
 ;

/// octinteger     ::=  "0" ("o" | "O") octdigit+
OCT_INTEGER
 : '0' [oO] OCT_DIGIT+
 ;

/// hexinteger     ::=  "0" ("x" | "X") hexdigit+
HEX_INTEGER
 : '0' [xX] HEX_DIGIT+
 ;

/// bininteger     ::=  "0" ("b" | "B") bindigit+
BIN_INTEGER
 : '0' [bB] BIN_DIGIT+
 ;

/// floatnumber   ::=  pointfloat | exponentfloat
FLOAT_NUMBER
 : POINT_FLOAT
 | EXPONENT_FLOAT
 ;

/// imagnumber ::=  (floatnumber | intpart) ("j" | "J")
IMAG_NUMBER
 : ( FLOAT_NUMBER | INT_PART ) [jJ]
 ;

NAME
 : ID_START ID_CONTINUE*
 ;

PHYSICAL_NEWLINE
 : ('\r'? '\n' | '\r' | '\f')
 ;

fragment SHORT_BYTES
 : '\'' ( SHORT_BYTES_CHAR_NO_SINGLE_QUOTE | BYTES_ESCAPE_SEQ )* '\''
 | '"' ( SHORT_BYTES_CHAR_NO_DOUBLE_QUOTE | BYTES_ESCAPE_SEQ )* '"'
 ;

fragment LONG_BYTES
 : '\'\'\'' LONG_BYTES_ITEM*? '\'\'\''
 | '"""' LONG_BYTES_ITEM*? '"""'
 ;

fragment LONG_BYTES_ITEM
 : LONG_BYTES_CHAR
 | BYTES_ESCAPE_SEQ
 ;

fragment SHORT_BYTES_CHAR_NO_SINGLE_QUOTE
 : [\u0000-\u0009]
 | [\u000B-\u000C]
 | [\u000E-\u0026]
 | [\u0028-\u005B]
 | [\u005D-\u007F]
 ;

fragment SHORT_BYTES_CHAR_NO_DOUBLE_QUOTE
 : [\u0000-\u0009]
 | [\u000B-\u000C]
 | [\u000E-\u0021]
 | [\u0023-\u005B]
 | [\u005D-\u007F]
 ;

fragment LONG_BYTES_CHAR
 : [\u0000-\u005B]
 | [\u005D-\u007F]
 ;

fragment BYTES_ESCAPE_SEQ
 : '\\' [\u0000-\u007F]
 ;

SKIP_
 : ( SPACES | COMMENT ) -> skip
 ;

LINE_JOINING
 : '\\' SPACES? PHYSICAL_NEWLINE?
 ;

fragment SHORT_STRING
 : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
 | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
 ;

/// longstring      ::=  "'''" longstringitem* "'''" | '"""' longstringitem* '"""'
fragment LONG_STRING
 : '\'\'\'' LONG_STRING_ITEM*? '\'\'\''
 | '"""' LONG_STRING_ITEM*? '"""'
 ;

fragment LONG_STRING_ITEM
 : LONG_STRING_CHAR
 | STRING_ESCAPE_SEQ
 ;

fragment LONG_STRING_CHAR
 : ~'\\'
 ;

fragment STRING_ESCAPE_SEQ
 : '\\' .
 ;

/// nonzerodigit   ::=  "1"..."9"
fragment NON_ZERO_DIGIT
 : [1-9]
 ;

fragment DIGIT
 : [0-9]
 ;

/// octdigit       ::=  "0"..."7"
fragment OCT_DIGIT
 : [0-7]
 ;

/// hexdigit       ::=  digit | "a"..."f" | "A"..."F"
fragment HEX_DIGIT
 : [0-9a-fA-F]
 ;

/// bindigit       ::=  "0" | "1"
fragment BIN_DIGIT
 : [01]
 ;

/// pointfloat    ::=  [intpart] fraction | intpart "."
fragment POINT_FLOAT
 : INT_PART? FRACTION
 | INT_PART '.'
 ;

/// exponentfloat ::=  (intpart | pointfloat) exponent
fragment EXPONENT_FLOAT
 : ( INT_PART | POINT_FLOAT ) EXPONENT
 ;

/// intpart       ::=  digit+
fragment INT_PART
 : DIGIT+
 ;

/// fraction      ::=  "." digit+
fragment FRACTION
 : '.' DIGIT+
 ;

/// exponent      ::=  ("e" | "E") ["+" | "-"] digit+
fragment EXPONENT
 : [eE] [+-]? DIGIT+
 ;

fragment SPACES
 : [ \t]+
 ;

fragment COMMENT
 : '#' ~[\r\n\f]*
 ;

fragment ID_START
 : [a-z]
 | [A-Z]
 | '_'
 ;

fragment ID_CONTINUE
 : ID_START
 | [0-9]
 ;
