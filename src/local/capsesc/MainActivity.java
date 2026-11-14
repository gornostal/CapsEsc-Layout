package local.capsesc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Shortcuts into settings screens the Boox UI hides, plus a key tester. */
public class MainActivity extends Activity {
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        col.setPadding(pad, pad, pad, pad);
        col.setBackgroundColor(Color.WHITE);

        col.addView(text("1. Connect the keyboard over Bluetooth.\n"
                + "2. Tap the first button, tap the keyboard's name, and choose\n"
                + "    \"English (US), Caps Lock = Esc, Cmd = Alt\".\n"
                + "3. Come back here and press Caps Lock: the tester below should say KEYCODE_ESCAPE,\n"
                + "    and Cmd should say KEYCODE_ALT_LEFT.", 18));
        col.addView(button("Open physical keyboard settings", Settings.ACTION_HARD_KEYBOARD_SETTINGS));
        col.addView(button("Open developer options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        col.addView(button("Open About (build number)", Settings.ACTION_DEVICE_INFO_SETTINGS));
        status = text("Key tester: press any key on the keyboard.", 20);
        status.setTextIsSelectable(false);
        col.addView(status);

        ScrollView sv = new ScrollView(this);
        sv.addView(col);
        setContentView(sv);
    }

    private TextView text(String s, int sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(Color.BLACK);
        t.setPadding(0, 24, 0, 24);
        return t;
    }

    private Button button(String label, final String action) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextSize(18);
        btn.setAllCaps(false);
        btn.setFocusable(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(action));
                } catch (Exception e) {
                    status.setText("Could not open " + action + ":\n" + e);
                }
            }
        });
        return btn;
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.KEYCODE_BACK || code == KeyEvent.KEYCODE_HOME
                || code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN
                || code == KeyEvent.KEYCODE_PAGE_UP || code == KeyEvent.KEYCODE_PAGE_DOWN) {
            return super.dispatchKeyEvent(e);
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            InputDevice d = e.getDevice();
            String dev = d == null ? "unknown device"
                    : d.getName() + String.format("  (vendor %04x, product %04x)", d.getVendorId(), d.getProductId());
            status.setText("Key tester\n"
                    + KeyEvent.keyCodeToString(code) + "\n"
                    + "scancode " + e.getScanCode() + "\n"
                    + "Caps Lock state: " + (e.isCapsLockOn() ? "ON" : "off") + "\n"
                    + dev);
        }
        return true; // consume, so Esc does not fall back to Back and close this screen
    }
}
