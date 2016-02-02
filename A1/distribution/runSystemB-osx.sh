#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $DIR
./jre/osx/Contents/Home/bin/java -jar systemB.jar "$DIR";