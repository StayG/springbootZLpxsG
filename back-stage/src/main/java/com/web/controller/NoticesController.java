package com.web.controller;

import com.web.annotation.IgnoreAuth;
import com.web.domain.Req.NoticesReq;
import com.web.domain.Teachers;
import com.web.service.RedisCacheService;
import com.web.service.TeachersService;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import com.web.service.NoticesService;
import com.web.domain.Notices;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 公告 前端控制器
 * </p>
 *
 * @author HHW
 */
@Slf4j
@RestController
@RequestMapping("/notices")
public class NoticesController {

    @Resource
    private NoticesService noticesService;
    
    @Resource
    private TeachersService teachersService;
    
    @Resource
    private OperationLogUtil operationLogUtil;
    
    @Resource
    private RedisCacheService redisCacheService;

    /**
     * 后台分页查询（支持数据隔离）
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody NoticesReq req, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 注入角色信息
        req.setRole(role);
        
        // 如果是教师角色，注入教师 ID 和科目 ID 实现数据隔离
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                req.setTeacherId(userId);
                req.setKemuId(teacher.getKemuTypes());
            }
        }
        
        // 生成缓存 key
        String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                         (req.getKemuId() != null ? req.getKemuId() : "all") + ":" +
                         (req.getTeacherId() != null ? req.getTeacherId() : "all");
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getNoticeListCache(cacheKey);
        if (cachedData != null) {
            log.info("✅ 公告列表缓存命中：{}", cacheKey);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 公告列表缓存未命中，查询数据库：{}", cacheKey);
        PageUtils page = noticesService.selectPage(req);
        
        // 存入缓存（10分钟）
        redisCacheService.setNoticeListCache(cacheKey, page);
        
        return Result.success(page);
    }

    /**
     * 新增（自动关联当前用户信息）
     * 
     * @param notices
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody Notices notices, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名，如 "教师"
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名，如 "teachers"
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [公告保存] 开始 ==========");
        System.out.println("[公告保存] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[公告保存] 2. 接收到的前端数据 - title: " + notices.getTitle());
        System.out.println("[公告保存] 3. 前端数据中的 teacherId: " + notices.getTeacherId() + ", kemuId: " + notices.getKemuId());
        
        // 如果是教师角色（支持中英文判断），自动关联教师 ID 和科目 ID
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            System.out.println("[公告保存] 4. 检测到教师角色，开始查询教师信息...");
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                System.out.println("[公告保存] 5. 查询到教师信息 - 姓名: " + teacher.getRealName() + ", kemuTypes: " + teacher.getKemuTypes());
                notices.setTeacherId(userId);
                notices.setKemuId(teacher.getKemuTypes());
                System.out.println("[公告保存] 6. 已设置 - teacherId: " + notices.getTeacherId() + ", kemuId: " + notices.getKemuId());
            } else {
                System.out.println("[公告保存] ❌ 警告：未找到教师信息，userId=" + userId);
            }
        } else if ("managers".equals(tableName) || "管理员".equals(role)) {
            // 管理员发布公告，设置 teacher_id=0 表示系统公告
            System.out.println("[公告保存] 4. 检测到管理员角色，设置为系统公告");
            notices.setTeacherId(0);
            notices.setKemuId(0);
            System.out.println("[公告保存] 5. 已设置 - teacherId: " + notices.getTeacherId() + ", kemuId: " + notices.getKemuId());
        } else {
            System.out.println("[公告保存] ❌ 未知角色或未登录 - role: " + role + ", tableName: " + tableName);
        }
        
        System.out.println("[公告保存] 7. 最终保存的数据 - teacherId: " + notices.getTeacherId() + ", kemuId: " + notices.getKemuId());
        System.out.println("========== [公告保存] 结束 ==========\n");
        
        noticesService.save(notices);
        
        // 清除公告列表缓存
        redisCacheService.clearNoticeListCache();
        log.info("🗑️  清除公告列表缓存（新增公告）");
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "公告信息", "新增", "新增公告：" + notices.getTitle());
        
        return Result.success();
    }

    /**
     * 修改（保持教师信息不变）
     * 
     * @param notices
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Notices notices, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("[公告更新] role=" + role + ", tableName=" + tableName + ", userId=" + userId + ", notices.id=" + notices.getId());
        
        // 如果是教师角色，保持教师 ID 和科目 ID 不变（防止越权修改）
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                // 强制设置教师ID和科目ID，防止前端篡改
                notices.setTeacherId(userId);
                notices.setKemuId(teacher.getKemuTypes());
                System.out.println("[公告更新] 教师更新公告 - 强制设置teacherId=" + userId);
            }
        }
        // 管理员更新公告时，保持原有值不变（不强制覆盖）
        
        noticesService.updateById(notices);
        
        // 清除公告缓存
        redisCacheService.deleteNoticeInfoCache(notices.getId());
        redisCacheService.clearNoticeListCache();
        log.info("🗑️  清除公告缓存（更新公告 ID={}）", notices.getId());
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "公告信息", "修改", "修改公告：" + notices.getTitle());
        
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
        // 先从缓存获取
        Object cachedData = redisCacheService.getNoticeInfoCache(id);
        if (cachedData != null) {
            log.info("✅ 公告详情缓存命中：ID={}", id);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 公告详情缓存未命中，查询数据库：ID={}", id);
        Object noticeInfo = noticesService.info(id);
        
        // 存入缓存（10分钟）
        redisCacheService.setNoticeInfoCache(id, noticeInfo);
        
        return Result.success(noticeInfo);
    }

    /**
     * 根据id删除（带权限校验）
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 查询要删除的公告
        Notices notices = noticesService.getById(id);
        if (notices == null) {
            return Result.error("公告不存在");
        }
        
        // 如果是教师角色，只能删除自己发布的公告
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            if (!userId.equals(notices.getTeacherId())) {
                return Result.error("无权操作：只能删除自己发布的公告");
            }
        }
        
        String title = notices.getTitle();
        noticesService.removeById(id);
        
        // 清除公告缓存
        redisCacheService.deleteNoticeInfoCache(id);
        redisCacheService.clearNoticeListCache();
        log.info("🗑️  清除公告缓存（删除公告 ID={}）", id);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "公告信息", "删除", "删除公告：" + title);
        
        return Result.success();
    }

    /**
     * 批量删除（带权限校验）
     * 
     * @param ids
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody Integer[] ids, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 如果是教师角色，只能删除自己发布的公告
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            for (Integer id : ids) {
                Notices notices = noticesService.getById(id);
                if (notices == null) {
                    return Result.error("公告不存在：ID=" + id);
                }
                if (!userId.equals(notices.getTeacherId())) {
                    return Result.error("无权操作：只能删除自己发布的公告（ID=" + id + "）");
                }
            }
        }
        
        boolean success = noticesService.removeBatchByIds(Arrays.asList(ids));
        
        // 清除公告缓存
        if (success) {
            for (Integer id : ids) {
                redisCacheService.deleteNoticeInfoCache(id);
            }
            redisCacheService.clearNoticeListCache();
            log.info("🗑️  清除公告缓存（批量删除 {} 条公告）", ids.length);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "公告信息", "删除", "批量删除公告，共 " + ids.length + " 条");
        }
        
        return Result.success(success);
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/list")
    public Result list(@RequestBody NoticesReq req) {
        PageUtils page = noticesService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(noticesService.list());
    }

    /**
     * 测试公告缓存功能
     * 
     * @param kemuId 科目ID（可选）
     * @return 测试结果
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer kemuId) {
        log.info("\n========== 开始测试公告缓存 ==========");
        
        try {
            // 测试1：公告列表缓存
            NoticesReq req1 = new NoticesReq();
            req1.setPage(1);
            req1.setLimit(10);
            if (kemuId != null) {
                req1.setKemuId(kemuId);
            }
            
            long start1 = System.currentTimeMillis();
            PageUtils page1 = noticesService.selectPage(req1);
            long time1 = System.currentTimeMillis() - start1;
            log.info("第1次查询（无缓存）：耗时 {}ms，查询到 {} 条公告", time1, page1.getList() != null ? page1.getList().size() : 0);
            
            // 测试2：公告列表缓存命中
            NoticesReq req2 = new NoticesReq();
            req2.setPage(1);
            req2.setLimit(10);
            if (kemuId != null) {
                req2.setKemuId(kemuId);
            }
            
            long start2 = System.currentTimeMillis();
            PageUtils page2 = noticesService.selectPage(req2);
            long time2 = System.currentTimeMillis() - start2;
            log.info("第2次查询（缓存命中）：耗时 {}ms，查询到 {} 条公告", time2, page2.getList() != null ? page2.getList().size() : 0);
            
            // 测试3：公告详情缓存（如果有公告）
            Long time3 = null, time4 = null;
            if (page1.getList() != null && !page1.getList().isEmpty()) {
                Object firstItem = page1.getList().get(0);
                Integer noticeId = null;
                if (firstItem instanceof Notices) {
                    noticeId = ((Notices) firstItem).getId();
                }
                
                if (noticeId != null) {
                    long start3 = System.currentTimeMillis();
                    Notices notice1 = noticesService.getById(noticeId);
                    time3 = System.currentTimeMillis() - start3;
                    log.info("第3次查询（公告详情，无缓存）：耗时 {}ms，公告ID={}", time3, noticeId);
                    
                    long start4 = System.currentTimeMillis();
                    Notices notice2 = noticesService.getById(noticeId);
                    time4 = System.currentTimeMillis() - start4;
                    log.info("第4次查询（公告详情，缓存命中）：耗时 {}ms，公告ID={}", time4, noticeId);
                }
            }
            
            // 计算性能提升
            double improvement1 = time2 > 0 ? (double) time1 / time2 : 0;
            Double improvement2 = (time3 != null && time4 != null && time4 > 0) ? (double) time3 / time4 : null;
            
            log.info("========== 公告缓存测试完成 ==========\n");
            
            // 返回测试结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("公告列表第1次查询耗时(ms)", time1);
            result.put("公告列表第2次查询耗时(ms)", time2);
            result.put("公告列表性能提升", String.format("%.1f倍", improvement1));
            if (time3 != null && time4 != null) {
                result.put("公告详情第1次查询耗时(ms)", time3);
                result.put("公告详情第2次查询耗时(ms)", time4);
                result.put("公告详情性能提升", String.format("%.1f倍", improvement2));
            }
            result.put("查询到的公告数量", page1.getList() != null ? page1.getList().size() : 0);
            result.put("总公告数", page1.getTotal());
            result.put("测试说明", "第1次查询从数据库获取，第2次查询从Redis缓存获取");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试公告缓存失败", e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }

}
