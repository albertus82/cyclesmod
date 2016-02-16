@ECHO OFF
IF "%1" == "/?" GOTO HLP
IF NOT "%3" == "" GOTO ERR
IF /I "%1" == "/C" GOTO CON
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
ECHO.CyclesMod: too many parameters - %3
ECHO.Try 'CYCLESMOD /?' for more information.
GOTO END
:HLP
ECHO.Start CyclesMod application.
ECHO.
ECHO.Usage: CYCLESMOD [[sourcefile] ^| /C [destination]] [/?]
ECHO.
ECHO.  sourcefile     CFG or INF file to load (GUI mode).
ECHO.  /C             Start in console mode.
ECHO.  destination    Path for generated BIKES.CFG and BIKES.INF (console mode).
ECHO.  /?             Display this help and exit.
GOTO END
:END