package com.bq.ivan.bqevernote.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bq.ivan.bqevernote.R;


/**
 * @author rwondratschek
 */
public abstract class AbstractContainerFragment extends Fragment {

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private View view;
    private Button addNoteBT;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_abstract_container, container, false);


        initViews();
        setListeners();

        if (savedInstanceState == null) {
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, 200L);
        }

        return view;
    }

    private void initViews() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        addNoteBT = (Button) view.findViewById(R.id.addNoteBT);
    }

    private void setListeners() {
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

        view.findViewById(R.id.fragment_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        addNoteBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });
    }

    public void refresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        loadData();
    }

    protected abstract void loadData();

    public abstract void onFabClick();

    private final SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh();
        }
    };
}
