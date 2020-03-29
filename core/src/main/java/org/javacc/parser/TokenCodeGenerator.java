
package org.javacc.parser;

public interface TokenCodeGenerator {

	  boolean generateCodeForToken(CodeGeneratorSettings settings);
	  boolean generateCodeForDefaultToken(CodeGeneratorSettings settings);
}
