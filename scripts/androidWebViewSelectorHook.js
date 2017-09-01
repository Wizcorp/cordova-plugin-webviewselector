module.exports = function(ctx) {
	// make sure android platform is part of build
	if (ctx.opts.platforms.indexOf('android') < 0) { return; }

	console.log('Android webViewSelector hook installation');

	var fs = ctx.requireCordovaModule('fs');
	var path = ctx.requireCordovaModule('path');
	var deferral = ctx.requireCordovaModule('q').defer();

	var fileToPatch = 'platforms/android/CordovaLib/src/org/apache/cordova/CordovaWebViewImpl.java';
	var patch = '        WebViewSelector.updateWebViewPreference(context, preferences);\n';
	var patchAfter = 'public static CordovaWebViewEngine createEngine(Context context, CordovaPreferences preferences) {\n';

	var file = path.join(ctx.opts.projectRoot, fileToPatch);
	fs.readFile(file, 'utf8', function(err, data) {
		var fileContent = data;
		if (fileContent.indexOf('WebViewSelector.updateWebViewPreference') !== -1) {
			return deferral.resolve();
		}
		if (err) {
			deferral.reject('Android webViewSelector hook installation failed at readFile: ' + err);
		} else {
			fileContent = fileContent.replace(patchAfter, patchAfter + patch);
			fs.writeFile(file, fileContent, 'utf8', function (err) {
				if (err) {
					deferral.reject('Android webViewSelector hook installation failed at writeFile: ' + err);
				} else {
					deferral.resolve();
				}
			});
		}
	});

	return deferral.promise;
};