<!--
See LICENSE.
-->

# JavaCC

Java Compiler Compiler (JavaCC) is the most popular parser generator for use with Java applications,  and it allows generating C+= and C# parsers since version 8.

A parser generator is a tool that reads a grammar specification (in plain text) and converts it to a program (of some programming language) that can recognize matches to the grammar.

In addition to the parser generator itself, JavaCC provides other standard capabilities related to parser generation such as tree building (via a tool called JJTree included with JavaCC), actions and debugging.

All you need:
- to create a JavaCC grammar specification: a text editor at minimum, or a modern IDE,
- to generate the JavaCC parser: JavaCC itself and a Java Runtime Environment (JRE),
- to compile the generated parser: a compiler for the target programming language (currently Java, C++, C#),
- to run the compiled generated parser: the standard environment for running a such compiled programe (a JRE for Java...)

This README is just an index to the different documented sections, which are also presented in the companion [JavaCC 8 web site](https://javacc.github.io/javacc-8/).

## Publishing status

#### Version 8

Currently published releases:  

- ![Maven Central Version](https://img.shields.io/maven-central/v/org.javacc.generator/java?label=Java generator)
- ![Maven Central Version](https://img.shields.io/maven-central/v/org.javacc.generator/cpp?label=C%2B%2B generator)
- ![Maven Central Version](https://img.shields.io/maven-central/v/org.javacc.generator/csharp?label=C%23 generator)
- ![Maven Central Version](https://img.shields.io/maven-central/v/org.javacc/core?label=Core)
- ![Maven Central Version](https://img.shields.io/maven-central/v/org/javacc?label=parent)
- ![GitHub Release](https://img.shields.io/github/v/release/javacc/javacc-8?label=GitHub%20v8)  

Next release to publish: 8.1.0 (Feb 2026) (Maven, GitHub)  

Currently published snapshots:  

- ![Maven Central Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjavacc%2Fgenerator%2Fjava%2Fmaven-metadata.xml&label=Java)

- ![Maven Central Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjavacc%2Fgenerator%2Fcpp%2Fmaven-metadata.xml&label=C%2B%2B&strategy=latestProperty)

- ![Maven Central Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjavacc%2Fgenerator%2Fcsharp%2Fmaven-metadata.xml&label=C%23&strategy=latestProperty)

- ![Maven Central Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjavacc%2Fcore%2Fmaven-metadata.xml&label=Core)

- ![Maven Central Version](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjavacc%2Fmaven-metadata.xml&label=parent)

#### Version 7

Currently published release:  

![Maven Central Version](https://img.shields.io/maven-central/v/net.java.dev.javacc/javacc?label=Maven%20v7) ![GitHub Release](https://img.shields.io/github/v/release/javacc/javacc?label=GitHub%20v7)  

## What is JavaCC

See [What is JavaCC](docs/what-is-javacc.md).  

## Starting using JavaCC

Beginners can look at [Starting using JavaCC](docs/starting-using-javacc.md).  

## Versions

If you read this README.md, you should be under the **v8** repository.

See [Versions](docs/versions.md) for an explanation of differences between v8 and v7.  

## Release notes

See [Release notes](docs/release-notes.md).  

## Building JavaCC

See [Building JavaCC](docs/README_BUILD.md) if you want to hack or contribute to JavaCC.  

## Support

See [Support](docs/support.md) before submitting an issue.  

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
