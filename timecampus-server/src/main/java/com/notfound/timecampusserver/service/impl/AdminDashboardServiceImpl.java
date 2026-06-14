package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.vo.AdminDashboardDistributionVO;
import com.notfound.timecampuspojo.vo.AdminDashboardMetricVO;
import com.notfound.timecampuspojo.vo.AdminDashboardStatsVO;
import com.notfound.timecampusserver.mapper.AdminDashboardMapper;
import com.notfound.timecampusserver.service.AdminDashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int TREND_DAYS = 14;
    private static final int RECENT_DAYS = 7;

    private final AdminDashboardMapper adminDashboardMapper;

    public AdminDashboardServiceImpl(AdminDashboardMapper adminDashboardMapper) {
        this.adminDashboardMapper = adminDashboardMapper;
    }

    @Override
    public AdminDashboardStatsVO stats() {
        int poiCount = adminDashboardMapper.countPois();
        int activePoiCount = adminDashboardMapper.countActivePois();
        int mediaCount = adminDashboardMapper.countMedia();
        int approvedMediaCount = adminDashboardMapper.countApprovedMedia();
        int pendingUgcCount = adminDashboardMapper.countPendingUgc();
        int pendingCommentCount = adminDashboardMapper.countPendingComments();
        int recentMediaCount = adminDashboardMapper.countNewMediaInDays(RECENT_DAYS);
        int recentUgcCount = adminDashboardMapper.countNewUgcInDays(RECENT_DAYS);

        AdminDashboardStatsVO stats = new AdminDashboardStatsVO();
        stats.setMetrics(List.of(
                metric("POI 总数", poiCount, "活跃 POI " + activePoiCount + " 个", "来自当前 POI 表"),
                metric("图片内容", mediaCount, "已通过内容 " + approvedMediaCount + " 条", "+" + recentMediaCount + " 本周"),
                metric("待审核 UGC", pendingUgcCount, "图片投稿 " + pendingUgcCount + " 条", "+" + recentUgcCount + " 本周"),
                metric("评论待处理", pendingCommentCount, "待审核评论 " + pendingCommentCount + " 条", "评论功能保留审计")
        ));
        stats.setTrends(adminDashboardMapper.listContentTrends(TREND_DAYS));
        stats.setReviewDistribution(adminDashboardMapper.listReviewDistribution());
        stats.setMediaTypeDistribution(adminDashboardMapper.listMediaTypeDistribution());
        return stats;
    }

    private AdminDashboardMetricVO metric(String label, int value, String detail, String trend) {
        AdminDashboardMetricVO metric = new AdminDashboardMetricVO();
        metric.setLabel(label);
        metric.setValue(String.valueOf(value));
        metric.setDetail(detail);
        metric.setTrend(trend);
        return metric;
    }
}
