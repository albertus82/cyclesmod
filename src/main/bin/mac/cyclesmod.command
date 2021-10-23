#!/bin/sh
java -Xms${cli.vm.initialHeapSize}m -Xmx${cli.vm.maxHeapSize}m -cp "`dirname $0`/../Resources/Java/${mac.build.finalName}.${project.packaging}" ${cli.mainClass} "$@"
