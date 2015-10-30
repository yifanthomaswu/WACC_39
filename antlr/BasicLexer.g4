lexer grammar BasicLexer;

//binary operators
PLUS: '+' ;
MINUS: '-' ;
MULTIPLY: '*' ;
DIVIDE: '/' ;
MODULUS: '%' ;
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
fragment DIGIT : '0'..'9' ; 

INTEGER: DIGIT+ ;

//base type
INT_TYPE: 'int' ;
BOOL_TYPE: 'bool' ;
CHAR_TYPR: 'char' ;
STRING_TYPR: 'string' ;

//array type
OPEN_SQUARE_BR: '[' ;
CLOSE_SQUARE_BR: ']' ;

//pair type
PAIR_SEPERATOR: ',' ;
PAIR: 'pair' ;

//pair elem
FIRST_ELEM: 'fst' ;
SECOND_ELEM: 'snd' ;

//bool liter
TRUE: 'true' ;
FALSE: 'false' ;

//char liter
QUOTE: '\'' ;

//string liter
DOUBLE_QUOTE: '"' ;

//character
CHAR: (~('\\' | '\'' | '"')  | '\\n') ;

//escaped char
ESCAPED_CHAR: ('0' | 'b' | 't' | 'n' | 'f' | 'r' | '"' | '\'' | '\\') ;
