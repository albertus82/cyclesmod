@ECHO OFF
IF /I "%1" == "-c" GOTO CON
IF /I "%1" == "--help" GOTO CON
GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1 %2 %3
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -Xms@console.vm.initialHeapSize@m -Xmx@console.vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1 %2 %3
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START "" javaw.exe -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1
IF NOT "%JAVA_HOME%" == "" START "" "%JAVA_HOME%\bin\javaw.exe" -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar "%~dp0@launch4j.jarName@" %1
GOTO END
:END