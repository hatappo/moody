#!/bin/sh

status=$1
message_succeeded=${2:-"\033[42m 🟢 Succeeded \033[m"}
message_failed=${3:-"\033[101m 🟥 Failed \033[m"}

if [ "" = "$status" ]; then
  :
elif [ "0" = "$status" ]; then
  echo $message_succeeded
else
  echo $message_failed
fi

exit $status
