#!/bin/sh
java -Xms${unexepack.vm.initialHeapSize}m -Xmx${unexepack.vm.maxHeapSize}m -cp "`dirname $0`/../Resources/Java/${mac.build.finalName}.${project.packaging}:`dirname $0`/../Resources/Java/lib/*" ${unexepack.mainClass} "$@"
