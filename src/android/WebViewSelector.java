// Usage: put this line as first line of "public static CordovaWebViewEngine createEngine" in CordovaWebViewImpl.java:
// WebViewSelector.updateWebViewPreference(context, preferences);
// You can also add a line in your config.xml to determine the maximum API on which crosswalk should be used:
// <preference name="webviewselector-xwalkMaxSdkVersion" value="23" />

package org.apache.cordova;

import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class WebViewSelector extends CordovaPlugin {
	public enum WebViewType {
		SYSTEM,
		CROSSWALK
	};

	private static final String TAG = "WebViewSelector";
	private static int crosswalkMaxSdkVersion = 23; // API 23 = Android 6.0, API 24 = Android 7.0
	private static String systemWebViewClass;
	private static String crosswalkWebViewClass;
	private static WebViewType currentWebView;

	private static String getInAppConfigFilePath(Context context) {
		return context.getFilesDir().getAbsolutePath() + "/" + "webview.txt";
	}

	private static String getUserConfigFilePath(Context context) {
		return context.getExternalFilesDir(null).getAbsolutePath() + "/" + "webview.txt";
	}

	private static boolean isCrosswalkInstalled () {
		return !crosswalkWebViewClass.equals(systemWebViewClass);
	}

	private static String readValueFromFile(String filePath) {
		String aBuffer = "";
		try {
			File file = new File(filePath);
			if (!file.exists()) { return ""; }
			FileInputStream fIn = new FileInputStream(file);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
			String aDataRow = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow;
			}
			myReader.close();
			return aBuffer;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static boolean setEngine(Context context, WebViewType webViewType) {
		String filePath = getInAppConfigFilePath(context);
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				if (!file.createNewFile()) { return false; }
			} catch (java.io.IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write("");
			writer.append(Integer.toString(webViewType.ordinal()));
			writer.close();
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static String getWebViewClassFromType(WebViewType webViewType) {
		if (webViewType == WebViewType.SYSTEM) {
			return systemWebViewClass;
		} else if (webViewType == WebViewType.CROSSWALK) {
			return crosswalkWebViewClass;
		} else {
			return "";
		}
	}

	private static JSONArray getAvailableEngines(Context context) {
		JSONArray availableEngines = new JSONArray();
		availableEngines.put(WebViewType.SYSTEM.toString());
		if (isCrosswalkInstalled()) {
			availableEngines.put(WebViewType.CROSSWALK.toString());
		}
		return availableEngines;
	}

	public static void updateWebViewPreference(Context context, CordovaPreferences preferences) {
		systemWebViewClass = SystemWebViewEngine.class.getCanonicalName();
		crosswalkWebViewClass = preferences.getString("webview", systemWebViewClass);
		crosswalkMaxSdkVersion = preferences.getInteger("webviewselector-xwalkMaxSdkVersion", crosswalkMaxSdkVersion);
		currentWebView = determineWebViewEngine(context);
		preferences.set("webview", getWebViewClassFromType(currentWebView));
	}

	private static WebViewType determineWebViewEngine(Context context) {
		if (!isCrosswalkInstalled()) {
			return WebViewType.SYSTEM;
		}

		String customValue;

		// Checking if we have a custom value set from outside the app
		customValue = readValueFromFile(getUserConfigFilePath(context));
		if (customValue.equals(Integer.toString(WebViewType.SYSTEM.ordinal()))) {
			return WebViewType.SYSTEM;
		} else if (customValue.equals(Integer.toString(WebViewType.CROSSWALK.ordinal()))) {
			return WebViewType.CROSSWALK;
		}

		// Checking if we have a custom value set from inside the app
		customValue = readValueFromFile(getInAppConfigFilePath(context));
		if (customValue.equals(Integer.toString(WebViewType.SYSTEM.ordinal()))) {
			return WebViewType.SYSTEM;
		} else if (customValue.equals(Integer.toString(WebViewType.CROSSWALK.ordinal()))) {
			return WebViewType.CROSSWALK;
		}

		if (Build.VERSION.SDK_INT > crosswalkMaxSdkVersion) {
			return WebViewType.SYSTEM;
		} else {
			return WebViewType.CROSSWALK;
		}
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		Context context = cordova.getActivity().getApplicationContext();
		if (action.equals("setEngine")) {
			String error = "";
			String webViewTypeId = args.getString(0);
			WebViewType webViewType = currentWebView;
			try {
				webViewType = WebViewType.valueOf(webViewTypeId);
			} catch (IllegalArgumentException e) {
				error = "unknown webView type: " + webViewTypeId;
				e.printStackTrace();
			}
			if (error.equals("")) {
				boolean success = setEngine(context, webViewType);
				if (!success) {
					error = "unable to save configuration";
				}
			}
			PluginResult.Status status = error.equals("") ? PluginResult.Status.OK : Status.ERROR;
			final PluginResult result = new PluginResult(status, error);
			callbackContext.sendPluginResult(result);
		} else if (action.equals("getEngine")) {
			final PluginResult result = new PluginResult(PluginResult.Status.OK, currentWebView.toString());
			callbackContext.sendPluginResult(result);
		} else if (action.equals("getAvailableEngines")) {
			final PluginResult result = new PluginResult(PluginResult.Status.OK, getAvailableEngines(context));
			callbackContext.sendPluginResult(result);
		}
		return true;
	}
}