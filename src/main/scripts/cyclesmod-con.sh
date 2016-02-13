if [ "$JAVA_HOME" != "" ]
then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $1 $2
else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModCon $1 $2
fi
