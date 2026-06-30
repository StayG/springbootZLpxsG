package com.web.utils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.web.domain.Managers;
import com.web.domain.Teachers;
import com.web.domain.Users;
import com.web.service.ManagersService;
import com.web.service.TeachersService;
import com.web.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码迁移工具
 * 用于将数据库中的明文密码批量转换为加密密码
 * 
 * 使用方法：
 * 1. 创建一个临时的 Controller 或测试类
 * 2. 注入 PasswordMigrationUtil
 * 3. 调用 migrateAllPasswords() 方法
 * 4. 迁移完成后删除相关代码
 * 
 * @author HHW
 */
@Component
public class PasswordMigrationUtil {

    @Resource
    private UsersService usersService;

    @Resource
    private TeachersService teachersService;

    @Resource
    private ManagersService managersService;

    /**
     * 迁移所有用户的密码
     * 将明文密码转换为 BCrypt 加密密码
     * 
     * @return 迁移结果统计
     */
    public MigrationResult migrateAllPasswords() {
        MigrationResult result = new MigrationResult();

        // 迁移用户表
        result.usersMigrated = migrateUsersPasswords();
        
        // 迁移教师表
        result.teachersMigrated = migrateTeachersPasswords();
        
        // 迁移管理员表
        result.managersMigrated = migrateManagersPasswords();

        return result;
    }

    /**
     * 迁移用户密码
     */
    private int migrateUsersPasswords() {
        List<Users> users = usersService.list();
        int count = 0;
        
        for (Users user : users) {
            String password = user.getPassword();
            // 检查密码是否已加密
            if (password != null && !PasswordUtil.isEncoded(password)) {
                // 加密明文密码
                user.setPassword(PasswordUtil.encode(password));
                usersService.updateById(user);
                count++;
                System.out.println("已迁移用户: " + user.getUserName() + " (ID: " + user.getId() + ")");
            }
        }
        
        return count;
    }

    /**
     * 迁移教师密码
     */
    private int migrateTeachersPasswords() {
        List<Teachers> teachers = teachersService.list();
        int count = 0;
        
        for (Teachers teacher : teachers) {
            String password = teacher.getPassword();
            // 检查密码是否已加密
            if (password != null && !PasswordUtil.isEncoded(password)) {
                // 加密明文密码
                teacher.setPassword(PasswordUtil.encode(password));
                teachersService.updateById(teacher);
                count++;
                System.out.println("已迁移教师: " + teacher.getRealName() + " (ID: " + teacher.getId() + ")");
            }
        }
        
        return count;
    }

    /**
     * 迁移管理员密码
     */
    private int migrateManagersPasswords() {
        List<Managers> managers = managersService.list();
        int count = 0;
        
        for (Managers manager : managers) {
            String password = manager.getPassword();
            // 检查密码是否已加密
            if (password != null && !PasswordUtil.isEncoded(password)) {
                // 加密明文密码
                manager.setPassword(PasswordUtil.encode(password));
                managersService.updateById(manager);
                count++;
                System.out.println("已迁移管理员: " + manager.getUserName() + " (ID: " + manager.getId() + ")");
            }
        }
        
        return count;
    }

    /**
     * 迁移结果统计
     */
    public static class MigrationResult {
        public int usersMigrated = 0;
        public int teachersMigrated = 0;
        public int managersMigrated = 0;

        public int getTotal() {
            return usersMigrated + teachersMigrated + managersMigrated;
        }

        @Override
        public String toString() {
            return String.format(
                "密码迁移完成！\n" +
                "- 用户: %d 条\n" +
                "- 教师: %d 条\n" +
                "- 管理员: %d 条\n" +
                "- 总计: %d 条",
                usersMigrated, teachersMigrated, managersMigrated, getTotal()
            );
        }
    }
}
