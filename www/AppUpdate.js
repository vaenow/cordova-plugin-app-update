var exec = require('cordova/exec');

exports.checkAppUpdate = function(success, error) {
    exec(success, error, "AppUpdate", "checkAppUpdate", []);
};