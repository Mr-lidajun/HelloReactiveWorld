package com.packtpub.rxjava_essentials.chapter5;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.packtpub.rxjava_essentials.R;
import com.packtpub.rxjava_essentials.apps.AppInfo;
import com.packtpub.rxjava_essentials.apps.ApplicationAdapter;
import com.packtpub.rxjava_essentials.apps.ApplicationsList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2017/4/5 18:19.
 * @desc: GroupByExampleFragment
 */

public class GroupByExampleFragment extends Fragment {
    private static final String TAG = GroupByExampleFragment.class.getSimpleName();

    @BindView(R.id.fragment_example_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_example_swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ApplicationAdapter mAdapter;

    private ArrayList<AppInfo> mAddedApps = new ArrayList<>();
    private Unbinder mUnbinder;

    public GroupByExampleFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_example, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = new ApplicationAdapter(new ArrayList<>(), R.layout.applications_list_item);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.myPrimaryColor));
        mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        // Progress
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView.setVisibility(View.GONE);

        List<AppInfo> apps = ApplicationsList.getInstance().getList();
        loadList(apps);
        //groupBy();
        window();
    }

    private void loadList(List<AppInfo> apps) {
        mRecyclerView.setVisibility(View.VISIBLE);

        Observable<GroupedObservable<String, AppInfo>> groupedItems =
                Observable.from(apps).groupBy(new Func1<AppInfo, String>() {
                    @Override
                    public String call(AppInfo appInfo) {
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
                        return formatter.format(new Date(appInfo.getLastUpdateTime()));
                    }
                });

        Observable.concat(groupedItems)
                .subscribe(new Observer<AppInfo>() {
                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "Something went south!", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(AppInfo appInfo) {
                        mAddedApps.add(appInfo);
                        mAdapter.addApplication(mAddedApps.size() - 1, appInfo);
                    }
                });
    }

    /**
     * 对源Observable产生的结果进行分组，形成一个类型为GroupedObservable的结果集
     */
    private void groupBy() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(10)
                .groupBy(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long value) {
                        return value % 3;
                    }
                })
        .subscribe(new Action1<GroupedObservable<Long, Long>>() {
            @Override
            public void call(GroupedObservable<Long, Long> result) {
                result.subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long value) {
                        Log.d(TAG, "key:" + result.getKey() +", value:" + value);
                    }
                });
            }
        });
    }
    
    private void window() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(12)
                .window(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Observable<Long>>() {
                    @Override
                    public void call(Observable<Long> observable) {
                        System.out.println("subdivide begin......");
                        observable.subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                Log.d(TAG, "Next:" + aLong);
                            }
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
