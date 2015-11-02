parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

// EOF indicates that the program must consume to the end of the input.
program: BEGIN (func)* stat END EOF ;

func: (type | arrayType) IDENT OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES IS stat END ;

paramList: param (COMMA param)* ;

param: (type | arrayType) IDENT ;

stat: SKIP
| (type | arrayType) IDENT ASSIGN assignRhs
| assignLhs ASSIGN assignRhs
| READ assignLhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN stat ELSE stat FI
| WHILE expr DO stat DONE
| BEGIN stat END
| stat SEMICOLON stat
;

assignLhs: IDENT
| arrayElem
| pairElem
;

assignRhs: expr
| arrayLiter
| NEW_PAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES
| pairElem
| CALL IDENT OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES
;

argList: expr (COMMA expr)* ;

pairElem: FST expr
| SND expr
;

type: baseType
| pairType
;

baseType: INT
| BOOL
| CHAR
| STRING
;

arrayType: type (OPEN_SQUARE_BR CLOSE_SQUARE_BR)* ;

pairType: PAIR OPEN_PARENTHESES pairElemType COMMA pairElemType CLOSE_PARENTHESES ;

pairElemType: baseType
| arrayType
| PAIR
;

expr: intLiter
| boolLiter
| CHAR_LITER
| STR_LITER
| PAIR_LITER
| IDENT
| arrayElem
| unaryOper expr
| expr binaryOper expr
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

unaryOper: NOT
| MINUS
| LEN
| ORD
| CHR
;

binaryOper: MULT
| DIV
| MOD
| PLUS
| MINUS
| GT
| GTEQ
| LT
| LTEQ
| EQ
| NE
| AND
| OR
;

arrayElem: IDENT (OPEN_SQUARE_BR expr CLOSE_SQUARE_BR)+ ;

intLiter: (intSign)? INTEGER ;

intSign: PLUS
| MINUS
;

boolLiter: TRUE
| FALSE
;

arrayLiter: OPEN_SQUARE_BR (expr (COMMA expr)*)? CLOSE_SQUARE_BR ;
