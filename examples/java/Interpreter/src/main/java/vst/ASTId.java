package vst;

public class ASTId extends Node {
	 String name;

	public ASTId(int id) {
		super(id);
	}

	public ASTId(SPLParser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public void jjtAccept(SPLParserVisitor visitor, Object data) {

		visitor.visit(this, data);
	}
}
