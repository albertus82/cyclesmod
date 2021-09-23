#!/bin/sh
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
java -Xms${unexepack.vm.initialHeapSize}m -Xmx${unexepack.vm.maxHeapSize}m -cp "$PRGDIR/${project.build.finalName}.${project.packaging}:$PRGDIR/lib/*" ${unexepack.mainClass} "$@"
