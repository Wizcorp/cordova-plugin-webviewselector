# cordova-plugin-webviewselector

This plugin is designed to allow developers that are using Crosswalk with [cordova-plugin-crosswalk-webview](https://github.com/crosswalk-project/cordova-plugin-crosswalk-webview) to be able to switch dynamically between Crosswalk webview and default system webview (on Android).

#### Why anyone would want that?

Crosswalk being not maintained anymore, the future is in the use of system's webview. But on older devices you may want to keep a well working older version of Chromium. You may also want to let your application's users to switch themselves between engines for some reasons, like if your heuristic to select the best engine automatically depending on their device is not perfect.

#### How is it working?

This plugin is only changing Cordova's [webview setting](https://github.com/crosswalk-project/cordova-plugin-crosswalk-webview/blob/master/plugin.xml#L28) dynamically before the creation of the webview.

#### Compatibility

Currently tested with:
- Cordova Android Engine `4.1.1`.
- Cordova CLI `6.5.0`.

*TODO: test on more recent versions.*

# Prerequisites

You need to have [cordova-plugin-crosswalk-webview](https://github.com/crosswalk-project/cordova-plugin-crosswalk-webview) installed in your project.

# Installation

After having added the plugin to your project, you can add the following line in your config.xml (optional):
```xml
<preference name="webviewselector-xwalkMaxSdkVersion" value="23" />
```
This controls which webview engine will be used by default based on Android [API level](https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels). Up to this preference's value, Crosswalk will be used. Above, it will be system's webview.

If you omit this setting, default value is `23`: Crosswalk will be used on all devices up to Android 6.0 and system webview starting with Android 7.0.

## Possible permission conflict with other plugins

This plugin requires the WRITE_EXTERNAL_STORAGE permission on Android 4.3 (SDK version 18) and less (further versions are allowing an application to write in its own application-specific directories without it):
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
```

If you are using another plugin requiring the same permission but without (or different) condition, Cordova is not able to merge them alone, you have to manually keep the one with the larger scope. More information [here](https://github.com/Wizcorp/cordova-plugin-webviewselector/issues/3).

# Usage

`WebViewSelector` is available on: `window.cordova.plugins.WebViewSelector`.

### WebViewType

Contains available engine ids:

```javascript
WebViewType.SYSTEM
WebViewType.CROSSWALK
```

### currentEngine

Contains id of current engine:

```javascript
var ws = window.cordova.plugins.WebViewSelector;
if (ws.currentEngine === ws.WebViewType.CROSSWALK) {
	// ...
}
```

### availableEngines

Array of available engines ids. If Crosswalk is not installed it will only contain `WebViewType.SYSTEM`.

```javascript
var ws = window.cordova.plugins.WebViewSelector;
if (ws.availableEngines.length > 1) {
	// we have at least 2 engines available
}
```

### setEngine

Change webview engine. Application needs to be restarted for change to take effect, `window.location.reload()` being not enough for that. It's possible to kill the application programmatically with `navigator.app.exitApp()`.

```javascript
var ws = window.cordova.plugins.WebViewSelector;
ws.setEngine(ws.WebViewType.SYSTEM, function (error) {
	if (error) {
		return console.error(error);
	}
	alert('Kill and restart application for change to take effect.');
});
```

# Manual switch

In addition to javascript interface, user can connect his phone to a computer and drop a file named `webview.txt` containg `0` for system webview or `1` for Crosswalk inside `Internal storage/Android/data/yourAppId/files/`. This setting will prevail over any other (TODO: it should have an effect only during the first restart and be automatically deleted).