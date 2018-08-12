package org.cafebabepy.util;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public static boolean isDirectory(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String resourceName = packageNameToResourceName(packageName);
        URL url = classLoader.getResource(resourceName);
        if (url == null) {
            return false;
        }

        String protocol = url.getProtocol();

        if ("file".equals(protocol)) {
            return isDirectoryInFileSystem(url);

        } else if ("jar".equals(protocol)) {
            return isDirectoryInJarFile(url, resourceName);
        }

        throw new IllegalArgumentException("Unsupported Class Load Protocol[" + protocol + "]");
    }

    private static boolean isDirectoryInFileSystem(URL url) {
        return new File(url.getFile()).isDirectory();
    }

    private static boolean isDirectoryInJarFile(URL jarFileUrl, String packageName) throws IOException {
        JarURLConnection jarUrlConnection = (JarURLConnection) jarFileUrl.openConnection();

        try (JarFile jarFile = jarUrlConnection.getJarFile()) {
            JarEntry jarEntry = jarFile.getJarEntry(packageName);
            if (jarEntry == null) {
                return false;
            }

            return jarEntry.isDirectory();
        }
    }

    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String resourceName = packageNameToResourceName(packageName);
        URL url = classLoader.getResource(resourceName);
        if (url == null) {
            return new LinkedHashSet<>();
        }

        String protocol = url.getProtocol();

        try {
            if ("file".equals(protocol)) {
                return getClassesInFileSystem(url, packageName, classLoader);

            } else if ("jar".equals(protocol)) {
                return getClassesInJarFile(url, packageName, classLoader);
            }

        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }

        throw new IllegalArgumentException("Unsupported Class Load Protocol[" + protocol + "]");
    }

    private static Set<Class<?>> getClassesInFileSystem(URL url, String packageName, ClassLoader classLoader) throws ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();

        File dir = new File(url.getFile());
        String[] dirList = dir.list();
        if (dirList != null) {
            for (String path : dirList) {
                File entry = new File(dir, path);
                if (entry.isFile() && isClassFile(entry.getName())) {
                    classes.add(classLoader.loadClass(packageName + "." + fileNameToClassName(entry.getName())));
                }
            }
        }

        return classes;
    }

    private static Set<Class<?>> getClassesInJarFile(URL jarFileUrl, String packageName, ClassLoader classLoader) throws ClassNotFoundException, IOException {
        Set<Class<?>> classes = new LinkedHashSet<>();

        JarURLConnection jarUrlConnection = (JarURLConnection) jarFileUrl.openConnection();

        try (JarFile jarFile = jarUrlConnection.getJarFile()) {
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
        }

        return classes;
    }

    public static String packageNameToResourceName(String packageName) {
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
