package vst;

public class ASTVarDeclaration extends SimpleNode {
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
