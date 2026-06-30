package com.web.exception;

import com.web.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;



@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public Result exceptionHandler(BusinessException e) {
        LOG.error("涓氬姟寮傚父", e);
        Result result = new Result();
        result.setCode(e.getCode());
        result.setMsg(e.getMsg());
        return result;
    }


    
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e){
        LOG.error("绯荤粺寮傚父", e);
        Result result = new Result();
        result.setCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        result.setMsg(ErrorCode.INTERNAL_SERVER_ERROR.getMsg());
        return result;
    }




}
