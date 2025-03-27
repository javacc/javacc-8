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

[Home](../index.md) > [Tutorials](index.md) > Lexer Tips

--------------------------------------------------------------------------------

There are many ways to write the lexical specification for a grammar, but the performance of the generated token manager can vary significantly depending on how you do this.

This section presents a few tips for writing good lexical specifications.

### Contents

- [String Literals](#string-literals)
    * [Use string literals as much as possible](#use-string-literals-as-much-as-possible)
    * [Avoid string literals for the same token](#avoid-string-literals-for-the-same-token)
    * [Order string literals by length](#order-string-literals-by-length)
  
- [Lexical States](#lexical-states)
    * [Minimize use of lexical states](#minimize-use-of-lexical-states)
    * [Use SKIP as much as possible](#use-skip-as-much-as-possible)
    * [Avoid using SKIP with lexical actions and state changes](#avoid-using-skip-with-lexical-actions-and-state-changes)
    * [Avoid using MORE if possible](#avoid-using-more-if-possible)
  
- [Other](#other)
    * [Use ~[] by itself](#use-by-itself)
    * [Avoid using IGNORE_CASE selectively](#avoid-using-ignore_case-selectively)


## String Literals

### Use string literals as much as possible

Try to specify as many string literals as possible.

These are recognized by a Deterministic Finite Automata (DFA), which is much faster than the Non-deterministic Finite Automata (NFA) needed to recognize other kinds of complex regular expressions.

For example, to skip blanks / tabs / new lines:

```java
SKIP : { " " | "\t" | "\n" }
```

is more efficient than doing:

```java
SKIP : { < ([" ", "\t", "\n"])+ > }
```

Because in the first case you only have `String` literals, it will generate a DFA whereas for the second case it will generate an NFA.

### Avoid string literals for the same token

Try to avoid having a choice of String literals for the same token.

For example:

```java
< NONE : "\"none\"" | "\'none\'" >
```

Instead, have two different token types for this and use a non-terminal which is a choice between those choices.

The above example can be written as:

```java
< NONE1 : "\"none\"" >
|
< NONE2 : "\'none\'" >
```

and define a non-terminal called `None()` as:

```java
void None() : {}
{
  <NONE1> | <NONE2>
}
```

This will make recognition much faster. Note that if the choice is between two complex regular expressions, it is OK to have the choice.

### Order string literals by length

Specify all string literals in order of increasing length, i.e. all shorter string literals before longer ones.

This will help optimizing the bit vectors needed for string literals.


## Lexical States

### Minimize use of lexical states

Try to minimize the use of lexical states.

When using them, try to move all your complex regular expressions into a single lexical state, leaving others to just recognize simple string literals.

### Use SKIP as much as possible

Try to `SKIP` as much possible if you don't care about certain patterns.

Here, you have to be a bit careful about `EOF`. Seeing an `EOF` after `SKIP` is fine whereas, seeing an `EOF` after a `MORE` is a lexical error.

### Avoid using SKIP with lexical actions and state changes

Try to avoid lexical actions and lexical state changes with `SKIP` specifications, especially for single character `SKIP`'s like ` `, `\t`, `\n` etc).

For such cases, a simple loop is generated to eat up the `SKIP`'ed single characters. So, if there is a lexical action or state change associated with this, it is not possible to it this way.

### Avoid using MORE if possible

Try to avoid specifying lexical actions with `MORE` specifications.

Generally every `MORE` should end up in a `TOKEN` (or `SPECIAL_TOKEN`) finally so you can do the action there at the `TOKEN` level, if it is possible.

## Other

### Use `~[]` by itself

Try to use the pattern `~[]` by itself as much as possible.

For example, doing

```java
MORE : { < ~[] > }
```

is better than doing
```java
TOKEN : { < (~[])+ > }
```

Of course, if your grammar dictates that one of these cannot be used, then you don't have a choice, but try to use `< ~[] >` as much as possible.

### Avoid using IGNORE_CASE selectively

There is heavy performance penalty for setting `IGNORE_CASE` for some regular expressions and not for others in the same lexical state.

Best practise is to set the `IGNORE_CASE` option at the grammar level. If that is not possible, then try to have it set for *all* regular expressions in a lexical state.

<br>

---

[Top](#contents)

[Token Manager](token-manager.md) &hellip; [Lookahead](lookahead.md) &hellip; [CharStream](charstream.md) &hellip; [Error Handling](error-handling.md) &hellip; [Lexer Tips](lexer-tips.md) &hellip; [Examples](examples.md)

<br>
