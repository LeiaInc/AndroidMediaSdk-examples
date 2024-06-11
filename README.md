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
 * androidmediasdk >= 0.5.45
 * sdk-faceTrackingService >= 0.7.69
   
 [if using MultiviewImage & LIF format]
 * androidsdk-photoformat >= 5.3.9
 * com.google.code.gson:gson:2.8.6
 
### Settings:
#### AndroidManifest

```android:extractNativeLibs="true"```


#### build.gradle

1.

```targetSdkVersion <= 30```

