@ECHO OFF
IF /I "%1" == "-c" GOTO CON
IF /I "%1" == "--help" GOTO CON
GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms4m -Xmx16m -jar "%~dp0CyclesMod.jar" %1 %2 %3
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms4m -Xmx16m -jar "%~dp0CyclesMod.jar" %1 %2 %3
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START "" javaw.exe -Xms8m -Xmx32m -jar "%~dp0CyclesMod.jar" %1
IF NOT "%JAVA_HOME%" == "" START "" "%JAVA_HOME%\bin\javaw.exe" -Xms8m -Xmx32m -jar "%~dp0CyclesMod.jar" %1
GOTO END
:END