# LawnChoir

LawnChoir is a **fork of [Lawnchair](https://github.com/LawnchairLauncher/lawnchair)** (branch `16-dev`). It keeps the full Lawnchair history and code, and adds changes on top so that Lawnchair can run as the system launcher (Quickstep) on a ROM that was not built with it, and so that app launch and close animations can be interrupted at any moment.

It is not affiliated with the Lawnchair project. All credit for Lawnchair goes to its authors and contributors; this fork only carries the changes described below. Lawnchair's license (GPL-3.0, see `LICENSE.txt`) applies unchanged.

## Disclaimer

**Use this at your own risk.** This software installs a Magisk module that adds a privileged system app, replaces the system launcher and changes how the system draws and animates windows. It is provided as is, without any warranty of any kind, as stated in the GPL-3.0 license. The author and the contributors of this fork are not responsible for any damage, data loss, boot loop, bricked device, voided warranty, broken banking or Play Integrity checks, or any other consequence of installing, using or building it.

Back up your data and your original `boot.img` before you install anything, and make sure you know how to flash it back with `fastboot`. If you do not know what any of this means, do not install it.

## What it does

- **Interruptible app animations.** Opening an app from its icon can be interrupted by Home, Back or a swipe up, and the window closes from where it is instead of jumping to full screen. Closing an app and opening another icon in the middle of the close animation works as well, and the chain open, close, open can be repeated without a black screen.
- **Runs as system launcher on PixelOS 17 (Android 17), without building Android.** It is installed as a privileged system app by a Magisk module, with an overlay that makes it the recents component. Upstream `16-dev` is compiled against the Android 16 hidden APIs, so the fork contains the fixes for what changed in Android 17 (see below).
- Recents work from Home, from inside an app (live tile) and from the swipe up and hold gesture, with previews, Screenshot, Lens and Share.

Tested on a Nothing Phone (1) with official PixelOS 17 and Magisk. Other devices and ROMs are untested.

## Android 17 port notes

The details are in [`lawnchoir/docs/GUIDA-MODULO-LAWNCHAIR.md`](lawnchoir/docs/GUIDA-MODULO-LAWNCHAIR.md) (in Italian). In short:

- `IShellTransitions` has one more method on PixelOS 17, so every transaction code is the Android 16 one plus one, and `registerRemote` takes only the `RemoteTransition` (the filter is inside it). The calls are made by hand in `SystemUiProxy.kt`. Without this the Home transition filter is never registered.
- `IRecentsAnimationRunner.onAnimationStart` and `ISystemUiProxy.onBackEvent` changed their parameters, and are read and written by hand.
- Private framework resources, desktop mode flags and aconfig flags that moved or disappeared are replaced by local values or shims.
- The recents task overlay (`TaskContainer`, thumbnail cache, `TaskOverlayFactoryImpl`) needed fixes for the previews, Screenshot and Lens.
- The animation runners were changed to merge an interrupting close into a playing launch, to end the playing animation before the next one starts, and to queue a close that arrives before the launch animation has begun.

## Building and installing

1. Build the APK: `./gradlew assembleLawnWithQuickstepNightlyDebug` (JDK 21, Android SDK with platform 37 and build-tools 37.0.0).
2. Build the Magisk module with the scripts in [`lawnchoir/module/`](lawnchoir/module). The module contains the APK as a privileged system app, the privileged permission list, a recents overlay and a boot-loop guard that disables the module after three failed boots.
3. Install the module from Magisk, reboot, and pick the launcher as the default Home app.

Read the guide before installing. Keep a copy of your `boot.img` so that you can recover if the phone does not boot.

## Syncing with upstream

```
git remote add upstream https://github.com/LawnchairLauncher/lawnchair
git fetch upstream
git merge upstream/16-dev
```

## License

GPL-3.0, as Lawnchair. See `LICENSE.txt`.
