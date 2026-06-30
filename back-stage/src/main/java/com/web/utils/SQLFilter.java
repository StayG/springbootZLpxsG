package com.web.utils;

import cn.hutool.core.util.StrUtil;
import com.web.exception.BusinessException;

public class SQLFilter {

    public static String sqlInject(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        // 去掉单引号、双引号、分号、反斜杠
        str = StrUtil.replace(str, "'", "");
        str = StrUtil.replace(str, "\"", "");
        str = StrUtil.replace(str, ";", "");
        str = StrUtil.replace(str, "\\", "");

        // 转换成小写
        str = str.toLowerCase();

        // 定义 SQL 关键词
        String[] keywords = { "master", "truncate", "insert", "select", "delete", "update", "declare", "alter",
                "drop" };

        // 检查是否包含非法关键词
        for (String keyword : keywords) {
            if (str.indexOf(keyword) != -1) {
                throw new BusinessException("包含非法字符");
            }
        }

        return str;
    }
}
