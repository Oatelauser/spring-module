package com.abm.module.api;

/**
 * 模块名的三元组元数据信息
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public record TriadMetadata(String groupId, String artifactId, String version) {

    public String getJarFileName() {
        return artifactId + "-" + version + ".jar";
    }

    /**
     * 解析形如如下格式的字符串
     * <p>
     * 1.com.abm.boot:spring-boot-starter-module:jar:1.0
     * 2.org.springframework.boot:spring-boot-starter:jar:3.2.1:compile
     *
     * @param dependency 依赖字符串
     * @return 元数据信息
     */
    public static TriadMetadata create(String dependency) {
        String[] words = dependency.split(":");
        return switch (words.length) {
            case 4, 5 -> new TriadMetadata(words[0], words[1], words[3]);
            default -> throw new IllegalArgumentException("不符合规范格式字符串" + dependency
                    + "，规范格式：org.springframework.boot:spring-boot-starter:jar:3.2.1:compile");
        };
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
