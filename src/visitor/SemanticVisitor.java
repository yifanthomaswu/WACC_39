package visitor;

import antlr.*;

public class SemanticVisitor extends BasicParserBaseVisitor<Void> {

	public Void visitProgram(BasicParser.ProgramContext ctx) {
		System.out.println("Good morning, who's awake!?");
		// ctx.ident().IDENT();
		System.out.println("ctx symbol?: " + ctx.getText());
		
		return visitChildren(ctx);
	}

	public Void visitBaseType(BasicParser.BaseTypeContext ctx) {
		System.out.println("In base type, depth: " + ctx.depth());
		// System.out.println("In base type, INT: " + ctx.INT());
		System.out.println("In base type, getStart(): " + ctx.getStart());
		System.out.println("In base type, start: " + ctx.start);
		System.out.println("In base type, stop: " + ctx.stop);
		System.out.println("ctx symbol?: " + ctx.getText());
		return visitChildren(ctx);
	}

	public Void visitArrayType(BasicParser.ArrayTypeContext ctx) {
		return visitChildren(ctx);
	}

	public Void visitPairType(BasicParser.PairTypeContext ctx) {
		return visitChildren(ctx);
	}

	public Void visitPairElemType(BasicParser.PairElemTypeContext ctx) {
		return visitChildren(ctx);
	}

	public Void visitExpr(BasicParser.ExprContext ctx) {
		return visitChildren(ctx);
	}

}
