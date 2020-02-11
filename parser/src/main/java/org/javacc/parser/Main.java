// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/*
 * Copyright (c) 2006, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Sun Microsystems, Inc. nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import org.javacc.utils.CodeBuilder;
import org.javacc.utils.OptionInfo;
import org.javacc.utils.OptionType;

import java.util.Set;

/**
 * Entry point.
 */
public class Main {

  protected Main() {}

  private static void help_message() {
    System.out.println("Usage:");
    System.out.println("    javacc option-settings inputfile");
    System.out.println("");
    System.out.println("\"option-settings\" is a sequence of settings separated by spaces.");
    System.out.println("Each option setting must be of one of the following forms:");
    System.out.println("");
    System.out.println("    -optionname=value (e.g., -STATIC=false)");
    System.out.println("    -optionname:value (e.g., -STATIC:false)");
    System.out.println("    -optionname       (equivalent to -optionname=true.  e.g., -STATIC)");
    System.out.println("    -NOoptionname     (equivalent to -optionname=false. e.g., -NOSTATIC)");
    System.out.println("");
    System.out.println("Option settings are not case-sensitive, so one can say \"-nOsTaTiC\" instead");
    System.out.println("of \"-NOSTATIC\".  Option values must be appropriate for the corresponding");
    System.out.println("option, and must be either an integer, a boolean, or a string value.");
    System.out.println("");

    // 2013/07/23 -- Changed this to auto-generate from metadata in Options so
    // that help is always in-sync with codebase
    Main.printOptions();

    System.out.println("EXAMPLE:");
    System.out.println("    javacc -STATIC=false -LOOKAHEAD:2 -debug_parser mygrammar.jj");
    System.out.println("");
  }

  private static void printOptions() {

    Set<OptionInfo> options = Options.getUserOptions();

    int maxLengthInt = 0;
    int maxLengthBool = 0;
    int maxLengthString = 0;

    for (OptionInfo i : options) {
      int length = i.getName().length();

      if (i.getType() == OptionType.INTEGER) {
        maxLengthInt = length > maxLengthInt ? length : maxLengthInt;
      } else if (i.getType() == OptionType.BOOLEAN) {
        maxLengthBool = length > maxLengthBool ? length : maxLengthBool;

      } else if (i.getType() == OptionType.STRING) {
        maxLengthString = length > maxLengthString ? length : maxLengthString;

      } else {
        // Not interested
      }
    }

    if (maxLengthInt > 0) {
      System.out.println("The integer valued options are:");
      System.out.println("");
      for (OptionInfo i : options) {
        Main.printOptionInfo(OptionType.INTEGER, i, maxLengthInt);
      }
      System.out.println("");
    }


    if (maxLengthBool > 0) {
      System.out.println("The boolean valued options are:");
      System.out.println("");
      for (OptionInfo i : options) {
        Main.printOptionInfo(OptionType.BOOLEAN, i, maxLengthBool);
      }
      System.out.println("");
    }

    if (maxLengthString > 0) {
      System.out.println("The string valued options are:");
      System.out.println("");
      for (OptionInfo i : options) {
        Main.printOptionInfo(OptionType.STRING, i, maxLengthString);
      }
      System.out.println("");
    }
  }

  private static void printOptionInfo(OptionType filter, OptionInfo optionInfo, int padLength) {
    if (optionInfo.getType() == filter) {
      Object default1 = optionInfo.getDefault();
      System.out.println("    " + Main.padRight(optionInfo.getName(), padLength + 1) + (default1 == null ? ""
          : ("(default : " + (default1.toString().length() == 0 ? "<<empty>>" : default1) + ")")));
    }
  }

  private static String padRight(String name, int maxLengthInt) {
    int nameLength = name.length();
    if (nameLength == maxLengthInt) {
      return name;
    } else {
      int charsToPad = maxLengthInt - nameLength;
      StringBuilder sb = new StringBuilder(charsToPad);
      sb.append(name);

      for (int i = 0; i < charsToPad; i++) {
        sb.append(" ");
      }

      return sb.toString();
    }
  }

  /**
   * A main program that exercises the parser.
   */
  public static void main(String args[]) throws Exception {
    int errorcode = Main.mainProgram(args);
    System.exit(errorcode);
  }

  /**
   * The method to call to exercise the parser from other Java programs. It
   * returns an error code. See how the main program above uses this method.
   */
  public static int mainProgram(String args[]) throws Exception {

    // Initialize all static state
    JavaCCContext context = Main.reInitAll();

    JavaCCGlobals.bannerLine("Parser Generator", "");

    JavaCCParser parser = null;
    if (args.length == 0) {
      System.out.println("");
      Main.help_message();
      return 1;
    } else {
      System.out.println("(type \"javacc\" with no arguments for help)");
    }

    if (Options.isOption(args[args.length - 1])) {
      System.out.println("Last argument \"" + args[args.length - 1] + "\" is not a filename.");
      return 1;
    }
    for (int arg = 0; arg < (args.length - 1); arg++) {
      if (!Options.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.");
        return 1;
      }
      Options.setCmdLineOption(args[arg]);
    }


    try {
      java.io.File fp = new java.io.File(args[args.length - 1]);
      if (!fp.exists()) {
        System.out.println("File " + args[args.length - 1] + " not found.");
        return 1;
      }
      if (fp.isDirectory()) {
        System.out.println(args[args.length - 1] + " is a directory. Please use a valid file name.");
        return 1;
      }
      parser = new JavaCCParser(new java.io.BufferedReader(new java.io.InputStreamReader(
          new java.io.FileInputStream(args[args.length - 1]), Options.getGrammarEncoding())));
    } catch (SecurityException se) {
      System.out.println("Security violation while trying to open " + args[args.length - 1]);
      return 1;
    } catch (java.io.FileNotFoundException e) {
      System.out.println("File " + args[args.length - 1] + " not found.");
      return 1;
    }

    try {
      System.out.println("Reading from file " + args[args.length - 1] + " . . .");
      // JavaCCGlobals.fileName = JavaCCGlobals.origFileName = args[args.length
      // - 1];
      context.globals().jjtreeGenerated = JavaCCGlobals.isGeneratedBy("JJTree", args[args.length - 1]);
      context.globals().toolNames = JavaCCGlobals.getToolNames(args[args.length - 1]);
      parser.javacc_input(context);

      JavaCCGlobals.createOutputDir(Options.getOutputDirectory(), context);


      boolean unicodeWarning = false;
      if (Options.getUnicodeInput()) {
        unicodeWarning = true;
        System.out.println("Note: UNICODE_INPUT option is specified. "
            + "Please make sure you create the parser/lexer using a Reader with the correct character encoding.");
      }

      Semanticize.start(context);
      boolean isBuildParser = Options.getBuildParser();


      CodeGenerator codeGenerator = context.globals().getCodeGenerator(context);
      if (codeGenerator != null) {
        ParserCodeGenerator parserCodeGenerator = codeGenerator.getParserCodeGenerator(context);
        if (isBuildParser && (parserCodeGenerator != null)) {
          ParserData parserData = Main.createParserData(context);
          CodeGeneratorSettings settings = CodeGeneratorSettings.of(Options.getOptions());
          parserCodeGenerator.generateCode(settings, parserData);
          parserCodeGenerator.finish(settings, parserData);
        }

        // Must always create the lexer object even if not building a parser.
        LexGen lg = new LexGen(context);
        TokenizerData tokenizerData = lg.generateTokenizerData(false, unicodeWarning);

        Options.setStringOption(Options.NONUSER_OPTION__PARSER_NAME, context.globals().cu_name);

        if (context.errors().get_error_count() != 0) {
          throw new MetaParseException();
        }
        if (Options.isGenerateBoilerplateCode()) {
          if ((!codeGenerator.getTokenCodeGenerator(context)
              .generateCodeForToken(CodeGeneratorSettings.of(Options.getOptions())))
              || (!codeGenerator.generateHelpers(context, CodeGeneratorSettings.of(Options.getOptions()), tokenizerData))) {
            context.errors().semantic_error("Could not generate the code for Token or helper classes.");
          }
        }
      }


      if ((context.errors().get_error_count() == 0) && (isBuildParser || Options.getBuildTokenManager())) {
        if (context.errors().get_warning_count() == 0) {
          if (isBuildParser) {
            System.out.println("Parser generated successfully.");
          }
        } else {
          System.out.println("Parser generated with 0 errors and " + context.errors().get_warning_count() + " warnings.");
        }
        return 0;
      } else {
        System.out.println("Detected " + context.errors().get_error_count() + " errors and "
            + context.errors().get_warning_count() + " warnings.");
        return (context.errors().get_error_count() == 0) ? 0 : 1;
      }
    } catch (MetaParseException e) {
      System.out.println("Detected " + context.errors().get_error_count() + " errors and "
          + context.errors().get_warning_count() + " warnings.");
      return 1;
    } catch (ParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + (context.errors().get_error_count() + 1) + " errors and "
          + context.errors().get_warning_count() + " warnings.");
      return 1;
    }
  }

  private static ParserData createParserData(JavaCCContext context) {
    ParserData parserData = new ParserData();
    parserData.bnfproductions = context.globals().bnfproductions;
    parserData.parserName = context.globals().cu_name;
    parserData.tokenCount = context.globals().tokenCount;
    parserData.namesOfTokens = context.globals().names_of_tokens;
    parserData.productionTable = context.globals().production_table;
    StringBuilder decls = new StringBuilder();
    if (context.globals().otherLanguageDeclTokenBeg != null) {
      // int line = context.globals().otherLanguageDeclTokenBeg.beginLine;
      for (Token t = context.globals().otherLanguageDeclTokenBeg; t != context.globals().otherLanguageDeclTokenEnd; t =
          t.next) {
        decls.append(CodeBuilder.toString(t));
      }
    }
    parserData.decls = decls.toString();
    return parserData;
  }

  public static JavaCCContext reInitAll() {
    JavaCCContext context = new JavaCCContext();
    Options.init();
    return context;
  }
}
