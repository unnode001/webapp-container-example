package com.webcontainer.stage5;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 第五阶段：部署器
 * 负责自动扫描、加载和卸载Web应用
 */
public class WebAppDeployer {

    private final File webAppsDir;
    private final ClassLoader containerClassLoader;
    private final Map<String, WebAppContext> deployedApps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scannerExecutor;

    private volatile boolean running = false;

    public WebAppDeployer(String webAppsPath, ClassLoader containerClassLoader) {
        this.webAppsDir = new File(webAppsPath);
        this.containerClassLoader = containerClassLoader;
        this.scannerExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WebApp-Deployer");
            t.setDaemon(true);
            return t;
        });

        System.out.println("Web应用部署器初始化: " + webAppsDir.getAbsolutePath());
    }

    /**
     * 启动部署器
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;

        // 确保webapps目录存在
        if (!webAppsDir.exists()) {
            webAppsDir.mkdirs();
            System.out.println("创建webapps目录: " + webAppsDir.getAbsolutePath());
        }

        // 初始部署：扫描现有应用
        performInitialDeployment();

        // 启动定期扫描任务（每5秒扫描一次）
        scannerExecutor.scheduleWithFixedDelay(this::scanAndDeploy, 5, 5, TimeUnit.SECONDS);

        System.out.println("Web应用部署器启动完成，扫描间隔: 5秒");
    }

    /**
     * 停止部署器
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        // 停止扫描任务
        scannerExecutor.shutdown();
        try {
            if (!scannerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scannerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scannerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 卸载所有应用
        for (WebAppContext context : deployedApps.values()) {
            undeployApp(context.getWebAppName());
        }

        System.out.println("Web应用部署器已停止");
    }

    /**
     * 初始部署：扫描并部署所有现有应用
     */
    private void performInitialDeployment() {
        System.out.println("执行初始部署扫描...");

        File[] appDirs = webAppsDir.listFiles(File::isDirectory);
        if (appDirs != null) {
            for (File appDir : appDirs) {
                String appName = appDir.getName();
                if (!deployedApps.containsKey(appName)) {
                    deployApp(appName, appDir);
                }
            }
        }

        System.out.println("初始部署完成，已部署 " + deployedApps.size() + " 个应用");
    }

    /**
     * 扫描并部署新应用
     */
    private void scanAndDeploy() {
        try {
            if (!running) {
                return;
            }

            File[] appDirs = webAppsDir.listFiles(File::isDirectory);
            if (appDirs == null) {
                return;
            }

            // 检查新增的应用
            for (File appDir : appDirs) {
                String appName = appDir.getName();
                if (!deployedApps.containsKey(appName)) {
                    deployApp(appName, appDir);
                }
            }

            // 检查已删除的应用
            for (String deployedAppName : deployedApps.keySet()) {
                File appDir = new File(webAppsDir, deployedAppName);
                if (!appDir.exists() || !appDir.isDirectory()) {
                    undeployApp(deployedAppName);
                }
            }

        } catch (Exception e) {
            System.err.println("扫描和部署过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 部署Web应用
     */
    private synchronized void deployApp(String appName, File appDir) {
        try {
            System.out.println("部署Web应用: " + appName);

            // 确定Context Path
            String contextPath = appName.equals("ROOT") ? "" : "/" + appName;

            // 创建WebAppContext
            WebAppContext context = new WebAppContext(contextPath, appName, appDir, containerClassLoader);

            // 启动应用
            context.start();

            // 记录已部署的应用
            deployedApps.put(appName, context);

            System.out.println("Web应用部署成功: " + appName + " -> " + contextPath);

        } catch (Exception e) {
            System.err.println("部署Web应用失败 [" + appName + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 卸载Web应用
     */
    private synchronized void undeployApp(String appName) {
        try {
            WebAppContext context = deployedApps.remove(appName);
            if (context != null) {
                System.out.println("卸载Web应用: " + appName);

                // 停止应用
                context.stop();

                System.out.println("Web应用卸载成功: " + appName);
            }

        } catch (Exception e) {
            System.err.println("卸载Web应用失败 [" + appName + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理HTTP请求
     */
    public boolean handleRequest(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response) {
        String requestURI = request.getRequestURI();

        // 查找匹配的Context
        WebAppContext bestMatch = null;
        int longestMatch = -1;

        for (WebAppContext context : deployedApps.values()) {
            String contextPath = context.getContextPath();

            // ROOT应用的特殊处理
            if (contextPath.isEmpty()) {
                if (bestMatch == null) {
                    bestMatch = context;
                    longestMatch = 0;
                }
            } else if (requestURI.startsWith(contextPath)) {
                if (contextPath.length() > longestMatch) {
                    bestMatch = context;
                    longestMatch = contextPath.length();
                }
            }
        }

        if (bestMatch != null) {
            return bestMatch.handleRequest(request, response);
        }

        return false; // 没有匹配的应用
    }

    /**
     * 获取已部署的应用信息
     */
    public Map<String, WebAppContext> getDeployedApps() {
        return new ConcurrentHashMap<>(deployedApps);
    }

    /**
     * 获取部署统计信息
     */
    public DeploymentStats getDeploymentStats() {
        int totalApps = deployedApps.size();
        int runningApps = 0;

        for (WebAppContext context : deployedApps.values()) {
            if (context.isStarted()) {
                runningApps++;
            }
        }

        return new DeploymentStats(totalApps, runningApps);
    }

    /**
     * 部署统计信息
     */
    public static class DeploymentStats {
        private final int totalApps;
        private final int runningApps;

        public DeploymentStats(int totalApps, int runningApps) {
            this.totalApps = totalApps;
            this.runningApps = runningApps;
        }

        public int getTotalApps() {
            return totalApps;
        }

        public int getRunningApps() {
            return runningApps;
        }

        @Override
        public String toString() {
            return String.format("DeploymentStats{total=%d, running=%d}", totalApps, runningApps);
        }
    }
}
