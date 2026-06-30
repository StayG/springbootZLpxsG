package com.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.web.annotation.IgnoreAuth;
import com.web.domain.Req.TeachersReq;
import com.web.service.TokenService;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.PasswordUtil;
import com.web.utils.Result;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import com.web.service.TeachersService;
import com.web.domain.Teachers;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 教师 前端控制器
 * </p>
 *
 * @author HHW
 *
 */
@RestController
@RequestMapping("/teachers")
public class TeachersController {

    @Resource
    private TeachersService teachersService;

    @Resource
    private TokenService tokenService;
    
    @Resource
    private OperationLogUtil operationLogUtil;

    @IgnoreAuth
    @RequestMapping(value = "/login")
    public Result login(String userName, String password, String captcha) {
        Teachers u = teachersService.getOne(
                Wrappers.lambdaQuery(Teachers.class)
                        .eq(Teachers::getUserName, userName));
        if (u == null || !PasswordUtil.matches(password, u.getPassword())) {
            return Result.error("账号或密码错误");
        }
        // 检查教师状态，禁用的教师不允许登录
        if (u.getStatus() != null && u.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }
        String token = tokenService.generateToken(u.getId(), userName, "teachers", "教师");
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
        Teachers teachers = teachersService.getById(userId);
        // 如果查到了数据且状态为 0（禁用），则拦截
        if (teachers != null && teachers.getStatus() != null && teachers.getStatus() == 0) {
            return Result.error("账号已被禁用，无法登录");
        }
        return Result.success(teachers);
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
        Teachers u = teachersService.getById((Integer) request.getSession().getAttribute("userId"));
        if (u != null && PasswordUtil.matches(oldPassword, u.getPassword())) {
            // 加密新密码
            u.setPassword(PasswordUtil.encode(newPassword));
            teachersService.updateById(u);
            return Result.success();
        } else {
            return Result.error("旧密码错误");
        }

    }

    /**
     * 重置密码
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody TeachersReq req) {
        Teachers u = teachersService.getOne(
                Wrappers.lambdaQuery(Teachers.class)
                        .eq(Teachers::getUserName, req.getUserName()));
        if (u == null) {
            return Result.error("用户名不存在");
        }
        // 加密新密码
        u.setPassword(PasswordUtil.encode(req.getPassword()));
        teachersService.updateById(u);
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
        request.getSession().invalidate();
        return Result.success("退出成功");
    }

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody TeachersReq req) {
        PageUtils page = teachersService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 新增
     * 
     * @param teachers
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody Teachers teachers, HttpServletRequest request) {
        teachersService.save(teachers);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "教师管理", "新增", "新增教师：" + teachers.getRealName());
        
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param teachers
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Teachers teachers, HttpServletRequest request) {
        teachersService.updateById(teachers);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "教师管理", "修改", "修改教师：" + teachers.getRealName());
        
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
        return Result.success(teachersService.info(id));
    }

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        Teachers teacher = teachersService.getById(id);
        String teacherName = teacher != null ? teacher.getRealName() : "ID:" + id;
        boolean success = teachersService.removeById(id);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "教师管理", "删除", "删除教师：" + teacherName);
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
        boolean success = teachersService.removeBatchByIds(ids);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "教师管理", "删除", "批量删除教师，共 " + ids.size() + " 条");
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

        Teachers teachers = teachersService.getById(id);
        if (teachers == null) {
            return Result.error("用户不存在");
        }

        teachers.setStatus(status);
        teachersService.updateById(teachers);
        
        // 记录操作日志
        String action = status == 1 ? "启用" : "禁用";
        operationLogUtil.logSuccess(request, "教师管理", action, action + "教师：" + teachers.getRealName());
        
        return Result.success();
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @PostMapping("/list")
    public Result list(@RequestBody TeachersReq req) {
        PageUtils page = teachersService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(teachersService.list());
    }

    /**
     * 批量导入教师
     * 
     * @param file Excel文件
     * @param request
     * @return
     */
    @PostMapping("/import")
    public Result importTeachers(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, HttpServletRequest request) {
        try {
            // 检查文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return Result.error("只支持 Excel 文件（.xlsx 或 .xls）");
            }
            
            // 读取 Excel 文件
            org.apache.poi.ss.usermodel.Workbook workbook;
            if (fileName.endsWith(".xlsx")) {
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            } else {
                workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(file.getInputStream());
            }
            
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errorMsg = new StringBuilder();
            
            // 科目映射
            java.util.Map<String, Integer> kemuMap = new java.util.HashMap<>();
            kemuMap.put("语文", 1);
            kemuMap.put("数学", 2);
            kemuMap.put("英语", 3);
            kemuMap.put("物理", 4);
            kemuMap.put("化学", 5);
            kemuMap.put("生物", 6);
            kemuMap.put("政治", 7);
            kemuMap.put("历史", 8);
            kemuMap.put("地理", 9);
            
            // 从第二行开始读取（第一行是表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                try {
                    // 读取单元格数据
                    String userName = getCellValue(row.getCell(0));
                    String realName = getCellValue(row.getCell(1));
                    String gender = getCellValue(row.getCell(2));
                    String email = getCellValue(row.getCell(3));
                    String phone = getCellValue(row.getCell(4));
                    String kemuName = getCellValue(row.getCell(5));
                    String title = getCellValue(row.getCell(6));
                    
                    // 验证必填字段
                    if (userName == null || userName.trim().isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：用户名不能为空；");
                        errorCount++;
                        continue;
                    }
                    if (realName == null || realName.trim().isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：真实姓名不能为空；");
                        errorCount++;
                        continue;
                    }
                    if (email == null || email.trim().isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：邮箱不能为空；");
                        errorCount++;
                        continue;
                    }
                    if (phone == null || phone.trim().isEmpty()) {
                        errorMsg.append("第").append(i + 1).append("行：电话不能为空；");
                        errorCount++;
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (teachersService.getOne(Wrappers.lambdaQuery(Teachers.class)
                            .eq(Teachers::getUserName, userName.trim())) != null) {
                        errorMsg.append("第").append(i + 1).append("行：用户名已存在；");
                        errorCount++;
                        continue;
                    }
                    
                    // 检查邮箱是否已存在
                    if (teachersService.getOne(Wrappers.lambdaQuery(Teachers.class)
                            .eq(Teachers::getEmail, email.trim())) != null) {
                        errorMsg.append("第").append(i + 1).append("行：邮箱已被使用；");
                        errorCount++;
                        continue;
                    }
                    
                    // 创建教师对象
                    Teachers teacher = new Teachers();
                    teacher.setUserName(userName.trim());
                    teacher.setRealName(realName.trim());
                    teacher.setGender(gender != null ? gender.trim() : "男");
                    teacher.setEmail(email.trim());
                    teacher.setPhone(phone.trim());
                    teacher.setPassword(PasswordUtil.encode("123456")); // 默认密码加密
                    teacher.setStatus(1); // 默认启用
                    
                    // 设置任教科目
                    if (kemuName != null && !kemuName.trim().isEmpty()) {
                        Integer kemuId = kemuMap.get(kemuName.trim());
                        if (kemuId != null) {
                            teacher.setKemuTypes(kemuId);
                        }
                    }
                    
                    // 设置职称
                    if (title != null && !title.trim().isEmpty()) {
                        teacher.setTitle(title.trim());
                    }
                    
                    // 保存教师
                    teachersService.save(teacher);
                    successCount++;
                    
                } catch (Exception e) {
                    errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("；");
                    errorCount++;
                }
            }
            
            workbook.close();
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "教师管理", "导入", 
                String.format("批量导入教师，成功 %d 条，失败 %d 条", successCount, errorCount));
            
            // 返回结果
            if (errorCount > 0) {
                String message = String.format("导入完成！成功 %d 条，失败 %d 条。失败原因：%s", 
                    successCount, errorCount, errorMsg.toString());
                Result<Integer> result = Result.success(successCount);
                result.setMsg(message);
                return result;
            } else {
                String message = String.format("导入成功！共导入 %d 条数据", successCount);
                Result<Integer> result = Result.success(successCount);
                result.setMsg(message);
                return result;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取单元格的值
     */
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    // 数字类型，转换为字符串（去除小数点）
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

}
