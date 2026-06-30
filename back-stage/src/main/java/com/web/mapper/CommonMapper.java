package com.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {

    Long todayAppointmentCount(@Param("merchantId") Integer merchantId, @Param("status") Integer status);
}
