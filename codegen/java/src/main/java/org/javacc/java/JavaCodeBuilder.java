
package org.javacc.java;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.Context;
import org.javacc.utils.CodeBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * The {@link JavaCodeBuilder} class.
 */
class JavaCodeBuilder extends CodeBuilder<JavaCodeBuilder> {

  private final StringBuffer buffer = new StringBuffer();


  private String             packageName;
  private final List<String> imports = new ArrayList<>();

  /**
   * Constructs an instance of {@link CodeBuilder}.
   *
   * @param options
   */
  private JavaCodeBuilder(Context context, CodeGeneratorSettings options) {
    super(context, options);
  }

  /**
   * Get the {@link StringBuffer}
   */
  @Override
  protected final StringBuffer getBuffer() {
    return buffer;
  }

  /**
   * Set the Java package name
   *
   * @param packageName
   */
  JavaCodeBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /**
   * Set the Java import name
   *
   * @param importName
   */
  JavaCodeBuilder addImportName(String importName) {
    this.imports.add(importName);
    return this;
  }

  @Override
  protected final void build() {
    StringBuffer buffer = new StringBuffer();

    if (packageName.length() > 0) {
      buffer.append("package ").append(packageName).append(";\n\n");
    }
    if (!imports.isEmpty()) {
      for (String importName : imports) {
        buffer.append("import ").append(importName).append(";\n\n");
      }
    }

    buffer.append(getBuffer());

    store(getFile(), buffer);
  }

  /**
   * Constructs an instance of {@link JavaCodeBuilder}.
   *
   * @param options
   */
  static JavaCodeBuilder of(Context context, CodeGeneratorSettings options) {
    return new JavaCodeBuilder(context, options);
  }
}
