
package org.javacc.java;

import org.javacc.jjtree.DefaultJJTreeVisitor;
import org.javacc.jjtree.JJTreeContext;
import org.javacc.parser.CodeGenerator;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.Options;
import org.javacc.parser.TokenizerData;

public class JavaCodeGenerator implements CodeGenerator {

  /**
   * The name of the Java code generator.
   */
  @Override
  public final String getName() {
    return "Java";
  }

  /**
   * Generate any other support files you need.
   */
  @Override
  public boolean generateHelpers(Context context, CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    JavaTemplates templates = JavaTemplates.getTemplates();

    try {
      JavaHelperFiles.generateSimple("/templates/TokenMgrError.template",
          JavaTemplates.getTokenMgrErrorClass() + ".java", settings, context);
      JavaHelperFiles.generateSimple(templates.getParseExceptionTemplateResourceUrl(), "ParseException.java", settings,
          context);

      JavaHelperFiles.gen_Constants(context, tokenizerData);

      if (Options.isGenerateBoilerplateCode()) {
        JavaHelperFiles.gen_Token(context);
        if (Options.getUserTokenManager()) {
          // CBA -- I think that Token managers are unique so will always be
          // generated
          JavaHelperFiles.gen_TokenManager(context);
        }

        if (Options.getUserCharStream()) {
          JavaHelperFiles.generateSimple("/templates/CharStream.template", "CharStream.java", settings, context);
        } else if (Options.getJavaUnicodeEscape()) {
          JavaHelperFiles.generateSimple(templates.getJavaCharStreamTemplateResourceUrl(), "JavaCharStream.java",
              settings, context);
        } else {
          JavaHelperFiles.generateSimple(templates.getSimpleCharStreamTemplateResourceUrl(), "SimpleCharStream.java",
              settings, context);
        }

        if (JavaTemplates.isJavaModern()) {
          JavaHelperFiles.genMiscFile("Provider.java", "/templates/gwt/Provider.template", context);
          JavaHelperFiles.genMiscFile("StringProvider.java", "/templates/gwt/StringProvider.template", context);
          // This provides a bridge to standard Java readers.
          JavaHelperFiles.genMiscFile("StreamProvider.java", "/templates/gwt/StreamProvider.template", context);
        }
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * The Token class generator.
   */
  @Override
  public final TokenCodeGenerator getTokenCodeGenerator(Context context) {
    return new TokenCodeGenerator(context);
  }

  /**
   * The TokenManager class generator.
   */
  @Override
  public final TokenManagerCodeGenerator getTokenManagerCodeGenerator(Context context) {
    return new TokenManagerCodeGenerator(context);
  }

  /**
   * The Parser class generator.
   */
  @Override
  public final ParserCodeGenerator getParserCodeGenerator(Context context) {
    return new ParserCodeGenerator(context);
  }

  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code
   * generator. The JJTree preprocesor.
   */
  @Override
  public final DefaultJJTreeVisitor getJJTreeCodeGenerator(JJTreeContext context) {
    return new JJTreeCodeGenerator(context);
  }
}
