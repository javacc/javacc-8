[Home](../index.md) > [Documentation](index.md) > Command Line Interface

---

You can obtain a synopsis of the command line syntax by simply typing:

## JavaCC

```java
$ java -cp <path-to-javacc-8-core.jar> javacc
```

Output:

```java
Java Compiler Compiler Version 8.1.0 (Parser Generator)

Usage:
    javacc option-settings inputfile

"option-settings" is a sequence of settings separated by spaces.
Each option setting must be of one of the following forms:

    -optionname=value (e.g. -STATIC=false)
    -optionname:value (e.g. -STATIC:false)
    -optionname       (same as -optionname=true;  e.g. -STATIC)
    -NOoptionname     (same as -optionname=false; e.g. -NOSTATIC)

Option names are case-insensitive: one can use "-nOsTaTiC" instead of "-NOSTATIC".
Option values must be appropriate for the corresponding option,
 and must be either an integer, a boolean or a string value.

The integer valued options are:

    CHOICE_AMBIGUITY_CHECK (default: 2)
    DEPTH_LIMIT            (default: 0)
    LOOKAHEAD              (default: 1)
    OTHER_AMBIGUITY_CHECK  (default: 1)

The boolean valued options are:

    BUILD_PARSER                    (default: true )
    BUILD_TOKEN_MANAGER             (default: true )
    CACHE_TOKENS                    (default: false)
    COMMON_TOKEN_ACTION             (default: false)
    CPP_THROWS_EXCEPTIONS           (default: false)
    CPP_USE_ARRAY                   (default: false)
    DEBUG_LOOKAHEAD                 (default: false)
    DEBUG_PARSER                    (default: false)
    DEBUG_TOKEN_MANAGER             (default: false)
    ERROR_REPORTING                 (default: true )
    FORCE_LA_CHECK                  (default: false)
    GENERATE_BOILERPLATE            (default: true )
    HAS_NAMESPACE                   (default: false)
    IGNORE_ACTIONS                  (default: false)
    IGNORE_CASE                     (default: false)
    JAVA_UNICODE_ESCAPE             (default: false)
    KEEP_LINE_COLUMN                (default: true )
    LEGACY_EXCEPTION_HANDLING       (default: true )
    NO_DFA                          (default: false)
    SANITY_CHECK                    (default: true )
    STATIC                          (default: true )
    STOP_ON_FIRST_ERROR             (default: false)
    SUPPORT_CLASS_VISIBILITY_PUBLIC (default: true )
    TOKEN_MANAGER_USES_PARSER       (default: false)
    UNICODE_INPUT                   (default: false)
    USER_CHAR_STREAM                (default: false)
    USER_TOKEN_MANAGER              (default: false)

The string valued options are:

    CODE_GENERATOR            (default: "")
    GRAMMAR_ENCODING          (default: "")
    JAVA_TEMPLATE_TYPE        (default: "classic")
    LIBRARY                   (default: "")
    NAMESPACE                 (default: "")
    OUTPUT_DIRECTORY          (default: ".")
    PARSER_INCLUDE            (default: "")
    STACK_LIMIT               (default: "")
    TOKEN_CLASS               (default: "")
    TOKEN_CONSTANTS_INCLUDE   (default: "")
    TOKEN_CONSTANTS_NAMESPACE (default: "")
    TOKEN_EXTENDS             (default: "")
    TOKEN_FACTORY             (default: "")
    TOKEN_INCLUDE             (default: "")
    TOKEN_MANAGER_INCLUDE     (default: "")
    TOKEN_NAMESPACE           (default: "")

EXAMPLE:
    javacc -STATIC=false -LOOKAHEAD:2 -NONO_DFA -debug_parser mygrammar.jj
```

Any option may be set either on the command line as shown above, or in the grammar file as described in the [JavaCC grammar](grammar.md). The effect is exactly the same.

If the same option is set in both the command line and the grammar file, then the option setting in the command line takes precedence.

## JJTree

```java
$ java -cp <path-to-javacc-8-core.jar> jjtree
```

Output:

```java
Java Compiler Compiler Version 8.1.0 (Tree Builder)

Usage:
    jjtree option-settings inputfile

"option-settings" is a sequence of settings separated by spaces.
Each option setting must be of one of the following forms:

    -optionname=value (e.g. -STATIC=false)
    -optionname:value (e.g. -STATIC:false)
    -optionname       (same as -optionname=true;  e.g. -STATIC)
    -NOoptionname     (same as -optionname=false; e.g. -NOSTATIC)

Option names are case-insensitive, so one can use "-nOsTaTiC" instead of "-NOSTATIC".
Option values must be appropriate for the corresponding option,
 and must be either an integer, a boolean or a string value.

The boolean valued JJTree-specific options are:

    BUILD_NODE_FILES         (default: true )
    MULTI                    (default: false)
    NODE_DEFAULT_VOID        (default: false)
    NODE_SCOPE_HOOK          (default: false)
    NODE_USES_PARSER         (default: false)
    TRACK_TOKENS             (default: false)
    VISITOR                  (default: false)

The string valued JJTree-specific options are:

    JJTREE_OUTPUT_DIRECTORY  (default: value of (JavaCC) OUTPUT_DIRECTORY option)
    NODE_CLASS               (default: "")
    NODE_DIRECTORY           (default: value of JJTREE_OUTPUT_DIRECTORY option)
    NODE_EXTENDS             (default: "")
    NODE_FACTORY             (default: "")
    NODE_PACKAGE             (default: "")
    NODE_PREFIX              (default: "AST")
    OUTPUT_FILE              (default: replace input file suffix by .jj)
    VISITOR_DATA_TYPE        (default: "")
    VISITOR_EXCEPTION        (default: "")
    VISITOR_RETURN_TYPE      (default: "Object")

JJTree accepts all JavaCC options, and inserts them into the generated file;
 it also uses the following ones:

    IGNORE_ACTIONS           (default: false)
    NAMESPACE                (default: "")
    OUTPUT_DIRECTORY         (default: ".")
    STATIC                   (default: true )

EXAMPLES:
    jjtree -STATIC=false -VISITOR_RETURN_TYPE="my.pkg.type" mygrammar.jjt
```

---

[TOP](#javacc)

[JavaCC Command Line](cli.md) &hellip; [JavaCC Grammar](grammar.md) &hellip; [JavaCC BNF](bnf.md) &hellip; [JavaCC API](api.md) &hellip; [JJTree](jjtree.md) &hellip; [JJDoc](jjdoc.md)

<br>
