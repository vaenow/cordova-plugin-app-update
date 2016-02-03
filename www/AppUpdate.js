var exec = require('cordova/exec');

var _defaultUIValues = {
    updateLatest       : "",
    updateTitle        : "",
    updateMessage      : "",
    updateUpdateBtn    : "",
    updating           : "",
    updateCancel       : "",
    updateErrorTitle   : "",
    updateErrorMessage : "",
    updateErrorYesBtn  : "",
};

exports.checkAppUpdate = function(success, error, updateUrl, UIValues) {
    UIValues = UIValues ? [UIValues] : [];
    Object.keys(_defaultUIValues).forEach(function(k) {
        UIValues[k] = UIValues[k] || _defaultUIValues[k];
    });
    updateUrl && UIValues.push(updateUrl);
	var updateArgs = UIValues;
    exec(success, error, "AppUpdate", "checkAppUpdate", updateArgs);
};