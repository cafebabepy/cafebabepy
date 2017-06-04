package org.cafebabepy.util;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/03.
 */
public class ModuleOrClassSplitter {

    private String name;

    private String moduleName;

    private String simpleName;

    public ModuleOrClassSplitter(String name) {
        this.name = name;

        int index = this.name.lastIndexOf('.');
        if (index == -1) {
            this.moduleName = null;
            this.name = name;

        } else {
            this.moduleName = this.name.substring(0, index);
            this.name = this.name.substring(index + 1, this.name.length());
        }
    }

    public String getName() {
        return this.name;
    }

    public Optional<String> getModuleName() {
        return Optional.ofNullable(this.moduleName);
    }

    public String getSimpleName() {
        return this.simpleName;
    }
}
