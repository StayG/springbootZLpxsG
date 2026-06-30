package com.web.domain.Resp;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.domain.Teachers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TeachersResp extends Teachers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任教科目名称")
    private String kemuTypesName;

}
