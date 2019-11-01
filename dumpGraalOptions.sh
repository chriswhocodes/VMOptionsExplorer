#!/bin/sh

# install CE native-image with: gu install native-image
# install EE native image with gu install --file native-image-installable-svm-svmee-linux-amd64-19.2.0.1.jar
# downloaded from https://www.oracle.com/technetwork/graalvm/downloads/index.html

export GRAAL_BIN_CE=/home/chris/openjdk/graalvm-ce-19.2.1/bin
export GRAAL_BIN_EE=/home/chris/openjdk/graalvm-ee-19.2.1/bin

$GRAAL_BIN_CE/java -XX:+JVMCIPrintProperties > graal_ce.vm 2>/dev/null
$GRAAL_BIN_EE/java -XX:+JVMCIPrintProperties > graal_ee.vm 2>/dev/null

$GRAAL_BIN_CE/native-image --expert-options-all > graal_ce.native 2>/dev/null
$GRAAL_BIN_EE/native-image --expert-options-all > graal_ee.native 2>/dev/null

