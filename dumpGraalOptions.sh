#!/bin/sh

# install native-image with: gu install native-image

export GRAAL_BIN_CE=../graalvm-ce-19.1.1/bin
export GRAAL_BIN_EE=../graalvm-ee-19.1.1/bin

$GRAAL_BIN_CE/java -XX:+JVMCIPrintProperties > graal_ce.vm 2>/dev/null
$GRAAL_BIN_EE/java -XX:+JVMCIPrintProperties > graal_ee.vm 2>/dev/null

$GRAAL_BIN_CE/native-image --expert-options-all > graal_ce.native 2>/dev/null
$GRAAL_BIN_EE/native-image --expert-options-all > graal_ee.native 2>/dev/null

