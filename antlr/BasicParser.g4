parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

// EOF indicates that the program must consume to the end of the input.
program: BEGIN (func)* stat END EOF ;

func: type ident OPEN_PARENTHESES (paramList)? CLOSE_PARENTHESES IS funcStat END ;

funcStat: (stat SEMICOLON)* (RETURN expr | EXIT expr | IF expr THEN funcStat ELSE funcStat FI) ;

paramList: param (COMMA param)* ;

param: type ident ;

stat: SKIP                                                       # SkipStat
| type ident ASSIGN assignRhs                                    # AssignVarStat
| assignLhs ASSIGN assignRhs                                     # AssignLhsToRhsStat
| READ assignLhs                                                 # ReadStat
| FREE expr                                                      # FreeStat
| RETURN expr                                                    # ReturnStat
| EXIT expr                                                      # ExitStat
| PRINT expr                                                     # PrintStat
| PRINTLN expr                                                   # PrintlnStat
| IF expr THEN stat ELSE stat FI                                 # IfThenElseStat
| WHILE expr DO stat DONE                                        # WhileStat
| BEGIN stat END                                                 # BeginStat
| stat SEMICOLON stat                                            # StatList
;

assignLhs: ident                                                 # LhsIdent
| arrayElem                                                      # LhsArrayElem
| pairElem                                                       # LhsPairElem
;

assignRhs: expr                                                  # RhsExpr
| arrayLiter                                                     # RhsArrayLiter
| NEW_PAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES    # RhsNewPair
| pairElem                                                       # RhsPairElem
| CALL ident OPEN_PARENTHESES (argList)? CLOSE_PARENTHESES       # RhsFunctionCall
;

argList: expr (COMMA expr)* ;

pairElem: FST expr                                               # FstPairElem
| SND expr                                                       # SndPairElem
;

type: baseType                                                   
| arrayType                                                      
| pairType                                                       
;

baseType: BASE_TYPE ;

arrayType: (baseType | pairType) (OPEN_SQUARE_BR CLOSE_SQUARE_BR)* ;

pairType: PAIR OPEN_PARENTHESES pairElemType COMMA pairElemType CLOSE_PARENTHESES ;

pairElemType: baseType                                           # PairElemBase
| arrayType                                                      # PairElemArray
| PAIR                                                           # PairPairElem
;

expr: intLiter                                                   # IntExpr
| boolLiter                                                      # BoolExpr
| charLiter                                                      # CharExpr
| stringLiter                                                    # StringExpr
| pairLiter                                                      # PairExpr
| ident                                                          # IdentExpr
| arrayElem                                                      # ArrayElemExpr
| unaryOper expr                                                 # UnOpExpr
| expr binaryOper expr                                           # BinOpExpr
| OPEN_PARENTHESES expr CLOSE_PARENTHESES                        # ParensExpr
;

unaryOper: UNARY_OPER | MINUS ;

binaryOper: BINARY_OPER | MINUS ;

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
