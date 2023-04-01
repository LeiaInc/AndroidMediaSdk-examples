# AndroidMediaSdk-examples
This examples showing basic functions AndroidMediaSDK:
* playing 3d stereo video 
* playing mono video in 3d with automatic conversion

## How to build 
1. Copy Leia Libraries into each project /libs folder
2. Either run `./gradlew build` or import into AndroidStudio and build

## Leia AndroidMediaSDK app basics
For Android app to be able to use AndroidMediaSDK it needs certian libraries and settings

### Libraries:
 * androidmediasdk-0.1.xx
 * snpe-1.64.0
 * sdk-faceTrackingService-0.6.xxx
 
### Settings:
#### AndroidManifest

```android:extractNativeLibs="true"```


#### build.gradle

1.

```targetSdkVersion <= 30```

2.

```android {
    packagingOptions {
        pickFirst 'lib/x86/libc++_shared.so'
        pickFirst 'lib/x86_64/libc++_shared.so'
        pickFirst 'lib/armeabi-v7a/libc++_shared.so'
        pickFirst 'lib/arm64-v8a/libc++_shared.so'
        doNotStrip "*/armeabi-v7a/libsnpe_*.so"
        doNotStrip "*/arm64-v8a/libsnpe_*.so"
    }```
