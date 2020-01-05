#!/bin/bash

#takes care of the current directory
SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#http://askubuntu.com/questions/237893/firefox-sluggish-printing-error-message
export $(dbus-launch)
export NSS_USE_SHARED_DB=ENABLED
chmod 700 $SCRIPT_DIR/firefox 
chmod 700 $SCRIPT_DIR/xvfb-run 
RES=`cat $SCRIPT_DIR/display_size`
$SCRIPT_DIR/xvfb-run -a --server-args="-screen 0 ${RES}x24" $SCRIPT_DIR/firefox "$@"