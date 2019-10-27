#!/bin/sh
if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -D@mainClass@.main.mode=console -jar `dirname $0`/@macos.jarFileName@ $1 $2 $3 $4 $5 $6
  else java -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -D@mainClass@.main.mode=console -jar `dirname $0`/@macos.jarFileName@ $1 $2 $3 $4 $5 $6
fi
