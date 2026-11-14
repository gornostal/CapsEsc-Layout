# capsesc-layout

A tiny Android app that makes Caps Lock send Escape on a physical keyboard,
system-wide, without root.

## Problem

- Keyboard: Baseus Brilliance Series magnetic keyboard case (iPad Mini 6 size),
  paired over Bluetooth. It has no Esc key.
- Tablet: Onyx Boox Go 7, Android 13, Boox firmware 4.2.
- Use case: Termius (ssh) and vim, where Esc is essential.

What did not work:

- **Reprogramming the keyboard.** Its USB-C port is charge-only (it never
  enumerates on USB), and firmware remapping was a dead end.
- **Key Mapper (accessibility-service remap).** It sees the key after Android
  has already handled it as Caps Lock, so the Caps Lock toggle leaks through
  alongside Esc.
- **adb.** Not needed in the end. The Boox settings UI hides both the
  physical-keyboard screen and developer options.

## Solution

Android lets any app ship extra physical-keyboard layouts. A layout file (KCM)
can remap a key before the system interprets it, so Android never sees a
Caps Lock press at all.

- `res/raw/capsesc_us.kcm` is an overlay on the stock US layout with two lines
  mapping Caps Lock to `ESCAPE`: one by Linux scancode (58), one by HID usage
  (0x070039).
- `res/xml/keyboard_layouts.xml` declares the layout
  "English (US), Caps Lock = Esc".
- `LayoutReceiver` is an empty receiver. The system never calls it; it only
  reads its meta-data to discover the layout.
- `MainActivity` exists because Boox hides the relevant settings. It has
  buttons that open the physical-keyboard settings, developer options and the
  About screen, plus a key tester that shows keycode, scancode, Caps Lock
  state and the source device.

The app requests no permissions.

## Build

Requires the Android SDK (build-tools and a platform `android.jar`), a JDK,
and a keystore. Defaults match the machine it was written on; override with
`ANDROID_SDK`, `BUILD_TOOLS`, `PLATFORM`, `KEYSTORE`, `KS_PASS`.

    ./build.sh

Output: `capsesc-layout.apk` in the project root (git-ignored). There is no
Gradle project; the script calls aapt2, javac, d8, zipalign and apksigner
directly.

## Install and activate

1. Copy the APK to the tablet (MTP works without USB debugging) and install it
   from the file manager.
2. Connect the keyboard over Bluetooth.
3. Open the "Caps to Esc" app, tap "Open physical keyboard settings", tap the
   keyboard's name, and choose "English (US), Caps Lock = Esc".
4. Press Caps Lock in the app's key tester: it should show `KEYCODE_ESCAPE`
   with Caps Lock state off.

## Things to know

- The app must stay installed; the layout lives inside it.
- The layout choice is stored per keyboard. Re-pairing or a firmware update
  may require picking it again.
- Caps Lock is no longer available on a keyboard using this layout.
- The layout is US English only. Other languages need their own KCM file and
  an extra `<keyboard-layout>` entry.
- To remap more keys, add `map key <scancode> <KEYCODE>` lines to the KCM,
  bump `versionCode` in `AndroidManifest.xml`, rebuild and reinstall. Use the
  key tester to find scancodes.
- Android 14 and later changed the layout picker UI; this was only verified on
  Android 13.
- On Boox, the USB debug toggle is reportedly under Apps > top-right menu >
  App Management, not under Settings. That path was not confirmed on this
  device.
