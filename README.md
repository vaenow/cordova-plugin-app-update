# cordova-plugin-app-update
auto detect app update for cordova

#Install
###Latest published version on npm (with Cordova CLI >= 5.0.0)
`cordova plugin add cordova-plugin-app-update`

#Usage
 - Simple:
```js
window.AppUpdate.checkAppUpdate(onSuccess, onFail);
```
 - Verbose
```js
var appUpdate = cordova.require('cordova-plugin-app-update.AppUpdate');
appUpdate.checkAppUpdate(onSuccess, onFail);
```

#Platforms
Android only

#License
MIT
