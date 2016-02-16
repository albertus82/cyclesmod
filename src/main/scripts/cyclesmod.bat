@ECHO OFF
IF "%1" == "/c" GOTO CON
IF "%1" == "/C" GOTO CON
IF "%1" == "/?" GOTO HLP
GOTO GUI
:CON
IF "%JAVA_HOME%" == "" java.exe -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModCon %2
IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java.exe" -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModCon %2
GOTO END
:GUI
IF "%JAVA_HOME%" == "" START "" javaw.exe -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModGui %1
IF NOT "%JAVA_HOME%" == "" START "" "%JAVA_HOME%\bin\javaw.exe" -classpath "%~dp0cyclesmod.jar;%~dp0lib/*" it.albertus.cycles.CyclesModGui %1
GOTO END
:ERR
ECHO.CyclesMod: unrecognized option '%1'
ECHO.Try 'CYCLESMOD /?' for more information.
GOTO END
:HLP
ECHO.Launches CyclesMod application.
ECHO.
ECHO.Usage: CYCLESMOD [[sourcefile] | /C [destination]]
ECHO.
ECHO   sourcefile     CFG or INF file to load.
ECHO   /C             Runs in console mode.
ECHO   destination    Destination path for generated BIKES.CFG and BIKES.INF.
ECHO.
:END