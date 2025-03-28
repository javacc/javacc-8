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

[Home](../index.md) > [Tutorials](index.md) > Token Manager

---

** TODO to be reviewed, mainly on debug traces that may have changed from v7 laundry to v8 **

This tutorial refers to examples that are available in the source code on [GitHub](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead).


### Contents

- [What is "looking ahead", why, how?](#what-is-looking-ahead-why-how)
    * [A tour through example 1](#a-tour-through-example-1)
    * [Avoiding backtracking](#avoiding-backtracking)
    * [Choice points in JavaCC grammars](#choice-points-in-javacc-grammars)
    * [Default choice determination algorithm](#default-choice-determination-algorithm)
      + [Example 2](#example-2)
      + [Example 3](#example-3)
      + [Example 4](#example-4)
      + [Example 5](#example-5)
    * [Overriding the default choice determination algorithm](#overriding-the-default-choice-determination-algorithm)
      + [Option 1 - Modifying the grammar](#option-1-modifying-the-grammar)
          - [Example 6](#example-6)
          - [Example 7](#example-7)
      + [Option 2 - Adding parser hints](#option-2-adding-parser-hints)
  
- [Setting a global LOOKAHEAD](#setting-a-global-lookahead)
  
- [Setting a local LOOKAHEAD](#setting-a-local-lookahead)
    * [Setting a "multi-token" LOOKAHEAD](#setting-a-multi-token-lookahead)
      + [Example 8](#example-8)
      + [Example 9](#example-9)
      + [Example 10](#example-10)
    * [Setting a "syntactic" LOOKAHEAD](#setting-a-syntactic-lookahead)
    * [Setting a "semantic" LOOKAHEAD](#setting-a-semantic-lookahead)
    * [General syntax of a local LOOKAHEAD](#general-syntax-of-a-local-lookahead)
  
- ["Nested evaluation" of local LOOKAHEADs](#nested-evaluation-of-local-lookaheads)
  
- [Local LOOKAHEADs at non choice points](#local-lookaheads-at-non-choice-points)
    * [Not inside an optional construct](#not-inside-an-optional-construct)
    * [Inside the optional construct construct zero-or-more ()*](#inside-the-optional-construct-zero-or-more)
    * [Inside the optional construct construct one-or-more ()+](#inside-the-optional-construct-one-or-more)
    * [Inside the optional construct construct zero-or-one ()? / []](#inside-the-optional-construct-zero-or-one)
    * [An example of a grammar managing its versions through lookaheads at non choice points](#an-example-of-a-grammar-managing-its-versions-through-lookaheads-at-non-choice-points)
    * [An example of rewriting a grammar with semantic lookaheads at non choice points](#an-example-of-rewriting-a-grammar-with-semantic-lookaheads-at-non-choice-points)
  
- [Keeping the warnings displayed](#keeping-the-warnings-displayed)
  
- [Reading the parser and lookahead debug traces](#reading-the-parser-and-lookahead-debug-traces)
  
## What is "looking ahead", why, how?

The job of a parser is to read an input stream and determine whether or not the input stream conforms to the grammar.

This determination in its most general form can be quite time consuming.

### A tour through example 1

```java
void Input() :
{}
{
  "a" BC() "c"
}

void BC() :
{}
{
  "b" [ "c" ]
}
```

In this simple example, it is quite clear that there are exactly two strings that match the above grammar, namely:

```java
abc
abcc
```

The general way to perform this match is to walk through the grammar based on the string as follows (here we use `abc` as the input string):

| Step  | Description |
| :---: | :--- |
| 1 | There is only one choice here - the first input character must be `a` and since that is indeed the case, we are OK.|
| 2 | We now proceed on to non-terminal BC. Again there is only one choice for the next input character - it must be `b`. The input matches this one too, so we are still OK.|
| 3 | We now come to a *choice point* in the grammar. We can either go inside the `[...]` and match it, or ignore it altogether. We decide to go inside. So the next input character must be a `c`. We are again OK.|
| 4 | Now we have completed with non-terminal BC and go back to non-terminal `Input`. Now the grammar says the next character must be yet another `c`. But there are no more input characters, so we have a problem.|
| 5 | When we have such a problem in the general case, we conclude that we may have made a bad choice somewhere. In this case, we made a bad choice in step [3]. So we retrace our steps back to step [3] and make another choice and try that. This process is called *backtracking*.|
| 6 | We have now backtracked and made the other choice we could have made at step [3] - namely, ignore the `[...]`. Now we have completed with non-terminal BC and go back to non-terminal `Input`. Now the grammar says the next character must be yet another `c`. The next input character is a `c`, so we are OK now.|
| 7 | We realize we have reached the end of the grammar (end of non-terminal `Input`) successfully. This means we have successfully matched the string `abc` to the grammar.|

As the above example indicates, the general problem of matching an input with a grammar may result in large amounts of backtracking and making new choices and this can consume a lot of time. The amount of time taken can also be a function of how the grammar is written. Note that many grammars can be written to cover the same set of inputs - or the same language, i.e. there can be multiple equivalent grammars for the same input language.

The following grammar would speed up the parsing of the same language as compared to the previous grammar:

```java
void Input() :
{}
{
  "a" "b" "c" [ "c" ]
}
```

The following grammar slows it down even more since the parser has to backtrack all the way to the beginning:

```java
void Input() :
{}
{
  "a" "b" "c" "c"
|
  "a" "b" "c"
}
```

We can even have a grammar that looks like the following:

```java
void Input() :
{}
{
  "a" ( BC1() | BC2() )
}

void BC1() :
{}
{
  "b" "c" "c"
}

void BC2() :
{}
{
  "b" "c" [ "c" ]
}
```

This grammar can match `abcc` in two ways, and is therefore considered *ambiguous*.

### Avoiding backtracking

The performance hit from such backtracking is unacceptable for most systems that include a parser. Hence most parsers do not backtrack in this general manner - or do not backtrack at all. Rather, they make decisions at choice points based on limited information and then commit to it.

Parsers generated by JavaCC make decisions at choice points based on some exploration of tokens further ahead in the input stream, and once they make such a decision, they commit to it - i.e. no backtracking is performed once a decision is made.

The process of exploring tokens further in the input stream is termed *looking ahead* into the input stream - hence our use of the term `LOOKAHEAD`.

Since some of these decisions may be made with less than perfect information, you need to know something about `LOOKAHEAD` to make your grammar work correctly. *N.B. JavaCC will warn you in these situations*.

The two ways in which you make the choice decisions work properly are:

1. Modify the grammar to make it simpler.
2. Insert hints at the more complicated choice points to help the parser make the right choices.


### Choice points in JavaCC grammars

There are 4 types of choice points in JavaCC:

| Expansion                                                   | Description |
| :---------------------------------------------------------: | :--- |
| <code>( exp1 \| exp2 \| ... )</code> | The generated parser must somehow determine which of `exp1`, `exp2` etc to select to continue parsing. |
| <code>( exp )?</code> | The generated parser must somehow determine whether to choose `exp` or to continue beyond the `( exp )?` without choosing `exp`.  
*N.B. `( exp )?` may also be written as `[ exp ]`, as well as `( exp | {} )`*. |
| <code>( exp )*</code> | The generated parser must do the same thing as in the previous case, and furthermore, after each time a successful match of `exp` (if `exp` was chosen) is completed, this choice determination must be made again. |
| <code>( exp )+</code> | This is essentially similar to the previous case with a mandatory first match to `exp`. |

Remember that token specifications that occur within angular brackets `<...>` also have choice points. But these choices are made in different ways and are the subject of a different tutorial.

### Default choice determination algorithm

The default choice determination algorithm looks ahead 1 token in the input stream and uses this to help make its choice at choice points. The following examples will describe the default algorithm fully.

#### Example 2

Consider the following grammar:

```java
void basic_expr() :
{}
{
  <ID> "(" expr() ")" // Choice 1
|
  "(" expr() ")"      // Choice 2
|
  "new" <ID>          // Choice 3
}
```

The choice determination algorithm works as follows:

```java
if (next token is <ID>) {
  // choose Choice 1
} else if (next token is "(") {
  // choose Choice 2
} else if (next token is "new") {
  // choose Choice 3
} else {
  // produce an error message
}
```

In the above example, the grammar has been written such that the default choice determination algorithm does the right thing. Another thing to note is that the choice determination algorithm works in a top to bottom order - if `Choice 1` was selected, the other choices are not even considered. While this is not an issue in this example (except for performance) it will become important when local ambiguities require the insertion of `LOOKAHEAD` hints.

#### Example 3

Consider the modified grammar:

```java
void basic_expr() :
{}
{
  <ID> "(" expr() ")" // Choice 1
|
  "(" expr() ")"      // Choice 2
|
  "new" <ID>          // Choice 3
|
  <ID> "." <ID>       // Choice 4
}
```

Then the default algorithm will always choose `Choice 1` when the next input token is `<ID>` and never choose `Choice 4` even if the token following `<ID>` is a `.`.

You can try running the parser generated from [Example 3](#example-3) on the input `id1.id2`. It will complain that it encountered a `.` when it was expecting a `(`.

*N.B. When you built the parser, it would have given you the following warning message:*

```java
Warning: Choice conflict involving two expansions at
         line 25, column 3 and line 31, column 3 respectively.
         A common prefix is: <ID>
         Consider using a lookahead of 2 for earlier expansion.
```

JavaCC detected a situation in the grammar which may cause the default lookahead algorithm to do strange things. The generated parser will still work using the default lookahead algorithm, but it may not do what you expect of it.

#### Example 4

Now consider the following grammar:

```java
void identifier_list() :
{}
{
  <ID> ( "," <ID> )*
}
```

Suppose the first `<ID>` has already been matched and that the parser has reached the choice point (the `(...)*` construct). Here's how the choice determination algorithm works:

```java
while (next token is ",") {
  choose the nested expansion (i.e. go into the (...)* construct)
  consume the "," token
  if (next token is <ID>) {
    consume it, otherwise report error
  }
}
```

In the above example, note that the choice determination algorithm does not look beyond the `(...)*` construct to make its decision.

#### Example 5

Suppose there was another production in that same grammar as follows:

```java
void funny_list() :
{}
{
  identifier_list() "," <INT>
}
```

When the default algorithm is making a choice at `( "," <ID> )*` it will always go into the `(...)*` construct if the next token is a `,`. It will do this even when `identifier_list` was called from `funny_list` and the token after the `,` is an `<INT>`. Intuitively, the right thing to do in this situation is to skip the `(...)*` construct and return to `funny_list`.

As a concrete example, suppose your input was `id1, id2, 5`, the parser will complain that it encountered a `5` when it was expecting an `<ID>`.

*N.B. When you built the parser, it would have given you the following warning message:*

```java
Warning: Choice conflict in (...)* construct at line 25, column 8.
         Expansion nested within construct and expansion following construct
         have common prefixes, one of which is: ","
         Consider using a lookahead of 2 or more for nested expansion.
```

JavaCC detected a situation in the grammar which may cause the default lookahead algorithm to do strange things. The generated parser will still work using the default lookahead algorithm, but it may not do what you expect of it.

We have shown examples of two kinds of choice points in the examples above - `exp1 | exp2 | ...`, and `(exp)*`. The other two types of choice points `(exp)+` and `(exp)?` behave similarly to `(exp)*` so it is not necessary to provide further examples of their use.

              

### Overriding the default choice determination algorithm

So far, we have described the default lookahead algorithm of the generated parsers. In the majority of situations, the default algorithm works just fine. In situations where it does not work well, JavaCC provides you with warning messages like the ones shown above. If you have a grammar that goes through JavaCC without producing any warnings, then the grammar is a `LL(1)` grammar. Essentially, `LL(1)` grammars are those that can be handled by top-down parsers (such as those generated by JavaCC) using at most one token of `LOOKAHEAD`.

When you get these warning messages, you can do one of two things.

#### Option 1 - Modifying the grammar

You can modify your grammar so that the warning messages go away. That is, you can attempt to make your grammar `LL(1)` by making some changes to it.

##### Example 6

The following grammar shows how you how to change [Example 3](#example-3) to make it `LL(1)`:

```java
void basic_expr() :
{}
{
  <ID> ( "(" expr() ")" | "." <ID> )
|
  "(" expr() ")"
|
  "new" <ID>
}
```

What we have done here is to refactor the fourth choice into the first choice. Note how we have placed their common first token `<ID>` outside the parentheses, and then within the parentheses we have yet another choice which can now be performed by looking at only one token in the input stream and comparing it with `(` and `.`. This process of modifying grammars to make them `LL(1)` is called *left factoring*.

##### Example 7

The following grammar shows how [Example 5](#example-5) may be changed to make it `LL(1)`:

```java
void funny_list() :
{}
{
  <ID> "," ( <ID> "," )* <INT>
}
```

*N.B. This change is somewhat more drastic.*

#### Option 2 - Adding parser hints

You can provide the generated parser with some hints to help it out in the non-`LL(1)` situations that the warning messages bring to your attention.

All such hints are specified using either setting the global `LOOKAHEAD` value to a larger value or by using the `LOOKAHEAD(...)` construct to provide a local hint.

A design decision must be made to determine if `Option 1` or `Option 2` is the right one to take. The only advantage of choosing `Option 1` is that it makes your grammar perform better. JavaCC generated parsers can handle `LL(1)` constructs much faster than other constructs. However, the advantage of choosing `Option 2` is that you have a simpler grammar - one that is easier to develop and maintain, and focuses on human-friendliness and not machine-friendliness.

Sometimes `Option 2` is the only choice - especially in the presence of lexical actions.

Suppose [Example 3](#example-3) contained actions as shown below:

```java
void basic_expr() :
{}
{
  { initMethodTables(); } <ID> "(" expr() ")"
|
  "(" expr() ")"
|
  "new" <ID>
|
  { initObjectTables(); } <ID> "." <ID>
}
```

                                  

Since the actions are different, left-factoring cannot be performed (unless the actions can be refactored to a common one).

## Setting a global LOOKAHEAD

You can set a global `LOOKAHEAD` specification by using the option `LOOKAHEAD` either from the command line, or at the beginning of the grammar file in the options section. The value of this option is an integer which is the number of tokens to look ahead when making choice decisions. As you may have guessed, the default value of this option is `1` - which derives the default `LOOKAHEAD` algorithm described above.

Suppose you set the value of this option to `2`. Then the `LOOKAHEAD` algorithm derived from this looks at two tokens (instead of just one token) before making a choice decision. Hence, in [Example 3](#example-3), `Choice 1` will be taken only if the next two tokens are `<ID>` and `(`, while `Choice 4` will be taken only if the next two tokens are `<ID>` and `.`. Hence, the parser will now work properly for [Example 3](#example-3). Similarly, the problem with [Example 5](#example-5) also goes away since the parser goes into the `(...)*` construct only when the next two tokens are `,` and `<ID>`.

By setting the global `LOOKAHEAD` to `2` the parsing algorithm essentially becomes `LL(2)`. Since you can set the global `LOOKAHEAD` to any value, parsers generated by JavaCC are called `LL(k)` parsers.

## Setting a local LOOKAHEAD

You can also set a local `LOOKAHEAD` specification that affects only a specific choice point. This way, the majority of the grammar can remain `LL(1)` and hence perform better, while at the same time one gets the flexibility of `LL(k)` grammars.

There are 3 basic ways to specify a local `LOOKAHEAD` (the *multi-token*, the *syntactic* and the *semantic* ways), which may be combined (therefore giving 6 combinations). We'll look at each one.

### Setting a "multi-token" LOOKAHEAD

#### Example 8

Here's how [Example 3](#example-3) is modified with local `LOOKAHEAD` to fix the choice ambiguity problem:

```java
void basic_expr() :
{}
{
  LOOKAHEAD(2)
  <ID> "(" expr() ")" // Choice 1
|
  "(" expr() ")"      // Choice 2
|
  "new" <ID>          // Choice 3
|
  <ID> "." <ID>       // Choice 4
}
```

Only the first choice (the first condition in the translation below) is affected by the `LOOKAHEAD` specification. All others continue to use a single token of `LOOKAHEAD`:

```java
if (next 2 tokens are <ID> and "(" ) {
  // choose Choice 1
} else if (next token is "(") {
  // choose Choice 2
} else if (next token is "new") {
  // choose Choice 3
} else if (next token is <ID>) {
  // choose Choice 4
} else {
  // produce an error message
}
```

#### Example 9

Similarly, [Example 5](#example-5) can be modified as shown below:

```java
void identifier_list() :
{}
{
  <ID> ( LOOKAHEAD(2) "," <ID> )*
}
```

*N.B. The `LOOKAHEAD` specification has to occur inside the `(...)` which is where the choice is being made. The translation for this construct is shown below:*

```java
consume the <ID> token
while (next 2 tokens are "," and <ID>) {
  choose the nested expansion (i.e., go into the (...)* construct)
  consume the "," token
  consume the <ID> token
}
```

**We strongly discourage you from modifying the global LOOKAHEAD default**.

Most grammars are predominantly `LL(1)`, hence you will be unnecessarily degrading performance by converting the entire grammar to `LL(k)` to facilitate just some portions of the grammar that are not `LL(1)`. If your grammar and input files being parsed are very small, then this is okay.

You should also keep in mind that the warning messages JavaCC prints when it detects ambiguities at choice points (such as the two messages shown earlier) simply tells you that the specified choice points are not `LL(1)`. JavaCC does not verify the correctness of your local `LOOKAHEAD` specification - it assumes you know what you are doing.


#### Example 10

JavaCC cannot verify the correctness of local `LOOKAHEAD`s as the following example of `if` statements illustrates:

```java
void IfStm() :
{}
{
 "if" C() S() [ "else" S() ]
}

void S() :
{}
{
  ...
|
  IfStm()
}
```

This example is the famous *dangling else* problem. If you have a program that looks like:

```java
if C1 if C2 S1 else S2
```

The `else S2` can be bound to either of the two `if` statements. The standard interpretation is that it is bound to the inner `if` statement (the one closest to it). The default choice determination algorithm happens to do the right thing, but it still prints the following warning message:

```java
Warning: Choice conflict in [...] construct at line 25, column 15.
         Expansion nested within construct and expansion following construct
         have common prefixes, one of which is: "else"
         Consider using a lookahead of 2 or more for nested expansion.
```

To suppress the warning message, you could simply tell JavaCC that you know what you are doing as follows:

```java
void IfStm() :
{}
{
 "if" C() S() [ LOOKAHEAD(1) "else" S() ]
}
```

#### More on the choice conflict warnings

As seen before, JavaCC emits warnings similar than the followings
:

```java
Warning: Choice conflict involving two expansions at
         line ll, column cc and line ll2, column cc2 respectively.
         A common prefix is: <ID>
         Consider using a lookahead of 2 for earlier expansion.
```

```java
Warning: Choice conflict involving two expansions at
         line ll, column cc and line ll2, column cc2 respectively.
         A common prefix is: <ID> "a"
         Consider using a lookahead of 3 for earlier expansion.
```

```java
Warning: Choice conflict in [...] construct at line ll, column cc.
         Expansion nested within construct and expansion following construct
         have common prefixes, one of which is: "a"
         Consider using a lookahead of 2 or more for nested expansion.
```

```java
Warning: Choice conflict in [...] construct at line ll, column cc.
         Expansion nested within construct and expansion following construct
         have common prefixes, one of which is: "a" "b"
         Consider using a lookahead of 3 or more for nested expansion.
```

Note that JavaCC will not try to find the longest common prefixes, it reports only the first one or the first two common tokens and tells to consider using a lookahead of '2 or more' or '3 or more'; most of the time, following blindly a try '3' and test, then '4' and test, then '5' and test... approach leads to a big disappointment.

This is specially true when the choices include optional parts (like in the following extract), as it may be impossible to know in advance the number of common tokens:

```java
void pfx() :
{}
{
  (
    // javacc says common prefix is "x" "x" & lookahead of 3 or more,
    //  but it should be "x" "x" "a" "b"+ & lookahead of 5+
    LOOKAHEAD(3)
    "x"
    "x"
    "a"
    ( "b" | "c" )+
    "d"
  | 
    "x"
    ( "x" "a" | "b" )+
    "e"
  )
}
```

### Setting a "syntactic" LOOKAHEAD

Consider the following production taken from the Java grammar:

```java
void TypeDeclaration() :
{}
{
  ClassDeclaration()
|
  InterfaceDeclaration()
}
```

At the syntactic level, `ClassDeclaration` can start with any number of `abstract`, `final`, and `public` statements. While a subsequent semantic check will produce error messages for multiple uses of the same modifier, this does not happen until parsing is completely over. Similarly, `InterfaceDeclaration` can start with any number of `abstract` and `public` statements.

What if the next tokens in the input stream are a very large number of `abstract` statements (say 100 of them) followed by `interface`? It is clear that a fixed amount of `LOOKAHEAD` (such as `LOOKAHEAD(100)`) will not suffice. One can argue that this is such a weird situation that it does not warrant any reasonable error message and that it is okay to make the wrong choice in some pathological situations.

But suppose one wanted to be precise about this. The solution here is to set the `LOOKAHEAD` to infinity - that is, set no bounds on the number of tokens to `LOOKAHEAD`. One way to do this is to use a very large integer value (such as the largest possible integer) as follows:

```java
void TypeDeclaration() :
{}
{
  LOOKAHEAD(2147483647)
  ClassDeclaration()
|
  InterfaceDeclaration()
}
```

One can also achieve the same effect with *syntactic* `LOOKAHEAD`. In syntactic `LOOKAHEAD`, you specify an expansion to try it out and, if that succeeds, then the following choice is taken.

The above example can be rewritten using syntactic `LOOKAHEAD` as follows:

```java
void TypeDeclaration() :
{}
{
  LOOKAHEAD(ClassDeclaration())
  ClassDeclaration()
|
  InterfaceDeclaration()
}
```

Essentially, what this is saying is:

```java
if (enough next tokens from the input stream match at least one possible sequence of non-terminals in ClassDeclaration) {
  // choose ClassDeclaration()
} else if (next token matches InterfaceDeclaration) {
  // choose InterfaceDeclaration()
} else {
  // produce an error message
}
```

The problem with the above syntactic `LOOKAHEAD` specification is that the `LOOKAHEAD` calculation takes too much time and does a lot of unnecessary checking. In this case, the `LOOKAHEAD` calculation can stop as soon as the token `class` is encountered, but the specification forces the calculation to continue until the end of the class declaration has been reached, which is rather time consuming.

This problem can be solved by placing a shorter expansion to try out in the syntactic `LOOKAHEAD` specification as in the following example:

```java
void TypeDeclaration() :
{}
{
  LOOKAHEAD( ( "abstract" | "final" | "public" )* "class" )
  ClassDeclaration()
|
  InterfaceDeclaration()
}
```

Essentially, what this is saying is:

```java
if (enough next tokens from the input stream match the sequence of zero to many
  "abstract", "final", and "public" tokens followed by a "class" token) {
  // choose ClassDeclaration()
} else if (next token matches InterfaceDeclaration) {
  // choose InterfaceDeclaration()
} else {
  // produce an error message
}
```

By doing this, you make the choice determination algorithm stop as soon as it sees `class` i.e. make its decision at the earliest possible time.

You can place a bound on the number of tokens to consume during syntactic lookahead as follows:

```java
void TypeDeclaration() :
{}
{
  LOOKAHEAD(10, ( "abstract" | "final" | "public" )* "class" )
  ClassDeclaration()
|
  InterfaceDeclaration()
}
```
In this case, the `LOOKAHEAD` determination is not permitted to go beyond `10` tokens. If it reaches this limit and is still successfully matching `( "abstract" | "final" | "public" )* "class"`, then `ClassDeclaration` is selected.

When such a limit is not specified, it defaults to the largest integer value (`2147483647`).

### Setting a "semantic" LOOKAHEAD

Let us go back to [Example 1](#a-tour-through-example-1):

```java
void Input() :
{}
{
  "a" BC() "c"
}

void BC() :
{}
{
  "b" [ "c" ]
}
```

Let us suppose that there is a good reason for writing a grammar this way (maybe the way actions are embedded). As noted earlier, this grammar recognizes two string `abc` and `abcc`. The problem here is that the default `LL(1)` algorithm will choose the `[ "c" ]` in `BC()` every time it sees a `c` and therefore `abc` will never be matched, because `Input()` will not match its `"c"`. We need to specify that this choice must be made only when the next token is a `c`, and the token following that is not a `c`. This is a negative statement - one that cannot be made using syntactic `LOOKAHEAD`.

We can use semantic `LOOKAHEAD` for this purpose. With semantic `LOOKAHEAD`, you can specify any arbitrary boolean expression whose evaluation determines which choice to take at a choice point.

The above example can be instrumented with semantic `LOOKAHEAD` as follows:

```java
void BC() :
{}
{
  "b"
  [ LOOKAHEAD( { getToken(1).kind == TC && getToken(2).kind != TC } )
    < TC : "c" >
  ]
}
```

First, we give the token `c` a label `TC` so that we can refer to it from the semantic `LOOKAHEAD`. The boolean expression essentially states the desired property.

The choice determination decision is therefore:

```java
if (next token is "c" and following token is not "c") {
  // choose the nested expansion (i.e., go into the [...] construct)
} else {
  // go beyond the [...] construct without entering it.
}
```

This example can be rewritten to combine both syntactic and semantic `LOOKAHEAD` as follows :

```java
void BC() :
{}
{
  "b"
  [ 
    LOOKAHEAD( "c", { getToken(2).kind != TC } )
    < TC : "c" >
  ]
}
```

Recognize the first `c` using syntactic `LOOKAHEAD` and the absence of the second using semantic `LOOKAHEAD`.

### General syntax of a local LOOKAHEAD

We've pretty much covered the various aspects of `LOOKAHEAD` in the previous sections. We shall now present a formal language reference for `LOOKAHEAD` in JavaCC.

The general structure of a `LOOKAHEAD` specification is:

```java
LOOKAHEAD ( amount, expansion, { boolean_expression } )
```

The `amount` specifies the (maximum) number of tokens to `LOOKAHEAD`, `expansion` specifies the expansion to use to perform syntactic `LOOKAHEAD`, and `boolean_expression` is the expression to use for semantic `LOOKAHEAD`.

At least one of the three entries must be present. If more than one are present, they are separated by commas. And when there are more than one, they are logically anded, in an order that depends on the combination (more on this in the following examples).

The values for each of these entities, when they are present or not (between the delimiters) are defined below:

```java
amount:
  - if present, it takes the given value
  - if not present, and if expansion is present, it takes the value 2147483647
  - otherwise (case only the boolean_expression is present) it takes the value 0
```

                                                               

```java
expansion:
  - if present, it takes the given expansion
  - if not present, it takes the expansion being considered
```

```java
boolean_expression:
  - if present, it takes the given boolean_expression
  - if not present, it takes the value true (and thus no expression is generated)
```
   
                      

When `amount` is `0`:

- if there is a `{ boolean_expression }`, the 0 amount is not taken in account (i.e. is like if it was not there, the semantic lookahead is not affected),
- otherwise, no token scanning nor syntactic `LOOKAHEAD` is performed *AND the remaining choices ARE NOT GENERATED*: it merely means that the developer wants to erase the expansion_choice keeping only the first expansion and wants the grammar fail if the next token does not match the beginning of the expansion (why these desires ? may be for a quick and dirty temporary test case).

When `amount` is `1`, it merely means that the default choice determination algorithm is what is wanted.

As the usual `amount` is `>1`, the lookahead form `LOOKAHEAD(am)` is called a "multi-token" lookahead, and the lookahead form `LOOKAHEAD(am, expansion)` could be called a "limited syntactic" lookahead.

## "Nested evaluation" of local LOOKAHEADs

Up to here we have described the local lookaheads algorithms when the following expansions do not include themselves lookahead specifications.

But BNF productions are very often nested, so it is quite frequent the following expansions do include themselves local lookahead specifications.

Now we describe this case.

Lets consider the following grammar, where the 6 possible syntaxes (but 15 combinations as amounts of `0`, `1` and `>1` may trigger different behaviors) could occur for each of the 2 `LOOKAHEAD` specifications: (the full code can be found in [NestedLookaheadGrammar_Example11.jj](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead/src/main/javacc/NestedLookaheadGrammar_Example11.jj))

```java
void A() : {}
{
  LOOKAHEAD(amount, expansion, { expression }) // A-lookahead-spec
  B() V()
|
  C() W()
}
  
void B() : {}
{
  LOOKAHEAD(amount, expansion, { expression }) // B-lookahead-spec
  C() X()
|
  D() Y()
}
  
void C() : {}
{
  D() Z()
}
```

We'll look at the "evaluation" or not of the inner B-lookahead-spec when the outer A-lookahead-spec is "evaluated".

NOTE: we are not speaking of the lookahead evaluated within a `B()` call (in the generated code in `B()` or in additional methods) (which is not affected by `A()`, as `B()` can be called from elsewhere), we are speaking of the lookahead evaluated in `A()` (in the generated code in `A()` or in additional methods) for the A-lookahead, which can incorporate parts of B-lookaheads! So do not be confused about the term "nested evaluation".

Instead of giving summarizing rules with conditions, we prefer to give the result for each possibility.

In the following list, we have the 15 different possibilities for the outer A-lookahead specification.

In it we call `common-nested-B-lookahead` the algorithm that JavaCC will extract from `B()`, which performs parts of the `B()` inner lookaheads (this is counter intuitive and the source of misunderstandings), and that will be incorporated and most of the time called in the resulting A-lookahead algorithm. It logically returns true or false for success or failure. Remember that it is not the execution of `B()` (for example, parser actions are not incorporated in it). Also, this algorithm is independent of `A()` (so it will be incorporated and most of the time called in all the callers lookaheads). And by the way, if `B()` contains a `0` amount lookahead specification at some choice point, execution of `B()` will not perform a lookahead at this point and will not call the following choices, but execution of the `common-nested-B-lookahead` algorithm will process this choice point and the next choices!

1. outer A amount 0

        no outer A-lookahead is generated (because of the amount of 0 - inner B-lookahead does not influence this)

2. outer A amount 1:

        common-nested-B-lookahead will be evaluated, what will tell failure or success

3. outer A amount >1:

        common-nested-B-lookahead will be evaluated, what will tell failure or success

4. outer A expansion: 

        common-nested-B-lookahead will be evaluated, what will tell failure or success

5. outer A { expression }: 

        outer A-lookahead reduces (as of no amount which defaults to 0) to evaluating the expression, what will tell success or failure (inner B-lookahead does not influence this)

6. outer A amount 0, expansion: 

        no outer A-lookahead is generated (because of the amount of 0 - inner B-lookahead does not influence this)

7. outer A amount 1, expansion: 

        common-nested-B-lookahead will be evaluated, what will tell failure or success

8. outer A amount >1, expansion: 

        common-nested-B-lookahead will be evaluated, what will tell failure or success

9. outer A expansion, { expression }: 

        common-nested-B-lookahead will be evaluated, and if it tells failure that will be all, otherwise then expression will be evaluated, what will tell failure or success

10. outer A amount 0, { expression }: 

        outer A-lookahead reduces (as of the amount of 0) to evaluating the expression, what will tell success or failure (inner B-lookahead does not influence this)

11. outer A amount 1, { expression }: 

        common-nested-B-lookahead will be evaluated, and if it tells failure that will be all, otherwise then expression will be evaluated, what will tell failure or success

12. outer A amount >1, { expression }: 

        common-nested-B-lookahead will be evaluated, and if it tells failure that will be all, otherwise then expression will be evaluated, what will tell failure or success

13. outer A amount 0, expansion, { expression }: 

        outer A-lookahead reduces (as of the amount of 0) to evaluating the expression (inner B-lookahead does not influence this); note that the expansion is ignored

14. outer A amount 1, expansion, { expression }: 

        common-nested-B-lookahead will be evaluated, and if it tells failure that will be all, otherwise then expression will be evaluated, what will tell failure or success

15. outer A amount >1, expansion, { expression }: 

        common-nested-B-lookahead will be evaluated, and if it tells failure that will be all, otherwise then expression will be evaluated, what will tell failure or success

It is worth noting that:

- if present the expression is evaluated last

So now what is inside this `common-nested-B-lookahead`? It depends on the different forms of the inner B-lookahead before the choice. Here under we have the 15 different possibilities for the inner B-lookahead specification:

1. inner B amount 0:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

2. inner B amount 1:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

3. inner B amount >1:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

4. inner B expansion:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

5. inner B { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

6. inner B amount 0, expansion:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

7. inner B amount 1, expansion:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

8. inner B amount >1, expansion:

        scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

9. inner B expansion, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure
        
10. inner B amount 0, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

11. inner B amount 1, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

12. inner B amount >1, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

13. inner B amount 0, expansion, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

14. inner B amount 1, expansion, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

15. inner B amount >1, expansion, { expression }:

        evaluates the expression, and if it tells failure that will be all, otherwise then scans (up to the remaining limit number of) token(s) to match the choice, what will tell success or failure

It is worth noting that:

- the given or default amount and the given expansion have no impact on the generated algorithm: it tries to match part or all of the following expansion at the choice point, up to the remaining number of tokens considering the top level lookahead amount (not the nested lookahead amount) and the number of already scanned tokens (therefore the term `scans (up to the remaining limit number of) token(s)`)
- if present the expression is evaluated first

## Local LOOKAHEADs at non choice points

A local `LOOKAHEAD` specification primary use is to override the default choice algorithm at a choice point (i.e. a point where there is a choice conflict).

But it is not infrequent that the developer mistakenly puts one at a non choice point (i.e. a point where there is a no choice conflict - which does not mean it is not a choice). To be kind with him, instead of rising an error, JavaCC will raise a warning telling:
- "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored." otherwise.

Besides this, JavaCC allows to put a `LOOKAHEAD` specification with a semantic part at a non choice point and considers it, for a special kind of processing. In this case, JavaCC will raise a warning telling:
- "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here."

In fact, the implementation seems not to fit exactly with these rules / warnings. To be more precise, we try to investigate this through a systematic analysis through a grammar whose the full code can be found in [NonChoiceLookaheadGrammar_Example12.jj](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead/src/main/javacc/NonChoiceLookaheadGrammar_Example12.jj).

### Not inside an optional construct

Lets consider first the `LOOKAHEAD` specification not inside an optional construct (`[]`, `()?`, `()*`, `()+`) (and not at a choice point):

```java
void A() : {}
{
  "v_0"
  "x_0" "end"
|
  "v_1"
  LOOKAHEAD(amount, expansion, { expression }) // A-lookahead-spec
  "x_1" "end"
}
```

In the following list, we have the 15 different possibilities for the A-lookahead specification.

1. amount 0

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed IGNORED
        So this case is of NO REAL USE although is does not harm

2. amount 1:

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored" but this is somewhat WRONG:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF parsing will continue on next tokens
        So this case is of NO MEANINGFUL BEHAVIOR

3. amount >1:

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored" but this is somewhat WRONG:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF parsing will continue on next tokens
        So this case is of NO MEANINGFUL BEHAVIOR

4. expansion: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored" but this is somewhat WRONG:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF parsing will continue on next tokens
        So this case is of NO MEANINGFUL BEHAVIOR

5. { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case can have a MEANINGFUL BEHAVIOR in some corner cases

6. amount 0, expansion: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed IGNORED
        So this case is of NO REAL USE although is does not harm

7. amount 1, expansion: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored" but this is somewhat WRONG:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF parsing will continue on next tokens
        So this case is of NO MEANINGFUL BEHAVIOR

8. amount >1, expansion: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored" but this is somewhat WRONG:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF parsing will continue on next tokens
        So this case is of NO MEANINGFUL BEHAVIOR

9. expansion, { expression }: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here." and this is almost RIGHT:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF:
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case is of NO MEANINGFUL BEHAVIOR

10. amount 0, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case can have a MEANINGFUL BEHAVIOR in some corner cases

11. amount 1, { expression }: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here." and this is almost RIGHT:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF:
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case is of NO MEANINGFUL BEHAVIOR

12. amount >1, { expression }: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here." and this is almost RIGHT:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF:
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case is of NO MEANINGFUL BEHAVIOR

13. amount 0, expansion, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case can have a MEANINGFUL BEHAVIOR in some corner cases

14. amount 1, expansion, { expression }: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here." and this is almost RIGHT:
        after the "v_1" token, anything else than an EOF throws a ParseException, and after an EOF:
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case is of NO MEANINGFUL BEHAVIOR

15. amount >1, expansion, { expression }: 

        JavaCC emits the warning "Encountered LOOKAHEAD(...) at a non-choice location.  Only semantic lookahead will be considered here." and this is almost RIGHT:
        after the "v_1" token, anything else than an EOF throws a c, and after an EOF:
        the semantic expression is evaluated, and if false it gives a ParseException, and if true parsing continues on next tokens 
        So this case is of NO MEANINGFUL BEHAVIOR

*N.B. using non-terminals (even "empty" ones - with a single `{}` action and no production) instead of terminals in the expansion part in and after the `A-lookahead-spec` does not impact the previous observations.*

Note: case 5, which can be of some interest although it throws a `ParseException` if the expression evaluates to false (as cases 10 & 13), is illustrated in an example discussed further.

### Inside the optional construct zero-or-more ()*

Lets consider now the `LOOKAHEAD` specification inside the optional construct `()*` (and not at a choice point):

```java
void E() : {}
{
  "v_0"
  ( "x_0" )*
  "end"
|
  "v_1"
  (
    LOOKAHEAD(amount, expansion, { expression }) // E-lookahead-spec
    "x_1"
  )*
  "end"
}
```

In the same manner, in the following list, we have the 15 different possibilities for the E-lookahead specification. The results are different than the previous ones.

1. amount 0

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        but it leads to a compiler error
        So this case is of NO USE

2. amount 1:

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed IGNORED:
        the logic behaves as if none, which is ok
        So this case is of NO MEANINGFUL USE although it does not harm

3. amount >1:

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        the logic behaves as if inside a choice, which is ok
        So this case is of NO MEANINGFUL USE although it does not harm

4. expansion: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        the logic behaves as if inside a choice, which is ok
        So this case is of NO MEANINGFUL USE although it does not harm

5. { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        as long as the semantic expression evaluates to true, the tokens of the `()*` construct ares consumed, and this stops when the semantic expression evaluates to false
        So this case can have a MEANINGFUL USE (like dynamic control of the number of occurrences of ()*))

6. amount 0, expansion: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        But it leads to a compiler error
        So this case is of NO USE

7. amount 1, expansion: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed IGNORED:
        the logic behaves as if none, which is ok
        So this case is of NO MEANINGFUL USE although it does not harm

8. amount >1, expansion: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        the logic behaves as if inside a choice, which is ok
        So this case is of NO MEANINGFUL USE although it does not harm

9. expansion, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        it loops scanning for the given expansion then evaluating the expression, exiting the loop when the expression evaluates to false
        So this case is of REAL USE (additional semantic validation - like dynamic control on the occurrences of ()*)

10. amount 0, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED
        as long as the semantic expression evaluates to true, the tokens of the `()*` construct ares consumed, and this stops when the semantic expression evaluates to false
        So this case can have a MEANINGFUL USE (like dynamic control of the number of occurrences of ()*))

11. amount 1, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        it loops scanning for the next token then evaluating the expression, exiting the loop when the expression evaluates to false
        So this case is of REAL USE (additional semantic validation - like dynamic control on the occurrences of ()*)

12. amount >1, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        it loops scanning for the 2 next tokens then evaluating the expression, exiting the loop when the expression evaluates to false
        So this case is of REAL USE (additional semantic validation - like dynamic control on the occurrences of ()*)

13. amount 0, expansion, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED
        as long as the semantic expression evaluates to true, the expansion is consumed (up to the given number of tokens limit), and this stops when the semantic expression evaluates to false
        So this case can have a MEANINGFUL USE (like dynamic control of the number of occurrences of ()*))

14. amount 1, expansion, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        it loops scanning for the one token of the given expansion then evaluating the expression, exiting the loop when the expression evaluates to false
        So this case is of REAL USE (additional semantic validation - like dynamic control on the occurrences of ()*)

15. amount >1, expansion, { expression }: 

        JavaCC does NOT emit a warning but the LOOKAHEAD specification is indeed CONSIDERED:
        it loops scanning for the given expansion (up to the given number of tokens limit) then evaluating the expression, exiting the loop when the expression evaluates to false
        So this case is of REAL USE (additional semantic validation - like dynamic control on the occurrences of ()*)

Note: cases 5, 9, 10, 11, 12, 13, 14 & 15 (all with an expression), do not throw a `ParseException` if the expression evaluates to false (as in the previous section), which broadens the scope of their use.

### Inside the optional construct one-or-more ()+

Using the `LOOKAHEAD` specification inside the optional construct `()+` (and not at a choice point) leads to the same observations as in the previous section: the structure of the generated method is the same, the tokens are consumed at the beginning of the while loop instead of at the end.

### Inside the optional construct zero-or-one ()? / []

Using the `LOOKAHEAD` specification inside the optional constructs `()?` or `[]` (and not at a choice point) leads to the roughly same observations as in the section dealing with the `()*` construct: although the structure of the generated method is a little different (no labeled while loops), the behaviors are the same, except for cases 1 and 6, which do not lead to compiler errors, but are of no sensible use, as it removes the optional aspect of the construct.

### An example of a grammar managing its versions through lookaheads at non choice points

For a more realistic exercise, look at an example grammar that tries to use parser actions and semantic lookaheads at non choice points to cover 2 versions of the grammar specification, whose full code can be found in [VersionAwareGrammar_Example13.jj](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead/src/main/javacc/VersionAwareGrammar_Example13.jj).

Productions can be dedicated to a specific version or cover multiple versions:

- by using parser actions they can be made tolerant to some required version, that is raise a warning when a version 2 syntax input is parsed by a version 1 & 2 able production required externally to accept only version 1 syntax inputs

- by using semantic lookaheads at non choice points they can be made intolerant to some required version, that is raise a `ParseException` when a version 2 syntax input is parsed by a version 1 & 2 able production required externally to accept only version 1 syntax inputs

### An example of rewriting a grammar with semantic lookaheads at non choice points

Back to the example shown at [More on the choice conflict warnings](#more-on-the-choice-conflict-warnings):

```java
void pfx() :
{}
{
  (
    // javacc says common prefix is "x" "x" & lookahead of 3,
    //  but it should be "x" "x" "a" "b"+ & lookahead of 5+
    LOOKAHEAD(3)
    "x"
    "x" "a"
    ( "b" | "c" )+
    "d"
  | 
    "x"
    ( "x" "a" | "b" )+
    "e"
  )
}
```

We can completely remove looking ahead for more than 1 token (and get no choice conflict warnings) by rewriting it like this:

```java
/** pfx rewritten by distributing / factorizing / reordering / combing / adding actions & semantic LOOKAHEADs */
void pfx_rw4() :
{}
{
  { prod = "pfx_rw4"; }
  (
    "x"
    (
      "x" "a"
      (
        "b"
        // 7- start
        { c = x = false; } // initialise flags
        (
          ( "b"
          | "c" { c = true; } // set flag
          | "x" "a" { x = true;} // set flag
          )*
          (
            LOOKAHEAD( 1, { !x } ) // 1 is needed! - exclude unwanted combination with flag
            { String sd = "LOOKAHEAD( 1, { !x } ) before \"d\""; } // just as a tag in the generated code
            "d"
          | LOOKAHEAD( 1, { !c } ) // 1 is needed! - exclude unwanted combination with flag
            { String se = "LOOKAHEAD( 1, { !c } ) before \"e\""; } // just as a tag in the generated code
            "e"
          )
        )
        // 7- end
      | "c" ( "b" | "c" )* "d"
      | "x" "a" ( "x" "a" | "b" )+ "e"
      | "e"
      )
    | "b" ( "x" "a" | "b" )* "e"
    )
  )
}
}
```

Obviously, this form of the grammar is quite different than the original one, and there are other possible forms, like the following with a syntactic lookahead (which performs more work than the previous form, as shown in the lookahead debug trace):

```java
/** pfx with syntactic LOOKAHEAD */
void pfx_synla() :
{}
{
  (
    // full syntactic lookahead
    LOOKAHEAD("x" "x" "a" ( "b" | "c" )+ "d")
    { String sy = "LOOKAHEAD(\"x\" \"x\" \"a\" ( \"b\" | \"c\" )+ \"d\")"; } // just as a tag in the generated code
    "x"
    "x"
    "a"
    ( "b" | "c" )+
    "d"
  | 
    "x"
    ( "x" "a" | "b" )+
    "e"
  )
}
```

The full code for this example (with the detailed rewriting steps) can be found at [RewrittenGrammar_Example14.jj](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead/src/main/javacc/RewrittenGrammar_Example14.jj):

## Keeping the warnings displayed

When you specify a `LOOKAHEAD` where you had a warning, JavaCC assumes you know what you are doing and does not display the warning (remember that this does not imply that you have fixed the problem).

However, during your grammar tuning or long after when you come back on it to write a new version, you may want to see again these warning to check if your lookahead specifications are still relevant regarding them.

Instead of commenting all your lookahead specifications, you can set the option `FORCE_LA_CHECK` to `true`. This will tell JavaCC to display all warnings messages - while still to take in account the lookahead specifications.

By the way, it can be a good habit to add comments to your lookahead specifications recording their warnings (type of choice conflict, prefix...): it can greatly ease the work of the people who will later maintain and modify the grammar, as they will be able so see if some change needs to be studied.

## Reading the parser and lookahead debug traces

In this section we'll describe the parser and lookahead debug trace you can obtain by turning on the options  `DEBUG_PARSER = true;` and `DEBUG_LOOKAHEAD = true;`. Note that last one implicitly turns on the first one.

We'll take a slightly modified version of example 8 grammar, whose full code can be found in [ReadingLookaheadDebugTrace_Example8.jj](https://github.com/javacc/javacc-8-java/blob/release/examples/Lookahead/src/main/javacc/ReadingLookaheadDebugTrace_Example8.jj).

The interesting parts are:

```java
options
{
  ...
//  DEBUG_PARSER = true; // (JavaCC - default false)
  DEBUG_LOOKAHEAD = true; // (JavaCC - default false)
}

PARSER_BEGIN(ReadingLookaheadDebugTrace_Example8)
...
public class ReadingLookaheadDebugTrace_Example8 {

  public static void main(final String args[]) throws ParseException {
    ReadingLookaheadDebugTrace_Example8 parser = null;
    for (int i = 0; i < input.length; i++)
    {
      System.out.println("input " + i+ " : " + input[i]); 
      parser = new ReadingLookaheadDebugTrace_Example8(new StringReader(input[i]));
      try { parser.basic_expr(); }
      // catch PE & TMR for running all the test suite
      catch (ParseException pe) { parser.report("ParseException: " + pe.getMessage()); }
      catch (TokenMgrError tme) { parser.report("TokenMgrError: " + tme.getMessage()); }
      finally { System.out.println(); }
    }
  }

...
  static String input [] = new String [] { //
    "new cd",       // -> choice 3
    "( EXPR )",     // -> choice 2
    "e.f",          // -> choice 4
    "ab ( EXPR )",  // -> choices 1 / 1
    "ba ( )",       // -> choices 1 / 2
    "new .cd",      // -> ParseException
  };

}

PARSER_END(ReadingLookaheadDebugTrace_Example8)
...
void basic_expr() : {}
{
  LOOKAHEAD(2) // choice 1
  <ID> args()
| // choice 2
  "(" expr() ")"
| // choice 3
  "new" <ID>
| // choice 4
  <ID> "." <ID>
}

void expr() : {} { "EXPR" }

void args() : {}
{
  LOOKAHEAD(2)
  "(" expr() ")"
|
  "(" ")"
}
```

In this grammar with have:

* a top level `LOOKAHEAD` in production `basic_expr()`, for the choice conflict between choices 1 and 4
* a nested `LOOKAHEAD` (the one in production `args()`, called in production `basic_expr()` )
 
On the first input (`new cd`, which matches choice 3, we get the following trace:

```
input 0 : new cd
Call: 0: basic_expr-53-1 on next token <"new" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 55 column 3 in basic_expr-53-1) on next token <"new" at line 1 column 1>
    Visited token (la=1): <"new" at line 1 column 1>; Expected token: <<ID>>
  Return: 2: Exiting FAILED LOOKAHEAD(2/1) (at line 55 column 3 in basic_expr-53-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <"new" at line 1 column 1>
  Consumed token: <<ID>: "cd" at line 1 column 5>
Return: 0: basic_expr-53-1 on consumed token <"cd" at line 1 column 5>
```

Pairs of `Call: n: prod-line-col on next token <...>` / `Return: n: prod-line-col on consumed token <...>` lines and `Consumed token <...>` lines are parser traces on productions calls, where:

* `n` is (twice) the indentation level (0, 2x1, 2x2...), for easier matching of the lines pairs
* `prod` is the name of the production, `line` and `col` are the begining line and column numbers of the production in the grammar file
* the token information in `<...>` includes its image and its position in the input stream

Pairs of `Call: n: Entering LOOKAHEAD(la) (at ... in ...) on next token <...>` / `Return: n: Exiting FAILED/SUCCESSFUL LOOKAHEAD(la/laix) (at ... in ...) on consumed token <...>` lines and `Visited token <...>` lines are traces on lookahead calls, quite similar with the previous ones, with in addition:

* the information on a failure or a success of the `LOOKAHEAD`
* the information on the 1-based (as in the grammar) amount limit in `la` and the 0-based current index of the to be scanned token in `laix` or in `(la=n)`
* the line and column of the location in the grammar file of the `LOOKAHEAD` statement, after the `at`

A token marked `visited` is a token scanned by the lookahead algorithm (including the default non explicit algorithm) - and may be put back in the input stream -, a token marked `consumed` is a token really consumed in the input stream.

So in this first input case:

* entering the top level production, the choice 1 & 4 lookahead (expecting `<ID> "("`) is performed: it fails on the first token (found `"new"`)
* then javacc knows it can be only `"("` (choice 2) or `"new"` (choice 3), and proceeds on consuming `"new"`, then `<ID>`.
 
Now on the second input (`( EXPR )`, which matches choice 2, we get the following trace:

```
input 1 : ( EXPR )
Call: 0: basic_expr-53-1 on next token <"(" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 55 column 3 in basic_expr-53-1) on next token <"(" at line 1 column 1>
    Visited token (la=1): <"(" at line 1 column 1>; Expected token: <<ID>>
  Return: 2: Exiting FAILED LOOKAHEAD(2/1) (at line 55 column 3 in basic_expr-53-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <"(" at line 1 column 1>
  Call: 2: expr-65-1 on next token <"EXPR" at line 1 column 3>
    Consumed token: <"EXPR" at line 1 column 3>
  Return: 2: expr-65-1 on consumed token <"EXPR" at line 1 column 3>
  Consumed token: <")" at line 1 column 8>
Return: 0: basic_expr-53-1 on consumed token <")" at line 1 column 8>
```

In this second input case, we have something quite similar with the previous one, with the following difference: the second expansion_unit being a production `expr()`instead of a token `"new"`, there is a production call/return trace instead of a consumed token trace.
 
Now on the third input (`( e.f )`, which matches choice 4, we get the following trace:

```
input 2 : e.f
Call: 0: basic_expr-53-1 on next token <"e" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 55 column 3 in basic_expr-53-1) on next token <"e" at line 1 column 1>
    Visited token (la=1): <<ID>: "e" at line 1 column 1>; Expected token: <<ID>>
    Call: 4: LookAhead(1) STARTED (at line 69 column 3 in args-67-1) on next token <"e" at line 1 column 1>
      Visited token (la=0): <"." at line 1 column 2>; Expected token: <"(">
      Visited token (la=0): <"." at line 1 column 2>; Expected token: <"(">
    Return: 4: LookAhead(0) FAILED (at line 69 column 3 in args-67-1) on consumed token <"null" at line 0 column 0>
  Return: 2: Exiting FAILED LOOKAHEAD(2/0) (at line 55 column 3 in basic_expr-53-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <<ID>: "e" at line 1 column 1>
  Consumed token: <"." at line 1 column 2>
  Consumed token: <<ID>: "f" at line 1 column 3>
Return: 0: basic_expr-53-1 on consumed token <"f" at line 1 column 3>
```

So in this third input case:

* entering the top level production, the choice 1 & 4 lookahead (expecting `<ID> "("`) is performed: it succeeds on the first token (trace `Visited token (la=1): <<ID>: "e" at line 1 column 1>; Expected token: <<ID>>`)
* then javacc starts looking ahead for the first choice (choice 1) to resolve the choice conflict; this choice continues with an expansion_unit which is the production `args()` (found in line 66 column 1 in the grammar file), which itself starts with the (nested) lookahead found in line 68 column 3 in the grammar file (`LOOKAHEAD(2)`)
* but at this point there is only 1 to be scanned token left (as shown in `Call: 4: LookAhead(1) STARTED ...`, where the parameter is a 1-based amount)
* the nested lookahead has 2 choices with the same prefix `"(")`, both fail (the 2 trace lines `Visited token (la=0): <"." at line 1 column 2>; Expected token: <"(">`), so it does not consume any token (therefore the "null" token in the next trace line)
* the top level lookahead can now only look for the remaining choice (choice 4), therefore it consumes the 3 tokens, which luckily match
 
Now on the fourth input (`ab ( EXPR )`, which matches top level choice 1 / nested choice 1, we get the following trace:

```
input 3 : ab ( EXPR )
Call: 0: basic_expr-53-1 on next token <"ab" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 55 column 3 in basic_expr-53-1) on next token <"ab" at line 1 column 1>
    Visited token (la=1): <<ID>: "ab" at line 1 column 1>; Expected token: <<ID>>
    Call: 4: LookAhead(1) STARTED (at line 69 column 3 in args-67-1) on next token <"ab" at line 1 column 1>
      Visited token (la=0): <"(" at line 1 column 4>; Expected token: <"(">
    Return: 4: LOOKAHEAD SUCCESS (0) (at line 69 column 3 in args-67-1) on consumed token <"null" at line 0 column 0>
  Return: 2: Caught SUCCESSFUL LOOKAHEAD(2/0) (at line 55 column 3 in basic_expr-53-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <<ID>: "ab" at line 1 column 1>
  Call: 2: args-67-1 on next token <"(" at line 1 column 4>
    Call: 4: Entering LOOKAHEAD(2) (at line 69 column 3 in args-67-1) on next token <"(" at line 1 column 4>
      Visited token (la=1): <"(" at line 1 column 4>; Expected token: <"(">
      Visited token (la=0): <"EXPR" at line 1 column 6>; Expected token: <"EXPR">
    Return: 4: Caught SUCCESSFUL LOOKAHEAD(2/0) (at line 69 column 3 in args-67-1) on consumed token <"ab" at line 1 column 1>
    Consumed token: <"(" at line 1 column 4>
    Call: 4: expr-65-1 on next token <"EXPR" at line 1 column 6>
      Consumed token: <"EXPR" at line 1 column 6>
    Return: 4: expr-65-1 on consumed token <"EXPR" at line 1 column 6>
    Consumed token: <")" at line 1 column 11>
  Return: 2: args-67-1 on consumed token <")" at line 1 column 11>
Return: 0: basic_expr-53-1 on consumed token <")" at line 1 column 11>
```

So in this fourth input case:

* entering the top level production, the choice 1 & 4 lookahead (expecting `<ID> "("`) is performed: it succeeds on the first token (trace `Visited token (la=1): <<ID>: "e" at line 1 column 1>; Expected token: <<ID>>`)
* then javacc starts looking ahead for the first choice (choice 1) to resolve the choice conflict; this choice continues with an expansion_unit which is the production `args()` (found in line 66 column 1 in the grammar file), which itself starts with the (nested) lookahead found in line 68 column 3 in the grammar file (`LOOKAHEAD(2)`)
* but at this point there is only 1 token left (therefore the `LookAhead(1)`)
* up to that point the behavior is the same as in the previous input case
* the nested lookahead has 2 choices with the same prefix `"(")`, and the first matches  (line `Visited token (la=0): <"(" at line 1 column 4>; Expected token: <"(">`), so the level 4 and 2 calls return with success, and choice 1 is selected, token `"ab"` is consumed as an `<ID>`
* then javacc tries to match the production `args()`, and its `LOOKAHEAD(2)` is performed (call 4 / return 4) on its first choice `"(" expr() ")"` (which javacc had transformed in `"(" "EXPR" ")"`) and the 2 scanned tokens match the expected ones
* so they are consumed, and the last token luckily matches and is consumed

Now on the fifth input (`ba ( )`, which matches top level choice 1 / nested choice 2, we get the following trace:

```
input 4 : ba ( )
Call: 0: basic_expr-53-1 on next token <"ba" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 55 column 3 in basic_expr-53-1) on next token <"ba" at line 1 column 1>
    Visited token (la=1): <<ID>: "ba" at line 1 column 1>; Expected token: <<ID>>
    Call: 4: LookAhead(1) STARTED (at line 69 column 3 in args-67-1) on next token <"ba" at line 1 column 1>
      Visited token (la=0): <"(" at line 1 column 4>; Expected token: <"(">
    Return: 4: LOOKAHEAD SUCCESS (0) (at line 69 column 3 in args-67-1) on consumed token <"null" at line 0 column 0>
  Return: 2: Caught SUCCESSFUL LOOKAHEAD(2/0) (at line 55 column 3 in basic_expr-53-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <<ID>: "ba" at line 1 column 1>
  Call: 2: args-67-1 on next token <"(" at line 1 column 4>
    Call: 4: Entering LOOKAHEAD(2) (at line 69 column 3 in args-67-1) on next token <"(" at line 1 column 4>
      Visited token (la=1): <"(" at line 1 column 4>; Expected token: <"(">
      Visited token (la=0): <")" at line 1 column 6>; Expected token: <"EXPR">
    Return: 4: Exiting FAILED LOOKAHEAD(2/0) (at line 69 column 3 in args-67-1) on consumed token <"ba" at line 1 column 1>
    Consumed token: <"(" at line 1 column 4>
    Consumed token: <")" at line 1 column 6>
  Return: 2: args-67-1 on consumed token <")" at line 1 column 6>
Return: 0: basic_expr-53-1 on consumed token <")" at line 1 column 6>
```

So in this fifth input case:

* we have at the beginning the same behavior as in the previous case
* in the nested lookahead, choice 1 fails, so choice 2 is the single remaining possibility, so javacc consumes the 2 last tokens, which luckily match


Now on the sixth input (`new .cd`), which does not match any choice, we get the following trace:

```
input 5 : new .cd
Call: 0: basic_expr-54-1 on next token <"new" at line 1 column 1>
  Call: 2: Entering LOOKAHEAD(2) (at line 56 column 3 in basic_expr-54-1) on next token <"new" at line 1 column 1>
    Visited token (la=1): <"new" at line 1 column 1>; Expected token: <<ID>>
  Return: 2: Exiting FAILED LOOKAHEAD(2/1) (at line 56 column 3 in basic_expr-54-1) on consumed token <"null" at line 0 column 0>
  Consumed token: <"new" at line 1 column 1>
  UNEXPECTED consumed token: Expected token: <<ID>>); Consumed token: <"." at line 1 column 5> in basic_expr-54-1
Return: 0: basic_expr-54-1 on consumed token <"new" at line 1 column 1>
swallowed ParseException: Encountered " "." ". "" at line 1, column 5.
Was expecting:
    <ID> (in basic_expr-54-1) ...
 ```

So in this sixth input case we have other lines:

* one with information on the expected consumed token and the really not-matching consumed token `UNEXPECTED consumed token ...`
* the ParseException message, with all the expected tokens (with the productions where they were expected), and the error tokens (*to be clarified*) (marked with `(in ?) ...`


<!---
## `JAVACODE` Productions

*To be done.*
-->
<br>

---

[Top](#contents)

[Token Manager](token-manager.md) &hellip; [Lookahead](lookahead.md) &hellip; [CharStream](charstream.md) &hellip; [Error Handling](error-handling.md) &hellip; [Lexer Tips](lexer-tips.md) &hellip; [Examples](examples.md)

<br>
