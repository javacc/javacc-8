
package org.javacc.cpp;

import java.io.File;

import org.javacc.jjtree.DefaultJJTreeVisitor;
import org.javacc.jjtree.JJTreeContext;
import org.javacc.parser.CodeGenerator;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.Options;
import org.javacc.parser.TokenizerData;

public class CppCodeGenerator implements CodeGenerator {

  static final boolean IS_DEBUG = true;

  /**
   * The name of the C# code generator.
   */
  @Override
  public final String getName() {
    return "C++";
  }

  /**
   * Generate any other support files you need.
   */
  @Override
  public final boolean generateHelpers(Context context, CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    try {
        try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
            builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "CharStream.h"));
            builder.addTools(JavaCCGlobals.toolName);

            builder.switchToIncludeFile();
            builder.printTemplate("/templates/cpp/CharStream.h.template");
          }

        try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
            builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "DefaultCharStream.cc"));
            builder.addTools(JavaCCGlobals.toolName);
            builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

            builder.printTemplate("/templates/cpp/DefaultCharStream.cc.template");
            builder.switchToIncludeFile();
            builder.printTemplate("/templates/cpp/DefaultCharStream.h.template");
          }

      try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "TokenManagerError.cc"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

        builder.printTemplate("/templates/cpp/TokenManagerError.cc.template");
        builder.switchToIncludeFile();
        builder.printTemplate("/templates/cpp/TokenManagerError.h.template");
      }

      try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "ParseException.cc"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

        builder.printTemplate("/templates/cpp/ParseException.cc.template");
        builder.switchToIncludeFile();
        builder.printTemplate("/templates/cpp/ParseException.h.template");
      }

      try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "TokenManager.h"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

        builder.printTemplate("/templates/cpp/TokenManager.h.template");
      }

      try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
          builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "JavaCC.h"));
          builder.addTools(JavaCCGlobals.toolName);
          builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);

          builder.printTemplate("/templates/cpp/JavaCC.h.template");
        }

      if (!Options.getLibrary().isEmpty()) {
    	  try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
    		builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "ImportExport.h"));
          	builder.addTools(JavaCCGlobals.toolName);
            builder.addOption(Options.USEROPTION__CPP_LIBRARY); 
          	builder.printTemplate("/templates/cpp/ImportExport.h.template");
    	  }
      }
      
      try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
          builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "DefaultParserErrorHandler.cc"));
          builder.addTools(JavaCCGlobals.toolName);
          builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);
          builder.addOption(
          		Options.USEROPTION__STATIC, 
          		Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC,
          		Options.USEROPTION__BUILD_PARSER,
          		Options.USEROPTION__BUILD_TOKEN_MANAGER);

          builder.printTemplate("/templates/cpp/DefaultParserErrorHandler.cc.template");
          builder.switchToIncludeFile();
          builder.printTemplate("/templates/cpp/DefaultParserErrorHandler.h.template");
        }

      try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "ParserErrorHandler.h"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(
        		Options.USEROPTION__STATIC, 
        		Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC,
        		Options.USEROPTION__BUILD_PARSER,
        		Options.USEROPTION__BUILD_TOKEN_MANAGER);

        builder.printTemplate("/templates/cpp/ParserErrorHandler.h.template");
      }

      try (CppCodeBuilder builder = CppCodeBuilder.of(context, settings)) {
          builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "DefaultTokenManagerErrorHandler.cc"));
          builder.addTools(JavaCCGlobals.toolName);
          builder.addOption(Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);
          builder.addOption(
          		Options.USEROPTION__STATIC, 
          		Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC,
          		Options.USEROPTION__BUILD_PARSER,
          		Options.USEROPTION__BUILD_TOKEN_MANAGER);

          builder.printTemplate("/templates/cpp/DefaultTokenManagerErrorHandler.cc.template");
          builder.switchToIncludeFile();
          builder.printTemplate("/templates/cpp/DefaultTokenManagerErrorHandler.h.template");
        }

      try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, settings)) {
        builder.setFile(new File((String) settings.get("OUTPUT_DIRECTORY"), "TokenManagerErrorHandler.h"));
        builder.addTools(JavaCCGlobals.toolName);
        builder.addOption(
        		Options.USEROPTION__STATIC, 
        		Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC,
        		Options.USEROPTION__BUILD_PARSER,
        		Options.USEROPTION__BUILD_TOKEN_MANAGER);

        builder.printTemplate("/templates/cpp/TokenManagerErrorHandler.h.template");
      }

      OtherFilesGenCPP.start(context, tokenizerData);
    } catch (Exception e) {
    	e.printStackTrace();
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
