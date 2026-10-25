set -e
M=$HOME/module
mkdir -p $M/META-INF/com/google/android
echo '#MAGISK' > $M/META-INF/com/google/android/updater-script
cat > $M/META-INF/com/google/android/update-binary <<'EOS'
#!/sbin/sh
#################
# Initialization
#################
umask 022
ui_print() { echo "$1"; }
require_new_magisk() {
  ui_print "*******************************"
  ui_print " Please install Magisk v20.4+! "
  ui_print "*******************************"
  exit 1
}
#########################
# Load util_functions.sh
#########################
OUTFD=$2
ZIPFILE=$3
mount /data 2>/dev/null
[ -f /data/adb/magisk/util_functions.sh ] || require_new_magisk
. /data/adb/magisk/util_functions.sh
[ $MAGISK_VER_CODE -lt 20400 ] && require_new_magisk
install_module
exit 0
EOS
chmod 755 $M/META-INF/com/google/android/update-binary $M/customize.sh
cd $M && rm -f ../lawnchair_quickstep_magisk.zip && zip -qr9 ../lawnchair_quickstep_magisk.zip META-INF module.prop customize.sh system
unzip -l ../lawnchair_quickstep_magisk.zip | tail -12
sha256sum ../lawnchair_quickstep_magisk.zip
