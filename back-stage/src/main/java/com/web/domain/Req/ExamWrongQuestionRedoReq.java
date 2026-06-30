package com.web.domain.Req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExamWrongQuestionRedoReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "错题本记录 id")
    private Integer id;

    @Schema(description = "本次重做作答内容")
    private String redoAnswer;
}
