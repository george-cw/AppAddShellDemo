package com.example.signatureprotect;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String APP_SIGN = "308202c9308201b1a003020102020421c3ff0d300d06092a864886f70d01010b0500301531133011060355040b130a6f70656e736f75726365301e170d3139303131373032343832305a170d3434303131313032343832305a301531133011060355040b130a6f70656e736f7572636530820122300d06092a864886f70d01010105000382010f003082010a0282010100d085309d44d34d80af0809e01895898575dbe6be0706f602d0313465da762c9baae5e8e8e5c97d57fa8ce08fc15aaf98246e6c00d5fafa5f7da02b23846421a19420e430f4329435ad36a755ce0c3089c07ce42b479e6a2e75e9854c20314de3ed52bef6c0558b7e111f0baa68268e6f2c31c6d12927c33b31c995c3a972b805c3ac1c403f3f2f8d6c0c4cb37d5235df0199d84a0ecb6f849e0e0fb323940f52a282ee4875186c65b64e825bb2bbbcdcf8d39ea0224ab41d8582bfd3614794f0f5d4cfb9a7f02296df83f6e14a30095237ac974756a41a717994fc68e48c2bbd93a7515e267e5347443d685b2914c183fe63f8980669d13de535ebb1a060ae950203010001a321301f301d0603551d0e04160414251714a6b92983b7d7c2d47405be7d25fbe4c02e300d06092a864886f70d01010b050003820101001ed7d98baaad210390e6de436c7579f9b5186ee6c02bd920470f470774e7ff8d294c6e99a1cf3da52dae151cc427e1978cfcf42020b0f95f558b4f6fcefb75fbab1725c72e04572e92933e7222b749254116cba672cbe7867015faeffdc4766514d110e04f3c5179ac213fb28b3e57365f406c2651ac631e156c8a60d74ed36cf14e322a47b459c8d8cc88ac95e2d5903143d6b582aa35a09d8e2b9f1953211508c320de2c76d7ad6613fcac2a2e618aa69667f4672299b9fe7e60a0d5888a5659b8608cd0cd74856e46f22466a0704aea8afb458c5e069ae0351ec9786c695d8d19c0fb9e8af443feb7b31c382604e970b49af031693987bd32b1fd9c281035";
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        Log.d(TAG, "signature protect MainActivity onCreate");
        Log.d(TAG, getSignature());

        if (!isOwnApp() || !isOwnApp2()){
            Log.d(TAG, "is not own app exit");
            killMyself();
        }
    }

    public static void killMyself(){
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static String getSignature(){
        Context context = mContext;
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            Signature[]signatures = packageInfo.signatures;
            StringBuilder stringBuilder = new StringBuilder();
            for(Signature signature : signatures){
                stringBuilder.append(signature.toCharsString());
            }
            return stringBuilder.toString();
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return "";
    }

    private boolean isOwnApp(){
        if (APP_SIGN.equalsIgnoreCase(getSignature())){
            return true;
        }
        return false;
    }

    private boolean isOwnApp2(){
        if (SignatureJni.isEqual("")){
            return true;
        }
        return false;
    }
}
