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

  public static final String DEFAULT_STATE = "DEFAULT";
  private final Context      context;

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

  public LexGen(Context context) {
    this.context = context;
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

  private LexerContext BuildLexStatesTable(boolean unicodeWarning) {
    LexerContext lexerContext = new LexerContext(context);
    lexerContext.unicodeWarningGiven = unicodeWarning;
    Iterator<TokenProduction> it = context.globals().rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[context.globals().lexstate_I2S.size()];
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
    actions[0] = context.globals().actForEof;
    initStates = new Hashtable<>();
    lexerContext.canMatchAnyChar = new int[maxLexStates];
    canLoop = new boolean[maxLexStates];
    singlesToSkip = new NfaState[maxLexStates];

    for (i = 0; i < maxLexStates; i++) {
      lexerContext.canMatchAnyChar[i] = -1;
    }

    hasNfa = new boolean[maxLexStates];
    lexerContext.mixed = new boolean[maxLexStates];
    initMatch = new int[maxLexStates];
    newLexState = new String[maxOrdinal];
    newLexState[0] = context.globals().nextStateForEof;
    lexerContext.lexStates = new int[maxOrdinal];
    lexerContext.ignoreCase = new boolean[maxOrdinal];
    rexprs = new RegularExpression[maxOrdinal];
    lexerContext.allImages = new String[maxOrdinal];
    canReachOnMore = new boolean[maxLexStates];
    return lexerContext;
  }

  private int GetIndex(String name, String[] lexStateNames) {
    for (int i = 0; i < lexStateNames.length; i++) {
      if ((lexStateNames[i] != null) && lexStateNames[i].equals(name)) {
        return i;
      }
    }

    throw new Error(); // Should never come here
  }

  public TokenizerData generateTokenizerData(boolean generateDataOnly, boolean unicodeWarning) throws IOException {
    if (!Options.getBuildTokenManager() || Options.getUserTokenManager() || (context.errors().get_error_count() > 0)) {
      return new TokenizerData();
    }

    final CodeGenerator codeGenerator = context.getCodeGenerator();
    List<RegularExpression> choices = new ArrayList<>();
    TokenProduction tp;
    int i, j;

    LexerContext lexerContext = BuildLexStatesTable(unicodeWarning);

    boolean ignoring = false;

    TokenizerData tokenizerData = new TokenizerData();
    tokenizerData.lexStateNames = new String[maxLexStates];
    for (int l : context.globals().lexstate_I2S.keySet()) {
      tokenizerData.lexStateNames[l] = context.globals().lexstate_I2S.get(l);
    }

    // while (e.hasMoreElements())
    for (int k = 0; k < tokenizerData.lexStateNames.length; k++) {
      int startState = -1;
      lexerContext.clear();

      // String key = (String)e.nextElement();
      String key = tokenizerData.lexStateNames[k];

      lexerContext.lexStateIndex = GetIndex(key, tokenizerData.lexStateNames);
      List<TokenProduction> allTps = allTpsForState.get(key);
      initStates.put(key, initialState = new NfaState(lexerContext));
      ignoring = false;

      singlesToSkip[lexerContext.lexStateIndex] = new NfaState(lexerContext);

      if (key.equals(LexGen.DEFAULT_STATE)) {
        defaultLexState = lexerContext.lexStateIndex;
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
          lexerContext.curRE = respec.rexp;

          rexprs[lexerContext.curKind = lexerContext.curRE.ordinal] = lexerContext.curRE;
          lexerContext.lexStates[lexerContext.curRE.ordinal] = lexerContext.lexStateIndex;
          lexerContext.ignoreCase[lexerContext.curRE.ordinal] = ignore;

          if (lexerContext.curRE.private_rexp) {
            kinds[lexerContext.curRE.ordinal] = -1;
            continue;
          }

          if (!Options.getNoDfa() && (lexerContext.curRE instanceof RStringLiteral)
              && !((RStringLiteral) lexerContext.curRE).image.equals("")) {
            ((RStringLiteral) lexerContext.curRE).GenerateDfa(lexerContext.curRE.ordinal, lexerContext);
            if ((i != 0) && !lexerContext.mixed[lexerContext.lexStateIndex] && (ignoring != ignore)) {
              lexerContext.mixed[lexerContext.lexStateIndex] = true;
            }
          } else if (lexerContext.curRE.CanMatchAnyChar()) {
            if ((lexerContext.canMatchAnyChar[lexerContext.lexStateIndex] == -1)
                || (lexerContext.canMatchAnyChar[lexerContext.lexStateIndex] > lexerContext.curRE.ordinal)) {
              lexerContext.canMatchAnyChar[lexerContext.lexStateIndex] = lexerContext.curRE.ordinal;
            }
          } else {
            Nfa temp;

            if (lexerContext.curRE instanceof RChoice) {
              choices.add(lexerContext.curRE);
            }

            temp = lexerContext.curRE.GenerateNfa(ignore, lexerContext);
            temp.end.isFinal = true;
            temp.end.kind = lexerContext.curRE.ordinal;
            initialState.AddMove(temp.start);
          }

          if (kinds.length < lexerContext.curRE.ordinal) {
            int[] tmp = new int[lexerContext.curRE.ordinal + 1];

            System.arraycopy(kinds, 0, tmp, 0, kinds.length);
            kinds = tmp;
          }
          // System.out.println(" ordina : " + curRE.ordinal);

          kinds[lexerContext.curRE.ordinal] = kind;

          if ((respec.nextState != null)
              && !respec.nextState.equals(tokenizerData.lexStateNames[lexerContext.lexStateIndex])) {
            newLexState[lexerContext.curRE.ordinal] = respec.nextState;
          }

          if ((respec.act != null) && (respec.act.getActionTokens() != null)
              && (respec.act.getActionTokens().size() > 0)) {
            actions[lexerContext.curRE.ordinal] = respec.act;
          }

          switch (kind) {
            case TokenProduction.SPECIAL:
              toSpecial[lexerContext.curRE.ordinal / 64] |= 1L << (lexerContext.curRE.ordinal % 64);
              toSkip[lexerContext.curRE.ordinal / 64] |= 1L << (lexerContext.curRE.ordinal % 64);
              break;
            case TokenProduction.SKIP:
              toSkip[lexerContext.curRE.ordinal / 64] |= 1L << (lexerContext.curRE.ordinal % 64);
              break;
            case TokenProduction.MORE:
              toMore[lexerContext.curRE.ordinal / 64] |= 1L << (lexerContext.curRE.ordinal % 64);

              if (newLexState[lexerContext.curRE.ordinal] != null) {
                canReachOnMore[GetIndex(newLexState[lexerContext.curRE.ordinal], tokenizerData.lexStateNames)] = true;
              } else {
                canReachOnMore[lexerContext.lexStateIndex] = true;
              }

              break;
            case TokenProduction.TOKEN:
              toToken[lexerContext.curRE.ordinal / 64] |= 1L << (lexerContext.curRE.ordinal % 64);
              break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures(lexerContext);

      for (i = 0; i < initialState.epsilonMoves.size(); i++) {
        initialState.epsilonMoves.elementAt(i).GenerateCode();
      }

      if (hasNfa[lexerContext.lexStateIndex] = (lexerContext.generatedStates != 0)) {
        initialState.GenerateCode();
        startState = initialState.GenerateInitMoves();
      }

      if ((initialState.kind != Integer.MAX_VALUE) && (initialState.kind != 0)) {
        if ((initMatch[lexerContext.lexStateIndex] == 0)
            || (initMatch[lexerContext.lexStateIndex] > initialState.kind)) {
          initMatch[lexerContext.lexStateIndex] = initialState.kind;
        }
      } else if (initMatch[lexerContext.lexStateIndex] == 0) {
        initMatch[lexerContext.lexStateIndex] = Integer.MAX_VALUE;
      }

      RStringLiteral.FillSubString(lexerContext);

      if (hasNfa[lexerContext.lexStateIndex] && !lexerContext.mixed[lexerContext.lexStateIndex]) {
        RStringLiteral.GenerateNfaStartStates(initialState, lexerContext);
      }

      RStringLiteral.UpdateStringLiteralData(totalNumStates, lexerContext);
      NfaState.UpdateNfaData(totalNumStates, startState, lexerContext.lexStateIndex,
          lexerContext.canMatchAnyChar[lexerContext.lexStateIndex], lexerContext);
      totalNumStates += lexerContext.generatedStates;
      if (stateSetSize < lexerContext.generatedStates) {
        stateSetSize = lexerContext.generatedStates;
      }
    }

    for (i = 0; i < choices.size(); i++) {
      ((RChoice) choices.get(i)).CheckUnmatchability(lexerContext.lexStates, lexerContext.context);
    }

    CheckEmptyStringMatch(lexerContext, tokenizerData);

    tokenizerData.setParserName(context.globals().cu_name);
    NfaState.BuildTokenizerData(tokenizerData, lexerContext);
    RStringLiteral.BuildTokenizerData(tokenizerData, lexerContext);

    int[] newLexStateIndices = new int[maxOrdinal];
    StringBuilder tokenMgrDecls = new StringBuilder();
    if ((context.globals().token_mgr_decls != null) && (context.globals().token_mgr_decls.size() > 0)) {
      // Token t = token_mgr_decls.get(0);
      for (j = 0; j < context.globals().token_mgr_decls.size(); j++) {
        tokenMgrDecls.append(context.globals().token_mgr_decls.get(j).image + " ");
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
        if (act.getActionTokens().get(k).specialToken != null)
          sb.append(act.getActionTokens().get(k).specialToken.image);
        sb.append(act.getActionTokens().get(k).image);
      }
      actionStrings.put(i, sb.toString());
    }
    tokenizerData.setDefaultLexState(defaultLexState);
    tokenizerData.updateMatchInfo(actionStrings, newLexStateIndices, toSkip, toSpecial, toMore, toToken,
        lexerContext.allImages);
    Map<Integer, String> labels = new HashMap<>();
    String[] images = new String[context.globals().rexps_of_tokens.size() + 1];
    for (Integer o : context.globals().rexps_of_tokens.keySet()) {
      RegularExpression re = context.globals().rexps_of_tokens.get(o);
      String label = re.label;
      if ((label != null) && (label.length() > 0)) {
        labels.put(o, label);
      }
      if (re instanceof RStringLiteral) {
        images[o] = ((RStringLiteral) re).image;
      }
    }
    tokenizerData.setLabelsAndImages(context.globals().names_of_tokens, images);

    if (generateDataOnly) {
      return tokenizerData;
    }

    TokenManagerCodeGenerator gen = codeGenerator.getTokenManagerCodeGenerator(context);
    CodeGeneratorSettings settings = CodeGeneratorSettings.of(Options.getOptions());
    gen.generateCode(settings, tokenizerData);
    gen.finish(settings, tokenizerData);
    return tokenizerData;
  }

  private void CheckEmptyStringMatch(LexerContext lexerContext, TokenizerData tokenizerData) {
    int i, j, k, len;
    boolean[] seen = new boolean[maxLexStates];
    boolean[] done = new boolean[maxLexStates];
    String cycle;
    String reList;

    Outer:
    for (i = 0; i < maxLexStates; i++) {
      if (done[i] || (initMatch[i] == 0) || (initMatch[i] == Integer.MAX_VALUE)
          || (lexerContext.canMatchAnyChar[i] != -1)) {
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
        if ((initMatch[j] == 0) || (initMatch[j] == Integer.MAX_VALUE) || (lexerContext.canMatchAnyChar[j] != -1)) {
          continue Outer;
        }
        if (len != 0) {
          reList += "; ";
        }
        reList += "line " + rexprs[initMatch[j]].getLine() + ", column " + rexprs[initMatch[j]].getColumn();
        len++;
      }

      if (newLexState[initMatch[j]] == null) {
        cycle += tokenizerData.lexStateNames[lexerContext.lexStates[initMatch[j]]];
      }

      for (k = 0; k < maxLexStates; k++) {
        canLoop[k] |= seen[k];
      }

      if (len == 0) {
        context.errors().warning(rexprs[initMatch[i]],
            "Regular expression"
                + ((rexprs[initMatch[i]].label.equals("")) ? "" : (" for " + rexprs[initMatch[i]].label))
                + " can be matched by the empty string (\"\") in lexical state " + tokenizerData.lexStateNames[i]
                + ". This can result in an endless loop of " + "empty string matches.");
      } else {
        context.errors().warning(rexprs[initMatch[i]],
            "Regular expression"
                + ((rexprs[initMatch[i]].label.equals("")) ? "" : (" for " + rexprs[initMatch[i]].label))
                + " can be matched by the empty string (\"\") in lexical state " + tokenizerData.lexStateNames[i]
                + ". This regular expression along with the " + "regular expressions at " + reList
                + " forms the cycle \n   " + cycle + "\ncontaining regular expressions with empty matches."
                + " This can result in an endless loop of empty string matches.");
      }
    }
  }
}
