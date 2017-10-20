#!/bin/sh

CUR_DIR=$(cd "$(dirname "$0")"; pwd)
SCRIPT_PID=$(ps -ef | grep "$CUR_DIR/synapse.sh" | grep -v grep | awk  '{print $2} ')
ps -ef | grep "$SCRIPT_PID" | grep -v grep | awk  '{print $2} ' | xargs kill
