/**
 * ProxyApplication.java
 * @author f0rest
 *
 * 2014-6-15
 */
package com.example.unshellapp;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexClassLoader;

public class ProxyApplication extends Application {

	private static final String appkey = "APPLICATION_CLASS_NAME";
	private String apkFileName;
	private String odexPath;
	private String libPath;
	private static Context mContext;

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		mContext = base;
		Log.i("info", "ProxyApplilcation attachBaseContext()");
		try {
			File odex = this.getDir("payload_odex", MODE_PRIVATE);
			File libs = this.getDir("payload_lib", MODE_PRIVATE);
			odexPath = odex.getAbsolutePath();
			libPath = libs.getAbsolutePath();
			apkFileName = odex.getAbsolutePath() + "/payload.apk";
			Log.i("info", "path: " + apkFileName);
			File dexFile = new File(apkFileName);
			if (!dexFile.exists())
				dexFile.createNewFile();
			byte[] dexdata = this.readDexFileFromApk();
			Log.i("info", "classes.dex's byte.length=" + dexdata.length);
			//FileOutputStream out = new FileOutputStream(dexFile);
			//out.write(dexdata);
			//out.flush();
			//out.close();
			this.splitPayLoadFromDex(dexdata);
			Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", new Class[] {},
					new Object[] {});
			String packageName = this.getPackageName();
			WeakReference wr = null;
			if (Build.VERSION.SDK_INT >= 19) {
				ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect("android.app.ActivityThread", currentActivityThread, "mPackages");
				wr = (WeakReference) mPackages.get(packageName);

			} else {
				HashMap mPackages = (HashMap) RefInvoke.getFieldOjbect("android.app.ActivityThread", currentActivityThread, "mPackages");
				wr = (WeakReference) mPackages.get(packageName);
			}
			if (wr != null) {
				DexClassLoader dLoader = new DexClassLoader(apkFileName, odexPath, libPath, (ClassLoader) RefInvoke.getFieldOjbect(
						"android.app.LoadedApk", wr.get(), "mClassLoader"));
				RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader", wr.get(), dLoader);
				Object actObj = dLoader.loadClass("com.example.signatureprotect.MainActivity");
				Log.i("info", "actObj"+actObj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		{
			Log.i("info", "ProxyApplilcation oncreate()");
			// ���ԴӦ��������Appliction�������滻ΪԴӦ��Applicaiton���Ա㲻Ӱ��Դ�����߼���
			String appClassName = null;
			try {
				Log.i("info", getPackageName());
				ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				if (bundle != null && bundle.containsKey("APPLICATION_CLASS_NAME")) {
					appClassName = bundle.getString("APPLICATION_CLASS_NAME");
					Log.i("info", appClassName);
				} else {
					Log.i("info", "return");
					return;
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("info", "exception");
			}
			Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", new Class[] {},
					new Object[] {});
			Object mBoundApplication = RefInvoke.getFieldOjbect("android.app.ActivityThread", currentActivityThread, "mBoundApplication");
			Object loadedApkInfo = RefInvoke.getFieldOjbect("android.app.ActivityThread$AppBindData", mBoundApplication, "info");
			Log.i("info", loadedApkInfo.toString());
			RefInvoke.setFieldOjbect("android.app.LoadedApk", "mApplication", loadedApkInfo, null);
			Object oldApplication = RefInvoke.getFieldOjbect("android.app.ActivityThread", currentActivityThread, "mInitialApplication");
			Log.i("info", oldApplication.toString());
			ArrayList<Application> mAllApplications = (ArrayList<Application>) RefInvoke.getFieldOjbect("android.app.ActivityThread",
					currentActivityThread, "mAllApplications");
			mAllApplications.remove(oldApplication);
			ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvoke.getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
					"mApplicationInfo");
			ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvoke.getFieldOjbect("android.app.ActivityThread$AppBindData",
					mBoundApplication, "appInfo");
			appinfo_In_LoadedApk.className = appClassName;
			appinfo_In_AppBindData.className = appClassName;
			Application app = (Application) RefInvoke.invokeMethod("android.app.LoadedApk", "makeApplication", loadedApkInfo, new Class[] {
					boolean.class, Instrumentation.class }, new Object[] { false, null });
			Log.i("info", app.toString());
			RefInvoke.setFieldOjbect("android.app.ActivityThread", "mInitialApplication", currentActivityThread, app);
			// Exception ArrayMap can't cast to HashMap
			ArrayMap mProviderMap = (ArrayMap) RefInvoke.getFieldOjbect("android.app.ActivityThread", currentActivityThread, "mProviderMap");
			Iterator it = mProviderMap.values().iterator();
			while (it.hasNext()) {
				Object providerClientRecord = it.next();
				Object localProvider = RefInvoke.getFieldOjbect("android.app.ActivityThread$ProviderClientRecord", providerClientRecord,
						"mLocalProvider");
				Log.i("localProvider", "localProvider " + localProvider);
				if (localProvider != null)
				RefInvoke.setFieldOjbect("android.content.ContentProvider", "mContext", localProvider, app);
			}
			app.onCreate();
		}
	}

	int fromByteArray(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	private void splitPayLoadFromDex(byte[] data) throws IOException {
		byte[] apkdata = decrypt(data);
		int ablen = apkdata.length;
		byte[] dexlen = new byte[4];
		System.arraycopy(apkdata, ablen - 4, dexlen, 0, 4);
		ByteArrayInputStream bais = new ByteArrayInputStream(dexlen);
		DataInputStream in = new DataInputStream(bais);
		int readInt = in.readInt();
		System.out.println(fromByteArray(dexlen));
		System.out.println(Arrays.toString(dexlen));
		System.out.println(Integer.toHexString(readInt));
		byte[] newdex = new byte[readInt];
		System.arraycopy(apkdata, ablen - 4 - readInt, newdex, 0, readInt);
		File file = new File(apkFileName);
		try {
			FileOutputStream localFileOutputStream = new FileOutputStream(file);
			localFileOutputStream.write(newdex);
			localFileOutputStream.close();
		} catch (IOException localIOException) {
			throw new RuntimeException(localIOException);
		}

		ZipInputStream localZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
		while (true) {
			ZipEntry localZipEntry = localZipInputStream.getNextEntry();
			if (localZipEntry == null) {
				localZipInputStream.close();
				break;
			}
			String name = localZipEntry.getName();
			Log.i("name ", name);
			String abi = "lib";
			String CPU_ABI = android.os.Build.CPU_ABI;
			Log.d("abi", "CPU_ABI = " + CPU_ABI);
			if (isART64() || CPU_ABI.contains("arm64")){
				abi = "lib/arm64-v8a";
			} else {
				abi = "lib/armeabi-v7a";
			}
			Log.i("abi","abi path: " + abi);
			if (name.startsWith(abi) && name.endsWith(".so")) {
				File storeFile = new File(libPath + "/" + name.substring(name.lastIndexOf('/')));
				storeFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(storeFile);
				byte[] arrayOfByte = new byte[1024];
				while (true) {
					int i = localZipInputStream.read(arrayOfByte);
					if (i == -1)
						break;
					fos.write(arrayOfByte, 0, i);
				}
				fos.flush();
				fos.close();
			}
			localZipInputStream.closeEntry();
		}
		localZipInputStream.close();

	}

	private byte[] readDexFileFromApk() throws IOException {
		ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
		ZipInputStream localZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(this.getApplicationInfo().sourceDir)));
		Log.i("info", this.getApplicationInfo().sourceDir);
		while (true) {
			ZipEntry localZipEntry = localZipInputStream.getNextEntry();
			if (localZipEntry == null) {
				localZipInputStream.close();
				break;
			}
			if (localZipEntry.getName().equals("classes.dex")) {
				Log.i("info", "readDexFileFromApk find classes.dex");
				byte[] arrayOfByte = new byte[1024];
				while (true) {
					int i = localZipInputStream.read(arrayOfByte);
					if (i == -1)
						break;
					dexByteArrayOutputStream.write(arrayOfByte, 0, i);
				}
			}
			localZipInputStream.closeEntry();
		}
		localZipInputStream.close();
		return dexByteArrayOutputStream.toByteArray();
	}

	// //ֱ�ӷ������ݣ����߿�������Լ����ܷ���
	private byte[] decrypt(byte[] data) {
		for(int i=0; i < data.length; i++){
			data[i] = (byte)(0xFF ^ data[i]);
		}
		return data;
	}

	private static boolean isART64() {

		final String tag = "is64ART";
		final String fileName = "art";

		try {
			ClassLoader classLoader = mContext.getClassLoader();
			Class<?> cls = ClassLoader.class;
			Method method = cls.getDeclaredMethod("findLibrary", String.class);
			Object object = method.invoke(classLoader, fileName);
			if (object != null) {
				return ((String)object).contains("lib64");
			}
		} catch (Exception e) {
			//如果发生异常就用方法②
			return is64bitCPU();
		}

		return false;
	}

	private static boolean is64bitCPU() {
		String CPU_ABI = null;
		if (Build.VERSION.SDK_INT >= 21) {
			String[] CPU_ABIS = Build.SUPPORTED_ABIS;
			if (CPU_ABIS.length > 0) {
				CPU_ABI = CPU_ABIS[0];
			}
		} else {
			CPU_ABI = Build.CPU_ABI;
		}

		if (CPU_ABI != null && CPU_ABI.contains("arm64")) {
			return true;
		}

		return false;
	}
}