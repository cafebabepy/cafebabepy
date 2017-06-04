grammar Python;

tokens {
    INDENT, DEDENT
}

@lexer::members {

  // A queue where extra tokens are pushed on (see the NEWLINE lexer rule).
  private java.util.LinkedList<Token> tokens = new java.util.LinkedList<>();

  // The stack that keeps track of the indentation level.
  private java.util.LinkedList<Integer> indents = new java.util.LinkedList<>();

  // The amount of opened braces, brackets and parenthesis.
  private int opened = 0;

  // The most recently produced token.
  private Token lastToken = null;

  @Override
  public void emit(Token t) {
    super.setToken(t);
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {
    if (_input.LA(1) == EOF && !this.indents.isEmpty()) {

      // Remove any trailing EOF tokens from our buffer.
      for (int i = tokens.size() - 1; i >= 0; i--) {
        if (tokens.get(i).getType() == EOF) {
          tokens.remove(i);
        }
      }

      // First emit an extra line break that serves as the end of the statement.
      this.emit(getCommonToken(PythonParser.NEWLINE, "\n"));

      // Now emit as much DEDENT tokens as needed.
      while (!indents.isEmpty()) {
        this.emit(createDedent());
        // pop
        indents.removeFirst();
      }

      // Put the EOF back on the token stream.
      this.emit(getCommonToken(PythonParser.EOF, "<EOF>"));
    }

    Token next = super.nextToken();

    if (next.getChannel() == Token.DEFAULT_CHANNEL) {
      // Keep track of the last token on the default channel.
      this.lastToken = next;
    }

    return tokens.isEmpty() ? next : tokens.poll();
  }

  private Token createDedent() {
    CommonToken dedent = getCommonToken(PythonParser.DEDENT, "");
    dedent.setLine(this.lastToken.getLine());
    return dedent;
  }

  private CommonToken getCommonToken(int type, String text) {
    int stop = this.getCharIndex() - 1;
    int start = text.isEmpty() ? stop : stop - text.length() + 1;
    return new CommonToken(this._tokenFactorySourcePair, type, DEFAULT_TOKEN_CHANNEL, start, stop);
  }

  static int getIndentCount(String spaces) {
    int count = 0;

    for (char ch : spaces.toCharArray()) {
      switch (ch) {
        case '\t':
          count += 8 - (count % 8);
          break;
        default:
          count++;
      }
    }

    return count;
  }

  boolean atStartOfInput() {
    return super.getCharPositionInLine() == 0 && super.getLine() == 1;
  }
}

file_input
 : ( NEWLINE | stmt )* EOF
 ;

decorator
 : '@' dotted_name ('(' arglist? ')')? NEWLINE
 ;

decorators
 : decorator+
 ;

decorated
 : decorators (classdef | funcdef | async_funcdef)
 ;

async_funcdef
 : 'async' funcdef
 ;

funcdef
 : 'def' NAME parameters ('->' test)? ':' suite
 ;

parameters
 : '(' typedargslist? ')'
 ;

typedargslist
 : tfpdef ('=' test)? (',' tfpdef ('=' test)?)* (',' ('*' tfpdef? (',' tfpdef ('=' test)?)* (',' ('**' tfpdef ','?)?)? | '**' tfpdef ','?)?)?
  | '*' tfpdef? (',' tfpdef ('=' test)?)* (',' ('**' tfpdef ','?)?)?
  | '**' tfpdef ','?
  ;

tfpdef
 : NAME (':' test)?
 ;

varargslist
 : vfpdef ('=' test)? (',' vfpdef ('=' test)?)* (',' ('*' vfpdef? (',' vfpdef ('=' test)?)* (',' ('**' vfpdef ','?)?)? | '**' vfpdef ','?)?)?
 | '*' vfpdef? (',' vfpdef ('=' test)?)* (',' ('**' vfpdef ','?)?)?
 | '**' vfpdef ','?
 ;

vfpdef
 : NAME
 ;

stmt
 : simple_stmt
 | compound_stmt
 ;

simple_stmt
 : small_stmt (';' small_stmt)* ';'? NEWLINE
 ;

small_stmt
 : expr_stmt
 | del_stmt
 | pass_stmt
 | flow_stmt
 | import_stmt
 | global_stmt
 | nonlocal_stmt
 | assert_stmt
 ;

expr_stmt
 : testlist_star_expr (annassign | augassign (yield_expr | testlist)
 | '=' ( yield_expr | testlist_star_expr))*
 ;

annassign
  : ':' test ('=' test)?
  ;

testlist_star_expr
 : ( test | star_expr) (',' (test | star_expr))* ','?
 ;

augassign
 : '+='
 | '-='
 | '*='
 | '@='
 | '/='
 | '%='
 | '&='
 | '|='
 | '^='
 | '<<='
 | '>>='
 | '**='
 | '//='
 ;

del_stmt
 : 'del' exprlist
 ;

pass_stmt
 : 'pass'
 ;

flow_stmt
 : break_stmt
 | continue_stmt
 | return_stmt
 | raise_stmt
 | yield_stmt
 ;

break_stmt
 : 'break'
 ;

continue_stmt
 : 'continue'
 ;

return_stmt
 : 'return' testlist?
 ;

yield_stmt
 : yield_expr
 ;

raise_stmt
 : 'raise' (test ('from' test)?)?
 ;

import_stmt
 : import_name
 | import_from
 ;

import_name
 : 'import' dotted_as_names
 ;

import_from
 : 'from' (('.' | '...')* dotted_name | ('.' | '...')+)
   'import' ('*' | '(' import_as_names ')' | import_as_names)
 ;

import_as_name
 : NAME ('as' NAME)?
 ;

dotted_as_name
 : dotted_name ('as' NAME)?
 ;

import_as_names
 : import_as_name (',' import_as_name)* ','?
 ;

dotted_as_names
 : dotted_as_name (',' dotted_as_name)*
 ;

dotted_name
 : NAME ('.' NAME)*
 ;

global_stmt
 : 'global' NAME (',' NAME)*
 ;

nonlocal_stmt
 : 'nonlocal' NAME (',' NAME)*
 ;

assert_stmt
 : 'assert' test (',' test)?
 ;

compound_stmt
 : if_stmt
 | while_stmt
 | for_stmt
 | try_stmt
 | with_stmt
 | funcdef
 | classdef
 | decorated
 | async_stmt;

async_stmt
 : 'async' (funcdef | with_stmt | for_stmt)
 ;

if_stmt
 : 'if' test ':' suite ('elif' test ':' suite)* ('else' ':' suite)?
 ;

while_stmt
 : 'while' test ':' suite ('else' ':' suite)?
 ;

for_stmt
 : 'for' exprlist 'in' testlist ':' suite ('else' ':' suite)
 ;

try_stmt: ('try' ':' suite ((except_clause ':' suite)+
          ('else' ':' suite)?
          ('finally' ':' suite)?
          |'finally' ':' suite))
;

with_stmt
 : 'with' with_item (',' with_item)* ':' suite
 ;

with_item
 : test ('as' expr)?
 ;

except_clause
 : 'except' (test ('as' NAME)?)?
 ;

suite
 : simple_stmt | NEWLINE INDENT stmt+ DEDENT
 ;

test
 : or_test ('if' or_test 'else' test)?
 | lambdef
 ;

test_nocond
 : or_test
 | lambdef_nocond
 ;

lambdef
 : 'lambda' varargslist? ':' test
 ;

lambdef_nocond
 : 'lambda' varargslist? ':' test_nocond
 ;

or_test
 : and_test ('or' and_test)*
 ;

and_test
 : not_test ('and' not_test)*
 ;

not_test
 : 'not' not_test
 | comparison
 ;

comparison
 : expr (comp_op expr)*
 ;

comp_op
 : '<'
 | '>'
 | '=='
 | '>='
 | '<='
 | '<>'
 | '!='
 | 'in'
 | 'not' 'in'
 | 'is'
 | 'is' 'not'
 ;

star_expr
 : '*' expr
 ;

expr
 : xor_expr ('|' xor_expr)*
 ;

xor_expr
 : and_expr ('^' and_expr)*
 ;

and_expr
 : shift_expr ('&' shift_expr)*
 ;

shift_expr
 : arith_expr (('<<' | '>>') arith_expr)*
 ;

arith_expr
 : term (('+' | '-') term)*
 ;

term
 : factor (('*' | '@' | '/' | '%' | '//') factor)*
 ;

factor
 : ('+' | '-' | '~') factor | power
 ;

power
 : atom_expr ('**' factor)?
 ;

atom_expr
 : 'await'? atom trailer*
 ;

atom
 : '(' (yield_expr | testlist_comp)? ')'
 | '[' testlist_comp ']'
 | '{' dictorsetmaker '}'
 | NAME
 | number
 | str+
 | '...'
 | 'None'
 | 'True'
 | 'False'
 ;

testlist_comp
 : (test | star_expr) (comp_for | (',' (test | star_expr))* ','?)
 ;

trailer
 : '(' arglist? ')'
 | '[' subscriptlist ']'
 | '.' NAME
 ;

subscriptlist
 : subscript (',' subscript)* ','?
 ;

subscript
 : test
 | test? ':' test? sliceop?
 ;

sliceop
 : ':' test?
 ;

exprlist
 : (expr | star_expr) (',' (expr | star_expr))* ','?
 ;

testlist
 : test (',' test)* ','?
 ;

dictorsetmaker
 : ((test ':' test | '**' expr)
    (comp_for | (',' (test ':' test | '**' expr))* ','?)) |
   ((test | star_expr)
    (comp_for | (',' (test | star_expr))* ','?))
 ;

classdef
 : 'class' NAME ('(' arglist? ')')? ':' suite
 ;

arglist
 : argument (',' argument)*  ','?
 ;

argument
 : test comp_for?
 | test '=' test
 | '**' test
 | '*' test
 ;

comp_iter
 : comp_for
 | comp_if
 ;

comp_for
 : 'async'? 'for' exprlist 'in' or_test comp_iter?
 ;

comp_if
 : 'if' test_nocond comp_iter?
 ;

yield_expr
 : 'yield' yield_arg?
 ;

yield_arg
 : 'from' test
 | testlist
 ;

str
 : STRING_LITERAL
// | BYTES_LITERAL
 ;

number
 : integer
 | FLOAT_NUMBER
 | IMAG_NUMBER
 ;

integer
 : DECIMAL_INTEGER
 | OCT_INTEGER
 | HEX_INTEGER
 | BIN_INTEGER
 ;

STRING_LITERAL
 : [uU]? [rR]? ( SHORT_STRING | LONG_STRING )
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

NEWLINE
 : ( {atStartOfInput()}?   SPACES
   | ( '\r'? '\n' | '\r' | '\f' ) SPACES?
   )
   {
       String newLine = getText().replaceAll("[^\r\n\f]+", "");
       String spaces = getText().replaceAll("[\r\n\f]+", "");
       int next = _input.LA(1);

       if (opened > 0 || next == '\r' || next == '\n' || next == '\f' || next == '#') {
           // If we're inside a list or on a blank line, ignore all indents,
           // dedents and line breaks.
           skip();

       } else {
           emit(getCommonToken(NEWLINE, newLine));

           int indent = getIndentCount(spaces);
           int previous = indents.isEmpty() ? 0 : indents.peekFirst();

           if (indent == previous) {
                skip();

           } else if (indent > previous) {
                // push
                indents.addFirst(indent);
                emit(getCommonToken(PythonParser.INDENT, spaces));

           } else {
               // Possibly emit more than 1 DEDENT token.
               while(!indents.isEmpty() && indents.peekFirst() > indent) {
                   this.emit(createDedent());
                   // pop
                   indents.removeFirst();
               }
           }
       }
   }
 ;

SKIP_
 : ( SPACES | COMMENT | LINE_JOINING ) -> skip
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

fragment LINE_JOINING
 : '\\' SPACES? ( '\r'? '\n' | '\r' | '\f' )
 ;

fragment ID_START
 : [a-z]
 | [A-Z]
 ;

fragment ID_CONTINUE
 : ID_START
 | [0-9]
 ;


