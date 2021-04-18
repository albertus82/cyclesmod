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
java -Xms${console.vm.initialHeapSize}m -Xmx${console.vm.maxHeapSize}m -Dmode=cli -jar "$PRGDIR/${project.build.finalName}.${project.packaging}" "$@"
