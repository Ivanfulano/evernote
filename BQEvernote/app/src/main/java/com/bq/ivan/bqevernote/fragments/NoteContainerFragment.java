package com.bq.ivan.bqevernote.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.bq.ivan.bqevernote.R;
import com.bq.ivan.bqevernote.activities.MainActivity;
import com.bq.ivan.bqevernote.io.CreateNewNoteTask;
import com.bq.ivan.bqevernote.io.FindNotesTask;
import com.bq.ivan.bqevernote.util.ViewUtil;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;

import net.vrallev.android.task.TaskResult;

import java.util.List;

/**
 * @author rwondratschek
 */
public class NoteContainerFragment extends AbstractContainerFragment {

    private static final int MAX_NOTES = 20;

    private static final String KEY_NOTEBOOK = "KEY_NOTEBOOK";
    private static final String KEY_LINKED_NOTEBOOK = "KEY_LINKED_NOTEBOOK";

    public static NoteContainerFragment create() {
        return create(null, null);
    }

    public static NoteContainerFragment create(@Nullable Notebook notebook, @Nullable LinkedNotebook linkedNotebook) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_NOTEBOOK, notebook);
        args.putSerializable(KEY_LINKED_NOTEBOOK, linkedNotebook);

        NoteContainerFragment fragment = new NoteContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Notebook mNotebook;
    private LinkedNotebook mLinkedNotebook;

    private String mQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mNotebook = (Notebook) getArguments().getSerializable(KEY_NOTEBOOK);
        mLinkedNotebook = (LinkedNotebook) getArguments().getSerializable(KEY_LINKED_NOTEBOOK);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            //noinspection ConstantConditions
            ((MainActivity) activity).getSupportActionBar().setTitle(R.string.notes);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CreateNoteDialogFragment.REQ_SELECT_IMAGE:
                Fragment fragment = getChildFragmentManager().findFragmentByTag(CreateNoteDialogFragment.TAG);
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
    public void onFindNotes(List<NoteRef> noteRefList) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (noteRefList == null || noteRefList.isEmpty()) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EmptyFragment.create("notes"))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, NoteListFragment.create(noteRefList))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @TaskResult
    public void onCreateNewNote(Note note) {
        if (note != null) {
            refresh();
        } else {
            ViewUtil.showSnackbar(mSwipeRefreshLayout, "Create note failed");
        }
    }

    @Override
    protected void loadData() {
        NoteFilter noteFilter = new NoteFilter();
        new FindNotesTask(0, MAX_NOTES, mNotebook, mLinkedNotebook, mQuery, NoteSortOrder.UPDATED.getValue()).start(this);
        mQuery = null;
    }

    @Override
    public void onFabClick() {
        new CreateNoteDialogFragment().show(getChildFragmentManager(), CreateNoteDialogFragment.TAG);
    }

    public void createNewNote(String title, String content, CreateNewNoteTask.ImageData imageData) {
        new CreateNewNoteTask(title, content, imageData, mNotebook, mLinkedNotebook).start(this);
    }

    public void search(String query) {
        mQuery = query;
        refresh();
    }
}
