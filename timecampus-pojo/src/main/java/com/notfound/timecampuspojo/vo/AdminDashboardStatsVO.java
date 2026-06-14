package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理员仪表盘统计数据")
public class AdminDashboardStatsVO {

    @Schema(description = "顶部指标卡")
    private List<AdminDashboardMetricVO> metrics = new ArrayList<>();

    @Schema(description = "近 14 天内容增长趋势")
    private List<AdminDashboardTrendVO> trends = new ArrayList<>();

    @Schema(description = "审核状态分布")
    private List<AdminDashboardDistributionVO> reviewDistribution = new ArrayList<>();

    @Schema(description = "媒体类型分布")
    private List<AdminDashboardDistributionVO> mediaTypeDistribution = new ArrayList<>();

    public List<AdminDashboardMetricVO> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<AdminDashboardMetricVO> metrics) {
        this.metrics = metrics;
    }

    public List<AdminDashboardTrendVO> getTrends() {
        return trends;
    }

    public void setTrends(List<AdminDashboardTrendVO> trends) {
        this.trends = trends;
    }

    public List<AdminDashboardDistributionVO> getReviewDistribution() {
        return reviewDistribution;
    }

    public void setReviewDistribution(List<AdminDashboardDistributionVO> reviewDistribution) {
        this.reviewDistribution = reviewDistribution;
    }

    public List<AdminDashboardDistributionVO> getMediaTypeDistribution() {
        return mediaTypeDistribution;
    }

    public void setMediaTypeDistribution(List<AdminDashboardDistributionVO> mediaTypeDistribution) {
        this.mediaTypeDistribution = mediaTypeDistribution;
    }
}
