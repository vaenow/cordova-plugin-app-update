
# cordova-plugin-app-update
App updater for Cordova/PhoneGap

# Preview
![enter image description here](https://github.com/vaenow/cordova-plugin-app-update/blob/master/res/img/Screenshot_2015-10-31-13-42-13.jpg)

# 

![enter image description here](https://github.com/vaenow/cordova-plugin-app-update/blob/master/res/img/Screenshot_2015-10-31-13-42-19.jpg)
#Install
###Latest published version on npm (with Cordova CLI >= 5.0.0)
`cordova plugin add cordova-plugin-app-update`

# Usage
 - Simple:
```js
window.AppUpdate.checkAppUpdate(onSuccess, onFail);
```
 - Verbose
```js
var appUpdate = cordova.require('cordova-plugin-app-update.AppUpdate');
appUpdate.checkAppUpdate(onSuccess, onFail);
```


- version code you may need
versionName | versionCode
------- | ----------------
0.0.1  | 12
0.3.4  | 3042  
3.2.4   | 302042
12.234.221  | 1436212


# Platforms
Android only

# License
MIT
