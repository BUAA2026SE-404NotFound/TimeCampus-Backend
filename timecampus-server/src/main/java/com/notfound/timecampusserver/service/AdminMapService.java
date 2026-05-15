package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.AdminMapOverviewVO;

public interface AdminMapService {
    AdminMapOverviewVO overview(Integer status, String keyword, String commentStatus, Integer limit);
}
