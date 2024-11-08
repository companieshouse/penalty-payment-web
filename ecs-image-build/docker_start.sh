#!/bin/bash 
# Start script for pps-pay-web

PORT=8080
exec java -jar -Dserver.port="${PORT}" "pps-pay-web.jar"
