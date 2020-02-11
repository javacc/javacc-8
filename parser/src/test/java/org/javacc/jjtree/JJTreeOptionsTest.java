
package org.javacc.jjtree;

import org.javacc.parser.JavaCCContext;
import org.javacc.parser.Options;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test the JJTree-specific options.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class JJTreeOptionsTest extends TestCase {

  public void testOutputDirectory() {
    JavaCCContext context = new JavaCCContext();
    JJTreeOptions.init();

    assertEquals(new File("."), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("."), JJTreeOptions.getJJTreeOutputDirectory());

    Options.setInputFileOption(null, null, Options.USEROPTION__OUTPUT_DIRECTORY, "test/output", context);
    assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("test/output"), JJTreeOptions.getJJTreeOutputDirectory());

    Options.setInputFileOption(null, null, "JJTREE_OUTPUT_DIRECTORY", "test/jjtreeoutput", context);
    assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("test/jjtreeoutput"), JJTreeOptions.getJJTreeOutputDirectory());

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testNodeFactory() {
    JJTreeOptions.init();
    JavaCCContext context = new JavaCCContext();

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", Boolean.FALSE, context);
    assertEquals(JJTreeOptions.getNodeFactory(), "");

    JJTreeOptions.init();
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", Boolean.TRUE, context);
    assertEquals(JJTreeOptions.getNodeFactory(), "*");

    JJTreeOptions.init();
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", "mypackage.MyNode", context);
    assertEquals(JJTreeOptions.getNodeFactory(), "mypackage.MyNode");

    assertEquals(0, context.errors().get_warning_count());

    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testNodeClass() {
    JJTreeOptions.init();
    JavaCCContext context = new JavaCCContext();

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());

    assertEquals("", JJTreeOptions.getNodeClass());
    // Need some functional tests, as well.
  }

  public void testValidate() {
    JJTreeOptions.init();
    JavaCCContext context = new JavaCCContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.validate(context);
    assertEquals(1, context.errors().get_warning_count());

    JJTreeOptions.init();
    context = new JavaCCContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.setCmdLineOption("VISITOR=true");
    JJTreeOptions.validate(context);
    assertEquals(0, context.errors().get_warning_count());

    JJTreeOptions.init();
    context = new JavaCCContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.validate(context);
    assertEquals(1, context.errors().get_warning_count());
  }

  public void testValidateReturnType() {
    JJTreeOptions.init();
    JavaCCContext context = new JavaCCContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    JJTreeOptions.validate(context);
    assertEquals(1, context.errors().get_warning_count());

    JJTreeOptions.init();
    context = new JavaCCContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    JJTreeOptions.setCmdLineOption("VISITOR=true");
    JJTreeOptions.validate(context);
    assertEquals(0, context.errors().get_warning_count());

    JJTreeOptions.init();
    context = new JavaCCContext();
    
    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    JJTreeOptions.validate(context);
    assertEquals(1, context.errors().get_warning_count());
  }
}
