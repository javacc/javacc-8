
package org.javacc.parser;

import org.javacc.jjtree.JJTreeContext;

public interface CodeGenerator {

  /**
   * Get the name of the code generator.
   */
  String getName();

  /**
   * Generate any other support files you need.
   */
  boolean generateHelpers(Context context, CodeGeneratorSettings settings, TokenizerData tokenizerData);

  /**
   * The Token class generator.
   */
  TokenCodeGenerator getTokenCodeGenerator(Context context);

  /**
   * The TokenManager class generator.
   */
  TokenManagerCodeGenerator getTokenManagerCodeGenerator(Context context);

  /**
   * The Parser class generator.
   */
  ParserCodeGenerator getParserCodeGenerator(Context context);

  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code
   * generator. The JJTree preprocesor.
   */
  org.javacc.jjtree.DefaultJJTreeVisitor getJJTreeCodeGenerator(JJTreeContext context);
}
