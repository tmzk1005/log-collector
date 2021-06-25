#!/bin/bash

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <MainClassName>"
    exit 0
fi

MAIN_CLASS=$1

JAVA_CMD=$(command -v java)

if [[ ! -x $JAVA_CMD ]] ; then
    echo "No executable java command found!" >&2
    exit 1
fi

JAVA_VERSION=$(${JAVA_CMD} -version 2>&1 | head -n 1 | awk '{print $3}' | sed -e 's/^"//' -e 's/"$//' | awk -F . '{print $1}')

if [[ $JAVA_VERSION -lt 11 ]]; then
    echo "Current java version is $JAVA_VERSION, please use 11 or greater."
    exit 1
fi

unset CLASSPATH
CLASSPATH=""

# mapfile -t jars < <(ls -- libs/*.jar)
jars=($(ls -- libs/*.jar))
for jar in "${jars[@]}"
do
    CLASSPATH="${CLASSPATH}${jar}:"
done

JVM_OPTS=()
JVM_OPT_CONF_FILE="$LOG_COLLECTOR_HOME/config/jvmoptions"
if [[ -f $JVM_OPT_CONF_FILE ]] ; then
    lines=$(sed -e "s/^\s*//" -e '/^#/d' -e 's/\s*$//' -e '/^$/d' "$JVM_OPT_CONF_FILE")
    while read -r line
    do
        JVM_OPTS+=("$line")
    done <<< "$lines"
fi

cd "$LOG_COLLECTOR_HOME" || exit 1

exec "${JAVA_CMD}" "${JVM_OPTS[@]}" -cp "${CLASSPATH}" -DLOG_COLLECTOR_HOME="${LOG_COLLECTOR_HOME}" "${MAIN_CLASS}"
