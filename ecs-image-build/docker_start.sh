#!/bin/bash 
# Start script for penalty-payment-web

PORT=8080
exec java -jar -Dserver.port="${PORT}" "penalty-payment-web.jar"
