#!/bin/sh

# install CE native-image with: gu install native-image
# install EE native image with gu install --file native-image-installable-svm-svmee-linux-amd64-19.2.0.1.jar
# downloaded from https://www.oracle.com/technetwork/graalvm/downloads/index.html

export GRAAL_VERSION=21.1.0

# JAVA 8

export JAVA_VERSION=java8

export GRAAL_BIN_CE_8=/home/chris/openjdk/graalvm-ce-$JAVA_VERSION-$GRAAL_VERSION/bin
export GRAAL_BIN_EE_8=/home/chris/openjdk/graalvm-ee-$JAVA_VERSION-$GRAAL_VERSION/bin

$GRAAL_BIN_CE_8/java -XX:+JVMCIPrintProperties > graal_ce_$JAVA_VERSION.vm 2>/dev/null
$GRAAL_BIN_EE_8/java -XX:+JVMCIPrintProperties > graal_ee_$JAVA_VERSION.vm 2>/dev/null

$GRAAL_BIN_CE_8/native-image --expert-options-all > graal_ce_$JAVA_VERSION.native 2>/dev/null
$GRAAL_BIN_EE_8/native-image --expert-options-all > graal_ee_$JAVA_VERSION.native 2>/dev/null

# JAVA 11

export JAVA_VERSION=java11

export GRAAL_BIN_CE_11=/home/chris/openjdk/graalvm-ce-$JAVA_VERSION-$GRAAL_VERSION/bin
export GRAAL_BIN_EE_11=/home/chris/openjdk/graalvm-ee-$JAVA_VERSION-$GRAAL_VERSION/bin

$GRAAL_BIN_CE_11/java -XX:+JVMCIPrintProperties > graal_ce_$JAVA_VERSION.vm 2>/dev/null
$GRAAL_BIN_EE_11/java -XX:+JVMCIPrintProperties > graal_ee_$JAVA_VERSION.vm 2>/dev/null

$GRAAL_BIN_CE_11/native-image --expert-options-all > graal_ce_$JAVA_VERSION.native 2>/dev/null
$GRAAL_BIN_EE_11/native-image --expert-options-all > graal_ee_$JAVA_VERSION.native 2>/dev/null

# JAVA 16

export JAVA_VERSION=java16

export GRAAL_BIN_CE_16=/home/chris/openjdk/graalvm-ce-$JAVA_VERSION-$GRAAL_VERSION/bin
export GRAAL_BIN_EE_16=/home/chris/openjdk/graalvm-ee-$JAVA_VERSION-$GRAAL_VERSION/bin

$GRAAL_BIN_CE_16/java -XX:+JVMCIPrintProperties > graal_ce_$JAVA_VERSION.vm 2>/dev/null
$GRAAL_BIN_EE_16/java -XX:+JVMCIPrintProperties > graal_ee_$JAVA_VERSION.vm 2>/dev/null

$GRAAL_BIN_CE_16/native-image --expert-options-all > graal_ce_$JAVA_VERSION.native 2>/dev/null
$GRAAL_BIN_EE_16/native-image --expert-options-all > graal_ee_$JAVA_VERSION.native 2>/dev/null

