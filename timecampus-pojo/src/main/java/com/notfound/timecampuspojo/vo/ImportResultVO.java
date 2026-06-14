package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "批量导入结果")
public class ImportResultVO {
    @Schema(description = "本次提交条目总数", example = "10")
    private int total;
    @Schema(description = "成功条数", example = "8")
    private int successCount;
    @Schema(description = "失败条数", example = "2")
    private int failCount;
    @Schema(description = "失败明细")
    private List<FailureItem> failures;

    public ImportResultVO() {
        this.failures = new ArrayList<>();
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<FailureItem> getFailures() {
        return failures;
    }

    public void setFailures(List<FailureItem> failures) {
        this.failures = failures;
    }

    public void addFailure(int index, String reason) {
        if (this.failures == null) {
            this.failures = new ArrayList<>();
        }
        FailureItem item = new FailureItem();
        item.setIndex(index);
        item.setReason(reason);
        this.failures.add(item);
    }

    public static class FailureItem {
        @Schema(description = "失败条目在 items 数组中的下标", example = "3")
        private int index;
        @Schema(description = "失败原因", example = "poi not found: 999")
        private String reason;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

