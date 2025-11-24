#!/bin/bash

# Compile script for Flight Reservation System
# This compiles all Java source files with the MySQL connector in the classpath

echo "Compiling Flight Reservation System..."

# Create directories if they don't exist (for compiled .class files)
mkdir -p businesslogic/entities/enums
mkdir -p businesslogic/services
mkdir -p datalayer/dao
mkdir -p datalayer/impl
mkdir -p datalayer/database
mkdir -p gui/auth
mkdir -p gui/common
mkdir -p tests

# Compile all Java files
javac -cp "lib/mysql-connector-j-9.0.0.jar" \
  businesslogic/entities/*.java \
  businesslogic/entities/enums/*.java \
  businesslogic/services/*.java \
  datalayer/dao/*.java \
  datalayer/impl/*.java \
  datalayer/database/*.java \
  gui/*.java \
  gui/auth/*.java \
  gui/common/*.java \
  tests/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "To test the database connection, run:"
    echo "  java -cp \".:lib/mysql-connector-j-9.0.0.jar\" datalayer.database.DatabaseConnectionTest"
else
    echo "❌ Compilation failed. Check the error messages above."
    exit 1
fi

