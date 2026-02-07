{% comment %}<!--
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
<!--
## Starting using JavaCC
-->{% endcomment %}

### Contents

- [Use JavaCC within an IDE](#use-javacc-within-an-ide)
    * [IntelliJ IDEA](#intellij-idea)
    * [Eclipse IDE](#eclipse-ide)
    * [Maven](#maven)
        + [Maven with version 8](#maven-with-version-8)
        + [Maven with version 7](#maven-with-version-7)
    * [Gradle](#gradle)
        + [Gradle with version 8](#gradle-with-version-8)
        + [Gradle with version 7](#gradle-with-version-7)
- [Use JavaCC from the command line](#use-javacc-from-the-command-line)
    * [Download](#download)
        + [Download version 8](#download-version-8)
        + [Download version 7](#download-version-7)
    * [Install](#install)
        + [Install version 8](#install-version-8)
        + [Install version 7](#install-version-7)
- [Quick migration guide from v7 to v8](#quick-migration-guide-from-v7-to-v8)
- [Customizing your generated code](#customizing-your-generated-code)
- [Rebuilding JavaCC](#rebuilding-javacc)

You can use JavaCC either from the command line or through an IDE.

### Use JavaCC within an IDE

Minimal requirements for an IDE are:
* Support for Java 1.8+, and if needed for the other target programming language (C++ or C#)

Depending on the build environment of the user choice: support for Ant or Maven or Gradle with Java

#### IntelliJ IDEA

The IntelliJ IDE supports Maven & Gradle out of the box and offers a plugin for JavaCC development.

* IntelliJ download: [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/)
* IntelliJ JavaCC Plugin: [https://plugins.jetbrains.com/plugin/11431-javacc/](https://plugins.jetbrains.com/plugin/11431-javacc/)

<!---
Check out our [Setting up IntelliJ](https://ci.apache.org/projects/flink/flink-docs-master/flinkDev/ide_setup.html#intellij-idea) guide for details.
-->

#### Eclipse IDE

The Eclipse IDE supports Maven & Gradle out of the box and offers a plugin for JavaCC development.

* Eclipse download: [https://www.eclipse.org/ide/](https://www.eclipse.org/ide/)
* Eclipse JavaCC Plugin: [https://marketplace.eclipse.org/content/javacc-eclipse-plug](https://marketplace.eclipse.org/content/javacc-eclipse-plug)

#### Maven

Add the following specialized plugin to your `pom.xml` file, under the build / reporting plugins, or under one or more profiles.  

(Note that you can use the well known Apache Exec Plugin to run JavaCC, with appropriate jars in the classpath, the main entry point and standard arguments passing - but you will not benefit from the specialized plugin features.)

##### Maven with version 8

(For version 8 you must use the javacc-maven-plugin from the JavaCC organization, not from others. Note that version 8.1.0 benefits from a bunch of improvements over previous versions and the configuration is a little different.)  

Adapt the versions (in the examples below it is set to `3.8.0` and`8.1.0`), the execution(s) (goals `javacc` and/or `jjtree-javacc`) and the `codeGenerator` setting for the generator (`java`, `cpp`, `csharp`).  
Also add the configuration settings for those you want to use a non default value.  

See the [JavaCC Maven Plugin web site](https://javacc.github.io/javacc-maven-plugin/) (in the Apache Maven format) or the [JavaCC Maven Plugin GitHub repository](https://github.com/javacc/javacc-maven-plugin/) for full explanations of parameters, inheritance, intermediate directories, debug...  

```
          <plugin>
              <groupId>org.javacc.plugin</groupId>
              <artifactId>javacc-maven-plugin</artifactId>
              <version>3.8.0</version>
              <executions>
                  <!-- as many executions as different sets of grammars similar configurations -->
                  <!-- here we use a non standard layout: 
                        ("my cc" and "gen-src" instead of "src" and "generated-sources") -->
                  <execution>
                      <id>jjtree-javacc</id>
                      <phase>generate-sources</phase>
                      <goals>
                          <goal>jjtree-javacc</goal>
                      </goals>
                      <configuration>
                        <includes>
                          <include>...</include> <!-- default is *.jjt for JJTree -->
                        </includes>
                        <excludes>
                          <exclude>...</exclude> <!-- default is nothing -->
                        </excludes>
                        <keepIntermediateDirectory>false</keepIntermediateDirectory> <!-- the default -->
                        <skip>false</skip> <!-- the default -->
                        <sourceDirectory>my cc</sourceDirectory>
                        <jjtreeCmdLineArgs>
                          <arg>-JJTREE_OUTPUT_DIRECTORY="${project.build.directory}/gen-src/jjtree"</jjtod>
                          <arg>-CODE_GENERATOR="Java"</arg>
                          <arg>-MULTI=true</arg>
                        </jjtreeCmdLineArgs>
                        <javaccCmdLineArgs>
                          <arg>-OUTPUT_DIRECTORY="${project.build.directory}/gen-src/javacc"</jjod>
                          <arg>-CODE_GENERATOR="Java"</arg>
                          <arg>-STATIC:false</arg>
                        </javaccCmdLineArgs>
                      </configuration>
                  </execution>
              </executions>
              <dependencies>
                  <!-- declare all used generators artifacts;  the core may be omited,
                        as it is a dependency of the generators -->
                  <!--            <dependency>-->
                  <!--              <groupId>org.javacc</groupId>-->
                  <!--              <artifactId>core</artifactId>-->
                  <!--              <version>8.1.0</version>-->
                  <!--              <scope>runtime</scope>-->
                  <!--            </dependency>-->
                  <dependency>
                      <groupId>org.javacc.generator</groupId>
                      <artifactId>java</artifactId>
                      <version>8.1.0</version>
                      <scope>runtime</scope>
                  </dependency>
                </dependencies>
            </plugin>
```

##### Maven with version 7

(You can use the [javacc-maven-plugin](https://www.mojohaus.org/javacc-maven-plugin/) from MojoHaus or the [javacc-maven-plugin](https://github.com/javacc/javacc-maven-plugin) from the JavaCC organization.)  

Same as above, with a single different dependency, and without the `codeGenerator` setting, and with named parameters in the configuration (like `<static>true</static>`).

```
<dependency>
    <groupId>net.java.dev.javacc</groupId>
    <artifactId>javacc</artifactId>
    <version>7.0.13</version>
</dependency>
```

#### Gradle

##### Gradle with version 8

*Courtesy of [JSqlParser](https://github.com/JSQLParser)*  

The following entries will provide you with the tasks `javacc:compileJavacc`, `javacc:compileJjtree` and `javacc:jjdoc` which will be executed automatically before `compileJava`:

```gradle
plugins {
   id "org.javacc.javacc" version "latest.release"
}
repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()

    // Sonatype OSSRH Snapshots
    maven {
        url = uri('https://s01.oss.sonatype.org/content/repositories/snapshots/')
    }
}
dependencies {
    javacc 'org.javacc:core:8.0.1'
    javacc 'org.javacc.generator:java:8.0.1'
}
```

##### Gradle with version 7

Add the following to your `build.gradle` file.

```
repositories {
    mavenLocal()
    maven {
        url = 'https://mvnrepository.com/artifact/net.java.dev.javacc/javacc'
    }
}
dependencies {
    compile group: 'net.java.dev.javacc', name: 'javacc', version: '7.0.13'
}
```

### Use JavaCC from the command line

#### Download

Download the latest stable release (at least the binaries and the sources) in a so called download directory:

##### Download version 8

Download the core and the generator(s) you are going to use:

* JavaCC Core 8.0.1 - [Binaries](https://repo1.maven.org/maven2/org/javacc/core/8.0.1/core-8.0.1.jar), [Source (zip)](https://github.com/javacc/javacc-8-core/archive/core-8.0.1.zip), [Source (tar.gz)](https://github.com/javacc/javacc-8-core/archive/core-8.0.1.tar.gz), [Javadocs](https://repo1.maven.org/maven2/org/javacc/core/8.0.1/core-8.0.1-javadoc.jar)

* JavaCC C++ 8.0.1 - [Binaries](https://repo1.maven.org/maven2/org/javacc/generator/cpp/8.0.1/cpp-8.0.1.jar), [Source (zip)](https://github.com/javacc/javacc-8-cpp/archive/cpp-8.0.1.zip), [Source (tar.gz)](https://github.com/javacc/javacc-8-cpp/archive/cpp-8.0.1.tar.gz), [Javadocs](https://repo1.maven.org/maven2/org/javacc/generator/cpp/8.0.1/cpp-8.0.1-javadoc.jar)

* JavaCC C# 8.0.1 - [Binaries](https://repo1.maven.org/maven2/org/javacc/generator/csharp/8.0.1/csharp-8.0.1.jar), [Source (zip)](https://github.com/javacc/javacc-8-csharp/archive/csharp-8.0.1.zip), [Source (tar.gz)](https://github.com/javacc/javacc-8-csharp/archive/csharp-8.0.1.tar.gz), [Javadocs](https://repo1.maven.org/maven2/org/javacc/generator/csharp/8.0.1/csharp-8.0.1-javadoc.jar)

* JavaCC Java 8.0.1 - [Binaries](https://repo1.maven.org/maven2/org/javacc/generator/java/8.0.1/java-8.0.1.jar), [Source (zip)](https://github.com/javacc/javacc-8-java/archive/java-8.0.1.zip), [Source (tar.gz)](https://github.com/javacc/javacc-8-java/archive/java-8.0.1.tar.gz), [Javadocs](https://repo1.maven.org/maven2/org/javacc/generator/java/8.0.1/java-8.0.1-javadoc.jar)

All JavaCC v8 *releases* are available via [GitHub](https://github.com/javacc/javacc-8/releases) and [Maven](https://mvnrepository.com/artifact/org/javacc) including checksums and cryptographic signatures.

##### Download version 7

* JavaCC 7.0.13 - [Binaries](https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.13/javacc-7.0.13.jar), [Source (zip)](https://github.com/javacc/javacc/archive/javacc-7.0.13.zip), [Source (tar.gz)](https://github.com/javacc/javacc/archive/javacc-7.0.13.tar.gz), [Javadocs](https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.13/javacc-7.0.13-javadoc.jar), [Release Notes](docs/release-notes.md)

All JavaCC v7 releases are available via [GitHub](https://github.com/javacc/javacc/releases) and [Maven](https://mvnrepository.com/artifact/net.java.dev.javacc/javacc) including checksums and cryptographic signatures.

For all previous releases, please see [stable releases](docs/downloads.md).

#### Install

##### Install version 8

*TODO to be written*. Help welcomed!

##### Install version 7

Once you have downloaded the files, navigate to the download directory and unzip the sources file(s), this creating a so called JavaCC installation directory:

`$ unzip javacc-7.0.13.zip`  
or  
`$ tar xvf javacc-7.0.13.tar.gz`

Then create a new `target` directory under the installation directory, and copy or move the binary file `javacc-7.0.13.jar` under this `target` directory, and copy or rename it to `javacc.jar`.

Then add the `scripts/` directory under the JavaCC installation directory to your `PATH`. The JavaCC, JJTree, and JJDoc invocation scripts/executables reside in this directory.

On UNIX based systems, the scripts may not be executable immediately. This can be solved by using the command from the `javacc-7.0.13/` directory:
`chmod +x scripts/javacc`

#### Write your grammar and generate your parser

You can then create and edit a grammar file with your favorite text editor.

Then use the appropriate script for generating your parser from your grammar.

### Quick migration guide from v7 to v8

- If you used JJTree, you have to replace all occurrences of the v7 class `SimpleNode` with the v8 class `Node` (and if you used the v7 interface `Node` you have to replace it with the v8 interface `Tree`).  

- If you used JJTree and Java and you need to generate *public* nodes, you have to use the new JJTree option `SINGLE_TREE_FILE` set to `false` (this will make JJTree generate nodes classes in their own java source files; the default value is `true` which makes JJTree generate node files in a single `<parser>Tree.java` source file, in which classes cannot be public).  

- If you customized a generated class, it should be wise (or even necessary) that you rebuild the standard generated class and re-customize it, as the full compatibility is not guaranteed: an example with `SimpleCharStream`:  

    * build your grammar with option `USER_CHAR_STREAM = false`;, it will generate a fresh `SimpleCharStream.java` in the generated sources
    * move this generated `SimpleCharStream.java` into your sources, delete the one in the generated sources folder and activate or delete the `USER_CHAR_STREAM` option in your grammar
    * port the customizations to the `SimpleCharStream.java` of your sources

- You may also get some javac compiler errors in the generated parser coming from your user actions (e.g. duplicated variables in switch statements as the generated code has less blocks, uninitialized variables...), but these should be easily fixed.  

### Customizing your generated code

You can customize the generated code in some areas, those that are driven by template files: in the generators there are `src/main/resources/templates` folders that contain different `.template` files (you can get them by downloading them from the GitHub repository).  

Of course you need to understand what you should not alter (signatures of methods called by other generated methods...) and what you can change (output messages...) in the template files.  

Then in order to use your modified template file, you have to integrate it in the classpath in the step(s) you use for generating the parser:  
- on the **command line**, you just prepend the file to the classpath (`java -cp <path-to-custom-template>;... ...`)
- under **ant**, you just prepend the file to the classpath (`<java classpath="<path-to-custom-template>;..." ...>`)
- under **Maven**, you have to package your custom template in a jar, install it (in the local repository), and add this artifact in the dependency list of the plugin: see example [CustomTemplate](https://github.com/javacc/javacc-8-java/examples/CustomTemplate)
- under **Gradle**: *to be completed*

### Rebuilding JavaCC 

For more details and information, see [README_BUILD](README_BUILD.html#build).  

