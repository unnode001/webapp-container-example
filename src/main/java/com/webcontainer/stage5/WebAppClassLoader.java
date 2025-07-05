package com.webcontainer.stage5;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 第五阶段：Web应用类加载器
 * 为每个Web应用提供独立的类加载环境，实现应用隔离
 */
public class WebAppClassLoader extends URLClassLoader {

    private final String webAppName;
    private final File webAppDir;
    private final ClassLoader containerClassLoader;

    public WebAppClassLoader(String webAppName, File webAppDir, ClassLoader parent) {
        super(buildClassPath(webAppDir), parent);
        this.webAppName = webAppName;
        this.webAppDir = webAppDir;
        this.containerClassLoader = parent;

        System.out.println("创建Web应用类加载器: " + webAppName + " -> " + webAppDir.getAbsolutePath());
    }

    /**
     * 构建Web应用的类路径
     */
    private static URL[] buildClassPath(File webAppDir) {
        List<URL> urls = new ArrayList<>();

        try {
            // 添加 WEB-INF/classes 目录
            File classesDir = new File(webAppDir, "WEB-INF/classes");
            if (classesDir.exists() && classesDir.isDirectory()) {
                urls.add(classesDir.toURI().toURL());
                System.out.println("  添加类路径: " + classesDir.getAbsolutePath());
            }

            // 添加 WEB-INF/lib 目录下的所有JAR文件
            File libDir = new File(webAppDir, "WEB-INF/lib");
            if (libDir.exists() && libDir.isDirectory()) {
                File[] jarFiles = libDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
                if (jarFiles != null) {
                    for (File jarFile : jarFiles) {
                        urls.add(jarFile.toURI().toURL());
                        System.out.println("  添加JAR: " + jarFile.getName());
                    }
                }
            }

            // 如果没有找到标准目录，将整个应用目录添加到类路径
            if (urls.isEmpty()) {
                urls.add(webAppDir.toURI().toURL());
                System.out.println("  添加应用根目录: " + webAppDir.getAbsolutePath());
            }

        } catch (MalformedURLException e) {
            System.err.println("构建类路径时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        return urls.toArray(new URL[0]);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 遵循Servlet规范的类加载顺序：
        // 1. 首先检查是否已经加载
        Class<?> clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        // 2. 对于容器核心类和JDK类，委托给父加载器
        if (isContainerClass(name)) {
            try {
                clazz = containerClassLoader.loadClass(name);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch (ClassNotFoundException e) {
                // 继续尝试从Web应用中加载
            }
        }

        // 3. 尝试从Web应用自己的类路径中加载
        try {
            clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            // 继续委托给父加载器
        }

        // 4. 最后委托给父加载器
        clazz = super.loadClass(name, resolve);
        return clazz;
    }

    /**
     * 判断是否为容器核心类（不应该被Web应用重写）
     */
    private boolean isContainerClass(String className) {
        return className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("com.webcontainer.") ||
                className.startsWith("sun.") ||
                className.startsWith("org.xml.") ||
                className.startsWith("org.w3c.");
    }

    @Override
    public String toString() {
        return "WebAppClassLoader{" +
                "webAppName='" + webAppName + '\'' +
                ", webAppDir=" + webAppDir.getAbsolutePath() +
                '}';
    }

    /**
     * 获取Web应用名称
     */
    public String getWebAppName() {
        return webAppName;
    }

    /**
     * 获取Web应用目录
     */
    public File getWebAppDir() {
        return webAppDir;
    }

    /**
     * 销毁类加载器，释放资源
     */
    public void destroy() {
        try {
            close();
            System.out.println("Web应用类加载器已销毁: " + webAppName);
        } catch (Exception e) {
            System.err.println("销毁Web应用类加载器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
