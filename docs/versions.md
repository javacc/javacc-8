<!--
See LICENSE.
-->
<!--
## Versions
-->

The RECOMMENDED version is version **8.1.0**.  
It separates the parser (the core) from the generators (for the different languages); development and maintenance effort will be mainly on this version.  
This version lies on different Git repositories / Java & Maven projects / jars:
- the base [javacc-8](https://github.com/javacc/javacc-8)
- the [core](https://github.com/javacc/javacc-8-core)
- the generators:
    * [Java](https://github.com/javacc/javacc-8-java)
    * [C++](https://github.com/javacc/javacc-8-cpp)
    * [C#](https://github.com/javacc/javacc-8-csharp)

After cloning, the expected folder structure would look like:  

```bash
ls -d *
javacc-8/  javacc-8-core/  javacc-8-java/ javacc-8-cpp/ javacc-8-csharp/
```

The previous versions (4, 5, 6, 7) are widely spread; effort to migrate to version 8 should be minimum.  
Their last versions lie on a single Git repository / Java & Maven project / jar:
- [javacc](https://github.com/javacc/javacc)

Differences between v8 versus v7: very small at the grammar level, more important at the generated sources level:
- the JavaCC/JJTree grammar part is the same
- most of JavaCC/JJTree options should be the same, but some may be removed and others appear in v8
- the Java grammar part should be nearly the same (may be some Java 7 & Java 8 features will appear in v8 and not in v7); in the future Java 11..17..21.. features would appear only in v8)
- the C++ / C# grammar parts may be somewhat different
- some generated files are not much different, others are (the token manager is token driven)

