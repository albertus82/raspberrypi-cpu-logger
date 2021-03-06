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

cd "$PRGDIR" && java -Xms8m -Xmx36m -Djava.util.logging.config.file=conf/logging.properties src/main/java/RaspberryPiCpuLogger.java conf/api.key
