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
  private static Hashtable<String, List<TokenProduction>> allTpsForState = new Hashtable<>();

  private static int[]                                    kinds;
  private static int                                      maxOrdinal     = 1;
  private static String[]                                 newLexState;
  private static Action[]                                 actions;
  private static Hashtable<String, NfaState>              initStates     = new Hashtable<>();
  private static int                                      stateSetSize;
  private static int                                      totalNumStates;
  private static int                                      maxLexStates;

  private static NfaState[]                               singlesToSkip;
  private static long[]                                   toSkip;
  private static long[]                                   toSpecial;
  private static long[]                                   toMore;
  private static long[]                                   toToken;
  private static int                                      defaultLexState;
  private static RegularExpression[]                      rexprs;
  private static int[]                                    initMatch;
  private static boolean[]                                canLoop;
  private static boolean[]                                canReachOnMore;
  private static boolean[]                                hasNfa;
  private static NfaState                                 initialState;

  static int                                              lexStateIndex  = 0;
  static int                                              curKind;
  static RegularExpression                                curRE;
  static int[]                                            lexStates;
  static int[]                                            canMatchAnyChar;
  static boolean[]                                        ignoreCase;
  static boolean[]                                        mixed;

  public static String[]                                  lexStateName;
  public static TokenizerData                             tokenizerData;
  public static boolean                                   generateDataOnly;

  private static void BuildLexStatesTable() {
    Iterator<TokenProduction> it = JavaCCGlobals.rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[JavaCCGlobals.lexstate_I2S.size()];
    while (it.hasNext()) {
      tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      List<TokenProduction> tps;

      for (i = 0; i < tp.lexStates.length; i++) {
        if ((tps = LexGen.allTpsForState.get(tp.lexStates[i])) == null) {
          tmpLexStateName[LexGen.maxLexStates++] = tp.lexStates[i];
          LexGen.allTpsForState.put(tp.lexStates[i], tps = new ArrayList<>());
        }

        tps.add(tp);
      }

      if (respecs == null || respecs.size() == 0) {
        continue;
      }

      RegularExpression re;
      for (i = 0; i < respecs.size(); i++) {
        if (LexGen.maxOrdinal <= (re = respecs.get(i).rexp).ordinal) {
          LexGen.maxOrdinal = re.ordinal + 1;
        }
      }
    }

    LexGen.kinds = new int[LexGen.maxOrdinal];
    LexGen.toSkip = new long[LexGen.maxOrdinal / 64 + 1];
    LexGen.toSpecial = new long[LexGen.maxOrdinal / 64 + 1];
    LexGen.toMore = new long[LexGen.maxOrdinal / 64 + 1];
    LexGen.toToken = new long[LexGen.maxOrdinal / 64 + 1];
    LexGen.toToken[0] = 1L;
    LexGen.actions = new Action[LexGen.maxOrdinal];
    LexGen.actions[0] = JavaCCGlobals.actForEof;
    LexGen.initStates = new Hashtable<>();
    LexGen.canMatchAnyChar = new int[LexGen.maxLexStates];
    LexGen.canLoop = new boolean[LexGen.maxLexStates];
    LexGen.lexStateName = new String[LexGen.maxLexStates];
    LexGen.singlesToSkip = new NfaState[LexGen.maxLexStates];
    // System.arraycopy(tmpLexStateName, 0, lexStateName, 0, maxLexStates);

    for (int l : JavaCCGlobals.lexstate_I2S.keySet()) {
      LexGen.lexStateName[l] = JavaCCGlobals.lexstate_I2S.get(l);
    }

    for (i = 0; i < LexGen.maxLexStates; i++) {
      LexGen.canMatchAnyChar[i] = -1;
    }

    LexGen.hasNfa = new boolean[LexGen.maxLexStates];
    LexGen.mixed = new boolean[LexGen.maxLexStates];
    LexGen.initMatch = new int[LexGen.maxLexStates];
    LexGen.newLexState = new String[LexGen.maxOrdinal];
    LexGen.newLexState[0] = JavaCCGlobals.nextStateForEof;
    LexGen.lexStates = new int[LexGen.maxOrdinal];
    LexGen.ignoreCase = new boolean[LexGen.maxOrdinal];
    LexGen.rexprs = new RegularExpression[LexGen.maxOrdinal];
    RStringLiteral.allImages = new String[LexGen.maxOrdinal];
    LexGen.canReachOnMore = new boolean[LexGen.maxLexStates];
  }

  private static int GetIndex(String name) {
    for (int i = 0; i < LexGen.lexStateName.length; i++) {
      if (LexGen.lexStateName[i] != null && LexGen.lexStateName[i].equals(name)) {
        return i;
      }
    }

    throw new Error(); // Should never come here
  }

  public void start() throws IOException {
    if (!Options.getBuildTokenManager() || Options.getUserTokenManager() || JavaCCErrors.get_error_count() > 0) {
      return;
    }

    final CodeGenerator codeGenerator = JavaCCGlobals.getCodeGenerator();
    List<RegularExpression> choices = new ArrayList<>();
    TokenProduction tp;
    int i, j;

    LexGen.BuildLexStatesTable();

    boolean ignoring = false;

    // while (e.hasMoreElements())
    for (int k = 0; k < LexGen.lexStateName.length; k++) {
      int startState = -1;
      NfaState.ReInit();
      RStringLiteral.ReInit();

      // String key = (String)e.nextElement();
      String key = LexGen.lexStateName[k];

      LexGen.lexStateIndex = LexGen.GetIndex(key);
      List<TokenProduction> allTps = LexGen.allTpsForState.get(key);
      LexGen.initStates.put(key, LexGen.initialState = new NfaState());
      ignoring = false;

      LexGen.singlesToSkip[LexGen.lexStateIndex] = new NfaState();

      if (key.equals("DEFAULT")) {
        LexGen.defaultLexState = LexGen.lexStateIndex;
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
          LexGen.curRE = respec.rexp;

          LexGen.rexprs[LexGen.curKind = LexGen.curRE.ordinal] = LexGen.curRE;
          LexGen.lexStates[LexGen.curRE.ordinal] = LexGen.lexStateIndex;
          LexGen.ignoreCase[LexGen.curRE.ordinal] = ignore;

          if (LexGen.curRE.private_rexp) {
            LexGen.kinds[LexGen.curRE.ordinal] = -1;
            continue;
          }

          if (!Options.getNoDfa() && LexGen.curRE instanceof RStringLiteral
              && !((RStringLiteral) LexGen.curRE).image.equals("")) {
            ((RStringLiteral) LexGen.curRE).GenerateDfa(LexGen.curRE.ordinal);
            if (i != 0 && !LexGen.mixed[LexGen.lexStateIndex] && ignoring != ignore) {
              LexGen.mixed[LexGen.lexStateIndex] = true;
            }
          } else if (LexGen.curRE.CanMatchAnyChar()) {
            if (LexGen.canMatchAnyChar[LexGen.lexStateIndex] == -1
                || LexGen.canMatchAnyChar[LexGen.lexStateIndex] > LexGen.curRE.ordinal) {
              LexGen.canMatchAnyChar[LexGen.lexStateIndex] = LexGen.curRE.ordinal;
            }
          } else {
            Nfa temp;

            if (LexGen.curRE instanceof RChoice) {
              choices.add(LexGen.curRE);
            }

            temp = LexGen.curRE.GenerateNfa(ignore);
            temp.end.isFinal = true;
            temp.end.kind = LexGen.curRE.ordinal;
            LexGen.initialState.AddMove(temp.start);
          }

          if (LexGen.kinds.length < LexGen.curRE.ordinal) {
            int[] tmp = new int[LexGen.curRE.ordinal + 1];

            System.arraycopy(LexGen.kinds, 0, tmp, 0, LexGen.kinds.length);
            LexGen.kinds = tmp;
          }
          // System.out.println(" ordina : " + curRE.ordinal);

          LexGen.kinds[LexGen.curRE.ordinal] = kind;

          if (respec.nextState != null && !respec.nextState.equals(LexGen.lexStateName[LexGen.lexStateIndex])) {
            LexGen.newLexState[LexGen.curRE.ordinal] = respec.nextState;
          }

          if (respec.act != null && respec.act.getActionTokens() != null && respec.act.getActionTokens().size() > 0) {
            LexGen.actions[LexGen.curRE.ordinal] = respec.act;
          }

          switch (kind) {
            case TokenProduction.SPECIAL:
              LexGen.toSpecial[LexGen.curRE.ordinal / 64] |= 1L << (LexGen.curRE.ordinal % 64);
              LexGen.toSkip[LexGen.curRE.ordinal / 64] |= 1L << (LexGen.curRE.ordinal % 64);
              break;
            case TokenProduction.SKIP:
              LexGen.toSkip[LexGen.curRE.ordinal / 64] |= 1L << (LexGen.curRE.ordinal % 64);
              break;
            case TokenProduction.MORE:
              LexGen.toMore[LexGen.curRE.ordinal / 64] |= 1L << (LexGen.curRE.ordinal % 64);

              if (LexGen.newLexState[LexGen.curRE.ordinal] != null) {
                LexGen.canReachOnMore[LexGen.GetIndex(LexGen.newLexState[LexGen.curRE.ordinal])] = true;
              } else {
                LexGen.canReachOnMore[LexGen.lexStateIndex] = true;
              }

              break;
            case TokenProduction.TOKEN:
              LexGen.toToken[LexGen.curRE.ordinal / 64] |= 1L << (LexGen.curRE.ordinal % 64);
              break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures();

      for (i = 0; i < LexGen.initialState.epsilonMoves.size(); i++) {
        LexGen.initialState.epsilonMoves.elementAt(i).GenerateCode();
      }

      if (LexGen.hasNfa[LexGen.lexStateIndex] = (NfaState.generatedStates != 0)) {
        LexGen.initialState.GenerateCode();
        startState = LexGen.initialState.GenerateInitMoves();
      }

      if (LexGen.initialState.kind != Integer.MAX_VALUE && LexGen.initialState.kind != 0) {
        if (LexGen.initMatch[LexGen.lexStateIndex] == 0
            || LexGen.initMatch[LexGen.lexStateIndex] > LexGen.initialState.kind) {
          LexGen.initMatch[LexGen.lexStateIndex] = LexGen.initialState.kind;
        }
      } else if (LexGen.initMatch[LexGen.lexStateIndex] == 0) {
        LexGen.initMatch[LexGen.lexStateIndex] = Integer.MAX_VALUE;
      }

      RStringLiteral.FillSubString();

      if (LexGen.hasNfa[LexGen.lexStateIndex] && !LexGen.mixed[LexGen.lexStateIndex]) {
        RStringLiteral.GenerateNfaStartStates(LexGen.initialState);
      }

      RStringLiteral.UpdateStringLiteralData(LexGen.totalNumStates, LexGen.lexStateIndex);
      NfaState.UpdateNfaData(LexGen.totalNumStates, startState, LexGen.lexStateIndex,
          LexGen.canMatchAnyChar[LexGen.lexStateIndex]);
      LexGen.totalNumStates += NfaState.generatedStates;
      if (LexGen.stateSetSize < NfaState.generatedStates) {
        LexGen.stateSetSize = NfaState.generatedStates;
      }
    }

    for (i = 0; i < choices.size(); i++) {
      ((RChoice) choices.get(i)).CheckUnmatchability();
    }

    LexGen.CheckEmptyStringMatch();

    LexGen.tokenizerData.setParserName(JavaCCGlobals.cu_name);
    NfaState.BuildTokenizerData(LexGen.tokenizerData);
    RStringLiteral.BuildTokenizerData(LexGen.tokenizerData);

    int[] newLexStateIndices = new int[LexGen.maxOrdinal];
    StringBuilder tokenMgrDecls = new StringBuilder();
    if (JavaCCGlobals.token_mgr_decls != null && JavaCCGlobals.token_mgr_decls.size() > 0) {
      // Token t = token_mgr_decls.get(0);
      for (j = 0; j < JavaCCGlobals.token_mgr_decls.size(); j++) {
        tokenMgrDecls.append(JavaCCGlobals.token_mgr_decls.get(j).image + " ");
      }
    }
    LexGen.tokenizerData.setDecls(tokenMgrDecls.toString());
    Map<Integer, String> actionStrings = new HashMap<>();
    for (i = 0; i < LexGen.maxOrdinal; i++) {
      if (LexGen.newLexState[i] == null) {
        newLexStateIndices[i] = -1;
      } else {
        newLexStateIndices[i] = LexGen.GetIndex(LexGen.newLexState[i]);
      }
      // For java, we have this but for other languages, eventually we will
      // simply have a string.
      Action act = LexGen.actions[i];
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
    LexGen.tokenizerData.setDefaultLexState(LexGen.defaultLexState);
    LexGen.tokenizerData.setLexStateNames(LexGen.lexStateName);
    LexGen.tokenizerData.updateMatchInfo(actionStrings, newLexStateIndices, LexGen.toSkip, LexGen.toSpecial,
        LexGen.toMore, LexGen.toToken);
    Map<Integer, String> labels = new HashMap<>();
    String[] images = new String[JavaCCGlobals.rexps_of_tokens.size() + 1];
    for (Integer o : JavaCCGlobals.rexps_of_tokens.keySet()) {
      RegularExpression re = JavaCCGlobals.rexps_of_tokens.get(o);
      String label = re.label;
      if (label != null && label.length() > 0) {
        labels.put(o, label);
      }
      if (re instanceof RStringLiteral) {
        images[o] = ((RStringLiteral) re).image;
      }
    }
    LexGen.tokenizerData.setLabelsAndImages(JavaCCGlobals.names_of_tokens, images);

    if (LexGen.generateDataOnly) {
      return;
    }

    TokenManagerCodeGenerator gen = codeGenerator.getTokenManagerCodeGenerator();
    CodeGeneratorSettings settings = CodeGeneratorSettings.of(Options.getOptions());
    gen.generateCode(settings, LexGen.tokenizerData);
    gen.finish(settings, LexGen.tokenizerData);
  }

  private static void CheckEmptyStringMatch() {
    int i, j, k, len;
    boolean[] seen = new boolean[LexGen.maxLexStates];
    boolean[] done = new boolean[LexGen.maxLexStates];
    String cycle;
    String reList;

    Outer:
    for (i = 0; i < LexGen.maxLexStates; i++) {
      if (done[i] || LexGen.initMatch[i] == 0 || LexGen.initMatch[i] == Integer.MAX_VALUE
          || LexGen.canMatchAnyChar[i] != -1) {
        continue;
      }

      done[i] = true;
      len = 0;
      cycle = "";
      reList = "";

      for (k = 0; k < LexGen.maxLexStates; k++) {
        seen[k] = false;
      }

      j = i;
      seen[i] = true;
      cycle += LexGen.lexStateName[j] + "-->";
      while (LexGen.newLexState[LexGen.initMatch[j]] != null) {
        cycle += LexGen.newLexState[LexGen.initMatch[j]];
        if (seen[j = LexGen.GetIndex(LexGen.newLexState[LexGen.initMatch[j]])]) {
          break;
        }

        cycle += "-->";
        done[j] = true;
        seen[j] = true;
        if (LexGen.initMatch[j] == 0 || LexGen.initMatch[j] == Integer.MAX_VALUE || LexGen.canMatchAnyChar[j] != -1) {
          continue Outer;
        }
        if (len != 0) {
          reList += "; ";
        }
        reList += "line " + LexGen.rexprs[LexGen.initMatch[j]].getLine() + ", column "
            + LexGen.rexprs[LexGen.initMatch[j]].getColumn();
        len++;
      }

      if (LexGen.newLexState[LexGen.initMatch[j]] == null) {
        cycle += LexGen.lexStateName[LexGen.lexStates[LexGen.initMatch[j]]];
      }

      for (k = 0; k < LexGen.maxLexStates; k++) {
        LexGen.canLoop[k] |= seen[k];
      }

      if (len == 0) {
        JavaCCErrors.warning(LexGen.rexprs[LexGen.initMatch[i]],
            "Regular expression"
                + ((LexGen.rexprs[LexGen.initMatch[i]].label.equals("")) ? ""
                    : (" for " + LexGen.rexprs[LexGen.initMatch[i]].label))
                + " can be matched by the empty string (\"\") in lexical state " + LexGen.lexStateName[i]
                + ". This can result in an endless loop of " + "empty string matches.");
      } else {
        JavaCCErrors.warning(LexGen.rexprs[LexGen.initMatch[i]],
            "Regular expression"
                + ((LexGen.rexprs[LexGen.initMatch[i]].label.equals("")) ? ""
                    : (" for " + LexGen.rexprs[LexGen.initMatch[i]].label))
                + " can be matched by the empty string (\"\") in lexical state " + LexGen.lexStateName[i]
                + ". This regular expression along with the " + "regular expressions at " + reList
                + " forms the cycle \n   " + cycle + "\ncontaining regular expressions with empty matches."
                + " This can result in an endless loop of empty string matches.");
      }
    }
  }

  static void reInit() {
    LexGen.actions = null;
    LexGen.allTpsForState = new Hashtable<>();
    LexGen.canLoop = null;
    LexGen.canMatchAnyChar = null;
    LexGen.canReachOnMore = null;
    LexGen.curKind = 0;
    LexGen.curRE = null;
    LexGen.defaultLexState = 0;
    LexGen.hasNfa = null;
    LexGen.ignoreCase = null;
    LexGen.initMatch = null;
    LexGen.initStates = new Hashtable<>();
    LexGen.initialState = null;
    LexGen.kinds = null;
    LexGen.lexStateIndex = 0;
    LexGen.lexStateName = null;
    LexGen.lexStates = null;
    LexGen.maxLexStates = 0;
    LexGen.maxOrdinal = 1;
    LexGen.mixed = null;
    LexGen.newLexState = null;
    LexGen.rexprs = null;
    LexGen.singlesToSkip = null;
    LexGen.stateSetSize = 0;
    LexGen.toMore = null;
    LexGen.toSkip = null;
    LexGen.toSpecial = null;
    LexGen.toToken = null;
    LexGen.tokenizerData = new TokenizerData();
    LexGen.totalNumStates = 0;
    LexGen.generateDataOnly = false;
  }
}
