package com.notfound.timecampusserver.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "timecampus.ai.seedream")
public class SeedreamImageProperties {

    private boolean enabled = false;
    private String apiKey;
    private String endpoint = "https://ark.cn-beijing.volces.com/api/plan/v3/images/generations";
    private String model = "doubao-seedream-5.0-lite";
    private String size = "2K";
    private String outputFormat = "jpeg";
    private boolean watermark = false;
    private double referenceStrength = 0.82;
    private long maxUploadBytes = 10 * 1024 * 1024;
    private List<Background> backgrounds = defaultBackgrounds();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public boolean isWatermark() {
        return watermark;
    }

    public void setWatermark(boolean watermark) {
        this.watermark = watermark;
    }

    public double getReferenceStrength() {
        return Math.max(0.0, Math.min(referenceStrength, 1.0));
    }

    public void setReferenceStrength(double referenceStrength) {
        this.referenceStrength = referenceStrength;
    }

    public long getMaxUploadBytes() {
        return Math.max(1, maxUploadBytes);
    }

    public void setMaxUploadBytes(long maxUploadBytes) {
        this.maxUploadBytes = maxUploadBytes;
    }

    public List<Background> getBackgrounds() {
        return backgrounds;
    }

    public void setBackgrounds(List<Background> backgrounds) {
        this.backgrounds = backgrounds == null ? new ArrayList<>() : backgrounds;
    }

    private static List<Background> defaultBackgrounds() {
        return List.of(
                new Background(
                        "main-building-1987",
                        "主楼合影",
                        "1987",
                        "主楼前的历史集体照",
                        "classpath:seedream-backgrounds/main-building-1987.jpg"
                ),
                new Background(
                        "track-meet-1953",
                        "运动场主席台",
                        "1953",
                        "建校早期运动场场景",
                        "classpath:seedream-backgrounds/track-meet-1953.jpg"
                ),
                new Background(
                        "building-one-old",
                        "一号楼俯瞰",
                        "历史影像",
                        "八楼区域早期校园景象",
                        "classpath:seedream-backgrounds/building-one-old.jpg"
                ),
                new Background(
                        "library-opening-1986",
                        "图书馆开馆典礼",
                        "1986",
                        "图书馆开馆典礼现场",
                        "classpath:seedream-backgrounds/library-opening-1986.jpg"
                )
        );
    }

    public static class Background {
        private String id;
        private String title;
        private String year;
        private String description;
        private String resource;

        public Background() {
        }

        public Background(String id, String title, String year, String description, String resource) {
            this.id = id;
            this.title = title;
            this.year = year;
            this.description = description;
            this.resource = resource;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }
    }
}
