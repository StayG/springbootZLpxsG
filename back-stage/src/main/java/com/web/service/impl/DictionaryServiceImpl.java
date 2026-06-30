package com.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.Dictionary;
import com.web.domain.Req.DictionaryReq;
import com.web.domain.Resp.DictionaryResp;
import com.web.mapper.DictionaryMapper;
import com.web.service.DictionaryService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    @Resource
    private DictionaryMapper dictionaryMapper;

    @Override
    public PageUtils selectPage(DictionaryReq req) {
        try {
            if (req == null) {
                req = new DictionaryReq();
            }
            Page<DictionaryResp> page = new Query<DictionaryResp>(req.getPageInfo()).getPage();
            List<DictionaryResp> records = dictionaryMapper.selectRespList(page, req);
            if (records == null) {
                records = new ArrayList<>();
            }
            page.setRecords(records);
            return new PageUtils(page);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("???????" + e.getMessage());
            throw e;
        }
    }

    @Override
    public DictionaryResp info(Integer id) {
        return BeanUtil.copyProperties(dictionaryMapper.selectById(id), DictionaryResp.class);
    }

    @Override

    public void dictionaryConvert(Object obj, HttpServletRequest request) {
        try {
            if (obj == null)
                return;

            List<String> fieldNameList = new ArrayList<>();
            Class tempClass = obj.getClass();
            while (tempClass != null) {
                Field[] declaredFields = tempClass.getDeclaredFields();
                for (Field f : declaredFields) {
                    f.setAccessible(true);
                    if (f.getType().getName().equals("java.lang.Integer") && f.getName().contains("Types")) {
                        fieldNameList.add(f.getName());
                    }
                }
                tempClass = tempClass.getSuperclass();
            }

            ServletContext servletContext = request.getServletContext();
            @SuppressWarnings("unchecked")
            Map<String, Map<Integer, String>> dictionaryMap = (Map<String, Map<Integer, String>>) servletContext
                    .getAttribute("dictionaryMap");

            for (String s : fieldNameList) {
                Field types = null;
                if (hasField(obj.getClass(), s)) {
                    types = obj.getClass().getDeclaredField(s);
                } else {
                    types = obj.getClass().getSuperclass().getDeclaredField(s);
                }
                Field value = obj.getClass().getDeclaredField(s.replace("Types", "Value"));
                types.setAccessible(true);
                value.setAccessible(true);

                if (StrUtil.isNotEmpty(String.valueOf(types.get(obj)))) {
                    int i = Integer.parseInt(String.valueOf(types.get(obj)));
                    char[] chars = s.toCharArray();
                    StringBuffer sbf = new StringBuffer();
                    for (int b = 0; b < chars.length; b++) {
                        char ch = chars[b];
                        if (ch <= 90 && ch >= 65) {
                            sbf.append("_");
                            ch += 32;
                        }
                        sbf.append(ch);
                    }
                    System.out.println("dict key = " + sbf.toString() + ", code = " + i);
                    System.out
                            .println("dictionaryMap keys = " + (dictionaryMap == null ? null : dictionaryMap.keySet()));
                    String s2 = dictionaryMap.get(sbf.toString()).get(i);
                    value.set(obj, s2);
                } else {
                    value.set(obj, "");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasField(Class c, String fieldName) {
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (fieldName.equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDictionaryName(String dicCode, Integer codeIndex) {
        if (dicCode == null || dicCode.trim().isEmpty() || codeIndex == null) {
            return "";
        }
        try {
            Dictionary dictionary = this.lambdaQuery()
                    .eq(Dictionary::getDicCode, dicCode)
                    .eq(Dictionary::getCodeIndex, codeIndex)
                    .one();
            if (dictionary != null && dictionary.getIndexName() != null) {
                return dictionary.getIndexName();
            }
        } catch (Exception e) {
            System.err.println("???????dicCode=" + dicCode + ", codeIndex=" + codeIndex + ", error=" + e.getMessage());
        }
        return "";
    }

    @Override
    public boolean isSubjectNameExists(String indexName, Long excludeId) {
        if (indexName == null || indexName.trim().isEmpty()) {
            return false;
        }
        try {
            var query = this.lambdaQuery()
                    .eq(Dictionary::getDicCode, "kemu_types")
                    .eq(Dictionary::getIndexName, indexName.trim());
            
            // 编辑时排除当前记录
            if (excludeId != null) {
                query.ne(Dictionary::getId, excludeId);
            }
            
            long count = query.count();
            return count > 0;
        } catch (Exception e) {
            System.err.println("检查科目名称是否存在失败: " + e.getMessage());
            return false;
        }
    }
}
