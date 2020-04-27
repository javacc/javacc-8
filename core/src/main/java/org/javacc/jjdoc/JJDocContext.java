
package org.javacc.jjdoc;

import org.javacc.parser.Context;
import org.javacc.parser.Options;


public class JJDocContext extends Context {

  public JJDocContext() {
    super(new Options());

    Options.optionValues.put("ONE_TABLE", Boolean.TRUE);
    Options.optionValues.put("TEXT", Boolean.FALSE);
    Options.optionValues.put("XTEXT", Boolean.FALSE);
    Options.optionValues.put("BNF", Boolean.FALSE);
    Options.optionValues.put("JCC", Boolean.FALSE);
    Options.optionValues.put("OUTPUT_FILE", "");
    Options.optionValues.put("CSS", "");
  }

  /**
   * Find the one table value.
   */
  public final boolean getOneTable() {
    return Options.booleanValue("ONE_TABLE");
  }

  /**
   * Find the CSS value.
   */
  public final String getCSS() {
    return Options.stringValue("CSS");
  }

  /**
   * Find the text value.
   */
  public final boolean getText() {
    return Options.booleanValue("TEXT");
  }

  public final boolean getXText() {
    return Options.booleanValue("XTEXT");
  }

  /**
   * Find the BNF value.
   */
  public final boolean getBNF() {
    return Options.booleanValue("BNF");
  }

  /**
   * Find the BNF value.
   */
  public final boolean getJCC() {
    return Options.booleanValue("JCC");
  }

  /**
   * Find the output file value.
   */
  public final String getOutputFile() {
    return Options.stringValue("OUTPUT_FILE");
  }
}
