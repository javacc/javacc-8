javacc-8~javacc
---------------
- pb: don't find module core  

- google-java-format eclipse plugin:  
    - javacc-formatter_1.xml
    - javacc-cleanup_1.xml
    - need to add in eclipse.ini after -vmargs:  
`--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED`  
`--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED`  

Done
---
8 july 2024  
- *.xml:
    - updated Copyrights / licenses,
    - changed <?xml version='1.0' encoding='ISO-8859-1' ?> -> <?xml version="1.0" encoding="UTF-8"?>
    - reformatted
    - committed; pushed in error; 
    - sreeni gave access to me for javacc-8-java; push ok on this project, not on others

9 july 2024  
- *.pom.xml:
    - maven validate: ok except for parent (child module core does not exist)
    - maven install: core ok, java ko: fixed launched config maven_home not embedded
- updated all /LICENSE files
- changed all projects encodings to inherited (UTF-8)

10 july 2024  
- found how to configure nar-maven-plugin with my MSVC (in cpp/it/pom.xml)
- need to reduce attack surface to allow .exe files be exectable : under Powershell admin:  
`Add-MpPreference -AttackSurfaceReductionOnlyExclusions c:\devs\gitrepo\*`  
`Add-MpPreference -AttackSurfaceReductionOnlyExclusions c:\devs\gitrepo\*\*`  
and so on up to 9 levels  
`Add-MpPreference -AttackSurfaceReductionOnlyExclusions c:\devs\gitrepo\*\*\*\*\*\*\*\*\*`  

Aug 2024
- found how to configure plexus-compiler-csharp with my MSVC (in csharp/it/pom.xml)
- aligned & fixed all /bugs tests cases
- found issue with cpp TOKEN_MANAGER_INCLUDE option which is not generated in the TM.cc
- found that DEBUG_TOKEN_MANAGER output to stdout for C++ (ok) and to stderr for java & C# (ko, should align with DEBUG_PARSER & DEBUG_LOOKAHEAD)

To know
-------
- Eclipse m2e: launch configuration:
    - cocher Resolve Workspace artifacts la première fois que l'on change les versions des artifacts, puis l'enlever pour que les install soient les mêmes qu'en ligne de commande
    - ajouter dans l'onglet Environnement la variable JAVA_HOME pointant sur le bon JDK car m2e ne semble pas passer le JRE fixé dans l'onglet JRE au plugin invoker
    - pour javacc-8-cpp:
        - ajouter dans l'onglet Environnement la variable PATH concaténant le %PATH% courant et le répertoire du compilateur csc.exe, sous %VisualStudioDir%/MSBuild\Current\Bin\Roslyn (ou modifier directement la variable système PATH et arrêter/redémarrer Eclipse)
        - ou créer une variable d'environnement CsharpDir vers le répertoire du compilateur csc.exe et faire un profile avec dans la configuration de maven-compiler-plugin la ligne <executable>${env.CSHARPDIR}\csc.exe</executable>
    
- profiles: (https://maven.apache.org/guides/introduction/introduction-to-profiles.html)
    - run-its dans it/pom.xml, activé si la propriété maven.test.skip n'est pas à true, pour invoker les exécutions des tests sur bugs/examples/grammars
    - windows/linux/macos, pour cpp/csharp/js?, suivant l'os, pour fixer les paramètres à nar-maven-plugin
    - jcc/jjt / 8/7/6 / w, activés pour 8 si existent répertoires /src/main/javacc et/ou /src/main/jjtree, non activés pour les autres - w semble être la copie de jjc -, pour fixer la version du plugin javacc (org... pour 8 et net... pour 7/6) et la configuration des plugins javacc- et nar-

- nar plugin:
    - message "OUTPUT>cl : Command line warning D9024 : unrecognized source file type '', object file assumed" quand l'extension du fichier n'est pas .c ou .cpp (ici .cc)
    - pour supprimer le message le plugin devrait passer l'option /Tp filename ou l'option globale /TP
    - message "OUTPUT>cl : Command line warning D9027 : source file '' ignored" quand ??? il faudrait logger la ligne de commande de compilation

To do
-----

- corriger traces debug_parser=true avec keep_line_column = false

- voir sur cpp si DefaultCharStream est un SimpleCharStream ou un JavaCharStream

- voir tous les tests avec <skip>true</skip>

- move java code in tests elsewhere

- ajouter les nouveaux examples/lookahead java/cpp/csharp et les grammaires non buildées dans java/...

- voir à avoir les mêmes examples/grammars et les mêmes exécutions dans java/cpp/csharp

- voir à changer les répertoires src/main/java en src/main/cpp et src/main/csharp dans cpp/csharp

- voir les cleans

- voir s'il faut garder les poms parents sautés

- voir à propertiser ignoreFailures

- revoir si toutes les grammaires et exemples ont bien un pom

- voir à mettre des builds reproductibles https://maven.apache.org/guides/mini/guide-reproducible-builds.html)

- voir les properties / plugins / devs / contrs propres à chaque projet, ou tout dans le pom javacc-8

- voir à utiliser les fichiers de configuration sous .mvn (https://maven.apache.org/configure.html)

- voir <root>${basedir}</root>, <sitePluginVersion>3.3</sitePluginVersion>

- voir filtering pour externaliser les propriétés

- voir à passer le surefire plugin au failsafe plugin

- voir à enlever les <postBuildHookScript>verify.bsh</postBuildHookScript>

- voir où déclarer <artifactId>exec-maven-plugin</artifactId>

- voir à cloner javacc-8 dans les sous-projets dans les maven.yml pour permettre aux actions des sous-projets de trouver le pom parent

- remove under javacc-8: codegen, rulesets, src, target, tests, *.bat, check keystore, update /javacc-8/pom.xml, update docs & .md, see for security.md

- voir
[INFO] --- javacc:3.0.3:javacc (jcc) @ core ---
[WARNING] 1 problem was encountered while building the effective model for net.sourceforge.cobertura:cobertura:jar:2.1.1

- nar-maven-plugin:  
    - version 3.10.1 lit les propriétés nar.windows.xxx.yyy, puis les clés de registre, mais pas les variables d'environnement
    - version 3.10.2-SNAPSHOT lit les variables d'environnement VisualStudioVersion et VSnnnCOMNTOOLS (où nnn est la forme interne de la version, ex 1710 pour 17.10), mais n'est pas publiée sur le repository des snapshots Sonatype

- voir à utiliser spotless pour la vérification du formatage

- voir https://oss.carbou.me/license-maven-plugin/  

- .xml: passer les licences en fin de fichier avec ligne entête

- faire une nouvelle version du javacc-maven-plugin en nettoyant, en particulier les dépendances (cobertura...)

Escapes
-------

add_escapes()

javacc-8-core::parser.JavaCCGlobals.add_escapes(), utilisé dans
- core:   jjdoc.JJDoc.emitRE() public static sur SingleCharacter, CharacterRange et RStringLiteral: pour l'affichage de ?
- core:   parser.LookaheadCalc.image() private static, sur RStringLiteral; pour l'affichage des warnings
- csharp: csharp.TokenManagerCodeGenerator.generateConstantsClass() private, sur le TokenizerData, pour créer le code du tableau tokenImage[]
- java:   java.JavaHelperFiles.gen_Constants() static, sur RStringLiteral, pour créer le code du tableau tokenImage[]

javacc-8-cpp::templates/cpp/ParseException.template: ParseException.add_escapes() (corrigé de addEscapes) utilisé dans
- cpp: templates/cpp/ParseException.cc.template: ParseException::initialise() private, sur le token rencontré, pour créer le message de ParseException

javacc-8-csharp::templates/csharp/ParseException.template.add_escapes(), utilisé dans
- csharp: templates/csharp/ParseException.template: ParseException::initialise() public static, sur le token rencontré, pour créer le message de ParseException
- csharp: templates/csharp/TokenManagerDriver.template: appels à ParseException.add_escapes() pour affichage des traces de debug sur tokenImage[i] ou lexStateNames[l]

javacc-8-java::templates/gwt/ParseException.template.add_escapes(), utilisé dans
- java: templates/gwt/ParseException.template: ParseException::initialise() static, sur le token rencontré, pour créer le message de ParseException
- java: templates/ParseException.template: ParseException::initialise() static, sur le token rencontré, pour créer le message de ParseException


addEscapes()

javacc-8-java::templates/TokenMgrError.template.addEscapes() protected static, utilisé dans
- java: java.ParserCodeGenerator.generateCode(), dans les traces (générées) de debug du TokenManager sur le token consumed ou visited (trace_token/trace_scan)
- java: templates/TokenMgrError.template.LexicalErr() protected static, pour affichage du message de LexicalError


addUnicodeEscapes()

javacc-8-core::parser.JavaCCGlobals.addUnicodeEscapes() public static, utilisé dans
- core: parser.Token.printTokenOnly() public, pour copier / générer le code utilisateur (?)
- java: java.ParserCodeGenerator.generateCode(), dans les traces (générées) de debug du parser (call/return)

javacc-8-core::jjtree.TokenUtils.addUnicodeEscapes() public static utilisé dans
- core: jjtree.TokenUtils.print() public, pour copier / générer le code utilisateur (specials + token) (?)
- core: jjtree.JJTreeNode.print() public, pour copier / générer le code utilisateur (specials + token) (?)
- core: utils.CodeBuilder.escapeToUnicode() public, en relai de l'appel à TokenUtils.addUnicodeEscapes()

javacc-8-cpp::templates/cpp/TokenManagerError.cc.template (def) et JavaCC.h.template (decl) addUnicodeEscapes() public, utilisé dans
- cpp: cpp.ParserCodeGenerator.generateCode(), dans le message de parseError() et les traces (générées) de debug sur le token consumed ou visited
- cpp: templates/cpp/DefaultParserErrorHandler.cc.template: DefaultParserErrorHandler::unexpectedToken() (Expecting but got) et parseError() (Encountered) pour créer un message d'erreur
- cpp: templates/cpp/TableDrivenTokenManager.cc.template: dans les traces (générées) de debug du TokenManager




