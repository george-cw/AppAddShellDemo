# AppAddShellDemo
Android app add shell demo

这是一个android app 加壳的demo
根据Android 应用安全防护和逆向分析改写，由于书里的资源要在csdn上下载，或者找不到地址
故上传到github，一起学习

工程包括三个模块
源APP（app），壳APP（unshellapp），加壳工具（shelltool）

源APP是一个简单的helloworld工程，检测是否修改了签名，如果被第三方修改了则退出
有两种方式判断，java代码中和.so中都加了判断，不过用于判断的签名实际是壳的签名，源app是动态加载的

可参考理解代码：
https://blog.csdn.net/androidsecurity/article/details/8809542#commentsedit
https://blog.csdn.net/itfootball/article/details/50962459

壳app有几个地方要配置一些源app的信息：
AndroidManifest.xml的application添加：
<meta-data android:name="APPLICATION_CLASS_NAME" android:value="com.example.signatureprotect.MyApplication"/>

ProxyApplication.java
attachBaseContext函数末尾：
Object actObj = dLoader.loadClass("com.example.signatureprotect.MainActivity");
Log.i("info", "actObj"+actObj);

so的拷贝需要根据手机的abi平台去匹配，demo只是简单根据android.os.Build.CPU_ABI参数判断

shellTool工具使用：
运行main函数
File payloadSrcFile = new File("signature_1.0-debug.apk");//源app
File unShellDexFile = new File("unshellapp.dex");//壳app里解压的classes.dex
生成classes.dex, 替换壳app里的classes.dex，生成新的unshellapp-debug.apk
重新签名(可在梆梆加固下载个签名工具)
