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

# JavaCC

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org/javacc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org/javacc)
[![Javadocs](https://www.javadoc.io/badge/org/javacc.svg)](https://www.javadoc.io/doc/org/javacc)

Java Compiler Compiler (JavaCC) is the most popular parser generator for use with Java applications,  and it allows generating C+= and C# parsers since version 8..

A parser generator is a tool that reads a grammar specification (in plain text) and converts it to a program (of some programming language) that can recognize matches to the grammar.

In addition to the parser generator itself, JavaCC provides other standard capabilities related to parser generation such as tree building (via a tool called JJTree included with JavaCC), actions and debugging.

All you need:
- to create a JavaCC grammar specification: a text editor at minimum, or a modern IDE,
- to generate the JavaCC parser: JavaCC itself and a Java Runtime Environment (JRE),
- to compile the generated parser: a compiler for the target programming language (currently Java, C++, C#),
- to run the compiled generated parser: the standard environment for running a such compiled programe (a JRE for Java...)

This README is just an index to the different documented sections, which are also presented in the companion [JavaCC 8 web site](https://javacc.github.io/javacc-8/).

## What is JavaCC

See [What is JavaCC](docs/what-is-javacc.md).  

## Starting using JavaCC

See [Starting using JavaCC](docs/starting-using-javacc.md).  

## Versions

If you read this README.md, you should be under the **v8** repository.

See [Versions](docs/versions.md) for an explanation of differences between v8 and v7.  

## Release notes

See [Release notes](docs/release-notes.md).  

## Building JavaCC

See [Building JavaCC](docs/README_BUILD.md) if you want to hack or contribute to JavaCC.  

## Support

See [Support](docs/support.md).  

## Contributing

This is an active open-source project. We are always open to people who want to use the system or contribute to it.  
Contact us through the [repository discussions](https://github.com/javacc/javacc-8/discussions) area if you are looking for implementation tasks that fit your skills.

## License

JavaCC is an open source project released under the [BSD-3-Clause](LICENSE).  
The JavaCC project was originally developed at Sun Microsystems Inc. by [Sreeni Viswanadha](https://github.com/kaikalur) and [Sriram Sankar](https://twitter.com/sankarsearch).  

<br>

---

[Top](#javacc)

<br>
