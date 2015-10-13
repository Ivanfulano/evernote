package com.bq.ivan.bqevernote.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bq.ivan.bqevernote.R;
import com.bq.ivan.bqevernote.fragments.CreateNoteDialogFragment;
import com.bq.ivan.bqevernote.fragments.NoteContainerFragment;
import com.bq.ivan.bqevernote.io.GetUserTask;
import com.bq.ivan.bqevernote.util.Util;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;

import net.vrallev.android.task.TaskResult;

public class MainActivity extends AppCompatActivity {

    private int mSelectedNavItem;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EvernoteSession.getInstance().isLoggedIn()) {
            // LoginChecker will call finish
            return;
        }

        setContentView(R.layout.activity_main);

        Resources resources = getResources();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            loadNotes();
            new GetUserTask().start(this);

        } else if (mUser != null) {
            onGetUser(mUser);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CreateNoteDialogFragment.REQ_SELECT_IMAGE:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    // somehow the event doesn't get dispatched correctly
                    fragment.onActivityResult(requestCode, resultCode, data);
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @TaskResult
    public void onGetUser(User user) {
        mUser = user;
    }

    private void loadNotes() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, NoteContainerFragment.create())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

    }
}
