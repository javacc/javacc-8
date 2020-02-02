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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generate lexer.
 */
public class LexGen {

  // Hashtable of vectors
  private Hashtable<String, List<TokenProduction>> allTpsForState = new Hashtable<>();

  private int[]                                    kinds;
  private int                                      maxOrdinal     = 1;
  private String[]                                 newLexState;
  private Action[]                                 actions;
  private Hashtable<String, NfaState>              initStates     = new Hashtable<>();
  private int                                      stateSetSize;
  private int                                      totalNumStates;
  private int                                      maxLexStates;

  private NfaState[]                               singlesToSkip;
  private long[]                                   toSkip;
  private long[]                                   toSpecial;
  private long[]                                   toMore;
  private long[]                                   toToken;
  private int                                      defaultLexState;
  private RegularExpression[]                      rexprs;
  private int[]                                    initMatch;
  private boolean[]                                canLoop;
  private boolean[]                                canReachOnMore;
  private boolean[]                                hasNfa;
  private NfaState                                 initialState;

  public LexGen() {
    actions = null;
    allTpsForState = new Hashtable<>();
    canLoop = null;
    canReachOnMore = null;
    defaultLexState = 0;
    hasNfa = null;
    initMatch = null;
    initStates = new Hashtable<>();
    initialState = null;
    kinds = null;
    maxLexStates = 0;
    maxOrdinal = 1;
    newLexState = null;
    rexprs = null;
    singlesToSkip = null;
    stateSetSize = 0;
    toMore = null;
    toSkip = null;
    toSpecial = null;
    toToken = null;
    totalNumStates = 0;
  }

  private LexData BuildLexStatesTable() {
    LexData lexData = new LexData();
    Iterator<TokenProduction> it = JavaCCGlobals.rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[JavaCCGlobals.lexstate_I2S.size()];
    while (it.hasNext()) {
      tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      List<TokenProduction> tps;

      for (i = 0; i < tp.lexStates.length; i++) {
        if ((tps = allTpsForState.get(tp.lexStates[i])) == null) {
          tmpLexStateName[maxLexStates++] = tp.lexStates[i];
          allTpsForState.put(tp.lexStates[i], tps = new ArrayList<>());
        }

        tps.add(tp);
      }

      if ((respecs == null) || (respecs.size() == 0)) {
        continue;
      }

      RegularExpression re;
      for (i = 0; i < respecs.size(); i++) {
        if (maxOrdinal <= (re = respecs.get(i).rexp).ordinal) {
          maxOrdinal = re.ordinal + 1;
        }
      }
    }

    kinds = new int[maxOrdinal];
    toSkip = new long[(maxOrdinal / 64) + 1];
    toSpecial = new long[(maxOrdinal / 64) + 1];
    toMore = new long[(maxOrdinal / 64) + 1];
    toToken = new long[(maxOrdinal / 64) + 1];
    toToken[0] = 1L;
    actions = new Action[maxOrdinal];
    actions[0] = JavaCCGlobals.actForEof;
    initStates = new Hashtable<>();
    lexData.canMatchAnyChar = new int[maxLexStates];
    canLoop = new boolean[maxLexStates];
    singlesToSkip = new NfaState[maxLexStates];

    for (i = 0; i < maxLexStates; i++) {
      lexData.canMatchAnyChar[i] = -1;
    }

    hasNfa = new boolean[maxLexStates];
    lexData.mixed = new boolean[maxLexStates];
    initMatch = new int[maxLexStates];
    newLexState = new String[maxOrdinal];
    newLexState[0] = JavaCCGlobals.nextStateForEof;
    lexData.lexStates = new int[maxOrdinal];
    lexData.ignoreCase = new boolean[maxOrdinal];
    rexprs = new RegularExpression[maxOrdinal];
    RStringLiteral.allImages = new String[maxOrdinal];
    canReachOnMore = new boolean[maxLexStates];
    return lexData;
  }

  private int GetIndex(String name, String[] lexStateNames) {
    for (int i = 0; i < lexStateNames.length; i++) {
      if ((lexStateNames[i] != null) && lexStateNames[i].equals(name)) {
        return i;
      }
    }

    throw new Error(); // Should never come here
  }

  public TokenizerData generateTokenizerData(boolean generateDataOnly) throws IOException {
    if (!Options.getBuildTokenManager() || Options.getUserTokenManager() || (JavaCCErrors.get_error_count() > 0)) {
      return new TokenizerData();
    }

    final CodeGenerator codeGenerator = JavaCCGlobals.getCodeGenerator();
    List<RegularExpression> choices = new ArrayList<>();
    TokenProduction tp;
    int i, j;

    LexData lexData = BuildLexStatesTable();

    boolean ignoring = false;

    TokenizerData tokenizerData = new TokenizerData();
    tokenizerData.lexStateNames = new String[maxLexStates];
    for (int l : JavaCCGlobals.lexstate_I2S.keySet()) {
      tokenizerData.lexStateNames[l] = JavaCCGlobals.lexstate_I2S.get(l);
    }

    // while (e.hasMoreElements())
    for (int k = 0; k < tokenizerData.lexStateNames.length; k++) {
      int startState = -1;
      NfaState.ReInit();
      RStringLiteral.ReInit();

      // String key = (String)e.nextElement();
      String key = tokenizerData.lexStateNames[k];

      lexData.lexStateIndex = GetIndex(key, tokenizerData.lexStateNames);
      List<TokenProduction> allTps = allTpsForState.get(key);
      initStates.put(key, initialState = new NfaState(lexData));
      ignoring = false;

      singlesToSkip[lexData.lexStateIndex] = new NfaState(lexData);

      if (key.equals("DEFAULT")) {
        defaultLexState = lexData.lexStateIndex;
      }

      for (i = 0; i < allTps.size(); i++) {
        tp = allTps.get(i);
        int kind = tp.kind;
        boolean ignore = tp.ignoreCase;
        List<RegExprSpec> rexps = tp.respecs;

        if (i == 0) {
          ignoring = ignore;
        }

        for (j = 0; j < rexps.size(); j++) {
          RegExprSpec respec = rexps.get(j);
          lexData.curRE = respec.rexp;

          rexprs[lexData.curKind = lexData.curRE.ordinal] = lexData.curRE;
          lexData.lexStates[lexData.curRE.ordinal] = lexData.lexStateIndex;
          lexData.ignoreCase[lexData.curRE.ordinal] = ignore;

          if (lexData.curRE.private_rexp) {
            kinds[lexData.curRE.ordinal] = -1;
            continue;
          }

          if (!Options.getNoDfa() && (lexData.curRE instanceof RStringLiteral)
              && !((RStringLiteral) lexData.curRE).image.equals("")) {
            ((RStringLiteral) lexData.curRE).GenerateDfa(lexData.curRE.ordinal);
            if ((i != 0) && !lexData.mixed[lexData.lexStateIndex] && (ignoring != ignore)) {
              lexData.mixed[lexData.lexStateIndex] = true;
            }
          } else if (lexData.curRE.CanMatchAnyChar()) {
            if ((lexData.canMatchAnyChar[lexData.lexStateIndex] == -1)
                || (lexData.canMatchAnyChar[lexData.lexStateIndex] > lexData.curRE.ordinal)) {
              lexData.canMatchAnyChar[lexData.lexStateIndex] = lexData.curRE.ordinal;
            }
          } else {
            Nfa temp;

            if (lexData.curRE instanceof RChoice) {
              choices.add(lexData.curRE);
            }

            temp = lexData.curRE.GenerateNfa(ignore, lexData);
            temp.end.isFinal = true;
            temp.end.kind = lexData.curRE.ordinal;
            initialState.AddMove(temp.start);
          }

          if (kinds.length < lexData.curRE.ordinal) {
            int[] tmp = new int[lexData.curRE.ordinal + 1];

            System.arraycopy(kinds, 0, tmp, 0, kinds.length);
            kinds = tmp;
          }
          // System.out.println(" ordina : " + curRE.ordinal);

          kinds[lexData.curRE.ordinal] = kind;

          if ((respec.nextState != null)
              && !respec.nextState.equals(tokenizerData.lexStateNames[lexData.lexStateIndex])) {
            newLexState[lexData.curRE.ordinal] = respec.nextState;
          }

          if ((respec.act != null) && (respec.act.getActionTokens() != null) && (respec.act.getActionTokens().size() > 0)) {
            actions[lexData.curRE.ordinal] = respec.act;
          }

          switch (kind) {
            case TokenProduction.SPECIAL:
              toSpecial[lexData.curRE.ordinal / 64] |= 1L << (lexData.curRE.ordinal % 64);
              toSkip[lexData.curRE.ordinal / 64] |= 1L << (lexData.curRE.ordinal % 64);
              break;
            case TokenProduction.SKIP:
              toSkip[lexData.curRE.ordinal / 64] |= 1L << (lexData.curRE.ordinal % 64);
              break;
            case TokenProduction.MORE:
              toMore[lexData.curRE.ordinal / 64] |= 1L << (lexData.curRE.ordinal % 64);

              if (newLexState[lexData.curRE.ordinal] != null) {
                canReachOnMore[GetIndex(newLexState[lexData.curRE.ordinal], tokenizerData.lexStateNames)] = true;
              } else {
                canReachOnMore[lexData.lexStateIndex] = true;
              }

              break;
            case TokenProduction.TOKEN:
              toToken[lexData.curRE.ordinal / 64] |= 1L << (lexData.curRE.ordinal % 64);
              break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures();

      for (i = 0; i < initialState.epsilonMoves.size(); i++) {
        initialState.epsilonMoves.elementAt(i).GenerateCode();
      }

      if (hasNfa[lexData.lexStateIndex] = (NfaState.generatedStates != 0)) {
        initialState.GenerateCode();
        startState = initialState.GenerateInitMoves();
      }

      if ((initialState.kind != Integer.MAX_VALUE) && (initialState.kind != 0)) {
        if ((initMatch[lexData.lexStateIndex] == 0) || (initMatch[lexData.lexStateIndex] > initialState.kind)) {
          initMatch[lexData.lexStateIndex] = initialState.kind;
        }
      } else if (initMatch[lexData.lexStateIndex] == 0) {
        initMatch[lexData.lexStateIndex] = Integer.MAX_VALUE;
      }

      RStringLiteral.FillSubString(lexData);

      if (hasNfa[lexData.lexStateIndex] && !lexData.mixed[lexData.lexStateIndex]) {
        RStringLiteral.GenerateNfaStartStates(initialState, lexData);
      }

      RStringLiteral.UpdateStringLiteralData(totalNumStates, lexData);
      NfaState.UpdateNfaData(totalNumStates, startState, lexData.lexStateIndex,
          lexData.canMatchAnyChar[lexData.lexStateIndex]);
      totalNumStates += NfaState.generatedStates;
      if (stateSetSize < NfaState.generatedStates) {
        stateSetSize = NfaState.generatedStates;
      }
    }

    for (i = 0; i < choices.size(); i++) {
      ((RChoice) choices.get(i)).CheckUnmatchability(lexData.lexStates);
    }

    CheckEmptyStringMatch(lexData, tokenizerData);

    tokenizerData.setParserName(JavaCCGlobals.cu_name);
    NfaState.BuildTokenizerData(tokenizerData);
    RStringLiteral.BuildTokenizerData(tokenizerData);

    int[] newLexStateIndices = new int[maxOrdinal];
    StringBuilder tokenMgrDecls = new StringBuilder();
    if ((JavaCCGlobals.token_mgr_decls != null) && (JavaCCGlobals.token_mgr_decls.size() > 0)) {
      // Token t = token_mgr_decls.get(0);
      for (j = 0; j < JavaCCGlobals.token_mgr_decls.size(); j++) {
        tokenMgrDecls.append(JavaCCGlobals.token_mgr_decls.get(j).image + " ");
      }
    }
    tokenizerData.setDecls(tokenMgrDecls.toString());
    Map<Integer, String> actionStrings = new HashMap<>();
    for (i = 0; i < maxOrdinal; i++) {
      if (newLexState[i] == null) {
        newLexStateIndices[i] = -1;
      } else {
        newLexStateIndices[i] = GetIndex(newLexState[i], tokenizerData.lexStateNames);
      }
      // For java, we have this but for other languages, eventually we will
      // simply have a string.
      Action act = actions[i];
      if (act == null) {
        continue;
      }
      StringBuilder sb = new StringBuilder();
      for (int k = 0; k < act.getActionTokens().size(); k++) {
        sb.append(act.getActionTokens().get(k).image);
        sb.append(" ");
      }
      actionStrings.put(i, sb.toString());
    }
    tokenizerData.setDefaultLexState(defaultLexState);
    tokenizerData.updateMatchInfo(actionStrings, newLexStateIndices, toSkip, toSpecial, toMore, toToken);
    Map<Integer, String> labels = new HashMap<>();
    String[] images = new String[JavaCCGlobals.rexps_of_tokens.size() + 1];
    for (Integer o : JavaCCGlobals.rexps_of_tokens.keySet()) {
      RegularExpression re = JavaCCGlobals.rexps_of_tokens.get(o);
      String label = re.label;
      if ((label != null) && (label.length() > 0)) {
        labels.put(o, label);
      }
      if (re instanceof RStringLiteral) {
        images[o] = ((RStringLiteral) re).image;
      }
    }
    tokenizerData.setLabelsAndImages(JavaCCGlobals.names_of_tokens, images);

    if (generateDataOnly) {
      return tokenizerData;
    }

    TokenManagerCodeGenerator gen = codeGenerator.getTokenManagerCodeGenerator();
    CodeGeneratorSettings settings = CodeGeneratorSettings.of(Options.getOptions());
    gen.generateCode(settings, tokenizerData);
    gen.finish(settings, tokenizerData);
    return tokenizerData;
  }

  private void CheckEmptyStringMatch(LexData lexData, TokenizerData tokenizerData) {
    int i, j, k, len;
    boolean[] seen = new boolean[maxLexStates];
    boolean[] done = new boolean[maxLexStates];
    String cycle;
    String reList;

    Outer:
      for (i = 0; i < maxLexStates; i++) {
        if (done[i] || (initMatch[i] == 0) || (initMatch[i] == Integer.MAX_VALUE) || (lexData.canMatchAnyChar[i] != -1)) {
          continue;
        }

        done[i] = true;
        len = 0;
        cycle = "";
        reList = "";

        for (k = 0; k < maxLexStates; k++) {
          seen[k] = false;
        }

        j = i;
        seen[i] = true;
        cycle += tokenizerData.lexStateNames[j] + "-->";
        while (newLexState[initMatch[j]] != null) {
          cycle += newLexState[initMatch[j]];
          if (seen[j = GetIndex(newLexState[initMatch[j]], tokenizerData.lexStateNames)]) {
            break;
          }

          cycle += "-->";
          done[j] = true;
          seen[j] = true;
          if ((initMatch[j] == 0) || (initMatch[j] == Integer.MAX_VALUE) || (lexData.canMatchAnyChar[j] != -1)) {
            continue Outer;
          }
          if (len != 0) {
            reList += "; ";
          }
          reList += "line " + rexprs[initMatch[j]].getLine() + ", column " + rexprs[initMatch[j]].getColumn();
          len++;
        }

        if (newLexState[initMatch[j]] == null) {
          cycle += tokenizerData.lexStateNames[lexData.lexStates[initMatch[j]]];
        }

        for (k = 0; k < maxLexStates; k++) {
          canLoop[k] |= seen[k];
        }

        if (len == 0) {
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression"
                  + ((rexprs[initMatch[i]].label.equals("")) ? "" : (" for " + rexprs[initMatch[i]].label))
                  + " can be matched by the empty string (\"\") in lexical state " + tokenizerData.lexStateNames[i]
                      + ". This can result in an endless loop of " + "empty string matches.");
        } else {
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression"
                  + ((rexprs[initMatch[i]].label.equals("")) ? "" : (" for " + rexprs[initMatch[i]].label))
                  + " can be matched by the empty string (\"\") in lexical state " + tokenizerData.lexStateNames[i]
                      + ". This regular expression along with the " + "regular expressions at " + reList
                      + " forms the cycle \n   " + cycle + "\ncontaining regular expressions with empty matches."
                      + " This can result in an endless loop of empty string matches.");
        }
      }
  }


  /**
   * Lexer Context.
   */
  class LexData {

    int               lexStateIndex = 0;
    int               curKind;
    RegularExpression curRE;
    int[]             lexStates;
    int[]             canMatchAnyChar;
    boolean[]         ignoreCase;
    boolean[]         mixed;

    LexData() {
      canMatchAnyChar = null;
      curKind = 0;
      curRE = null;
      ignoreCase = null;
      lexStateIndex = 0;
      lexStates = null;
      mixed = null;
    }
  }
}
