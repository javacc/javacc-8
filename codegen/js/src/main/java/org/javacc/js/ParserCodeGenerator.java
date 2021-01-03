// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.js;

import org.javacc.parser.*;

import java.io.File;
import java.util.*;

/**
 * Generate the parser.
 */
public class ParserCodeGenerator implements org.javacc.parser.ParserCodeGenerator {

  private static final String parserTemplate =
      "/templates/csharp/ParserDriver.template";

  /**
   * These lists are used to maintain expansions for which code generation
   * in phase 2 and phase 3 is required.  Whenever a call is generated to
   * a phase 2 or phase 3 routine, a corresponding entry is added here if
   * it has not already been added.
   * The phase 3 routines have been optimized in version 0.7pre2.  Essentially
   * only those methods (and only those portions of these methods) are
   * generated that are required.  The lookahead amount is used to determine
   * this.  This change requires the use of a hash table because it is now
   * possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads.  The hash table provides a easily searchable
   * capability to determine the previous requests.
   * The phase 3 routines now are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads).  The second step then generates these
   * methods.
   * This optimization and the hashtable makes it look like we do not need
   * the flag "phase3done" any more.  But this has not been removed yet.
   */
  private final List phase2list = new ArrayList();
  private final List phase3list = new ArrayList();
  private final Hashtable phase3table = new Hashtable();

  private int maskIndex = 0;
  private int jj2Index = 0;
  private boolean lookaheadNeeded;
  private List maskVals = new ArrayList();
  private int gensymindex = 0;
  private int indentamt;
  private boolean jj2LA;
  private CodeGenHelper codeGenerator = new CodeGenHelper();
  private int cline = 1;
  private int ccol = 1;

  private final Map<Expansion, String> internalNames = 
      new HashMap<Expansion, String>();
  private final Map<Expansion, Integer> internalIndexes = 
      new HashMap<Expansion, Integer>();
  private ParserData parserData;
 
  @Override
  public void generateCode(
      CodeGeneratorSettings settings, ParserData parserData) {
    this.parserData = parserData;

    String superClass = (String)settings.get(
                             Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    settings.put("parserName", parserData.parserName);
    settings.put("superClass", (superClass == null || superClass.equals(""))
                      ? "" : " :  " + superClass);
    if (Options.getNamespace() != null) {
      settings.put("NAMESPACE", Options.getNamespace());
    }
    try {
      if (Options.getNamespace() != null) {
        codeGenerator.genCodeLine("namespace " + Options.getNamespace() + " {\n");
      }

      codeGenerator.genCode("public partial class " + parserData.parserName + " : ");
      if (settings.containsKey("suerpClass")) {
        codeGenerator.genCode(settings.get("superClass") + ", ");
      }

      codeGenerator.genCodeLine(parserData.parserName + "Constants {");
      codeGenerator.genCodeLine(parserData.decls);

      if (JavaCCGlobals.jjtreeGenerated)
      {
        codeGenerator.genCodeLine("  JJT" + parserData.parserName + "State jjtree = new JJT" + parserData.parserName + "State();");
      }

      processProductions(settings, codeGenerator);
      settings.put("numproductions", internalIndexes.size());
      codeGenerator.writeTemplate(parserTemplate, settings);
      codeGenerator.genCodeLine("\n}");
      if (Options.getNamespace() != null) {
        codeGenerator.genCodeLine("\n}");
      }
      codeGenerator.saveOutput(Options.getOutputDirectory() + File.separator + parserData.parserName + ".cs");
    } catch(Exception e) {
e.printStackTrace();
      assert(false);
    }
  }

  @Override
  public void finish(
      CodeGeneratorSettings settings,
      ParserData parserData) {
  }

  // TODO(sreeni): Fix this mess.
  private void GenerateCodeProduction(
      CodeProduction production,
      CodeGeneratorSettings settings,
      CodeGenHelper codeGenerator) {
    Token t = (production.getReturnTypeTokens().get(0));
    codeGenerator.printTokenSetup(t); ccol = 1;
    codeGenerator.printLeadingComments(t);
    codeGenerator.genCode(
        "  " + (production.getAccessMod() != null ? production.getAccessMod() + " " : ""));
    cline = t.beginLine; ccol = t.beginColumn;
    codeGenerator.printTokenOnly(t);
    for (int i = 1; i < production.getReturnTypeTokens().size(); i++) {
      t = (production.getReturnTypeTokens().get(i));
      codeGenerator.printToken(t);
    }
    codeGenerator.printTrailingComments(t);
    codeGenerator.genCode(" " + production.getLhs() + "(");
    if (production.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup((production.getParameterListTokens().get(0)));
      for (java.util.Iterator it = production.getParameterListTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    codeGenerator.genCode(")");
    for (java.util.Iterator it = production.getThrowsList().iterator(); it.hasNext();) {
      codeGenerator.genCode(", ");
      java.util.List name = (java.util.List)it.next();
      for (java.util.Iterator it2 = name.iterator(); it2.hasNext();) {
        t = (Token)it2.next();
        codeGenerator.genCode(t.image);
      }
    }
    codeGenerator.genCode(" {");
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("    trace_call(\"" + codeGenerator.addUnicodeEscapes(production.getLhs()) + "\");");
      codeGenerator.genCode("    try {");
    }
    if (production.getCodeTokens().size() != 0) {
      codeGenerator.printTokenSetup((production.getCodeTokens().get(0))); cline--;
      codeGenerator.printTokenList(production.getCodeTokens());
    }
    codeGenerator.genCodeLine("");
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("    } finally {");
      codeGenerator.genCodeLine("      trace_return(\"" + codeGenerator.addUnicodeEscapes(production.getLhs()) + "\");");
      codeGenerator.genCodeLine("    }");
    }
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
  }

  private void processProductions(
      CodeGeneratorSettings settings,
      CodeGenHelper codeGenerator) {
    NormalProduction p;
    JavaCodeProduction jp;
    CppCodeProduction cp;
    Token t = null;

    this.codeGenerator = codeGenerator;
    for (Iterator prodIterator = parserData.bnfproductions.iterator();
         prodIterator.hasNext();) {
      p = (NormalProduction)prodIterator.next();
      if (p instanceof CodeProduction) {
        GenerateCodeProduction((CodeProduction)p, settings, codeGenerator);
      } else {
        buildPhase1Routine((BNFProduction)p);
      }
    }

    for (int phase2index = 0; phase2index < phase2list.size(); phase2index++) {
      buildPhase2Routine((Lookahead)(phase2list.get(phase2index)));
    }

    int phase3index = 0;

    while (phase3index < phase3list.size()) {
      for (; phase3index < phase3list.size(); phase3index++) {
        setupPhase3Builds((Phase3Data)(phase3list.get(phase3index)));
      }
    }

    for (java.util.Enumeration enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      buildPhase3Routine((Phase3Data)(enumeration.nextElement()), false);
    }

    codeGenerator.switchToMainFile();
  }

  private String internalName(Expansion e) {
    return internalNames.containsKey(e) ? internalNames.get(e) : "";
  }

  private int internalIndex(Expansion e) {
    return internalIndexes.get(e);
  }

  /**
   * The phase 1 routines generates their output into String's and dumps
   * these String's once for each method.  These String's contain the
   * special characters '\u0001' to indicate a positive indent, and '\u0002'
   * to indicate a negative indent.  '\n' is used to indicate a line terminator.
   * The characters '\u0003' and '\u0004' are used to delineate portions of
   * text where '\n's should not be followed by an indentation.
   */

  /**
   * An array used to store the first sets generated by the following method.
   * A true entry means that the corresponding token is in the first set.
   */
  private boolean[] firstSet;

  /**
   * Sets up the array "firstSet" above based on the Expansion argument
   * passed to it.  Since this is a recursive function, it assumes that
   * "firstSet" has been reset before the first call.
   */
  private void genFirstSet(Expansion exp) {
    if (exp instanceof RegularExpression) {
      firstSet[((RegularExpression)exp).ordinal] = true;
    } else if (exp instanceof NonTerminal) {
      if (!(((NonTerminal)exp).getProd() instanceof CodeProduction))
      {
        genFirstSet(((BNFProduction)(((NonTerminal)exp).getProd())).getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.getChoices().size(); i++) {
        genFirstSet((ch.getChoices().get(i)));
      }
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      Object obj = seq.units.get(0);
      if ((obj instanceof Lookahead) && (((Lookahead)obj).getActionTokens().size() != 0)) {
        jj2LA = true;
      }
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion unit = seq.units.get(i);
        // Javacode productions can not have FIRST sets. Instead we generate the FIRST set
        // for the preceding LOOKAHEAD (the semantic checks should have made sure that
        // the LOOKAHEAD is suitable).
        if (unit instanceof NonTerminal && ((NonTerminal)unit).getProd() instanceof CodeProduction) {
          if (i > 0 && seq.units.get(i-1) instanceof Lookahead) {
            Lookahead la = (Lookahead)seq.units.get(i-1);
            genFirstSet(la.getLaExpansion());
          }
        } else {
          genFirstSet((seq.units.get(i)));
        }
        if (!Semanticize.emptyExpansionExists((seq.units.get(i)))) {
          break;
        }
      }
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      genFirstSet(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      genFirstSet(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      genFirstSet(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      genFirstSet(tb.exp);
    }
  }

  /**
   * Constants used in the following method "buildLookaheadChecker".
   */
  final int NOOPENSTM = 0;
  final int OPENIF = 1;
  final int OPENSWITCH = 2;

  private void dumpLookaheads(Lookahead[] conds, String[] actions) {
    for (int i = 0; i < conds.length; i++) {
      System.err.println("Lookahead: " + i);
      System.err.println(conds[i].dump(0, new HashSet()));
      System.err.println();
    }
  }

  private int switchIndex = 0;
  /**
   * This method takes two parameters - an array of Lookahead's
   * "conds", and an array of String's "actions".  "actions" contains
   * exactly one element more than "conds".  "actions" are Java source
   * code, and "conds" translate to conditions - so lets say
   * "f(conds[i])" is true if the lookahead required by "conds[i]" is
   * indeed the case.  This method returns a string corresponding to
   * the Java code for:
   *
   *   if (f(conds[0]) actions[0]
   *   else if (f(conds[1]) actions[1]
   *   . . .
   *   else actions[action.length-1]
   *
   * A particular action entry ("actions[i]") can be null, in which
   * case, a noop is generated for that action.
   */
  String buildLookaheadChecker(Lookahead[] conds, String[] actions) {

    // The state variables.
    int state = NOOPENSTM;
    int indentAmt = 0;
    boolean[] casedValues = new boolean[parserData.tokenCount];
    String retval = "";
    Lookahead la;
    Token t = null;
    int tokenMaskSize = (parserData.tokenCount-1)/32 + 1;
    int[] tokenMask = null;

    // Iterate over all the conditions.
    int index = 0;
    while (index < conds.length) {

      la = conds[index];
      jj2LA = false;

      if (la.getAmount() == 0 ||
          Semanticize.emptyExpansionExists(la.getLaExpansion())) {

        // This handles the following cases:
        // . If syntactic lookahead is not wanted (and hence explicitly specified
        //   as 0).
        // . If it is possible for the lookahead expansion to recognize the empty
        //   string - in which case the lookahead trivially passes.
        // . If the lookahead expansion has a JAVACODE production that it directly
        //   expands to - in which case the lookahead trivially passes.
        if (la.getActionTokens().size() == 0) {
          // In addition, if there is no semantic lookahead, then the
          // lookahead trivially succeeds.  So break the main loop and
          // treat this case as the default last action.
          break;
        } else {
          // This case is when there is only semantic lookahead
          // (without any preceding syntactic lookahead).  In this
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
            retval += " else {" + "\u0001";
            if (Options.getErrorReporting()) {
              retval += "\njj_la1[" + maskIndex + "] = jj_gen;";
              maskIndex++;
            }
            maskVals.add(tokenMask);
            retval += "\n" + "if (";
            indentAmt++;
          }
          codeGenerator.printTokenSetup((la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }
      } else if (la.getAmount() == 1 && la.getActionTokens().size() == 0) {
        // Special optimal processing when the lookahead is exactly 1, and there
        // is no semantic lookahead.

        if (firstSet == null) {
          firstSet = new boolean[parserData.tokenCount];
        }
        for (int i = 0; i < parserData.tokenCount; i++) {
          firstSet[i] = false;
        }
        // jj2LA is set to false at the beginning of the containing "if" statement.
        // It is checked immediately after the end of the same statement to determine
        // if lookaheads are to be performed using calls to the jj2 methods.
        genFirstSet(la.getLaExpansion());
        // genFirstSet may find that semantic attributes are appropriate for the next
        // token.  In which case, it sets jj2LA to true.
        if (!jj2LA) {

          // This case is if there is no applicable semantic lookahead and the lookahead
          // is one (excluding the earlier cases such as JAVACODE, etc.).
          switch (state) {
          case OPENIF:
            retval += "\u0002\n" + "} else {\u0001";
            // Control flows through to next case.
          case NOOPENSTM:
            retval += "\n" + "int switch_" + ++switchIndex + " = (";
            retval += "(jj_ntk==-1)?jj_ntk_f():jj_ntk);\n\u0001";
            for (int i = 0; i < parserData.tokenCount; i++) {
              casedValues[i] = false;
            }
            indentAmt++;
            tokenMask = new int[tokenMaskSize];
            for (int i = 0; i < tokenMaskSize; i++) {
              tokenMask[i] = 0;
            }
            break;
            // Don't need to do anything if state is OPENSWITCH.
          case OPENSWITCH:
            retval += " else ";
            break;
          }
          retval += "if (false\u0001";
          boolean first = true;
          for (int i = 0; i < parserData.tokenCount; i++) {
            if (firstSet[i]) {
              if (!casedValues[i]) {
                casedValues[i] = true;
                retval += "\u0002\n || switch_" + switchIndex + " == ";
                int j1 = i/32;
                int j2 = i%32;
                tokenMask[j1] |= 1 << j2;
                String s = (parserData.namesOfTokens.get(new Integer(i)));
                if (s == null) {
                  retval += i;
                } else {
                  retval += s;
                }
                retval += "\u0001";
              }
            }
          }
          retval += ") {";
          retval += actions[index];
          retval += "\u0002\n}";
          state = OPENSWITCH;
        }

      } else {
        // This is the case when lookahead is determined through calls to
        // jj2 methods.  The other case is when lookahead is 1, but semantic
        // attributes need to be evaluated.  Hence this crazy control structure.

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
          retval += "else {" + "\u0001";
          if (Options.getErrorReporting()) {
            retval += "\njj_la1[" + maskIndex + "] = jj_gen;";
            maskIndex++;
          }
          maskVals.add(tokenMask);
          retval += "\n" + "if (";
          indentAmt++;
        }
        jj2Index++;

        internalNames.put(la.getLaExpansion(), "_" + jj2Index);;
        internalIndexes.put(la.getLaExpansion(), jj2Index);
        phase2list.add(la);
        retval += "jj_2" + internalName(la.getLaExpansion()) + "(" + la.getAmount() + ")";
        if (la.getActionTokens().size() != 0) {
          // In addition, there is also a semantic lookahead.  So concatenate
          // the semantic check with the syntactic one.
          retval += " && (";
          codeGenerator.printTokenSetup((la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ")";
        }
        retval += ") {\u0001" + actions[index];
        state = OPENIF;
      }

      index++;
    }

    // Generate code for the default case.  Note this may not
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
      retval += " else {" + "\u0001";
      if (Options.getErrorReporting()) {
        retval += "\njj_la1[" + maskIndex + "] = jj_gen;";
        maskVals.add(tokenMask);
        maskIndex++;
      }
      retval += actions[index];
      break;
    }
    for (int i = 0; i < indentAmt; i++) {
      retval += "\u0002\n}";
    }

    return retval;
  }

  void dumpFormattedString(String str) {
    char ch = ' ';
    char prevChar;
    boolean indentOn = true;
    for (int i = 0; i < str.length(); i++) {
      prevChar = ch;
      ch = str.charAt(i);
      if (ch == '\n' && prevChar == '\r') {
        // do nothing - we've already printed a new line for the '\r'
        // during the previous iteration.
      } else if (ch == '\n' || ch == '\r') {
        if (indentOn) {
          phase1NewLine();
        } else {
          codeGenerator.genCodeLine("");
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
        codeGenerator.genCode(ch);
      }
    }
  }

  void buildPhase1Routine(BNFProduction p) {
    Token t;
    t = (p.getReturnTypeTokens().get(0));
    boolean voidReturn = t.kind == JavaCCParserConstants.VOID;
    codeGenerator.printTokenSetup(t); ccol = 1;
    codeGenerator.printLeadingComments(t);
    codeGenerator.genCode((p.getAccessMod() != null ? p.getAccessMod() : "public")+ " ");
    cline = t.beginLine; ccol = t.beginColumn;
    codeGenerator.printTokenOnly(t);
    for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
      t = (p.getReturnTypeTokens().get(i));
      codeGenerator.printToken(t);
    }
    codeGenerator.printTrailingComments(t);
    codeGenerator.genCode(" " + p.getLhs() + "(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup((p.getParameterListTokens().get(0)));
      for (Iterator it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    codeGenerator.genCode(")");

    for (Iterator it = p.getThrowsList().iterator(); it.hasNext();) {
      codeGenerator.genCode(", ");
      java.util.List name = (java.util.List)it.next();
      for (Iterator it2 = name.iterator(); it2.hasNext();) {
        t = (Token)it2.next();
        codeGenerator.genCode(t.image);
      }
    }

    codeGenerator.genCode(" {");

    indentamt = 4;
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("    trace_call(\"" + codeGenerator.addUnicodeEscapes(p.getLhs()) + "\");");
      codeGenerator.genCodeLine("    try {");
      indentamt = 6;
    }
    
    if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) &&
        p.getDeclarationTokens().size() != 0) {
      codeGenerator.printTokenSetup((p.getDeclarationTokens().get(0)));
      cline--;
      for (Iterator it = p.getDeclarationTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    
    String code = phase1ExpansionGen(p.getExpansion());
    dumpFormattedString(code);
    codeGenerator.genCodeLine("");
    
    if (p.isJumpPatched() && !voidReturn) {
      codeGenerator.genCodeLine("    throw new System.Exception(\"Missing return statement in function\");");
    }
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("    } finally {");
      codeGenerator.genCodeLine("      trace_return(\"" + codeGenerator.addUnicodeEscapes(p.getLhs()) + "\");");
      codeGenerator.genCodeLine("    }");
    }

    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");
  }

  void phase1NewLine() {
    codeGenerator.genCodeLine("");
    for (int i = 0; i < indentamt; i++) {
      codeGenerator.genCode(" ");
    }
  }

  String phase1ExpansionGen(Expansion e) {
    String retval = "";
    Token t = null;
    Lookahead[] conds;
    String[] actions;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        codeGenerator.printTokenSetup((e_nrw.lhsTokens.get(0)));
        for (Iterator it = e_nrw.lhsTokens.iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      // We allow things like String s = <MYTOKEN>.image
      String tail = e_nrw.rhsToken == null ? "" : "." + e_nrw.rhsToken.image;
      Object label = e_nrw.label;
      if (label.equals("")) {
        // See if there is a name given.
        label = parserData.namesOfTokens.get(new Integer(e_nrw.ordinal));
      }
      if (label == null) {
        label = e_nrw.ordinal;
      }
      retval += "jj_consume_token(" + label + ")" + tail + ";";
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      retval += "\n";
      if (e_nrw.getLhsTokens().size() != 0) {
        codeGenerator.printTokenSetup((e_nrw.getLhsTokens().get(0)));
        for (Iterator it = e_nrw.getLhsTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      retval += e_nrw.getName() + "(";
      if (e_nrw.getArgumentTokens().size() != 0) {
        codeGenerator.printTokenSetup((e_nrw.getArgumentTokens().get(0)));
        for (Iterator it = e_nrw.getArgumentTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += ");";
    } else if (e instanceof Action) {
      Action e_nrw = (Action)e;
      retval += "\u0003\n";
      if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) &&
          e_nrw.getActionTokens().size() != 0) {
        codeGenerator.printTokenSetup((e_nrw.getActionTokens().get(0))); ccol = 1;
        for (Iterator it = e_nrw.getActionTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += "\u0004";
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      conds = new Lookahead[e_nrw.getChoices().size()];
      actions = new String[e_nrw.getChoices().size() + 1];
      actions[e_nrw.getChoices().size()] = "\n" + "jj_consume_token(-1);\n" + "throw new ParseException();" ;

      // In previous line, the "throw" never throws an exception since the
      // evaluation of jj_consume_token(-1) causes ParseException to be
      // thrown first.
      Sequence nestedSeq;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nestedSeq = (Sequence)(e_nrw.getChoices().get(i));
        actions[i] = phase1ExpansionGen(nestedSeq);
        conds[i] = (Lookahead)(nestedSeq.units.get(0));
      }
      retval = buildLookaheadChecker(conds, actions);
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        retval += phase1ExpansionGen((e_nrw.units.get(i)));
      }
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
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
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
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
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
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
      TryBlock e_nrw = (TryBlock)e;
      Expansion nested_e = e_nrw.exp;
      java.util.List list;
      retval += "\n";
      retval += "try {\u0001";
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      for (int i = 0; i < e_nrw.catchblks.size(); i++) {
        retval += " catch (";
        list = (e_nrw.catchblks.get(i));
        if (list.size() != 0) {
          codeGenerator.printTokenSetup((Token)(list.get(0))); ccol = 1;
          for (Iterator it = list.iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
      if (e_nrw.finallyblk != null) {
        retval += " finally {\u0003\n";

        if (e_nrw.finallyblk.size() != 0) {
          codeGenerator.printTokenSetup((e_nrw.finallyblk.get(0))); ccol = 1;
          for (Iterator it = e_nrw.finallyblk.iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
    }
    return retval;
  }

  void buildPhase2Routine(Lookahead la) {
    Expansion e = la.getLaExpansion();
    codeGenerator.genCodeLine(
        "private bool jj_2" + internalName(e) + "(int xla)");
    codeGenerator.genCodeLine(" {");
    codeGenerator.genCodeLine("    jj_la = xla; jj_lastpos = jj_scanpos = token;");

    codeGenerator.genCodeLine("    jj_done = false;");
    codeGenerator.genCodeLine("    if (!jj_3" + internalName(e) + "() || jj_done) return true;");
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("jj_save(" + internalIndex(e) + ", xla);");
    }
    codeGenerator.genCodeLine("return false;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    Phase3Data p3d = new Phase3Data(e, la.getAmount());
    phase3list.add(p3d);
    phase3table.put(e, p3d);
  }

  private boolean xsp_declared;

  Expansion jj3_expansion;

  String genReturn(boolean value) {
    String retval = (value ? "true" : "false");
    if (Options.getDebugLookahead() && jj3_expansion != null) {
      String tracecode = "trace_return(\"" + codeGenerator.addUnicodeEscapes(((NormalProduction)jj3_expansion.parent).getLhs()) +
      "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private void generate3R(Expansion e, Phase3Data inf)
  {
    Expansion seq = e;
    if (internalName(e).equals(""))
    {
      while (true)
      {
        if (seq instanceof Sequence && ((Sequence)seq).units.size() == 2)
        {
          seq = ((Sequence)seq).units.get(1);
        }
        else if (seq instanceof NonTerminal)
        {
          NonTerminal e_nrw = (NonTerminal)seq;
          NormalProduction ntprod = parserData.productionTable.get(e_nrw.getName());
          if (ntprod instanceof CodeProduction)
          {
            break; // nothing to do here
          }
          else
          {
            seq = ntprod.getExpansion();
          }
        }
        else
          break;
      }

      if (seq instanceof RegularExpression)
      {
        internalNames.put(e, "jj_scan_token(" + ((RegularExpression)seq).ordinal + ")");
        return;
      }

      gensymindex++;
      internalNames.put(e, "R_" + gensymindex);
      internalIndexes.put(e, gensymindex);
    }
    Phase3Data p3d = (Phase3Data)(phase3table.get(e));
    if (p3d == null || p3d.count < inf.count) {
      p3d = new Phase3Data(e, inf.count);
      phase3list.add(p3d);
      phase3table.put(e, p3d);
    }
  }

  void setupPhase3Builds(Phase3Data inf) {
    Expansion e = inf.exp;
    if (e instanceof RegularExpression) {
      ; // nothing to here
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (parserData.productionTable.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        ; // nothing to do here
      } else {
        generate3R(ntprod.getExpansion(), inf);
      }
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        generate3R((e_nrw.getChoices().get(i)), inf);
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (e_nrw.units.get(i));
        setupPhase3Builds(new Phase3Data(eseq, cnt));
        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      setupPhase3Builds(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      generate3R(e_nrw.expansion, inf);
    }
  }

  private String getTypeForToken() {
    return "Token";
  }

  private String genjj_3Call(Expansion e)
  {
    String name = internalName(e);
    if (name.startsWith("jj_scan_token"))
      return name;
    else
      return "jj_3" + name + "()";
  }

  Hashtable generated = new Hashtable();
  void buildPhase3Routine(Phase3Data inf, boolean recursive_call) {
    Expansion e = inf.exp;
    Token t = null;
    String name =internalName(e);
    if (name.startsWith("jj_scan_token"))
      return;

    if (!recursive_call) {
      codeGenerator.genCodeLine("private bool jj_3" + name + "()");

      codeGenerator.genCodeLine(" {");
      codeGenerator.genCodeLine("    if (jj_done) return true;");
      xsp_declared = false;
      if (Options.getDebugLookahead() && e.parent instanceof NormalProduction) {
        codeGenerator.genCode("    ");
        if (Options.getErrorReporting()) {
          codeGenerator.genCode("if (!jj_rescan) ");
        }
        codeGenerator.genCodeLine("trace_call(\"" + codeGenerator.addUnicodeEscapes(((NormalProduction)e.parent).getLhs()) + "(LOOKING AHEAD...)\");");
        jj3_expansion = e;
      } else {
        jj3_expansion = null;
      }
    }
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      Object label = e_nrw.label;
      if (label.equals("")) {
        // See if there is a name given.
        label = parserData.namesOfTokens.get(new Integer(e_nrw.ordinal));
      }
      if (label == null) {
        label = e_nrw.ordinal;
      }
      codeGenerator.genCodeLine("    if (jj_scan_token(" + label + ")) " + genReturn(true));
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (parserData.productionTable.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        codeGenerator.genCodeLine("    if (true) { jj_la = 0; jj_scanpos = jj_lastpos; " + genReturn(false) + "}");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        codeGenerator.genCodeLine("    if (" + genjj_3Call(ntexp)+ ") " + genReturn(true));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      if (e_nrw.getChoices().size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
        }
        codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      }
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nested_seq = (Sequence)(e_nrw.getChoices().get(i));
        Lookahead la = (Lookahead)(nested_seq.units.get(0));
        if (la.getActionTokens().size() != 0) {
          // We have semantic lookahead that must be evaluated.
          lookaheadNeeded = true;
          codeGenerator.genCodeLine("    jj_lookingAhead = true;");
          codeGenerator.genCode("    jj_semLA = ");
          codeGenerator.printTokenSetup((la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            codeGenerator.printToken(t);
          }
          codeGenerator.printTrailingComments(t);
          codeGenerator.genCodeLine(";");
          codeGenerator.genCodeLine("    jj_lookingAhead = false;");
        }
        codeGenerator.genCode("    if (");
        if (la.getActionTokens().size() != 0) {
          codeGenerator.genCode("!jj_semLA || ");
        }
        if (i != e_nrw.getChoices().size() - 1) {
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") {");
          codeGenerator.genCodeLine("    jj_scanpos = xsp;");
        } else {
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") " + genReturn(true));
        }
      }
      for (int i = 1; i < e_nrw.getChoices().size(); i++) {
        codeGenerator.genCodeLine("    }");
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (e_nrw.units.get(i));
        buildPhase3Routine(new Phase3Data(eseq, cnt), true);
        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Routine(new Phase3Data(e_nrw.exp, inf.count), true);
    } else if (e instanceof OneOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
    }
    if (!recursive_call) {
      codeGenerator.genCodeLine("    " + genReturn(false));
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }
  }

  int minimumSize(Expansion e) {
    return minimumSize(e, Integer.MAX_VALUE);
  }

  /*
   * Returns the minimum number of tokens that can parse to this expansion.
   */
  int minimumSize(Expansion e, int oldMin) {
    int retval = 0;  // should never be used.  Will be bad if it is.
    if (e.inMinimumSize) {
      // recursive search for minimum size unnecessary.
      return Integer.MAX_VALUE;
    }
    e.inMinimumSize = true;
    if (e instanceof RegularExpression) {
      retval = 1;
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (parserData.productionTable.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        retval = Integer.MAX_VALUE;
        // Make caller think this is unending (for we do not go beyond JAVACODE during
        // phase3 execution).
      } else {
        Expansion ntexp = ntprod.getExpansion();
        retval = minimumSize(ntexp);
      }
    } else if (e instanceof Choice) {
      int min = oldMin;
      Expansion nested_e;
      Choice e_nrw = (Choice)e;
      for (int i = 0; min > 1 && i < e_nrw.getChoices().size(); i++) {
        nested_e = (e_nrw.getChoices().get(i));
        int min1 = minimumSize(nested_e, min);
        if (min > min1) min = min1;
      }
      retval = min;
    } else if (e instanceof Sequence) {
      int min = 0;
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (e_nrw.units.get(i));
        int mineseq = minimumSize(eseq);
        if (min == Integer.MAX_VALUE || mineseq == Integer.MAX_VALUE) {
          min = Integer.MAX_VALUE; // Adding infinity to something results in infinity.
        } else {
          min += mineseq;
          if (min > oldMin)
            break;
        }
      }
      retval = min;
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      retval = minimumSize(e_nrw.exp);
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      retval = minimumSize(e_nrw.expansion);
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
   * This is the number of tokens that can still be consumed.  This
   * number is used to limit the number of jj3 methods generated.
   */
  int count;

  Phase3Data(Expansion e, int c) {
    exp = e;
    count = c;
  }
}
