package local.capsesc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Never actually invoked; the system only reads this receiver's meta-data. */
public class LayoutReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) { }
}
