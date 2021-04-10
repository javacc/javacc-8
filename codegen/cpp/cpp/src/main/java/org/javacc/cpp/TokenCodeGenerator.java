
package org.javacc.cpp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.Options;

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
  /*
  @Override
  public boolean generateCodeForToken(CodeGeneratorSettings settings) {
   try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
 
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "Token.h"));
      builder.addTools(JavaCCGlobals.toolName);
      builder.addOption(
    		  Options.USEROPTION__STATIC, 
    		  Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

      builder.switchToIncludeFile();
      builder.printTemplate("/templates/cpp/Token.h.template");
    } catch (IOException e) {
      return false;
    }
	  return true;
  }
*/

@Override
public boolean generateCodeForToken(CodeGeneratorSettings settings) {
//	if (Options.getUserTokenManager()) {
//		return true;
//	}
    try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "Token.cc"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(
      		  Options.USEROPTION__STATIC, 
      		  Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

        builder.printTemplate("/templates/cpp/Token.cc.template");
        builder.switchToIncludeFile();
        builder.printTemplate("/templates/cpp/Token.h.template");
      } catch (IOException e) {
        return false;
      }
      return true;
    }
}

