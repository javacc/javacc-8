// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.java;

import org.javacc.Version;
import org.javacc.jjtree.ASTBNFAction;
import org.javacc.jjtree.ASTBNFDeclaration;
import org.javacc.jjtree.ASTBNFNodeScope;
import org.javacc.jjtree.ASTBNFNonTerminal;
import org.javacc.jjtree.ASTBNFOneOrMore;
import org.javacc.jjtree.ASTBNFSequence;
import org.javacc.jjtree.ASTBNFTryBlock;
import org.javacc.jjtree.ASTBNFZeroOrMore;
import org.javacc.jjtree.ASTBNFZeroOrOne;
import org.javacc.jjtree.ASTCompilationUnit;
import org.javacc.jjtree.ASTExpansionNodeScope;
import org.javacc.jjtree.ASTGrammar;
import org.javacc.jjtree.ASTJavacodeBody;
import org.javacc.jjtree.ASTNodeDescriptor;
import org.javacc.jjtree.ASTProduction;
import org.javacc.jjtree.DefaultJJTreeVisitor;
import org.javacc.jjtree.IO;
import org.javacc.jjtree.JJTreeContext;
import org.javacc.jjtree.JJTreeGlobals;
import org.javacc.jjtree.JJTreeNode;
import org.javacc.jjtree.Node;
import org.javacc.jjtree.NodeScope;
import org.javacc.jjtree.SimpleNode;
import org.javacc.jjtree.Token;
import org.javacc.jjtree.TokenUtils;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.Options;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

class JJTreeCodeGenerator extends DefaultJJTreeVisitor {

  private final JJTreeContext context;

  JJTreeCodeGenerator(JJTreeContext context) {
    this.context = context;
  }

  @Override
  public Object defaultVisit(SimpleNode node, Object data) {
    visit((JJTreeNode) node, data);
    return null;
  }

  @Override
  public Object visit(ASTGrammar node, Object data) {
    IO io = (IO) data;
    io.println(
        "/*@bgen(jjtree) " + JavaCCGlobals.getIdString("JJTree", new File(io.getOutputFileName()).getName()) + " */");
    io.print("/*@egen*/");
    return node.childrenAccept(this, io);
  }

  @Override
  public Object visit(ASTBNFAction node, Object data) {
    IO io = (IO) data;
    /*
     * Assume that this action requires an early node close, and then try to
     * decide whether this assumption is false. Do this by looking outwards
     * through the enclosing expansion units. If we ever find that we are
     * enclosed in a unit which is not the final unit in a sequence we know that
     * an early close is not required.
     */

    NodeScope ns = NodeScope.getEnclosingNodeScope(node);
    if ((ns != null) && !ns.isVoid()) {
      boolean needClose = true;
      Node sp = node.getScopingParent(ns);

      JJTreeNode n = node;
      while (true) {
        Node p = n.jjtGetParent();
        if ((p instanceof ASTBNFSequence) || (p instanceof ASTBNFTryBlock)) {
          if (n.getOrdinal() != (p.jjtGetNumChildren() - 1)) {
            /* We're not the final unit in the sequence. */
            needClose = false;
            break;
          }
        } else if ((p instanceof ASTBNFZeroOrOne) || (p instanceof ASTBNFZeroOrMore)
            || (p instanceof ASTBNFOneOrMore)) {
          needClose = false;
          break;
        }
        if (p == sp) {
          /* No more parents to look at. */
          break;
        }
        n = (JJTreeNode) p;
      }
      if (needClose) {
        JJTreeCodeGenerator.openJJTreeComment(io, null);
        io.println();
        insertCloseNodeAction(ns, io, getIndentation(node));
        JJTreeCodeGenerator.closeJJTreeComment(io);
      }
    }

    return visit((JJTreeNode) node, io);
  }

  @Override
  public Object visit(ASTBNFDeclaration node, Object data) {
    IO io = (IO) data;
    if (!node.node_scope.isVoid()) {
      String indent = "";
      if (TokenUtils.hasTokens(node)) {
        for (int i = 1; i < node.getFirstToken().beginColumn; ++i) {
          indent += " ";
        }
      } else {
        indent = "  ";
      }

      JJTreeCodeGenerator.openJJTreeComment(io, node.node_scope.getNodeDescriptorText());
      io.println();
      insertOpenNodeCode(node.node_scope, io, indent);
      JJTreeCodeGenerator.closeJJTreeComment(io);
    }

    return visit((JJTreeNode) node, io);
  }

  @Override
  public Object visit(ASTBNFNodeScope node, Object data) {
    IO io = (IO) data;
    if (node.node_scope.isVoid()) {
      return visit((JJTreeNode) node, io);
    }

    String indent = getIndentation(node.expansion_unit);

    JJTreeCodeGenerator.openJJTreeComment(io, node.node_scope.getNodeDescriptor().getDescriptor());
    io.println();
    tryExpansionUnit(node.node_scope, io, indent, node.expansion_unit);
    return null;
  }

  @Override
  public Object visit(ASTCompilationUnit node, Object data) {
    IO io = (IO) data;
    Token t = node.getFirstToken();
    while (true) {
      if (t == JJTreeGlobals.parserImports) {

        // If the parser and nodes are in separate packages (NODE_PACKAGE
        // specified in
        // OPTIONS), then generate an import for the node package.
        if (!JJTreeGlobals.nodePackageName.equals("")
            && !JJTreeGlobals.nodePackageName.equals(JJTreeGlobals.packageName)) {
          io.getOut().println("");
          io.getOut().println("import " + JJTreeGlobals.nodePackageName + ".*;");
        }
      }

      if (t == JJTreeGlobals.parserImplements) {
        if (t.image.equals("implements")) {
          node.print(t, io);
          JJTreeCodeGenerator.openJJTreeComment(io, null);
          io.getOut().print(" " + JavaTemplates.nodeConstants() + ", ");
          JJTreeCodeGenerator.closeJJTreeComment(io);
        } else {
          // t is pointing at the opening brace of the class body.
          JJTreeCodeGenerator.openJJTreeComment(io, null);
          io.getOut().print("implements " + JavaTemplates.nodeConstants());
          JJTreeCodeGenerator.closeJJTreeComment(io);
          node.print(t, io);
        }
      } else {
        node.print(t, io);
      }

      if (t == JJTreeGlobals.parserClassBodyStart) {
        String treeClazzName = "JJT" + JJTreeGlobals.parserName + "State";
        JJTreeCodeGenerator.openJJTreeComment(io, null);
        io.println();
        io.println("  protected " + JavaUtil.getStatic() + treeClazzName + " jjtree = new " + treeClazzName + "();");
        io.println();
        JJTreeCodeGenerator.closeJJTreeComment(io);
      }

      if (t == node.getLastToken()) {
        return null;
      }
      t = t.next;
    }
  }

  @Override
  public Object visit(ASTExpansionNodeScope node, Object data) {
    IO io = (IO) data;
    String indent = getIndentation(node.expansion_unit);
    JJTreeCodeGenerator.openJJTreeComment(io, node.node_scope.getNodeDescriptor().getDescriptor());
    io.println();
    insertOpenNodeAction(node.node_scope, io, indent);
    tryExpansionUnit(node.node_scope, io, indent, node.expansion_unit);

    // Print the "whiteOut" equivalent of the Node descriptor to preserve
    // line numbers in the generated file.
    ((ASTNodeDescriptor) node.jjtGetChild(1)).jjtAccept(this, io);
    return null;
  }

  @Override
  public Object visit(ASTJavacodeBody node, Object data) {
    IO io = (IO) data;
    if (node.node_scope.isVoid()) {
      return visit((JJTreeNode) node, io);
    }

    Token first = node.getFirstToken();

    String indent = "";
    for (int i = 4; i < first.beginColumn; ++i) {
      indent += " ";
    }

    JJTreeCodeGenerator.openJJTreeComment(io, node.node_scope.getNodeDescriptorText());
    io.println();
    insertOpenNodeCode(node.node_scope, io, indent);
    tryTokenSequence(node.node_scope, io, indent, first, node.getLastToken());
    return null;
  }

  /*
   * This method prints the tokens corresponding to this node recursively
   * calling the print methods of its children. Overriding this print method in
   * appropriate nodes gives the output the added stuff not in the input.
   */

  private Object visit(JJTreeNode node, Object data) {
    IO io = (IO) data;
    /*
     * Some productions do not consume any tokens. In that case their first and
     * last tokens are a bit strange.
     */
    if (node.getLastToken().next == node.getFirstToken()) {
      return null;
    }

    Token t1 = node.getFirstToken();
    Token t = new Token();
    t.next = t1;
    JJTreeNode n;
    for (int ord = 0; ord < node.jjtGetNumChildren(); ord++) {
      n = (JJTreeNode) node.jjtGetChild(ord);
      while (true) {
        t = t.next;
        if (t == n.getFirstToken()) {
          break;
        }
        node.print(t, io);
      }
      n.jjtAccept(this, io);
      t = n.getLastToken();
    }
    while (t != node.getLastToken()) {
      t = t.next;
      node.print(t, io);
    }

    return null;
  }


  private static void openJJTreeComment(IO io, String arg) {
    if (arg != null) {
      io.print("/*@bgen(jjtree) " + arg + " */");
    } else {
      io.print("/*@bgen(jjtree)*/");
    }
  }


  private static void closeJJTreeComment(IO io) {
    io.print("/*@egen*/");
  }


  private String getIndentation(JJTreeNode n) {
    return getIndentation(n, 0);
  }


  private String getIndentation(JJTreeNode n, int offset) {
    String s = "";
    for (int i = offset + 1; i < n.getFirstToken().beginColumn; ++i) {
      s += " ";
    }
    return s;
  }

  private void insertOpenNodeCode(NodeScope ns, IO io, String indent) {
    String type = ns.node_descriptor.getNodeType(context);
    final String nodeClass;
    if ((context.treeOptions().getNodeClass().length() > 0) && !context.treeOptions().getMulti()) {
      nodeClass = context.treeOptions().getNodeClass();
    } else {
      nodeClass = type;
    }

    /*
     * Ensure that there is a template definition file for the node type.
     */
    NodeFiles.generateNodeType(type);

    io.print(indent + nodeClass + " " + ns.nodeVar + " = ");
    String p = Options.getStatic() ? "null" : "this";
    String parserArg = context.treeOptions().getNodeUsesParser() ? (p + ", ") : "";

    if (context.treeOptions().getNodeFactory().equals("*")) {
      // Old-style multiple-implementations.
      io.println("(" + nodeClass + ")" + nodeClass + ".jjtCreate(" + parserArg + ns.node_descriptor.getNodeId() + ");");
    } else if (context.treeOptions().getNodeFactory().length() > 0) {
      io.println("(" + nodeClass + ")" + context.treeOptions().getNodeFactory() + ".jjtCreate(" + parserArg
          + ns.node_descriptor.getNodeId() + ");");
    } else {
      io.println("new " + nodeClass + "(" + parserArg + ns.node_descriptor.getNodeId() + ");");
    }

    if (ns.usesCloseNodeVar()) {
      io.println(indent + "boolean " + ns.closedVar + " = true;");
    }
    io.println(indent + ns.node_descriptor.openNode(ns.nodeVar));
    if (context.treeOptions().getNodeScopeHook()) {
      io.println(indent + "jjtreeOpenNodeScope(" + ns.nodeVar + ");");
    }

    if (context.treeOptions().getTrackTokens()) {
      io.println(indent + ns.nodeVar + ".jjtSetFirstToken(getToken(1));");
    }
  }


  private void insertCloseNodeCode(NodeScope ns, IO io, String indent, boolean isFinal) {
    String closeNode = ns.node_descriptor.closeNode(ns.nodeVar);
    io.println(indent + closeNode);
    if (ns.usesCloseNodeVar() && !isFinal) {
      io.println(indent + ns.closedVar + " = false;");
    }
    if (context.treeOptions().getNodeScopeHook()) {
      io.println(indent + "if (jjtree.nodeCreated()) {");
      io.println(indent + " jjtreeCloseNodeScope(" + ns.nodeVar + ");");
      io.println(indent + "}");
    }

    if (context.treeOptions().getTrackTokens()) {
      io.println(indent + ns.nodeVar + ".jjtSetLastToken(getToken(0));");
    }
  }


  private void insertOpenNodeAction(NodeScope ns, IO io, String indent) {
    io.println(indent + "{");
    insertOpenNodeCode(ns, io, indent + "  ");
    io.println(indent + "}");
  }


  private void insertCloseNodeAction(NodeScope ns, IO io, String indent) {
    io.println(indent + "{");
    insertCloseNodeCode(ns, io, indent + "  ", false);
    io.println(indent + "}");
  }


  private void insertCatchBlocks(NodeScope ns, IO io, Enumeration<String> thrown_names, String indent) {
    String thrown;
    if (thrown_names.hasMoreElements()) {
      io.println(indent + "} catch (Throwable " + ns.exceptionVar + ") {");

      if (ns.usesCloseNodeVar()) {
        io.println(indent + "  if (" + ns.closedVar + ") {");
        io.println(indent + "    jjtree.clearNodeScope(" + ns.nodeVar + ");");
        io.println(indent + "    " + ns.closedVar + " = false;");
        io.println(indent + "  } else {");
        io.println(indent + "    jjtree.popNode();");
        io.println(indent + "  }");
      }

      while (thrown_names.hasMoreElements()) {
        thrown = thrown_names.nextElement();
        io.println(indent + "  if (" + ns.exceptionVar + " instanceof " + thrown + ") {");
        io.println(indent + "    throw (" + thrown + ")" + ns.exceptionVar + ";");
        io.println(indent + "  }");
      }
      /*
       * This is either an Error or an undeclared Exception. If it's an Error
       * then the cast is good, otherwise we want to force the user to declare
       * it by crashing on the bad cast.
       */
      io.println(indent + "  throw (Error)" + ns.exceptionVar + ";");
    }

  }

  private void tryTokenSequence(NodeScope ns, IO io, String indent, Token first, Token last) {
    io.println(indent + "try {");
    JJTreeCodeGenerator.closeJJTreeComment(io);

    /*
     * Print out all the tokens, converting all references to `jjtThis' into the
     * current node variable.
     */
    for (Token t = first; t != last.next; t = t.next) {
      TokenUtils.print(t, io, "jjtThis", ns.nodeVar);
    }

    JJTreeCodeGenerator.openJJTreeComment(io, null);
    io.println();

    Enumeration<String> thrown_names = ns.production.throws_list.elements();
    insertCatchBlocks(ns, io, thrown_names, indent);

    io.println(indent + "} finally {");
    if (ns.usesCloseNodeVar()) {
      io.println(indent + "  if (" + ns.closedVar + ") {");
      insertCloseNodeCode(ns, io, indent + "    ", true);
      io.println(indent + "  }");
    }
    io.println(indent + "}");
    JJTreeCodeGenerator.closeJJTreeComment(io);
  }


  private static void findThrown(NodeScope ns, Hashtable<String, String> thrown_set, JJTreeNode expansion_unit) {
    if (expansion_unit instanceof ASTBNFNonTerminal) {
      /*
       * Should really make the nonterminal explicitly maintain its name.
       */
      String nt = expansion_unit.getFirstToken().image;
      ASTProduction prod = JJTreeGlobals.productions.get(nt);
      if (prod != null) {
        Enumeration<String> e = prod.throws_list.elements();
        while (e.hasMoreElements()) {
          String t = e.nextElement();
          thrown_set.put(t, t);
        }
      }
    }
    for (int i = 0; i < expansion_unit.jjtGetNumChildren(); ++i) {
      JJTreeNode n = (JJTreeNode) expansion_unit.jjtGetChild(i);
      JJTreeCodeGenerator.findThrown(ns, thrown_set, n);
    }
  }


  private void tryExpansionUnit(NodeScope ns, IO io, String indent, JJTreeNode expansion_unit) {
    io.println(indent + "try {");
    JJTreeCodeGenerator.closeJJTreeComment(io);

    expansion_unit.jjtAccept(this, io);

    JJTreeCodeGenerator.openJJTreeComment(io, null);
    io.println();

    Hashtable<String, String> thrown_set = new Hashtable<>();
    JJTreeCodeGenerator.findThrown(ns, thrown_set, expansion_unit);
    Enumeration<String> thrown_names = thrown_set.elements();
    insertCatchBlocks(ns, io, thrown_names, indent);

    io.println(indent + "} finally {");
    if (ns.usesCloseNodeVar()) {
      io.println(indent + "  if (" + ns.closedVar + ") {");
      insertCloseNodeCode(ns, io, indent + "    ", true);
      io.println(indent + "  }");
    }
    io.println(indent + "}");
    JJTreeCodeGenerator.closeJJTreeComment(io);
  }

  @Override
  public void generateHelperFiles() throws java.io.IOException {
    CodeGeneratorSettings options = CodeGeneratorSettings.of(Options.getOptions());
    options.set(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);

    try (JavaCodeBuilder builder = JavaCodeBuilder.of(context, options)) {
      builder
      .setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), "JJT" + JJTreeGlobals.parserName + "State.java"));
      builder.setVersion(Version.version).addTools(JavaCCGlobals.toolName);
      NodeFiles.generateProlog(builder);
      builder.printTemplate("/templates/JJTTreeState.template");
    }

    NodeFiles.generateOutputFiles(context);
  }
}
