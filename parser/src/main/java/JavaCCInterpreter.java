
import org.javacc.parser.JavaCCContext;
import org.javacc.parser.JavaCCParser;
import org.javacc.parser.LexGen;
import org.javacc.parser.Main;
import org.javacc.parser.MetaParseException;
import org.javacc.parser.Options;
import org.javacc.parser.Semanticize;
import org.javacc.parser.TokenizerData;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JavaCCInterpreter {

  public static void main(String[] args) throws Exception {
    // Initialize all static state
    JavaCCContext context = Main.reInitAll();
    Options.set(Options.NONUSER_OPTION__INTERPRETER, true);
    Options.set("STATIC", false);
    // TODO JavaCCParser parser = null;
    for (int arg = 0; arg < (args.length - 2); arg++) {
      if (!Options.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.");
        System.exit(1);
      }
      Options.setCmdLineOption(args[arg]);
    }

    String input = "";
    String grammar = "";
    try {
      File fp = new File(args[args.length - 2]);
      byte[] buf = new byte[(int) fp.length()];

      try (DataInputStream istream = new DataInputStream(new BufferedInputStream(new FileInputStream(fp)))) {
        istream.readFully(buf);
      }
      grammar = new String(buf);
      File inputFile = new File(args[args.length - 1]);
      buf = new byte[(int) inputFile.length()];
      try (DataInputStream istream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
        istream.readFully(buf);
      }
      input = new String(buf);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
    JavaCCInterpreter interp = new JavaCCInterpreter();
    interp.runTokenizer(grammar, input, context);
  }

  public void runTokenizer(String grammar, String input, JavaCCContext context) {
    try {
      JavaCCParser parser = new JavaCCParser(new StringReader(grammar));
      parser.javacc_input(context);
      // Options.init();
      Options.set(Options.NONUSER_OPTION__INTERPRETER, true);
      Semanticize.start(context);
      LexGen lg = new LexGen(context);
      TokenizerData tokenizerData = lg.generateTokenizerData(true, false);
      if (context.errors().get_error_count() == 0) {
        long l = System.currentTimeMillis();
        JavaCCInterpreter.tokenize(tokenizerData, input);
        System.err.println("Tokenized in: " + (System.currentTimeMillis() - l));
      }
    } catch (MetaParseException e) {
      System.out.println("Detected " + context.errors().get_error_count() + " errors and "
          + context.errors().get_warning_count() + " warnings.");
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.toString());
      System.out.println("Detected " + (context.errors().get_error_count() + 1) + " errors and "
          + context.errors().get_warning_count() + " warnings.");
      System.exit(1);
    }
  }

  static int                   line, col;
  static boolean               prevCR;
  static Map<Integer, Integer> lineBoundaries = new TreeMap<>();
  static int                   maxpos         = -1;

  static int getLine(int pos) {
    for (int key : JavaCCInterpreter.lineBoundaries.keySet()) {
      if (pos >= key) {
        return JavaCCInterpreter.lineBoundaries.get(key);
      }
    }
    return -1;
  }

  static void updateLineCol(int pos, char c) {
    if (pos < JavaCCInterpreter.maxpos) {
      return;
    }
    JavaCCInterpreter.maxpos = pos;
    if ((c == '\r') || ((c == '\n') && !JavaCCInterpreter.prevCR)) {
      JavaCCInterpreter.line++;
      JavaCCInterpreter.col = 0;
      JavaCCInterpreter.prevCR = c == '\r';
      JavaCCInterpreter.lineBoundaries.put(pos, JavaCCInterpreter.line);
    } else {
      JavaCCInterpreter.col++;
    }
  }

  public static void tokenize(TokenizerData tokenizerData, String input) {
    // First match the string literals.
    final int input_size = input.length();
    int curPos = 0;
    JavaCCInterpreter.line = 1;
    JavaCCInterpreter.col = 0;
    int curLexState = tokenizerData.defaultLexState;
    Set<Integer> curStates = new HashSet<>();
    Set<Integer> newStates = new HashSet<>();
    int tokline, tokcol;
    System.out.println("*** Starting in lexical state: " + tokenizerData.lexStateNames[curLexState]);
    while (curPos < input_size) {
      int beg = curPos;
      int matchedPos = beg;
      int matchedKind = Integer.MAX_VALUE;
      int nfaStartState = tokenizerData.initialStates.get(curLexState);

      char c = input.charAt(curPos);
      JavaCCInterpreter.updateLineCol(curPos, c);
      if (Options.getIgnoreCase()) {
        c = Character.toLowerCase(c);
      }
      int key = (curLexState << 16) | c;
      final List<String> literals = tokenizerData.literalSequence.get(key);
      tokline = JavaCCInterpreter.getLine(curPos);
      tokcol = JavaCCInterpreter.col;

      if (literals != null) {
        // We need to go in order so that the longest match works.
        for (int litIndex = 0; litIndex < literals.size(); litIndex++) {
          String s = literals.get(litIndex);
          int charIndex = 1;
          // See which literal matches.
          while ((charIndex < s.length()) && ((curPos + charIndex) < input_size)) {
            c = input.charAt(curPos + charIndex);
            JavaCCInterpreter.updateLineCol(curPos + charIndex, c);
            if (Options.getIgnoreCase()) {
              c = Character.toLowerCase(c);
            }
            if (c != s.charAt(charIndex)) {
              break;
            }
            charIndex++;
          }
          if (charIndex == s.length()) {
            // Found a string literal match.
            matchedKind = tokenizerData.literalKinds.get(key).get(litIndex);
            matchedPos = (curPos + charIndex) - 1;
            nfaStartState = tokenizerData.kindToNfaStartState.get(matchedKind);
            curPos += charIndex;
            break;
          }
        }
      }

      if (nfaStartState != -1) {
        // We need to add the composite states first.
        curStates.add(nfaStartState);
        curStates.addAll(tokenizerData.nfa.get(nfaStartState).compositeStates);
        do {
          int kind = Integer.MAX_VALUE;
          c = input.charAt(curPos);
          JavaCCInterpreter.updateLineCol(curPos, c);
          if (Options.getIgnoreCase()) {
            c = Character.toLowerCase(c);
          }

          for (int state : curStates) {
            TokenizerData.NfaState nfaState = tokenizerData.nfa.get(state);
            if (nfaState.characters.contains(c)) {
              if (kind > nfaState.kind) {
                kind = nfaState.kind;
              }

              newStates.addAll(nfaState.nextStates);
            }
          }

          Set<Integer> tmp = newStates;
          newStates = curStates;
          curStates = tmp;
          newStates.clear();
          if (kind != Integer.MAX_VALUE) {
            matchedKind = kind;
            matchedPos = curPos;
          }
        } while (!curStates.isEmpty() && (++curPos < input_size));
      }

      if ((matchedPos == beg) && (matchedKind > tokenizerData.wildcardKind.get(curLexState))) {
        matchedKind = tokenizerData.wildcardKind.get(curLexState);
      }
      if (matchedKind != Integer.MAX_VALUE) {
        TokenizerData.MatchInfo matchInfo = tokenizerData.allMatches.get(matchedKind);
        if (matchInfo.action != null) {
          System.err.println("Actions not implemented (yet) in intererpreted mode");
        }
        if (matchInfo.matchType == TokenizerData.MatchType.TOKEN) {
          String label = tokenizerData.labels.get(matchedKind);
          if (label == null) {
            label = "Token kind: " + matchedKind;
          }
          System.out.println("Token: " + label + "; image: \"" + input.substring(beg, matchedPos + 1) + "\" at: "
              + tokline + ":" + tokcol);
        }
        if (matchInfo.newLexState != -1) {
          curLexState = matchInfo.newLexState;
        }
        curPos = matchedPos + 1;
      } else if (curPos < input_size) {
        System.err.println("Encountered token error at char: " + input.charAt(curPos));
        System.exit(1);
      }
    }
    System.err.println("Matched EOF");
  }
}
