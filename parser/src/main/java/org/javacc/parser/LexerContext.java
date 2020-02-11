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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The lexer context provides all variables for a {@link LexGen} instance.
 */
class LexerContext {

  final JavaCCContext context;
  int                 lexStateIndex;
  int                 curKind;
  RegularExpression   curRE;
  int[]               lexStates;
  int[]               canMatchAnyChar;
  boolean[]           ignoreCase;
  boolean[]           mixed;


  // NFAState variables
  boolean                          unicodeWarningGiven   = false;
  int                              generatedStates       = 0;

  int                              idCnt                 = 0;
  int                              dummyStateIndex       = -1;
  boolean                          done;
  boolean                          mark[];
  //
  List<NfaState>                   allStates             = new ArrayList<>();
  List<NfaState>                   indexedAllStates      = new ArrayList<>();
  Hashtable<String, NfaState>      equivStatesTable      = new Hashtable<>();
  Hashtable<String, int[]>         allNextStates         = new Hashtable<>();
  Hashtable<String, Integer>       stateNameForComposite = new Hashtable<>();
  Hashtable<String, int[]>         compositeStateTable   = new Hashtable<>();
  Hashtable<String, String>        stateBlockTable       = new Hashtable<>();
  private Hashtable<String, int[]> stateSetsToFix        = new Hashtable<>();


  final Map<Integer, NfaState>       initialStates         = new HashMap<>();
  final Map<Integer, List<NfaState>> statesForLexicalState = new HashMap<>();
  final Map<Integer, Integer>        nfaStateOffset        = new HashMap<>();
  final Map<Integer, Integer>        matchAnyChar          = new HashMap<>();


  // RStringLiteral variable
  int                         maxStrKind      = 0;
  int                         maxLen          = 0;
  int[]                       maxLenForActive = new int[100]; // 6400
  // tokens
  int[][]                     intermediateKinds;
  int[][]                     intermediateMatchedPos;

  boolean                     subString[];
  boolean                     subStringAtPos[];
  Hashtable<String, long[]>[] statesForPos;


  String[]                          allImages;
  final Map<Integer, List<String>>  literalsByLength   = new HashMap<>();
  final Map<Integer, List<Integer>> literalKinds       = new HashMap<>();
  final Map<Integer, Integer>       kindToLexicalState = new HashMap<>();
  final Set<Integer>                kindToIgnoreCase   = new HashSet<>();
  final Map<Integer, NfaState>      nfaStateMap        = new HashMap<>();

  LexerContext(JavaCCContext context) {
    this.context = context;
    canMatchAnyChar = null;
    curKind = 0;
    curRE = null;
    ignoreCase = null;
    lexStateIndex = 0;
    lexStates = null;
    mixed = null;

    clear();

    // NfaState variables
    initialStates.clear();
    statesForLexicalState.clear();
    nfaStateOffset.clear();
    matchAnyChar.clear();

    // RStringLiteral variables
    allImages = null;
    literalsByLength.clear();
    literalKinds.clear();
    kindToLexicalState.clear();
    kindToIgnoreCase.clear();
    nfaStateMap.clear();
  }

  /**
   * Initialize all the variables, so that there is no interference between the
   * various states of the lexer.
   *
   * Need to call this method after generating code for each lexical state.
   */
  protected final void clear() {
    // NFAState variables
    generatedStates = 0;
    idCnt = 0;
    dummyStateIndex = -1;
    done = false;
    mark = null;
    allStates.clear();
    indexedAllStates.clear();
    equivStatesTable.clear();
    allNextStates.clear();
    compositeStateTable.clear();
    stateBlockTable.clear();
    stateNameForComposite.clear();
    stateSetsToFix.clear();


    // RStringLiteral variables
    maxStrKind = 0;
    maxLen = 0;
    maxLenForActive = new int[100]; // 6400 tokens
    intermediateKinds = null;
    intermediateMatchedPos = null;
    subString = null;
    subStringAtPos = null;
    statesForPos = null;
  }
}
