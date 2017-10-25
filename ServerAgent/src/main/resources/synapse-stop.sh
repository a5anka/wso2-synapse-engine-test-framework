#!/bin/sh

CUR_DIR=$(cd "$(dirname "$0")"; pwd)
SCRIPT_PID=$(ps -ef | grep "$CUR_DIR/synapse.sh" | grep -v grep | awk  '{print $2} ')

if [ ! -z "$SCRIPT_PID"  ]
then
  ps -ef | grep "$SCRIPT_PID" | grep -v grep | awk  '{print $2} ' | xargs kill
fi
