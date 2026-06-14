package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员仪表盘分布项")
public class AdminDashboardDistributionVO {

    @Schema(description = "分布项名称")
    private String label;

    @Schema(description = "分布项值")
    private Integer value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
