//
// Created by chang on 2018/12/28.
//

//
// Created by chang on 2018/4/13.
//

#include <stdio.h>
#include <stdlib.h>
#include "com_example_SignatureProtect_SignatureJni.h"
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include<dlfcn.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "Decode-jni-call", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "Decode-jni-call", __VA_ARGS__))

#ifdef __cplusplus
extern "C" {
#endif
const char *app_sig = "308202c9308201b1a003020102020421c3ff0d300d06092a864886f70d01010b0500301531133011060355040b130a6f70656e736f75726365301e170d3139303131373032343832305a170d3434303131313032343832305a301531133011060355040b130a6f70656e736f7572636530820122300d06092a864886f70d01010105000382010f003082010a0282010100d085309d44d34d80af0809e01895898575dbe6be0706f602d0313465da762c9baae5e8e8e5c97d57fa8ce08fc15aaf98246e6c00d5fafa5f7da02b23846421a19420e430f4329435ad36a755ce0c3089c07ce42b479e6a2e75e9854c20314de3ed52bef6c0558b7e111f0baa68268e6f2c31c6d12927c33b31c995c3a972b805c3ac1c403f3f2f8d6c0c4cb37d5235df0199d84a0ecb6f849e0e0fb323940f52a282ee4875186c65b64e825bb2bbbcdcf8d39ea0224ab41d8582bfd3614794f0f5d4cfb9a7f02296df83f6e14a30095237ac974756a41a717994fc68e48c2bbd93a7515e267e5347443d685b2914c183fe63f8980669d13de535ebb1a060ae950203010001a321301f301d0603551d0e04160414251714a6b92983b7d7c2d47405be7d25fbe4c02e300d06092a864886f70d01010b050003820101001ed7d98baaad210390e6de436c7579f9b5186ee6c02bd920470f470774e7ff8d294c6e99a1cf3da52dae151cc427e1978cfcf42020b0f95f558b4f6fcefb75fbab1725c72e04572e92933e7222b749254116cba672cbe7867015faeffdc4766514d110e04f3c5179ac213fb28b3e57365f406c2651ac631e156c8a60d74ed36cf14e322a47b459c8d8cc88ac95e2d5903143d6b582aa35a09d8e2b9f1953211508c320de2c76d7ad6613fcac2a2e618aa69667f4672299b9fe7e60a0d5888a5659b8608cd0cd74856e46f22466a0704aea8afb458c5e069ae0351ec9786c695d8d19c0fb9e8af443feb7b31c382604e970b49af031693987bd32b1fd9c281035";

JNIEXPORT jboolean JNICALL Java_com_example_signatureprotect_SignatureJni_isEqual
  (JNIEnv *env, jclass jcla, jstring sig)
  {
        char *className = "com/example/signatureprotect/MainActivity";
        jclass clazz = (env)->FindClass(className);
        if (clazz == NULL)
        {
            LOGI("do not find class '%s'", className);
            return false;
        }
        LOGI("find class '%s'", className);
        jmethodID method = (env)->GetStaticMethodID( clazz, "getSignature", "()Ljava/lang/String;");
        if (method == NULL)
        {
             LOGI("do not find method");
             return false;
        }

        LOGI("find method");
        jstring obj = (jstring)(env)->CallStaticObjectMethod( clazz, method);
        if (obj == NULL){
            LOGI("invoke error: %p", obj);
            return false;
        }

        LOGI("invoke method");
        const char *str = (env)->GetStringUTFChars(obj, 0);
        LOGI("str %s", str);
        int cmpval = strcmp(str, app_sig);
        LOGI("strcmp pass");
        if (cmpval == 0)
        {
            LOGI("equal return true");
            return true;
        }
        (env)->ReleaseStringUTFChars(obj,str);
        LOGI("equal return false");
        return false;
  }

  jint JNI_OnLoad(JavaVM* vm, void* reserved)
  {
      JNIEnv* env = NULL;
      jint result = -1;

    LOGI("JNI_OnLoad");
      if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
          LOGI("ERROR: GetEnv failed\n");
          goto bail;
      }
      if(env == NULL)
            return result;

      /* success -- return valid version number */
      result = JNI_VERSION_1_4;
      if (!Java_com_example_signatureprotect_SignatureJni_isEqual(env, NULL, NULL))
      {
            char *className = "com/example/signatureprotect/MainActivity";
            jclass clazz = (env)->FindClass(className);
            if (clazz == NULL)
            {
                 LOGI("do not find class '%s'", className);
                 return false;
            }
            jmethodID method = (env)->GetStaticMethodID( clazz, "killMyself", "()V");
            if (method == NULL)
            {
                LOGI("do not find method");
                return result;
           }

           LOGI("find method");
           (env)->CallStaticVoidMethod( clazz, method);
      }

  bail:
      return result;
  }

#ifdef __cplusplus
}
#endif
