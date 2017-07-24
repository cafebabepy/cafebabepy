package org.cafebabepy.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by yotchang4s on 2017/06/01.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String resourceName = packageName.replace(".", "/");
        URL url = classLoader.getResource(resourceName);
        if (url == null) {
            return new LinkedHashSet<>();
        }

        String protocol = url.getProtocol();

        try {
            if ("file".equals(protocol)) {
                return getClassesWithFile(classLoader, packageName, new File(url.getFile()));

            } else if ("jar".equals(protocol)) {
                return getClassesWithJarFile(classLoader, packageName, url);
            }

        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        throw new IllegalArgumentException("Unsupported Class Load Protodol[" + protocol + "]");
    }

    private static Set<Class<?>> getClassesWithFile(ClassLoader classLoader, String packageName, File dir) throws ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();

        for (String path : dir.list()) {
            File entry = new File(dir, path);
            if (entry.isFile() && isClassFile(entry.getName())) {
                classes.add(classLoader.loadClass(packageName + "." + fileNameToClassName(entry.getName())));
            }
        }

        return classes;
    }

    private static Set<Class<?>> getClassesWithJarFile(ClassLoader classLoader, String packageName, URL jarFileUrl) throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new LinkedHashSet<>();

        JarURLConnection jarUrlConnection = (JarURLConnection) jarFileUrl.openConnection();
        JarFile jarFile = null;

        try {
            jarFile = jarUrlConnection.getJarFile();
            Enumeration<JarEntry> jarEnum = jarFile.entries();

            String recourceName = packageNameToResourceName(packageName);

            while (jarEnum.hasMoreElements()) {
                JarEntry jarEntry = jarEnum.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(recourceName) && isClassFile(name)) {
                    String subName = name.substring(recourceName.length() + 1);
                    if (!subName.contains("/")) {
                        classes.add(classLoader.loadClass(resourceNameToClassName(jarEntry.getName())));
                    }
                }
            }

        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        return classes;
    }

    private static String packageNameToResourceName(String packageName) {
        return packageName.replace('.', '/');
    }

    private static String fileNameToClassName(String name) {
        return name.substring(0, name.length() - ".class".length());
    }

    private static String resourceNameToClassName(String resourceName) {
        return fileNameToClassName(resourceName).replace('/', '.');
    }

    private static boolean isClassFile(String fileName) {
        return fileName.endsWith(".class");
    }
}
