
package org.javacc.java;

import org.javacc.parser.Context;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.Options;
import org.javacc.parser.Token;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class JavaUtil {

  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package[^a-z]+([^;]+)", Pattern.CASE_INSENSITIVE);

  private JavaUtil() {}


  /**
   * Parses the package from the insertion points.
   *
   * @return
   */
  public static String parsePackage(Context context) {
    Token t = null;
    StringWriter writer = new StringWriter();
    try (PrintWriter printer = new PrintWriter(writer)) {
      if ((context.globals().cu_to_insertion_point_1.size() != 0)
          && (context.globals().cu_to_insertion_point_1.get(0).kind == JavaCCParserConstants.PACKAGE)) {
        for (int i = 1; i < context.globals().cu_to_insertion_point_1.size(); i++) {
          if (context.globals().cu_to_insertion_point_1.get(i).kind == JavaCCParserConstants.SEMICOLON) {
            JavaUtil.printTokenSetup(context.globals().cu_to_insertion_point_1.get(0), context);
            for (int j = 0; j <= i; j++) {
              t = context.globals().cu_to_insertion_point_1.get(j);
              JavaUtil.printToken(t, printer, true, context);
            }
            JavaUtil.printTrailingComments(t, printer, true, context);
            printer.println("");
            printer.println("");
            break;
          }
        }
      }
    }

    String text = writer.toString();
    if (text == null) {
      return "";
    }

    Matcher matcher = JavaUtil.PACKAGE_PATTERN.matcher(text);
    return matcher.find() ? matcher.group(1) : "";
  }


  public static String getStatic() {
    return (Options.getStatic() ? "static " : "");
  }

  public static String getBooleanType() {
    return "boolean";
  }

  private static void printTokenSetup(Token t, Context context) {
    Token tt = t;
    while (tt.specialToken != null) {
      tt = tt.specialToken;
    }
    context.globals().cline = tt.beginLine;
    context.globals().ccol = tt.beginColumn;
  }

  private static void printToken(Token t, java.io.PrintWriter ostr, boolean escape, Context context) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) {
        tt = tt.specialToken;
      }
      while (tt != null) {
        ostr.append(tt.printTokenOnly(context.globals(), escape));
        tt = tt.next;
      }
    }
    ostr.append(t.printTokenOnly(context.globals(), escape));
  }

  private static void printTrailingComments(Token t, java.io.PrintWriter ostr, boolean escape, Context context) {
    if (t.next == null) {
      return;
    }

    JavaUtil.printLeadingComments(t.next, escape, context);
  }


  private static String printLeadingComments(Token t, boolean escape, Context context) {
    String retval = "";
    if (t.specialToken == null) {
      return retval;
    }
    Token tt = t.specialToken;
    while (tt.specialToken != null) {
      tt = tt.specialToken;
    }
    while (tt != null) {
      retval += tt.printTokenOnly(context.globals(), escape);
      tt = tt.next;
    }
    if ((context.globals().ccol != 1) && (context.globals().cline != t.beginLine)) {
      retval += "\n";
      context.globals().cline++;
      context.globals().ccol = 1;
    }
    return retval;
  }
}
