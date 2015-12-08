parser grammar WACCParser;

options {
  tokenVocab=WACCLexer;
}

// EOF indicates that the program must consume to the end of the input.
program: BEGIN (func)* stat END EOF ;

func: type ident OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES IS stat END ;

paramList: param (COMMA param)* ;

param: type ident ;

stat: SKIP                                                       # SkipStat
| type ident ASSIGN assignRhs                                    # VarDeclStat
| assignLhs ASSIGN assignRhs                                     # AssignStat
| READ assignLhs                                                 # ReadStat
| FREE expr                                                      # FreeStat
| RETURN expr                                                    # ReturnStat
| EXIT expr                                                      # ExitStat
| PRINT expr                                                     # PrintStat
| PRINTLN expr                                                   # PrintlnStat
| IF expr THEN stat ELSE stat FI                                 # IfStat
| WHILE expr DO stat DONE                                        # WhileStat
| BEGIN stat END                                                 # ScopingStat
| stat SEMICOLON stat                                            # CompStat
;

assignLhs: ident                                                 # LhsIdent
| arrayElem                                                      # LhsArrayElem
| pairElem                                                       # LhsPairElem
;

assignRhs: expr                                                  # RhsExpr
| arrayLiter                                                     # RhsArrayLiter
| NEW_PAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES    # RhsNewPair
| pairElem                                                       # RhsPairElem
| CALL ident OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES       # RhsCall
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

arrayType: (baseType | pairType) (OPEN_SQUARE_BR CLOSE_SQUARE_BR)+ ;

pairType: PAIR OPEN_PARENTHESES pairElemType COMMA pairElemType CLOSE_PARENTHESES ;

pairElemType: baseType
| arrayType
| PAIR
;

expr: intLiter                                                   # IntExpr
| boolLiter                                                      # BoolExpr
| charLiter                                                      # CharExpr
| stringLiter                                                    # StringExpr
| pairLiter                                                      # PairExpr
| ident                                                          # IdentExpr
| arrayElem                                                      # ArrayElemExpr
| unaryOper expr                                                 # UnOpExpr
| expr (MULT | DIV | MOD) expr                                   # BinOpPrec1Expr
| expr (PLUS | MINUS) expr                                       # BinOpPrec2Expr
| expr (GRT | GRT_EQUAL | LESS | LESS_EQUAL) expr                # BinOpPrec3Expr
| expr (EQUAL | NOT_EQUAL) expr                                  # BinOpPrec4Expr
| expr AND expr                                                  # BinOpPrec5Expr
| expr OR expr                                                   # BinOpPrec6Expr
| OPEN_PARENTHESES expr CLOSE_PARENTHESES                        # ParensExpr
;

unaryOper: UNARY_OPER | MINUS ;

ident: IDENT ;

arrayElem: ident (OPEN_SQUARE_BR expr CLOSE_SQUARE_BR)+ ;

intLiter: INT_LITER {
  long n = Long.parseLong($INT_LITER.getText());
  if (n > Integer.MAX_VALUE || n < Integer.MIN_VALUE) {
    notifyErrorListeners("Syntactic Error at " + $INT_LITER.getLine() + ":" + $INT_LITER.getCharPositionInLine() + " -- " + "Integer value " + n + " is too large for a 32-bit signed integer");
  }
} ;

boolLiter: BOOL_LITER ;

charLiter: CHAR_LITER ;

stringLiter: STR_LITER ;

arrayLiter: OPEN_SQUARE_BR (expr (COMMA expr)*)? CLOSE_SQUARE_BR ;

pairLiter: NULL ;
