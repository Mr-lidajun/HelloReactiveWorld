package com.packtpub.rxjava_essentials.chapter5;

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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2017/3/28 18:19.
 * @desc: MapExampleFragment
 */

public class MapExampleFragment extends Fragment {
    private static final String TAG = MapExampleFragment.class.getSimpleName();

    @BindView(R.id.fragment_example_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_example_swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ApplicationAdapter mAdapter;

    private ArrayList<AppInfo> mAddedApps = new ArrayList<>();
    private Unbinder mUnbinder;

    public MapExampleFragment() {
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
        flatMap();
    }

    private void loadList(List<AppInfo> apps) {
        mRecyclerView.setVisibility(View.VISIBLE);

        Observable.from(apps)
                .map(new Func1<AppInfo, AppInfo>() {
            @Override
            public AppInfo call(AppInfo appInfo) {
                String currentName = appInfo.getName();
                String lowerCaseName = currentName.toLowerCase();
                appInfo.setName(lowerCaseName);
                return appInfo;
            }
        }).subscribe(new Observer<AppInfo>() {
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
     * 把Observable产生的结果转换成多个Observable，然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者
     */
    private void flatMap() {
        Observable.just(getContext().getExternalCacheDir())
                .flatMap(new Func1<File, Observable<File>>() {
            @Override
            public Observable<File> call(File file) {
                // 参数file是just操作符产生的结果，这里判断file是不是目录文件，如果是目录文件，
                // 则递归查找其子文件flatMap操作符神奇的地方在于，返回的结果还是一个Observable，
                // 而这个Observable其实是包含多个文件的Observable的，输出应该是ExternalCacheDir下的所有文件
                return listFiles(file);
            }
        }).subscribe(new Action1<File>() {
            @Override
            public void call(File file) {
                Log.d(TAG, file.getAbsolutePath());
            }
        });
    }

    private Observable<File> listFiles(File f) {
        if (f.isDirectory()) {
            return Observable.from(f.listFiles())
                    .flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    return listFiles(file);
                }
            });
        } else {
            return Observable.just(f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
