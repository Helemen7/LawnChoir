set -e
M=$HOME/module
cat > $M/post-fs-data.sh <<'EOS'
#!/system/bin/sh
# Boot-loop guard. Counts boots that did not reach boot_completed (service.sh resets the
# counter once the system is up) and disables this module after 3 failed boots in a row.
MODDIR=${0%/*}
CNT=/data/adb/lawnchair_quickstep_boot_attempts
n=$(cat "$CNT" 2>/dev/null)
[ -z "$n" ] && n=0
n=$((n + 1))
echo "$n" > "$CNT"
if [ "$n" -gt 3 ]; then
  touch "$MODDIR/disable"
  echo 0 > "$CNT"
fi
EOS
cat > $M/service.sh <<'EOS'
#!/system/bin/sh
# Reached once the system is up: the last boot worked, so reset the boot-loop guard.
CNT=/data/adb/lawnchair_quickstep_boot_attempts
until [ "$(getprop sys.boot_completed)" = "1" ]; do sleep 5; done
sleep 30
echo 0 > "$CNT"
EOS
chmod 755 $M/post-fs-data.sh $M/service.sh $M/customize.sh $M/META-INF/com/google/android/update-binary
cd $M && rm -f ../lawnchair_quickstep_magisk.zip && zip -qr9 ../lawnchair_quickstep_magisk.zip META-INF module.prop customize.sh post-fs-data.sh service.sh system
unzip -l ../lawnchair_quickstep_magisk.zip | grep -E "post-fs|service|module.prop|Lawnchair.apk|permissions"
sha256sum ../lawnchair_quickstep_magisk.zip
