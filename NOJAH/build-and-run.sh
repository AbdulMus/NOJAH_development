#!/bin/bash

# Find directory
cd "$(dirname "$0")"

# Clean and build
mvn clean install

# Opens website (must refresh)
open http://localhost:8080/

# Check if the build was successful
if [ $? -eq 0 ]; then
    # Run the application
    mvn spring-boot:run
else
    # If failed, display error
    echo "Build failed. Please check the errors above."
fi
