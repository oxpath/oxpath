#!/bin/bash

#takes care of the current directory
SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
chmod 700 $SCRIPT_DIR/firefox 
$SCRIPT_DIR/firefox "$@"