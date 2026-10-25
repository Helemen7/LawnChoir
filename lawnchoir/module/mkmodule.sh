set -e
SDK=$HOME/android-sdk
BT=$SDK/build-tools/37.0.0
JAR=$SDK/platforms/android-37.0/android.jar
PKG=app.lawnchair.nightly.debug
APK=$(ls $HOME/lawnchair/build/outputs/apk/lawnWithQuickstepNightly/debug/*.apk | head -1)
M=$HOME/module
rm -rf $M $HOME/ovl && mkdir -p $HOME/ovl/res/values $M
# --- overlay (static RRO on the framework)
cat > $HOME/ovl/AndroidManifest.xml <<XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="app.lawnchair.overlay.recents">
    <overlay android:targetPackage="android"
             android:isStatic="true"
             android:priority="200"/>
</manifest>
XML
cat > $HOME/ovl/res/values/config.xml <<XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="config_recentsComponentName" translatable="false">$PKG/com.android.quickstep.RecentsActivity</string>
</resources>
XML
$BT/aapt2 compile --dir $HOME/ovl/res -o $HOME/ovl/compiled.zip
$BT/aapt2 link -o $HOME/ovl/unsigned.apk -I $JAR --manifest $HOME/ovl/AndroidManifest.xml --min-sdk-version 34 --target-sdk-version 37 $HOME/ovl/compiled.zip
[ -f $HOME/.android/debug.keystore ] || { mkdir -p $HOME/.android; keytool -genkeypair -keystore $HOME/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1; }
$BT/apksigner sign --ks $HOME/.android/debug.keystore --ks-pass pass:android --key-pass pass:android --out $HOME/ovl/LawnchairRecentsOverlay.apk $HOME/ovl/unsigned.apk
# --- module tree
mkdir -p $M/system/system_ext/priv-app/Lawnchair $M/system/system_ext/etc/permissions $M/system/product/overlay/LawnchairRecentsOverlay
cp "$APK" $M/system/system_ext/priv-app/Lawnchair/Lawnchair.apk
cp $HOME/ovl/LawnchairRecentsOverlay.apk $M/system/product/overlay/LawnchairRecentsOverlay/
echo OVERLAY_OK
