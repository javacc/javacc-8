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

package org.javacc.cpp;


import org.javacc.parser.Action;
import org.javacc.parser.BNFProduction;
import org.javacc.parser.Choice;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.CodeProduction;
import org.javacc.parser.Context;
import org.javacc.parser.CppCodeProduction;
import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.Lookahead;
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
   * capability to determine the previous requests. The phase 3 routines now are
   * performed in a two step process - the first step gathers the requests
   * (replacing requests with lower lookaheads with those requiring larger
   * lookaheads). The second step then generates these methods. This
   * optimization and the hashtable makes it look like we do not need the flag
   * "phase3done" any more. But this has not been removed yet.
   */

  private final Context                 context;
  private CppCodeBuilder                codeGenerator;

  private final Map<Expansion, String>  internalNames   = new HashMap<>();
  private final Map<Expansion, Integer> internalIndexes = new HashMap<>();

  ParserCodeGenerator(Context context) {
    this.context = context;
  }

  void printInclude(String include) {
    if (include != null && include.length() > 0) {
      if (include.charAt(0) == '<') {
        codeGenerator.println("#include " + include);
      } else {
        codeGenerator.println("#include \"" + include + "\"");
      }
    }
  }
  @Override
  public void generateCode(CodeGeneratorSettings settings, ParserData parserData) {
    List<String> tn = new ArrayList<>(context.globals().toolNames);
    tn.add(JavaCCGlobals.toolName);

    File file = new File(Options.getOutputDirectory(), parserData.parserName + ".cc");
    codeGenerator = CppCodeBuilder.of(context, settings).setFile(file);

    
    if (context.globals().jjtreeGenerated) {
      codeGenerator.switchToStaticsFile();
      codeGenerator.println("#include \"" + context.globals().cu_name + "Tree.h\"\n");
    }

    codeGenerator.switchToIncludeFile();
    String guard = "JAVACC_" + parserData.parserName.toUpperCase() + "_H";
    codeGenerator.println("#ifndef " + guard);
    codeGenerator.println("#define " + guard);
    codeGenerator.println();
    
    if (!Options.getLibrary().isEmpty()) {
        codeGenerator.println("#include \"ImportExport.h\"");
    }
    codeGenerator.println("#include \"JavaCC.h\"");
    codeGenerator.println("#include \"CharStream.h\"");
    codeGenerator.println("#include \"Token.h\"");
    codeGenerator.println("#include \"TokenManager.h\"");
    if (!Options.getTokenInclude().isEmpty()) {
        codeGenerator.println("#include \"" + Options.getTokenInclude() +"\"");
    }
    printInclude(Options.getParserInclude());

    if (Options.getTokenConstantsInclude().isEmpty()) {
        codeGenerator.println("#include \"" + context.globals().cu_name + "Constants.h\"");
    } else {
        codeGenerator.println("#include \"" + Options.getTokenConstantsInclude() + "\"");
    }

    if (context.globals().jjtreeGenerated) {
      codeGenerator.println("#include \"JJT" + context.globals().cu_name + "State.h\"");
    }

    codeGenerator.println("#include \"DefaultParserErrorHandler.h\"");

    if (context.globals().jjtreeGenerated) {
      codeGenerator.println("#include \"" + context.globals().cu_name + "Tree.h\"");
    }

    if (Options.hasNamespace()) {
      codeGenerator.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
    }

    codeGenerator.print("  struct ");
    if (!Options.getLibrary().isEmpty()) {
    	codeGenerator.print(parserData.parserName.toUpperCase() + "_API ");
    }
    codeGenerator.println("JJCalls {");
    codeGenerator.println("    int        gen;");
    codeGenerator.println("    int        arg;");
    codeGenerator.println("    JJCalls*   next;");
    codeGenerator.println("    Token*     first;");
    codeGenerator.println("    ~JJCalls() { if (next) delete next; }");
    codeGenerator.println("     JJCalls() { next = nullptr; arg = 0; gen = -1; first = nullptr; }");
    codeGenerator.println("  };");
    codeGenerator.println("");


    codeGenerator.genClassStart("", context.globals().cu_name, new String[] {}, new String[0]);

    codeGenerator.switchToMainFile();
    if (context.globals().cu_to_insertion_point_2.size() != 0) {
      codeGenerator.printTokenSetup(context.globals().cu_to_insertion_point_2.get(0));
      for (Token token : context.globals().cu_to_insertion_point_2) {
        codeGenerator.printToken(token);
      }
    }

    codeGenerator.switchToMainFile();

    if (Options.hasNamespace()) {
      codeGenerator.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
    }

    codeGenerator.println("");
    codeGenerator.println("");

    build(codeGenerator);

    codeGenerator.switchToIncludeFile();
    codeGenerator.println("");
    codeGenerator.println("public: ");
    codeGenerator.println("  void setErrorHandler(ParserErrorHandler* eh) {");
    codeGenerator.println("    if (delete_eh) delete errorHandler;");
    codeGenerator.println("    errorHandler = eh;");
    codeGenerator.println("    delete_eh = false;");
    codeGenerator.println("  }");
    codeGenerator.println("  const ParserErrorHandler* getErrorHandler() {");
    codeGenerator.println("    return errorHandler;");
    codeGenerator.println("  }");
    codeGenerator.println("  static const JJChar* getTokenImage(int kind) {");
    codeGenerator.println("    return kind >= 0 ? " + getTokenImages() + "[kind] : " + getTokenImages() + "[0];");
    codeGenerator.println("  }");
    codeGenerator.println("  static const JJChar* getTokenLabel(int kind) {");
    codeGenerator.println("    return kind >= 0 ? " + getTokenLabels() + "[kind] : " + getTokenLabels() + "[0];");
    codeGenerator.println("  }");
    codeGenerator.println("");
    codeGenerator.println("  TokenManager*          token_source = nullptr;");
    codeGenerator.println("  CharStream*            jj_input_stream = nullptr;");
    codeGenerator.println("  Token*                 token = nullptr;  // Current token.");
    codeGenerator.println("  Token*                 jj_nt = nullptr;  // Next token.");
    codeGenerator.println("");
    codeGenerator.println("private: ");
    codeGenerator.println("  int                    jj_ntk;");

    codeGenerator.println("  JJCalls                jj_2_rtns[" + (context.globals().jj2index + 1) + "];");
    codeGenerator.println("  bool                   jj_rescan;");
    codeGenerator.println("  int                    jj_gc;");
    codeGenerator.println("  Token*                 jj_scanpos;");
    codeGenerator.println("  Token*                 jj_lastpos;");
    codeGenerator.println("  int                    jj_la;");
    codeGenerator.println("  bool                   jj_lookingAhead;  // Whether we are looking ahead.");
    codeGenerator.println("  bool                   jj_semLA;");

    codeGenerator.println("  int                    jj_gen;");
    codeGenerator.println("  int                    jj_la1[" + (context.globals().maskindex + 1) + "];");
    codeGenerator.println("  ParserErrorHandler*    errorHandler = nullptr;");
    codeGenerator.println("");
    codeGenerator.println("protected: ");
    codeGenerator.println("  bool                   delete_eh = false;");
    codeGenerator.println("  bool                   delete_tokens = true;");
    codeGenerator.println("  bool                   hasError;");
    codeGenerator.println("");
    int tokenMaskSize = ((context.globals().tokenCount - 1) / 32) + 1;

    if (Options.getErrorReporting() && (tokenMaskSize > 0)) {
      codeGenerator.switchToStaticsFile();
      codeGenerator.println("#include \"TokenManagerError.h\"");
      for (int i = 0; i < tokenMaskSize; i++) {
        if (context.globals().maskVals.size() > 0) {
          codeGenerator.println("static unsigned int jj_la1_" + i + "[] = {");
          for (int[] tokenMask : context.globals().maskVals) {
            codeGenerator.print("0x" + Integer.toHexString(tokenMask[i]) + ",");
          }
          codeGenerator.println("};");
        }
      }
    }

    if (Options.getDepthLimit() > 0) {
      codeGenerator.println("  private: int jj_depth;");
      codeGenerator.println("  private: bool jj_depth_error;");
      codeGenerator.println("  friend class __jj_depth_inc;");
      codeGenerator.println("  class __jj_depth_inc {public:");
      codeGenerator.println("    " + context.globals().cu_name + "* parent;");
      codeGenerator.println("    __jj_depth_inc(" + context.globals().cu_name + "* p): parent(p) { parent->jj_depth++; };");
      codeGenerator.println("    ~__jj_depth_inc(){ parent->jj_depth--; }");
      codeGenerator.println("  };");
    }
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("  public: size_t jj_stack_limit;");
      codeGenerator.println("  private: void* jj_stack_base;");
      codeGenerator.println("  private: bool jj_stack_error;");
    }

    codeGenerator.println("");

    codeGenerator.println("  /** Constructor with user supplied TokenManager. */");

    codeGenerator.switchToIncludeFile(); // TEMP
    codeGenerator.println("  Token*                 head;");
    codeGenerator.println("");
    codeGenerator.println("public: ");
    codeGenerator.generateMethodDefHeader(" ", context.globals().cu_name,
        context.globals().cu_name + "(TokenManager* tokenManager)");
    codeGenerator.println("{");
    codeGenerator.println("    head = nullptr;");
    codeGenerator.println("    ReInit(tokenManager);");
    if (Options.getTokenManagerUsesParser()) {
      codeGenerator.println("    tokenManager->setParser(this);");
    }
    codeGenerator.println("}");

    codeGenerator.switchToIncludeFile();
    codeGenerator.println("  virtual ~" + context.globals().cu_name + "();");
    codeGenerator.switchToMainFile();
    codeGenerator.println(context.globals().cu_name + "::~" + context.globals().cu_name + "()");
    codeGenerator.println("{");
    codeGenerator.println("  clear();");
    codeGenerator.println("}");
    codeGenerator.generateMethodDefHeader("void", context.globals().cu_name, "ReInit(TokenManager* tokenManager)");
    codeGenerator.println("{");
    codeGenerator.println("    clear();");
    codeGenerator.println("    errorHandler = new DefaultParserErrorHandler();");
    codeGenerator.println("    delete_eh = true;");
    codeGenerator.println("    hasError = false;");
    codeGenerator.println("    token_source = tokenManager;");
    codeGenerator.println("    head = token = new " + getTokenType() + ";");
    codeGenerator.println("    jj_lookingAhead = false;");
    codeGenerator.println("    jj_rescan = false;");
    codeGenerator.println("    jj_done = false;");
    codeGenerator.println("    jj_scanpos = jj_lastpos = nullptr;");
    codeGenerator.println("    jj_gc = 0;");
    codeGenerator.println("    jj_kind = -1;");
    codeGenerator.println("    indent = 0;");
    codeGenerator.println("    trace = " + Options.getDebugParser() + ";");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("    jj_stack_limit = " + Options.getStackLimit() + ";");
      codeGenerator.println("    jj_stack_error = jj_stack_check(true);");
    }

    if (Options.getCacheTokens()) {
      codeGenerator.println("    token->next() = jj_nt = token_source->getNextToken();");
    } else {
      codeGenerator.println("    jj_ntk = -1;");
    }
    if (context.globals().jjtreeGenerated) {
      codeGenerator.println("    jjtree.reset();");
    }
    if (Options.getDepthLimit() > 0) {
      codeGenerator.println("    jj_depth = 0;");
      codeGenerator.println("    jj_depth_error = false;");
    }
    if (Options.getErrorReporting()) {
      codeGenerator.println("    jj_gen = 0;");
      if (context.globals().maskindex > 0) {
        codeGenerator.println("    for (int i = 0; i < " + context.globals().maskindex + "; i++) jj_la1[i] = -1;");
      }
    }
    codeGenerator.println("  }");
    codeGenerator.println("");

    codeGenerator.generateMethodDefHeader("void", context.globals().cu_name, "clear()");
    codeGenerator.println("{");
    codeGenerator.println("  //Since token manager was generate from outside,");
    codeGenerator.println("  //parser should not take care of deleting");
    codeGenerator.println("  //if (token_source) delete token_source;");
    codeGenerator.println("  if (delete_tokens && head) {");
    codeGenerator.println("    Token* next;");
    codeGenerator.println("    Token* t = head;");
    codeGenerator.println("    while (t) {");
    codeGenerator.println("      next = t->next();");
    codeGenerator.println("      delete t;");
    codeGenerator.println("      t = next;");
    codeGenerator.println("    }");
    codeGenerator.println("  }");
    codeGenerator.println("  if (delete_eh) {");
    codeGenerator.println("    delete errorHandler, errorHandler = nullptr;");
    codeGenerator.println("    delete_eh = false;");
    codeGenerator.println("  }");
    if (Options.getDepthLimit() > 0) {
      codeGenerator.println("  assert(jj_depth==0);");
    }
    codeGenerator.println("}");
    codeGenerator.println("");

    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("");
      codeGenerator.switchToIncludeFile();
      codeGenerator.println(" virtual");
      codeGenerator.switchToMainFile();
      codeGenerator.generateMethodDefHeader("bool ", context.globals().cu_name, "jj_stack_check(bool init)");
      codeGenerator.println("  {");
      codeGenerator.println("     if(init) {");
      codeGenerator.println("       jj_stack_base = nullptr;");
      codeGenerator.println("       return false;");
      codeGenerator.println("     } else {");
      codeGenerator.println("       volatile int q = 0;");
      codeGenerator.println("       if(!jj_stack_base) {");
      codeGenerator.println("         jj_stack_base = (void*)&q;");
      codeGenerator.println("         return false;");
      codeGenerator.println("       } else {");
      codeGenerator.println("         // Stack can grow in both directions, depending on arch");
      codeGenerator.println("         std::ptrdiff_t used = (char*)jj_stack_base-(char*)&q;");
      codeGenerator.println("         return (std::abs(used) > jj_stack_limit);");
      codeGenerator.println("       }");
      codeGenerator.println("     }");
      codeGenerator.println("  }");
    }


    codeGenerator.generateMethodDefHeader("Token* ", context.globals().cu_name, "jj_consume_token(int kind)");
    codeGenerator.println("  {");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("    if(kind != -1 && (jj_stack_error || jj_stack_check(false))) {");
      codeGenerator.println("      if (!jj_stack_error) {");
      codeGenerator.println("        errorHandler->otherError(\"Stack overflow while trying to parse\");");
      codeGenerator.println("        jj_stack_error=true;");
      codeGenerator.println("      }");
      codeGenerator.println("      return jj_consume_token(-1);");
      codeGenerator.println("    }");
    }
    if (Options.getCacheTokens()) {
      codeGenerator.println("    Token* oldToken = token;");
      codeGenerator.println("    if ((token = jj_nt)->next() != nullptr) jj_nt = jj_nt->next();");
      codeGenerator.println("    else jj_nt = jj_nt->next() = token_source->getNextToken();");
    } else {
      codeGenerator.println("    Token* oldToken;");
      codeGenerator.println("    if ((oldToken = token)->next() != nullptr) token = token->next();");
      codeGenerator.println("    else token = token->next() = token_source->getNextToken();");
      codeGenerator.println("    jj_ntk = -1;");
    }
    codeGenerator.println("    if (token->kind() == kind) {");
    if (Options.getErrorReporting()) {
      codeGenerator.println("      jj_gen++;");
      if (context.globals().jj2index != 0) {
        codeGenerator.println("      if (++jj_gc > 100) {");
        codeGenerator.println("        jj_gc = 0;");
        codeGenerator.println("        for (int i = 0; i < " + context.globals().jj2index + "; i++) {");
        codeGenerator.println("          JJCalls *c = &jj_2_rtns[i];");
        codeGenerator.println("          while (c != nullptr) {");
        codeGenerator.println("            if (c->gen < jj_gen) c->first = nullptr;");
        codeGenerator.println("            c = c->next;");
        codeGenerator.println("          }");
        codeGenerator.println("        }");
        codeGenerator.println("      }");
      }
    }
    if (Options.getDebugParser()) {
      codeGenerator.println("      trace_token(token, \"\");");
    }
    codeGenerator.println("      return token;");
    codeGenerator.println("    }");
    if (Options.getCacheTokens()) {
      codeGenerator.println("    jj_nt = token;");
    }
    codeGenerator.println("    token = oldToken;");
    if (Options.getErrorReporting()) {
      codeGenerator.println("    jj_kind = kind;");
    }
    // codeGenerator.genCodeLine(" throw generateParseException();");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("    if (!jj_stack_error) {");
    }
    codeGenerator.println("    const JJString expectedImage = getTokenImage(kind);");
    codeGenerator.println("    const JJString expectedLabel = getTokenLabel(kind);");

    codeGenerator.println("    const Token*   actualToken   = getToken(1);");
    codeGenerator.println("    const JJString actualImage   = getTokenImage(actualToken->kind());");
    codeGenerator.println("    const JJString actualLabel   = getTokenLabel(actualToken->kind());");
    codeGenerator.println(
        "    errorHandler->unexpectedToken(expectedImage, expectedLabel, actualImage, actualLabel, actualToken);");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.println("    }");
    }
    codeGenerator.println("    hasError = true;");
    codeGenerator.println("    return token;");
    codeGenerator.println("  }");
    codeGenerator.println("");

    if (context.globals().jj2index != 0) {
      codeGenerator.switchToMainFile();
      codeGenerator.generateMethodDefHeader("bool ", context.globals().cu_name, "jj_scan_token(int kind)");
      codeGenerator.println("{");
      if (!Options.getStackLimit().equals("")) {
        codeGenerator.println("    if(kind != -1 && (jj_stack_error || jj_stack_check(false))) {");
        codeGenerator.println("      if (!jj_stack_error) {");
        codeGenerator
        .println("        errorHandler->otherError(\"Stack overflow while trying to parse\");");
        codeGenerator.println("        jj_stack_error=true;");
        codeGenerator.println("      }");
        codeGenerator.println("      return jj_consume_token(-1);");
        codeGenerator.println("    }");
      }
      codeGenerator.println("    if (jj_scanpos == jj_lastpos) {");
      codeGenerator.println("      jj_la--;");
      codeGenerator.println("      if (jj_scanpos->next() == nullptr) {");
      codeGenerator.println("        jj_lastpos = jj_scanpos = jj_scanpos->next() = token_source->getNextToken();");
      codeGenerator.println("      } else {");
      codeGenerator.println("        jj_lastpos = jj_scanpos = jj_scanpos->next();");
      codeGenerator.println("      }");
      codeGenerator.println("    } else {");
      codeGenerator.println("      jj_scanpos = jj_scanpos->next();");
      codeGenerator.println("    }");
      if (Options.getErrorReporting()) {
        codeGenerator.println("    if (jj_rescan) {");
        codeGenerator.println("      int i = 0; Token* tok = token;");
        codeGenerator.println("      while (tok != nullptr && tok != jj_scanpos) { i++; tok = tok->next(); }");
        codeGenerator.println("      if (tok != nullptr) jj_add_error_token(kind, i);");
        if (Options.getDebugLookahead()) {
          codeGenerator.println("    } else {");
          codeGenerator.println("      trace_scan(jj_scanpos, kind);");
        }
        codeGenerator.println("    }");
      } else if (Options.getDebugLookahead()) {
        codeGenerator.println("    trace_scan(jj_scanpos, kind);");
      }
      codeGenerator.println("    if (jj_scanpos->kind() != kind) return true;");
      // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos)
      // throw jj_ls;");
      codeGenerator.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) { return jj_done = true; }");
      codeGenerator.println("    return false;");
      codeGenerator.println("  }");
      codeGenerator.println("");
    }
    codeGenerator.println("");
    codeGenerator.println("/** Get the next Token. */");
    codeGenerator.generateMethodDefHeader("Token* ", context.globals().cu_name, "getNextToken()");
    codeGenerator.println("{");
    if (Options.getCacheTokens()) {
      codeGenerator.println("    if ((token = jj_nt)->next() != nullptr) jj_nt = jj_nt->next();");
      codeGenerator.println("    else jj_nt = jj_nt->next() = token_source->getNextToken();");
    } else {
      codeGenerator.println("    if (token->next() != nullptr) token = token->next();");
      codeGenerator.println("    else token = token->next() = token_source->getNextToken();");
      codeGenerator.println("    jj_ntk = -1;");
    }
    if (Options.getErrorReporting()) {
      codeGenerator.println("    jj_gen++;");
    }
    if (Options.getDebugParser()) {
      codeGenerator.println("      trace_token(token, \" (in getNextToken)\");");
    }
    codeGenerator.println("    return token;");
    codeGenerator.println("  }");
    codeGenerator.println("");
    codeGenerator.println("/** Get the specific Token. */");
    codeGenerator.generateMethodDefHeader("Token* ", context.globals().cu_name, "getToken(int index)");
    codeGenerator.println("{");
    if (context.globals().lookaheadNeeded) {
      codeGenerator.println("    Token* t = jj_lookingAhead ? jj_scanpos : token;");
    } else {
      codeGenerator.println("    Token* t = token;");
    }
    codeGenerator.println("    for (int i = 0; i < index; i++) {");
    codeGenerator.println("      if (t->next() != nullptr) t = t->next();");
    codeGenerator.println("      else t = t->next() = token_source->getNextToken();");
    codeGenerator.println("    }");
    codeGenerator.println("    return t;");
    codeGenerator.println("  }");
    codeGenerator.println("");
    if (!Options.getCacheTokens()) {
      codeGenerator.generateMethodDefHeader("int", context.globals().cu_name, "jj_ntk_f()");
      codeGenerator.println("{");

      codeGenerator.println("    if ((jj_nt=token->next()) == nullptr)");
      codeGenerator.println("      return (jj_ntk = (token->next()=token_source->getNextToken())->kind());");
      codeGenerator.println("    else");
      codeGenerator.println("      return (jj_ntk = jj_nt->kind());");
      codeGenerator.println("  }");
      codeGenerator.println("");
    }

    codeGenerator.switchToIncludeFile();
    codeGenerator.println("private:");
    codeGenerator.println("  int jj_kind;");
    if (Options.getErrorReporting()) {
      codeGenerator.println("  int** jj_expentries;");
      codeGenerator.println("  int*  jj_expentry;");
      if (context.globals().jj2index != 0) {
        codeGenerator.switchToStaticsFile();
        // For now we don't support ERROR_REPORTING in the C++ version.
        // codeGenerator.genCodeLine(" static int *jj_lasttokens = new
        // int[100];");
        // codeGenerator.genCodeLine(" static int jj_endpos;");
        codeGenerator.println("");

        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "jj_add_error_token(int kind, int pos)");
        codeGenerator.println("  {");
        // For now we don't support ERROR_REPORTING in the C++ version.

        // codeGenerator.genCodeLine(" if (pos >= 100) return;");
        // codeGenerator.genCodeLine(" if (pos == jj_endpos + 1) {");
        // codeGenerator.genCodeLine(" jj_lasttokens[jj_endpos++] = kind;");
        // codeGenerator.genCodeLine(" } else if (jj_endpos != 0) {");
        // codeGenerator.genCodeLine(" jj_expentry = new int[jj_endpos];");
        // codeGenerator.genCodeLine(" for (int i = 0; i < jj_endpos; i++) {");
        // codeGenerator.genCodeLine(" jj_expentry[i] = jj_lasttokens[i];");
        // codeGenerator.genCodeLine(" }");
        // codeGenerator.genCodeLine(" jj_entries_loop: for (java.util.Iterator
        // it = jj_expentries.iterator(); it.hasNext();) {");
        // codeGenerator.genCodeLine(" int[] oldentry = (int[])(it->next());");
        // codeGenerator.genCodeLine(" if (oldentry.length ==
        // jj_expentry.length) {");
        // codeGenerator.genCodeLine(" for (int i = 0; i < jj_expentry.length;
        // i++) {");
        // codeGenerator.genCodeLine(" if (oldentry[i] != jj_expentry[i]) {");
        // codeGenerator.genCodeLine(" continue jj_entries_loop;");
        // codeGenerator.genCodeLine(" }");
        // codeGenerator.genCodeLine(" }");
        // codeGenerator.genCodeLine(" jj_expentries.add(jj_expentry);");
        // codeGenerator.genCodeLine(" break jj_entries_loop;");
        // codeGenerator.genCodeLine(" }");
        // codeGenerator.genCodeLine(" }");
        // codeGenerator.genCodeLine(" if (pos != 0) jj_lasttokens[(jj_endpos =
        // pos) - 1] = kind;");
        // codeGenerator.genCodeLine(" }");
        codeGenerator.println("  }");
      }
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.println("protected:");
      codeGenerator.println("  /** Generate ParseException. */");
      codeGenerator.generateMethodDefHeader("  virtual void ", context.globals().cu_name, "parseError()");
      codeGenerator.println("   {");
      if (Options.getErrorReporting()) {
          codeGenerator.println(
                  "      JJERR << JJWIDE(Parse error at : ) << token->beginLine() << JJWIDE(:) << token->beginColumn() << JJWIDE( after token: ) << addUnicodeEscapes(token->image()) << JJWIDE( encountered: ) << addUnicodeEscapes(getToken(1)->image()) << std::endl;");
      }
      codeGenerator.println("   }");
      /*
       * generateMethodDefHeader("ParseException", cu_name,
       * "generateParseException()"); codeGenerator.genCodeLine("   {");
       * //codeGenerator.genCodeLine("    jj_expentries.clear();");
       * //codeGenerator.genCodeLine("    bool[] la1tokens = new boolean[" +
       * tokenCount + "];");
       * //codeGenerator.genCodeLine("    if (jj_kind >= 0) {");
       * //codeGenerator.genCodeLine("      la1tokens[jj_kind] = true;");
       * //codeGenerator.genCodeLine("      jj_kind = -1;");
       * //codeGenerator.genCodeLine("    }");
       * //codeGenerator.genCodeLine("    for (int i = 0; i < " + maskindex +
       * "; i++) {");
       * //codeGenerator.genCodeLine("      if (jj_la1[i] == jj_gen) {");
       * //codeGenerator.genCodeLine("        for (int j = 0; j < 32; j++) {");
       * //for (int i = 0; i < (tokenCount-1)/32 + 1; i++) {
       * //codeGenerator.genCodeLine("          if ((jj_la1_" + i +
       * "[i] & (1<<j)) != 0) {"); //genCode("            la1tokens["); //if (i
       * != 0) { //genCode((32*i) + "+"); //}
       * //codeGenerator.genCodeLine("j] = true;");
       * //codeGenerator.genCodeLine("          }"); //}
       * //codeGenerator.genCodeLine("        }");
       * //codeGenerator.genCodeLine("      }");
       * //codeGenerator.genCodeLine("    }");
       * //codeGenerator.genCodeLine("    for (int i = 0; i < " + tokenCount +
       * "; i++) {"); //codeGenerator.genCodeLine("      if (la1tokens[i]) {");
       * //codeGenerator.genCodeLine("        jj_expentry = new int[1];");
       * //codeGenerator.genCodeLine("        jj_expentry[0] = i;");
       * //codeGenerator.genCodeLine("        jj_expentries.add(jj_expentry);");
       * //codeGenerator.genCodeLine("      }");
       * //codeGenerator.genCodeLine("    }"); //if (jj2index != 0) {
       * //codeGenerator.genCodeLine("    jj_endpos = 0;");
       * //codeGenerator.genCodeLine("    jj_rescan_token();");
       * //codeGenerator.genCodeLine("    jj_add_error_token(0, 0);"); //}
       * //codeGenerator.genCodeLine("    int exptokseq[][1] = new int[1];");
       * //codeGenerator.
       * genCodeLine("    for (int i = 0; i < jj_expentries.size(); i++) {");
       * //if (!Options.getGenerateGenerics()) //codeGenerator.
       * genCodeLine("      exptokseq[i] = (int[])jj_expentries.get(i);");
       * //else //codeGenerator.
       * genCodeLine("      exptokseq[i] = jj_expentries.get(i);");
       * //codeGenerator.genCodeLine("    }");
       * codeGenerator.genCodeLine("    return new _ParseException();");//token,
       * nullptr, tokenImages);"); codeGenerator.genCodeLine(" }");
       */
    } else {
      codeGenerator.println("protected:");
      codeGenerator.println("  /** Generate ParseException. */");
      codeGenerator.generateMethodDefHeader("virtual void ", context.globals().cu_name, "parseError()");
      codeGenerator.println("   {");
      if (Options.getErrorReporting()) {
          codeGenerator.println(
                  "      JJERR << "
                  + "JJWIDE(Parse error at : ) << token->beginLine() << JJWIDE(:) "
                  + "<< token->beginColumn() << JJWIDE( after token: ) << addUnicodeEscapes(token->image())"
                  + " << JJWIDE( encountered: ) << addUnicodeEscapes(getToken(1)->image()) << std::endl;");
      }
      codeGenerator.println("   }");
      /*
       * generateMethodDefHeader("ParseException", cu_name,
       * "generateParseException()"); codeGenerator.genCodeLine("   {");
       * codeGenerator.genCodeLine("    Token* errortok = token->next();"); if
       * (Options.getKeepLineColumn()) codeGenerator.
       * genCodeLine("    int line = errortok.beginLine, column = errortok.beginColumn;"
       * ); codeGenerator.
       * genCodeLine("    JJString mess = (errortok->kind() == 0) ? tokenImages[0] : errortok->image();"
       * ); if (Options.getKeepLineColumn())
       * codeGenerator.genCodeLine("    return new _ParseException();");// +
       * //"\"Parse error at line \" + line + \", column \" + column + \".  " +
       * //"Encountered: \" + mess);"); else
       * codeGenerator.genCodeLine("    return new _ParseException();");//
       * \"Parse error at <unknown location>.  " +
       * //"Encountered: \" + mess);"); codeGenerator.genCodeLine("  }");
       */
    }
    codeGenerator.println("");

    codeGenerator.switchToIncludeFile();
    codeGenerator.println("private:");
    codeGenerator.println("  int  indent; // trace indentation");
    codeGenerator.println("  bool trace = " + Options.getDebugParser() + ";");
    codeGenerator.println("  bool trace_la = " + Options.getDebugLookahead() + ";" );
    codeGenerator.println("");
    codeGenerator.println("public:");
    codeGenerator.generateMethodDefHeader("  bool", context.globals().cu_name, "trace_enabled()");
    codeGenerator.println("  {");
    codeGenerator.println("    return trace;");
    codeGenerator.println("  }");
    codeGenerator.println("");
    codeGenerator.generateMethodDefHeader("  bool", context.globals().cu_name, "trace_la_enabled()");
    codeGenerator.println("  {");
    codeGenerator.println("    return trace_la;");
    codeGenerator.println("  }");
    codeGenerator.println("");
    if (Options.getDebugParser()) {
      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "enable_tracing()");
      codeGenerator.println("{");
      codeGenerator.println("    trace = true;");
      codeGenerator.println("}");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "disable_tracing()");
      codeGenerator.println("{");
      codeGenerator.println("    trace = false;");
      codeGenerator.println("}");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_call(const char *s)");
      codeGenerator.println("  {");
      codeGenerator.println("    if (trace_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.println("      JJLOG << \"Call:   \" << s << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("    indent += 2;");
      codeGenerator.println("  }");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_return(const char *s)");
      codeGenerator.println("  {");
      codeGenerator.println("    indent -= 2;");
      codeGenerator.println("    if (trace_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.println("      JJLOG << \"Return: \" << s << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("  }");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_token(const Token* token, const char* where)");
      codeGenerator.println("  {");
      codeGenerator.println("    if (trace_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.print("      JJLOG << JJWIDE(Consumed token: ) << addUnicodeEscapes(");
      codeGenerator.print(getTokenLabels());
      codeGenerator.println("[token->kind()]) << JJCOMMA << JJSPACE << JJQUOTE << addUnicodeEscapes(token->image()) << JJQUOTE;");
      codeGenerator.println(
              "      JJLOG << JJSPACE << JJWIDE(at) << JJSPACE << token->beginLine() << JJWIDE(:) << token->beginColumn() << *where << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("  }");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_scan(const Token* token, int t2)");
      codeGenerator.println("  {");
      codeGenerator.println("    if (trace_la_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.print("      JJLOG << JJWIDE(Visited  token: ) << addUnicodeEscapes(");
      codeGenerator.print(getTokenLabels());
      codeGenerator.println("[token->kind()]) << JJCOMMA << JJSPACE << JJQUOTE << addUnicodeEscapes(token->image()) << JJQUOTE;");
      codeGenerator.println(
          "      JJLOG << JJSPACE << JJWIDE(at) << JJSPACE << token->beginLine() << JJWIDE(:) << token->beginColumn() << JJCOMMA << JJSPACE << JJWIDE(Expected token: ) << addUnicodeEscapes("
              + getTokenLabels() + "[t2]) << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("  }");
      codeGenerator.println("");
      
    } else {
        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "enable_tracing()");
        codeGenerator.println("  {");
        codeGenerator.println("  }");
        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "disable_tracing()");
        codeGenerator.println("  {");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }

    if (Options.getDebugLookahead()) {
        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "enable_la_tracing()");
        codeGenerator.println("{");
        codeGenerator.println("    trace_la = true;");
        codeGenerator.println("}");
        codeGenerator.println("");

        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "disable_la_tracing()");
        codeGenerator.println("{");
        codeGenerator.println("    trace_la = false;");
        codeGenerator.println("}");
        codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_la_call(const char *s)");
      codeGenerator.println("  {");
      codeGenerator.println("    if (trace_la_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.println("      JJLOG << \"Call:   \" << s << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("    indent += 2;");
      codeGenerator.println("  }");
      codeGenerator.println("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "trace_la_return(const char *s)");
      codeGenerator.println("  {");
      codeGenerator.println("    indent -= 2;");
      codeGenerator.println("    if (trace_la_enabled()) {");
      codeGenerator.println("      for (int no = 0; no < indent; no++) { JJLOG << JJSPACE; }");
      codeGenerator.println("      JJLOG << \"Return: \" << s << std::endl;");
      codeGenerator.println("    }");
      codeGenerator.println("  }");
      codeGenerator.println("");
    } else {
        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "enable_la_tracing()");
        codeGenerator.println("  {");
        codeGenerator.println("  }");
        codeGenerator.switchToIncludeFile();
        codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "disable_la_tracing()");
        codeGenerator.println("  {");
        codeGenerator.println("  }");
        codeGenerator.println("");
      }

    if ((context.globals().jj2index != 0) && Options.getErrorReporting()) {
      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "jj_rescan_token()");
      codeGenerator.println("{");
      codeGenerator.println("    jj_rescan = true;");
      codeGenerator.println("    for (int i = 0; i < " + context.globals().jj2index + "; i++) {");
      // codeGenerator.genCodeLine(" try {");
      codeGenerator.println("      JJCalls *p = &jj_2_rtns[i];");
      codeGenerator.println("      do {");
      codeGenerator.println("        if (p->gen > jj_gen) {");
      codeGenerator.println("          jj_la = p->arg; jj_lastpos = jj_scanpos = p->first;");
      codeGenerator.println("          switch (i) {");
      for (int i = 0; i < context.globals().jj2index; i++) {
        codeGenerator.println("            case " + i + ": jj_3_" + (i + 1) + "(); break;");
      }
      codeGenerator.println("          }");
      codeGenerator.println("        }");
      codeGenerator.println("        p = p->next;");
      codeGenerator.println("      } while (p != nullptr);");
      // codeGenerator.genCodeLine(" } catch(LookaheadSuccess ls) { }");
      codeGenerator.println("    }");
      codeGenerator.println("    jj_rescan = false;");
      codeGenerator.println("  }");
      codeGenerator.println("");

      codeGenerator.generateMethodDefHeader("  void", context.globals().cu_name, "jj_save(int index, int xla)");
      codeGenerator.println("{");
      codeGenerator.println("    JJCalls *p = &jj_2_rtns[index];");
      codeGenerator.println("    while (p->gen > jj_gen) {");
      codeGenerator.println("      if (p->next == nullptr) { p = p->next = new JJCalls(); break; }");
      codeGenerator.println("      p = p->next;");
      codeGenerator.println("    }");
      codeGenerator.println("    p->gen = jj_gen + xla - jj_la; p->first = token; p->arg = xla;");
      codeGenerator.println("  }");
      codeGenerator.println("");
    }

    if (context.globals().cu_from_insertion_point_2.size() != 0) {
      Token t = null;
      codeGenerator.printTokenSetup((context.globals().cu_from_insertion_point_2.get(0)));
      for (Iterator<Token> it = context.globals().cu_from_insertion_point_2.iterator(); it.hasNext();) {
        t = it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    codeGenerator.println("");

    // in the include file close the class signature
    codeGenerator.switchToIncludeFile();

    // copy other stuff
    Token t1 = context.globals().otherLanguageDeclTokenBeg;
    Token t2 = context.globals().otherLanguageDeclTokenEnd;
    while (t1 != t2) {
      codeGenerator.printToken(t1);
      t1 = t1.next;
    }
    codeGenerator.println("\n");
    if (context.globals().jjtreeGenerated) {
      codeGenerator.println("  JJT" + context.globals().cu_name + "State jjtree;");
    }
    codeGenerator.println("private:");
    codeGenerator.println("  bool jj_done;");

    codeGenerator.println("};");
  }

@Override
  public void finish(CodeGeneratorSettings settings, ParserData parserData) {
    if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
      codeGenerator.println(Options.stringValue("NAMESPACE_CLOSE"));
      codeGenerator.println("#endif");
      codeGenerator.switchToMainFile();
      codeGenerator.println(Options.stringValue("NAMESPACE_CLOSE"));
    } else {
        codeGenerator.println("#endif");
    }

    try {
      codeGenerator.close();
    } catch (IOException e) {
      throw new Error(e);
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
                retval += "jj_nt->kind()";
                retval += ") {\u0001";
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
                String s = addTokenNamespace(context.globals().names_of_tokens.get(Integer.valueOf(i)));
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
        String amount;
        if (la.getAmount() == Integer.MAX_VALUE) {
            amount = "INT_MAX";
        } else {
            amount = Integer.toString(la.getAmount());
        }
        
        retval += "jj_2" + internalNames.get(la.getLaExpansion()) + "(" + amount + ")";
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

  // Print method header and return the ERROR_RETURN string.
  private String generateCPPMethodheader(BNFProduction p, Token t) {
    StringBuffer sig = new StringBuffer();
    String ret, params;

    String method_name = p.getLhs();
    boolean void_ret = false;
    boolean ptr_ret = false;

    codeGenerator.printTokenSetup(t);
    codeGenerator.getLeadingComments(t);
    sig.append(t.image);
    if (t.kind == JavaCCParserConstants.VOID) {
      void_ret = true;
    }
    if (t.kind == JavaCCParserConstants.STAR) {
      ptr_ret = true;
    }

    for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
      t = p.getReturnTypeTokens().get(i);
      sig.append(CodeBuilder.toString(t));
      if (t.kind == JavaCCParserConstants.VOID) {
        void_ret = true;
      }
      if (t.kind == JavaCCParserConstants.STAR) {
        ptr_ret = true;
      }
    }

    codeGenerator.getTrailingComments(t);
    ret = sig.toString();

    sig.setLength(0);
    sig.append("(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup(p.getParameterListTokens().get(0));
      for (Iterator<Token> it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = it.next();
        sig.append(CodeBuilder.toString(t));
      }
      sig.append(codeGenerator.getTrailingComments(t));
    }
    sig.append(")");
    params = sig.toString();

    // For now, just ignore comments
    codeGenerator.generateMethodDefHeader(ret, context.globals().cu_name, p.getLhs() + params, sig.toString());

    // Generate a default value for error return.
    String default_return;
    if (ptr_ret) {
      default_return = "NULL";
    } else if (void_ret) {
      default_return = "";
    } else {
      default_return = "0"; // 0 converts to most (all?) basic types.
    }

    StringBuffer ret_val = new StringBuffer("\n#if !defined ERROR_RET_" + method_name + "\n");
    ret_val.append("#define ERROR_RET_" + method_name + " " + default_return + "\n");
    ret_val.append("#endif\n");
    ret_val.append("#define __ERROR_RET__ ERROR_RET_" + method_name + "\n");

    return ret_val.toString();
  }


  private void genStackCheck(boolean voidReturn) {
    if (Options.getDepthLimit() > 0) {
      if (!voidReturn) {
        codeGenerator.println("if(jj_depth_error){ return __ERROR_RET__; }");
      } else {
        codeGenerator.println("if(jj_depth_error){ return; }");
      }
      codeGenerator.println("__jj_depth_inc __jj_depth_counter(this);");
      codeGenerator.println("if(jj_depth > " + Options.getDepthLimit() + ") {");
      codeGenerator.println("  jj_depth_error = true;");
      codeGenerator.println("  jj_consume_token(-1);");
      codeGenerator
      .println("  errorHandler->parseError(token, getToken(1), __FUNCTION__), hasError = true;");
      if (!voidReturn) {
        codeGenerator.println("  return __ERROR_RET__;"); // Non-recoverable
        // error
      } else {
        codeGenerator.println("  return;"); // Non-recoverable error
      }
      codeGenerator.println("}");
    }
  }

  private void buildPhase1Routine(BNFProduction p) {
    Token t = p.getReturnTypeTokens().get(0);
    boolean voidReturn = false;
    if (t.kind == JavaCCParserConstants.VOID) {
      voidReturn = true;
    }
    String error_ret = null;
    error_ret = generateCPPMethodheader(p, t);

    codeGenerator.print(" {");

    if ((Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) && (error_ret != null))
        || ((Options.getDepthLimit() > 0) && !voidReturn)) {
      codeGenerator.print(error_ret);
    } else {
      error_ret = null;
    }

    genStackCheck(voidReturn);

    indentamt = 4;
    if (Options.getDebugParser()) {
      codeGenerator.println("");
      codeGenerator.println("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\""
          + codeGenerator.escapeToUnicode(p.getLhs()) + "\"); });");
      codeGenerator.println("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\""
          + codeGenerator.escapeToUnicode(p.getLhs()) + "\"); });");
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
      codeGenerator.println("    throw \"Missing return statement in function\";");
    }
    if (Options.getDebugParser()) {
      codeGenerator.println("    } catch(...) { }");
    }
    if (!voidReturn) {
      codeGenerator.println("assert(false);");
    }

    if (error_ret != null) {
      codeGenerator.println("\n#undef __ERROR_RET__\n");
    }
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
      String tail = e_nrw.rhsToken == null ? ");" : ")->" + e_nrw.rhsToken.image + ";";
      if (e_nrw.label.equals("")) {
        Object label = context.globals().names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          retval += "jj_consume_token(" + addTokenNamespace((String) label + tail);
        } else {
          retval += "jj_consume_token(" + e_nrw.ordinal + tail;
        }
      } else {
        retval += "jj_consume_token(" + addTokenNamespace(e_nrw.label + tail);
      }

      if (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
        retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
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
      if (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
        retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
      }
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
      actions[e_nrw.getChoices().size()] = "\n" + "jj_consume_token(-1);\n"
          + "errorHandler->parseError(token, getToken(1), __FUNCTION__), hasError = true;"
          + (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) ? "return __ERROR_RET__;\n" : "");

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
        if (!context.globals().jjtreeGenerated) {
          // for the last one, if it's an action, we will not protect it.
          Expansion elem = e_nrw.units.get(i);
          if (!(elem instanceof Action) || !(e.parent instanceof BNFProduction) || (i != (e_nrw.units.size() - 1))) {
            wrap_in_block = true;
            retval += "\nif (!hasError) {";
          }
        }
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
      retval += "while (!hasError) {\u0001";
      retval += phase1ExpansionGen(nested_e);
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\ngoto end_label_" + labelIndex + ";";

      retval += buildLookaheadChecker(conds, actions);
      retval += "\u0002\n" + "}";
      retval += "\nend_label_" + labelIndex + ": ;";
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
      retval += "while (!hasError) {\u0001";
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\ngoto end_label_" + labelIndex + ";";
      retval += buildLookaheadChecker(conds, actions);
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      retval += "\nend_label_" + labelIndex + ": ;";
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
          codeGenerator.printTokenSetup(list.get(0));
          for (Iterator<Token> it = list.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeBuilder.toString(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        // retval += " ";
        // t = (Token)(e_nrw.ids.get(i));
        // codeGenerator.printTokenSetup(t);
        // retval += codeGenerator.getStringToPrint(t);
        // retval += codeGenerator.getTrailingComments(t);
        // retval += ") {\u0003\n";
        list = e_nrw.catchblks.get(i);
        if (list.size() != 0) {
          codeGenerator.printTokenSetup((list.get(0)));
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
    codeGenerator.println(" inline bool ", "jj_2" + internalNames.get(e) + "(int xla)");
    codeGenerator.println(" {");
    codeGenerator.println("    jj_la = xla; jj_lastpos = jj_scanpos = token;");

    String ret_suffix = "";
    if (Options.getDepthLimit() > 0) {
      ret_suffix = " && !jj_depth_error";
    }

    codeGenerator.println("    jj_done = false;");
    codeGenerator.println("    return (!jj_3" + internalNames.get(e) + "() || jj_done)" + ret_suffix + ";");
    if (Options.getErrorReporting()) {
      codeGenerator.println(" { jj_save(" + (Integer.parseInt(internalNames.get(e).substring(1)) - 1) + ", xla); }");
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
          "trace_la_return(\"" + codeGenerator.escapeToUnicode(((NormalProduction) jj3_expansion.parent).getLhs())
          + "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private static String getTokenImages() {
    return addTokenNamespace("tokenImages");
  }

  private static String getTokenLabels() {
    return addTokenNamespace("tokenLabels");
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
        RegularExpression re = (RegularExpression) seq;
        String jj_scan_token = "jj_scan_token";
        if (re.label.equals("")) {
          Object label = context.globals().names_of_tokens.get(Integer.valueOf(re.ordinal));
          if (label != null) {
            jj_scan_token += "(" + addTokenNamespace((String) label) + ")";
          } else {
            jj_scan_token += "(" + re.ordinal + ")";
          }
        } else {
          jj_scan_token += "(" + addTokenNamespace(re.label) + ")";
        }
        internalNames.put(e, jj_scan_token);
        return;
      }

      gensymindex++;
      // if (gensymindex == 100)
      // {
      // new Error().codeGenerator.printStackTrace();
      // System.out.println(" ***** seq: " + seq.internal_name + "; size: " +
      // ((Sequence)seq).units.size());
      // }
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
  
  private static String addTokenNamespace(String token) {
      if (token != null) {
          if (!Options.getTokenConstantsNamespace().isEmpty()) {
              token = Options.getTokenConstantsNamespace() + "::" + token;
          }
      }
      return token;
  }

  private String getTokenType() {
      String type = "Token";
      if (Options.getTokenClass().isEmpty()) {
          type = "Token";
      } else {
          type = Options.getTokenClass();
      }
      
      if (!Options.getTokenNamespace().isEmpty()) { 
          type = Options.getTokenNamespace() + "::" + type;
      }
      return type;
  }

  private String getTokenTypePointer() {
      return getTokenType() + "*";
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
      codeGenerator.println(" inline bool ", "jj_3" + internalNames.get(e) + "()");

      codeGenerator.println(" {");
      codeGenerator.println("    if (jj_done) return true;");
      if (Options.getDepthLimit() > 0) {
        codeGenerator.println("#define __ERROR_RET__ true");
      }
      genStackCheck(false);
      xsp_declared = false;
      if (Options.getDebugLookahead() && (e.parent instanceof NormalProduction)) {
        codeGenerator.print("    ");
        if (Options.getErrorReporting()) {
          codeGenerator.print("if (!jj_rescan) ");
        }
        codeGenerator.println("trace_la_call(\"" + codeGenerator.escapeToUnicode(((NormalProduction) e.parent).getLhs())
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
          codeGenerator.println("    if (jj_scan_token(" + addTokenNamespace((String) label) + ")) " + genReturn(true));
        } else {
          codeGenerator.println("    if (jj_scan_token(" + e_nrw.ordinal + ")) " + genReturn(true));
        }
      } else {
        codeGenerator.println("    if (jj_scan_token(" + addTokenNamespace(e_nrw.label) + ")) " + genReturn(true));
      }
      // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos)
      // " + genReturn(false));
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
        // codeGenerator.genCodeLine(" if (jj_3" + ntexp.internal_name + "()) "
        // + genReturn(true));
        codeGenerator.println("    if (" + genjj_3Call(ntexp) + ") " + genReturn(true));
        // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos ==
        // jj_lastpos) " + genReturn(false));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice) e;
      if (e_nrw.getChoices().size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          codeGenerator.println("    Token* xsp;");
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
          // codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "())
          // {");
          // codeGenerator.println(genjj_3Call(nested_seq) + ") {");
          codeGenerator.println(genjj_3Call(nested_seq) + ") {");
          codeGenerator.println("    jj_scanpos = xsp;");
        } else {
          // codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "())
          // " + genReturn(true));
          codeGenerator.println(genjj_3Call(nested_seq) + ") " + genReturn(true));
          // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos ==
          // jj_lastpos) " + genReturn(false));
        }
      }
       for (int i = 1; i < e_nrw.getChoices().size(); i++) {
       // codeGenerator.genCodeLine(" } else if (jj_la == 0 && jj_scanpos ==
       // jj_lastpos) " + genReturn(false));
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

        // System.out.println("minimumSize: line: " + eseq.line + ", column: " +
        // eseq.column + ": " +
        // minimumSize(eseq));//Test Code

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
        codeGenerator.println("    Token* xsp;");
      }
      OneOrMore e_nrw = (OneOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      // codeGenerator.genCodeLine(" if (jj_3" + nested_internalNames.get(e) +
      // "()) " + genReturn(true));
      codeGenerator.println("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos)
      // " + genReturn(false));
      codeGenerator.println("    while (true) {");
      codeGenerator.println("      xsp = jj_scanpos;");
      // codeGenerator.genCodeLine(" if (jj_3" + nested_internalNames.get(e) +
      // "()) { jj_scanpos = xsp; break; }");
      codeGenerator.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos)
      // " + genReturn(false));
      codeGenerator.println("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.println("    Token* xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore) e;
      Expansion nested_e = e_nrw.getExpansion();
      codeGenerator.println("    while (true) {");
      codeGenerator.println("      xsp = jj_scanpos;");
      // codeGenerator.genCodeLine(" if (jj_3" + nested_internalNames.get(e) +
      // "()) { jj_scanpos = xsp; break; }");
      codeGenerator.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      // codeGenerator.genCodeLine(" if (jj_la == 0 && jj_scanpos == jj_lastpos)
      // " + genReturn(false));
      codeGenerator.println("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.println("    Token* xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne) e;
      Expansion nested_e = e_nrw.getExpansion();
      codeGenerator.println("    xsp = jj_scanpos;");
      // codeGenerator.genCodeLine(" if (jj_3" + nested_internalNames.get(e) +
      // "()) jj_scanpos = xsp;");
      codeGenerator.println("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
      // codeGenerator.genCodeLine(" else if (jj_la == 0 && jj_scanpos ==
      // jj_lastpos) " + genReturn(false));
    }
    if (!recursive_call) {
      codeGenerator.println("    " + genReturn(false));
      if (Options.getDepthLimit() > 0) {
        codeGenerator.println("#undef __ERROR_RET__");
      }
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

  private void build(CppCodeBuilder codeGenerator) {
    NormalProduction p;
    CppCodeProduction cp;

    this.codeGenerator = codeGenerator;
    for (Iterator<NormalProduction> prodIterator = context.globals().bnfproductions.iterator(); prodIterator.hasNext();) {
      p = prodIterator.next();
      if (p instanceof CppCodeProduction) {
        cp = (CppCodeProduction) p;

        StringBuffer sig = new StringBuffer();
        String ret, params;
        Token t = null;

        p.getLhs();

        for (Token element : p.getReturnTypeTokens()) {
          t = element;
          CodeBuilder.toString(t);
          sig.append(t.toString());
          sig.append(" ");
        }

        if (t != null) {
          codeGenerator.getTrailingComments(t);
        }
        ret = sig.toString();

        sig.setLength(0);
        sig.append("(");
        if (p.getParameterListTokens().size() != 0) {
          codeGenerator.printTokenSetup(p.getParameterListTokens().get(0));
          for (Iterator<Token> it = p.getParameterListTokens().iterator(); it.hasNext();) {
            t = it.next();
            sig.append(CodeBuilder.toString(t));
          }
          sig.append(codeGenerator.getTrailingComments(t));
        }
        sig.append(")");
        params = sig.toString();

        // For now, just ignore comments
        codeGenerator.generateMethodDefHeader(ret, context.globals().cu_name, p.getLhs() + params, sig.toString());
        codeGenerator.println(" {");
        if (Options.getDebugParser()) {
          codeGenerator.println("");
          codeGenerator.println("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\""
              + codeGenerator.escapeToUnicode(cp.getLhs()) + "\"); });");
          codeGenerator.println("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\""
              + codeGenerator.escapeToUnicode(cp.getLhs()) + "\"); });");
          codeGenerator.println("    try {");
        }
        if (cp.getCodeTokens().size() != 0) {
          codeGenerator.printTokenSetup(cp.getCodeTokens().get(0));
          codeGenerator.printTokenList(cp.getCodeTokens());
        }
        codeGenerator.println("");
        if (Options.getDebugParser()) {
          codeGenerator.println("    } catch(...) { }");
        }
        codeGenerator.println("  }");
        codeGenerator.println("");
      } else if (p instanceof JavaCodeProduction) {
        context.errors().semantic_error("Cannot use JAVACODE productions with C++ output (yet).");
        continue;
      } else {
        buildPhase1Routine((BNFProduction) p);
      }
    }

    codeGenerator.switchToIncludeFile();
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
    codeGenerator.switchToMainFile();
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

