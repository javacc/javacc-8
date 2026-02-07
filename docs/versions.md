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
## Versions
-->{% endcomment %}

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

