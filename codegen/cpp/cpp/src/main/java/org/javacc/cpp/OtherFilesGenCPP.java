// Copyright 2012 Google Inc. All Rights Reserved.
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

package org.javacc.cpp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.MetaParseException;
import org.javacc.parser.Options;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.RegExprSpec;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.TokenProduction;
import org.javacc.parser.TokenizerData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the Constants file.
 */
class OtherFilesGenCPP {
	  static void printTokenImages(CppCodeBuilder builder, Context context) {
	      builder.println("  /** Literal token image. */");
	      int cnt = 0;
	      builder.println("  static const JJChar tokenImage_" + cnt + "[] = ");
	      OtherFilesGenCPP.printCharArray(builder, "<EOF>");
	      builder.println(";");

	      for (TokenProduction tp : context.globals().rexprlist) {
	        for (RegExprSpec res : tp.respecs) {
	          RegularExpression re = res.rexp;
	          builder.println("  static const JJChar tokenImage_" + ++cnt + "[] = ");
	          if (re instanceof RStringLiteral) {
	        	String image = ((RStringLiteral) re).image;
	            OtherFilesGenCPP.printCharArray(builder, image);
	          } else if (!re.label.equals("")) {
	            OtherFilesGenCPP.printCharArray(builder, "<" + re.label + ">");
	          } else {
	            if (re.tpContext.kind == TokenProduction.TOKEN) {
	              context.errors().warning(re, "Consider giving this non-string token a label for better error reporting.");
	            }
	            OtherFilesGenCPP.printCharArray(builder, "<token of kind " + re.ordinal + ">");
	          }
	          builder.println(";");
	        }
	      }

	      builder.println("  static const JJChar* const tokenImages[] = {");
	      for (int i = 0; i <= cnt; i++) {
	        builder.println("tokenImage_" + i + ", ");
	      }
	      builder.println("  };");
	      builder.println();

	  }
	  static void printTokenLabels(CppCodeBuilder builder, Context context) {
	      builder.println("  /** Literal token label. */");
	      int cnt = 0;
	      builder.println("  static const JJChar tokenLabel_" + cnt + "[] = ");
	      OtherFilesGenCPP.printCharArray(builder, "<EOF>");
	      builder.println(";");

	      for (TokenProduction tp : context.globals().rexprlist) {
	        for (RegExprSpec res : tp.respecs) {
	          RegularExpression re = res.rexp;
	          builder.println("  static const JJChar tokenLabel_" + ++cnt + "[] = ");
	          if (re instanceof RStringLiteral) {
	        	String label = ((RStringLiteral) re).label;
	            OtherFilesGenCPP.printCharArray(builder, "<" + label + ">");
	          } else if (!re.label.equals("")) {
	            OtherFilesGenCPP.printCharArray(builder, "<" + re.label + ">");
	          } else {
	            if (re.tpContext.kind == TokenProduction.TOKEN) {
	              context.errors().warning(re, "Consider giving this non-string token a label for better error reporting.");
	            }
	            OtherFilesGenCPP.printCharArray(builder, "<token of kind " + re.ordinal + ">");
	          }
	          builder.println(";");
	        }
	      }

	      builder.println("  static const JJChar* const tokenLabels[] = {");
	      for (int i = 0; i <= cnt; i++) {
	        builder.println("tokenLabel_" + i + ", ");
	      }
	      builder.println("  };");
	      builder.println();

	  }
  static void start(Context context, TokenizerData tokenizerData) throws MetaParseException {
    if (context.errors().get_error_count() != 0) {
      throw new MetaParseException();
    }

    List<String> toolnames = new ArrayList<>(context.globals().toolNames);
    toolnames.add(JavaCCGlobals.toolName);


    try (CppCodeBuilder builder = CppCodeBuilder.ofHeader(context, CodeGeneratorSettings.create())) {
      builder.setFile(new File(Options.getOutputDirectory(), context.globals().cu_name + "Constants.h"));
      builder.addTools(toolnames.toArray(new String[toolnames.size()]));

      builder.println();
      builder.println("/**");
      builder.println(" * Token literal values and constants.");
      builder.println(" * Generated by org.javacc.cpp.OtherFilesGenCPP#start()");
      builder.println(" */");

      String guard = "JAVACC_" + context.globals().cu_name.toUpperCase() + "CONSTANTS_H";
      builder.println("#ifndef " + guard);
      builder.println("#define " + guard);
      builder.println();
      builder.println("#include \"JavaCC.h\"");
      builder.println();
      if (Options.hasNamespace()) {
        builder.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      String constPrefix = "const";
      builder.println("  /** End of File. */");
      builder.println(constPrefix + "  int _EOF = 0;");
      for (RegularExpression re : context.globals().ordered_named_tokens) {
        builder.println("  /** RegularExpression Id. */");
        builder.println(constPrefix + "  int " + re.label + " = " + re.ordinal + ";");
      }
      builder.println();

      if (!Options.getUserTokenManager() && Options.getBuildTokenManager()) {
        for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
          builder.println("  /** Lexical state. */");
          builder.println(constPrefix + "  int " + tokenizerData.lexStateNames[i] + " = " + i + ";");
        }
        builder.println();
      }
      printTokenImages(builder, context);
      printTokenLabels(builder, context);
      
      if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
        builder.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
      builder.println("#endif");
    } catch (java.io.IOException e) {
      context.errors().semantic_error("Could not open file " + context.globals().cu_name + "Constants.h for writing.");
      throw new Error();
    }
  }

  // Used by the CPP code generatror
  private static void printCharArray(CppCodeBuilder builder, String s) {
    builder.print("{");
    for (int i = 0; i < s.length(); i++) {
      builder.print("0x" + Integer.toHexString(s.charAt(i)) + ", ");
    }
    builder.print("0}");
  }
}
