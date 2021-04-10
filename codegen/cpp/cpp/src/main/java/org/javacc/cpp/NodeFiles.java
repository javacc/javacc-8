
package org.javacc.cpp;

import org.javacc.Version;
import org.javacc.jjtree.ASTNodeDescriptor;
import org.javacc.jjtree.JJTreeContext;
import org.javacc.jjtree.JJTreeGlobals;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class NodeFiles {

  private NodeFiles() {}

  private static List<String> headersForJJTreeH = new ArrayList<>();
  /**
   * ID of the latest version (of JJTree) in which one of the Node classes was
   * modified.
   */
  private static final String nodeVersion       = Version.version;

  private static Set<String>  nodesToBuild      = new HashSet<>();

  static void generateNodeType(String nodeType) {
    if (!nodeType.equals("Tree") && !nodeType.equals("Node")) {
      NodeFiles.nodesToBuild.add(nodeType);
    }
  }

  private static String nodeIncludeFile(File outputDirectory) {
    return new File(outputDirectory, "Tree.h").getAbsolutePath();
  }

  private static String simpleNodeCodeFile(File outputDirectory) {
    return new File(outputDirectory, "Node.cc").getAbsolutePath();
  }

  private static String jjtreeIncludeFile(File outputDirectory) {
    return new File(outputDirectory, JJTreeGlobals.parserName + "Tree.h").getAbsolutePath();
  }

  private static String jjtreeASTNodeImplFile(File outputDirectory, String s) {
    return new File(outputDirectory, s + ".cc").getAbsolutePath();
  }

  private static String jjtreeImplFile(File outputDirectory, String s) {
    return new File(outputDirectory, s + ".cc").getAbsolutePath();
  }

  private static String visitorIncludeFile(File outputDirectory) {
    String name = NodeFiles.visitorClass();
    return new File(outputDirectory, name + ".h").getAbsolutePath();
  }

  static void generateOutputFiles(JJTreeContext context) throws IOException {
    NodeFiles.generateNodeHeader(context);
    NodeFiles.generateSimpleNode(context);
    NodeFiles.generateOneTree(context, false);
    NodeFiles.generateMultiTree(context);
    NodeFiles.generateTreeConstants(context);
    NodeFiles.generateVisitors(context);
  }

  private static void generateNodeHeader(JJTreeContext context) {
    CodeGeneratorSettings optionMap = CodeGeneratorSettings.of(Options.getOptions());
    optionMap.set("PARSER_NAME", JJTreeGlobals.parserName);
    optionMap.set("VISITOR_RETURN_TYPE", NodeFiles.getVisitorReturnType());
    optionMap.set("VISITOR_DATA_TYPE", NodeFiles.getVisitorArgumentType());
    optionMap.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(NodeFiles.getVisitorReturnType().equals("void")));

    try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, optionMap)) {
      builder.setFile(new File(NodeFiles.nodeIncludeFile(context.treeOptions().getJJTreeOutputDirectory())));
      builder.setVersion(NodeFiles.nodeVersion).addTools(JJTreeGlobals.toolName);
      builder.addOption("MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS",
          "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC");

      builder.printTemplate("/templates/cpp/Tree.h.template");
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  private static void generateSimpleNode(JJTreeContext context) {
    CodeGeneratorSettings optionMap = CodeGeneratorSettings.of(Options.getOptions());
    optionMap.set(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    optionMap.set("VISITOR_RETURN_TYPE", NodeFiles.getVisitorReturnType());
    optionMap.set("VISITOR_DATA_TYPE", NodeFiles.getVisitorArgumentType());
    optionMap.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(NodeFiles.getVisitorReturnType().equals("void")));

    try (CppCodeBuilder builder = CppCodeBuilder.of(context, optionMap)) {
      builder.setFile(new File(NodeFiles.simpleNodeCodeFile(context.treeOptions().getJJTreeOutputDirectory())));
      builder.setVersion(NodeFiles.nodeVersion).addTools(JJTreeGlobals.toolName);
      builder.addOption("MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS",
          "NODE_FACTORY", Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

      builder.printTemplate("/templates/cpp/Node.cc.template");
      builder.switchToIncludeFile();
      builder.printTemplate("/templates/cpp/Node.h.template");
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  private static void generateOneTree(JJTreeContext context, boolean generateOneTreeImpl) {
    CodeGeneratorSettings optionMap = CodeGeneratorSettings.of(Options.getOptions());
    optionMap.set("PARSER_NAME", JJTreeGlobals.parserName);
    optionMap.set("VISITOR_RETURN_TYPE", NodeFiles.getVisitorReturnType());
    optionMap.set("VISITOR_DATA_TYPE", NodeFiles.getVisitorArgumentType());
    optionMap.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(NodeFiles.getVisitorReturnType().equals("void")));

    try (CppCodeBuilder builder =
        generateOneTreeImpl ? CppCodeBuilder.of(context, optionMap) : CppCodeBuilder.ofHeader(context, optionMap)) {
      builder.setFile(new File(NodeFiles.jjtreeIncludeFile(context.treeOptions().getJJTreeOutputDirectory())));
      builder.setVersion(NodeFiles.nodeVersion).addTools(JJTreeGlobals.toolName);
      builder.addOption("MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS",
          "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC");

      builder.switchToIncludeFile();
      String guard = "JAVACC_" + "ONE_TREE_H";
      builder.println("#ifndef " + guard);
      builder.println("#define " + guard);
      builder.println();
      builder.println("#include \"Node.h\"");
      for (String s : NodeFiles.nodesToBuild) {
        builder.println("#include \"" + s + ".h\"");
        if (generateOneTreeImpl) {
          builder.switchToMainFile();
          builder.printTemplate("/templates/cpp/MultiNode.cc.template",
              CodeGeneratorSettings.create().set("NODE_TYPE", s));
          builder.switchToIncludeFile();
        }
      }
      builder.println("#endif");
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  private static void generateMultiTree(JJTreeContext context) {
    for (String node : NodeFiles.nodesToBuild) {
      if (new File(NodeFiles.jjtreeASTNodeImplFile(context.treeOptions().getASTNodeDirectory(), node)).exists()) {
        continue;
      }

      CodeGeneratorSettings optionMap = CodeGeneratorSettings.of(Options.getOptions());
      optionMap.set(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
      optionMap.set("VISITOR_RETURN_TYPE", NodeFiles.getVisitorReturnType());
      optionMap.set("VISITOR_DATA_TYPE", NodeFiles.getVisitorArgumentType());
      optionMap.set("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(NodeFiles.getVisitorReturnType().equals("void")));
      optionMap.set("NODE_TYPE", node);

      try (CppCodeBuilder builder = CppCodeBuilder.of(context, optionMap)) {
        builder.setFile(new File(NodeFiles.jjtreeImplFile(context.treeOptions().getJJTreeOutputDirectory(), node)));
        builder.setVersion(NodeFiles.nodeVersion).addTools(JJTreeGlobals.toolName);
        builder.addOption("MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS",
            "NODE_FACTORY", Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

        builder.printTemplate("/templates/cpp/MultiNode.cc.template");
        builder.switchToIncludeFile();
        builder.printTemplate("/templates/cpp/MultiNode.h.template");
      } catch (IOException e) {
        throw new Error(e.toString());
      }
    }
  }

  private static String nodeConstants() {
    return JJTreeGlobals.parserName + "TreeConstants";
  }

  private static void generateTreeConstants(JJTreeContext context) {
    List<String> nodeIds = ASTNodeDescriptor.getNodeIds();
    List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

    File file = new File(context.treeOptions().getJJTreeOutputDirectory(), NodeFiles.nodeConstants() + ".h");
    NodeFiles.headersForJJTreeH.add(file.getName());

    try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, CodeGeneratorSettings.create())) {
      builder.setFile(file);

      String guard = "JAVACC_" + nodeConstants().toUpperCase() + "_H";
      builder.println("#ifndef " + guard);
      builder.println("#define " + guard);
      builder.println();
      builder.println("#include \"JavaCC.h\"");

       if (Options.hasNamespace()) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      builder.println("enum {");
      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = nodeIds.get(i);
        builder.println("  " + n + " = " + i + ",");
      }

      builder.println("};");
      builder.println();

      for (int i = 0; i < nodeNames.size(); ++i) {
        builder.println("  static JJChar jjtNodeName_arr_", i, "[] = ");
        builder.printCharArray(nodeNames.get(i));
        builder.println(";");
      }
      builder.println("  static JJString jjtNodeName[] = {");
      for (int i = 0; i < nodeNames.size(); i++) {
        builder.println("jjtNodeName_arr_", i, ", ");
      }
      builder.println("  };");

      if (Options.hasNamespace()) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
      builder.println("#endif");
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  private static String visitorClass() {
    return JJTreeGlobals.parserName + "Visitor";
  }

  private static String getVisitMethodName(String className) {
    StringBuffer sb = new StringBuffer("visit");
    if (Options.booleanValue("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME")) {
      sb.append(Character.toUpperCase(className.charAt(0)));
      for (int i = 1; i < className.length(); i++) {
        sb.append(className.charAt(i));
      }
    }

    return sb.toString();
  }

  private static String getVisitorArgumentType() {
    String ret = Options.stringValue("VISITOR_DATA_TYPE");
    return (ret == null) || ret.equals("") || ret.equals("Object") ? "void *" : ret;
  }

  private static String getVisitorReturnType() {
    String ret = Options.stringValue("VISITOR_RETURN_TYPE");
    return (ret == null) || ret.equals("") || ret.equals("Object") ? "void" : ret;
  }

  private static void generateVisitors(JJTreeContext context) {
    if (!context.treeOptions().getVisitor()) {
      return;
    }

    try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, CodeGeneratorSettings.create())) {
      builder.setFile(new File(NodeFiles.visitorIncludeFile(context.treeOptions().getJJTreeOutputDirectory())));

      String guard = "JAVACC_" + JJTreeGlobals.parserName.toUpperCase() + "_VISITOR_H";
      builder.println("#ifndef " + guard);
      builder.println("#define " + guard);
      builder.println();
      builder.println("#include \"JavaCC.h\"");
      builder.println("#include \"" + JJTreeGlobals.parserName + "Tree.h" + "\"");

      if (Options.hasNamespace()) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      NodeFiles.generateVisitorInterface(builder, context);
      NodeFiles.generateDefaultVisitor(builder, context);

      if (Options.hasNamespace()) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
      builder.println("#endif");
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  private static void generateVisitorInterface(CppCodeBuilder builder, JJTreeContext context) {
    String name = NodeFiles.visitorClass();
    List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

    builder.println("class " + name);
    builder.println("{");

    String argumentType = NodeFiles.getVisitorArgumentType();
    String returnType = NodeFiles.getVisitorReturnType();
    if (!context.treeOptions().getVisitorDataType().equals("")) {
      argumentType = context.treeOptions().getVisitorDataType();
    }
    builder.println("public:");

    builder.println("  virtual " + returnType + " visit(const Node *node, " + argumentType + " data) = 0;");
    if (context.treeOptions().getMulti()) {
      for (String n : nodeNames) {
        if (n.equals("void")) {
          continue;
        }
        String nodeType = context.treeOptions().getNodePrefix() + n;
        builder.println("  virtual " + returnType + " " + NodeFiles.getVisitMethodName(nodeType) + "(const " + nodeType
            + " *node, " + argumentType + " data) = 0;");
      }
    }

    builder.println("  virtual ~" + name + "() { }");
    builder.println("};");
  }

  private static String defaultVisitorClass() {
    return JJTreeGlobals.parserName + "DefaultVisitor";
  }

  private static void generateDefaultVisitor(CppCodeBuilder builder, JJTreeContext context) {
    String className = NodeFiles.defaultVisitorClass();
    List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

    builder.println("class " + className + " : public " + NodeFiles.visitorClass() + " {");

    String argumentType = NodeFiles.getVisitorArgumentType();
    String ret = NodeFiles.getVisitorReturnType();

    builder.println("public:");
    builder.println("  virtual " + ret + " defaultVisit(const Node *node, " + argumentType + " data) = 0;");

    builder.println("  virtual " + ret + " visit(const Node *node, " + argumentType + " data) {");
    builder.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
    builder.println("  }");

    if (context.treeOptions().getMulti()) {
      for (String n : nodeNames) {
        if (n.equals("void")) {
          continue;
        }
        String nodeType = context.treeOptions().getNodePrefix() + n;
        builder.println("  virtual " + ret + " " + NodeFiles.getVisitMethodName(nodeType) + "(const " + nodeType
            + " *node, " + argumentType + " data) {");
        builder.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
        builder.println("  }");
      }
    }
    builder.println("  ~" + className + "() { }");
    builder.println("};");
  }
}
