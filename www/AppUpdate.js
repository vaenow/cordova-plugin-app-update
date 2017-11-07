var exec = require('cordova/exec');

/**
 * Check if there is an update to the App.
 * 
 * This function can be called in three ways:
 * 1. checkAppUpdate(updateUrl)
 * 2. checkAppUpdate(updateUrl, options)
 * 3. checkAppUpdate(sucessCallback, errorCallback, updateUrl, options)
 * 
 * @param successOrUrl The success callback or the URL where the update data  is located
 * @param errorOrOptions The function called on error or the authentication options
 * @param updateUrl The URL where the update data is located
 * @param options An object that may contain the authentication options
 */
exports.checkAppUpdate = function(successOrUrl, errorOrOptions, updateUrl, options) {
    // If the update URL hasnt been set in the updateUrl then assume no callbacks passed
    var successCallback = updateUrl ? successOrUrl : null;
    var errorCallback = updateUrl ? errorOrOptions : null;
    
    // This handles case 2, where there is an updateURL and options set
    if ( !updateUrl && typeof errorOrOptions === 'object' ) {
        options = errorOrOptions;
    }
    
    // If there is no updateUrl then assume that the URL is the first paramater
    updateUrl = updateUrl ? updateUrl : successOrUrl;
    
    options = options ? options : {};
    
    exec(successCallback, errorCallback, "AppUpdate", "checkAppUpdate",  [updateUrl, options]);
};
