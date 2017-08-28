var exec = require('cordova/exec');
var channel = require('cordova/channel');

var WebViewSelector = {};

WebViewSelector.WebViewType = {
	SYSTEM: 0,
	CROSSWALK: 1
};

WebViewSelector.currentEngine = null;
WebViewSelector.availableEngines = null;

WebViewSelector.setEngine = function(engineId, cb) {
	if (WebViewSelector.WebViewType[engineId] === undefined) {
		return cb('unknown webview type: ' + engineId);
	}
	exec(
		function success() {
			WebViewSelector.currentEngine = engineId;
			cb();
		},
		function error(error) {
			cb(error || 'unable to save configuration');
		},
		'WebViewSelector',
		'setEngine',
		[engineId]
	);
};

function retrieveCurrentEngine(cb) {
	exec(
		function success(engineId) {
			WebViewSelector.currentEngine = engineId;
			cb && cb(null, engineId);
		}, function error(error) {
			cb && cb(error || 'unable to retrieve webView engine id');
		},
		'WebViewSelector',
		'getEngine',
		[]
	);
};

function getAvailableEngines(cb) {
	exec(
		function success(availableEngines) {
			WebViewSelector.availableEngines = availableEngines;
			cb && cb(null, availableEngines);
		}, function error(error) {
			cb && cb(error || 'unable to retrieve available engines');
		},
		'WebViewSelector',
		'getAvailableEngines',
		[]
	);
};

module.exports = WebViewSelector;

channel.onCordovaReady.subscribe(function () {
	retrieveCurrentEngine();
	getAvailableEngines();
});