package org.cafebabepy.runtime.module.sys;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.object.java.PyDictObject;

import java.util.LinkedHashMap;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefinePyModule(name = "sys")
public class PySysModule extends AbstractCafeBabePyModule {

    public PySysModule(Python runtime) {
        super(runtime);
    }

    @Override
    public void initialize() {
        super.initialize();

        PyDictObject modules = new PyDictObject(this.runtime, this.runtime.getSysModuleMap());
        getScope().put(this.runtime.str("modules"), modules);

        this.runtime.defineModule(this);

        PyObject version_info = this.runtime.newPyObject("sys.version_info", false);
        getScope().put(this.runtime.str("version_info"), version_info);

        String os_arch = System.getProperty("os.arch");
        String os_name = System.getProperty("os.name").replaceAll("¥s+", "-").toLowerCase();

        String _multiarch = os_arch + "-" + os_name;
        String cache_tag = Python.APPLICATION_NAME + "-" + Python.MAJOR + Python.MINOR;

        int release = 0x0f;
        switch (Python.RELEASE_LEVEL) {
            case "final":
                release = 0x0f;
                break;

            case "candidate":
                release = 0x0c;

            case "beta":
                release = 0x0b;
                break;

            case "alpha":
                release = 0x0a;
                break;
        }

        String hexversionStr = String.format("%02x%02x%02x%01x%01x", Python.MAJOR, Python.MINOR, Python.MICRO, release, Python.SERIAL);
        int hexversion = Integer.parseInt(hexversionStr, 16);

        LinkedHashMap<String, PyObject> simpleNamespaceMap = new LinkedHashMap<>();
        simpleNamespaceMap.put("_multiarch", this.runtime.str(_multiarch));
        simpleNamespaceMap.put("cache_tag", this.runtime.str(cache_tag));
        simpleNamespaceMap.put("hexversion", this.runtime.number(hexversion));
        simpleNamespaceMap.put("name", this.runtime.str(Python.APPLICATION_NAME));
        simpleNamespaceMap.put("version", version_info);

        PyObject implementation = this.runtime.newPyObject("SimpleNamespace",
                new PyObject[0], simpleNamespaceMap
        );

        getScope().put(this.runtime.str("implementation"), implementation);
    }

    @DefinePyFunction(name = "exc_info")
    public PyObject exc_info() {
        // FIXME types stub
        PyObject traceback1 = this.runtime.newPyObject("traceback", false);
        PyObject traceback2 = this.runtime.newPyObject("traceback", false);
        PyObject traceback3 = this.runtime.newPyObject("traceback", false);

        return this.runtime.list(traceback1, traceback2, traceback3);
    }
}
