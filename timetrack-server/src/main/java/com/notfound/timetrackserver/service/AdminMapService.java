package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.vo.AdminMapOverviewVO;

public interface AdminMapService {
    AdminMapOverviewVO overview(Integer status, String keyword, String commentStatus, Integer limit);
}
