@ECHO OFF
IF "%JAVA_HOME%" == "" START /BELOWNORMAL "" javaw.exe -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModGui %1
IF NOT "%JAVA_HOME%" == "" START /BELOWNORMAL "" "%JAVA_HOME%\bin\javaw.exe" -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModGui %1