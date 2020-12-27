// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.javacc.utils.OptionInfo;
import org.javacc.utils.OptionType;

/**
 * A class with static state that stores all option information.
 */
public class Options {

  /**
   * Limit subclassing to derived classes.
   */
  public Options() {}

  /**
   * These are options that are not settable by the user themselves, and that
   * are set indirectly via some configuration of user options
   */
  public static final String NONUSER_OPTION__NAMESPACE_CLOSE               	= "NAMESPACE_CLOSE";
  public static final String NONUSER_OPTION__HAS_NAMESPACE                 	= "HAS_NAMESPACE";
  public static final String NONUSER_OPTION__NAMESPACE_OPEN                	= "NAMESPACE_OPEN";
  public static final String NONUSER_OPTION__PARSER_NAME                   	= "PARSER_NAME";
  public static final String NONUSER_OPTION__PARSER_NAME_UPPER_CASE        	= "PARSER_NAME_UPPER_CASE";
  public static final String NONUSER_OPTION__LEGACY_EXCEPTION_HANDLING     	= "LEGACY_EXCEPTION_HANDLING";
  public static final String NONUSER_OPTION__INTERPRETER                   	= "INTERPRETER_MODE";
  /**
   * Options that the user can specify from .javacc file
   */
  public static final String USEROPTION__JAVA_TEMPLATE_TYPE                	= "JAVA_TEMPLATE_TYPE";
  public static final String USEROPTION__GENERATE_BOILERPLATE              	= "GENERATE_BOILERPLATE";
  public static final String USEROPTION__CODE_GENERATOR                    	= "CODE_GENERATOR";
  public static final String USEROPTION__NO_DFA                            	= "NO_DFA";
  public static final String USEROPTION__STATIC                            	= "STATIC";
  public static final String USEROPTION__LOOKAHEAD                         	= "LOOKAHEAD";
  public static final String USEROPTION__IGNORE_CASE                       	= "IGNORE_CASE";
  public static final String USEROPTION__UNICODE_INPUT                     	= "UNICODE_INPUT";
  public static final String USEROPTION__JAVA_UNICODE_ESCAPE               	= "JAVA_UNICODE_ESCAPE";
  public static final String USEROPTION__ERROR_REPORTING                   	= "ERROR_REPORTING";
  public static final String USEROPTION__DEBUG_TOKEN_MANAGER               	= "DEBUG_TOKEN_MANAGER";
  public static final String USEROPTION__DEBUG_LOOKAHEAD                   	= "DEBUG_LOOKAHEAD";
  public static final String USEROPTION__DEBUG_PARSER                      	= "DEBUG_PARSER";
  public static final String USEROPTION__OTHER_AMBIGUITY_CHECK             	= "OTHER_AMBIGUITY_CHECK";
  public static final String USEROPTION__CHOICE_AMBIGUITY_CHECK            	= "CHOICE_AMBIGUITY_CHECK";
  public static final String USEROPTION__CACHE_TOKENS                      	= "CACHE_TOKENS";
  public static final String USEROPTION__COMMON_TOKEN_ACTION               	= "COMMON_TOKEN_ACTION";
  public static final String USEROPTION__FORCE_LA_CHECK                    	= "FORCE_LA_CHECK";
  public static final String USEROPTION__SANITY_CHECK                      	= "SANITY_CHECK";
  public static final String USEROPTION__TOKEN_MANAGER_USES_PARSER         	= "TOKEN_MANAGER_USES_PARSER";
  public static final String USEROPTION__BUILD_TOKEN_MANAGER               	= "BUILD_TOKEN_MANAGER";
  public static final String USEROPTION__BUILD_PARSER                      	= "BUILD_PARSER";
  public static final String USEROPTION__USER_CHAR_STREAM                  	= "USER_CHAR_STREAM";
  public static final String USEROPTION__JDK_VERSION                       	= "JDK_VERSION";
  public static final String USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC   	= "SUPPORT_CLASS_VISIBILITY_PUBLIC";
  public static final String USEROPTION__GENERATE_ANNOTATIONS              	= "GENERATE_ANNOTATIONS";
  public static final String USEROPTION__GENERATE_STRING_BUILDER           	= "GENERATE_STRING_BUILDER";
  public static final String USEROPTION__GENERATE_GENERICS                 	= "GENERATE_GENERICS";
  public static final String USEROPTION__GENERATE_CHAINED_EXCEPTION        	= "GENERATE_CHAINED_EXCEPTION";
  public static final String USEROPTION__OUTPUT_DIRECTORY                  	= "OUTPUT_DIRECTORY";
  public static final String USEROPTION__KEEP_LINE_COLUMN                  	= "KEEP_LINE_COLUMN";
  public static final String USEROPTION__GRAMMAR_ENCODING                  	= "GRAMMAR_ENCODING";
  public static final String USEROPTION__DEPTH_LIMIT                       	= "DEPTH_LIMIT";
  
  
  public static final String USEROPTION__CPP_NAMESPACE                     	= "NAMESPACE";
  public static final String USEROPTION__IGNORE_ACTIONS                    	= "IGNORE_ACTIONS";
  public static final String USEROPTION__CPP_STOP_ON_FIRST_ERROR           	= "STOP_ON_FIRST_ERROR";
  public static final String USEROPTION__CPP_STACK_LIMIT                   	= "STACK_LIMIT";
  public static final String USEROPTION__CPP_USE_ARRAY                   	= "CPP_USE_ARRAY";
  public static final String USEROPTION__CPP_LIBRARY	                   	= "LIBRARY";

  public static final String USEROPTION__TOKEN_EXTENDS                     	= "TOKEN_EXTENDS";
  public static final String USEROPTION__CPP_TOKEN_FACTORY                 	= "TOKEN_FACTORY";
  public static final String USEROPTION__CPP_TOKEN_INCLUDE  	           	= "TOKEN_INCLUDE";
  public static final String USEROPTION__CPP_TOKEN_CLASS	                = "TOKEN_CLASS";
  public static final String USEROPTION__CPP_TOKEN_NAMESPACE         	    = "TOKEN_NAMESPACE";
  public static final String USEROPTION__CPP_TOKEN_CONSTANTS_INCLUDE        = "TOKEN_CONSTANTS_INCLUDE";
  public static final String USEROPTION__CPP_TOKEN_CONSTANTS_NAMESPACE 	    = "TOKEN_CONSTANTS_NAMESPACE";
  
  public static final String USEROPTION__CPP_PARSER_INCLUDE  	            = "PARSER_INCLUDE";
  
  public static final String USEROPTION__CPP_TOKEN_MANAGER_INCLUDE         	= "TOKEN_MANAGER_INCLUDE";
  public static final String USEROPTION__TOKEN_MANAGER_SUPER_CLASS         	= "TOKEN_MANAGER_SUPER_CLASS";
  
  																				
  public static final String USEROPTION__USER_TOKEN_MANAGER                	= "USER_TOKEN_MANAGER";
    
  /**
   * 2013/07/22 -- GWT Compliant Output -- no external dependencies on GWT, but
   * generated code adds loose coupling to IO, for 6.1 release, this is opt-in,
   * moving forward to 7.0, after thorough testing, this will likely become the
   * default option with classic being deprecated
   */
  public static final String JAVA_TEMPLATE_TYPE_MODERN  = "modern";

  /**
   * The old style of Java code generation (tight coupling of code to Java IO
   * classes - not GWT compatible)
   */
  public static final String JAVA_TEMPLATE_TYPE_CLASSIC = "classic";


  private static final Set<OptionInfo> userOptions;


  static {
    TreeSet<OptionInfo> temp = new TreeSet<>();

    temp.add(new OptionInfo(Options.USEROPTION__LOOKAHEAD, OptionType.INTEGER, Integer.valueOf(1)));

    temp.add(new OptionInfo(Options.USEROPTION__CHOICE_AMBIGUITY_CHECK, OptionType.INTEGER, Integer.valueOf(2)));
    temp.add(new OptionInfo(Options.USEROPTION__OTHER_AMBIGUITY_CHECK, OptionType.INTEGER, Integer.valueOf(1)));
    temp.add(new OptionInfo(Options.USEROPTION__STATIC, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__CODE_GENERATOR, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__NO_DFA, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__DEBUG_PARSER, OptionType.BOOLEAN, Boolean.FALSE));

    temp.add(new OptionInfo(Options.USEROPTION__DEBUG_LOOKAHEAD, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__DEBUG_TOKEN_MANAGER, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__ERROR_REPORTING, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__JAVA_UNICODE_ESCAPE, OptionType.BOOLEAN, Boolean.FALSE));

    temp.add(new OptionInfo(Options.USEROPTION__UNICODE_INPUT, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__IGNORE_CASE, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__USER_TOKEN_MANAGER, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__USER_CHAR_STREAM, OptionType.BOOLEAN, Boolean.FALSE));

    temp.add(new OptionInfo(Options.USEROPTION__BUILD_PARSER, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__BUILD_TOKEN_MANAGER, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__SANITY_CHECK, OptionType.BOOLEAN, Boolean.TRUE));

    temp.add(new OptionInfo(Options.USEROPTION__FORCE_LA_CHECK, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__COMMON_TOKEN_ACTION, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__CACHE_TOKENS, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__KEEP_LINE_COLUMN, OptionType.BOOLEAN, Boolean.TRUE));

    temp.add(new OptionInfo(Options.USEROPTION__GENERATE_CHAINED_EXCEPTION, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__GENERATE_GENERICS, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__GENERATE_BOILERPLATE, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__GENERATE_STRING_BUILDER, OptionType.BOOLEAN, Boolean.FALSE));

    temp.add(new OptionInfo(Options.USEROPTION__GENERATE_ANNOTATIONS, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC, OptionType.BOOLEAN, Boolean.TRUE));
    temp.add(new OptionInfo(Options.USEROPTION__OUTPUT_DIRECTORY, OptionType.STRING, "."));
    temp.add(new OptionInfo(Options.USEROPTION__JDK_VERSION, OptionType.STRING, "1.5"));
    temp.add(new OptionInfo(Options.USEROPTION__JAVA_TEMPLATE_TYPE, OptionType.STRING, Options.JAVA_TEMPLATE_TYPE_CLASSIC));

    temp.add(new OptionInfo(Options.USEROPTION__GRAMMAR_ENCODING, OptionType.STRING, ""));

    temp.add(new OptionInfo(Options.USEROPTION__CPP_NAMESPACE, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.NONUSER_OPTION__HAS_NAMESPACE, OptionType.BOOLEAN, Boolean.FALSE));
    
    temp.add(new OptionInfo(Options.USEROPTION__IGNORE_ACTIONS, OptionType.BOOLEAN, Boolean.FALSE));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR, OptionType.BOOLEAN, Boolean.FALSE));

    temp.add(new OptionInfo(Options.USEROPTION__DEPTH_LIMIT, OptionType.INTEGER, Integer.valueOf(0)));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_STACK_LIMIT, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_USE_ARRAY, OptionType.BOOLEAN, false));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_LIBRARY, OptionType.STRING, ""));

        
    temp.add(new OptionInfo(Options.USEROPTION__TOKEN_EXTENDS, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_FACTORY, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_CLASS, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_NAMESPACE, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_CONSTANTS_INCLUDE, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_CONSTANTS_NAMESPACE, OptionType.STRING, ""));
    
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_INCLUDE, OptionType.STRING, ""));
    temp.add(new OptionInfo(Options.USEROPTION__CPP_PARSER_INCLUDE, OptionType.STRING, ""));   
    temp.add(new OptionInfo(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDE, OptionType.STRING, ""));   

    temp.add(new OptionInfo(Options.USEROPTION__TOKEN_MANAGER_USES_PARSER, OptionType.BOOLEAN, Boolean.FALSE));

    userOptions = Collections.unmodifiableSet(temp);
  }

  /**
   * A mapping of option names (Strings) to values (Integer, Boolean, String).
   * This table is initialized by the main program. Its contents defines the set
   * of legal options. Its initial values define the default option values, and
   * the option types can be determined from these values too.
   */
  public static Map<String, Object> optionValues = null;

  /**
   * Initialize for JavaCC
   */
  public static void init() {
    Options.optionValues = new HashMap<>();
    Options.cmdLineSetting = new HashSet<>();
    Options.inputFileSetting = new HashSet<>();

    Options.userOptions.forEach(o -> Options.optionValues.put(o.getName(), o.getDefault()));

    Object object = Options.optionValues.get(Options.USEROPTION__JAVA_TEMPLATE_TYPE);
    boolean isLegacy = Options.JAVA_TEMPLATE_TYPE_CLASSIC.equals(object);
    Options.optionValues.put(Options.NONUSER_OPTION__LEGACY_EXCEPTION_HANDLING, isLegacy);
  }

  /**
   * Convenience method to retrieve integer options.
   */
  public static int intValue(final String option) {
    return ((Integer) Options.optionValues.get(option)).intValue();
  }

  /**
   * Convenience method to retrieve boolean options.
   */
  public static boolean booleanValue(final String option) {
    Object o = Options.optionValues.get(option);
    return (o != null) && ((Boolean) o).booleanValue();
  }

  /**
   * Convenience method to retrieve string options.
   */
  public static String stringValue(final String option) {
    return (String) Options.optionValues.get(option);
  }


  /**
   * Convenience method to retrieve string list options.
   */
  public static List<String> stringListValue(final String option) {
    Object object = Options.optionValues.get(option);
    if (object instanceof List<?>) {
    	return (List<String>) object;
    }
    return null;
  }


  public static Object objectValue(final String option) {
    return Options.optionValues.get(option);
  }


  public static Map<String, Object> getOptions() {
    HashMap<String, Object> ret = new HashMap<>(Options.optionValues);
    return ret;
  }

  /**
   * Keep track of what options were set as a command line argument. We use this
   * to see if the options set from the command line and the ones set in the
   * input files clash in any way.
   */
  private static Set<String> cmdLineSetting   = null;

  /**
   * Keep track of what options were set from the grammar file. We use this to
   * see if the options set from the command line and the ones set in the input
   * files clash in any way.
   */
  private static Set<String> inputFileSetting = null;

  /**
   * Returns a string representation of the specified options of interest. Used
   * when, for example, generating Token.java to record the JavaCC options that
   * were used to generate the file. All of the options must be boolean values.
   *
   * @param interestingOptions the options of interest, eg
   *        {Options.USEROPTION__STATIC, Options.USEROPTION__CACHE_TOKENS}
   * @return the string representation of the options, eg
   *         "STATIC=true,CACHE_TOKENS=false"
   */
  public static String getOptionsString(String[] interestingOptions) {
    StringBuffer sb = new StringBuffer();

    for (int i = 0; i < interestingOptions.length; i++) {
      String key = interestingOptions[i];
      sb.append(key);
      sb.append('=');
      sb.append(Options.optionValues.get(key));
      if (i != (interestingOptions.length - 1)) {
        sb.append(',');
      }
    }

    return sb.toString();
  }

  /**
   * Determine if a given command line argument might be an option flag. Command
   * line options start with a dash&nbsp;(-).
   *
   * @param opt The command line argument to examine.
   * @return True when the argument looks like an option flag.
   */
  public static boolean isOption(final String opt) {
    return (opt != null) && (opt.length() > 1) && (opt.charAt(0) == '-');
  }

  /**
   * Help function to handle cases where the meaning of an option has changed
   * over time. If the user has supplied an option in the old format, it will be
   * converted to the new format.
   *
   * @param name The name of the option being checked.
   * @param value The option's value.
   * @return The upgraded value.
   */
  private static Object upgradeValue(final String name, Object value) {
    if (name.equalsIgnoreCase("NODE_FACTORY") && (value.getClass() == Boolean.class)) {
      if (((Boolean) value).booleanValue()) {
        value = "*";
      } else {
        value = "";
      }
    }

    return value;
  }

  public static void setInputFileOption(Object nameloc, Object valueloc, String name, Object value, Context context) {
    String nameUpperCase = name.toUpperCase();
    if (!Options.optionValues.containsKey(nameUpperCase)) {
      context.errors().warning(nameloc, "Bad option name \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    final Object existingValue = Options.optionValues.get(nameUpperCase);

    value = Options.upgradeValue(name, value);

    if (existingValue != null) {

      boolean isIndirectProperty = nameUpperCase.equalsIgnoreCase(Options.NONUSER_OPTION__LEGACY_EXCEPTION_HANDLING);

      Object object = null;
      if (value instanceof List) {
        object = ((List<?>) value).get(0);
      } else {
        object = value;
      }
      boolean isValidInteger = (object instanceof Integer) && (((Integer) value).intValue() <= 0);
      if (isIndirectProperty || (existingValue.getClass() != object.getClass()) || isValidInteger) {
        context.errors().warning(valueloc,
            "Bad option value \"" + value + "\" for \"" + name + "\".  Option setting will be ignored.");
        return;
      }

      if (Options.inputFileSetting.contains(nameUpperCase)) {
        context.errors().warning(nameloc, "Duplicate option setting for \"" + name + "\" will be ignored.");
        return;
      }

      if (Options.cmdLineSetting.contains(nameUpperCase)) {
        if (!existingValue.equals(value)) {
          context.errors().warning(nameloc, "Command line setting of \"" + name + "\" modifies option value in file.");
        }
        return;
      }
    }

    Options.optionValues.put(nameUpperCase, value);
    Options.inputFileSetting.add(nameUpperCase);

    // Special case logic block here for setting indirect flags

    if (nameUpperCase.equalsIgnoreCase(Options.USEROPTION__JAVA_TEMPLATE_TYPE)) {
      String templateType = (String) value;
      if (!Options.isValidJavaTemplateType(templateType)) {
        context.errors().warning(valueloc, "Bad option value \"" + value + "\" for \"" + name
            + "\".  Option setting will be ignored. Valid options : " + Options.getAllValidJavaTemplateTypes());
        return;
      }

      boolean isLegacy = Options.JAVA_TEMPLATE_TYPE_CLASSIC.equals(templateType);
      Options.optionValues.put(Options.NONUSER_OPTION__LEGACY_EXCEPTION_HANDLING, isLegacy);
    } else 
    if (nameUpperCase.equalsIgnoreCase(Options.USEROPTION__CPP_NAMESPACE)) {
      Options.processNamespaceOption((String) value);
    }
  }


  private static String getAllValidJavaTemplateTypes() {
    return Arrays
        .toString(Options.supportedJavaTemplateTypes.toArray(new String[Options.supportedJavaTemplateTypes.size()]));
  }


  /**
   * Process a single command-line option. The option is parsed and stored in
   * the optionValues map.
   *
   * @param arg
   */
  public static void setCmdLineOption(String arg) {
    final String s;

    if (arg.charAt(0) == '-') {
      s = arg.substring(1);
    } else {
      s = arg;
    }

    String name;
    Object Val;

    // Look for the first ":" or "=", which will separate the option name
    // from its value (if any).
    final int index1 = s.indexOf('=');
    final int index2 = s.indexOf(':');
    final int index;

    if (index1 < 0) {
      index = index2;
    } else if (index2 < 0) {
      index = index1;
    } else if (index1 < index2) {
      index = index1;
    } else {
      index = index2;
    }

    if (index < 0) {
      name = s.toUpperCase();
      if (Options.optionValues.containsKey(name)) {
        Val = Boolean.TRUE;
      } else if ((name.length() > 2) && (name.charAt(0) == 'N') && (name.charAt(1) == 'O')) {
        Val = Boolean.FALSE;
        name = name.substring(2);
      } else {
        System.out.println("Warning: Bad option \"" + arg + "\" will be ignored.");
        return;
      }
    } else {
      name = s.substring(0, index).toUpperCase();
      if (s.substring(index + 1).equalsIgnoreCase("TRUE")) {
        Val = Boolean.TRUE;
      } else if (s.substring(index + 1).equalsIgnoreCase("FALSE")) {
        Val = Boolean.FALSE;
      } else {
        try {
          int i = Integer.parseInt(s.substring(index + 1));
          if (i <= 0) {
            System.out.println("Warning: Bad option value in \"" + arg + "\" will be ignored.");
            return;
          }
          Val = Integer.valueOf(i);
        } catch (NumberFormatException e) {
          Val = s.substring(index + 1);
          if (s.length() > (index + 2)) {
            // i.e., there is space for two '"'s in value
            if ((s.charAt(index + 1) == '"') && (s.charAt(s.length() - 1) == '"')) {
              // remove the two '"'s.
              Val = s.substring(index + 2, s.length() - 1);
            }
          }
        }
      }
    }

    if (!Options.optionValues.containsKey(name)) {
      System.out.println("Warning: Bad option \"" + arg + "\" will be ignored.");
      return;
    }
    Object valOrig = Options.optionValues.get(name);
    if (Val.getClass() != valOrig.getClass()) {
      System.out.println("Warning: Bad option value in \"" + arg + "\" will be ignored.");
      return;
    }
    if (Options.cmdLineSetting.contains(name)) {
      System.out.println("Warning: Duplicate option setting \"" + arg + "\" will be ignored.");
      return;
    }

    Val = Options.upgradeValue(name, Val);

    Options.optionValues.put(name, Val);
    Options.cmdLineSetting.add(name);
    if (name.equalsIgnoreCase(Options.USEROPTION__CPP_NAMESPACE)) {
      Options.processNamespaceOption((String) Val);
    }
  }

  public static void normalize(Context context) {
    if (Options.getDebugLookahead() && !Options.getDebugParser()) {
      if (Options.cmdLineSetting.contains(Options.USEROPTION__DEBUG_PARSER)
          || Options.inputFileSetting.contains(Options.USEROPTION__DEBUG_PARSER)) {
        context.errors()
        .warning("True setting of option DEBUG_LOOKAHEAD overrides " + "false setting of option DEBUG_PARSER.");
      }
      Options.optionValues.put(Options.USEROPTION__DEBUG_PARSER, Boolean.TRUE);
    }

    // Now set the "GENERATE" options from the supplied (or default) JDK
    // version.

    Options.optionValues.put(Options.USEROPTION__GENERATE_CHAINED_EXCEPTION,
        Boolean.valueOf(Options.jdkVersionAtLeast(1.4)));
    Options.optionValues.put(Options.USEROPTION__GENERATE_GENERICS, Boolean.valueOf(Options.jdkVersionAtLeast(1.5)));
    Options.optionValues.put(Options.USEROPTION__GENERATE_STRING_BUILDER,
        Boolean.valueOf(Options.jdkVersionAtLeast(1.5)));
    Options.optionValues.put(Options.USEROPTION__GENERATE_ANNOTATIONS, Boolean.valueOf(Options.jdkVersionAtLeast(1.5)));
  }

  /**
   * Find the lookahead setting.
   *
   * @return The requested lookahead value.
   */
  public static int getLookahead() {
    return Options.intValue(Options.USEROPTION__LOOKAHEAD);
  }

  /**
   * Find the choice ambiguity check value.
   *
   * @return The requested choice ambiguity check value.
   */
  static int getChoiceAmbiguityCheck() {
    return Options.intValue(Options.USEROPTION__CHOICE_AMBIGUITY_CHECK);
  }

  /**
   * Find the other ambiguity check value.
   *
   * @return The requested other ambiguity check value.
   */
  static int getOtherAmbiguityCheck() {
    return Options.intValue(Options.USEROPTION__OTHER_AMBIGUITY_CHECK);
  }

  /**
   * Find the static value.
   *
   * @return The requested static value.
   */
  public static boolean getStatic() {
    return Options.booleanValue(Options.USEROPTION__STATIC);
  }

  public static String getCodeGenerator() {
    String retVal = Options.stringValue(Options.USEROPTION__CODE_GENERATOR);
    return Options.booleanValue(Options.NONUSER_OPTION__INTERPRETER) || retVal.equals("") ? null : retVal;
  }

  public static boolean getNoDfa() {
    return Options.booleanValue(Options.USEROPTION__NO_DFA);
  }

  /**
   * Find the debug parser value.
   *
   * @return The requested debug parser value.
   */
  public static boolean getDebugParser() {
    return Options.booleanValue(Options.USEROPTION__DEBUG_PARSER);
  }

  /**
   * Find the debug lookahead value.
   *
   * @return The requested debug lookahead value.
   */
  public static boolean getDebugLookahead() {
    return Options.booleanValue(Options.USEROPTION__DEBUG_LOOKAHEAD);
  }

  /**
   * Find the debug tokenmanager value.
   *
   * @return The requested debug tokenmanager value.
   */
  public static boolean getDebugTokenManager() {
    return Options.booleanValue(Options.USEROPTION__DEBUG_TOKEN_MANAGER);
  }

  /**
   * Find the error reporting value.
   *
   * @return The requested error reporting value.
   */
  public static boolean getErrorReporting() {
    return Options.booleanValue(Options.USEROPTION__ERROR_REPORTING);
  }

  /**
   * Find the Java unicode escape value.
   *
   * @return The requested Java unicode escape value.
   */
  public static boolean getJavaUnicodeEscape() {
    return Options.booleanValue(Options.USEROPTION__JAVA_UNICODE_ESCAPE);
  }

  /**
   * Find the unicode input value.
   *
   * @return The requested unicode input value.
   */
  public static boolean getUnicodeInput() {
    return Options.booleanValue(Options.USEROPTION__UNICODE_INPUT);
  }

  /**
   * Find the ignore case value.
   *
   * @return The requested ignore case value.
   */
  public static boolean getIgnoreCase() {
    return Options.booleanValue(Options.USEROPTION__IGNORE_CASE);
  }

  /**
   * Find the user token manager value.
   *
   * @return The requested user tokenmanager value.
   */
  public static boolean getUserTokenManager() {
    return Options.booleanValue(Options.USEROPTION__USER_TOKEN_MANAGER);
  }


  /**
   * Find the user charstream value.
   *
   * @return The requested user charstream value.
   */
  public static boolean getUserCharStream() {
    return Options.booleanValue(Options.USEROPTION__USER_CHAR_STREAM);
  }

  /**
   * Find the name of the char-stream
   */
  public static String getCharStreamName() {
    if (Options.getUserCharStream()) {
      return "CharStream";
    } else {
      return Options.getJavaUnicodeEscape() ? "JavaCharStream" : "SimpleCharStream";
    }
  }

  /**
   * Find the build parser value.
   *
   * @return The requested build parser value.
   */
  public static boolean getBuildParser() {
    return Options.booleanValue(Options.USEROPTION__BUILD_PARSER);
  }

  /**
   * Find the build token manager value.
   *
   * @return The requested build token manager value.
   */
  public static boolean getBuildTokenManager() {
    return Options.booleanValue(Options.USEROPTION__BUILD_TOKEN_MANAGER);
  }

  /**
   * Find the token includes
   *
   * @return The requested token includes;
   */
//  public static List<String> getIncludesForToken() {
//    return Options.stringListValue(Options.USEROPTION__CPP_INCLUDES_FOR_TOKEN);
//  }

  /**
   * Find the parser includes
   *
   * @return The requested parser includes;
   */
//  public static List<String> getIncludesForParser() {
//    return Options.stringListValue(Options.USEROPTION__CPP_INCLUDES_FOR_PARSER);
//  }

  /**
   * Find the token manager includes
   *
   * @return The requested token manager includes;
   */
//  public static List<String> getIncludesForTokenManager() {
//    return Options.stringListValue(Options.USEROPTION__CPP_INCLUDES_FOR_TOKEN_MANAGER);
//  }

  /**
   * Find the parser include
   *
   * @return The requested parser includes;
   */
  public static String getParserInclude() {
    return Options.stringValue(Options.USEROPTION__CPP_PARSER_INCLUDE);
  }

  /**
   * Find the token include
   *
   * @return The requested token include;
   */
  public static String getTokenInclude() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_INCLUDE);
  }

  /**
   * Find the token include
   *
   * @return The requested token include;
   */
  public static String getTokenClass() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_CLASS);
  }

  /**
   * Find the token include
   *
   * @return The requested token include;
   */
  public static String getTokenNamespace() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_NAMESPACE);
  }

  /**
   * Find the token constants 
   *
   * @return The requested token ;
   */
  public static String getTokenConstantsInclude() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_CONSTANTS_INCLUDE);
  }

  /**
   * Find the token constants namespace
   *
   * @return The requested token namespace;
   */
  public static String getTokenConstantsNamespace() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_CONSTANTS_NAMESPACE);
  }

  /**
   * Find the token manager Include
   *
   * @return The requested token manager includes;
   */
  public static String getTokenManagerInclude() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDE);
  }

  /**
   * Find the token manager uses parser value.
   *
   * @return The requested token manager uses parser value;
   */
  public static boolean getTokenManagerUsesParser() {
    return Options.booleanValue(Options.USEROPTION__TOKEN_MANAGER_USES_PARSER) && !Options.getStatic();
  }

  /**
   * Find the sanity check value.
   *
   * @return The requested sanity check value.
   */
  public static boolean getSanityCheck() {
    return Options.booleanValue(Options.USEROPTION__SANITY_CHECK);
  }

  /**
   * Find the force lookahead check value.
   *
   * @return The requested force lookahead value.
   */
  public static boolean getForceLaCheck() {
    return Options.booleanValue(Options.USEROPTION__FORCE_LA_CHECK);
  }

  /**
   * Find the common token action value.
   *
   * @return The requested common token action value.
   */

  public static boolean getCommonTokenAction() {
    return Options.booleanValue(Options.USEROPTION__COMMON_TOKEN_ACTION);
  }

  /**
   * Find the cache tokens value.
   *
   * @return The requested cache tokens value.
   */
  public static boolean getCacheTokens() {
    return Options.booleanValue(Options.USEROPTION__CACHE_TOKENS);
  }

  /**
   * Find the keep line column value.
   *
   * @return The requested keep line column value.
   */
  public static boolean getKeepLineColumn() {
    return Options.booleanValue(Options.USEROPTION__KEEP_LINE_COLUMN);
  }

  /**
   * Find the JDK version.
   *
   * @return The requested jdk version.
   */
  public static String getJdkVersion() {
    return Options.stringValue(Options.USEROPTION__JDK_VERSION);
  }

  /**
   * Should the generated code create Exceptions using a constructor taking a
   * nested exception?
   *
   * @return
   */
  public static boolean getGenerateChainedException() {
    return Options.booleanValue(Options.USEROPTION__GENERATE_CHAINED_EXCEPTION);
  }

  public static boolean isGenerateBoilerplateCode() {
    return Options.booleanValue(Options.USEROPTION__GENERATE_BOILERPLATE);
  }

  /**
   * As of 6.1 JavaCC now throws subclasses of {@link RuntimeException} rather
   * than {@link Error} s (by default), as {@link Error} s typically lead to the
   * closing down of the parent VM and are only to be used in extreme
   * circumstances (failure of parsing is generally not regarded as such). If
   * this value is set to true, then then {@link Error}s will be thrown (for
   * compatibility with older .jj files)
   *
   * @return true if throws errors (legacy), false if use
   *         {@link RuntimeException} s (better approach)
   */
  public static boolean isLegacyExceptionHandling() {
    boolean booleanValue = Options.booleanValue(Options.NONUSER_OPTION__LEGACY_EXCEPTION_HANDLING);
    return booleanValue;
  }

  /**
   * Should the generated code contain Generics?
   *
   * @return
   */
  public static boolean getGenerateGenerics() {
    return Options.booleanValue(Options.USEROPTION__GENERATE_GENERICS);
  }

  /**
   * Should the generated code use StringBuilder rather than StringBuffer?
   *
   * @return
   */
  public static boolean getGenerateStringBuilder() {
    return Options.booleanValue(Options.USEROPTION__GENERATE_STRING_BUILDER);
  }

  /**
   * Should the generated code contain Annotations?
   *
   * @return
   */
  public static boolean getGenerateAnnotations() {
    return Options.booleanValue(Options.USEROPTION__GENERATE_ANNOTATIONS);
  }

  /**
   * Should the generated code class visibility public?
   *
   * @return
   */
  public static boolean getSupportClassVisibilityPublic() {
    return Options.booleanValue(Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC);
  }

  /**
   * Determine if the output language is at least the specified version.
   *
   * @param version the version to check against. E.g. <code>1.5</code>
   * @return true if the output version is at least the specified version.
   */
  private static boolean jdkVersionAtLeast(double version) {
    double jdkVersion = Double.parseDouble(Options.getJdkVersion());

    // Comparing doubles is safe here, as it is two simple assignments.
    return jdkVersion >= version;
  }

  /**
   * Return the Token's superclass.
   *
   * @return The required base class for Token.
   */
  public static String getTokenExtends() {
    return Options.stringValue(Options.USEROPTION__TOKEN_EXTENDS);
  }

  /**
   * Return the Token's factory class.
   *
   * @return The required factory class for Token.
   */
  public static String getTokenFactory() {
    return Options.stringValue(Options.USEROPTION__CPP_TOKEN_FACTORY);
  }

  /**
   * Return the file encoding; this will return the file.encoding system
   * property if no value was explicitly set
   *
   * @return The file encoding (e.g., UTF-8, ISO_8859-1, MacRoman)
   */
  public static String getGrammarEncoding() {
    if (Options.stringValue(Options.USEROPTION__GRAMMAR_ENCODING).equals("")) {
      return System.getProperties().getProperty("file.encoding");
    } else {
      return Options.stringValue(Options.USEROPTION__GRAMMAR_ENCODING);
    }
  }

  public static String getNamespace() {
    return Options.stringValue(Options.USEROPTION__CPP_NAMESPACE);
  }

  public static boolean hasNamespace() {
	  return Options.booleanValue(NONUSER_OPTION__HAS_NAMESPACE);
  }

  public static boolean getCppUseArray() {
    return Options.booleanValue(Options.USEROPTION__CPP_USE_ARRAY);
  }
  
  public static String getLibrary() {
    return Options.stringValue(Options.USEROPTION__CPP_LIBRARY);
  }
	  
//  public static boolean isBinaryExe() {
//	return Options.stringValue(Options.USEROPTION__CPP_LIBRARY).equalsIgnoreCase("exe");
//  }
//  public static boolean isBinaryLib() {
//	return Options.stringValue(Options.USEROPTION__CPP_LIBRARY).equalsIgnoreCase("lib");
//  }
//	  
  public static File getOutputDirectory() {
    return new File(Options.stringValue(Options.USEROPTION__OUTPUT_DIRECTORY));
  }

  private static final Set<String> supportedJavaTemplateTypes = new HashSet<>();
  static {
    Options.supportedJavaTemplateTypes.add(Options.JAVA_TEMPLATE_TYPE_CLASSIC);
    Options.supportedJavaTemplateTypes.add(Options.JAVA_TEMPLATE_TYPE_MODERN);
  }


  private static boolean isValidJavaTemplateType(String type) {
    return type == null ? false : Options.supportedJavaTemplateTypes.contains(type.toLowerCase());
  }

  public static String getJavaTemplateType() {
    return Options.stringValue(Options.USEROPTION__JAVA_TEMPLATE_TYPE);
  }

  public static void set(String optionName, Object optionValue) {
    Options.optionValues.put(optionName, optionValue);
  }

  static void setStringOption(String optionName, String optionValue) {
    Options.optionValues.put(optionName, optionValue);
    if (optionName.equalsIgnoreCase(Options.USEROPTION__CPP_NAMESPACE)) {
      Options.processNamespaceOption(optionValue);
    }
    if (optionName.equalsIgnoreCase(Options.NONUSER_OPTION__PARSER_NAME)) {
    	Options.setStringOption(Options.NONUSER_OPTION__PARSER_NAME_UPPER_CASE, 
    		Options.stringValue(Options.NONUSER_OPTION__PARSER_NAME).toUpperCase());
    }
  }

  public static Pair<String,String> getOpenCloseNamespace(String namespace) {
	  Pair<String,String> pair = null;
	    if (namespace.length() > 0) {
	      StringTokenizer st = new StringTokenizer(namespace, "::");
	      String opening = st.nextToken() + " {";
	      String closing = "}";
	      while (st.hasMoreTokens()) {
	        opening = opening + "\nnamespace " + st.nextToken() + " {";
	        closing = closing + "\n}";
	      }
	      pair = new Pair<String, String>(opening, closing);
	    }
	  return pair;
  }
  private static void processNamespaceOption(String optionValue) {
	  Pair<String,String> pair = getOpenCloseNamespace(optionValue);
	  if (pair != null) {
      Options.optionValues.put(Options.NONUSER_OPTION__NAMESPACE_OPEN, pair.getFirst());
      Options.optionValues.put(Options.NONUSER_OPTION__HAS_NAMESPACE, Boolean.TRUE);
      Options.optionValues.put(Options.NONUSER_OPTION__NAMESPACE_CLOSE, pair.getSecond());
    }
  }

  public static boolean isTokenManagerRequiresParserAccess() {
    return Options.getTokenManagerUsesParser() && !Options.getStatic();
  }

  /**
   * Get defined parser recursion depth limit.
   *
   * @return The requested recursion limit.
   */
  public static int getDepthLimit() {
    return Options.intValue(Options.USEROPTION__DEPTH_LIMIT);
  }

  /**
   * Get defined parser stack usage limit.
   *
   * @return The requested stack usage limit.
   */
  public static String getStackLimit() {
    String limit = Options.stringValue(Options.USEROPTION__CPP_STACK_LIMIT);
    if (limit.equals("0")) {
      return "";
    } else {
      return limit;
    }
  }

  /**
   * Gets all the user options (in order)
   *
   * @return
   */
  static Set<OptionInfo> getUserOptions() {
    return Options.userOptions;
  }
  
  static boolean isNullOrEmpty(String string) {
	  if (string == null)
		  return true;
	  return string.isEmpty();
  }
  static boolean isNotEmpty(String string) {
	  if (string == null)
		  return false;
	  return !string.isEmpty();
  }

}
