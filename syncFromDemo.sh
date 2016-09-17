#!/bin/sh

# sync *.java
cp -r ../cordova-plugin-app-update-demo/platforms/android/src/com/vaenow/appupdate/android/* src/android/

# sync *.xml
cp -r ../cordova-plugin-app-update-demo/platforms/android/res/values* res/

# clean
rm res/values/strings.xml

