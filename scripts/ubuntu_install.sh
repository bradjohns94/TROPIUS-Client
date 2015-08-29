#!/bin/bash

# Create the assembly jar
cd "../client"
echo "Creating JAR file..."
sbt assembly > /dev/null 2>&1
if [ ! -d "$HOME/.tropius/" ]; then
    echo "Creating ${HOME}/.tropius/ directory..."
    mkdir "$HOME/.tropius/" > /dev/null 2>&1
fi
mv "target/scala-2.11/client-assembly-0.1.jar" "$HOME/.tropius/" > /dev/null 2>&1

# Move the upstart conf file to the corect location
cd "../scripts"
echo "Adding upstarts to ${HOME}/.config/upstart/..."
cp "tropius_upstart.conf" "$HOME/.config/upstart/" > /dev/null 2>&1
cp "spotify_upstart.conf" "$HOME/.config/upstart/" > /dev/null 2>&1

echo "Installation complete!"
