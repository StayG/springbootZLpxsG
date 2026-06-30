package com.web.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志 Excel 导出行
 */
@Data
public class SysOperationLogExcelDto {

    @ExcelProperty(value = "操作时间", index = 0)
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    @ExcelProperty(value = "操作人", index = 1)
    @ColumnWidth(14)
    private String adminName;

    @ExcelProperty(value = "操作模块", index = 2)
    @ColumnWidth(18)
    private String operationModule;

    @ExcelProperty(value = "操作类型", index = 3)
    @ColumnWidth(10)
    private String actionType;

    @ExcelProperty(value = "操作内容", index = 4)
    @ColumnWidth(48)
    private String content;

    @ExcelProperty(value = "IP地址", index = 5)
    @ColumnWidth(16)
    private String ip;

    @ExcelProperty(value = "操作结果", index = 6)
    @ColumnWidth(10)
    private String operationResult;
}
