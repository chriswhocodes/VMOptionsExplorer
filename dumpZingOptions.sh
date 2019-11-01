#!/bin/sh
/opt/zing/zing-jdk8/bin/java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions >zing8.out 2>/dev/null
/opt/zing/zing-jdk11/bin/java -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions >zing11.out 2>/dev/null
# see http://docs.azul.com/zing/index.htm
