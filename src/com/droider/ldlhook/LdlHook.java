package com.droider.ldlhook;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class LdlHook implements IXposedHookLoadPackage{

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals("cn.ledongli.ldl")){
			return;
		}
		
		final Class<?> dataOutputStream = findClass("java.io.DataOutputStream", lpparam.classLoader);
	    XposedBridge.hookAllMethods(dataOutputStream, "write", new XC_MethodHook() {
			@Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				String content = new String((byte[]) param.args[0]);
				Pattern pattern = Pattern.compile(".*steps%22%3A\\d+%2C.*");
		        Matcher matcher = pattern.matcher(content);
		        if (matcher.matches()){
		        	content = content.replaceAll("steps%22%3A\\d+%2C", "steps%22%3A100000%2C");
		        	URL url = new URL("http://pl.api.ledongli.cn/xq/io.ashx");
		        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		            conn.setRequestProperty("connection", "Keep-Alive");
		        	conn.setRequestMethod("POST");
		        	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		        	conn.setRequestProperty("charset", "utf-8");
		        	conn.setUseCaches(false);
		        	conn.setDoOutput(true);
		            conn.setDoInput(true);
		        	PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
		        	printWriter.print(content);
		        	printWriter.flush();
		        	printWriter.close();
		        	conn.getInputStream();
		        	param.args[0] = null;
		        }
			}
		});
	}

}
