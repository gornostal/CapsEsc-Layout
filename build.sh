#!/usr/bin/env bash
# Builds and signs capsesc-layout.apk using the Android SDK command-line tools.
# Overridable: ANDROID_SDK, BUILD_TOOLS, PLATFORM, KEYSTORE, KS_PASS.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
sdk="${ANDROID_SDK:-$HOME/Android/Sdk}"
bt="$sdk/build-tools/${BUILD_TOOLS:-36.1.0}"
jar="$sdk/platforms/${PLATFORM:-android-36}/android.jar"
ks="${KEYSTORE:-$HOME/.android/debug.keystore}"
ks_pass="${KS_PASS:-android}"
out="$here/build"

# If javac is not on PATH, use the JDK that the `java` on PATH belongs to.
if ! command -v javac >/dev/null; then
    PATH="$(dirname "$(readlink -f "$(command -v java)")"):$PATH"
fi

mkdir -p "$out/classes"
"$bt/aapt2" compile --dir "$here/res" -o "$out/res.zip"
"$bt/aapt2" link -o "$out/unsigned.apk" -I "$jar" --manifest "$here/AndroidManifest.xml" "$out/res.zip"
javac --release 8 -nowarn -cp "$jar" -d "$out/classes" "$here"/src/local/capsesc/*.java
find "$out/classes" -name '*.class' -print0 | xargs -0 "$bt/d8" --min-api 21 --lib "$jar" --output "$out"
zip -q -j "$out/unsigned.apk" "$out/classes.dex"
"$bt/zipalign" -f 4 "$out/unsigned.apk" "$out/aligned.apk"
"$bt/apksigner" sign --ks "$ks" --ks-pass "pass:$ks_pass" --key-pass "pass:$ks_pass" \
    --out "$here/capsesc-layout.apk" "$out/aligned.apk"
"$bt/apksigner" verify "$here/capsesc-layout.apk"
echo "Built $here/capsesc-layout.apk"
