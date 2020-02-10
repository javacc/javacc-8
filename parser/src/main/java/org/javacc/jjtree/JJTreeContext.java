
package org.javacc.jjtree;

import org.javacc.parser.JavaCCContext;
import org.javacc.parser.Options;


public class JJTreeContext extends JavaCCContext {

  private final JJTreeOptions treeOptions = new JJTreeOptions();

  public JJTreeContext() {
    Options.optionValues.put("MULTI", Boolean.FALSE);
    Options.optionValues.put("NODE_DEFAULT_VOID", Boolean.FALSE);
    Options.optionValues.put("NODE_SCOPE_HOOK", Boolean.FALSE);
    Options.optionValues.put("NODE_USES_PARSER", Boolean.FALSE);
    Options.optionValues.put("BUILD_NODE_FILES", Boolean.TRUE);
    Options.optionValues.put("VISITOR", Boolean.FALSE);
    Options.optionValues.put("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME", Boolean.FALSE);
    Options.optionValues.put("TRACK_TOKENS", Boolean.FALSE);

    Options.optionValues.put("NODE_PREFIX", "AST");
    Options.optionValues.put("NODE_PACKAGE", "");
    Options.optionValues.put("NODE_EXTENDS", "");
    Options.optionValues.put("NODE_CLASS", "");
    Options.optionValues.put("NODE_FACTORY", "");
    Options.optionValues.put("NODE_INCLUDES", "");
    Options.optionValues.put("OUTPUT_FILE", "");
    Options.optionValues.put("VISITOR_DATA_TYPE", "");
    Options.optionValues.put("VISITOR_RETURN_TYPE", "Object");
    Options.optionValues.put("VISITOR_EXCEPTION", "");

    Options.optionValues.put("NODE_DIRECTORY", "");
    Options.optionValues.put("JJTREE_OUTPUT_DIRECTORY", "");


    // TODO :: 2013/07/23 -- This appears to be a duplicate from the parent
    // class
    Options.optionValues.put(Options.USEROPTION__JDK_VERSION, "1.5");

    // Also appears to be a duplicate
    Options.optionValues.put(Options.USEROPTION__NAMESPACE, "");

    // Also appears to be a duplicate
    Options.optionValues.put(Options.USEROPTION__IGNORE_ACTIONS, Boolean.FALSE);
  }

  public final JJTreeOptions treeOptions() {
    return treeOptions;
  }

  /**
   * Check options for consistency
   */
  final void validate() {
    if (!treeOptions().getVisitor()) {
      if (treeOptions().getVisitorDataType().length() > 0) {
        errors().warning("VISITOR_DATA_TYPE option will be ignored since VISITOR is false");
      }
      if ((treeOptions().getVisitorReturnType().length() > 0)
          && !treeOptions().getVisitorReturnType().equals("Object")) {
        errors().warning("VISITOR_RETURN_TYPE option will be ignored since VISITOR is false");
      }
      if (treeOptions().getVisitorException().length() > 0) {
        errors().warning("VISITOR_EXCEPTION option will be ignored since VISITOR is false");
      }
    }
  }
}
