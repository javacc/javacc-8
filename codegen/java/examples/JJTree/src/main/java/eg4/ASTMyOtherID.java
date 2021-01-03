package eg4;
import eg4.Eg4;
import eg4.Eg4Visitor;
import eg4.Node;

public
class ASTMyOtherID extends Node {
  private String name;
  public ASTMyOtherID(int id) {
    super(id);
  }

  public ASTMyOtherID(Eg4 p, int id) {
    super(p, id);
  }

  /**
   * Set the name.
   * @param n the name
   */
  public void setName(String n) {
    name = n;
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.examples.jjtree.eg2.Node#toString()
   */
  public String toString() {
    return "Identifier: " + name;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(Eg4Visitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
