
package org.javacc.csharp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.utils.CodeBuilder.GenericCodeBuilder;

import java.io.File;
import java.io.IOException;

class TokenCodeGenerator implements org.javacc.parser.TokenCodeGenerator {

  private final Context context;

  TokenCodeGenerator(Context context) {
    this.context = context;
  }

  /**
   * The Token class generator.
   */
  @Override
  public boolean generateCodeForToken(CodeGeneratorSettings settings) {
    try (GenericCodeBuilder builder = GenericCodeBuilder.of(context, settings)) {
      builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "Token.cs"));
      builder.addTools(JavaCCGlobals.toolName).printTemplate("/templates/csharp/Token.template");
    } catch (IOException e) {
      return false;
    }
    return true;
  }

}
