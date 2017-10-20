var exec = require('cordova/exec');

exports.checkAppUpdate = function(success, error, updateUrl, options) {
    updateUrl = updateUrl ? updateUrl : '';
    options = options ? options : {};
    exec(success, error, "AppUpdate", "checkAppUpdate",  [updateUrl, options]);
};