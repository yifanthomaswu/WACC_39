lexer grammar BasicLexer;

//binary operators
MULTIPLY: '*' ;
DIVIDE: '/' ;
MODULUS: '%' ;
PLUS: '+' ;
MINUS: '-' ;
GREATER_THAN: '>' ;
GREATER_THAN_EQ: '>=' ;
SMALLER_THAN: '<' ;
SMALLER_THAN_EQ: '<=' ;
EQUAL: '==' ;
NOT_EQUAL: '!=' ;
AND: '&&' ;
OR: '||' ;

//unary operators
NOT: '!' ;
NEGATIVE: '-' ;
ARRAY_LENGTH: 'len' ;
ORD: 'ord' ;
CHR: 'chr' ;

//brackets
OPEN_PARENTHESES : '(' ;
CLOSE_PARENTHESES : ')' ;

//numbers
DIGIT : '0'..'9' ;

//function call
CALL: 'call' ;

//statements
SKIP: 'skip' ;
ASSIGNMENT: '=' ;
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
BEGIN: 'begin' ;
END: 'end' ;
STATEMENT_SEPARATOR: ';' ;

//base type
INT_TYPE: 'int' ;
BOOL_TYPE: 'bool' ;
CHAR_TYPE: 'char' ;
STRING_TYPE: 'string' ;

//array type
OPEN_SQUARE_BR: '[' ;
CLOSE_SQUARE_BR: ']' ;

//array literal
ARRAY_SEPARATOR: ',' ;

//pair type
PAIR_SEPARATOR: ',' ;
PAIR: 'pair' ;
NEW_PAIR: 'newpair' ;

//pair elem
FIRST_ELEM: 'fst' ;
SECOND_ELEM: 'snd' ;

//bool literal
TRUE: 'true' ;
FALSE: 'false' ;

//char literal
QUOTE: '\'' ;

//string literal
DOUBLE_QUOTE: '"' ;

//int sign
POSITIVE_SIGN: '+' ;
NEGATIVE_SIGN: '-' ;

//character
CHAR: (~('\\' | '\'' | '"')  | '\\' ESCAPED_CHAR) ;

//escaped char
ESCAPED_CHAR: ('0' | 'b' | 't' | 'n' | 'f' | 'r' | '"' | '\'' | '\\') ;

//ident begin
BEGIN_IDENT: ('_' | ['a'-'z'] | ['A'-'Z']);
REST_IDENT: ('_' | ['a'-'z'] | ['A'-'Z'] | ['0'-'9']) ;

//pair literal
NULL: 'null' ;

//comment
COMMENT: '#' ~[\r\n]* -> skip;

//arg list
ARG_SEPARATOR: ',' ;

//param list
PARAM_SEPARATOR: ',' ;

//func
FUNC_BEGIN: 'is' ;
FUNC_END: 'end' ;

//program
PROG_BEGIN: 'begin' ;
PROG_END: 'end' ;
