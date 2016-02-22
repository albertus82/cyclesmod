if [ "$1" = "-c" ] || [ "$1" = "-C" ] || [ "$1" = "--help" ] || [ "$1" = "--HELP" ]
  then if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx8m -jar cyclesmod.jar $1 $2 $3
  else java -Xms4m -Xmx8m -jar cyclesmod.jar $1 $2 $3
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx32m -jar cyclesmod.jar $1
  else java -Xms8m -Xmx32m -jar cyclesmod.jar $1
  fi
fi
