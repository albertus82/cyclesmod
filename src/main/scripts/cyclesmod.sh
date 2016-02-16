if [ "$1" = "-c" ] || [ "$1" = "-C" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  fi
elif [ "$1" = "--help" ]
  then
  echo "Launches CyclesMod application."
  echo
  echo "Usage: cyclesmod.sh [[sourcefile] | -c [destination]]"
  echo
  echo "  sourcefile     CFG or INF file to load."
  echo "  -c             Runs in console mode."
  echo "  destination    Destination path for generated BIKES.CFG and BIKES.INF."
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  fi
fi
