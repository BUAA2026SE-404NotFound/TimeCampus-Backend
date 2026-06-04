package com.notfound.timecampusserver.mcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecampus.rag")
public class TimeCampusRagProperties {

    private boolean vectorEnabled = false;
    private boolean lexicalFallbackEnabled = true;
    private int defaultTopK = 8;
    private int maxTopK = 20;
    private double similarityThreshold = 0.0;
    private int chunkMaxChars = 1200;
    private int chunkOverlapChars = 120;

    public boolean isVectorEnabled() {
        return vectorEnabled;
    }

    public void setVectorEnabled(boolean vectorEnabled) {
        this.vectorEnabled = vectorEnabled;
    }

    public boolean isLexicalFallbackEnabled() {
        return lexicalFallbackEnabled;
    }

    public void setLexicalFallbackEnabled(boolean lexicalFallbackEnabled) {
        this.lexicalFallbackEnabled = lexicalFallbackEnabled;
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK <= 0 ? 8 : defaultTopK;
    }

    public int getMaxTopK() {
        return maxTopK;
    }

    public void setMaxTopK(int maxTopK) {
        this.maxTopK = maxTopK <= 0 ? 20 : maxTopK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = Math.max(0.0, similarityThreshold);
    }

    public int getChunkMaxChars() {
        return chunkMaxChars;
    }

    public void setChunkMaxChars(int chunkMaxChars) {
        this.chunkMaxChars = chunkMaxChars <= 0 ? 1200 : chunkMaxChars;
    }

    public int getChunkOverlapChars() {
        return chunkOverlapChars;
    }

    public void setChunkOverlapChars(int chunkOverlapChars) {
        this.chunkOverlapChars = Math.max(0, chunkOverlapChars);
    }
}
