if [ "$JAVA_HOME" != "" ]
then "$JAVA_HOME/bin/java" -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
else java -classpath "cyclesmod.jar:lib/*" it.albertus.cycles.CyclesModGui $1
fi
