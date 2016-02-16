if [ "$1" = "--help" ]
  then
  echo "Start CyclesMod application."
  echo
  echo "Usage: cyclesmod.sh [[sourcefile] | -c [destination]] [--help]"
  echo
  echo "  sourcefile     CFG or INF file to load (GUI mode)"
  echo "  -c             start in console mode"
  echo "  destination    path for generated BIKES.CFG and BIKES.INF (console mode)"
  echo "  --help         display this help and exit"
elif [ "$3" != "" ]
  then
  echo "cyclesmod: too many parameters - $3"
  echo "Try 'cyclesmod.sh --help' for more information."
elif [ "$1" = "-c" ] || [ "$1" = "-C" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  else java -Xms4m -Xmx8m -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $2
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx32m -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  else java -Xms8m -Xmx32m -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
  fi
fi
