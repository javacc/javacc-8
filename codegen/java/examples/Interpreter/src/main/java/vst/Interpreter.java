package vst;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Stack;

public class Interpreter implements SPLParserVisitor {
	private final InputStream in;
	private final PrintStream out;
	private final PrintStream err;

	Map<String, Tree> symtab;
	Stack<Tree> nodestack;
	
	Interpreter(InputStream in, PrintStream out, PrintStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
	}

	@Override
	public void visit(Node node, Object data) {

	}

	@Override
	public void visit(ASTAssignment node, Object data) {
	}

	@Override
	public void visit(ASTOrNode node, Object data) {

	}

	@Override
	public void visit(ASTAndNode node, Object data) {

	}

	@Override
	public void visit(ASTBitwiseOrNode node, Object data) {

	}

	@Override
	public void visit(ASTBitwiseXorNode node, Object data) {

	}

	@Override
	public void visit(ASTBitwiseAndNode node, Object data) {

	}

	@Override
	public void visit(ASTEQNode node, Object data) {

	}

	@Override
	public void visit(ASTNENode node, Object data) {

	}

	@Override
	public void visit(ASTLTNode node, Object data) {

	}

	@Override
	public void visit(ASTGTNode node, Object data) {

	}

	@Override
	public void visit(ASTLENode node, Object data) {

	}

	@Override
	public void visit(ASTGENode node, Object data) {

	}

	@Override
	public void visit(ASTAddNode node, Object data) {

	}

	@Override
	public void visit(ASTSubtractNode node, Object data) {

	}

	@Override
	public void visit(ASTMulNode node, Object data) {

	}

	@Override
	public void visit(ASTDivNode node, Object data) {

	}

	@Override
	public void visit(ASTModNode node, Object data) {

	}

	@Override
	public void visit(ASTBitwiseComplNode node, Object data) {

	}

	@Override
	public void visit(ASTNotNode node, Object data) {

	}

	@Override
	public void visit(ASTIntConstNode node, Object data) {

	}

	@Override
	public void visit(ASTTrueNode node, Object data) {

	}

	@Override
	public void visit(ASTFalseNode node, Object data) {

	}

	@Override
	public void visit(ASTCompilationUnit node, Object data) {
	}

	@Override
	public void visit(ASTVarDeclaration node, Object data) {
	}

	@Override
	public void visit(ASTId node, Object data) {
	}

	@Override
	public void visit(ASTBlock node, Object data) {
	}

	@Override
	public void visit(ASTStatementExpression node, Object data) {
	}

	@Override
	public void visit(ASTIfStatement node, Object data) {
	}

	@Override
	public void visit(ASTWhileStatement node, Object data) {
	}

	@Override
	public void visit(ASTReadStatement node, Object data) {
	}

	@Override
	public void visit(ASTWriteStatement node, Object data) {
	}

}
