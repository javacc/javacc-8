javacc-8~javacc
---------------
* pb: don't find module core  

* google-java-format eclipse plugin:  
    - javacc-formatter_1.xml
    - javacc-cleanup_1.xml
    - need to add in eclipse.ini after -vmargs:  
`--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED`  

*  move java code in tests elsewhere

Done:  
8 july 2024  
* *.xml:
        - updated Copyrights / licenses,
        - changed <?xml version='1.0' encoding='ISO-8859-1' ?> -> <?xml version="1.0" encoding="UTF-8"?>
        - reformatted

To do:  



