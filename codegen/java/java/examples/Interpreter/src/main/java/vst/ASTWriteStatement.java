package vst;

class ASTWriteStatement extends Node {
	String name;

	public ASTWriteStatement(int id) {
		super(id);
	}

	public ASTWriteStatement(SPLParser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public void jjtAccept(SPLParserVisitor visitor, Object data) {

		visitor.visit(this, data);
	}
}
