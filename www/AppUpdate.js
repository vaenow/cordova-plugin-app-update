var exec = require('cordova/exec');

exports.checkAppUpdate = function(success, error, updateUrl, options) {
    updateUrl = updateUrl ? updateUrl : '';
    options = options ? options : {};
    options.authType = options.authType || ""; // fix issue #64
    exec(success, error, "AppUpdate", "checkAppUpdate",  [updateUrl, options]);
};
