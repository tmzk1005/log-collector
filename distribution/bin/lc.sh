#!/bin/bash

BIN_DIR=$(dirname "$0")
LOG_COLLECTOR_HOME=$(cd -P "$BIN_DIR"/.. || exit 1;pwd)
cd "$LOG_COLLECTOR_HOME" || exit 1
export LOG_COLLECTOR_HOME
exec "bin/run_class.sh" zk.logcollector.core.LogCollectorApp
