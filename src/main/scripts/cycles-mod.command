#!/bin/sh
if [ "$1" = "-c" ] || [ "$1" = "-C" ] || [ "$1" = "--help" ] || [ "$1" = "--HELP" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@artifactId@.jar $1 $2 $3
  else java -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@artifactId@.jar $1 $2 $3
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@artifactId@.jar $1 >/dev/null 2>&1 &
  else java -XstartOnFirstThread -Xms@vm.initialHeapSize@m -Xmx@vm.maxHeapSize@m -jar `dirname $0`/@artifactId@.jar $1 >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
fi
