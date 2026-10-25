import re, subprocess, glob, os, zipfile
H = os.path.expanduser('~')
apk = glob.glob(H + '/lawnchair/build/outputs/apk/lawnWithQuickstepNightly/debug/*.apk')[0]
aapt2 = H + '/android-sdk/build-tools/37.0.0/aapt2'
out = subprocess.run([aapt2, 'dump', 'permissions', apk], capture_output=True, text=True).stdout
pkg = re.search(r"package: (\S+)", out).group(1)
req = sorted(set(re.findall(r"uses-permission(?:-sdk-\d+)?: name='([^']+)'", out)))
lvl = {}
paths = [H + '/pixelos/frameworks/base/core/res/AndroidManifest.xml'] + glob.glob(H + '/pixelos/frameworks/base/packages/*/AndroidManifest.xml')
for p in paths:
    try: txt = open(p, encoding='utf8').read()
    except Exception: continue
    for m in re.finditer(r'<permission\s[^>]*?android:name="([^"]+)"[^>]*?>', txt, re.S):
        pl = re.search(r'android:protectionLevel="([^"]+)"', m.group(0))
        if pl: lvl.setdefault(m.group(1), pl.group(1))
priv = [r for r in req if r in lvl and any(k in lvl[r] for k in ('privileged', 'signature', 'preinstalled', 'recents'))]
M = H + '/module'
xml = ['<?xml version="1.0" encoding="utf-8"?>', '<permissions>', '    <privapp-permissions package="%s">' % pkg]
xml += ['        <permission name="%s"/>' % r for r in priv]
xml += ['    </privapp-permissions>', '</permissions>', '']
open(M + '/system/system_ext/etc/permissions/privapp-permissions-lawnchair.xml', 'w').write('\n'.join(xml))
open(M + '/module.prop', 'w').write('\n'.join([
    'id=lawnchair_quickstep',
    'name=Lawnchair Quickstep (interruptible launch)',
    'version=16-dev-8be1c0f-merge1',
    'versionCode=1',
    'author=local',
    'description=Installs Lawnchair as the system launcher and recents provider.',
    '']))
open(M + '/customize.sh', 'w').write('''#!/system/bin/sh
# Lawnchair needs privileged permissions that the framework grants only to a priv-app that is
# listed in an allowlist. Refuse on a ROM where the allowlist would not apply.
if [ "$API" -lt 36 ]; then
  abort "Needs Android 16 or newer (API 36+), found API $API"
fi
ui_print "- Lawnchair as priv-app + recents overlay"
ui_print "- If the phone does not boot, boot to safe mode or run in recovery/adb:"
ui_print "    magisk --remove-modules"
''')
# zip with Magisk layout (module.prop at the root)
zp = H + '/lawnchair_quickstep_magisk.zip'
if os.path.exists(zp): os.remove(zp)
with zipfile.ZipFile(zp, 'w', zipfile.ZIP_DEFLATED) as z:
    for root, _, files in os.walk(M):
        for f in files:
            full = os.path.join(root, f)
            z.write(full, os.path.relpath(full, M))
print('perms in allowlist:', len(priv), 'package:', pkg)
print('zip:', zp, os.path.getsize(zp) // 1024 // 1024, 'MB')
for n in zipfile.ZipFile(zp).namelist(): print(' ', n)
