
package org.javacc.jjtree;

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
    JJTreeContext context = new JJTreeContext();

    assertEquals(new File("."), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("."), context.treeOptions().getJJTreeOutputDirectory());

    Options.setInputFileOption(null, null, Options.USEROPTION__OUTPUT_DIRECTORY, "test/output", context);
    assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("test/output"), context.treeOptions().getJJTreeOutputDirectory());

    Options.setInputFileOption(null, null, "JJTREE_OUTPUT_DIRECTORY", "test/jjtreeoutput", context);
    assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
    assertEquals(new File("test/jjtreeoutput"), context.treeOptions().getJJTreeOutputDirectory());

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testNodeFactory() {
    JJTreeContext context = new JJTreeContext();

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", Boolean.FALSE, context);
    assertEquals(context.treeOptions().getNodeFactory(), "");

    context = new JJTreeContext();
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", Boolean.TRUE, context);
    assertEquals(context.treeOptions().getNodeFactory(), "*");

    context = new JJTreeContext();
    JJTreeOptions.setInputFileOption(null, null, "NODE_FACTORY", "mypackage.MyNode", context);
    assertEquals(context.treeOptions().getNodeFactory(), "mypackage.MyNode");

    assertEquals(0, context.errors().get_warning_count());

    assertEquals(0, context.errors().get_error_count());
    assertEquals(0, context.errors().get_parse_error_count());
    assertEquals(0, context.errors().get_semantic_error_count());
  }

  public void testNodeClass() {
    JJTreeContext context = new JJTreeContext();

    assertEquals(0, context.errors().get_warning_count());
    assertEquals(0, context.errors().get_error_count());

    assertEquals("", context.treeOptions().getNodeClass());
    // Need some functional tests, as well.
  }

  public void testValidate() {
    JJTreeContext context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    context.validate();
    assertEquals(1, context.errors().get_warning_count());

    context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    JJTreeOptions.setCmdLineOption("VISITOR=true");
    context.validate();
    assertEquals(0, context.errors().get_warning_count());

    context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=Object");
    context.validate();
    assertEquals(1, context.errors().get_warning_count());
  }

  public void testValidateReturnType() {
    JJTreeContext context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    context.validate();
    assertEquals(1, context.errors().get_warning_count());

    context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    JJTreeOptions.setCmdLineOption("VISITOR=true");
    context.validate();
    assertEquals(0, context.errors().get_warning_count());

    context = new JJTreeContext();

    JJTreeOptions.setCmdLineOption("VISITOR_DATA_TYPE=String");
    context.validate();
    assertEquals(1, context.errors().get_warning_count());
  }
}
