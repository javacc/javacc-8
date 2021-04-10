
package org.javacc.csharp;

import org.javacc.Version;
import org.javacc.jjtree.ASTNodeDescriptor;
import org.javacc.jjtree.JJTreeContext;
import org.javacc.jjtree.JJTreeGlobals;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Options;
import org.javacc.utils.CodeBuilder.GenericCodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class NodeFiles {

  private NodeFiles() {}

  /**
   * ID of the latest version (of JJTree) in which one of the Node classes was
   * modified.
   */
  private static final String nodeVersion  = Version.version;

  private static Set<String>  nodesToBuild = new HashSet<>();

  static void generateNodeType(String nodeType) {
    if (!nodeType.equals("Node") && !nodeType.equals("SimpleNode")) {
      NodeFiles.nodesToBuild.add(nodeType);
    }
  }

  private static void generateTreeNodes(JJTreeContext context) {
    CodeGeneratorSettings options = CodeGeneratorSettings.of(Options.getOptions());
    options.set(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(context.treeOptions().getVisitorReturnType().equals("void")));

    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, options)) {
      builder.setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), JJTreeGlobals.parserName + "Tree.cs"));
      builder.setVersion(NodeFiles.nodeVersion).addTools(JJTreeGlobals.toolName);
      builder.addOption("MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS",
          "NODE_FACTORY", Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      for (String node : NodeFiles.nodesToBuild) {
        builder.printTemplate("/templates/csharp/MultiNode.template",
            CodeGeneratorSettings.create().set("NODE_TYPE", node));
      }

      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  private static String nodeConstants() {
    return JJTreeGlobals.parserName + "TreeConstants";
  }

  private static void generateTreeConstants(JJTreeContext context) {
    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, CodeGeneratorSettings.create())) {
      builder.setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), NodeFiles.nodeConstants() + ".cs"));

      List<String> nodeIds = ASTNodeDescriptor.getNodeIds();
      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }
      builder.println("public class " + NodeFiles.nodeConstants());
      builder.println("{");

      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = nodeIds.get(i);
        builder.println("  public const int " + n + " = " + i + ";");
      }

      builder.println();
      builder.println();

      builder.println("  public static string[] jjtNodeName = {");
      for (String n : nodeNames) {
        builder.println("    \"" + n + "\",");
      }
      builder.println("  };");

      builder.println("}");
      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static String visitorClass() {
    return JJTreeGlobals.parserName + "Visitor";
  }

  private static void generateVisitor(JJTreeContext context) {
    if (!context.treeOptions().getVisitor()) {
      return;
    }

    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, CodeGeneratorSettings.create())) {
      builder.setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), NodeFiles.visitorClass() + ".cs"));

      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }
      builder.println("public interface " + NodeFiles.visitorClass());
      builder.println("{");

      String ve = NodeFiles.mergeVisitorException(context);

      String argumentType = "object";
      if (!context.treeOptions().getVisitorDataType().equals("")) {
        argumentType = context.treeOptions().getVisitorDataType();
      }

      builder.println("  " + context.treeOptions().getVisitorReturnType() + " Visit(SimpleNode node, " + argumentType + " data)"
          + ve + ";");
      if (context.treeOptions().getMulti()) {
        for (String n : nodeNames) {
          if (n.equals("void")) {
            continue;
          }
          String nodeType = context.treeOptions().getNodePrefix() + n;
          builder.println("  " + context.treeOptions().getVisitorReturnType() + " " + NodeFiles.getVisitMethodName(nodeType)
          + "(" + nodeType + " node, " + argumentType + " data)" + ve + ";");
        }
      }
      builder.println("}");
      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String defaultVisitorClass() {
    return JJTreeGlobals.parserName + "DefaultVisitor";
  }

  private static String getVisitMethodName(String className) {
    StringBuffer sb = new StringBuffer("Visit");
    if (Options.booleanValue("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME")) {
      sb.append(Character.toUpperCase(className.charAt(0)));
      for (int i = 1; i < className.length(); i++) {
        sb.append(className.charAt(i));
      }
    }

    return sb.toString();
  }

  private static void generateDefaultVisitor(JJTreeContext context) {
    if (!context.treeOptions().getVisitor()) {
      return;
    }

    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, CodeGeneratorSettings.create())) {
      builder.setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), NodeFiles.defaultVisitorClass() + ".cs"));

      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }
      builder.println("public class " + NodeFiles.defaultVisitorClass() + " : " + NodeFiles.visitorClass() + "{");

      String ve = NodeFiles.mergeVisitorException(context);

      String argumentType = "object";
      if (!context.treeOptions().getVisitorDataType().equals("")) {
        argumentType = context.treeOptions().getVisitorDataType();
      }

      String ret = context.treeOptions().getVisitorReturnType();
      builder
      .println("  public virtual " + ret + " defaultVisit(SimpleNode node, " + argumentType + " data)" + ve + "{");
      builder.println("    node.childrenAccept(this, data);");
      builder.println("    return" + (ret.trim().equals("void") ? "" : " data") + ";");
      builder.println("  }");

      builder.println("  public virtual " + ret + " Visit(SimpleNode node, " + argumentType + " data)" + ve + "{");
      builder.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
      builder.println("  }");

      if (context.treeOptions().getMulti()) {
        for (String n : nodeNames) {
          if (n.equals("void")) {
            continue;
          }
          String nodeType = context.treeOptions().getNodePrefix() + n;
          builder.println("  public virtual " + ret + " " + NodeFiles.getVisitMethodName(nodeType) + "(" + nodeType
              + " node, " + argumentType + " data)" + ve + "{");
          builder.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
          builder.println("  }");
        }
      }
      builder.println("}");
      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static String mergeVisitorException(JJTreeContext context) {
    String ve = context.treeOptions().getVisitorException();
    if (!"".equals(ve)) {
      ve = " throws " + ve;
    }
    return ve;
  }

  private static void generateDefaultNode(JJTreeContext context) throws IOException {
    CodeGeneratorSettings options = CodeGeneratorSettings.of(Options.getOptions());
    options.set(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(context.treeOptions().getVisitorReturnType().equals("void")));

    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, options)) {
      builder.setFile(new File(context.treeOptions().getJJTreeOutputDirectory(), "Node.cs"));
      builder.printTemplate("/templates/csharp/Node.template");
    }
  }

  static void generateOutputFiles(JJTreeContext context) throws IOException {
    NodeFiles.generateDefaultNode(context);
    NodeFiles.generateTreeNodes(context);
    NodeFiles.generateTreeConstants(context);
    NodeFiles.generateVisitor(context);
    NodeFiles.generateDefaultVisitor(context);
  }

}
