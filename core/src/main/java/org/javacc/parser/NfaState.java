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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * The state of a Non-deterministic Finite Automaton.
 */
class NfaState {


  private final long[]        asciiMoves         = new long[2];
  private char[]              rangeMoves         = null;
  private String              epsilonMovesString;
  private NfaState[]          epsilonMoveArray;

  private final int           id;
  private final LexerContext  lexerContext;
  private int                 lookingFor;
  private int                 usefulEpsilonMoves = 0;
  private int                 lexState;
  private int                 kindToPrint        = Integer.MAX_VALUE;

  private boolean             isComposite        = false;
  private int[]               compositeStates    = null;
  private final Set<NfaState> compositeStateSet  = new HashSet<>();
  // private int round = 0;
  private int                 onlyChar           = 0;
  private char                matchSingleChar;

  char[]                      charMoves          = null;
  NfaState                    next               = null;
  Vector<NfaState>            epsilonMoves       = new Vector<>();
  int                         stateName          = -1;
  int                         kind               = Integer.MAX_VALUE;
  int                         inNextOf;
  boolean                     isFinal            = false;

  public NfaState(LexerContext lexerContext) {
    id = lexerContext.idCnt++;
    lexerContext.allStates.add(this);
    this.lexerContext = lexerContext;
    lexState = lexerContext.lexStateIndex;
    lookingFor = lexerContext.curKind;
  }

  private NfaState CreateClone() {
    NfaState retVal = new NfaState(lexerContext);

    retVal.isFinal = isFinal;
    retVal.kind = kind;
    retVal.lookingFor = lookingFor;
    retVal.lexState = lexState;
    retVal.inNextOf = inNextOf;

    retVal.MergeMoves(this);

    return retVal;
  }

  private static void InsertInOrder(List<NfaState> v, NfaState s) {
    int j;

    for (j = 0; j < v.size(); j++) {
      if (v.get(j).id > s.id) {
        break;
      } else if (v.get(j).id == s.id) {
        return;
      }
    }

    v.add(j, s);
  }

  private static char[] ExpandCharArr(char[] oldArr, int incr) {
    char[] ret = new char[oldArr.length + incr];
    System.arraycopy(oldArr, 0, ret, 0, oldArr.length);
    return ret;
  }

  void AddMove(NfaState newState) {
    if (!epsilonMoves.contains(newState)) {
      NfaState.InsertInOrder(epsilonMoves, newState);
    }
  }

  private final void AddASCIIMove(char c) {
    asciiMoves[c / 64] |= (1L << (c % 64));
  }

  void AddChar(char c) {
    onlyChar++;
    matchSingleChar = c;
    int i;
    char temp;
    char temp1;

    if (c < 128) // ASCII char
    {
      AddASCIIMove(c);
      return;
    }

    if (charMoves == null) {
      charMoves = new char[10];
    }

    int len = charMoves.length;

    if (charMoves[len - 1] != 0) {
      charMoves = NfaState.ExpandCharArr(charMoves, 10);
      len += 10;
    }

    for (i = 0; i < len; i++) {
      if ((charMoves[i] == 0) || (charMoves[i] > c)) {
        break;
      }
    }

    if (!lexerContext.unicodeWarningGiven && (c > 0xff) && !Options.getJavaUnicodeEscape()
        && !Options.getUserCharStream()) {
      lexerContext.unicodeWarningGiven = true;
      lexerContext.context.errors().warning(lexerContext.curRE,
          "Non-ASCII characters used in regular expression.\n"
              + "Please make sure you use the correct Reader when you create the parser, "
              + "one that can handle your character set.");
    }

    temp = charMoves[i];
    charMoves[i] = c;

    for (i++; i < len; i++) {
      if (temp == 0) {
        break;
      }

      temp1 = charMoves[i];
      charMoves[i] = temp;
      temp = temp1;
    }
  }

  void AddRange(char left, char right) {
    onlyChar = 2;
    int i;
    char tempLeft1, tempLeft2, tempRight1, tempRight2;

    if (left < 128) {
      if (right < 128) {
        for (; left <= right; left++) {
          AddASCIIMove(left);
        }

        return;
      }

      for (; left < 128; left++) {
        AddASCIIMove(left);
      }
    }

    if (!lexerContext.unicodeWarningGiven && ((left > 0xff) || (right > 0xff)) && !Options.getJavaUnicodeEscape()
        && !Options.getUserCharStream()) {
      lexerContext.unicodeWarningGiven = true;
      lexerContext.context.errors().warning(lexerContext.curRE,
          "Non-ASCII characters used in regular expression.\n"
              + "Please make sure you use the correct Reader when you create the parser, "
              + "one that can handle your character set.");
    }

    if (rangeMoves == null) {
      rangeMoves = new char[20];
    }

    int len = rangeMoves.length;

    if (rangeMoves[len - 1] != 0) {
      rangeMoves = NfaState.ExpandCharArr(rangeMoves, 20);
      len += 20;
    }

    for (i = 0; i < len; i += 2) {
      if ((rangeMoves[i] == 0) || (rangeMoves[i] > left) || ((rangeMoves[i] == left) && (rangeMoves[i + 1] > right))) {
        break;
      }
    }

    tempLeft1 = rangeMoves[i];
    tempRight1 = rangeMoves[i + 1];
    rangeMoves[i] = left;
    rangeMoves[i + 1] = right;

    for (i += 2; i < len; i += 2) {
      if (tempLeft1 == 0) {
        break;
      }

      tempLeft2 = rangeMoves[i];
      tempRight2 = rangeMoves[i + 1];
      rangeMoves[i] = tempLeft1;
      rangeMoves[i + 1] = tempRight1;
      tempLeft1 = tempLeft2;
      tempRight1 = tempRight2;
    }
  }

  // From hereon down all the functions are used for code generation

  private static boolean EqualCharArr(char[] arr1, char[] arr2) {
    if (arr1 == arr2) {
      return true;
    }

    if ((arr1 != null) && (arr2 != null) && (arr1.length == arr2.length)) {
      for (int i = arr1.length; i-- > 0;) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  private boolean closureDone = false;

  /**
   * This function computes the closure and also updates the kind so that any
   * time there is a move to this state, it can go on epsilon to a new state in
   * the epsilon moves that might have a lower kind of token number for the same
   * length.
   */

  private void EpsilonClosure() {
    int i = 0;

    if (closureDone || lexerContext.mark[id]) {
      return;
    }

    lexerContext.mark[id] = true;

    // Recursively do closure
    for (i = 0; i < epsilonMoves.size(); i++) {
      epsilonMoves.get(i).EpsilonClosure();
    }

    Enumeration<NfaState> e = epsilonMoves.elements();

    while (e.hasMoreElements()) {
      NfaState tmp = e.nextElement();

      for (i = 0; i < tmp.epsilonMoves.size(); i++) {
        NfaState tmp1 = tmp.epsilonMoves.get(i);
        if (tmp1.UsefulState() && !epsilonMoves.contains(tmp1)) {
          NfaState.InsertInOrder(epsilonMoves, tmp1);
          lexerContext.done = false;
        }
      }

      if (kind > tmp.kind) {
        kind = tmp.kind;
      }
    }

    if (HasTransitions() && !epsilonMoves.contains(this)) {
      NfaState.InsertInOrder(epsilonMoves, this);
    }
  }

  private boolean UsefulState() {
    return isFinal || HasTransitions();
  }

  private boolean HasTransitions() {
    return ((asciiMoves[0] != 0L) || (asciiMoves[1] != 0L) || ((charMoves != null) && (charMoves[0] != 0))
        || ((rangeMoves != null) && (rangeMoves[0] != 0)));
  }

  private void MergeMoves(NfaState other) {
    // Warning : This function does not merge epsilon moves
    if (asciiMoves == other.asciiMoves) {
      lexerContext.context.errors().semantic_error(
          "Bug in JavaCC : Please send " + "a report along with the input that caused this. Thank you.");
      throw new Error();
    }

    asciiMoves[0] = asciiMoves[0] | other.asciiMoves[0];
    asciiMoves[1] = asciiMoves[1] | other.asciiMoves[1];

    if (other.charMoves != null) {
      if (charMoves == null) {
        charMoves = other.charMoves;
      } else {
        char[] tmpCharMoves = new char[charMoves.length + other.charMoves.length];
        System.arraycopy(charMoves, 0, tmpCharMoves, 0, charMoves.length);
        charMoves = tmpCharMoves;

        for (int i = 0; i < other.charMoves.length; i++) {
          AddChar(other.charMoves[i]);
        }
      }
    }

    if (other.rangeMoves != null) {
      if (rangeMoves == null) {
        rangeMoves = other.rangeMoves;
      } else {
        char[] tmpRangeMoves = new char[rangeMoves.length + other.rangeMoves.length];
        System.arraycopy(rangeMoves, 0, tmpRangeMoves, 0, rangeMoves.length);
        rangeMoves = tmpRangeMoves;
        for (int i = 0; i < other.rangeMoves.length; i += 2) {
          AddRange(other.rangeMoves[i], other.rangeMoves[i + 1]);
        }
      }
    }

    if (other.kind < kind) {
      kind = other.kind;
    }

    if (other.kindToPrint < kindToPrint) {
      kindToPrint = other.kindToPrint;
    }

    isFinal |= other.isFinal;
  }

  private NfaState CreateEquivState(List<NfaState> states) {
    NfaState newState = states.get(0).CreateClone();

    newState.next = new NfaState(lexerContext);

    NfaState.InsertInOrder(newState.next.epsilonMoves, states.get(0).next);

    for (int i = 1; i < states.size(); i++) {
      NfaState tmp2 = (states.get(i));

      if (tmp2.kind < newState.kind) {
        newState.kind = tmp2.kind;
      }

      newState.isFinal |= tmp2.isFinal;

      NfaState.InsertInOrder(newState.next.epsilonMoves, tmp2.next);
    }

    return newState;
  }

  private NfaState GetEquivalentRunTimeState() {
    Outer:
      for (int i = lexerContext.allStates.size(); i-- > 0;) {
        NfaState other = lexerContext.allStates.get(i);

        if ((this != other) && (other.stateName != -1) && (kindToPrint == other.kindToPrint)
            && (asciiMoves[0] == other.asciiMoves[0]) && (asciiMoves[1] == other.asciiMoves[1])
            && NfaState.EqualCharArr(charMoves, other.charMoves) && NfaState.EqualCharArr(rangeMoves, other.rangeMoves)) {
          if (next == other.next) {
            return other;
          } else if ((next != null) && (other.next != null)) {
            if (next.epsilonMoves.size() == other.next.epsilonMoves.size()) {
              for (int j = 0; j < next.epsilonMoves.size(); j++) {
                if (next.epsilonMoves.get(j) != other.next.epsilonMoves.get(j)) {
                  continue Outer;
                }
              }

              return other;
            }
          }
        }
      }

  return null;
  }

  // generates code (without outputting it) and returns the name used.
  void GenerateCode() {
    if (stateName != -1) {
      return;
    }

    if (next != null) {
      next.GenerateCode();
      if (next.kind != Integer.MAX_VALUE) {
        kindToPrint = next.kind;
      }
    }

    if ((stateName == -1) && HasTransitions()) {
      NfaState tmp = GetEquivalentRunTimeState();

      if (tmp != null) {
        stateName = tmp.stateName;
        return;
      }

      stateName = lexerContext.generatedStates++;
      lexerContext.indexedAllStates.add(this);
      GenerateNextStatesCode();
    }
  }

  static void ComputeClosures(LexerContext lexerContext) {
    for (int i = lexerContext.allStates.size(); i-- > 0;) {
      NfaState tmp = lexerContext.allStates.get(i);

      if (!tmp.closureDone) {
        tmp.OptimizeEpsilonMoves(true, lexerContext);
      }
    }

    for (int i = 0; i < lexerContext.allStates.size(); i++) {
      NfaState tmp = lexerContext.allStates.get(i);

      if (!tmp.closureDone) {
        tmp.OptimizeEpsilonMoves(false, lexerContext);
      }
    }

    for (int i = 0; i < lexerContext.allStates.size(); i++) {
      NfaState tmp = lexerContext.allStates.get(i);
      tmp.epsilonMoveArray = new NfaState[tmp.epsilonMoves.size()];
      tmp.epsilonMoves.copyInto(tmp.epsilonMoveArray);
    }
  }

  private void OptimizeEpsilonMoves(boolean optReqd, LexerContext lexerContext) {
    int i;

    // First do epsilon closure
    lexerContext.done = false;
    while (!lexerContext.done) {
      if ((lexerContext.mark == null) || (lexerContext.mark.length < lexerContext.allStates.size())) {
        lexerContext.mark = new boolean[lexerContext.allStates.size()];
      }

      for (i = lexerContext.allStates.size(); i-- > 0;) {
        lexerContext.mark[i] = false;
      }

      lexerContext.done = true;
      EpsilonClosure();
    }

    for (i = lexerContext.allStates.size(); i-- > 0;) {
      lexerContext.allStates.get(i).closureDone = lexerContext.mark[lexerContext.allStates.get(i).id];
    }

    // Warning : The following piece of code is just an optimization.
    // in case of trouble, just remove this piece.

    boolean sometingOptimized = true;

    NfaState newState = null;
    NfaState tmp1, tmp2;
    int j;
    List<NfaState> equivStates = null;

    while (sometingOptimized) {
      sometingOptimized = false;
      for (i = 0; optReqd && (i < epsilonMoves.size()); i++) {
        if ((tmp1 = epsilonMoves.get(i)).HasTransitions()) {
          for (j = i + 1; j < epsilonMoves.size(); j++) {
            if ((tmp2 = epsilonMoves.get(j)).HasTransitions() && ((tmp1.asciiMoves[0] == tmp2.asciiMoves[0])
                && (tmp1.asciiMoves[1] == tmp2.asciiMoves[1]) && NfaState.EqualCharArr(tmp1.charMoves, tmp2.charMoves)
                && NfaState.EqualCharArr(tmp1.rangeMoves, tmp2.rangeMoves))) {
              if (equivStates == null) {
                equivStates = new ArrayList<>();
                equivStates.add(tmp1);
              }

              NfaState.InsertInOrder(equivStates, tmp2);
              epsilonMoves.removeElementAt(j--);
            }
          }
        }

        if (equivStates != null) {
          sometingOptimized = true;
          String tmp = "";
          for (int l = 0; l < equivStates.size(); l++) {
            tmp += String.valueOf(equivStates.get(l).id) + ", ";
          }

          if ((newState = lexerContext.equivStatesTable.get(tmp)) == null) {
            newState = CreateEquivState(equivStates);
            lexerContext.equivStatesTable.put(tmp, newState);
          }

          epsilonMoves.removeElementAt(i--);
          epsilonMoves.add(newState);
          equivStates = null;
          newState = null;
        }
      }

      for (i = 0; i < epsilonMoves.size(); i++) {
        // if ((tmp1 = (NfaState)epsilonMoves.elementAt(i)).next == null)
        // continue;
        tmp1 = epsilonMoves.get(i);

        for (j = i + 1; j < epsilonMoves.size(); j++) {
          tmp2 = epsilonMoves.get(j);

          if (tmp1.next == tmp2.next) {
            if (newState == null) {
              newState = tmp1.CreateClone();
              newState.next = tmp1.next;
              sometingOptimized = true;
            }

            newState.MergeMoves(tmp2);
            epsilonMoves.removeElementAt(j--);
          }
        }

        if (newState != null) {
          epsilonMoves.removeElementAt(i--);
          epsilonMoves.add(newState);
          newState = null;
        }
      }
    }

    // End Warning

    // Generate an array of states for epsilon moves (not vector)
    if (epsilonMoves.size() > 0) {
      for (i = 0; i < epsilonMoves.size(); i++) {
        // Since we are doing a closure, just epsilon moves are unnecessary
        if (epsilonMoves.get(i).HasTransitions()) {
          usefulEpsilonMoves++;
        } else {
          epsilonMoves.removeElementAt(i--);
        }
      }
    }
  }

  private void GenerateNextStatesCode() {
    if (next.usefulEpsilonMoves > 0) {
      next.GetEpsilonMovesString();
    }
  }

  private String GetEpsilonMovesString() {
    int[] stateNames = new int[usefulEpsilonMoves];
    int cnt = 0;

    if (epsilonMovesString != null) {
      return epsilonMovesString;
    }

    if (usefulEpsilonMoves > 0) {
      NfaState tempState;
      epsilonMovesString = "{ ";
      for (int i = 0; i < epsilonMoves.size(); i++) {
        if ((tempState = epsilonMoves.get(i)).HasTransitions()) {
          if (tempState.stateName == -1) {
            tempState.GenerateCode();
          }

          lexerContext.indexedAllStates.get(tempState.stateName).inNextOf++;
          stateNames[cnt] = tempState.stateName;
          epsilonMovesString += tempState.stateName + ", ";
          if ((cnt++ > 0) && ((cnt % 16) == 0)) {
            epsilonMovesString += "\n";
          }
        }
      }

      epsilonMovesString += "};";
    }

    usefulEpsilonMoves = cnt;
    if ((epsilonMovesString != null) && (lexerContext.allNextStates.get(epsilonMovesString) == null)) {
      int[] statesToPut = new int[usefulEpsilonMoves];

      System.arraycopy(stateNames, 0, statesToPut, 0, cnt);
      lexerContext.allNextStates.put(epsilonMovesString, statesToPut);
    }

    return epsilonMovesString;
  }

  private final boolean CanMoveUsingChar(char c) {
    int i;

    if (onlyChar == 1) {
      return c == matchSingleChar;
    }

    if (c < 128) {
      return ((asciiMoves[c / 64] & (1L << (c % 64))) != 0L);
    }

    // Just check directly if there is a move for this char
    if ((charMoves != null) && (charMoves[0] != 0)) {
      for (i = 0; i < charMoves.length; i++) {
        if (c == charMoves[i]) {
          return true;
        } else if ((c < charMoves[i]) || (charMoves[i] == 0)) {
          break;
        }
      }
    }


    // For ranges, iterate through the table to see if the current char
    // is in some range
    if ((rangeMoves != null) && (rangeMoves[0] != 0)) {
      for (i = 0; i < rangeMoves.length; i += 2) {
        if ((c >= rangeMoves[i]) && (c <= rangeMoves[i + 1])) {
          return true;
        } else if ((c < rangeMoves[i]) || (rangeMoves[i] == 0)) {
          break;
        }
      }
    }

    // return (nextForNegatedList != null);
    return false;
  }

  private int MoveFrom(char c, List<NfaState> newStates) {
    if (CanMoveUsingChar(c)) {
      for (int i = next.epsilonMoves.size(); i-- > 0;) {
        NfaState.InsertInOrder(newStates, next.epsilonMoves.get(i));
      }

      return kindToPrint;
    }

    return Integer.MAX_VALUE;
  }

  static int MoveFromSet(char c, List<NfaState> states, List<NfaState> newStates) {
    int tmp;
    int retVal = Integer.MAX_VALUE;

    for (int i = states.size(); i-- > 0;) {
      if (retVal > (tmp = states.get(i).MoveFrom(c, newStates))) {
        retVal = tmp;
      }
    }

    return retVal;
  }


  /*
   * This function generates the bit vectors of low and hi bytes for common bit
   * vectors and returns those that are not common with anything (in loBytes)
   * and returns an array of indices that can be used to generate the function
   * names for char matching using the common bit vectors. It also generates
   * code to match a char with the common bit vectors. (Need a better comment).
   */


  static int AddStartStateSet(String stateSetString, LexerContext lexerContext) {
    return NfaState.AddCompositeStateSet(stateSetString, lexerContext, true);
  }

  private static int AddCompositeStateSet(String stateSetString, LexerContext lexerContext, boolean starts) {
    Integer stateNameToReturn;

    if ((stateNameToReturn = lexerContext.stateNameForComposite.get(stateSetString)) != null) {
      return stateNameToReturn.intValue();
    }

    int toRet = 0;
    int[] nameSet = lexerContext.allNextStates.get(stateSetString);

    if (!starts) {
      lexerContext.stateBlockTable.put(stateSetString, stateSetString);
    }

    if (nameSet == null) {
      throw new Error("JavaCC Bug: Please file a bug at: http://javacc.java.net");
    }

    if (nameSet.length == 1) {
      stateNameToReturn = Integer.valueOf(nameSet[0]);
      lexerContext.stateNameForComposite.put(stateSetString, stateNameToReturn);
      return nameSet[0];
    }

    for (int i = 0; i < nameSet.length; i++) {
      if (nameSet[i] == -1) {
        continue;
      }

      NfaState st = lexerContext.indexedAllStates.get(nameSet[i]);
      st.isComposite = true;
      st.compositeStates = nameSet;
    }

    while ((toRet < nameSet.length) && (starts && (lexerContext.indexedAllStates.get(nameSet[toRet]).inNextOf > 1))) {
      toRet++;
    }

    Enumeration<String> e = lexerContext.compositeStateTable.keys();
    String s;
    while (e.hasMoreElements()) {
      s = e.nextElement();
      if (!s.equals(stateSetString) && NfaState.Intersect(stateSetString, s, lexerContext)) {
        int[] other = lexerContext.compositeStateTable.get(s);

        while ((toRet < nameSet.length) && ((starts && (lexerContext.indexedAllStates.get(nameSet[toRet]).inNextOf > 1))
            || (NfaState.ElemOccurs(nameSet[toRet], other) >= 0))) {
          toRet++;
        }
      }
    }

    int tmp;

    if (toRet >= nameSet.length) {
      // TODO(sreeni) : Fix this mess.
      if ((lexerContext.context.getCodeGenerator() != null)
          || Options.booleanValue(Options.NONUSER_OPTION__INTERPRETER)) {
        tmp = lexerContext.generatedStates++;
      } else {
        if (lexerContext.dummyStateIndex == -1) {
          tmp = lexerContext.dummyStateIndex = lexerContext.generatedStates;
        } else {
          tmp = ++lexerContext.dummyStateIndex;
        }
      }

      if ((lexerContext.context.getCodeGenerator() != null)
          || Options.booleanValue(Options.NONUSER_OPTION__INTERPRETER)) {
        NfaState dummyState = new NfaState(lexerContext);
        dummyState.isComposite = true;
        dummyState.compositeStates = nameSet;
        dummyState.stateName = tmp;
        lexerContext.indexedAllStates.add(dummyState);
        // for (int c : dummyState.compositeStates) {
        // dummyState.compositeStateSet.add(indexedAllStates.get(c));
        // }
      }
    } else {
      tmp = nameSet[toRet];
    }

    stateNameToReturn = Integer.valueOf(tmp);
    lexerContext.stateNameForComposite.put(stateSetString, stateNameToReturn);
    lexerContext.compositeStateTable.put(stateSetString, nameSet);
    if ((lexerContext.context.getCodeGenerator() != null)
        || Options.booleanValue(Options.NONUSER_OPTION__INTERPRETER)) {
      NfaState tmpNfaState = lexerContext.indexedAllStates.get(tmp);
      for (int c : nameSet) {
        if (c < lexerContext.indexedAllStates.size()) {
          tmpNfaState.compositeStateSet.add(lexerContext.indexedAllStates.get(c));
        }
      }
    }

    return tmp;
  }

  int GenerateInitMoves() {
    GetEpsilonMovesString();

    if (epsilonMovesString == null) {
      epsilonMovesString = "null;";
    }

    return NfaState.AddStartStateSet(epsilonMovesString, lexerContext);
  }


  static String GetStateSetString(List<NfaState> states, LexerContext lexerContext) {
    if ((states == null) || (states.size() == 0)) {
      return "null;";
    }

    int[] set = new int[states.size()];
    String retVal = "{ ";
    for (int i = 0; i < states.size();) {
      int k;
      retVal += (k = states.get(i).stateName) + ", ";
      set[i] = k;

      if ((i++ > 0) && ((i % 16) == 0)) {
        retVal += "\n";
      }
    }

    retVal += "};";
    lexerContext.allNextStates.put(retVal, set);
    return retVal;
  }

  private static int ElemOccurs(int elem, int[] arr) {
    for (int i = arr.length; i-- > 0;) {
      if (arr[i] == elem) {
        return i;
      }
    }

    return -1;
  }

  private static boolean Intersect(String set1, String set2, LexerContext lexerContext) {
    if ((set1 == null) || (set2 == null)) {
      return false;
    }

    int[] nameSet1 = lexerContext.allNextStates.get(set1);
    int[] nameSet2 = lexerContext.allNextStates.get(set2);

    if ((nameSet1 == null) || (nameSet2 == null)) {
      return false;
    }

    if (nameSet1 == nameSet2) {
      return true;
    }

    for (int i = nameSet1.length; i-- > 0;) {
      for (int j = nameSet2.length; j-- > 0;) {
        if (nameSet1[i] == nameSet2[j]) {
          return true;
        }
      }
    }

    return false;
  }

  static void UpdateNfaData(int maxState, int startStateName, int lexicalStateIndex, int matchAnyCharKind,
      LexerContext lexerContext) {
    // Cleanup the state set.
    final Set<Integer> done = new HashSet<>();
    List<NfaState> cleanStates = new ArrayList<>();
    NfaState startState = null;
    for (int i = 0; i < lexerContext.allStates.size(); i++) {
      NfaState tmp = lexerContext.allStates.get(i);
      if (tmp.stateName == -1) {
        assert (tmp.kindToPrint == Integer.MAX_VALUE);
        continue;
      }
      if (done.contains(tmp.stateName)) {
        continue;
      }
      done.add(tmp.stateName);
      cleanStates.add(tmp);
      if (tmp.stateName == startStateName) {
        startState = tmp;
        if (tmp.isComposite) {
          for (int c : tmp.compositeStates) {
            tmp.compositeStateSet.add(lexerContext.indexedAllStates.get(c));
          }
        }
      }
    }

    lexerContext.initialStates.put(lexicalStateIndex, startState);
    lexerContext.statesForLexicalState.put(lexicalStateIndex, cleanStates);
    lexerContext.nfaStateOffset.put(lexicalStateIndex, maxState);
    if (matchAnyCharKind > 0) {
      lexerContext.matchAnyChar.put(lexicalStateIndex, matchAnyCharKind);
    } else {
      lexerContext.matchAnyChar.put(lexicalStateIndex, Integer.MAX_VALUE);
    }
  }

  static void BuildTokenizerData(TokenizerData tokenizerData, LexerContext lexerContext) {
    NfaState[] cleanStates;
    List<NfaState> cleanStateList = new ArrayList<>();
    for (int l = 0; l < tokenizerData.lexStateNames.length; l++) {
      int offset = lexerContext.nfaStateOffset.get(l);
      List<NfaState> states = lexerContext.statesForLexicalState.get(l);
      int maxStateName = 0;
      int minStateName = 0;
      for (int i = 0; i < states.size(); i++) {
        NfaState state = states.get(i);
        if (state.stateName == -1) {
          continue;
        }
        assert(state.stateName >= 0 && state.stateName < states.size());
        state.stateName += offset;
        maxStateName = Math.max(state.stateName, maxStateName);
        minStateName = Math.min(state.stateName, minStateName);
      }

      // Some of the NFA states that are epsilon move are mapped to others in the same lexical states so adjust those as well here.
      // See the GenerateCode method where we set the name one state to be that of an equivalent state.
      for (int i = 0; i < states.size(); i++) {
        NfaState s = states.get(i);
        if (s.next != null) {
          assert (s.lexState == s.next.lexState);
          for (NfaState next : s.next.epsilonMoveArray) {
            if (next.stateName != -1 && !states.contains(next)) {
              next.stateName += offset;
              if (!(next.stateName >= minStateName && next.stateName <= maxStateName)) {
                throw new Error("Bug: epsilon move nfa state: " + next.stateName + " not mapped to a valid state in rangE: " + minStateName + "-" + maxStateName);
              }
            }
          }
        }
      }

      cleanStateList.addAll(states);
    }

    cleanStates = new NfaState[cleanStateList.size()];
    Map<Integer, Set<Character>> charsForState = new HashMap<>();
    for (NfaState s : cleanStateList) {
      assert (cleanStates[s.stateName] == null);
      cleanStates[s.stateName] = s;
      Set<Character> chars = new TreeSet<>();
      for (int c = 0; c <= Character.MAX_VALUE; c++) {
        if (s.CanMoveUsingChar((char) c)) {
          chars.add((char) c);
        }
      }
      charsForState.put(s.stateName, chars);
    }

    for (NfaState s : cleanStates) {
      Set<Integer> nextStates = new TreeSet<>();
      if (s.next != null) {
        for (NfaState next : s.next.epsilonMoveArray) {
          nextStates.add(next.stateName);
        }
      }

      Set<Integer> composite = new TreeSet<>();
      if (s.isComposite) {
        for (NfaState c : s.compositeStateSet) {
          composite.add(c.stateName);
        }
      }

      tokenizerData.addNfaState(s.stateName, charsForState.get(s.stateName), nextStates, composite, s.kindToPrint);
    }

    Map<Integer, Integer> initStates = new HashMap<>();
    for (int l = 0; l < tokenizerData.lexStateNames.length; l++) {
      if (lexerContext.initialStates.get(l) == null) {
        initStates.put(l, -1);
      } else {
        initStates.put(l, lexerContext.initialStates.get(l).stateName);
      }
    }
    tokenizerData.setInitialStates(initStates);
    tokenizerData.setWildcardKind(lexerContext.matchAnyChar);
  }

  static NfaState getNfaState(int index, LexerContext lexerContext) {
    if (index == -1) {
      return null;
    }
    return lexerContext.indexedAllStates.get(index);
  }
}
