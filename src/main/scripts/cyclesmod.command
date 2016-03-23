#!/bin/sh
if [ "$1" = "-c" ] || [ "$1" = "-C" ] || [ "$1" = "--help" ] || [ "$1" = "--HELP" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -jar `dirname $0`/cyclesmod.jar $1 $2 $3
  else java -Xms4m -Xmx8m -jar `dirname $0`/cyclesmod.jar $1 $2 $3
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -XstartOnFirstThread -Xms8m -Xmx16m -jar `dirname $0`/cyclesmod.jar $1 >/dev/null 2>&1 &
  else java -XstartOnFirstThread -Xms8m -Xmx16m -jar `dirname $0`/cyclesmod.jar $1 >/dev/null 2>&1 &
  fi
  osascript -e 'tell application "Terminal" to quit' &
  exit
fi
