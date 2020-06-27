#!/bin/sh
docker run --rm azul/zulu-openjdk:8 java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -showversion >zulu8.out 2>/dev/null 
docker run --rm azul/zulu-openjdk:11 java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -showversion >zulu11.out 2>/dev/null
docker run --rm azul/zulu-openjdk:13 java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -showversion >zulu13.out 2>/dev/null
docker run --rm azul/zulu-openjdk:14 java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -showversion >zulu14.out 2>/dev/null
# https://docs.azul.com/zulu/
