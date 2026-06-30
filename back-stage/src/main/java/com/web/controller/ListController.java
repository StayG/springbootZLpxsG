package com.web.controller;

import com.web.service.*;
import com.web.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/list")
public class ListController {

    @Resource
    private CommonService commonService;
    @Resource
    private UsersService usersService;

    @GetMapping("/getClientHome")
    public Result getClientHome(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();

        Long todayAppointmentCount = commonService.todayAppointmentCount(null, null);

        map.put("todayAppointmentCount", todayAppointmentCount);
        return Result.success(map);
    }

    @GetMapping("/getAdminHome")
    public Result getAdminHome(HttpServletRequest request) {
        String role = (String) request.getSession().getAttribute("role");
        Integer merchant = null;
        if (role.equals("商家")) {
            // 根据今天的日期查询预约数量
            merchant = (Integer) request.getSession().getAttribute("userId");
        }
        Map<String, Object> map = new HashMap<>();

        Long totalUsers = usersService.lambdaQuery().count();
        Long todayAppointments = commonService.todayAppointmentCount(merchant, null);
        Long pendingTasks = commonService.todayAppointmentCount(merchant, 1);

        map.put("todayAppointments", todayAppointments);
        map.put("totalUsers", totalUsers);
        map.put("pendingTasks", pendingTasks);

        return Result.success(map);
    }

}
