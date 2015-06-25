#!/usr/bin/env bash
# Run as $ source scripts/export-classpath.sh

export CLASSPATH_PREFIX=`sbt "export full-classpath" | grep -v '\['`
