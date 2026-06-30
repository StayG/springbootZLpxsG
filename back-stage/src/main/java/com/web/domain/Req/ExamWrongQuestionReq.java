package com.web.domain.Req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ExamWrongQuestionReq extends PageInfo {
    private static final long serialVersionUID = 1L;

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "主键 id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "用户名")
    private String nickname;

    @Schema(description = "试卷 id")
    private Integer examPaperId;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "试题 id")
    private Integer examQuestionId;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "试题排序")
    private Integer examQuestionSequence;

    @Schema(description = "考生作答")
    private String examredetaisMyanswer;

    @Schema(description = "试题类")
    private String examQuestionTypes;

    @Schema(description = "选项")
    private String examQuestionOptions;


    @Schema(description = "记录时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date insertTime;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "掌握状态筛选：null全部，0未掌握，1已掌握")
    private Integer masteryStatus;
}
