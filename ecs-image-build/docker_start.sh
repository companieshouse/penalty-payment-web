#!/bin/bash 
# Start script for lfp-pay-web

PORT=8080
exec java -jar -Dserver.port="${PORT}" "lfp-pay-web.jar"
