#!/bin/bash

# Navigate to the project directory (if not already there)
cd "$(dirname "$0")"

# Clean and build the project using Maven
echo "Building the project..."
mvn clean install

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! Starting the application..."
    # Run the Spring Boot application
    mvn spring-boot:run
else
    echo "Build failed. Please check the errors above."
fi