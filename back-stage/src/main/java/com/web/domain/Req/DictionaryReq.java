package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DictionaryReq extends PageInfo {

    @Schema(description = "主键")
    private Long id;


    @Schema(description = "字段")
    private String dicCode;


    @Schema(description = "字段名")
    private String dicName;


    @Schema(description = "编码")
    private Integer codeIndex;


    @Schema(description = "编码名字")
    private String indexName;

    @Schema(description = "批量查询 ID 列表")
    private List<Long> ids;

    @Schema(description = "编码索引起始值")
    private Integer codeIndexStart;

    @Schema(description = "编码索引结束值")
    private Integer codeIndexEnd;

    public PageInfo getPageInfo() {
        return this;
    }

}
