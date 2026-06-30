package com.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.web.annotation.IgnoreAuth;
import com.web.domain.Managers;
import com.web.domain.Req.UsersReq;
import com.web.service.TokenService;
import com.web.service.RedisCacheService;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.PasswordUtil;
import com.web.utils.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import com.web.service.UsersService;
import com.web.domain.Users;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author HHW
 *
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    @Resource
    private UsersService usersService;

    @Resource
    private TokenService tokenService;
    
    @Resource
    private OperationLogUtil operationLogUtil;
    
    @Resource
    private RedisCacheService redisCacheService;

    @IgnoreAuth
    @RequestMapping(value = "/login")
    public Result login(String userName, String password, String captcha) {
        Users u = usersService.getOne(
                Wrappers.lambdaQuery(Users.class)
                        .eq(Users::getUserName, userName));
        if (u == null || !PasswordUtil.matches(password, u.getPassword())) {
            return Result.error("账号或密码错误");
        }
        // 检查用户状态，禁用的用户不允许登录
        if (u.getStatus() != null && u.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }
        
        // 登录成功，缓存用户信息（1小时）
        redisCacheService.setUserSessionCache(u.getId(), u);
        
        String token = tokenService.generateToken(u.getId(), userName, "users", "用户");
        return Result.success(token);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @param request
     * @return
     */
    @GetMapping("/session")
    public Result session(HttpServletRequest request) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 先从缓存获取
        Object cachedUser = redisCacheService.getUserSessionCache(userId);
        if (cachedUser != null) {
            // 缓存命中，直接返回
            Users users = (Users) cachedUser;
            // 再次检查状态（防止缓存期间被禁用）
            if (users.getStatus() != null && users.getStatus() == 0) {
                // 用户被禁用，清除缓存
                redisCacheService.deleteUserSessionCache(userId);
                return Result.error("账号已被禁用，无法登录");
            }
            return Result.success(users);
        }
        
        // 缓存未命中，查询数据库
        Users users = usersService.getById(userId);
        // 如果查到了数据且状态为 0（禁用），则拦截
        if (users != null && users.getStatus() != null && users.getStatus() == 0) {
            return Result.error("账号已被禁用，无法登录");
        }
        
        // 存入缓存（1小时）
        if (users != null) {
            redisCacheService.setUserSessionCache(userId, users);
        }
        
        return Result.success(users);
    }

    /**
     * 注册
     * 
     * @param users
     * @return
     */
    @IgnoreAuth
    @PostMapping("/register")
    public Result register(@RequestBody Users users) {
        // 检查用户名是否已存在
        if (usersService.getOne(
                Wrappers.lambdaQuery(Users.class)
                        .eq(Users::getUserName, users.getUserName())) != null) {
            return Result.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (users.getEmail() != null && !users.getEmail().isEmpty()) {
            if (usersService.getOne(
                    Wrappers.lambdaQuery(Users.class)
                            .eq(Users::getEmail, users.getEmail())) != null) {
                return Result.error("邮箱已被使用");
            }
        }
        
        // 加密密码
        if (users.getPassword() != null && !users.getPassword().isEmpty()) {
            users.setPassword(PasswordUtil.encode(users.getPassword()));
        }
        
        usersService.save(users);
        return Result.success();
    }

    /**
     * 修改密码
     * 
     * @param oldPassword
     * @param newPassword
     * @param request
     * @return
     */
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword, HttpServletRequest request) {
        Users u = usersService.getById((Integer) request.getSession().getAttribute("userId"));
        if (u != null && PasswordUtil.matches(oldPassword, u.getPassword())) {
            // 加密新密码
            u.setPassword(PasswordUtil.encode(newPassword));
            usersService.updateById(u);
            return Result.success();
        } else {
            return Result.error("旧密码错误");
        }

    }

    /**
     * 重置密码
     * 
     * @param users
     * @return
     */
    @IgnoreAuth
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody UsersReq req) {
        Users u = usersService.getOne(
                Wrappers.lambdaQuery(Users.class)
                        .eq(Users::getUserName, req.getUserName()));
        if (u == null) {
            return Result.error("用户名不存在");
        }
        // 加密新密码
        u.setPassword(PasswordUtil.encode(req.getPassword()));
        usersService.updateById(u);
        return Result.success();
    }

    /**
     * 退出
     */
    @RequestMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (token != null && !token.isEmpty()) {
            // 将token加入黑名单
            tokenService.addToBlacklist(token);
        }
        
        // 清除用户会话缓存
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId != null) {
            redisCacheService.deleteUserSessionCache(userId);
        }
        
        request.getSession().invalidate();
        return Result.success("退出成功");
    }

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody UsersReq req) {
        PageUtils page = usersService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 新增
     * 
     * @param users
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody Users users, HttpServletRequest request) {
        // 检查用户名是否已存在
        if (usersService.getOne(
                Wrappers.lambdaQuery(Users.class)
                        .eq(Users::getUserName, users.getUserName())) != null) {
            return Result.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (users.getEmail() != null && !users.getEmail().isEmpty()) {
            if (usersService.getOne(
                    Wrappers.lambdaQuery(Users.class)
                            .eq(Users::getEmail, users.getEmail())) != null) {
                return Result.error("邮箱已被使用");
            }
        }
        
        users.setBalance(0.0);
        // 如果前端没有传密码，使用默认密码
        if (users.getPassword() == null || users.getPassword().isEmpty()) {
            users.setPassword("123456");
        }
        // 加密密码
        users.setPassword(PasswordUtil.encode(users.getPassword()));
        usersService.save(users);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "用户管理", "新增", "新增用户：" + users.getUserName());
        
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param users
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Users users, HttpServletRequest request) {
        // 检查邮箱是否被其他用户使用
        if (users.getEmail() != null && !users.getEmail().isEmpty()) {
            Users existingUser = usersService.getOne(
                    Wrappers.lambdaQuery(Users.class)
                            .eq(Users::getEmail, users.getEmail())
                            .ne(Users::getId, users.getId()));
            if (existingUser != null) {
                return Result.error("邮箱已被其他用户使用");
            }
        }
        
        usersService.updateById(users);
        
        // 清除用户信息缓存
        redisCacheService.deleteUserInfoCache(users.getId());
        redisCacheService.deleteUserSessionCache(users.getId());
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "用户管理", "修改", "修改用户：" + users.getUserName());
        
        return Result.success();
    }

    /**
     * 根据id查询
     * 
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(usersService.info(id));
    }

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        Users user = usersService.getById(id);
        String userName = user != null ? user.getUserName() : "ID:" + id;
        boolean success = usersService.removeById(id);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "用户管理", "删除", "删除用户：" + userName);
        }
        
        return Result.success(success);
    }

    /**
     * 批量删除
     * 
     * @param ids
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids, HttpServletRequest request) {
        boolean success = usersService.removeBatchByIds(ids);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "用户管理", "删除", "批量删除用户，共 " + ids.size() + " 条");
        }
        
        return Result.success(success);
    }

    /**
     * 更新用户状态
     * 
     * @param req 包含 id 和 status
     * @return
     */
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestBody Map<String, Integer> req, HttpServletRequest request) {
        Integer id = req.get("id");
        Integer status = req.get("status");
        
        if (id == null || status == null) {
            return Result.error("参数错误");
        }

        Users users = usersService.getById(id);
        if (users == null) {
            return Result.error("用户不存在");
        }

        users.setStatus(status);
        usersService.updateById(users);
        
        // 清除用户信息缓存
        redisCacheService.deleteUserInfoCache(id);
        redisCacheService.deleteUserSessionCache(id);
        
        // 记录操作日志
        String action = status == 1 ? "启用" : "禁用";
        operationLogUtil.logSuccess(request, "用户管理", action, action + "用户：" + users.getUserName());
        
        return Result.success();
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @PostMapping("/list")
    public Result list(@RequestBody UsersReq req) {
        PageUtils page = usersService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(usersService.list());
    }

    /**
     * 测试用户缓存功能
     * 
     * @param userId 用户ID
     * @return
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam Integer userId) {
        try {
            long startTime1 = System.currentTimeMillis();
            
            // 第一次查询（应该从数据库查询）
            Users user1 = usersService.getById(userId);
            
            long endTime1 = System.currentTimeMillis();
            long time1 = endTime1 - startTime1;
            
            // 模拟缓存写入
            if (user1 != null) {
                redisCacheService.setUserSessionCache(userId, user1);
            }
            
            // 等待一小段时间
            Thread.sleep(100);
            
            long startTime2 = System.currentTimeMillis();
            
            // 第二次查询（应该从缓存获取）
            Object cachedUser = redisCacheService.getUserSessionCache(userId);
            
            long endTime2 = System.currentTimeMillis();
            long time2 = endTime2 - startTime2;
            
            // 检查 Redis 中是否有缓存
            boolean hasCached = cachedUser != null;
            
            // 构建测试结果
            java.util.Map<String, Object> testResult = new java.util.HashMap<>();
            testResult.put("测试类型", "用户信息缓存测试");
            testResult.put("用户ID", userId);
            testResult.put("用户名", user1 != null ? user1.getUserName() : "未找到");
            testResult.put("第一次查询耗时", time1 + "ms（从数据库）");
            testResult.put("第二次查询耗时", time2 + "ms（从缓存）");
            testResult.put("性能提升", time1 > 0 ? String.format("%.1f倍", (double)time1 / time2) : "N/A");
            testResult.put("缓存状态", hasCached ? "✅ 已缓存" : "❌ 未缓存");
            testResult.put("缓存Key", "user:session:" + userId);
            testResult.put("缓存过期时间", "1小时");
            
            if (time2 < time1 && hasCached) {
                testResult.put("测试结果", "✅ 缓存生效！第二次查询明显更快");
            } else if (!hasCached) {
                testResult.put("测试结果", "⚠️ 缓存未生效，请检查 Redis 连接");
            } else {
                testResult.put("测试结果", "⚠️ 性能提升不明显，可能是数据量太小");
            }
            
            // 应用场景说明
            testResult.put("应用场景", "登录用户信息查询、权限验证等高频操作");
            testResult.put("优化效果", "减少数据库查询，提升用户体验");
            
            return Result.success(testResult);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("测试结果", "❌ 测试失败");
            errorResult.put("错误信息", e.getMessage());
            return Result.error("缓存测试失败：" + e.getMessage());
        }
    }

}
