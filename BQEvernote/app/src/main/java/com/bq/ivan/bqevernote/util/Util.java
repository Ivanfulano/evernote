package com.bq.ivan.bqevernote.util;

import android.app.Activity;

import com.bq.ivan.bqevernote.activities.LoginActivity;
import com.evernote.client.android.EvernoteSession;

/**
 * @author rwondratschek
 */
public final class Util {

    private Util() {
        // no op
    }

    public static void logout(Activity activity) {
        EvernoteSession.getInstance().logOut();
        LoginActivity.launch(activity);
        activity.finish();
    }
}
