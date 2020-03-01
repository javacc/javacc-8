class ASTIdentifier extends Node {
	public ASTIdentifier(int id) {
		super(id);
	}

	public ASTIdentifier(EcmaScript p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public void setName(String image) {
		// TODO Auto-generated method stub
		
	}
}
