class ASTLiteral extends Node {
	public ASTLiteral(int id) {
		super(id);
	}

	public ASTLiteral(EcmaScript p, int id) {
		super(p, id);
	}

	/** Accept the visitor. **/
	public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public void setBooleanValue(String image) {
	}

	public void setDecimalValue(String image) {
	}

	public void setHexValue(String image) {
		// TODO Auto-generated method stub
		
	}

	public void setNullValue() {
		// TODO Auto-generated method stub
		
	}

	public void setRegexValue(String image) {
		// TODO Auto-generated method stub
		
	}

	public void setStringValue(String image) {
		// TODO Auto-generated method stub
		
	}
}
