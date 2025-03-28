#!/bin/bash

# Find directory
# shellcheck disable=SC2164
cd "$(dirname "$0")"

# Clean
mvn clean install

# Check if the build was successful
# shellcheck disable=SC2181
if [ $? -eq 0 ]; then
    # Detect OS and open the website accordingly
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        open http://localhost:8080/
    else
        # Linux (uses xdg-open)
        xdg-open http://localhost:8080/
    fi

else
    # If failed, display error
    echo "Build failed. Please check the errors above."
fi
