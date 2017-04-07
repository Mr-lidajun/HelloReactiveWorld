package com.packtpub.rxjava_essentials.apps;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2016/9/28 17:35.
 * @desc: AppInfo
 */
@Data
@Accessors(prefix = "m")
public class AppInfo implements Comparable<Object> {

    long mLastUpdateTime;

    String mName;

    String mIcon;

    public AppInfo(String name, String icon, long lastUpdateTime) {
        mName = name;
        mIcon = icon;
        mLastUpdateTime = lastUpdateTime;
    }

    @Override
    public int compareTo(Object another) {
        AppInfo f = (AppInfo) another;
        return getName().compareTo(f.getName());
    }
}
