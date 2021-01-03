
package org.javacc.csharp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.Options;
import org.javacc.parser.TokenizerData;
import org.javacc.utils.CodeBuilder;
import org.javacc.utils.CodeBuilder.GenericCodeBuilder;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that implements a table driven code generator for the token manager in
 * java.
 */
class TokenManagerCodeGenerator implements org.javacc.parser.TokenManagerCodeGenerator {

  private static final String tokenManagerTemplate = "/templates/csharp/TokenManagerDriver.template";

  private final Context context;
  private GenericCodeBuilder  codeGenerator;


  TokenManagerCodeGenerator(Context context) {
    this.context = context;
  }

  @Override
  public void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    String superClass = (String) settings.get(Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    settings.put("maxOrdinal", tokenizerData.allMatches.size());
    settings.put("maxLexStates", tokenizerData.lexStateNames.length);
    settings.put("nfaSize", tokenizerData.nfa.size());
    settings.put("charsVectorSize", ((Character.MAX_VALUE >> 6) + 1));
    settings.put("stateSetSize", tokenizerData.nfa.size());
    settings.put("parserName", tokenizerData.parserName);
    settings.put("maxLongs", (tokenizerData.allMatches.size() / 64) + 1);
    settings.put("parserName", tokenizerData.parserName);
    settings.put("charStreamName", "ICharStream");
    settings.put("defaultLexState", tokenizerData.defaultLexState);
    settings.put("decls", tokenizerData.decls);
    settings.put("superClass", ((superClass == null) || superClass.equals("")) ? "" : " :  " + superClass);
    settings.put("noDfa", Options.getNoDfa());
    if (Options.hasNamespace()) {
      settings.put("NAMESPACE", Options.getNamespace());
    }
    settings.put("generatedStates", tokenizerData.nfa.size());

    File file = new File(Options.getOutputDirectory(), tokenizerData.parserName + "TokenManager.cs");
    try {
      codeGenerator = GenericCodeBuilder.of(context, settings).setFile(file);
      if (!"".equals(Options.getNamespace())) {
        codeGenerator.println("namespace " + Options.getNamespace() + " {\n");
      }

      generateConstantsClass(tokenizerData);

      codeGenerator.printTemplate(TokenManagerCodeGenerator.tokenManagerTemplate);
      dumpDfaTables(codeGenerator, tokenizerData);
      dumpNfaTables(codeGenerator, tokenizerData);
      dumpMatchInfo(codeGenerator, tokenizerData);
      codeGenerator.print(
          "static " + tokenizerData.parserName + "TokenManager() {\n  InitStringLiteralData();\n  InitNfaData(); } ");
    } catch (IOException ioe) {
      assert (false);
    }
  }

  @Override
  public void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    // TODO(sreeni) : Fix this mess.
    codeGenerator.println("\n}");

    if (!"".equals(Options.getNamespace())) {
      codeGenerator.println("\n}");
    }
    if (!Options.getBuildTokenManager()) {
      return;
    }

    try {
      codeGenerator.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void dumpDfaTables(CodeBuilder<?> codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, int[]> startAndSize = new HashMap<>();
    int i = 0;

    codeGenerator.println("private static readonly int[] stringLiterals = {");
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
        codeGenerator.print(s.length());
        for (int k = 0; k < s.length(); k++) {
          codeGenerator.print(", ");
          codeGenerator.print((int) s.charAt(k));
          i++;
        }
        int kind = kinds.get(j);
        codeGenerator.print(", " + kind);
        codeGenerator.print(", " + tokenizerData.kindToNfaStartState.get(kind));
        i += 3;
        j++;
      }
      startAndSize.put(key, arr);
    }
    codeGenerator.println("};");

    // Static block to actually initialize the map from the int array above.
    codeGenerator.println("static void InitStringLiteralData() {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = startAndSize.get(key);
      codeGenerator.println("startAndSize[" + key + "] = new int[]{" + arr[0] + ", " + arr[1] + "};");
    }
    codeGenerator.println("}");
  }

  private void dumpNfaTables(CodeBuilder<?> codeGenerator, TokenizerData tokenizerData) {
    // WE do the following for java so that the generated code is reasonable
    // size and can be compiled. May not be needed for other languages.
    codeGenerator.println("private static readonly long[][] jjCharData = {");
    Map<Integer, TokenizerData.NfaState> nfa = tokenizerData.nfa;
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(",");
      }
      if (tmp == null) {
        codeGenerator.print("new long[] {}");
        continue;
      }
      codeGenerator.print("new long[] {");
      BitSet bits = new BitSet();
      for (char c : tmp.characters) {
        bits.set(c);
      }
      long[] longs = bits.toLongArray();
      for (int k = 0; k < longs.length; k++) {
        int rep = 1;
        while (((k + rep) < longs.length) && (longs[k + rep] == longs[k])) {
          rep++;
        }
        if (k > 0) {
          codeGenerator.print(", ");
        }
        codeGenerator.print(rep + ", ");
        // codeGenerator.genCode("0x" + Long.toHexString(longs[k]) + "L");
        codeGenerator.print("" + Long.toString(longs[k]) + "L");
        k += rep - 1;
      }
      codeGenerator.print("}");
    }
    codeGenerator.println("};");

    codeGenerator.println("private static readonly int[][] jjcompositeState = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      if (tmp == null) {
        codeGenerator.print("new int[]{}");
        continue;
      }
      codeGenerator.print("new int[]{");
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

    codeGenerator.println("private static readonly int[] jjmatchKinds = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      // TODO(sreeni) : Fix this mess.
      if (tmp == null) {
        codeGenerator.print(Integer.MAX_VALUE);
        continue;
      }
      codeGenerator.print(tmp.kind);
    }
    codeGenerator.println("};");

    codeGenerator.println("private static readonly int[][]  jjnextStateSet = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) {
        codeGenerator.println(", ");
      }
      if (tmp == null) {
        codeGenerator.print("new int[]{}");
        continue;
      }
      int k = 0;
      codeGenerator.print("new int[]{");
      for (int s : tmp.nextStates) {
        if (k++ > 0) {
          codeGenerator.print(", ");
        }
        codeGenerator.print(s);
      }
      codeGenerator.print("}");
    }
    codeGenerator.println("};");

    codeGenerator.println("private static readonly int[] jjInitStates  = {");
    int k = 0;
    for (int i : tokenizerData.initialStates.keySet()) {
      if (k++ > 0) {
        codeGenerator.print(", ");
      }
      codeGenerator.print(tokenizerData.initialStates.get(i));
    }
    codeGenerator.println("};");

    codeGenerator.println("private static readonly int[] canMatchAnyChar = {");
    k = 0;
    for (int i = 0; i < tokenizerData.wildcardKind.size(); i++) {
      if (k++ > 0) {
        codeGenerator.print(", ");
      }
      codeGenerator.print(tokenizerData.wildcardKind.get(i));
    }
    codeGenerator.println("};");
  }

  private void dumpMatchInfo(CodeBuilder<?> codeGenerator, TokenizerData tokenizerData) {
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
    codeGenerator.println("public static readonly string[] jjstrLiteralImages = {");

    int k = 0;
    for (int i = 0; i < allMatches.size(); i++) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      switch (matchInfo.matchType) {
        case SKIP:
          toSkip.set(i);
          break;
        case SPECIAL_TOKEN:
          toSkip.set(i);
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
      if (image != null) {
        codeGenerator.print("\"");
        for (int j = 0; j < image.length(); j++) {
          if (image.charAt(j) <= 0xff) {
            codeGenerator.print("\\0" + Integer.toOctalString(image.charAt(j)));
          } else {
            String hexVal = Integer.toHexString(image.charAt(j));
            if (hexVal.length() == 3) {
              hexVal = "0" + hexVal;
            }
            codeGenerator.print("\\u" + hexVal);
          }
        }
        codeGenerator.print("\"");
      } else {
        codeGenerator.println("null");
      }
    }
    codeGenerator.println("};");

    // Now generate the bit masks.
    TokenManagerCodeGenerator.generateBitVector("jjtoSkip", toSkip, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoSpecial", toSpecial, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoMore", toMore, codeGenerator);
    TokenManagerCodeGenerator.generateBitVector("jjtoToken", toToken, codeGenerator);

    codeGenerator.println("private static readonly int[] jjnewLexState = {");
    for (int i = 0; i < newStates.length; i++) {
      if (i > 0) {
        codeGenerator.print(", ");
      }
      // codeGenerator.genCode("0x" + Integer.toHexString(newStates[i]));
      codeGenerator.print("" + Integer.toString(newStates[i]));
    }
    codeGenerator.println("};");

    // Action functions.

    final String staticString = "";
    // Token actions.
    codeGenerator.println(staticString + "void TokenLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.TOKEN, "matchedToken.kind", codeGenerator);
    codeGenerator.println("}");

    // Skip actions.
    // TODO(sreeni) : Streamline this mess.

    codeGenerator.println(staticString + "void SkipLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SKIP, "jjmatchedKind", codeGenerator);
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SPECIAL_TOKEN, "jjmatchedKind", codeGenerator);
    codeGenerator.println("}");

    // More actions.
    codeGenerator.println(staticString + "void MoreLexicalActions() {");
    codeGenerator.println("jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.MORE, "jjmatchedKind", codeGenerator);
    codeGenerator.println("}");
  }

  private void dumpLexicalActions(Map<Integer, TokenizerData.MatchInfo> allMatches, TokenizerData.MatchType matchType,
      String kindString, CodeBuilder<?> codeGenerator) {
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

  private static void generateBitVector(String name, BitSet bits, CodeBuilder<?> codeGenerator) {
    codeGenerator.println("private static readonly long[] " + name + " = {");
    long[] longs = bits.toLongArray();
    for (int i = 0; i < longs.length; i++) {
      if (i > 0) {
        codeGenerator.print(", ");
      }
      // codeGenerator.genCode("0x" + Long.toHexString(longs[i]) + "L");
      codeGenerator.print("" + Long.toString(longs[i]) + "L");
    }
    codeGenerator.println("};");
  }

  private void generateConstantsClass(TokenizerData tokenizerData) {

    codeGenerator.println("public class " + tokenizerData.parserName + "Constants {");

    codeGenerator.println("public const int EOF  = 0;");
    for (Integer i : tokenizerData.labels.keySet()) {
      codeGenerator.println("public const int " + tokenizerData.labels.get(i) + " = " + i + ";");
    }

    codeGenerator.print("public static string[] tokenImage = { ");
    for (int i = 0; i < tokenizerData.images.length; i++) {
      if (i > 0) {
        codeGenerator.println(", ");
      }
      if (tokenizerData.images[i] == null) {
        codeGenerator.print("null");
      } else {
        codeGenerator.print("@\"" + tokenizerData.images[i].replace("\"", "\"\"") + "\"");
      }
    }
    codeGenerator.println("};");

    codeGenerator.println("public static string[] lexStateNames = {");
    for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
      if (i > 0) {
        codeGenerator.println(", ");
      }
      codeGenerator.println("\"" + tokenizerData.lexStateNames[i] + "\"");
    }
    codeGenerator.println("};");

    for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
      codeGenerator.println("public const int " + tokenizerData.lexStateNames[i] + " = " + i + ";");
    }

    codeGenerator.println("};");
  }
}
