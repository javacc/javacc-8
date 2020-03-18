
package org.javacc.parser;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test cases to prod at the valitity of Options a little.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class OptionsTest extends TestCase {

  public void testDefaults() {
    Options.init();
    Context context = new Context();

    assertEquals(53, Options.optionValues.size());

    assertEquals(true, Options.getBuildParser());
    assertEquals(true, Options.getBuildTokenManager());
    assertEquals(false, Options.getCacheTokens());
    assertEquals(false, Options.getCommonTokenAction());
    assertEquals(false, Options.getDebugLookahead());
    assertEquals(false, Options.getDebugParser());
    assertEquals(false, Options.getDebugTokenManager());
    assertEquals(true, Options.getErrorReporting());
    assertEquals(false, Options.getForceLaCheck());
    assertEquals(false, Options.getIgnoreCase());
    assertEquals(false, Options.getJavaUnicodeEscape());
    assertEquals(true, Options.getKeepLineColumn());
    assertEquals(true, Options.getSanityCheck());
    assertEquals(true, Options.getStatic());
    assertEquals(false, Options.getUnicodeInput());
    assertEquals(false, Options.getUserCharStream());
    assertEquals(false, Options.getUserTokenManager());
    assertEquals(false, Options.getTokenManagerUsesParser());

    assertEquals(2, Options.getChoiceAmbiguityCheck());
    assertEquals(1, Options.getLookahead());
    assertEquals(1, Options.getOtherAmbiguityCheck());

    assertEquals("1.5", Options.getJdkVersion());
    assertEquals(new File("."), Options.getOutputDirectory());
    assertEquals("", Options.getTokenExtends());
    assertEquals("", Options.getTokenFactory());
    assertEquals(System.getProperties().get("file.encoding"), Options.getGrammarEncoding());

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testSetBooleanOption() {
    Options.init();
    Context context = new Context();

    assertEquals(true, Options.getStatic());
    Options.setCmdLineOption("-NOSTATIC");
    assertEquals(false, Options.getStatic());

    assertEquals(false, Options.getJavaUnicodeEscape());
    Options.setCmdLineOption("-JAVA_UNICODE_ESCAPE:true");
    assertEquals(true, Options.getJavaUnicodeEscape());

    assertEquals(true, Options.getSanityCheck());
    Options.setCmdLineOption("-SANITY_CHECK=false");
    assertEquals(false, Options.getSanityCheck());

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }


  public void testIntBooleanOption() {
    Options.init();
    Context context = new Context();

    assertEquals(1, Options.getLookahead());
    Options.setCmdLineOption("LOOKAHEAD=2");
    assertEquals(2, Options.getLookahead());
    assertEquals(0, context.errors().get_warning_count());
    Options.setCmdLineOption("LOOKAHEAD=0");
    assertEquals(2, Options.getLookahead());
    assertEquals(0, context.errors().get_warning_count());
    Options.setInputFileOption(null, null, Options.USEROPTION__LOOKAHEAD, new Integer(0), context);
    assertEquals(2, Options.getLookahead());
    assertEquals(1, context.errors().get_warning_count());

    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testSetStringOption() {
    Options.init();
    Context context = new Context();

    assertEquals("", Options.getTokenExtends());
    Options.setCmdLineOption("-TOKEN_EXTENDS=java.lang.Object");
    assertEquals("java.lang.Object", Options.getTokenExtends());
    Options.setInputFileOption(null, null, Options.USEROPTION__TOKEN_EXTENDS, "Object", context);
    // File option does not override cmd line
    assertEquals("java.lang.Object", Options.getTokenExtends());

    Options.init();

    Options.setInputFileOption(null, null, Options.USEROPTION__TOKEN_EXTENDS, "Object", context);
    assertEquals("Object", Options.getTokenExtends());
    Options.setCmdLineOption("-TOKEN_EXTENDS=java.lang.Object");
    assertEquals("java.lang.Object", Options.getTokenExtends());
  }

  public void testSetNonexistentOption() {
    Options.init();
    Context context = new Context();

    assertEquals(0, context.errors().get_warning_count());
    Options.setInputFileOption(null, null, "NONEXISTENTOPTION", Boolean.TRUE, context);
    assertEquals(1, context.errors().get_warning_count());

    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testSetWrongTypeForOption() {
    Options.init();
    Context context = new Context();

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    Options.setInputFileOption(null, null, Options.USEROPTION__STATIC, new Integer(8), context);
    assertEquals(1, context.errors().get_warning_count());

    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testNormalize() {
    Options.init();
    Context context = new Context();

    assertEquals(false, Options.getDebugLookahead());
    assertEquals(false, Options.getDebugParser());

    Options.setCmdLineOption("-DEBUG_LOOKAHEAD=TRUE");
    Options.normalize(context);

    assertEquals(true, Options.getDebugLookahead());
    assertEquals(true, Options.getDebugParser());

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testOptionsString() throws ParseException {
    Options.init();

    Options.setCmdLineOption("-STATIC=False");
    Options.setCmdLineOption("-IGNORE_CASE=True");
    String[] options = { Options.USEROPTION__STATIC, Options.USEROPTION__IGNORE_CASE };
    String optionString = Options.getOptionsString(options);
    assertEquals("STATIC=false,IGNORE_CASE=true", optionString);
  }
}
