
package org.javacc.cpp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.Options;
import org.javacc.parser.TokenizerData;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that implements a table driven code generator for the token manager in
 * C++.
 */
class TokenManagerCodeGenerator implements org.javacc.parser.TokenManagerCodeGenerator {

  private static final String TokenManagerTemplate  = "/templates/cpp/TableDrivenTokenManager.cc.template";
  private static final String TokenManagerTemplateH = "/templates/cpp/TableDrivenTokenManager.h.template";

  private final Context 	context;
  private CppCodeBuilder    codeGenerator;

  TokenManagerCodeGenerator(Context context) {
    this.context = context;
  }

  @Override
  public void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    settings.putAll(Options.getOptions());
    settings.put(Options.NONUSER_OPTION__PARSER_NAME_UPPER_CASE, tokenizerData.parserName.toUpperCase());

    settings.put("maxOrdinal", tokenizerData.allMatches.size());
    settings.put("firstLexState", tokenizerData.lexStateNames[0]);
    settings.put("lastLexState", tokenizerData.lexStateNames[tokenizerData.lexStateNames.length - 1]);
    settings.put("nfaSize", tokenizerData.nfa.size());
    settings.put("charsVectorSize", ((Character.MAX_VALUE >> 6) + 1));
    settings.put("stateSetSize", tokenizerData.nfa.size());
    settings.put("parserName", tokenizerData.parserName);
    settings.put("maxLongs", (tokenizerData.allMatches.size() / 64) + 1);
    settings.put("parserName", tokenizerData.parserName);
    settings.put("charStreamName", Options.getCharStreamName());
    settings.put("defaultLexState", tokenizerData.defaultLexState);
    settings.put("decls", tokenizerData.decls);
    settings.put("noDfa", Options.getNoDfa());
    settings.put("generatedStates", tokenizerData.nfa.size());
    if (Options.getTokenClass().isEmpty())
    	settings.put("tokenClass", "Token");
    else
    	settings.put("tokenClass", Options.getTokenClass());
    if (Options.getTokenInclude().isEmpty())
    	settings.put("tokenInclude", "Token.h");
    else
    	settings.put("tokenInclude", Options.getTokenInclude());

    File file = new File(Options.getOutputDirectory(), tokenizerData.parserName + "TokenManager.cc");
    try {
      codeGenerator = CppCodeBuilder.of(context, settings).setFile(file);

      if (Options.hasNamespace()) {
        codeGenerator.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      codeGenerator.printTemplate(TokenManagerTemplate);

      codeGenerator.switchToIncludeFile(); // remaining variables

      codeGenerator.printTemplate(TokenManagerTemplateH, settings);
      codeGenerator.switchToStaticsFile();
      codeGenerator.println("#include \"TokenManagerError.h\"");
      codeGenerator.println("#include \"DefaultTokenManagerErrorHandler.h\"");

      if(!Options.getNoDfa())
      	dumpDfaTables(codeGenerator, tokenizerData);
      dumpNfaTables(codeGenerator, tokenizerData);
      dumpMatchInfo(codeGenerator, tokenizerData);
    } catch (IOException ioe) {
      assert (false);
    }
  }

  @Override
  public void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    if (!Options.getBuildTokenManager()) {
      return;
    }

    if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
      codeGenerator.switchToMainFile();
      codeGenerator.println(Options.stringValue("NAMESPACE_CLOSE"));
    }

    try {
      codeGenerator.close();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  private void dumpDfaTables(CppCodeBuilder codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, int[]> startAndSize = new HashMap<>();
    int i = 0;

    codeGenerator.println();
    codeGenerator.println("static const long long stringLiterals[] = {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = new int[2];
      List<String> l = tokenizerData.literalSequence.get(key);
      List<Integer> kinds = tokenizerData.literalKinds.get(key);
      arr[0] = i;
      arr[1] = l.size();
      int j = 0;
      if (i > 0) {
        codeGenerator.println(", ");
      }
      for (String s : l) {
        if (j > 0) {
          codeGenerator.println(", ");
        }
        int kind = kinds.get(j);
        boolean ignoreCase = tokenizerData.ignoreCaseKinds.contains(kind);
        codeGenerator.print(s.length() + "LL");
        codeGenerator.print(", ");
        codeGenerator.print(ignoreCase ? 1 : 0);
        for (int k = 0; k < s.length(); k++) {
          codeGenerator.print(", ");
          codeGenerator.print((int) s.charAt(k) + "LL");
          i++;
        }
        if (ignoreCase) {
          for (int k = 0; k < s.length(); k++) {
            codeGenerator.print(", ");
            codeGenerator.print((int) s.toUpperCase().charAt(k) + "LL");
            i++;
          }
        }
        codeGenerator.print(", " + kind + "LL");
        codeGenerator.print(", " + tokenizerData.kindToNfaStartState.get(kind) + "LL");
        i += 4;
        j++;
      }
      startAndSize.put(key, arr);
    }
    codeGenerator.println("};");
    codeGenerator.println();

    codeGenerator.switchToMainFile();
    // Token actions.
    codeGenerator
    .println("int " + tokenizerData.parserName + "TokenManager::getStartAndSize(int index, int isCount)\n{");
    codeGenerator.println("  switch(index) {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = startAndSize.get(key);
      codeGenerator.println("    case " + key + ": { return (isCount == 0) ? " + arr[0] + " : " + arr[1] + ";}");
    }
    codeGenerator.println("  }");
    codeGenerator.println("  return -1;");
    codeGenerator.println("}\n");

    codeGenerator.switchToStaticsFile();
  }

  private void dumpNfaTables(CppCodeBuilder codeGenerator, TokenizerData tokenizerData) {
    // WE do the following for java so that the generated code is reasonable
    // size and can be compiled. May not be needed for other languages.
    Map<Integer, TokenizerData.NfaState> nfa = tokenizerData.nfa;

    int length = 0;
    int lengths[] = new int[nfa.size()];
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (tmp != null) {
        BitSet bits = new BitSet();
        for (char c : tmp.characters) {
          bits.set(c);
        }

        lengths[i] = 0;
        long[] longs = bits.toLongArray();
        for (int k = 0; k < longs.length; k++) {
          int rep = 1;
          while (((k + rep) < longs.length) && (longs[k + rep] == longs[k])) {
            rep++;
          }
          k += rep - 1;
          lengths[i] = lengths[i] + 2;
        }
        length = Math.max(length, lengths[i]);
      }
    }

    if (Options.getCppUseArray()) {
    	codeGenerator.print("static const Array<");
        codeGenerator.print(length + 1);
        codeGenerator.println(", long long> jjCharData[] = {");
    } else {
    	codeGenerator.println("static const long long jjCharData[][" + length + 1 + "] = {");
    }
    
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(",");
      }
      if (tmp == null) {
        codeGenerator.print("{}");
        continue;
      }
      codeGenerator.print("{");
      BitSet bits = new BitSet();
      for (char c : tmp.characters) {
        bits.set(c);
      }
      long[] longs = bits.toLongArray();
      codeGenerator.print(lengths[i] + "LL");
      for (int k = 0; k < longs.length; k++) {
        int rep = 1;
        while (((k + rep) < longs.length) && (longs[k + rep] == longs[k])) {
          rep++;
        }
        codeGenerator.print(", ", rep + "LL, ");
        if (longs[k] == Long.MIN_VALUE) {
            codeGenerator.print("LLONG_MIN");
        } else {
        	codeGenerator.print("" + Long.toString(longs[k]) + "LL");
        }
        k += rep - 1;
      }
      codeGenerator.print("}");
    }
    codeGenerator.println("};");
    codeGenerator.println();

    length = 0;
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (tmp == null) {
        continue;
      }
      length = Math.max(length, tmp.compositeStates.size());
    }
    if (Options.getCppUseArray()) {
      codeGenerator.print("static const Array<");
      codeGenerator.print(length);
      codeGenerator.println(", int> jjcompositeState[] = {");
    } else {
        codeGenerator.println("static const int jjcompositeState[][" + length + "] = {");
    }
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      if (tmp == null) {
        codeGenerator.print("{}");
        continue;
      }
      codeGenerator.print("{");
      int k = 0;
      for (int st : tmp.compositeStates) {
        if (k++ > 0) {
          codeGenerator.print(", ");
        }
        codeGenerator.print(st);
      }
      codeGenerator.print("}");
    }
    codeGenerator.println("};");
    codeGenerator.println();

    codeGenerator.println("static const int jjmatchKinds[] = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      // TODO(sreeni) : Fix this mess.
      if (tmp == null) {
        codeGenerator.print("ALLBITSUP");
        continue;
      }
      codeGenerator.print(tmp.kind);
    }
    codeGenerator.println("};");
    codeGenerator.println();

    length = 0;
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (tmp == null) {
        continue;
      }
      length = Math.max(length, tmp.nextStates.size());
    }
    if (Options.getCppUseArray()) {
        codeGenerator.print("static const Array<");
        codeGenerator.print(length + 1);
        codeGenerator.println(", int> jjnextStateSet[] = {");
    } else {
    	codeGenerator.println("static const int jjnextStateSet[][" + (length + 1) + "] = {");
    }
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      if (tmp == null) {
        codeGenerator.print("{0}");
        continue;
      }
      codeGenerator.print("{");
      codeGenerator.print(tmp.nextStates.size());
      for (int s : tmp.nextStates) {
        codeGenerator.print(", ");
        codeGenerator.print(s);
      }
      codeGenerator.print("}");
    }
    codeGenerator.println("};");
    codeGenerator.println();

    codeGenerator.println("static const int jjInitStates[]  = {");
    int k = 0;
    for (int i : tokenizerData.initialStates.keySet()) {
      if (k++ > 0) {
        codeGenerator.print(", ");
      }
      codeGenerator.print(tokenizerData.initialStates.get(i));
    }
    codeGenerator.println("};");
    codeGenerator.println();

    codeGenerator.println("static const int canMatchAnyChar[] = {");
    k = 0;
    for (int i = 0; i < tokenizerData.wildcardKind.size(); i++) {
      if (k++ > 0) {
        codeGenerator.print(", ");
      }
      codeGenerator.print(tokenizerData.wildcardKind.get(i));
    }
    codeGenerator.println("};");
    codeGenerator.println();
  }
 
  private void printWide(String image) {
      if (image != null) {
        codeGenerator.print("JJWIDE(");
        for (int j = 0; j < image.length(); j++) {
          if (image.charAt(j) <= 0xff) {
            codeGenerator.print("\\" + Integer.toOctalString(image.charAt(j)));
          } else {
            String hexVal = Integer.toHexString(image.charAt(j));
            if (hexVal.length() == 3) {
              hexVal = "0" + hexVal;
            }
            codeGenerator.print("\\u" + hexVal);
          }
        }
        codeGenerator.print(")");
      } else {
        codeGenerator.print("JJEMPTY");
      }
  }
  private void dumpMatchInfo(CppCodeBuilder codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, TokenizerData.MatchInfo> allMatches = tokenizerData.allMatches;

    // A bit ugly.

    BitSet toSkip = new BitSet(allMatches.size());
    BitSet toSpecial = new BitSet(allMatches.size());
    BitSet toMore = new BitSet(allMatches.size());
    BitSet toToken = new BitSet(allMatches.size());
    int[] newStates = new int[allMatches.size()];
    toSkip.set(allMatches.size() + 1, true);
    toToken.set(allMatches.size() + 1, true);
    toMore.set(allMatches.size() + 1, true);
    toSpecial.set(allMatches.size() + 1, true);
    // Kind map.
    codeGenerator.println("static const JJString jjstrLiteralImages[] = {");
	int k = 0;
    for (int i : allMatches.keySet()) {
	      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
	      switch (matchInfo.matchType) {
	        case SKIP:
	          toSkip.set(i);
	          break;
	        case SPECIAL_TOKEN:
	          toSpecial.set(i);
	          break;
	        case MORE:
	          toMore.set(i);
	          break;
	        case TOKEN:
	          toToken.set(i);
	          break;
	      }
	      newStates[i] = matchInfo.newLexState;
	      String image = matchInfo.image;
	      if (k++ > 0) {
	        codeGenerator.println(", ");
	      }
	      printWide(image);
    }
    codeGenerator.println();
    codeGenerator.println("};");
    codeGenerator.println();

    // Now generate the bit masks.
    TokenManagerCodeGenerator.generateBitVector("jjtoSkip", toSkip, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoSpecial", toSpecial, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoMore", toMore, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoToken", toToken, codeGenerator);

    codeGenerator.println("static const int jjnewLexState[] = {");
    for (int i = 0; i < newStates.length; i++) {
      if (i > 0) {
        codeGenerator.print(", ");
      }
      // codeGenerator.genCode("0x" + Integer.toHexString(newStates[i]));
      codeGenerator.print("" + Integer.toString(newStates[i]));
    }
    codeGenerator.println("};");
    codeGenerator.println();

    // Action functions.

    // Token actions.
    codeGenerator.switchToMainFile();
    codeGenerator
    .println("void " + tokenizerData.parserName + "TokenManager::tokenLexicalActions(Token* matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.TOKEN, "matchedToken->kind()", codeGenerator);
    codeGenerator.println("}");

    codeGenerator
    .println("void " + tokenizerData.parserName + "TokenManager::skipLexicalActions(const Token* matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SKIP, "jjmatchedKind", codeGenerator);
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SPECIAL_TOKEN, "jjmatchedKind", codeGenerator);
    codeGenerator.println("}");

    // More actions.
    codeGenerator.println("void " + tokenizerData.parserName + "TokenManager::moreLexicalActions() {");
    codeGenerator.println("jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.MORE, "jjmatchedKind", codeGenerator);
    codeGenerator.println("}");
    codeGenerator.switchToStaticsFile();

    codeGenerator.printLiteralArray("lexStateNames", tokenizerData.lexStateNames);
  }

  private void dumpLexicalActions(Map<Integer, TokenizerData.MatchInfo> allMatches, TokenizerData.MatchType matchType, String kindString, CppCodeBuilder codeGenerator) {
    switch(matchType) {
    case SKIP: break;
    //codeGenerator.println("  if (curLexState == DEFAULT || curLexState == " + {");	  
    case SPECIAL_TOKEN: break;
    default: break;
    
    }
    codeGenerator.println("  switch(" + kindString + ") {");
    for (int i : allMatches.keySet()) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      if ((matchInfo.action == null) || (matchInfo.matchType != matchType)) {
        continue;
      }
      codeGenerator.println("    case " + i + ": {\n");
      codeGenerator.println("      " + matchInfo.action);
      codeGenerator.println("      break;");
      codeGenerator.println("    }");
    }
    codeGenerator.println("    default: break;");
    codeGenerator.println("  }");
  }

  private static void generateBitVector(String name, BitSet bits, CppCodeBuilder codeGenerator) {
    codeGenerator.println("static const unsigned long long " + name + "[] = {");
    codeGenerator.print("   ");
    long[] longs = bits.toLongArray();
    for (int i = 0; i < longs.length; i++) {
      if (i > 0) {
        codeGenerator.print(", ");
      }
      codeGenerator.print("" + Long.toUnsignedString(longs[i]) + "ULL");
    }
    codeGenerator.println("};");
  }
}
