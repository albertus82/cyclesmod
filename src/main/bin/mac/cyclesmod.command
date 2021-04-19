#!/bin/sh
java -Xms${cli.vm.initialHeapSize}m -Xmx${cli.vm.maxHeapSize}m -DC -jar "`dirname $0`/../Resources/Java/${mac.build.finalName}.${project.packaging}" "$@"
