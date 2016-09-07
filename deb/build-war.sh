#!/bin/bash

rm ./var/lib/jetty9/webapps/*.war

cd ..

./build.sh && ./build.sh -f build/scripts/jarsigner.xml && ./build.sh dist-war && mv ./dist/exist-2.2*.war ./deb/var/lib/jetty9/webapps/exist.war