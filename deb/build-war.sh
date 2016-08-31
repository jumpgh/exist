#!/bin/bash

cd ..

./build.sh && ./build.sh -f build/scripts/jarsigner.xml && ./build.sh dist-war && mv ./dist/exist-2.2*.war ./deb/opt/jetty9/webapps/exist.war
