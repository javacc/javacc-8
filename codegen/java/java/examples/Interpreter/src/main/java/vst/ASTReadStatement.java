package vst;

class ASTReadStatement extends Node {
	String name;

	public ASTReadStatement(int id) {
		super(id);
	}

	public ASTReadStatement(SPLParser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public void jjtAccept(SPLParserVisitor visitor, Object data) {

		visitor.visit(this, data);
	}
}
