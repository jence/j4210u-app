#!/bin/bash

# Get the current working directory
CURRENT_DIR="$(pwd)"

# Define relative paths
RELATIVE_LIBRARY_PATH="./"
RELATIVE_JAR_FILE="./j4210u.jar"

# Construct absolute paths
LIBRARY_PATH="$CURRENT_DIR/$RELATIVE_LIBRARY_PATH"
JAR_FILE="$CURRENT_DIR/$RELATIVE_JAR_FILE"

# Run Java with absolute paths
java -Djava.library.path="$LIBRARY_PATH" -jar "$JAR_FILE"
