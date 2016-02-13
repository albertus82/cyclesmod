@ECHO OFF
IF "%JAVA_HOME%" == "" java.exe -classpath "cyclesmod.jar;lib/*" it.albertus.cycles.CyclesModCon %1 %2
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -classpath "cyclesmod.jar;lib/*" it.albertus.cycles.CyclesModCon %1 %2