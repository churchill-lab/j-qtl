#!/bin/bash

# for running with debugging enabled
#$JAVA_HOME/bin/java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n -Djava.util.logging.config.class=org.jax.util.ResourceBasedLoggerConfiguration -jar dist/j-qtl-1.3.5.jar

# for running with debugging disabled
$JAVA_HOME/bin/java -Djava.util.logging.config.class=org.jax.util.ResourceBasedLoggerConfiguration -jar dist/j-qtl-1.3.5.jar
