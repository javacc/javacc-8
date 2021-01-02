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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes string literals.
 */

public class RStringLiteral extends RegularExpression {

  /**
   * The string image of the literal.
   */
  public String image;

  public RStringLiteral() {}

  RStringLiteral(Token token, String image) {
    this.image = image;
    setLine(token.beginLine);
    setColumn(token.beginColumn);
  }

  /**
   * Used for top level string literals.
   */
  void GenerateDfa(int kind, LexerContext lexerContext) {
    int len;

    if (lexerContext.maxStrKind <= ordinal) {
      lexerContext.maxStrKind = ordinal + 1;
    }

    if ((len = image.length()) > lexerContext.maxLen) {
      lexerContext.maxLen = len;
    }

    lexerContext.maxLenForActive[ordinal / 64] = Math.max(lexerContext.maxLenForActive[ordinal / 64], len - 1);
    lexerContext.allImages[ordinal] = image;
  }

  @Override
  public Nfa GenerateNfa(boolean ignoreCase, LexerContext lexerContext) {
    if (image.length() == 1) {
      RCharacterList temp = new RCharacterList(image.charAt(0));
      return temp.GenerateNfa(ignoreCase, lexerContext);
    }

    NfaState startState = new NfaState(lexerContext);
    NfaState theStartState = startState;
    NfaState finalState = null;

    if (image.length() == 0) {
      return new Nfa(theStartState, theStartState);
    }

    int i;

    for (i = 0; i < image.length(); i++) {
      finalState = new NfaState(lexerContext);
      startState.charMoves = new char[1];
      startState.AddChar(image.charAt(i));

      if (Options.getIgnoreCase() || ignoreCase) {
        startState.AddChar(Character.toLowerCase(image.charAt(i)));
        startState.AddChar(Character.toUpperCase(image.charAt(i)));
      }

      startState.next = finalState;
      startState = finalState;
    }

    return new Nfa(theStartState, finalState);
  }

  private static int GetStateSetForKind(int pos, int kind, LexerContext lexerContext) {
    if (lexerContext.mixed[lexerContext.lexStateIndex] || (lexerContext.generatedStates == 0)) {
      return -1;
    }

    Hashtable<String, long[]> allStateSets = lexerContext.statesForPos[pos];

    if (allStateSets == null) {
      return -1;
    }

    Enumeration<String> e = allStateSets.keys();

    while (e.hasMoreElements()) {
      String s = e.nextElement();
      long[] actives = allStateSets.get(s);

      s = s.substring(s.indexOf(", ") + 2);
      s = s.substring(s.indexOf(", ") + 2);

      if (s.equals("null;")) {
        continue;
      }

      if ((actives != null) && ((actives[kind / 64] & (1L << (kind % 64))) != 0L)) {
        return NfaState.AddStartStateSet(s, lexerContext);
      }
    }

    return -1;
  }

  /**
   * Returns true if s1 starts with s2 (ignoring case for each character).
   */
  static private boolean StartsWithIgnoreCase(String s1, String s2) {
    if (s1.length() < s2.length()) {
      return false;
    }

    for (int i = 0; i < s2.length(); i++) {
      char c1 = s1.charAt(i), c2 = s2.charAt(i);

      if ((c1 != c2) && (Character.toLowerCase(c2) != c1) && (Character.toUpperCase(c2) != c1)) {
        return false;
      }
    }

    return true;
  }

  static void FillSubString(LexerContext lexerContext) {
    String image;
    lexerContext.subString = new boolean[lexerContext.maxStrKind + 1];
    lexerContext.subStringAtPos = new boolean[lexerContext.maxLen];

    for (int i = 0; i < lexerContext.maxStrKind; i++) {
      lexerContext.subString[i] = false;

      if (((image = lexerContext.allImages[i]) == null) || (lexerContext.lexStates[i] != lexerContext.lexStateIndex)) {
        continue;
      }

      if (lexerContext.mixed[lexerContext.lexStateIndex]) {
        // We will not optimize for mixed case
        lexerContext.subString[i] = true;
        lexerContext.subStringAtPos[image.length() - 1] = true;
        continue;
      }

      for (int j = 0; j < lexerContext.maxStrKind; j++) {
        if ((j != i) && (lexerContext.lexStates[j] == lexerContext.lexStateIndex)
            && ((lexerContext.allImages[j]) != null)) {
          if (lexerContext.allImages[j].indexOf(image) == 0) {
            lexerContext.subString[i] = true;
            lexerContext.subStringAtPos[image.length() - 1] = true;
            break;
          } else if (Options.getIgnoreCase() && RStringLiteral.StartsWithIgnoreCase(lexerContext.allImages[j], image)) {
            lexerContext.subString[i] = true;
            lexerContext.subStringAtPos[image.length() - 1] = true;
            break;
          }
        }
      }
    }
  }

  private static final int GetStrKind(String str, LexerContext lexerContext) {
    for (int i = 0; i < lexerContext.maxStrKind; i++) {
      if (lexerContext.lexStates[i] != lexerContext.lexStateIndex) {
        continue;
      }

      String image = lexerContext.allImages[i];
      if ((image != null) && image.equals(str)) {
        return i;
      }
    }

    return Integer.MAX_VALUE;
  }

  static void GenerateNfaStartStates(NfaState initialState, LexerContext lexerContext) {
    boolean[] seen = new boolean[lexerContext.generatedStates];
    Hashtable<String, String> stateSets = new Hashtable<>();
    String stateSetString = "";
    int i, j, kind, jjmatchedPos = 0;
    int maxKindsReqd = (lexerContext.maxStrKind / 64) + 1;
    long[] actives;
    List<NfaState> newStates = new ArrayList<>();
    List<NfaState> oldStates = null, jjtmpStates;

    lexerContext.statesForPos = new Hashtable[lexerContext.maxLen];
    lexerContext.intermediateKinds = new int[lexerContext.maxStrKind + 1][];
    lexerContext.intermediateMatchedPos = new int[lexerContext.maxStrKind + 1][];

    for (i = 0; i < lexerContext.maxStrKind; i++) {
      if (lexerContext.lexStates[i] != lexerContext.lexStateIndex) {
        continue;
      }

      String image = lexerContext.allImages[i];

      if ((image == null) || (image.length() < 1)) {
        continue;
      }

      try {
        if (((oldStates = (List<NfaState>) initialState.epsilonMoves.clone()) == null) || (oldStates.size() == 0)) {
          return;
        }
      } catch (Exception e) {
        lexerContext.context.errors().semantic_error("Error cloning state vector");
      }

      lexerContext.intermediateKinds[i] = new int[image.length()];
      lexerContext.intermediateMatchedPos[i] = new int[image.length()];
      jjmatchedPos = 0;
      kind = Integer.MAX_VALUE;

      for (j = 0; j < image.length(); j++) {
        if ((oldStates == null) || (oldStates.size() <= 0)) {
          // Here, j > 0
          kind = lexerContext.intermediateKinds[i][j] = lexerContext.intermediateKinds[i][j - 1];
          jjmatchedPos = lexerContext.intermediateMatchedPos[i][j] = lexerContext.intermediateMatchedPos[i][j - 1];
        } else {
          kind = NfaState.MoveFromSet(image.charAt(j), oldStates, newStates);
          oldStates.clear();

          if ((j == 0) && (kind != Integer.MAX_VALUE)
              && (lexerContext.canMatchAnyChar[lexerContext.lexStateIndex] != -1)
              && (kind > lexerContext.canMatchAnyChar[lexerContext.lexStateIndex])) {
            kind = lexerContext.canMatchAnyChar[lexerContext.lexStateIndex];
          }

          if (RStringLiteral.GetStrKind(image.substring(0, j + 1), lexerContext) < kind) {
            lexerContext.intermediateKinds[i][j] = kind = Integer.MAX_VALUE;
            jjmatchedPos = 0;
          } else if (kind != Integer.MAX_VALUE) {
            lexerContext.intermediateKinds[i][j] = kind;
            jjmatchedPos = lexerContext.intermediateMatchedPos[i][j] = j;
          } else if (j == 0) {
            kind = lexerContext.intermediateKinds[i][j] = Integer.MAX_VALUE;
          } else {
            kind = lexerContext.intermediateKinds[i][j] = lexerContext.intermediateKinds[i][j - 1];
            jjmatchedPos = lexerContext.intermediateMatchedPos[i][j] = lexerContext.intermediateMatchedPos[i][j - 1];
          }

          stateSetString = NfaState.GetStateSetString(newStates, lexerContext);
        }

        if ((kind == Integer.MAX_VALUE) && ((newStates == null) || (newStates.size() == 0))) {
          continue;
        }

        int p;
        if (stateSets.get(stateSetString) == null) {
          stateSets.put(stateSetString, stateSetString);
          for (p = 0; p < newStates.size(); p++) {
            if (seen[newStates.get(p).stateName]) {
              newStates.get(p).inNextOf++;
            } else {
              seen[newStates.get(p).stateName] = true;
            }
          }
        } else {
          for (p = 0; p < newStates.size(); p++) {
            seen[newStates.get(p).stateName] = true;
          }
        }

        jjtmpStates = oldStates;
        oldStates = newStates;
        (newStates = jjtmpStates).clear();

        if (lexerContext.statesForPos[j] == null) {
          lexerContext.statesForPos[j] = new Hashtable<>();
        }

        if ((actives =
            (lexerContext.statesForPos[j].get(kind + ", " + jjmatchedPos + ", " + stateSetString))) == null) {
          actives = new long[maxKindsReqd];
          lexerContext.statesForPos[j].put(kind + ", " + jjmatchedPos + ", " + stateSetString, actives);
        }

        actives[i / 64] |= 1L << (i % 64);
        // String name = NfaState.StoreStateSet(stateSetString);
      }
    }
  }

  @Override
  public StringBuffer dump(int indent, Set<Expansion> alreadyDumped) {
    StringBuffer sb = super.dump(indent, alreadyDumped).append(' ').append(image);
    return sb;
  }

  @Override
  public String toString() {
    return super.toString() + " - " + image;
  }

  static void UpdateStringLiteralData(int generatedNfaStates, LexerContext lexerContext) {
    for (int kind = 0; kind < lexerContext.allImages.length; kind++) {
      if ((lexerContext.allImages[kind] == null) || lexerContext.allImages[kind].equals("")
          || (lexerContext.lexStates[kind] != lexerContext.lexStateIndex) ||
          lexerContext.mixed[lexerContext.lexStates[kind]]) {
        continue;
      }
      String s = lexerContext.allImages[kind];
      boolean ignoreCase = lexerContext.ignoreCase[kind];
      int actualKind;
      if ((lexerContext.intermediateKinds != null)
          && (lexerContext.intermediateKinds[kind][s.length() - 1] != Integer.MAX_VALUE)
          && (lexerContext.intermediateKinds[kind][s.length() - 1] < kind)) {
        lexerContext.context.errors().warning("Token: " + s + " will not be matched as " + "specified. It will be matched as token "
            + "of kind: " + lexerContext.intermediateKinds[kind][s.length() - 1] + " instead.");
        actualKind = lexerContext.intermediateKinds[kind][s.length() - 1];
      } else {
        actualKind = kind;
      }
      lexerContext.kindToLexicalState.put(actualKind, lexerContext.lexStateIndex);
      if (Options.getIgnoreCase() || ignoreCase) {
        s = s.toLowerCase();
      }
      char c = s.charAt(0);
      int key = (lexerContext.lexStateIndex << 16) | c;
      RStringLiteral.UpdateStringLiteralDataForKey(key, actualKind, s, lexerContext);

      if (ignoreCase) {
        lexerContext.kindToIgnoreCase.add(kind);
        c = s.toUpperCase().charAt(0);
        key = (lexerContext.lexStateIndex << 16) | c;
        RStringLiteral.UpdateStringLiteralDataForKey(key, actualKind, s, lexerContext);
      }

      int stateIndex = RStringLiteral.GetStateSetForKind(s.length() - 1, kind, lexerContext);
      if (stateIndex != -1) {
        lexerContext.nfaStateMap.put(actualKind, NfaState.getNfaState(stateIndex, lexerContext));
      } else {
        lexerContext.nfaStateMap.put(actualKind, null);
      }
    }
  }

  private static void UpdateStringLiteralDataForKey(int key, int actualKind, String s, LexerContext lexerContext) {
    List<String> l = lexerContext.literalsByLength.get(key);
    List<Integer> kinds = lexerContext.literalKinds.get(key);
    int j = 0;
    if (l == null) {
      lexerContext.literalsByLength.put(key, l = new ArrayList<>());
      assert (kinds == null);
      kinds = new ArrayList<>();
      lexerContext.literalKinds.put(key, kinds = new ArrayList<>());
    }
    while ((j < l.size()) && (l.get(j).length() > s.length())) {
      j++;
    }
    l.add(j, s);
    kinds.add(j, actualKind);
  }

  static void BuildTokenizerData(TokenizerData tokenizerData, LexerContext lexerContext) {
    Map<Integer, Integer> nfaStateIndices = new HashMap<>();
    for (int kind : lexerContext.nfaStateMap.keySet()) {
      if (lexerContext.nfaStateMap.get(kind) != null) {
        if (nfaStateIndices.put(kind, lexerContext.nfaStateMap.get(kind).stateName) != null) {
          System.err.println("ERROR: Multiple start states for kind: " + kind);
        }
      } else {
        nfaStateIndices.put(kind, -1);
      }
    }
    tokenizerData.setLiteralSequence(lexerContext.literalsByLength);
    tokenizerData.setLiteralKinds(lexerContext.literalKinds);
    tokenizerData.setIgnoreCaserKinds(lexerContext.kindToIgnoreCase);
    tokenizerData.setKindToNfaStartState(nfaStateIndices);
  }
}
