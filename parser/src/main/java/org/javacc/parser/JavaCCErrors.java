/*
 * Copyright (c) 2006, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Sun Microsystems, Inc. nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

/**
 * Output error messages and keep track of totals.
 */
public final class JavaCCErrors {

  private int parse_error_count;
  private int semantic_error_count;
  private int warning_count;

  JavaCCErrors() {
    this.parse_error_count = 0;
    this.semantic_error_count = 0;
    this.warning_count = 0;
  }

  public void error(String message, Object... arguments) {
    System.err.printf(message, arguments);
  }

  private void printLocationInfo(Object node) {
    if (node instanceof NormalProduction) {
      NormalProduction n = (NormalProduction) node;
      error("Line %s, Column %s: ", n.getLine(), n.getColumn());
    } else if (node instanceof TokenProduction) {
      TokenProduction n = (TokenProduction) node;
      error("Line %s, Column %s: ", n.getLine(), n.getColumn());
    } else if (node instanceof Expansion) {
      Expansion n = (Expansion) node;
      error("Line %s, Column %s: ", n.getLine(), n.getColumn());
    } else if (node instanceof CharacterRange) {
      CharacterRange n = (CharacterRange) node;
      error("Line %s, Column %s: ", n.getLine(), n.getColumn());
    } else if (node instanceof SingleCharacter) {
      SingleCharacter n = (SingleCharacter) node;
      error("Line %s, Column %s: ", n.getLine(), n.getColumn());
    } else if (node instanceof Token) {
      Token t = (Token) node;
      error("Line %s, Column %s: ", t.beginLine, t.beginColumn);
    }
  }

  public void parse_error(Object node, String mess) {
    error("Error: ");
    printLocationInfo(node);
    error(mess + "\n");
    parse_error_count++;
  }

  public int get_parse_error_count() {
    return parse_error_count;
  }

  public void semantic_error(Object node, String mess) {
    error("Error: ");
    printLocationInfo(node);
    error(mess + "\n");
    semantic_error_count++;
  }

  public void semantic_error(String mess) {
    error("Error: ");
    error(mess + "\n");
    semantic_error_count++;
  }

  public int get_semantic_error_count() {
    return semantic_error_count;
  }

  public void warning(Object node, String mess) {
    error("Warning: ");
    printLocationInfo(node);
    error(mess + "\n");
    warning_count++;
  }

  public void warning(String mess) {
    error("Warning: ");
    error(mess + "\n");
    warning_count++;
  }

  public int get_warning_count() {
    return warning_count;
  }

  public int get_error_count() {
    return parse_error_count + semantic_error_count;
  }

  public void fatal(String message) {
    error("Fatal Error: %s\n", message);
    throw new RuntimeException("Fatal Error: " + message);
  }
}
