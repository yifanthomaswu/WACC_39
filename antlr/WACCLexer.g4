lexer grammar WACCLexer;

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

PAIR: 'pair' ;

NULL: 'null' ;

// type
BASE_TYPE: 'int' | 'bool' | 'char' | 'string' ;

// unary operators
UNARY_OPER: '!' | 'len' | 'ord' | 'chr' ;

// binary operators
MULT: '*';
DIV: '/';
MOD: '%';
PLUS: '+';
MINUS: '-';
GRT: '>';
GRT_EQUAL: '>=';
LESS: '<';
LESS_EQUAL: '<=';
EQUAL: '==';
NOT_EQUAL: '!=';
AND: '&&';
OR: '||';


// brackets
OPEN_PARENTHESES : '(' ;
CLOSE_PARENTHESES : ')' ;

OPEN_SQUARE_BR: '[' ;
CLOSE_SQUARE_BR: ']' ;

// symbols
ASSIGN: '=' ;

SEMICOLON: ';' ;

COMMA: ',' ;

QUOTE: '\'' ;

DOUBLE_QUOTE: '"' ;

// bool
BOOL_LITER: 'true' | 'false' ;

// ident
IDENT: IDENT_CHAR (IDENT_CHAR | DIGIT)* ;
fragment IDENT_CHAR: [_a-zA-Z] ;

// digit
INT_LITER: (INT_SIGN)? (DIGIT)+ ;
fragment DIGIT: [0-9] ;
fragment INT_SIGN: [+-] ;

// character
CHAR_LITER: '\'' CHARACTER '\'' ;
STR_LITER: '"' (CHARACTER)* '"' ;
fragment CHARACTER: ~[\\'"] | ('\\' ESCAPED_CHAR) ;
fragment ESCAPED_CHAR: [0btnfr"'\\] ;

// comment and whitespace
COMMENT: '#' ~[\r\n]* '\r'? '\n' -> skip ;
WS: [ \t\r\n]+ -> skip ;
