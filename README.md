# cordova-plugin-app-update
auto detect app update for cordova

#Install
###Latest published version on npm (with Cordova CLI >= 5.0.0)
`cordova plugin add cordova-plugin-dynamic-update`

#Usage
 - Simple:
```js
window.AppUpdate.checkAppUpdate(onSuccess, onFail);
```
 - verbose
```js
var appUpdate = cordova.require('cordova/AppUpdate');
appUpdate.checkAppUpdate(onSuccess, onFail);
```

#Platforms
Android only

#License
