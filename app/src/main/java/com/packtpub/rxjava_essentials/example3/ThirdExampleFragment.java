package com.packtpub.rxjava_essentials.example3;

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
import com.packtpub.rxjava_essentials.R;
import com.packtpub.rxjava_essentials.apps.AppInfo;
import com.packtpub.rxjava_essentials.apps.ApplicationAdapter;
import com.packtpub.rxjava_essentials.apps.ApplicationsList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2017/3/28 18:19.
 * @desc: ${todo}
 */

public class ThirdExampleFragment extends Fragment {
    private static final String TAG = ThirdExampleFragment.class.getSimpleName();

    @BindView(R.id.fragment_example_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_example_swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ApplicationAdapter mAdapter;

    private ArrayList<AppInfo> mAddedApps = new ArrayList<>();
    private Unbinder mUnbinder;
    private Subscription mSubscribe;

    public ThirdExampleFragment() {
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
        AppInfo appOne = apps.get(0);

        AppInfo appTwo = apps.get(1);

        AppInfo appThree = apps.get(2);

        loadApps(appOne, appTwo, appThree);
        //defer();
        //range();
        interval();
    }

    private void loadApps(AppInfo appOne, AppInfo appTwo, AppInfo appThree) {
        mRecyclerView.setVisibility(View.VISIBLE);

        Observable.just(appOne, appTwo, appThree)
                .subscribe(new Observer<AppInfo>() {
            @Override
            public void onCompleted() {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), "Here is the list!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
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
     * 轮询
     */
    private void interval() {
        mSubscribe = Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(getActivity(), "Yeaaah!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(Long number) {
                        Toast.makeText(getActivity(), "I say " + number, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 从一个指定的数字X开始发射N个数字
     */
    private void range() {
        Observable.range(10, 3)
                .subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {
                Toast.makeText(getActivity(), "Yeaaah!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Integer number) {
                Toast.makeText(getActivity(), "I say " + number, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 推迟Observable
     */
    private void defer() {
        Observable<Integer> deferred = Observable.defer(this::getInt);
        Observable.timer(5, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        deferred.subscribe(number -> Log.d(TAG, String.valueOf(number)));
                    }
                });
    }

    private Observable<Integer> getInt() {
        return Observable.create(subscriber -> {
            if (subscriber.isUnsubscribed()) {
                return;
            }
            Log.d(TAG, "GETINT");
            subscriber.onNext(42);
            subscriber.onCompleted();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if (mSubscribe != null) {
            mSubscribe.unsubscribe();
        }
    }
}
