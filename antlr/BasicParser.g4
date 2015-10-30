parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

func: type ident OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES stat END

paramList: param (',' param)*

stat: SKIP
| type ident '=' assignRhs
| assignLhs '=' assignRhs
| READ assignLhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN stat ELSE stat FI
| WHILE expr DO stat DONE
| BEGIN stat END
| stat ';' stat

assignLhs: ident
| arrayElem
| pairElem

assignRhs: expr
| arrayLiter
| NEWPAIR OPEN_PARENTHESES expr ',' expr CLOSE_PARENTHESES
| pairElem
| CALL ident OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES

aryList: expr (',' expr)*

pairElem: FST expr
| SND expr

type: baseType
| arrayType
| pairType

baseType: INT
| BOOL
| CHAR
| STRING

arrayType: type '[' ']'

pairType: PAIR OPEN_PARENTHESES pairElemType '.' pairElemType CLOSE_PARENTHESES

pairElemType: baseType
| arrayType
| PAIR

expr: intLiter
| boolLiter
| charLiter
| strLiter
| pairLiter
| ident
| arrayElem
| unaryOper expr
| expr binaryOper expr
// | INTEGER
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

// EOF indicates that the program must consume to the end of the input.
// prog: (expr)*  EOF ;
program: BEGIN (func)* stat END EOF;
