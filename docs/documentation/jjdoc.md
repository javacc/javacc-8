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

[Home](../index.md) > [Documentation](index.md) > JJDoc

---

JJDoc takes a JavaCC / JJTree parser specification and produces documentation for the `BNF` grammar.

### Command line options

JJDoc can operate in several modes, determined by command line options.

| Option | Default | Description |
| :--- | :--- | :--- |
| `BNF` | `false` | Setting BNF to true causes JJDoc to generate a pure BNF document, with the extension `.bnf`.|
| `CSS` | `<css file>` | This option allows you to specify a `CSS` file name. If you supply a file name in this option it will appear in a `LINK` element in the `HEAD` section of the file. This option only applies to HTML output.|
| `JCC` | `false` | Setting `JCC` to true causes JJDoc to generate a plain text format description of the `BNF` in the JavaCC syntax format, with the extension `.bnf`.|
| `ONE_TABLE` | `true` | The default value of ONE_TABLE is used to generate a single HTML table for the BNF. Setting it to false will produce one table for every production in the grammar.|
| `OUTPUT_DIR` | `<input dir>` | The default behavior is to put the JJDoc output into a file in the same directory as the grammar file, with the same file name but with the extension set accordingly to the format options. You can supply a different director path with this option.|
| `OUTPUT_FILE` | `<input dir>` | The default behavior is to put the JJDoc output into a file in the same directory as the grammar file, with the same file name but with the extension set accordingly to the format options. You can supply a different full file path with this option.|
| `TEXT` | `false` | Setting `TEXT` to true causes JJDoc to generate a plain text format description of the `BNF`, with the extension `.txt`. Some formatting is done via tab characters, but the intention is to leave it as plain as possible.|
| `XTEXT` | `false` | Setting `XTEXT` to true causes JJDoc to generate a plain text format description of the `BNF` in the Eclipse XText format, with the extension `.xtext`.|

If none of the 4 options `BNF`, `JCC`, `TEXT` and `XTEXT` are set to `true`, JJDoc generates a BNF description embedded in an HTML 3.2 page, with the extension `.html`.

Comments in the JavaCC source that immediately precede a production are passed through to the generated documentation.

### Example

Example outputs from JJDoc for the JavaCC grammar are given as [text](javacc.txt) and [HTML](javacc.html).

<br>

---

[TOP](#command-line-options)

[JavaCC Command Line](cli.md) &hellip; [JavaCC Grammar](grammar.md) &hellip; [JavaCC BNF](bnf.md) &hellip; [JavaCC API](api.md) &hellip; [JJTree](jjtree.md) &hellip; [JJDoc](jjdoc.md)

<br>
