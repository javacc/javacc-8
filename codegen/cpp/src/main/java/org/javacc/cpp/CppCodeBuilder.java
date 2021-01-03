
package org.javacc.cpp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.parser.Options;
import org.javacc.utils.CodeBuilder;

import java.io.File;


/**
 * The {@link CppCodeBuilder} class.
 */
class CppCodeBuilder extends CodeBuilder<CppCodeBuilder> {

  private enum Buffer {
    Main,
    Include,
    Static;
  }

  private final boolean      headeOnly;
  private final StringBuffer mainBuffer    = new StringBuffer();
  private final StringBuffer includeBuffer = new StringBuffer();
  private final StringBuffer staticsBuffer = new StringBuffer();


  private Buffer kind;

  /**
   * Constructs an instance of {@link CodeBuilder}.
   *
   * @param options
   */
  private CppCodeBuilder(Context context, CodeGeneratorSettings options, boolean headeOnly) {
    super(context, options);
    this.headeOnly = headeOnly;
    kind = headeOnly ? Buffer.Include : Buffer.Main;
  }

  /**
   * Get the {@link StringBuffer}
   */
  @Override
  protected final StringBuffer getBuffer() {
    switch (kind) {
      case Include:
        return includeBuffer;
      case Static:
        return staticsBuffer;
      default:
    }
    return mainBuffer;
  }

  /**
   * Generate a class with a given name, an array of superclass and another
   * array of super interfaes
   */
  void genClassStart(String mod, String name, String[] superClasses, String[] superInterfaces) {
    print("class ");
    if (!Options.getLibrary().isEmpty()) {
    	print(name.toUpperCase() + "_API ");
    }
    print(name);
    if ((superClasses.length > 0) || (superInterfaces.length > 0)) {
      print(" : ");
    }

    genCommaSeperatedString(superClasses);
    genCommaSeperatedString(superInterfaces);
    println("{");
    println("public:");
  }

  @Override
  protected final void build() {
    String includeFileName = getFile().getName().replace(".cc", ".h");
    File includeFile = new File(getFile().getParentFile(), includeFileName);

    fixupLongLiterals(includeBuffer);
    store(includeFile, includeBuffer);

    if (headeOnly) {
      return;
    }

    mainBuffer.insert(0, staticsBuffer);

    mainBuffer.insert(0, "#include \"" + includeFileName + "\"\n");

    fixupLongLiterals(mainBuffer);
    store(getFile(), mainBuffer);
  }

  void generateMethodDefHeader(String modsAndRetType, String className, String nameAndParams) {
    generateMethodDefHeader(modsAndRetType, className, nameAndParams, null);
  }

  void generateMethodDefHeader(String qualifiedModsAndRetType, String className, String nameAndParams,
      String exceptions) {
    // for C++, we generate the signature in the header file and body in main file
    includeBuffer.append(qualifiedModsAndRetType + " " + nameAndParams);
    // if (exceptions != null)
    // includeBuffer.append(" throw(" + exceptions + ")");
    includeBuffer.append(";\n");

    String modsAndRetType = null;
    int i = qualifiedModsAndRetType.lastIndexOf(':');
    if (i >= 0) {
      modsAndRetType = qualifiedModsAndRetType.substring(i + 1);
    }

    if (modsAndRetType != null) {
      i = modsAndRetType.lastIndexOf("virtual");
      if (i >= 0) {
        modsAndRetType = modsAndRetType.substring(i + "virtual".length());
      }
    }
    if (qualifiedModsAndRetType != null) {
      i = qualifiedModsAndRetType.lastIndexOf("virtual");
      if (i >= 0) {
        qualifiedModsAndRetType = qualifiedModsAndRetType.substring(i + "virtual".length());
      }
    }
    String qualifierClass = (className == null) ? "" : className + "::";
    mainBuffer.append("\n" + qualifiedModsAndRetType + " " + qualifierClass + nameAndParams);
    // if (exceptions != null)
    // mainBuffer.append(" throw( " + exceptions + ")");
    switchToMainFile();
  }

  // HACK
  private void fixupLongLiterals(StringBuffer sb) {
    for (int i = 0; i < (sb.length() - 1); i++) {
      // int beg = i;
      char c1 = sb.charAt(i);
      char c2 = sb.charAt(i + 1);
      if (Character.isDigit(c1) || ((c1 == '0') && (c2 == 'x'))) {
        i += c1 == '0' ? 2 : 1;
        while (CppCodeBuilder.isHexDigit(sb.charAt(i))) {
          i++;
        }
        // Avoid replacing long long (LL) with unsigned long long (ULL)
        if ((sb.charAt(i) == 'L') && (sb.charAt(i + 1) != 'L') && (sb.charAt(i - 1) != 'L')) {
          // if (sb.charAt(i) == 'L' && (i >= sb.length() || sb.charAt(i + 1) !=
          // 'L')) {
          sb.insert(i, "UL");
        }
        i++;
      }
    }
  }

  /**
   * Return <code>true</code> if the char is a hex digit.
   *
   * @param c
   */
  private static boolean isHexDigit(char c) {
    return ((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
  }

  private final void genCommaSeperatedString(String[] strings) {
    for (int i = 0; i < strings.length; i++) {
      if (i > 0) {
        print(", ");
      }

      print(strings[i]);
    }
  }


  // Used by the CPP code generatror
  final CppCodeBuilder printCharArray(String s) {
    print("{");
    for (char c : s.toCharArray()) {
      print("0x" + Integer.toHexString(c) + ", ");
    }
    print("0}");
    return this;
  }

  public void printLiteralArray(String varName, String[] arr) {
    // First generate char array vars
    for (int i = 0; i < arr.length; i++) {
      println("static const JJChar " + varName + "_arr_" + i + "[] = ");
      printCharArray(arr[i]);
      println(";");
    }

    println("static const JJString " + varName + "[] = {");
    for (int i = 0; i < arr.length; i++) {
      print(varName + "_arr_" + i);
      if ((i + 1) < arr.length) {
        print(", ");
      }
      println();
    }
    println("};");
  }


  @Override
  public final String escapeToUnicode(String text) {
    return text;
  }

  void switchToMainFile() {
    kind = Buffer.Main;
  }

  void switchToIncludeFile() {
    kind = Buffer.Include;
  }

  void switchToStaticsFile() {
    kind = Buffer.Static;
  }

  /**
   * Constructs an instance of {@link CppCodeBuilder}.
   *
   * @param options
   */
  static CppCodeBuilder of(Context context, CodeGeneratorSettings options) {
    return new CppCodeBuilder(context, options, false);
  }

  /**
   * Constructs an instance of {@link CppCodeBuilder}.
   *
   * @param options
   */
  static CppCodeBuilder ofHeader(Context context, CodeGeneratorSettings options) {
    return new CppCodeBuilder(context, options, true);
  }
}
