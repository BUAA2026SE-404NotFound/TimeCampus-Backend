package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员仪表盘指标")
public class AdminDashboardMetricVO {

    @Schema(description = "指标标题")
    private String label;

    @Schema(description = "指标数值")
    private String value;

    @Schema(description = "指标说明")
    private String detail;

    @Schema(description = "趋势说明")
    private String trend;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
