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

package org.javacc.java;


import org.javacc.parser.Action;
import org.javacc.parser.BNFProduction;
import org.javacc.parser.Choice;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.CodeProduction;
import org.javacc.parser.Context;
import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.Lookahead;
import org.javacc.parser.MetaParseException;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.OneOrMore;
import org.javacc.parser.Options;
import org.javacc.parser.ParserData;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Semanticize;
import org.javacc.parser.Sequence;
import org.javacc.parser.Token;
import org.javacc.parser.TryBlock;
import org.javacc.parser.ZeroOrMore;
import org.javacc.parser.ZeroOrOne;
import org.javacc.utils.CodeBuilder;
import org.javacc.utils.CodeBuilder.GenericCodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generate the parser.
 */
class ParserCodeGenerator implements org.javacc.parser.ParserCodeGenerator {

  /**
   * These lists are used to maintain expansions for which code generation in
   * phase 2 and phase 3 is required. Whenever a call is generated to a phase 2
   * or phase 3 routine, a corresponding entry is added here if it has not
   * already been added. The phase 3 routines have been optimized in version
   * 0.7pre2. Essentially only those methods (and only those portions of these
   * methods) are generated that are required. The lookahead amount is used to
   * determine this. This change requires the use of a hash table because it is
   * now possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads. The hash table provides a easily searchable
   * capability to determine the previous requests. The phase 3 routines
   * nExpressionTreeConstantsow are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads). The second step then generates these
   * methods. This optimization and the hashtable makes it look like we do not
   * need the flag "phase3done" any more. But this has not been removed yet.
   */

  private GenericCodeBuilder            codeGenerator;

  private final Context           context;
  private final Map<Expansion, String>  internalNames   = new HashMap<>();
  private final Map<Expansion, Integer> internalIndexes = new HashMap<>();

  ParserCodeGenerator(Context context) {
    this.context = context;
  }

  @Override
  public void generateCode(CodeGeneratorSettings settings, ParserData parserData) {
    codeGenerator = GenericCodeBuilder.of(context, settings);
    codeGenerator.setFile(new File(Options.getOutputDirectory(), context.globals().cu_name + ".java"));

    context.globals().lookaheadNeeded = false;
    boolean isJavaModernMode = Options.getJavaTemplateType().equals(Options.JAVA_TEMPLATE_TYPE_MODERN);

    Token t = null;

    if (context.errors().get_error_count() != 0) {
      throw new RuntimeException(new MetaParseException());
    }

    if (Options.getBuildParser()) {
      final List<String> tn = new ArrayList<>(context.globals().toolNames);
      tn.add(JavaCCGlobals.toolName);

      boolean implementsExists = false;

      if (context.globals().cu_to_insertion_point_1.size() != 0) {
        Object firstToken = context.globals().cu_to_insertion_point_1.get(0);
        codeGenerator.printTokenSetup((Token) firstToken);
        for (final Iterator<Token> it = context.globals().cu_to_insertion_point_1.iterator(); it.hasNext();) {
          t = it.next();
          if (t.kind == JavaCCParserConstants.IMPLEMENTS) {
            implementsExists = true;
          } else if (t.kind == JavaCCParserConstants.CLASS) {
            implementsExists = false;
          }
          codeGenerator.printToken(t);
        }
      }

      // copy other stuff
      Token t1 = context.globals().otherLanguageDeclTokenBeg;
      Token t2 = context.globals().otherLanguageDeclTokenEnd;

      if (t1 != null) {
        while (t1.kind != JavaCCParserConstants.LBRACE) {
          codeGenerator.printToken(t1);

          if (t1.kind == JavaCCParserConstants.IMPLEMENTS) {
            implementsExists = true;
          } else if (t1.kind == JavaCCParserConstants.CLASS) {
            implementsExists = false;
          }
          t1 = t1.next;
        }
      }

      if (implementsExists) {
        codeGenerator.print(", ");
      } else {
        codeGenerator.print(" implements ");
      }
      codeGenerator.print(context.globals().cu_name + "Constants ");

      if (t1 != null) {
        while (t1.next != t2) {
          codeGenerator.printToken(t1);
          t1 = t1.next;
        }
      }

      if (context.globals().cu_to_insertion_point_2.size() != 0) {
        codeGenerator.printTokenSetup(context.globals().cu_to_insertion_point_2.get(0));
        for (Token token : context.globals().cu_to_insertion_point_2) {
          codeGenerator.printToken(token);
        }
      }

      codeGenerator.println();
      codeGenerator.println();

      build(codeGenerator);

      if (Options.getStatic()) {
        codeGenerator.println("  static private " + JavaUtil.getBooleanType() + " jj_initialized_once = false;");
      }
      if (Options.getUserTokenManager()) {
        codeGenerator.println("  /** User defined Token Manager. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "public TokenManager token_source;");
      } else {
        codeGenerator.println("  /** Generated Token Manager. */");
        codeGenerator.println(
            "  " + JavaUtil.getStatic() + "public " + context.globals().cu_name + "TokenManager token_source;");
        if (!Options.getUserCharStream()) {
          if (Options.getJavaUnicodeEscape()) {
            codeGenerator.println("  " + JavaUtil.getStatic() + "JavaCharStream jj_input_stream;");
          } else {
            codeGenerator.println("  " + JavaUtil.getStatic() + "SimpleCharStream jj_input_stream;");
          }
        }
      }
      codeGenerator.println("  /** Current token. */");
      codeGenerator.println("  " + JavaUtil.getStatic() + "public Token token;");
      codeGenerator.println("  /** Next token. */");
      codeGenerator.println("  " + JavaUtil.getStatic() + "public Token jj_nt;");
      if (!Options.getCacheTokens()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_ntk;");
      }
      if (Options.getDepthLimit() > 0) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_depth;");
      }
      if (context.globals().jj2index != 0) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private Token jj_scanpos, jj_lastpos;");
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_la;");
        if (context.globals().lookaheadNeeded) {
          codeGenerator.println("  /** Whether we are looking ahead. */");
          codeGenerator.println(
              "  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_lookingAhead = false;");
          codeGenerator
          .println("  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_semLA;");
        }
      }
      if (Options.getErrorReporting()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_gen;");
        codeGenerator.println("  " + JavaUtil.getStatic() + "final private int[] jj_la1 = new int["
            + context.globals().maskindex + "];");
        final int tokenMaskSize = ((context.globals().tokenCount - 1) / 32) + 1;
        for (int i = 0; i < tokenMaskSize; i++) {
          codeGenerator.println("  static private int[] jj_la1_" + i + ";");
        }
        codeGenerator.println("  static {");
        for (int i = 0; i < tokenMaskSize; i++) {
          codeGenerator.println("    jj_la1_init_" + i + "();");
        }
        codeGenerator.println(" }");
        for (int i = 0; i < tokenMaskSize; i++) {
          codeGenerator.println(" private static void jj_la1_init_" + i + "() {");
          codeGenerator.print("    jj_la1_" + i + " = new int[] {");
          for (int[] tokenMask : context.globals().maskVals) {
            codeGenerator.print("0x" + Integer.toHexString(tokenMask[i]) + ",");
          }
          codeGenerator.println("};");
          codeGenerator.println(" }");
        }
      }
      if ((context.globals().jj2index != 0) && Options.getErrorReporting()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "final private JJCalls[] jj_2_rtns = new JJCalls["
            + context.globals().jj2index + "];");
        codeGenerator
        .println("  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_rescan = false;");
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_gc = 0;");
      }
      codeGenerator.println("");

      if (Options.getDebugParser()) {
        codeGenerator.println("  {");
        codeGenerator.println("      enable_tracing();");
        codeGenerator.println("  }");
      }

      if (!Options.getUserTokenManager()) {
        if (Options.getUserCharStream()) {
          codeGenerator.println("  /** Constructor with user supplied CharStream. */");
          codeGenerator.println("  public " + context.globals().cu_name + "(CharStream stream) {");
          if (Options.getStatic()) {
            codeGenerator.println("  if (jj_initialized_once) {");
            codeGenerator.println("    System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
            codeGenerator.println("    System.out.println(\"     You must either use ReInit() "
                + "or set the JavaCC option STATIC to false\");");
            codeGenerator.println("    System.out.println(\"     during parser generation.\");");
            codeGenerator.println(
                "    throw new " + (Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException") + "();");
            codeGenerator.println("  }");
            codeGenerator.println("  jj_initialized_once = true;");
          }
          if (Options.getTokenManagerUsesParser()) {
            codeGenerator.println("  token_source = new " + context.globals().cu_name + "TokenManager(this, stream);");
          } else {
            codeGenerator.println("  token_source = new " + context.globals().cu_name + "TokenManager(stream);");
          }
          codeGenerator.println("  token = new Token();");
          if (Options.getCacheTokens()) {
            codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
          } else {
            codeGenerator.println("  jj_ntk = -1;");
          }
          if (Options.getDepthLimit() > 0) {
            codeGenerator.println("    jj_depth = -1;");
          }
          if (Options.getErrorReporting()) {
            codeGenerator.println("  jj_gen = 0;");
            if (context.globals().maskindex > 0) {
              codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
            }
            if (context.globals().jj2index != 0) {
              codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          codeGenerator.println("  }");
          codeGenerator.println("");
          codeGenerator.println("  /** Reinitialise. */");
          codeGenerator.println("  " + JavaUtil.getStatic() + "public void ReInit(CharStream stream) {");

          if (Options.isTokenManagerRequiresParserAccess()) {
            codeGenerator.println("  token_source.ReInit(this,stream);");
          } else {
            codeGenerator.println("  token_source.ReInit(stream);");
          }


          codeGenerator.println("  token = new Token();");
          if (Options.getCacheTokens()) {
            codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
          } else {
            codeGenerator.println("  jj_ntk = -1;");
          }
          if (Options.getDepthLimit() > 0) {
            codeGenerator.println("    jj_depth = -1;");
          }
          if (context.globals().lookaheadNeeded) {
            codeGenerator.println("  jj_lookingAhead = false;");
          }
          if (context.globals().jjtreeGenerated) {
            codeGenerator.println("  jjtree.reset();");
          }
          if (Options.getErrorReporting()) {
            codeGenerator.println("  jj_gen = 0;");
            if (context.globals().maskindex > 0) {
              codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
            }
            if (context.globals().jj2index != 0) {
              codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          codeGenerator.println("  }");
        } else {

          if (!isJavaModernMode) {
            codeGenerator.println("  /** Constructor with InputStream. */");
            codeGenerator.println("  public " + context.globals().cu_name + "(java.io.InputStream stream) {");
            codeGenerator.println("   this(stream, null);");
            codeGenerator.println("  }");
            codeGenerator.println("  /** Constructor with InputStream and supplied encoding */");
            codeGenerator
            .println("  public " + context.globals().cu_name + "(java.io.InputStream stream, String encoding) {");
            if (Options.getStatic()) {
              codeGenerator.println("  if (jj_initialized_once) {");
              codeGenerator
              .println("    System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
              codeGenerator.println("    System.out.println(\"     You must either use ReInit() or "
                  + "set the JavaCC option STATIC to false\");");
              codeGenerator.println("    System.out.println(\"     during parser generation.\");");
              codeGenerator.println(
                  "    throw new " + (Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException") + "();");
              codeGenerator.println("  }");
              codeGenerator.println("  jj_initialized_once = true;");
            }

            if (Options.getJavaUnicodeEscape()) {
              if (!Options.getGenerateChainedException()) {
                codeGenerator.println("  try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } "
                    + "catch(java.io.UnsupportedEncodingException e) {"
                    + " throw new RuntimeException(e.getMessage()); }");
              } else {
                codeGenerator.println("  try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } "
                    + "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
              }
            } else {
              if (!Options.getGenerateChainedException()) {
                codeGenerator.println("  try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } "
                    + "catch(java.io.UnsupportedEncodingException e) { "
                    + "throw new RuntimeException(e.getMessage()); }");
              } else {
                codeGenerator.println("  try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } "
                    + "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
              }
            }
            if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
              codeGenerator
              .println("  token_source = new " + context.globals().cu_name + "TokenManager(this, jj_input_stream);");
            } else {
              codeGenerator.println("  token_source = new " + context.globals().cu_name + "TokenManager(jj_input_stream);");
            }
            codeGenerator.println("  token = new Token();");
            if (Options.getCacheTokens()) {
              codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
            } else {
              codeGenerator.println("  jj_ntk = -1;");
            }
            if (Options.getDepthLimit() > 0) {
              codeGenerator.println("    jj_depth = -1;");
            }
            if (Options.getErrorReporting()) {
              codeGenerator.println("  jj_gen = 0;");
              if (context.globals().maskindex > 0) {
                codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
              }
              if (context.globals().jj2index != 0) {
                codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
              }
            }
            codeGenerator.println("  }");
            codeGenerator.println("");

            codeGenerator.println("  /** Reinitialise. */");
            codeGenerator
            .println("  " + JavaUtil.getStatic() + "public void ReInit(java.io.InputStream stream) {");
            codeGenerator.println("   ReInit(stream, null);");
            codeGenerator.println("  }");

            codeGenerator.println("  /** Reinitialise. */");
            codeGenerator.println(
                "  " + JavaUtil.getStatic() + "public void ReInit(java.io.InputStream stream, String encoding) {");
            if (!Options.getGenerateChainedException()) {
              codeGenerator.println("  try { jj_input_stream.ReInit(stream, encoding, 1, 1); } "
                  + "catch(java.io.UnsupportedEncodingException e) { "
                  + "throw new RuntimeException(e.getMessage()); }");
            } else {
              codeGenerator.println("  try { jj_input_stream.ReInit(stream, encoding, 1, 1); } "
                  + "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
            }

            if (Options.isTokenManagerRequiresParserAccess()) {
              codeGenerator.println("  token_source.ReInit(this,jj_input_stream);");
            } else {
              codeGenerator.println("  token_source.ReInit(jj_input_stream);");
            }

            codeGenerator.println("  token = new Token();");
            if (Options.getCacheTokens()) {
              codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
            } else {
              codeGenerator.println("  jj_ntk = -1;");
            }
            if (Options.getDepthLimit() > 0) {
              codeGenerator.println("    jj_depth = -1;");
            }
            if (context.globals().jjtreeGenerated) {
              codeGenerator.println("  jjtree.reset();");
            }
            if (Options.getErrorReporting()) {
              codeGenerator.println("  jj_gen = 0;");
              codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
              if (context.globals().jj2index != 0) {
                codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
              }
            }
            codeGenerator.println("  }");
            codeGenerator.println("");

          }

          final String readerInterfaceName = isJavaModernMode ? "Provider" : "java.io.Reader";
          final String stringReaderClass = isJavaModernMode ? "StringProvider" : "java.io.StringReader";


          codeGenerator.println("  /** Constructor. */");
          codeGenerator.println("  public " + context.globals().cu_name + "(" + readerInterfaceName + " stream) {");
          if (Options.getStatic()) {
            codeGenerator.println("  if (jj_initialized_once) {");
            codeGenerator.println("    System.out.println(\"ERROR: Second call to constructor of static parser. \");");
            codeGenerator.println("    System.out.println(\"     You must either use ReInit() or "
                + "set the JavaCC option STATIC to false\");");
            codeGenerator.println("    System.out.println(\"     during parser generation.\");");
            codeGenerator.println(
                "    throw new " + (Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException") + "();");
            codeGenerator.println("  }");
            codeGenerator.println("  jj_initialized_once = true;");
          }
          if (Options.getJavaUnicodeEscape()) {
            codeGenerator.println("  jj_input_stream = new JavaCharStream(stream, 1, 1);");
          } else {
            codeGenerator.println("  jj_input_stream = new SimpleCharStream(stream, 1, 1);");
          }
          if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
            codeGenerator
            .println("  token_source = new " + context.globals().cu_name + "TokenManager(this, jj_input_stream);");
          } else {
            codeGenerator.println("  token_source = new " + context.globals().cu_name + "TokenManager(jj_input_stream);");
          }
          codeGenerator.println("  token = new Token();");
          if (Options.getCacheTokens()) {
            codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
          } else {
            codeGenerator.println("  jj_ntk = -1;");
          }
          if (Options.getDepthLimit() > 0) {
            codeGenerator.println("    jj_depth = -1;");
          }
          if (Options.getErrorReporting()) {
            codeGenerator.println("  jj_gen = 0;");
            if (context.globals().maskindex > 0) {
              codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
            }
            if (context.globals().jj2index != 0) {
              codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          codeGenerator.println("  }");
          codeGenerator.println("");

          // Add-in a string based constructor because its convenient (modern
          // only to prevent regressions)
          if (isJavaModernMode) {
            codeGenerator.println("  /** Constructor. */");
            codeGenerator.println("  public " + context.globals().cu_name + "(String dsl) throws ParseException, "
                + JavaTemplates.getTokenMgrErrorClass() + " {");
            codeGenerator.println("    this(new " + stringReaderClass + "(dsl));");
            codeGenerator.println("  }");
            codeGenerator.println("");

            codeGenerator.println("  public void ReInit(String s) {");
            codeGenerator.println("   ReInit(new " + stringReaderClass + "(s));");
            codeGenerator.println("  }");

          }


          codeGenerator.println("  /** Reinitialise. */");
          codeGenerator
          .println("  " + JavaUtil.getStatic() + "public void ReInit(" + readerInterfaceName + " stream) {");
          if (Options.getJavaUnicodeEscape()) {
            codeGenerator.println(" if (jj_input_stream == null) {");
            codeGenerator.println("    jj_input_stream = new JavaCharStream(stream, 1, 1);");
            codeGenerator.println(" } else {");
            codeGenerator.println("    jj_input_stream.ReInit(stream, 1, 1);");
            codeGenerator.println(" }");
          } else {
            codeGenerator.println(" if (jj_input_stream == null) {");
            codeGenerator.println("    jj_input_stream = new SimpleCharStream(stream, 1, 1);");
            codeGenerator.println(" } else {");
            codeGenerator.println("    jj_input_stream.ReInit(stream, 1, 1);");
            codeGenerator.println(" }");
          }

          codeGenerator.println(" if (token_source == null) {");

          if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
            codeGenerator
            .println(" token_source = new " + context.globals().cu_name + "TokenManager(this, jj_input_stream);");
          } else {
            codeGenerator.println(" token_source = new " + context.globals().cu_name + "TokenManager(jj_input_stream);");
          }

          codeGenerator.println(" }");
          codeGenerator.println("");

          if (Options.isTokenManagerRequiresParserAccess()) {
            codeGenerator.println("  token_source.ReInit(this,jj_input_stream);");
          } else {
            codeGenerator.println("  token_source.ReInit(jj_input_stream);");
          }

          codeGenerator.println("  token = new Token();");
          if (Options.getCacheTokens()) {
            codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
          } else {
            codeGenerator.println("  jj_ntk = -1;");
          }
          if (Options.getDepthLimit() > 0) {
            codeGenerator.println("    jj_depth = -1;");
          }
          if (context.globals().jjtreeGenerated) {
            codeGenerator.println("  jjtree.reset();");
          }
          if (Options.getErrorReporting()) {
            codeGenerator.println("  jj_gen = 0;");
            if (context.globals().maskindex > 0) {
              codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
            }
            if (context.globals().jj2index != 0) {
              codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          codeGenerator.println("  }");

        }
      }
      codeGenerator.println("");
      if (Options.getUserTokenManager()) {
        codeGenerator.println("  /** Constructor with user supplied Token Manager. */");
        codeGenerator.println("  public " + context.globals().cu_name + "(TokenManager tm) {");
      } else {
        codeGenerator.println("  /** Constructor with generated Token Manager. */");
        codeGenerator.println("  public " + context.globals().cu_name + "(" + context.globals().cu_name + "TokenManager tm) {");
      }
      if (Options.getStatic()) {
        codeGenerator.println("  if (jj_initialized_once) {");
        codeGenerator.println("    System.out.println(\"ERROR: Second call to constructor of static parser. \");");
        codeGenerator.println("    System.out.println(\"     You must either use ReInit() or "
            + "set the JavaCC option STATIC to false\");");
        codeGenerator.println("    System.out.println(\"     during parser generation.\");");
        codeGenerator
        .println("    throw new " + (Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException") + "();");
        codeGenerator.println("  }");
        codeGenerator.println("  jj_initialized_once = true;");
      }
      codeGenerator.println("  token_source = tm;");
      codeGenerator.println("  token = new Token();");
      if (Options.getCacheTokens()) {
        codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
      } else {
        codeGenerator.println("  jj_ntk = -1;");
      }
      if (Options.getDepthLimit() > 0) {
        codeGenerator.println("    jj_depth = -1;");
      }
      if (Options.getErrorReporting()) {
        codeGenerator.println("  jj_gen = 0;");
        if (context.globals().maskindex > 0) {
          codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
        }
        if (context.globals().jj2index != 0) {
          codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      codeGenerator.println("  }");
      codeGenerator.println("");
      if (Options.getUserTokenManager()) {
        codeGenerator.println("  /** Reinitialise. */");
        codeGenerator.println("  public void ReInit(TokenManager tm) {");
      } else {
        codeGenerator.println("  /** Reinitialise. */");
        codeGenerator.println("  public void ReInit(" + context.globals().cu_name + "TokenManager tm) {");
      }
      codeGenerator.println("  token_source = tm;");
      codeGenerator.println("  token = new Token();");
      if (Options.getCacheTokens()) {
        codeGenerator.println("  token.next = jj_nt = token_source.getNextToken();");
      } else {
        codeGenerator.println("  jj_ntk = -1;");
      }
      if (Options.getDepthLimit() > 0) {
        codeGenerator.println("    jj_depth = -1;");
      }
      if (context.globals().jjtreeGenerated) {
        codeGenerator.println("  jjtree.reset();");
      }
      if (Options.getErrorReporting()) {
        codeGenerator.println("  jj_gen = 0;");
        if (context.globals().maskindex > 0) {
          codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
        }
        if (context.globals().jj2index != 0) {
          codeGenerator.println("  for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      codeGenerator.println("  }");
      codeGenerator.println("");
      codeGenerator.println(
          "  " + JavaUtil.getStatic() + "private Token jj_consume_token(int kind) throws ParseException {");
      if (Options.getCacheTokens()) {
        codeGenerator.println("  Token oldToken = token;");
        codeGenerator.println("  if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
        codeGenerator.println("  else jj_nt = jj_nt.next = token_source.getNextToken();");
      } else {
        codeGenerator.println("  Token oldToken;");
        codeGenerator.println("  if ((oldToken = token).next != null) token = token.next;");
        codeGenerator.println("  else token = token.next = token_source.getNextToken();");
        codeGenerator.println("  jj_ntk = -1;");
      }
      codeGenerator.println("  if (token.kind == kind) {");
      if (Options.getErrorReporting()) {
        codeGenerator.println("    jj_gen++;");
        if (context.globals().jj2index != 0) {
          codeGenerator.println("    if (++jj_gc > 100) {");
          codeGenerator.println("    jj_gc = 0;");
          codeGenerator.println("    for (int i = 0; i < jj_2_rtns.length; i++) {");
          codeGenerator.println("      JJCalls c = jj_2_rtns[i];");
          codeGenerator.println("      while (c != null) {");
          codeGenerator.println("      if (c.gen < jj_gen) c.first = null;");
          codeGenerator.println("      c = c.next;");
          codeGenerator.println("      }");
          codeGenerator.println("    }");
          codeGenerator.println("    }");
        }
      }
      if (Options.getDebugParser()) {
        codeGenerator.println("    trace_token(token, \"\");");
      }
      codeGenerator.println("    return token;");
      codeGenerator.println("  }");
      if (Options.getCacheTokens()) {
        codeGenerator.println("  jj_nt = token;");
      }
      codeGenerator.println("  token = oldToken;");
      if (Options.getErrorReporting()) {
        codeGenerator.println("  jj_kind = kind;");
      }
      codeGenerator.println("  throw generateParseException();");
      codeGenerator.println("  }");
      codeGenerator.println("");
      if (context.globals().jj2index != 0) {
        codeGenerator.println("  @SuppressWarnings(\"serial\")");
        codeGenerator.println("  static private final class LookaheadSuccess extends "
            + (Options.isLegacyExceptionHandling() ? "java.lang.Error" : "java.lang.RuntimeException") + " {");
        codeGenerator.println("    @Override");
        codeGenerator.println("    public Throwable fillInStackTrace() {");
        codeGenerator.println("      return this;");
        codeGenerator.println("    }");
        codeGenerator.println("  }");
        codeGenerator.println("  static private final LookaheadSuccess jj_ls = new LookaheadSuccess();");
        codeGenerator.println(
            "  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_scan_token(int kind) {");
        codeGenerator.println("  if (jj_scanpos == jj_lastpos) {");
        codeGenerator.println("    jj_la--;");
        codeGenerator.println("    if (jj_scanpos.next == null) {");
        codeGenerator.println("    jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();");
        codeGenerator.println("    } else {");
        codeGenerator.println("    jj_lastpos = jj_scanpos = jj_scanpos.next;");
        codeGenerator.println("    }");
        codeGenerator.println("  } else {");
        codeGenerator.println("    jj_scanpos = jj_scanpos.next;");
        codeGenerator.println("  }");
        if (Options.getErrorReporting()) {
          codeGenerator.println("  if (jj_rescan) {");
          codeGenerator.println("    int i = 0; Token tok = token;");
          codeGenerator.println("    while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }");
          codeGenerator.println("    if (tok != null) jj_add_error_token(kind, i);");
          if (Options.getDebugLookahead()) {
            codeGenerator.println("  } else {");
            codeGenerator.println("    trace_scan(jj_scanpos, kind);");
          }
          codeGenerator.println("  }");
        } else if (Options.getDebugLookahead()) {
          codeGenerator.println("  trace_scan(jj_scanpos, kind);");
        }
        codeGenerator.println("  if (jj_scanpos.kind != kind) return true;");
        codeGenerator.println("  if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
        codeGenerator.println("  return false;");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }
      codeGenerator.println("");
      codeGenerator.println("/** Get the next Token. */");
      codeGenerator.println("  " + JavaUtil.getStatic() + "final public Token getNextToken() {");
      if (Options.getCacheTokens()) {
        codeGenerator.println("  if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
        codeGenerator.println("  else jj_nt = jj_nt.next = token_source.getNextToken();");
      } else {
        codeGenerator.println("  if (token.next != null) token = token.next;");
        codeGenerator.println("  else token = token.next = token_source.getNextToken();");
        codeGenerator.println("  jj_ntk = -1;");
      }
      if (Options.getErrorReporting()) {
        codeGenerator.println("  jj_gen++;");
      }
      if (Options.getDebugParser()) {
        codeGenerator.println("    trace_token(token, \" (in getNextToken)\");");
      }
      codeGenerator.println("  return token;");
      codeGenerator.println("  }");
      codeGenerator.println("");
      codeGenerator.println("/** Get the specific Token. */");
      codeGenerator.println("  " + JavaUtil.getStatic() + "final public Token getToken(int index) {");
      if (context.globals().lookaheadNeeded) {
        codeGenerator.println("  Token t = jj_lookingAhead ? jj_scanpos : token;");
      } else {
        codeGenerator.println("  Token t = token;");
      }
      codeGenerator.println("  for (int i = 0; i < index; i++) {");
      codeGenerator.println("    if (t.next != null) t = t.next;");
      codeGenerator.println("    else t = t.next = token_source.getNextToken();");
      codeGenerator.println("  }");
      codeGenerator.println("  return t;");
      codeGenerator.println("  }");
      codeGenerator.println("");
      if (!Options.getCacheTokens()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_ntk_f() {");
        codeGenerator.println("  if ((jj_nt=token.next) == null)");
        codeGenerator.println("    return (jj_ntk = (token.next=token_source.getNextToken()).kind);");
        codeGenerator.println("  else");
        codeGenerator.println("    return (jj_ntk = jj_nt.kind);");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }
      if (Options.getErrorReporting()) {
        if (!Options.getGenerateGenerics()) {
          codeGenerator.println(
              "  " + JavaUtil.getStatic() + "private java.util.List jj_expentries = new java.util.ArrayList();");
        } else {
          codeGenerator.println("  " + JavaUtil.getStatic()
          + "private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();");
        }
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int[] jj_expentry;");
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_kind = -1;");
        if (context.globals().jj2index != 0) {
          codeGenerator.println("  " + JavaUtil.getStatic() + "private int[] jj_lasttokens = new int[100];");
          codeGenerator.println("  " + JavaUtil.getStatic() + "private int jj_endpos;");
          codeGenerator.println("");
          codeGenerator
          .println("  " + JavaUtil.getStatic() + "private void jj_add_error_token(int kind, int pos) {");
          codeGenerator.println("  if (pos >= 100) {");
          codeGenerator.println("   return;");
          codeGenerator.println("  }");
          codeGenerator.println("");
          codeGenerator.println("  if (pos == jj_endpos + 1) {");
          codeGenerator.println("    jj_lasttokens[jj_endpos++] = kind;");
          codeGenerator.println("  } else if (jj_endpos != 0) {");
          codeGenerator.println("    jj_expentry = new int[jj_endpos];");
          codeGenerator.println("");
          codeGenerator.println("    for (int i = 0; i < jj_endpos; i++) {");
          codeGenerator.println("    jj_expentry[i] = jj_lasttokens[i];");
          codeGenerator.println("    }");
          codeGenerator.println("");
          if (!Options.getGenerateGenerics()) {
            codeGenerator.println("    for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {");
            codeGenerator.println("    int[] oldentry = (int[])(it.next());");
          } else {
            codeGenerator.println("    for (int[] oldentry : jj_expentries) {");
          }

          codeGenerator.println("    if (oldentry.length == jj_expentry.length) {");
          codeGenerator.println("      boolean isMatched = true;");
          codeGenerator.println("");
          codeGenerator.println("      for (int i = 0; i < jj_expentry.length; i++) {");
          codeGenerator.println("      if (oldentry[i] != jj_expentry[i]) {");
          codeGenerator.println("        isMatched = false;");
          codeGenerator.println("        break;");
          codeGenerator.println("      }");
          codeGenerator.println("");
          codeGenerator.println("      }");
          codeGenerator.println("      if (isMatched) {");
          codeGenerator.println("      jj_expentries.add(jj_expentry);");
          codeGenerator.println("      break;");
          codeGenerator.println("      }");
          codeGenerator.println("    }");
          codeGenerator.println("    }");
          codeGenerator.println("");
          codeGenerator.println("    if (pos != 0) {");
          codeGenerator.println("    jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
          codeGenerator.println("    }");
          codeGenerator.println("  }");
          codeGenerator.println("  }");
        }
        codeGenerator.println("");
        codeGenerator.println("  /** Generate ParseException. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "public ParseException generateParseException() {");
        codeGenerator.println("  jj_expentries.clear();");
        codeGenerator.println("  " + JavaUtil.getBooleanType() + "[] la1tokens = new " + JavaUtil.getBooleanType() + "["
            + context.globals().tokenCount + "];");
        codeGenerator.println("  if (jj_kind >= 0) {");
        codeGenerator.println("    la1tokens[jj_kind] = true;");
        codeGenerator.println("    jj_kind = -1;");
        codeGenerator.println("  }");
        codeGenerator.println("  for (int i = 0; i < " + context.globals().maskindex + "; i++) {");
        codeGenerator.println("    if (jj_la1[i] == jj_gen) {");
        codeGenerator.println("    for (int j = 0; j < 32; j++) {");
        for (int i = 0; i < (((context.globals().tokenCount - 1) / 32) + 1); i++) {
          codeGenerator.println("      if ((jj_la1_" + i + "[i] & (1<<j)) != 0) {");
          codeGenerator.print("      la1tokens[");
          if (i != 0) {
            codeGenerator.print((32 * i) + "+");
          }
          codeGenerator.println("j] = true;");
          codeGenerator.println("      }");
        }
        codeGenerator.println("    }");
        codeGenerator.println("    }");
        codeGenerator.println("  }");
        codeGenerator.println("  for (int i = 0; i < " + context.globals().tokenCount + "; i++) {");
        codeGenerator.println("    if (la1tokens[i]) {");
        codeGenerator.println("    jj_expentry = new int[1];");
        codeGenerator.println("    jj_expentry[0] = i;");
        codeGenerator.println("    jj_expentries.add(jj_expentry);");
        codeGenerator.println("    }");
        codeGenerator.println("  }");
        if (context.globals().jj2index != 0) {
          codeGenerator.println("  jj_endpos = 0;");
          codeGenerator.println("  jj_rescan_token();");
          codeGenerator.println("  jj_add_error_token(0, 0);");
        }
        codeGenerator.println("  int[][] exptokseq = new int[jj_expentries.size()][];");
        codeGenerator.println("  for (int i = 0; i < jj_expentries.size(); i++) {");
        if (!Options.getGenerateGenerics()) {
          codeGenerator.println("    exptokseq[i] = (int[])jj_expentries.get(i);");
        } else {
          codeGenerator.println("    exptokseq[i] = jj_expentries.get(i);");
        }
        codeGenerator.println("  }");


        if (isJavaModernMode) {
          // Add the lexical state onto the exception message
          codeGenerator.println(
              "  return new ParseException(token, exptokseq, tokenImage, token_source == null ? null : token_source.lexStateNames[token_source.curLexState]);");
        } else {
          codeGenerator.println("  return new ParseException(token, exptokseq, tokenImage);");
        }

        codeGenerator.println("  }");
      } else {
        codeGenerator.println("  /** Generate ParseException. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "public ParseException generateParseException() {");
        codeGenerator.println("  Token errortok = token.next;");
        if (Options.getKeepLineColumn()) {
          codeGenerator.println("  int line = errortok.beginLine, column = errortok.beginColumn;");
        }
        codeGenerator.println("  String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;");
        if (Options.getKeepLineColumn()) {
          codeGenerator.println("  return new ParseException("
              + "\"Parse error at line \" + line + \", column \" + column + \".  " + "Encountered: \" + mess);");
        } else {
          codeGenerator.println(
              "  return new ParseException(\"Parse error at <unknown location>.  " + "Encountered: \" + mess);");
        }
        codeGenerator.println("  }");
      }
      codeGenerator.println("");

      codeGenerator
      .println("  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " trace_enabled;");
      codeGenerator.println("");
      codeGenerator.println("/** Trace enabled. */");
      codeGenerator.println("  " + JavaUtil.getStatic() + "final public boolean trace_enabled() {");
      codeGenerator.println("  return trace_enabled;");
      codeGenerator.println("  }");
      codeGenerator.println("");

      if (Options.getDebugParser()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private int trace_indent = 0;");
        codeGenerator.println("/** Enable tracing. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "final public void enable_tracing() {");
        codeGenerator.println("  trace_enabled = true;");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("/** Disable tracing. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "final public void disable_tracing() {");
        codeGenerator.println("  trace_enabled = false;");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  " + JavaUtil.getStatic() + "protected void trace_call(String s) {");
        codeGenerator.println("  if (trace_enabled) {");
        codeGenerator.println("    for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        codeGenerator.println("    System.out.println(\"Call: \" + s);");
        codeGenerator.println("  }");
        codeGenerator.println("  trace_indent = trace_indent + 2;");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  " + JavaUtil.getStatic() + "protected void trace_return(String s) {");
        codeGenerator.println("  trace_indent = trace_indent - 2;");
        codeGenerator.println("  if (trace_enabled) {");
        codeGenerator.println("    for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        codeGenerator.println("    System.out.println(\"Return: \" + s);");
        codeGenerator.println("  }");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  " + JavaUtil.getStatic() + "protected void trace_token(Token t, String where) {");
        codeGenerator.println("  if (trace_enabled) {");
        codeGenerator.println("    for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        codeGenerator.println("    System.out.print(\"Consumed token: <\" + tokenImage[t.kind]);");
        codeGenerator.println("    if (t.kind != 0 && !tokenImage[t.kind].equals(\"\\\"\" + t.image + \"\\\"\")) {");
        codeGenerator.println("    System.out.print(\": \\\"\" + " + JavaTemplates.getTokenMgrErrorClass()
        + ".addEscapes(" + "t.image) + \"\\\"\");");
        codeGenerator.println("    }");
        codeGenerator.println(
            "    System.out.println(\" at line \" + t.beginLine + " + "\" column \" + t.beginColumn + \">\" + where);");
        codeGenerator.println("  }");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  " + JavaUtil.getStatic() + "protected void trace_scan(Token t1, int t2) {");
        codeGenerator.println("  if (trace_enabled) {");
        codeGenerator.println("    for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        codeGenerator.println("    System.out.print(\"Visited token: <\" + tokenImage[t1.kind]);");
        codeGenerator.println("    if (t1.kind != 0 && !tokenImage[t1.kind].equals(\"\\\"\" + t1.image + \"\\\"\")) {");
        codeGenerator.println("    System.out.print(\": \\\"\" + " + JavaTemplates.getTokenMgrErrorClass()
        + ".addEscapes(" + "t1.image) + \"\\\"\");");
        codeGenerator.println("    }");
        codeGenerator.println("    System.out.println(\" at line \" + t1.beginLine + \""
            + " column \" + t1.beginColumn + \">; Expected token: <\" + tokenImage[t2] + \">\");");
        codeGenerator.println("  }");
        codeGenerator.println("  }");
        codeGenerator.println("");
      } else {
        codeGenerator.println("  /** Enable tracing. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "final public void enable_tracing() {");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  /** Disable tracing. */");
        codeGenerator.println("  " + JavaUtil.getStatic() + "final public void disable_tracing() {");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }

      if ((context.globals().jj2index != 0) && Options.getErrorReporting()) {
        codeGenerator.println("  " + JavaUtil.getStatic() + "private void jj_rescan_token() {");
        codeGenerator.println("  jj_rescan = true;");
        codeGenerator.println("  for (int i = 0; i < " + context.globals().jj2index + "; i++) {");
        codeGenerator.println("    try {");
        codeGenerator.println("    JJCalls p = jj_2_rtns[i];");
        codeGenerator.println("");
        codeGenerator.println("    do {");
        codeGenerator.println("      if (p.gen > jj_gen) {");
        codeGenerator.println("      jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;");
        codeGenerator.println("      switch (i) {");
        for (int i = 0; i < context.globals().jj2index; i++) {
          codeGenerator.println("        case " + i + ": jj_3_" + (i + 1) + "(); break;");
        }
        codeGenerator.println("      }");
        codeGenerator.println("      }");
        codeGenerator.println("      p = p.next;");
        codeGenerator.println("    } while (p != null);");
        codeGenerator.println("");
        codeGenerator.println("    } catch(LookaheadSuccess ls) { }");
        codeGenerator.println("  }");
        codeGenerator.println("  jj_rescan = false;");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  " + JavaUtil.getStatic() + "private void jj_save(int index, int xla) {");
        codeGenerator.println("  JJCalls p = jj_2_rtns[index];");
        codeGenerator.println("  while (p.gen > jj_gen) {");
        codeGenerator.println("    if (p.next == null) { p = p.next = new JJCalls(); break; }");
        codeGenerator.println("    p = p.next;");
        codeGenerator.println("  }");
        codeGenerator.println("");
        codeGenerator.println("  p.gen = jj_gen + xla - jj_la; ");
        codeGenerator.println("  p.first = token;");
        codeGenerator.println("  p.arg = xla;");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }

      if ((context.globals().jj2index != 0) && Options.getErrorReporting()) {
        codeGenerator.println("  static final class JJCalls {");
        codeGenerator.println("  int gen;");
        codeGenerator.println("  Token first;");
        codeGenerator.println("  int arg;");
        codeGenerator.println("  JJCalls next;");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }

      if (context.globals().cu_from_insertion_point_2.size() != 0) {
        codeGenerator.printTokenSetup(context.globals().cu_from_insertion_point_2.get(0));
        for (final Iterator<Token> it = context.globals().cu_from_insertion_point_2.iterator(); it.hasNext();) {
          t = it.next();
          codeGenerator.printToken(t);
        }
        codeGenerator.printTrailingComments(t);
      }
      codeGenerator.println("");
    }
    // codeBuilder.genCodeLine("}");
  }

  @Override
  public void finish(CodeGeneratorSettings settings, ParserData parserData) {
    try {
      codeGenerator.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private int     gensymindex = 0;
  private int     indentamt;
  private boolean jj2LA;


  /**
   * These lists are used to maintain expansions for which code generation in
   * phase 2 and phase 3 is required. Whenever a call is generated to a phase 2
   * or phase 3 routine, a corresponding entry is added here if it has not
   * already been added. The phase 3 routines have been optimized in version
   * 0.7pre2. Essentially only those methods (and only those portions of these
   * methods) are generated that are required. The lookahead amount is used to
   * determine this. This change requires the use of a hash table because it is
   * now possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads. The hash table provides a easily searchable
   * capability to determine the previous requests. The phase 3 routines now are
   * performed in a two step process - the first step gathers the requests
   * (replacing requests with lower lookaheads with those requiring larger
   * lookaheads). The second step then generates these methods. This
   * optimization and the hashtable makes it look like we do not need the flag
   * "phase3done" any more. But this has not been removed yet.
   */
  private final List<Lookahead>                  phase2list  = new ArrayList<>();
  private final List<Phase3Data>                 phase3list  = new ArrayList<>();
  private final Hashtable<Expansion, Phase3Data> phase3table = new Hashtable<>();

  /**
   * The phase 1 routines generates their output into String's and dumps these
   * String's once for each method. These String's contain the special
   * characters '\u0001' to indicate a positive indent, and '\u0002' to indicate
   * a negative indent. '\n' is used to indicate a line terminator. The
   * characters '\u0003' and '\u0004' are used to delineate portions of text
   * where '\n's should not be followed by an indentation.
   */

  /**
   * Returns true if there is a JAVACODE production that the argument expansion
   * may directly expand to (without consuming tokens or encountering
   * lookahead).
   */
  private boolean javaCodeCheck(Expansion exp) {
    if (exp instanceof RegularExpression) {
      return false;
    } else if (exp instanceof NonTerminal) {
      NormalProduction prod = ((NonTerminal) exp).getProd();
      if (prod instanceof CodeProduction) {
        return true;
      } else {
        return javaCodeCheck(prod.getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice) exp;
      for (Expansion element : ch.getChoices()) {
        if (javaCodeCheck(element)) {
          return true;
        }
      }
      return false;
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence) exp;
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion[] units = seq.units.toArray(new Expansion[seq.units.size()]);
        if ((units[i] instanceof Lookahead) && ((Lookahead) units[i]).isExplicit()) {
          // An explicit lookahead (rather than one generated implicitly).
          // Assume
          // the user knows what he / she is doing, e.g.
          // "A" ( "B" | LOOKAHEAD("X") jcode() | "C" )* "D"
          return false;
        } else if (javaCodeCheck(units[i])) {
          return true;
        } else if (!Semanticize.emptyExpansionExists(units[i])) {
          return false;
        }
      }
      return false;
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore) exp;
      return javaCodeCheck(om.getExpansion());
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore) exp;
      return javaCodeCheck(zm.getExpansion());
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne) exp;
      return javaCodeCheck(zo.getExpansion());
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock) exp;
      return javaCodeCheck(tb.exp);
    } else {
      return false;
    }
  }

  /**
   * An array used to store the first sets generated by the following method. A
   * true entry means that the corresponding token is in the first set.
   */
  private boolean[] firstSet;

  /**
   * Sets up the array "firstSet" above based on the Expansion argument passed
   * to it. Since this is a recursive function, it assumes that "firstSet" has
   * been reset before the first call.
   */
  private void genFirstSet(Expansion exp) {
    if (exp instanceof RegularExpression) {
      firstSet[((RegularExpression) exp).ordinal] = true;
    } else if (exp instanceof NonTerminal) {
      if (!(((NonTerminal) exp).getProd() instanceof CodeProduction)) {
        genFirstSet(((BNFProduction) ((NonTerminal) exp).getProd()).getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice) exp;
      for (Expansion element : ch.getChoices()) {
        genFirstSet(element);
      }
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence) exp;
      Object obj = seq.units.get(0);
      if ((obj instanceof Lookahead) && (((Lookahead) obj).getActionTokens().size() != 0)) {
        jj2LA = true;
      }
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion unit = seq.units.get(i);
        // Javacode productions can not have FIRST sets. Instead we generate the
        // FIRST set
        // for the preceding LOOKAHEAD (the semantic checks should have made
        // sure that
        // the LOOKAHEAD is suitable).
        if ((unit instanceof NonTerminal) && (((NonTerminal) unit).getProd() instanceof CodeProduction)) {
          if ((i > 0) && (seq.units.get(i - 1) instanceof Lookahead)) {
            Lookahead la = (Lookahead) seq.units.get(i - 1);
            genFirstSet(la.getLaExpansion());
          }
        } else {
          genFirstSet(seq.units.get(i));
        }
        if (!Semanticize.emptyExpansionExists(seq.units.get(i))) {
          break;
        }
      }
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore) exp;
      genFirstSet(om.getExpansion());
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore) exp;
      genFirstSet(zm.getExpansion());
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne) exp;
      genFirstSet(zo.getExpansion());
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock) exp;
      genFirstSet(tb.exp);
    }
  }

  /**
   * Constants used in the following method "buildLookaheadChecker".
   */
  private final int NOOPENSTM  = 0;
  private final int OPENIF     = 1;
  private final int OPENSWITCH = 2;

  /**
   * This method takes two parameters - an array of Lookahead's "conds", and an
   * array of String's "actions". "actions" contains exactly one element more
   * than "conds". "actions" are Java source code, and "conds" translate to
   * conditions - so lets say "f(conds[i])" is true if the lookahead required by
   * "conds[i]" is indeed the case. This method returns a string corresponding
   * to the Java code for:
   *
   * if (f(conds[0]) actions[0] else if (f(conds[1]) actions[1] . . . else
   * actions[action.length-1]
   *
   * A particular action entry ("actions[i]") can be null, in which case, a noop
   * is generated for that action.
   */
  private String buildLookaheadChecker(Lookahead[] conds, String[] actions) {

    // The state variables.
    int state = NOOPENSTM;
    int indentAmt = 0;
    boolean[] casedValues = new boolean[context.globals().tokenCount];
    String retval = "";
    Lookahead la;
    Token t = null;
    int tokenMaskSize = ((context.globals().tokenCount - 1) / 32) + 1;
    int[] tokenMask = null;

    // Iterate over all the conditions.
    int index = 0;
    while (index < conds.length) {

      la = conds[index];
      jj2LA = false;

      if ((la.getAmount() == 0) || Semanticize.emptyExpansionExists(la.getLaExpansion())
          || javaCodeCheck(la.getLaExpansion())) {

        // This handles the following cases:
        // . If syntactic lookahead is not wanted (and hence explicitly
        // specified
        // as 0).
        // . If it is possible for the lookahead expansion to recognize the
        // empty
        // string - in which case the lookahead trivially passes.
        // . If the lookahead expansion has a JAVACODE production that it
        // directly
        // expands to - in which case the lookahead trivially passes.
        if (la.getActionTokens().size() == 0) {
          // In addition, if there is no semantic lookahead, then the
          // lookahead trivially succeeds. So break the main loop and
          // treat this case as the default last action.
          break;
        } else {
          // This case is when there is only semantic lookahead
          // (without any preceding syntactic lookahead). In this
          // case, an "if" statement is generated.
          switch (state) {
            case NOOPENSTM:
              retval += "\n" + "if (";
              indentAmt++;
              break;
            case OPENIF:
              retval += "\u0002\n" + "} else if (";
              break;
            case OPENSWITCH:
              retval += "\u0002\n" + "default:" + "\u0001";
              if (Options.getErrorReporting()) {
                retval += "\njj_la1[" + context.globals().maskindex + "] = jj_gen;";
                context.globals().maskindex++;
              }
              context.globals().maskVals.add(tokenMask);
              retval += "\n" + "if (";
              indentAmt++;
          }
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator<Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }
      } else if ((la.getAmount() == 1) && (la.getActionTokens().size() == 0)) {
        // Special optimal processing when the lookahead is exactly 1, and there
        // is no semantic lookahead.

        if (firstSet == null) {
          firstSet = new boolean[context.globals().tokenCount];
        }
        for (int i = 0; i < context.globals().tokenCount; i++) {
          firstSet[i] = false;
        }
        // jj2LA is set to false at the beginning of the containing "if"
        // statement.
        // It is checked immediately after the end of the same statement to
        // determine
        // if lookaheads are to be performed using calls to the jj2 methods.
        genFirstSet(la.getLaExpansion());
        // genFirstSet may find that semantic attributes are appropriate for the
        // next
        // token. In which case, it sets jj2LA to true.
        if (!jj2LA) {

          // This case is if there is no applicable semantic lookahead and the
          // lookahead
          // is one (excluding the earlier cases such as JAVACODE, etc.).
          switch (state) {
            case OPENIF:
              retval += "\u0002\n" + "} else {\u0001";
              // Control flows through to next case.
            case NOOPENSTM:
              retval += "\n" + "switch (";
              if (Options.getCacheTokens()) {
                retval += "jj_nt.kind) {\u0001";
              } else {
                retval += "(jj_ntk==-1)?jj_ntk_f():jj_ntk) {\u0001";
              }
              for (int i = 0; i < context.globals().tokenCount; i++) {
                casedValues[i] = false;
              }
              indentAmt++;
              tokenMask = new int[tokenMaskSize];
              for (int i = 0; i < tokenMaskSize; i++) {
                tokenMask[i] = 0;
              }
              // Don't need to do anything if state is OPENSWITCH.
          }
          for (int i = 0; i < context.globals().tokenCount; i++) {
            if (firstSet[i]) {
              if (!casedValues[i]) {
                casedValues[i] = true;
                retval += "\u0002\ncase ";
                int j1 = i / 32;
                int j2 = i % 32;
                tokenMask[j1] |= 1 << j2;
                String s = context.globals().names_of_tokens.get(Integer.valueOf(i));
                if (s == null) {
                  retval += i;
                } else {
                  retval += s;
                }
                retval += ":\u0001";
              }
            }
          }
          retval += "{";
          retval += actions[index];
          retval += "\nbreak;\n}";
          state = OPENSWITCH;
        }

      } else {
        // This is the case when lookahead is determined through calls to
        // jj2 methods. The other case is when lookahead is 1, but semantic
        // attributes need to be evaluated. Hence this crazy control structure.

        jj2LA = true;

      }

      if (jj2LA) {
        // In this case lookahead is determined by the jj2 methods.

        switch (state) {
          case NOOPENSTM:
            retval += "\n" + "if (";
            indentAmt++;
            break;
          case OPENIF:
            retval += "\u0002\n" + "} else if (";
            break;
          case OPENSWITCH:
            retval += "\u0002\n" + "default:" + "\u0001";
            if (Options.getErrorReporting()) {
              retval += "\njj_la1[" + context.globals().maskindex + "] = jj_gen;";
              context.globals().maskindex++;
            }
            context.globals().maskVals.add(tokenMask);
            retval += "\n" + "if (";
            indentAmt++;
        }
        context.globals().jj2index++;
        // At this point, la.la_expansion.internal_name must be "".
        internalNames.put(la.getLaExpansion(), "_" + context.globals().jj2index);
        internalIndexes.put(la.getLaExpansion(), context.globals().jj2index);
        phase2list.add(la);
        retval += "jj_2" + internalNames.get(la.getLaExpansion()) + "(" + la.getAmount() + ")";
        if (la.getActionTokens().size() != 0) {
          // In addition, there is also a semantic lookahead. So concatenate
          // the semantic check with the syntactic one.
          retval += " && (";
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator<Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ")";
        }
        retval += ") {\u0001" + actions[index];
        state = OPENIF;
      }

      index++;
    }

    // Generate code for the default case. Note this may not
    // be the last entry of "actions" if any condition can be
    // statically determined to be always "true".

    switch (state) {
      case NOOPENSTM:
        retval += actions[index];
        break;
      case OPENIF:
        retval += "\u0002\n" + "} else {\u0001" + actions[index];
        break;
      case OPENSWITCH:
        retval += "\u0002\n" + "default:" + "\u0001";
        if (Options.getErrorReporting()) {
          retval += "\njj_la1[" + context.globals().maskindex + "] = jj_gen;";
          context.globals().maskVals.add(tokenMask);
          context.globals().maskindex++;
        }
        retval += actions[index];
    }
    for (int i = 0; i < indentAmt; i++) {
      retval += "\u0002\n}";
    }

    return retval;
  }

  private void dumpFormattedString(String str) {
    char ch = ' ';
    char prevChar;
    boolean indentOn = true;
    for (int i = 0; i < str.length(); i++) {
      prevChar = ch;
      ch = str.charAt(i);
      if ((ch == '\n') && (prevChar == '\r')) {
        // do nothing - we've already printed a new line for the '\r'
        // during the previous iteration.
      } else if ((ch == '\n') || (ch == '\r')) {
        if (indentOn) {
          phase1NewLine();
        } else {
          codeGenerator.println("");
        }
      } else if (ch == '\u0001') {
        indentamt += 2;
      } else if (ch == '\u0002') {
        indentamt -= 2;
      } else if (ch == '\u0003') {
        indentOn = false;
      } else if (ch == '\u0004') {
        indentOn = true;
      } else {
        codeGenerator.print(ch);
      }
    }
  }

  private void buildPhase1Routine(BNFProduction p) {
    Token t = p.getReturnTypeTokens().get(0);
    boolean voidReturn = false;
    if (t.kind == JavaCCParserConstants.VOID) {
      voidReturn = true;
    }
    codeGenerator.printTokenSetup(t);
    codeGenerator.printLeadingComments(t);
    codeGenerator.print(
        "  " + JavaUtil.getStatic() + "final " + (p.getAccessMod() != null ? p.getAccessMod() : "public") + " ");
    codeGenerator.printTokenOnly(t);
    for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
      t = p.getReturnTypeTokens().get(i);
      codeGenerator.printToken(t);
    }
    codeGenerator.printTrailingComments(t);
    codeGenerator.print(p.getLhs() + "(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup(p.getParameterListTokens().get(0));
      for (Iterator<Token> it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    codeGenerator.print(")");
    codeGenerator.print(" throws ParseException");

    for (List<Token> name : p.getThrowsList()) {
      codeGenerator.print(", ");
      for (Iterator<Token> it2 = name.iterator(); it2.hasNext();) {
        t = it2.next();
        codeGenerator.print(t.image);
      }
    }

    codeGenerator.print(" {");

    genStackCheck(voidReturn);

    indentamt = 4;
    if (Options.getDebugParser()) {
      codeGenerator.println("");
      codeGenerator.println("    trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) + "\");");
      codeGenerator.println("    try {");
      indentamt = 6;
    }

    if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) && (p.getDeclarationTokens().size() != 0)) {
      codeGenerator.printTokenSetup(p.getDeclarationTokens().get(0));
      for (Iterator<Token> it = p.getDeclarationTokens().iterator(); it.hasNext();) {
        t = it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }

    String code = phase1ExpansionGen(p.getExpansion());
    dumpFormattedString(code);
    codeGenerator.println("");

    if (p.isJumpPatched() && !voidReturn) {
      codeGenerator.println("    throw new " + (Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")
          + "(\"Missing return statement in function\");");
    }
    if (Options.getDebugParser()) {
      codeGenerator.println("    } finally {");
      codeGenerator.println("      trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) + "\");");
      codeGenerator.println("    }");
    }
    genStackCheckEnd();
    codeGenerator.println("}");
    codeGenerator.println("");
  }

  private void phase1NewLine() {
    codeGenerator.println("");
    for (int i = 0; i < indentamt; i++) {
      codeGenerator.print(" ");
    }
  }

  private String phase1ExpansionGen(Expansion e) {
    String retval = "";
    Token t = null;
    Lookahead[] conds;
    String[] actions;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression) e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.lhsTokens.get(0));
        for (Iterator<Token> it = e_nrw.lhsTokens.iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeBuilder.toString(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      String tail = e_nrw.rhsToken == null ? ");" : ")." + e_nrw.rhsToken.image + ";";
      if (e_nrw.label.equals("")) {
        Object label = context.globals().names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          retval += "jj_consume_token(" + (String) label + tail;
        } else {
          retval += "jj_consume_token(" + e_nrw.ordinal + tail;
        }
      } else {
        retval += "jj_consume_token(" + e_nrw.label + tail;
      }

    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal) e;
      retval += "\n";
      if (e_nrw.getLhsTokens().size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.getLhsTokens().get(0));
        for (Iterator<Token> it = e_nrw.getLhsTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeBuilder.toString(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      retval += e_nrw.getName() + "(";
      if (e_nrw.getArgumentTokens().size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.getArgumentTokens().get(0));
        for (Iterator<Token> it = e_nrw.getArgumentTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeBuilder.toString(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += ");";
    } else if (e instanceof Action) {
      Action e_nrw = (Action) e;
      retval += "\u0003\n";
      if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) && (e_nrw.getActionTokens().size() != 0)) {
        codeGenerator.printTokenSetup(e_nrw.getActionTokens().get(0));
        for (Iterator<Token> it = e_nrw.getActionTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeBuilder.toString(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += "\u0004";
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice) e;
      conds = new Lookahead[e_nrw.getChoices().size()];
      actions = new String[e_nrw.getChoices().size() + 1];
      actions[e_nrw.getChoices().size()] = "\n" + "jj_consume_token(-1);\n" + "throw new ParseException();";

      // In previous line, the "throw" never throws an exception since the
      // evaluation of jj_consume_token(-1) causes ParseException to be
      // thrown first.
      Sequence nestedSeq;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nestedSeq = (Sequence) e_nrw.getChoices().get(i);
        actions[i] = phase1ExpansionGen(nestedSeq);
        conds[i] = (Lookahead) nestedSeq.units.get(0);
      }
      retval = buildLookaheadChecker(conds, actions);
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence) e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        // For C++, since we are not using exceptions, we will protect all the
        // expansion choices with if (!error)
        boolean wrap_in_block = false;
        retval += phase1ExpansionGen(e_nrw.units.get(i));
        if (wrap_in_block) {
          retval += "\n}";
        }
      }
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead) ((Sequence) nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "label_" + labelIndex + ":\n";
      retval += "while (true) {\u0001";
      retval += phase1ExpansionGen(nested_e);
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\nbreak label_" + labelIndex + ";";

      retval += buildLookaheadChecker(conds, actions);
      retval += "\u0002\n" + "}";
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead) ((Sequence) nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "label_" + labelIndex + ":\n";
      retval += "while (true) {\u0001";
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\nbreak label_" + labelIndex + ";";
      retval += buildLookaheadChecker(conds, actions);
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne) e;
      Expansion nested_e = e_nrw.getExpansion();
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead) ((Sequence) nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = phase1ExpansionGen(nested_e);
      actions[1] = "\n;";
      retval += buildLookaheadChecker(conds, actions);
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock) e;
      Expansion nested_e = e_nrw.exp;
      List<Token> list;
      retval += "\n";
      retval += "try {\u0001";
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      for (int i = 0; i < e_nrw.catchblks.size(); i++) {
        retval += " catch (";
        list = e_nrw.types.get(i);
        if (list.size() != 0) {
          codeGenerator.printTokenSetup((list.get(0)));
          for (Iterator<Token> it = list.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += " ";
        // t = (Token)(e_nrw.ids.get(i));
        // codeBuilder.printTokenSetup(t);
        // retval += CodeGenHelper.getStringToPrint(t);
        // retval += codeBuilder.getTrailingComments(t);
        // retval += ") {\u0003\n";
        list = e_nrw.catchblks.get(i);
        if (list.size() != 0) {
          codeGenerator.printTokenSetup(list.get(0));
          for (Iterator<Token> it = list.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
      if (e_nrw.finallyblk != null) {
        retval += " finally {\u0003\n";

        if (e_nrw.finallyblk.size() != 0) {
          codeGenerator.printTokenSetup(e_nrw.finallyblk.get(0));
          for (Iterator<Token> it = e_nrw.finallyblk.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
    }
    return retval;
  }

  private void buildPhase2Routine(Lookahead la) {
    Expansion e = la.getLaExpansion();
    codeGenerator.println("  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_2"
        + internalNames.get(e) + "(int xla)");
    codeGenerator.println(" {");
    codeGenerator.println("    jj_la = xla; jj_lastpos = jj_scanpos = token;");

    String ret_suffix = "";
    if (Options.getDepthLimit() > 0) {
      ret_suffix = " && !jj_depth_error";
    }

    codeGenerator.println("    try { return (!jj_3" + internalNames.get(e) + "()" + ret_suffix + "); }");
    codeGenerator.println("    catch(LookaheadSuccess ls) { return true; }");
    if (Options.getErrorReporting()) {
      codeGenerator
      .println("    finally { jj_save(" + (Integer.parseInt(internalNames.get(e).substring(1)) - 1) + ", xla); }");
    }
    codeGenerator.println("  }");
    codeGenerator.println("");
    Phase3Data p3d = new Phase3Data(e, la.getAmount());
    phase3list.add(p3d);
    phase3table.put(e, p3d);
  }

  private boolean   xsp_declared;

  private Expansion jj3_expansion;

  private String genReturn(boolean value) {
    String retval = value ? "true" : "false";
    if (Options.getDebugLookahead() && (jj3_expansion != null)) {
      String tracecode =
          "trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(((NormalProduction) jj3_expansion.parent).getLhs())
          + "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private void generate3R(Expansion e, Phase3Data inf) {
    Expansion seq = e;
    if (!internalNames.containsKey(e) || internalNames.get(e).equals("")) {
      while (true) {
        if ((seq instanceof Sequence) && (((Sequence) seq).units.size() == 2)) {
          seq = ((Sequence) seq).units.get(1);
        } else if (seq instanceof NonTerminal) {
          NonTerminal e_nrw = (NonTerminal) seq;
          NormalProduction ntprod = context.globals().production_table.get(e_nrw.getName());
          if (ntprod instanceof CodeProduction) {
            break; // nothing to do here
          } else {
            seq = ntprod.getExpansion();
          }
        } else {
          break;
        }
      }

      if (seq instanceof RegularExpression) {
        internalNames.put(e, "jj_scan_token(" + ((RegularExpression) seq).ordinal + ")");
        return;
      }

      gensymindex++;
      internalNames.put(e, "R_" + e.getProductionName() + "_" + e.getLine() + "_" + e.getColumn() + "_" + gensymindex);
      internalIndexes.put(e, gensymindex);
    }
    Phase3Data p3d = phase3table.get(e);
    if ((p3d == null) || (p3d.count < inf.count)) {
      p3d = new Phase3Data(e, inf.count);
      phase3list.add(p3d);
      phase3table.put(e, p3d);
    }
  }

  private void setupPhase3Builds(Phase3Data inf) {
    Expansion e = inf.exp;
    if (e instanceof RegularExpression) {
      // nothing to here
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set. So
      // there's no need to check it below for "e_nrw" and "ntexp". In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal) e;
      NormalProduction ntprod = context.globals().production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        // nothing to do here
      } else {
        generate3R(ntprod.getExpansion(), inf);
      }
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice) e;
      for (Expansion element : e_nrw.getChoices()) {
        generate3R(element, inf);
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence) e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        setupPhase3Builds(new Phase3Data(eseq, cnt));
        cnt -= minimumSize(eseq);
        if (cnt <= 0) {
          break;
        }
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock) e;
      setupPhase3Builds(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore) e;
      generate3R(e_nrw.getExpansion(), inf);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore) e;
      generate3R(e_nrw.getExpansion(), inf);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne) e;
      generate3R(e_nrw.getExpansion(), inf);
    }
  }

  private String getTypeForToken() {
    return "Token";
  }

  private String genjj_3Call(Expansion e) {
    if (internalNames.containsKey(e) && internalNames.get(e).startsWith("jj_scan_token")) {
      return internalNames.get(e);
    } else {
      return "jj_3" + internalNames.get(e) + "()";
    }
  }

  private void buildPhase3Routine(Phase3Data inf, boolean recursive_call) {
    Expansion e = inf.exp;
    Token t = null;
    if (internalNames.containsKey(e) && internalNames.get(e).startsWith("jj_scan_token")) {
      return;
    }

    if (!recursive_call) {
      codeGenerator.println(
          "  " + JavaUtil.getStatic() + "private " + JavaUtil.getBooleanType() + " jj_3"
              + internalNames.get(e) + "()");
      codeGenerator.println(" {");
      genStackCheck(false);
      xsp_declared = false;
      if (Options.getDebugLookahead() && (e.parent instanceof NormalProduction)) {
        codeGenerator.print("    ");
        if (Options.getErrorReporting()) {
          codeGenerator.print("if (!jj_rescan) ");
        }
        codeGenerator.println("trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(((NormalProduction) e.parent).getLhs())
        + "(LOOKING AHEAD...)\");");
        jj3_expansion = e;
      } else {
        jj3_expansion = null;
      }
    }
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression) e;
      if (e_nrw.label.equals("")) {
        Object label = context.globals().names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          codeGenerator.println("    if (jj_scan_token(" + (String) label + ")) " + genReturn(true));
        } else {
          codeGenerator.println("    if (jj_scan_token(" + e_nrw.ordinal + ")) " + genReturn(true));
        }
      } else {
        codeGenerator.println("    if (jj_scan_token(" + e_nrw.label + ")) " + genReturn(true));
      }
      // codeBuilder.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos) "
      // + genReturn(false));
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set. So
      // there's no need to check it below for "e_nrw" and "ntexp". In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal) e;
      NormalProduction ntprod = context.globals().production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        codeGenerator.println("    if (true) { jj_la = 0; jj_scanpos = jj_lastpos; " + genReturn(false) + "}");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        codeGenerator.println("    if (" + genjj_3Call(ntexp) + ") " + genReturn(true));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice) e;
      if (e_nrw.getChoices().size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          codeGenerator.println("    " + getTypeForToken() + " xsp;");
        }
        codeGenerator.println("    xsp = jj_scanpos;");
      }
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nested_seq = (Sequence) e_nrw.getChoices().get(i);
        Lookahead la = (Lookahead) nested_seq.units.get(0);
        if (la.getActionTokens().size() != 0) {
          // We have semantic lookahead that must be evaluated.
          context.globals().lookaheadNeeded = true;
          codeGenerator.println("    jj_lookingAhead = true;");
          codeGenerator.print("    jj_semLA = ");
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator<Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            codeGenerator.printToken(t);
          }
          codeGenerator.printTrailingComments(t);
          codeGenerator.println(";");
          codeGenerator.println("    jj_lookingAhead = false;");
        }
        codeGenerator.print("    if (");
        if (la.getActionTokens().size() != 0) {
          codeGenerator.print("!jj_semLA || ");
        }
        if (i != (e_nrw.getChoices().size() - 1)) {
          codeGenerator.println(genjj_3Call(nested_seq) + ") {");
          codeGenerator.println("    jj_scanpos = xsp;");
        } else {
          codeGenerator.println(genjj_3Call(nested_seq) + ") " + genReturn(true));
        }
      }
       for (int i = 1; i < e_nrw.getChoices().size(); i++) {
    	   codeGenerator.println(" }");
       }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence) e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        buildPhase3Routine(new Phase3Data(eseq, cnt), true);
        cnt -= minimumSize(eseq);
        if (cnt <= 0) {
          break;
        }
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock) e;
      buildPhase3Routine(new Phase3Data(e_nrw.exp, inf.count), true);
    } else if (e instanceof OneOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.println("    " + getTypeForToken() + " xsp;");
      }
      OneOrMore e_nrw = (OneOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      codeGenerator.println("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      codeGenerator.println("    while (true) {");
      codeGenerator.println("      xsp = jj_scanpos;");
      codeGenerator.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      codeGenerator.println("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.println("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      codeGenerator.println("    while (true) {");
      codeGenerator.println("      xsp = jj_scanpos;");
      codeGenerator.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      codeGenerator.println("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.println("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne) e;
      Expansion nested_e = e_nrw.getExpansion();
      codeGenerator.println("    xsp = jj_scanpos;");
      codeGenerator.println("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
    }
    if (!recursive_call) {
      codeGenerator.println("    " + genReturn(false));
      genStackCheckEnd();
      codeGenerator.println("  }");
      codeGenerator.println("");
    }
  }

  private int minimumSize(Expansion e) {
    return minimumSize(e, Integer.MAX_VALUE);
  }

  /*
   * Returns the minimum number of tokens that can parse to this expansion.
   */
  private int minimumSize(Expansion e, int oldMin) {
    int retval = 0; // should never be used. Will be bad if it is.
    if (e.inMinimumSize) {
      // recursive search for minimum size unnecessary.
      return Integer.MAX_VALUE;
    }
    e.inMinimumSize = true;
    if (e instanceof RegularExpression) {
      retval = 1;
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal) e;
      NormalProduction ntprod = context.globals().production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        retval = Integer.MAX_VALUE;
        // Make caller think this is unending (for we do not go beyond JAVACODE
        // during
        // phase3 execution).
      } else {
        Expansion ntexp = ntprod.getExpansion();
        retval = minimumSize(ntexp);
      }
    } else if (e instanceof Choice) {
      int min = oldMin;
      Expansion nested_e;
      Choice e_nrw = (Choice) e;
      for (int i = 0; (min > 1) && (i < e_nrw.getChoices().size()); i++) {
        nested_e = e_nrw.getChoices().get(i);
        int min1 = minimumSize(nested_e, min);
        if (min > min1) {
          min = min1;
        }
      }
      retval = min;
    } else if (e instanceof Sequence) {
      int min = 0;
      Sequence e_nrw = (Sequence) e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        int mineseq = minimumSize(eseq);
        if ((min == Integer.MAX_VALUE) || (mineseq == Integer.MAX_VALUE)) {
          min = Integer.MAX_VALUE; // Adding infinity to something results in
          // infinity.
        } else {
          min += mineseq;
          if (min > oldMin) {
            break;
          }
        }
      }
      retval = min;
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock) e;
      retval = minimumSize(e_nrw.exp);
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore) e;
      retval = minimumSize(e_nrw.getExpansion());
    } else if (e instanceof ZeroOrMore) {
      retval = 0;
    } else if (e instanceof ZeroOrOne) {
      retval = 0;
    } else if (e instanceof Lookahead) {
      retval = 0;
    } else if (e instanceof Action) {
      retval = 0;
    }
    e.inMinimumSize = false;
    return retval;
  }

  private void genStackCheck(boolean voidReturn) {
    if (Options.getDepthLimit() > 0) {
      codeGenerator.println("if(++jj_depth > " + Options.getDepthLimit() + ") {");
      codeGenerator.println("  jj_consume_token(-1);");
      codeGenerator.println("  throw new ParseException();");
      codeGenerator.println("}");
      codeGenerator.println("try {");
    }
  }

  private void genStackCheckEnd() {
    if (Options.getDepthLimit() > 0) {
      codeGenerator.println(" } finally {");
      codeGenerator.println("   --jj_depth;");
      codeGenerator.println(" }");
    }
  }

  private void build(GenericCodeBuilder codeBuilder) {
    NormalProduction p;
    JavaCodeProduction jp;
    Token t = null;

    for (Iterator<NormalProduction> prodIterator = context.globals().bnfproductions.iterator(); prodIterator.hasNext();) {
      p = prodIterator.next();
      if (p instanceof JavaCodeProduction) {
        jp = (JavaCodeProduction) p;
        t = jp.getReturnTypeTokens().get(0);
        codeBuilder.printTokenSetup(t);
        codeBuilder.printLeadingComments(t);
        codeBuilder.print("  " + JavaUtil.getStatic() + (p.getAccessMod() != null ? p.getAccessMod() + " " : ""));
        codeBuilder.printTokenOnly(t);
        for (int i = 1; i < jp.getReturnTypeTokens().size(); i++) {
          t = jp.getReturnTypeTokens().get(i);
          codeBuilder.printToken(t);
        }
        codeBuilder.printTrailingComments(t);
        codeBuilder.print(" " + jp.getLhs() + "(");
        if (jp.getParameterListTokens().size() != 0) {
          codeBuilder.printTokenSetup(jp.getParameterListTokens().get(0));
          for (Iterator<Token> it = jp.getParameterListTokens().iterator(); it.hasNext();) {
            t = it.next();
            codeBuilder.printToken(t);
          }
          codeBuilder.printTrailingComments(t);
        }
        codeBuilder.print(")");
        codeBuilder.print(" throws ParseException");
        for (List<Token> name : jp.getThrowsList()) {
          codeBuilder.print(", ");
          for (Iterator<Token> it2 = name.iterator(); it2.hasNext();) {
            t = it2.next();
            codeBuilder.print(t.image);
          }
        }
        codeBuilder.print(" {");
        if (Options.getDebugParser()) {
          codeBuilder.println("");
          codeBuilder.println("    trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(jp.getLhs()) + "\");");
          codeBuilder.print("    try {");
        }
        if (jp.getCodeTokens().size() != 0) {
          codeBuilder.printTokenSetup(jp.getCodeTokens().get(0));
          codeBuilder.printTokenList(jp.getCodeTokens());
        }
        codeBuilder.println("");
        if (Options.getDebugParser()) {
          codeBuilder.println("    } finally {");
          codeBuilder.println("      trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(jp.getLhs()) + "\");");
          codeBuilder.println("    }");
        }
        codeBuilder.println("  }");
        codeBuilder.println("");
      } else {
        buildPhase1Routine((BNFProduction) p);
      }
    }

    for (Lookahead element : phase2list) {
      buildPhase2Routine(element);
    }

    int phase3index = 0;

    while (phase3index < phase3list.size()) {
      for (; phase3index < phase3list.size(); phase3index++) {
        setupPhase3Builds(phase3list.get(phase3index));
      }
    }

    for (Enumeration<Phase3Data> enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      buildPhase3Routine(enumeration.nextElement(), false);
    }
    // for (java.util.Enumeration enumeration = phase3table.elements();
    // enumeration.hasMoreElements();) {
    // Phase3Data inf = (Phase3Data)(enumeration.nextElement());
    // System.err.println("**** Table for: " + inf.exp.internal_name);
    // buildPhase3Table(inf);
    // System.err.println("**** END TABLE *********");
    // }
  }
}


/**
 * This class stores information to pass from phase 2 to phase 3.
 */
class Phase3Data {

  /*
   * This is the expansion to generate the jj3 method for.
   */
  Expansion exp;

  /*
   * This is the number of tokens that can still be consumed. This number is
   * used to limit the number of jj3 methods generated.
   */
  int count;

  Phase3Data(Expansion e, int c) {
    exp = e;
    count = c;
  }
}
