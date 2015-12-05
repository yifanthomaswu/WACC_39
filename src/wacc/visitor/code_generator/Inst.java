package wacc.visitor.code_generator;

public enum Inst {
  MOV,
  MOVGT,
  MOVLE,
  MOVGE,
  MOVLT,
  MOVEQ,
  MOVNE,
  PUSH,
  POP,
  LDR,
  LDREQ,
  LDRNE,
  LDRSB,
  LDRLT,
  LDRCS,
  STR,
  STRB,
  B,
  BEQ,
  BL,
  BLEQ,
  BLVS,
  BLNE,
  BLLT,
  BLCS,
  ADD,
  ADDS,
  SUB,
  SUBS,
  SMULL,
  CMP,
  AND,
  ORR;
}
