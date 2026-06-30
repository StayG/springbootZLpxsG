package com.web.domain.Resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class NoticesResp implements Serializable {

  private static final long serialVersionUID = 1L;

  @Schema(description = "id")
  private Integer id;

  @Schema(description = "标题")
  private String title;

  @Schema(description = "内容")
  private String content;

  @Schema(description = "图片")
  private String pictures;

  @Schema(description = "发布教师ID")
  private Integer teacherId;

  @Schema(description = "科目ID")
  private Integer kemuId;

  @Schema(description = "创建时间")
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  @Schema(description = "修改时间")
  @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;

}
