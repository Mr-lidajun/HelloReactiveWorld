package com.packtpub.rxjava_essentials.apps;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 2017/3/20 17:02.
 * @desc: ApplicationsList
 */

@Accessors(prefix = "m")
public class ApplicationsList {
    private static ApplicationsList ourInstance = new ApplicationsList();

    @Getter
    @Setter
    private List<AppInfo> mList;

    public ApplicationsList() {
    }

    public static ApplicationsList getInstance() {
        return ourInstance;
    }
}
