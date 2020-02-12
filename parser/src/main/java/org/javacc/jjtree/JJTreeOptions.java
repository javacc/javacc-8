// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/*
 * Copyright (c) 2005-2006, Kees Jan Koster kjkoster@kjkoster.org All rights
 * reserved.
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

package org.javacc.jjtree;

import org.javacc.parser.Options;

import java.io.File;

/**
 * The JJTree-specific options.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class JJTreeOptions extends Options {

  /**
   * Limit subclassing to derived classes.
   */
  protected JJTreeOptions() {}

  /**
   * Find the multi value.
   *
   * @return The requested multi value.
   */
  public final boolean getMulti() {
    return Options.booleanValue("MULTI");
  }

  /**
   * Find the node default void value.
   *
   * @return The requested node default void value.
   */
  public final boolean getNodeDefaultVoid() {
    return Options.booleanValue("NODE_DEFAULT_VOID");
  }

  /**
   * Find the node scope hook value.
   *
   * @return The requested node scope hook value.
   */
  public final boolean getNodeScopeHook() {
    return Options.booleanValue("NODE_SCOPE_HOOK");
  }

  /**
   * Find the node factory value.
   *
   * @return The requested node factory value.
   */
  public final String getNodeFactory() {
    return Options.stringValue("NODE_FACTORY");
  }

  /**
   * Find the node uses parser value.
   *
   * @return The requested node uses parser value.
   */
  public final boolean getNodeUsesParser() {
    return Options.booleanValue("NODE_USES_PARSER");
  }

  /**
   * Find the build node files value.
   *
   * @return The requested build node files value.
   */
  public final boolean getBuildNodeFiles() {
    return Options.booleanValue("BUILD_NODE_FILES");
  }

  /**
   * Find the visitor value.
   *
   * @return The requested visitor value.
   */
  public final boolean getVisitor() {
    return Options.booleanValue("VISITOR");
  }

  /**
   * Find the trackTokens value.
   *
   * @return The requested trackTokens value.
   */
  public final boolean getTrackTokens() {
    return Options.booleanValue("TRACK_TOKENS");
  }

  /**
   * Find the node prefix value.
   *
   * @return The requested node prefix value.
   */
  public final String getNodePrefix() {
    return Options.stringValue("NODE_PREFIX");
  }

  /**
   * Find the node super class name.
   *
   * @return The requested node super class
   */
  public final String getNodeExtends() {
    return Options.stringValue("NODE_EXTENDS");
  }

  /**
   * Find the node class name.
   *
   * @return The requested node class
   */
  public final String getNodeClass() {
    return Options.stringValue("NODE_CLASS");
  }

  /**
   * Find the node package value.
   *
   * @return The requested node package value.
   */
  public final String getNodePackage() {
    return Options.stringValue("NODE_PACKAGE");
  }

  /**
   * Find the output file value.
   *
   * @return The requested output file value.
   */
  public final String getOutputFile() {
    return Options.stringValue("OUTPUT_FILE");
  }

  /**
   * Find the visitor exception value
   *
   * @return The requested visitor exception value.
   */
  public final String getVisitorException() {
    return Options.stringValue("VISITOR_EXCEPTION");
  }

  /**
   * Find the visitor data type value
   *
   * @return The requested visitor data type value.
   */
  public final String getVisitorDataType() {
    return Options.stringValue("VISITOR_DATA_TYPE");
  }

  /**
   * Find the visitor return type value
   *
   * @return The requested visitor return type value.
   */
  public final String getVisitorReturnType() {
    return Options.stringValue("VISITOR_RETURN_TYPE");
  }

  /**
   * Find the output directory to place the generated <code>.jj</code> files
   * into. If none is configured, use the value of
   * <code>getOutputDirectory()</code>.
   *
   * @return The requested JJTree output directory
   */
  public final File getJJTreeOutputDirectory() {
    final String dirName = Options.stringValue("JJTREE_OUTPUT_DIRECTORY");
    File dir = null;

    if ("".equals(dirName)) {
      dir = Options.getOutputDirectory();
    } else {
      dir = new File(dirName);
    }

    return dir;
  }

  /**
   * Compute where are located the ASTNodes is any are defined
   *
   * @return the requested NODE_DIRECTORY directory
   */
  public final File getASTNodeDirectory() {
    final String dirName = Options.stringValue("NODE_DIRECTORY");
    File dir = null;

    if ("".equals(dirName)) {
      dir = getJJTreeOutputDirectory();
    } else {
      dir = new File(dirName);
    }
    return dir;
  }
}
