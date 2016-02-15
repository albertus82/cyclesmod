if [ "$1" = "-c" ] || [ "$1" = "-C" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  fi
elif [ "$1" = "--help" ]
  then
  echo "Launches CyclesMod application."
  echo
  echo "Usage: cyclesmod.sh [-c] [--help] [destination path]"
  echo
  echo "  -c        Runs in console mode"
  echo "  --help    Shows this help"
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  fi
fi
