package vst;

public class ASTVarDeclaration extends Node {
	int type;
	String name;

	public ASTVarDeclaration(int id) {
		super(id);
	}

	public ASTVarDeclaration(SPLParser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public void jjtAccept(SPLParserVisitor visitor, Object data) {

		visitor.visit(this, data);
	}
}
