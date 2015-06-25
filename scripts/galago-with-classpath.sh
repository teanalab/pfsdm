#!/usr/bin/env bash

CLASSPATH_PREFIX=`sbt "export full-classpath" | grep -v '\['` galago $@
