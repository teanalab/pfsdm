CLASSPATH_PREFIX=`sbt "export full-classpath" | grep -v '\['` galago $@
