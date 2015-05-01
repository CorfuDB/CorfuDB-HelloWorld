#!/usr/bin/env bash

CORFUDBBIN="${BASH_SOURCE-$0}"
CORFUDBBIN="$(dirname "${CORFUDBBIN}")"
CORFUDBBINDIR="$(cd "${CORFUDBBIN}"; pwd)"

if [ -e "$CORFUDBBIN/../share/corfudb/bin/corfuDBEnv.sh" ]; then
    . "$CORFUDBBINDIR"/../share/corfudb/bin/corfuDBEnv.sh
else
    . "$CORFUDBBINDIR"/corfuDBEnv.sh
fi

CORFUDB_DAEMON_OUT="/var/log/corfudb.${1}.log"
CORFUDBMAIN="org.corfudb.example.CorfuHelloWorld"

"$JAVA" -cp "$CLASSPATH" "-Dorg.slf4j.simpleLogger.defaultLogLevel=${CORFUDB_LOG4J_PROP}" $JVMFLAGS -Xmx16g "$CORFUDBMAIN" ${*:1} 2>&1 < /dev/null
