package com.web.domain.Resp;

import com.web.domain.Dictionary;
import lombok.Data;

import java.io.Serializable;


@Data
public class DictionaryResp extends Dictionary implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ExamQuestionTypes;

}
