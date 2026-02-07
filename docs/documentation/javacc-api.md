<!--
Copyright (c) 2020-2025, Sreeni Viswanadha <sreeni@viswanadha.net>.
Copyright (c) 2024-2025, Marc Mazas <mazas.marc@gmail.com>.
All rights reserved.
&para;
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
&para;
    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the copyright holders nor the names of its
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.
&para;
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.
-->

[Home](../index.md) > [Documentation](index.md) > JavaCC API

---

This page is a comprehensive list of all classes, methods, and variables available for use.

### Contents

- [JavaCC API](#javacc-api)
    * [Non-terminals in the input grammar](#non-terminals-in-the-input-grammar)
    * [Parser actions](#parser-actions)
    * [Token Manager interface](#token-manager-interface)
    * [Constructors and other initialization routines](#constructors-and-other-initialization-routines)
    * [Token class](#token-class)
    * [Reading tokens from the input stream](#reading-tokens-from-the-input-stream)
    * [Working with debugger tracing](#working-with-debugger-tracing)
    * [Customizing error messages](#customizing-error-messages)
    * [ErrorHandler interface (C++ only)](#errorhandler-interface-c-only)

- [JJTree API](#jjtree-api)

## JavaCC API

These classes, methods, and variables are typically used from the actions that are embedded in a JavaCC grammar. In the sample code used below, it is assumed that the name of the generated parser is `TheParser`.

### Non-terminals in the input grammar

---

For each non-terminal `NT` in the input grammar file, the following method is generated into the parser class:

```java
/*
 * When this method is called, the input stream is parsed to match this non-terminal.
 * On a successful parse, this method returns normally. On detection of a parse error,
 * an error message is displayed and the method returns by throwing an exception of
 * the type ParseException.
 */
returntype NT(parameters) throws ParseException;
```

Here, *returntype* and *parameters* are what were specified in the JavaCC input file in the definition of `NT` (where `NT` occurred on the left-hand side).

*Note that all non-terminals in a JavaCC input grammar have equal status - it is possible to parse to any non-terminal by calling the non-terminal's method*.

<br>

### Parser actions

---

```java
/*
 * This variable holds the last token consumed by the parser and can be used
 * in parser actions. This is exactly the same as the token returned by getToken(0).
 */
Token token;
```

In addition, the two methods - `getToken(int i)` and `getNextToken()` can also be used in actions to traverse the token list.

<br>

### Token Manager interface

---

Typically, the token manager interface is not to be used. Instead all access must be made through the parser interface. However, in certain situations - such as if you are not building a parser and building only the token manager - the token manager interface is useful.

The token manager provides the following routine:

```java
/*
 * Each call to this method returns the next token in the input stream.
 * This method throws a `ParseError` exception when there is a lexical error,
 * i.e. it could not find a match for any of the specified tokens from the
 * input stream.
 */
Token getNextToken() throws ParseError;
```

<br>

### Constructors and other initialization routines

---

```java
/*
 * This creates a new parser object, which in turn creates a new token manager
 * object that reads its tokens from "stream". This constructor is available
 * only when both the options USER_TOKEN_MANAGER and USER_CHAR_STREAM are false.
 *
 * If the option STATIC is true, this constructor (along with other constructors)
 * can be called exactly once to create a single parser object.
 */
TheParser.TheParser(java.io.InputStream stream)
```

```java
/*
 * Similar to the previous constructor, except that this one is available only
 * when the option USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is true.
 */
TheParser.TheParser(CharStream stream)
```

```java
/*
 * This reinitializes an existing parser object. In addition, it also reinitializes
 * the existing token manager object that corresponds to this parser object.
 *
 * The result is a parser object with the exact same functionality as one that was
 * created with the constructor above. The only difference is that new objects are
 * not created.
 *
 * This method is available only when both the options USER_TOKEN_MANAGER and
 * USER_CHAR_STREAM are false. If the option STATIC is true, this (along with the
 * other ReInit methods) is the only way to restart a parse operation for there
 * is only one parser and all one can do is reinitialize it.
 */
void TheParser.ReInit(java.io.InputStream stream)
```

```java
/*
 * Similar to the previous method, except that this one is available only when the
 * option USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is true.
 */
void TheParser.ReInit(CharStream stream)
```

```java
/*
 * This creates a new parser object which uses an already created token manager
 * object "tm" as its token manager.
 *
 * This constructor is only available if option USER_TOKEN_MANAGER is false.
 *
 * If the option STATIC is true, this constructor (along with other constructors)
 * can be called exactly once to create a single parser object.
 */
TheParser(TheParserTokenManager tm)
```

```java
/*
 * Similar to the previous constructor, except that this one is available only when
 * the option USER_TOKEN_MANAGER is true.
 */
TheParser(TokenManager tm)
```

```java
/*
 * This reinitializes an existing parser object with the token manager
 * object "tm" as its new token manager.
 *
 * This method is only available if option USER_TOKEN_MANAGER is false.
 *
 * If the option STATIC is true, this (along with the other ReInit methods)
 * is the only way to restart a parse operation for there is only one parser
 * and all one can do is reinitialize it.
 */
void TheParser.ReInit(TheParserTokenManager tm)
```

```java
/*
 * Similar to the previous method, except that this one is available only when
 * the option USER_TOKEN_MANAGER is true.
 */
void TheParser.ReInit(TokenManager tm)
```

```java
/*
 * Creates a new token manager object initialized to read input from "stream".
 *
 * When the option STATIC is true, this constructor may be called only once. This
 * is available only when USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is true.
 *
 * When USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is false (the default situation),
 * a constructor similar to the one above is available with the type CharStream
 * replaced as follows:
 * - When JAVA_UNICODE_ESCAPE is false and UNICODE_INPUT is false,
 *   CharStream is replaced by ASCII_CharStream.
 * - When JAVA_UNICODE_ESCAPE is false and UNICODE_INPUT is true,
 *   CharStream is replaced by UCode_CharStream.
 * - When JAVA_UNICODE_ESCAPE is true and UNICODE_INPUT is false,
 *   CharStream is replaced by ASCII_UCodeESC_CharStream.
 * - When JAVA_UNICODE_ESCAPE is true and UNICODE_INPUT is true,
 *   CharStream is replaced by UCode_UCodeESC_CharStream.
 */
TheParserTokenManager.TheParserTokenManager(CharStream stream)
```

```java
/*
 * Reinitializes the current token manager object to read input from "stream".
 *
 * When the option STATIC is true, this is the only way to restart a token manager operation.
 * This is available only when USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is true.
 *
 * When USER_TOKEN_MANAGER is false and USER_CHAR_STREAM is false (the default situation),
 * a constructor similar to the one above is available with the type CharStream
 * replaced as follows:
 * - When JAVA_UNICODE_ESCAPE is false and UNICODE_INPUT is false,
 *   CharStream is replaced by ASCII_CharStream.
 * - When JAVA_UNICODE_ESCAPE is false and UNICODE_INPUT is true,
 *   CharStream is replaced by UCode_CharStream.
 * - When JAVA_UNICODE_ESCAPE is true and UNICODE_INPUT is false,
 *   CharStream is replaced by ASCII_UCodeESC_CharStream.
 * - When JAVA_UNICODE_ESCAPE is true and UNICODE_INPUT is true,
 *   CharStream is replaced by UCode_UCodeESC_CharStream.
 */
void TheParserTokenManager.ReInit(CharStream stream)
```

<br>

### Token class

---

The Token class is the type of token objects that are created by the token manager after a successful scanning of the token stream. These token objects are then passed to the parser and are accessible to the actions in a JavaCC grammar usually by grabbing the return value of a token.

The methods `getToken()` and `getNextToken()` described below also give access to objects of this type.

Each `Token` object has the following fields and methods:

```java
/*
 * This is the index for this kind of token in the internal representation scheme of JavaCC.
 *
 * When tokens in the JavaCC input file are given labels, these labels are used to generate
 * int constants that can be used in actions. T
 *
 * The value 0 is always used to represent the predefined token <EOF>. A constant "EOF"
 * is generated for convenience in the ...Constants file.
 */
int kind;
```

```java
/*
 * These indicate the beginning and ending positions of the token as it appeared
 * in the input stream.
 */
int beginLine, beginColumn, endLine, endColumn;
```

```java
/*
 * This represents the image of the token as it appeared in the input stream.
 */
String image;
```

```java
/*
 * A reference to the next regular (non-special) token from the input stream.
 *
 * If this is the last token from the input stream, or if the token manager has
 * not read tokens beyond this one, this field is set to null.
 */
Token next;
```
The description in the above paragraph holds only if this token is also a regular token

Note there are two kinds of tokens:
* Regular tokens are the normal tokens that are fed to the parser.
* Special tokens are other useful tokens (like comments) that are not discarded (like white space).

For more information on the different kinds of tokens please see the token manager [tutorial](../tutorials/token-manager.md).

```java
/*
 * This field is used to access special tokens that occur prior to this token,
 * but after the immediately preceding regular (non-special) token.
 *
 * If there are no such special tokens, this field is set to null. When there are more
 * than one such special token, this field refers to the last of these special tokens,
 * which in turn refers to the next previous special token through its specialToken field,
 * and so on until the first special token (whose specialToken field is null).
 *
 * The next fields of special tokens refer to other special tokens that immediately
 * follow it (without an intervening regular token). If there is no such token,
 * this field is null.
 */
Token specialToken;
```

```java
/*
 * An optional attribute value of the Token.
 *
 * Tokens which are not used as syntactic sugar will often contain meaningful values
 * that will be used later on by the compiler or interpreter. This attribute value is
 * often different from the image. Any subclass of Token that actually wants to return
 * a non-null value can override this method as appropriate.
 */
public Object getValue();
```

```java
/*
 * Returns a new token object as its default behavior. If you wish to perform
 * special actions when a token is constructed or create subclasses of class Token
 * and instantiate them instead, you can redefine this method appropriately.
 *
 * The only constraint is that this method returns a new object of type Token
 * (or a subclass of Token).
 */
static final Token newToken(int ofKind);
```

```java
/*
 * Returns a new token object as its default behavior. If you wish to perform
 * special actions when a token is constructed or create subclasses of class Token
 * and instantiate them instead, you can redefine this method appropriately.
 *
 * The only constraint is that this method returns a new object of type Token
 * (or a subclass of Token).
 */
static final Token newToken(int ofKind, String image);
```

<br>

### Reading tokens from the input stream

---

There are two methods available for this purpose:

```java
/*
 * This method returns the next available token in the input stream and moves
 * the token pointer one step in the input stream (i.e., this changes the state
 ( of the input stream).
 *
 * If there are no more tokens available in the input stream, the exception
 * ParseError is thrown.
 *
 * Care must be taken when calling this method since it can interfere with the
 * parser's knowledge of the state of the input stream, current token, etc.
 */
Token TheParser.getNextToken() throws ParseError
```

```java
/*
 * This method returns the index-th token from the current token ahead in the
 * token stream.
 * - If index is 0, it returns the current token (the last token returned by
 *   getNextToken or consumed by the parser);
 * - if index is 1, it returns the next token (the next token that will be
 *   returned by getNextToken of consumed by the parser) and so on.
 *
 * The index parameter cannot be negative.
 *
 * This method does not change the input stream pointer (i.e., it does not change
 * the state of the input stream). If an attempt is made to access a token beyond
 * the last available token, the exception ParseError is thrown.
 *
 * If this method is called from a semantic lookahead specification, which in turn
 * is called during a lookahead determination process, the current token is temporarily
 * adjusted to be the token currently being inspected by the lookahead process.
 */
Token TheParser.getToken(int index) throws ParseError
```

<br>

### Working with debugger tracing

---

When you generate parsers with the options `DEBUG_PARSER` or `DEBUG_LOOKAHEAD`, these parsers produce a trace of their activity which is printed to the user console. You can insert calls to the following methods to control this tracing activity:

```java
void TheParser.enable_tracing()
void TheParser.disable_tracing()
```

For convenience, these methods are available even when you build parsers without the debug options. In this case, these methods are no-ops. Hence you can permanently leave these methods in your code and they automatically kick in when you use the debug options.

<br>

### Customizing error messages

---

To help the user in customizing error messages generated by the parser and lexer, the user is offered the facilities described in this section. In the case of the parser, these facilities are only available if the option `ERROR_REPORTING` is `true`, while in the case of the lexer, these facilities are always available.

The parser contains the following method definition:

```java
/*
 * To customize error reporting by the parser, the parser class must be subclassed
 * and this method redefined in the subclass.
 */
protected void token_error() { ... }
```

To help with creating your error reporting scheme, the following variables are available:

```java
/*
 * The line and column where the error was detected.
 */
protected int error_line, error_column;
```

```java
/*
 * The image of the offending token or set of tokens. When a lookahead of more than
 * 1 is used, more than one token may be present here.
 */
protected String error_string;
```

```java
/*
 * An array of images of legitimate token sequences. Here again, each legitimate token
 * sequence may be more than just one token when a lookahead of more than 1 is used.
 */
protected String[] expected_tokens;
```

The lexer contains the following method definition:

```java
/*
 * To customize error reporting by the lexer, the lexer class must be subclassed
 * and this method redefined in the subclass.
 */
protected void LexicalError() { ... }
```

To help with creating your error reporting scheme, the following variables are available:

```java
/*
 * The line and column where the error was detected.
 */
protected int error_line, error_column;
```


```java
/*
 * The partial string that has been read since the last successful token match was performed.
 */
protected String error_after;
```

```java
/*
 * The offending character.
 */
protected char curChar;
```

<br>

### ErrorHandler interface (C++ only)

---

Since the parser doesn't use exceptions in C++, we provide an interface - `ErrorHandler` that handles the various different errors encountered during the parse.

```java
/*
 * This protected field indicates the number of errors. If you are subclassing this class,
 * it's your responsibility to update this field.
 */
int error_count;
```

```java
/*
 * This public function is called when the parser encounters a different token when
 * expecting to consume a specific kind of token.
 *
 * Parameters:
 * - int expectedKind - token kind that the parser was trying to consume.
 * - string expectedToken - the image of the token - tokenImages[expectedKind].
 * - Token* actual - the actual token that the parser got instead.
 */
void handleUnexpectedToken()
```

```java
/*
 * This public function is called when the parser cannot continue parsing any further.
 *
 * Parameters:
 * - Token* last - the last token successfully parsed.
 * - Token* unexpected - the token at which the error occurs.
 * - string production - the name of the production in which this error occurs.
 */
void handleParseError()
```

```java
/*
 * This public function returns the number of errors.
 */
int getErrorCount()
```

<br>

## JJTree API

See in [JJTree API](jjtree.md#jjtree-api).

<br>

---

[Top](#contents)

[JavaCC Command Line](cli.md) &hellip; [JavaCC Grammar](grammar.md) &hellip; [JavaCC BNF](bnf.md) &hellip; [JavaCC API](api.md) &hellip; [JJTree](jjtree.md) &hellip; [JJDoc](jjdoc.md)

<br>
