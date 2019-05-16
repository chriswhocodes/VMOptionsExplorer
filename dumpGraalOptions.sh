#!/bin/sh

../graalvm-ce-19.0.0/bin/java -XX:+JVMCIPrintProperties > graal_ce.out
../graalvm-ee-19.0.0/bin/java -XX:+JVMCIPrintProperties > graal_ee.out

