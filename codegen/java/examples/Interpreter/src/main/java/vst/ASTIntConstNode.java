package vst;

public class ASTIntConstNode extends Node {
	public int val;
	
	public ASTIntConstNode(int id) {
		super(id);
	}

	public ASTIntConstNode(SPLParser p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public void jjtAccept(SPLParserVisitor visitor, Object data) {

		visitor.visit(this, data);
	}
}
