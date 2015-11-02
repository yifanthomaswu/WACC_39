lexer grammar BasicLexer;

// keywords
BEGIN: 'begin' ;
END: 'end' ;

IS: 'is' ;

SKIP: 'skip' ;
READ: 'read' ;
FREE: 'free' ;
RETURN: 'return' ;
EXIT: 'exit' ;
PRINT: 'print' ;
PRINTLN: 'println' ;
IF: 'if' ;
THEN: 'then' ;
ELSE: 'else' ;
FI: 'fi' ;
WHILE: 'while' ;
DO: 'do' ;
DONE: 'done' ;

NEW_PAIR: 'newpair' ;
CALL: 'call' ;

FST: 'fst' ;
SND: 'snd' ;

INT: 'int' ;
BOOL: 'bool' ;
CHAR: 'char' ;
STRING: 'string' ;

PAIR: 'pair' ;

LEN: 'len' ;
ORD: 'ord' ;
CHR: 'chr' ;

TRUE: 'true' ;
FALSE: 'false' ;

// brackets
OPEN_PARENTHESES : '(' ;
CLOSE_PARENTHESES : ')' ;

OPEN_SQUARE_BR: '[' ;
CLOSE_SQUARE_BR: ']' ;

// operators
MULT: '*' ;
DIV: '/' ;
MOD: '%' ;
PLUS: '+' ;
MINUS: '-' ;
GT: '>' ;
GTEQ: '>=' ;
LT: '<' ;
LTEQ: '<=' ;
EQ: '==' ;
NE: '!=' ;
AND: '&&' ;
OR: '||' ;

NOT: '!' ;

// symbols
ASSIGN: '=' ;

SEMICOLON: ';' ;

COMMA: ',' ;

QUOTE: '\'' ;

DOUBLE_QUOTE: '"' ;

// ident
IDENT: IDENT_CHAR (IDENT_CHAR | DIGIT)* ;
fragment IDENT_CHAR: [_a-zA-Z] ;

// digit
INTEGER: DIGIT+ ;
fragment DIGIT : [0-9] ;

// character
CHAR_LITER: '\'' CHARACTER '\'' ;
STR_LITER: '"' (CHARACTER)* '"' ;
fragment CHARACTER: (~('\\' | '\'' | '"') | '\\' ESCAPED_CHAR) ;
fragment ESCAPED_CHAR: ('0' | 'b' | 't' | 'n' | 'f' | 'r' | '"' | '\'' | '\\') ;

// pair
PAIR_LITER: 'null' ;

// comment and whitespace
COMMENT: '#' ~[\r\n]* '\r'? '\n' -> skip ;
WS: [ \t\r\n]+ -> skip ;
