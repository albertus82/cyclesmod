#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$1" = "-c" ] || [ "$1" = "-C" ] || [ "$1" = "--help" ] || [ "$1" = "--HELP" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -classpath "$PRGDIR/cyclesmod.jar:$PRGDIR/lib/*" it.albertus.cycles.CyclesMod $1 $2 $3
  else java -Xms4m -Xmx8m -classpath "$PRGDIR/cyclesmod.jar:$PRGDIR/lib/*" it.albertus.cycles.CyclesMod $1 $2 $3
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx16m -classpath "$PRGDIR/cyclesmod.jar:$PRGDIR/lib/*" it.albertus.cycles.CyclesMod $1
  else java -Xms8m -Xmx16m -classpath "$PRGDIR/cyclesmod.jar:$PRGDIR/lib/*" it.albertus.cycles.CyclesMod $1
  fi
fi
