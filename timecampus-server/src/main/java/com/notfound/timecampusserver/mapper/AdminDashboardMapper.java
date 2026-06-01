package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.vo.AdminDashboardDistributionVO;
import com.notfound.timecampuspojo.vo.AdminDashboardTrendVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    int countPois();

    int countActivePois();

    int countMedia();

    int countApprovedMedia();

    int countPendingUgc();

    int countPendingComments();

    int countNewMediaInDays(int days);

    int countNewUgcInDays(int days);

    List<AdminDashboardTrendVO> listContentTrends(int days);

    List<AdminDashboardDistributionVO> listReviewDistribution();

    List<AdminDashboardDistributionVO> listMediaTypeDistribution();
}
