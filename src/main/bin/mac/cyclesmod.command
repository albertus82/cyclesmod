#!/bin/sh
java -Xms${console.vm.initialHeapSize}m -Xmx${console.vm.maxHeapSize}m -Dmode=cli -jar "`dirname $0`/../Resources/Java/${mac.build.finalName}.${project.packaging}" "$@"
