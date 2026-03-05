#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_il_pacolo_com_news_security_NativeKeys_getWeatherApiKey(
        JNIEnv* env, jobject) {

    // Split the key so it's not found by simple string search in the .so binary
    std::string p1 = "97fb0baa";
    std::string p2 = "d55faa30";
    std::string p3 = "8a3ff5cc";
    std::string p4 = "c0a2d19e";

    return env->NewStringUTF((p1 + p2 + p3 + p4).c_str());
}