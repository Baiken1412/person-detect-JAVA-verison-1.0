package com.ruoyi.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 图片URL处理工具类
 * 功能：在存储和显示图片路径时进行转换
 * - 存储时：完整URL -> 相对路径
 * - 显示时：相对路径 -> 完整URL
 *
 * @author ruoyi
 * @date 2026-01-02
 */
@Component
public class ImageUrlUtil {

    /** 图片URL前缀（从配置文件读取） */
    private static String urlPrefix;

    /** 图片相对路径前缀 */
    private static final String PROFILE_PREFIX = "/profile/caseapp/";

    @Value("${ruoyi.profile-url:}")
    public void setUrlPrefix(String url) {
        ImageUrlUtil.urlPrefix = url;
    }

    /**
     * 将完整URL转换为相对路径（用于存储）
     *
     * @param fullUrl 完整URL，例如：https://192.168.1.20:8090/profile/caseapp/20260102/xxx.jpg
     * @return 相对路径，例如：20260102/xxx.jpg
     */
    public static String toRelativePath(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return fullUrl;
        }

        // 如果已经是相对路径（不包含http或/profile），直接返回
        if (!fullUrl.startsWith("http") && !fullUrl.startsWith("/profile")) {
            return fullUrl;
        }

        // 提取相对路径
        if (fullUrl.contains(PROFILE_PREFIX)) {
            int index = fullUrl.indexOf(PROFILE_PREFIX);
            return fullUrl.substring(index + PROFILE_PREFIX.length());
        }

        // 如果只包含 /profile/ 前缀
        if (fullUrl.startsWith("/profile/")) {
            return fullUrl.substring("/profile/caseapp/".length());
        }

        return fullUrl;
    }

    /**
     * 将相对路径转换为完整URL（用于显示）
     *
     * @param relativePath 相对路径，例如：20260102/xxx.jpg
     * @return 完整URL，例如：https://192.168.1.20:8090/profile/caseapp/20260102/xxx.jpg
     */
    public static String toFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return relativePath;
        }

        // 如果已经是完整URL，直接返回
        if (relativePath.startsWith("http")) {
            return relativePath;
        }

        // 如果已经包含/profile/前缀，拼接域名部分
        if (relativePath.startsWith("/profile/")) {
            return getUrlPrefix() + relativePath;
        }

        // 相对路径，拼接完整前缀
        return getUrlPrefix() + PROFILE_PREFIX + relativePath;
    }

    /**
     * 获取URL前缀
     * 格式：http://domain:port 或 https://domain:port
     */
    private static String getUrlPrefix() {
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            return urlPrefix;
        }

        // 如果配置文件中没有配置，使用默认值
        // 注意：这里应该从配置文件读取，避免硬编码
        return "http://localhost:8090";
    }

    /**
     * 批量转换URL数组为相对路径
     */
    public static String[] toRelativePathArray(String[] fullUrls) {
        if (fullUrls == null) {
            return null;
        }

        String[] result = new String[fullUrls.length];
        for (int i = 0; i < fullUrls.length; i++) {
            result[i] = toRelativePath(fullUrls[i]);
        }
        return result;
    }

    /**
     * 批量转换相对路径数组为完整URL
     */
    public static String[] toFullUrlArray(String[] relativePaths) {
        if (relativePaths == null) {
            return null;
        }

        String[] result = new String[relativePaths.length];
        for (int i = 0; i < relativePaths.length; i++) {
            result[i] = toFullUrl(relativePaths[i]);
        }
        return result;
    }
}
