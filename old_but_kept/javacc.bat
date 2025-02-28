setlocal
set NUMBER=8.0.0
set VERSION=-SNAPSHOT
set NAME=%NUMBER%%VERSION%
set M2=%USERPROFILE%\.m2\repository\org\javacc
set PARSER=%M2%\core\%NAME%\core-%NAME%.jar
set CPP=%M2%\codegen\cpp\%NAME%\cpp-%NAME%.jar
set JAVA=%M2%\codegen\java\%NAME%\java-%NAME%.jar
set CSHARP=%M2%\codegen\csharp\%NAME%\csharp-%NAME%.jar
set CP=%PARSER%;%JAVA%;%CPP%
java -cp %CP% javacc %*
endlocal
