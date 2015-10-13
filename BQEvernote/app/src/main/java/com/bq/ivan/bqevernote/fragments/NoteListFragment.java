package com.bq.ivan.bqevernote.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bq.ivan.bqevernote.R;
import com.bq.ivan.bqevernote.activities.ViewHtmlActivity;
import com.bq.ivan.bqevernote.io.BaseTask;
import com.bq.ivan.bqevernote.io.DeleteNoteTask;
import com.bq.ivan.bqevernote.io.FindNotesTask;
import com.bq.ivan.bqevernote.io.GetNoteContentTask;
import com.bq.ivan.bqevernote.io.GetNoteHtmlTask;
import com.bq.ivan.bqevernote.util.ParcelableUtil;
import com.bq.ivan.bqevernote.util.ViewUtil;
import com.evernote.android.intent.EvernoteIntent;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteClientFactory;
import com.evernote.client.android.type.NoteRef;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;

import net.vrallev.android.task.TaskResult;

import java.util.List;

/**
 * @author rwondratschek
 */
@SuppressWarnings("FieldCanBeLocal")
public class NoteListFragment extends Fragment {

    private static final String KEY_NOTE_LIST = "KEY_NOTE_LIST";

    private View view;
    private Spinner sortMethodsSP;

    private static final String KEY_NOTEBOOK = "KEY_NOTEBOOK";
    private static final String KEY_LINKED_NOTEBOOK = "KEY_LINKED_NOTEBOOK";
    private static final int MAX_NOTES = 20;
    private Notebook mNotebook;
    private LinkedNotebook mLinkedNotebook;
    private String mQuery;

    public static NoteListFragment create(List<NoteRef> noteRefList) {
        Bundle args = new Bundle();
        ParcelableUtil.putParcelableList(args, noteRefList, KEY_NOTE_LIST);

        NoteListFragment fragment = new NoteListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private List<NoteRef> mNoteRefList;

    private AbsListView mListView;
    private MyAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNoteRefList = getArguments().getParcelableArrayList(KEY_NOTE_LIST);

        mNotebook = (Notebook) getArguments().getSerializable(KEY_NOTEBOOK);
        mLinkedNotebook = (LinkedNotebook) getArguments().getSerializable(KEY_LINKED_NOTEBOOK);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note_list, container, false);

        initViews();
        setAdapters();
        setListeners();

        registerForContextMenu(mListView);


        return view;
    }

    private void initViews() {
        mListView = (AbsListView) view.findViewById(R.id.listView);
        sortMethodsSP = (Spinner) view.findViewById(R.id.sortTypeSP);
    }

    private void setAdapters() {
        mAdapter = new MyAdapter();

        mListView.setAdapter(mAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.sort_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortMethodsSP.setAdapter(adapter);
    }

    private void setListeners() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new GetNoteHtmlTask(mNoteRefList.get(position)).start(NoteListFragment.this, "html");
            }
        });

        sortMethodsSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sortList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void sortList(int position) {
        switch (position) {
            case 0:
                sortByTitle();
                break;
            case 1:
                sortByDate();
                break;
            default:
                break;
        }

    }

    private void sortByTitle() {
        new FindNotesTask(0, MAX_NOTES, mNotebook, mLinkedNotebook, mQuery, NoteSortOrder.TITLE.getValue()).start(this);
        mQuery = null;
    }

    private void sortByDate() {
        new FindNotesTask(0, MAX_NOTES, mNotebook, mLinkedNotebook, mQuery, NoteSortOrder.UPDATED.getValue()).start(this);
        mQuery = null;
    }

    @TaskResult
    public void onFindNotes(List<NoteRef> noteRefList) {
        mNoteRefList = noteRefList;
        mAdapter = new MyAdapter();

        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        NoteRef noteRef = mNoteRefList.get(info.position);

        switch (item.getItemId()) {
            case 0:
                new ShareNoteTask(noteRef).start(this);
                return true;

            case 1:
                Intent intent = EvernoteIntent.viewNote()
                        .setNoteGuid(noteRef.getGuid())
                        .create();

                if (EvernoteIntent.isEvernoteInstalled(getActivity())) {
                    startActivity(intent);
                } else {
                    ViewUtil.showSnackbar(mListView, R.string.evernote_not_installed);
                }
                return true;

            case 2:
                new GetNoteContentTask(noteRef).start(this, "content");
                return true;

            case 3:
                new DeleteNoteTask(noteRef).start(this);
                return true;

            default:
                return false;
        }
    }

    @TaskResult
    public void onNoteShared(String url) {
        if (!TextUtils.isEmpty(url)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            ViewUtil.showSnackbar(mListView, "URL is null");
        }
    }

    @TaskResult
    public void onNoteDeleted(DeleteNoteTask.Result result) {
        if (result != null) {
            ((AbstractContainerFragment) getParentFragment()).refresh();
        } else {
            ViewUtil.showSnackbar(mListView, "Delete note failed");
        }
    }

    @TaskResult(id = "content")
    public void onGetNoteContent(Note note) {
        if (note != null) {
            NoteContentDialogFragment.create(note).show(getChildFragmentManager(), NoteContentDialogFragment.TAG);
        } else {
            ViewUtil.showSnackbar(mListView, "Get content failed");
        }
    }

    @TaskResult(id = "html")
    public void onGetNoteContentHtml(String html, GetNoteHtmlTask task) {
        startActivity(ViewHtmlActivity.createIntent(getActivity(), task.getNoteRef(), html));
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNoteRefList.size();
        }

        @Override
        public NoteRef getItem(int position) {
            return mNoteRefList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            NoteRef noteRef = getItem(position);
            viewHolder.mTextView1.setText(noteRef.getTitle());

            return convertView;
        }
    }

    private static class ViewHolder {

        private final TextView mTextView1;

        public ViewHolder(View view) {
            mTextView1 = (TextView) view.findViewById(android.R.id.text1);
        }
    }

    private static final class ShareNoteTask extends BaseTask<String> {

        private final NoteRef mNoteRef;

        private ShareNoteTask(NoteRef noteRef) {
            super(String.class);
            mNoteRef = noteRef;
        }

        @Override
        protected String checkedExecute() throws Exception {
            EvernoteClientFactory clientFactory = EvernoteSession.getInstance().getEvernoteClientFactory();

            String shardId = clientFactory.getUserStoreClient().getUser().getShardId();
            String shareKey = clientFactory.getNoteStoreClient().shareNote(mNoteRef.getGuid());

            return "https://" + EvernoteSession.getInstance().getAuthenticationResult().getEvernoteHost()
                    + "/shard/" + shardId + "/sh/" + mNoteRef.getGuid() + "/" + shareKey;
        }
    }
}
