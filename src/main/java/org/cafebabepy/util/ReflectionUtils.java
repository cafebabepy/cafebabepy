package org.cafebabepy.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yotchang4s on 2017/06/01.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        Set<Class<?>> classes = new HashSet<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> e = classLoader.getResources(packageName.replace(".", "/"));

        try {
            while (e.hasMoreElements()) {
                URL url = e.nextElement();

                File dir = new File(url.getPath());
                String[] list = dir.list();
                if (list == null) {
                    continue;
                }

                for (String path : list) {
                    if (path.endsWith(".class")) {
                        String simpleClassName = path.substring(0, path.length() - 6);
                        Class<?> clazz = Class.forName(packageName + "." + simpleClassName);

                        classes.add(clazz);
                    }
                }
            }

            return classes;

        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe);
        }
    }
}
