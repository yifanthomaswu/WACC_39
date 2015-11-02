parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

// EOF indicates that the program must consume to the end of the input.
program: PROG_BEGIN (func)* stat PROG_END EOF ;

func: (type | arrayType) ident OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES FUNC_BEGIN stat FUNC_END ;

paramList: param (PARAM_SEPARATOR param)* ;

param: (type | arrayType) ident ;

stat: SKIP
| (type | arrayType) ident ASSIGNMENT assignRhs
| assignLhs ASSIGNMENT assignRhs
| READ assignLhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN stat ELSE stat FI
| WHILE expr DO stat DONE
| BEGIN stat END
| stat STATEMENT_SEPARATOR stat
;

assignLhs: ident
| arrayElem
| pairElem
;

assignRhs: expr
| arrayLiter
| NEW_PAIR OPEN_PARENTHESES expr PAIR_SEPARATOR expr CLOSE_PARENTHESES
| pairElem
| CALL ident OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES
;

argList: expr (ARG_SEPARATOR expr)* ;

pairElem: FIRST_ELEM expr
| SECOND_ELEM expr
;

type: baseType
| pairType
;

baseType: INT_TYPE
| BOOL_TYPE
| CHAR_TYPE
| STRING_TYPE
;

arrayType: type (OPEN_SQUARE_BR CLOSE_SQUARE_BR)* ;

pairType: PAIR OPEN_PARENTHESES pairElemType PAIR_SEPARATOR pairElemType CLOSE_PARENTHESES ;

pairElemType: baseType
| arrayType
| PAIR
;

expr: intLiter
| boolLiter
| CHAR_LITER
| STR_LITER
| pairLiter
| ident
| arrayElem
| unaryOper expr
| expr binaryOper expr
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

unaryOper: NOT
| NEGATIVE
| ARRAY_LENGTH
| ORD
| CHR
;

binaryOper: MULTIPLY
| DIVIDE
| MODULUS
| PLUS
| MINUS
| GREATER_THAN
| GREATER_THAN_EQ
| SMALLER_THAN
| SMALLER_THAN_EQ
| EQUAL
| NOT_EQUAL
| AND
| OR
;

ident: BEGIN_IDENT (REST_IDENT)* ;

arrayElem: ident (OPEN_SQUARE_BR expr CLOSE_SQUARE_BR)+ ;

intLiter: (intSign)? INTEGER ;

intSign: POSITIVE_SIGN
| NEGATIVE_SIGN
;

boolLiter: TRUE
| FALSE
;

arrayLiter: OPEN_SQUARE_BR (expr (ARRAY_SEPARATOR expr)*)? CLOSE_SQUARE_BR ;

pairLiter: NULL ;
