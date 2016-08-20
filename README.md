

![travis](https://travis-ci.org/vaenow/cordova-plugin-app-update.svg?branch=master)  

[![NPM](https://nodei.co/npm/cordova-plugin-app-update.png?downloads=true&downloadRank=true)](https://nodei.co/npm/cordova-plugin-app-update/)


# cordova-plugin-app-update
App updater for Cordova/PhoneGap

# Demo 
Try it yourself:

Just clone and install this demo.
[cordova-plugin-app-update-DEMO](https://github.com/vaenow/cordova-plugin-app-update-demo)
:tada:

 * 如果喜欢它，请别忘了给我一颗鼓励的星
 * Support me a `Star` if it is necessary.  :+1:
 
# Preview
![enter image description here](https://raw.githubusercontent.com/vaenow/cordova-plugin-app-update/master/res/img/Screenshot_2015-10-31-13-42-13.jpg)

# 

![enter image description here](https://raw.githubusercontent.com/vaenow/cordova-plugin-app-update/master/res/img/Screenshot_2015-10-31-13-42-19.jpg)

# Install

### Latest published version on npm (with Cordova CLI >= 5.0.0)
`cordova plugin add cordova-plugin-app-update --save`

# Usage
 - Simple:
```js
var updateUrl = "http://192.168.0.1/version.xml";
window.AppUpdate.checkAppUpdate(onSuccess, onFail, updateUrl);
```
 - Verbose
```js
var appUpdate = cordova.require('cordova-plugin-app-update.AppUpdate');
var updateUrl = "http://192.168.0.1/version.xml";
appUpdate.checkAppUpdate(onSuccess, onFail, updateUrl);
```


### versionCode

You can simply get the versionCode from typing those code in `Console`

```js
var versionCode = AppVersion.build
console.log(versionCode)  // 302048
```


versionName | versionCode
------- | ----------------
0.0.1  | 18
0.3.4  | 3048  
3.2.4   | 302048
12.234.221  | 1436218

### server version.xml file
 
```xml
<update>
    <version>302048</version>
    <name>name</name>
    <url>http://192.168.0.1/android.apk</url>
</update>
```


# Platforms
Android only

# License
MIT

# :snowflake: :beers:

* Please let me know if you have any questions.


