<!--
See LICENSE.
-->

# Building JavaCC v8

JavaCC's version 8 is targeted at:
- providing a better separation between the parsing part and the code generation part
- giving the end user more possibilities to customize his generated code
- improving the build and deployment process
- improving the quality of the product itself and of the generated parsers, by adding more CI tests with more coverage

The current owner of JavaCC and the current maintainers want <ins>the user base to migrate from v7 to v8</ins> and try to provide an easy way to do it, trying to avoid regressions.  

<ins>So while they will still study issues raised on v7, they will most of the time provide fixes only in v8.</ins>  

<ins>Some enhancements are already in v8, future enhancements will be provided only in v8</ins>, and only after the achievement of the above base improvements.  

## Contents

- [Projects layout](#projects-layout)
    * [Parent javacc/javacc-8](#parent-javacc-javacc-8)
    * [Core javacc/javacc-8-core](#core-javacc-javacc-8-core)
    * [Generators javacc/javacc-8-java, javacc/javacc-8-cpp, javacc/javacc-8-csharp](#generators-javacc-javacc-8-java-javacc-javacc-8-cpp-javacc-javacc-8-csharp)
  
- [JavaCC Maven plugins](#javacc-maven-plugins)
    * [MojoHaus javacc-maven-plugin](#mojohaus-javacc-maven-plugin)
    * [JavaCC javacc-maven-plugin](#javacc-javacc-maven-plugin)
  
- [Local build process](#local-build-process)
    * [Build](#build)
    * [Quick examples](#quick-examples)
    * [Artifacts versions](#artifacts-versions)
  
- [Projects at GitHub](#projects-at-github)
    * [Branches](#branches)
    * [Making commits and PRs](#making-commits-and-prs)
    * [Actions](#actions)
  
- [Sonatype release process](#sonatype-release-process)
  
## Projects layout

The choice has been made in the past to split the (v7) single Git repo / Java & Maven project into different new Git repositories / Java & Maven projects; we live with this.

The layout is based on a classical Maven layout with a parent project and modules; each project / module is in its own Git repo under the GitHub **javacc** organization. However these projects must be all at the same directory level (the modules are not nested inside the parent).

### Parent javacc/javacc-8

This is the parent project; Maven only project (no code to build / test).  
Holds global documentation (in `/docs`: javacc documentation, tutorials, faq, release-notes) and readmes (end user and technical user).

Its `/pom.xml` defines:
- the common general definitions
- the common dependencies
- the common plugins (those used by more than one module), their versions, and their common configuration
- the common profiles (`with-toolchains`, `clean-build`, `release`)

In particular all common / standard phases of the build lifecycle are defined here, and they do not appear in the modules poms, which however do run them.

### Core javacc/javacc-8-core

This module in charge of parsing the grammars and building context data to be used by the generators.  
Module with Java classes (in `/src`); holds the grammars, the parsing and context building classes, some JUnit tests, but no integration tests.

The **Core** must be built by an older version of JavaCC (v4...v7 or v8) called the bootstrap.  
There are no direct integration tests, those must be done through the 3 generators.  

Its `/pom.xml` defines:
- the specific configuration of the JavaCC Maven plugin to build the **Core** itself, mainly the bootstrap version of JavaCC to use

### Generators javacc/javacc-8-java, javacc/javacc-8-cpp, javacc/javacc-8-csharp

These modules are in charge of generating the Java / C++ / C# code using the context data produced by the **Core**.  
Modules with Java classes (in `/src`) for the generators, and grammars and Java / C++ / C# code for side grammars, which are some examples, some shared grammars and some tests.  

Their `/pom.xml` define:
- the specific profile `run-its` that allows to run the integration tests through the **Maven invoker plugin**

For the side grammars / integration tests, these modules have:
- an `/it` folder, which holds common stuff for the 3 following types of integration tests
- an `/issues` folder, which holds folders for sandbox tests and issues tests
- an `/examples` folder, which holds folders for the legacy examples, which also serve as integration tests
- an `/grammars` folder, which holds folders for the shared grammars, which also serve as integration tests

The `/it/pom.xml` define:
- the parent project (of all integration tests), which is the global parent project (`javacc/java-8`), not the containing generator project (as one would usually imagine)
- the specific configuration for the **JavaCC Maven plugin** to read the grammars
- the specific profiles for JavaCC or JJTree generation (`jjt8`, `jjc8`...)
- the definitions and configurations of the specific plugins for compiling the C++ code (**com.github.maven-nar.nar-maven-plugin**)
- the definitions and configurations of the specific plugins for compiling the C# code (**org.codehaus.plexus.plexus-compiler-csharp**)
- the specific profiles for C++ generation (`Windows`, `Linux`, `MacOS`, `nar-visualstudio-env`)
- the specific profiles for C# generation (`Windows`, `Linux`, `MacOS`, `csc-dir-env`)

The `pom.xml` of the 3 types of integration tests and those of their sub-folders define:
- either the (sub)modules if they have sub-folders
- or the executions for cleaning the result files, generating the parser, executing the parser and comparing the result with the ones expected

So this architecture allows:
- a developer to run individual (e.g. `/issues/bas02`), grouped (e.g. `/examples`) or full (at the top `/pom.xml`) integration tests
- a release manager to perform the full integration tests before publishing a release
- GitHub actions to perform the full integration tests on the corresponding configured projects

## JavaCC Maven plugins

A few words about the 2 main Maven plugins for JavaCC/JJTree.  
These Maven plugins are used in a Maven build in the **generate-sources** phase to generate parsers from a grammar prior to the **compile** phase.

### MojoHaus javacc-maven-plugin

In the past, up to version 7, the JavaCC Maven plugin at **MojoHaus** ([here](https://www.mojohaus.org/javacc-maven-plugin/)) was widely used.  
Its Maven reference is:

```
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>javacc-maven-plugin</artifactId>
```
This plugin is able to use a v7 release of JavaCC.

It is published in the old MVN Repository ([here](https://mvnrepository.com/artifact/org.codehaus.mojo/javacc-maven-plugin)) and in the Sonatype Maven Central Repository ([here](https://central.sonatype.com/artifact/org.codehaus.mojo/javacc-maven-plugin)).

### javacc/javacc-maven-plugin

Version 8 introduced the multi-module feature that the MojoHaus plugin is not able to handle, so a new JavaCC Maven plugin has been developed by the **javacc** organization, that can be found [here](https://github.com/javacc/javacc-maven-plugin).  
Its Maven reference is:

```
        <groupId>org.javacc.plugin</groupId>
        <artifactId>javacc-maven-plugin</artifactId>
```
Note that the **artifactId** part is the same, the **groupId** is the one different.  

This plugin is able to use a v7 or v8 release of JavaCC.

It is published in the old MVN Repository ([here](https://mvnrepository.com/artifact/org.javacc.plugin/javacc-maven-plugin)) and in the Sonatype Maven Central Repository ([here](https://central.sonatype.com/artifact/org.javacc.plugin/javacc-maven-plugin)).

## Local build process

### Build

*For the impatient user (the one who thinks every thing should work immediately):*

Clone the Git repository(ies) you need (all at the same directory level). Note that normally you should not need to hack the parent and the core projects, only the generators projects. Then run maven clean install.  

```bash
mkdir javacc-8
cd javacc-8

git clone git@github.com:javacc/javacc-8.git
cd javacc-8
# temporary workaround for bootstrapping, depending on pre-installed JavaCC 8.1.0 binaries
sed -i 's|<javacc.java.version>[^<]*</javacc.java.version>|<javacc.java.version>8.1.0</javacc.java.version>|' pom.xml
mvn clean install
cd ..


git clone git@github.com:javacc/javacc-8-core.git
cd javacc-8-core
# disable integration tests via `-P!run-its`
mvn clean install -P!run-its
cd ..

git clone git@github.com:javacc/javacc-8-java.git
cd javacc-8-java
# disable integration tests via `-P!run-its`
mvn clean install -P!run-its
cd ..
```

*For the patient user (the one who is not in a hurry and likes to check step by step that things go well):*

Clone the Git repository(ies) you need as above.  

Check that your JAVA_HOME, MAVEN_HOME and M2_HOME are compatible with your IDE settings (JavaCC requires a 1.8+ javac but some IDEs installations require a 11+ or 17+ JDK).  
You may want to configure a [toolchains](https://maven.apache.org/plugins/maven-toolchains-plugin/index.html) (and in that case the `with-toolchains` profile will be implicitly activated).  
You can generate it, with:  
`mvn toolchains:generate-jdk-toolchains-xml`  
You may check your configuration with:  
`mvn toolchains:display-discovered-jdk-toolchains`  


For the C++ / C# generators, you need to install a compiler/linker that the **Nar Maven plugin** / **Csharp Plexus compiler plugin** can find: see in the respective `it/pom.xml` the profiles `nar-visualstudio-env` / `csc-dir-env` and the environment variables you may have to set if you chose **Microsoft Visual Studio**.  

Under each cloned repository:  
- you may check that you get a valid effective pom, with:  
`mvn help:effective-pom`  
- you may take some time to resolve dependencies, even previous corrupted ones, with:  
`mvn dependency:resolve -U`  
- you may check you have correctly set your environment variables so that the right profiles are active, with:  
`mvn help:active-profiles`  

These steps should give you confidence that your Java / Maven / IDE / JavaCC build Maven plugins versions are compatible and that your environment is ok.

Then you can build a jar (without installing it and without integration tests):  
`mvn clean package`  

Then you can install the project/module (under your local Maven repository and with performing the integration tests):  
`mvn clean install`  

If some integration test fail, the module will not be installed; but you can just install the project/module without performing the integration tests by deactivating the specific profile:  
`mvn clean install -P!run-its`  

Note that running the well known `mvn clean install -DskipTests` will skip the JUnit tests (handled by the maven-surefire-plugin), not the integration tests (handled by the maven-invoker-plugin).  

*Modules in javacc-8/pom.xml*

### Quick examples

#### Just the java module

```
cd <somedir>
git clone git@github.com:javacc/javacc-8-java.git
cd javacc-8-java
```

Note that at this point the **pom.xml** should reference a parent SNAPSHOT version that is not installed in the local repository not published on a Sonatype repository, so you have to manually explicitly set the java generator version and change the parent version to the latest published RELEASE version:  

```
  <parent>
    <groupId>org</groupId>
    <artifactId>javacc</artifactId>
    <version>8.1.1-SNAPSHOT</version>
  </parent>

  <groupId>org.javacc.generator</groupId>
  <artifactId>java</artifactId>
  <!--  <version>8.1.1-SNAPSHOT</version>-->
  <packaging>jar</packaging>
```
should be changed to:  

```
  <parent>
    <groupId>org</groupId>
    <artifactId>javacc</artifactId>
    <!--  <version>8.1.1-SNAPSHOT</version>-->
    <version>8.1.0</version>
  </parent>

  <groupId>org.javacc.generator</groupId>
  <artifactId>java</artifactId>
  <version>8.1.1-SNAPSHOT</version>
  <packaging>jar</packaging>
```

Then:  

```
mvn clean install -P!run-its
```

#### Parent + core + java modules

```
cd <somedir>
git clone git@github.com:javacc/javacc-8.git
cd javacc-8
mvn clean install -P!run-its
```

Note that at this point the **pom.xml** should have detected that the latest java generator version jar (normally a SNAPSHOT) is not installed yet in the local repository and will use a previous version published on Sonatype. If this does not work well, you have to make a temporary modification of the **javacc.java.version** property at the beginning of the pom.  

```
    <javacc.java.version>8.1.1-SNAPSHOT</javacc.java.version>
```

should be changed to:  

```
    <javacc.java.version>8.1.0</javacc.java.version>
```

Then:  

```
cd ..
git clone git@github.com:javacc/javacc-8-core.git
cd javacc-8-core
mvn clean install -P!run-its
cd ..

git clone git@github.com:javacc/javacc-8-java.git
cd javacc-8-java
mvn clean install -P!run-its
cd ..
```

### Artifacts versions

#### Projects artifacts versions

The owner and the maintainers will be in charge of setting the projects artifacts versions; in general:  
- a change in a generator project will change its version and the version of the parent but not the version of the core
- a change in the core and the parent projects will change all projects versions

Locally, a user is free to manage / change the projects versions; but if he wants to submit a PR related to the source code, he must keep the ones of the current HEADs at GitHub; if he needs to change the build configuration he may be lead to change the project version.  

*To discuss the "releases / snapshots" management.*  

#### Integration tests artifacts versions

The integration tests artifacts do not have the same versions as the projects: their quite independent, and should not evolve much (at least when a significant coverage ratio will be reached).  

In fact, the integration tests artifacts should / will (?) not be published on the central repositories (as no project other than JavaCC itself should depend on them. So it is very probable that their versions will never evolve.

So locally a user should not need to change their versions. Changes will overwrite previous versions.

## Projects at GitHub

### Branches

It is worth noting that the *default* or *base* branches of the above projects at GitHub are named **release** and not **master** as usual (meaning that these branches are intended to be released in the nature by publishing them to the Maven Central Repositories.  

### Making commits and PRs

Commits and PRs must be signed. This rule is enforced by GitHub.  

### Actions

GitHub actions are configured (through `/.github/workflows/maven.yml` on *push* and *pull_requests* to run on the 3 platforms *windows*, *ubuntu* and *macos* a batched Maven install of the project and its ancestors (the highest in the hierarchy first).  
You can check their execution in the Actions tab.  
Note that the 3 jobs (one for each platform) are spawned in parallel, and as soon as one fails the others are interrupted and killed.  

## Contributing

*TODO to be written*  
Code formatting. Licenses.  

## Sonatype release process

*TODO to be written*  

JDK 8 / 11... ?
Modules JDK 9+ ?

