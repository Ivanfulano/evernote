package com.bq.ivan.bqevernote.application;

import android.app.Application;

import com.evernote.client.android.EvernoteSession;

/**
 * Created by ivan on 08/10/2015.
 */
public class EvernoteApplication extends Application {

    private static final String CONSUMER_KEY = "ivan-6322";
    private static final String CONSUMER_SECRET = "091e61a555193b83";

    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    @Override
    public void onCreate() {
        super.onCreate();

        new EvernoteSession.Builder(this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setForceAuthenticationInThirdPartyApp(true)
                .build(CONSUMER_KEY, CONSUMER_SECRET)
                .asSingleton();

        registerActivityLifecycleCallbacks(new LoginChecker());
    }
}
