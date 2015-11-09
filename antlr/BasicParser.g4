parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

// EOF indicates that the program must consume to the end of the input.
program: BEGIN (func)* stat END EOF ;

func: type ident OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES IS stat END ;

//funcStat: ((stat SEMICOLON)* (RETURN expr | EXIT expr)) | (IF expr THEN (stat SEMICOLON)* (RETURN expr | EXIT expr) ELSE (stat SEMICOLON)* (RETURN expr | EXIT expr) FI);

paramList: param (COMMA param)* ;

param: type ident ;

stat: SKIP
| type ident ASSIGN assignRhs
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

assignLhs: ident
| arrayElem
| pairElem
;

assignRhs: expr
| arrayLiter
| NEW_PAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES
| pairElem
| CALL ident OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES
;

argList: expr (COMMA expr)* ;

pairElem: FST expr
| SND expr
;

type: baseType
| arrayType
| pairType
;

baseType: BASE_TYPE ;

arrayType: (baseType | pairType) (OPEN_SQUARE_BR CLOSE_SQUARE_BR)* ;

pairType: PAIR OPEN_PARENTHESES pairElemType COMMA pairElemType CLOSE_PARENTHESES ;

pairElemType: baseType
| arrayType
| PAIR
;

expr: intLiter
| boolLiter
| charLiter
| stringLiter
| pairLiter
| ident
| arrayElem
| unaryOper expr
| expr binaryOper expr
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

unaryOper: UNARY_OPER | NEG;

binaryOper: BINARY_OPER | NEG;

ident: IDENT ;

arrayElem: ident (OPEN_SQUARE_BR expr CLOSE_SQUARE_BR)+ ;

intLiter: INT_LITER ;

boolLiter: BOOL_LITER ;

charLiter: CHAR_LITER ;

stringLiter: STR_LITER ;

arrayLiter: OPEN_SQUARE_BR (expr (COMMA expr)*)? CLOSE_SQUARE_BR ;

pairLiter: NULL;
