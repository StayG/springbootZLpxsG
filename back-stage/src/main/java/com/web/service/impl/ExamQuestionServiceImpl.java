package com.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamQuestion;
import com.web.domain.Req.ExamQuestionReq;
import com.web.domain.Resp.ExamQuestionResp;
import com.web.domain.dto.ExamQuestionExcelDto;
import com.web.mapper.ExamQuestionMapper;
import com.web.service.DictionaryService;
import com.web.service.ExamQuestionService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion>
        implements ExamQuestionService {

    @Resource
    private ExamQuestionMapper examQuestionMapper;

    @Resource
    private DictionaryService dictionaryService;

    @Override
    public PageUtils selectPage(ExamQuestionReq req) {
        Page<ExamQuestionResp> page = new Query<ExamQuestionResp>(req.getPageInfo()).getPage();
        List<ExamQuestionResp> records = examQuestionMapper.selectRespList(page, req);
        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamQuestionResp resp : records) {
                try {
                    if (resp != null) {
                        if (resp.getExamQuestionTypes() != null) {
                            String examQuestionTypesName = dictionaryService.getDictionaryName("exam_question_types",
                                    resp.getExamQuestionTypes());
                            if (examQuestionTypesName != null) {
                                resp.setExamQuestionTypesName(examQuestionTypesName);
                            }
                        }
                        if (resp.getKemuTypes() != null) {
                            String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                            if (kemuName != null) {
                                resp.setKemuTypesName(kemuName);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("填充字典名称失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public ExamQuestionResp info(Integer id) {
        ExamQuestion examQuestion = examQuestionMapper.selectById(id);
        if (examQuestion == null) {
            return null;
        }
        ExamQuestionResp resp = BeanUtil.copyProperties(examQuestion, ExamQuestionResp.class);
        if (dictionaryService != null && resp != null) {
            try {
                if (resp.getExamQuestionTypes() != null) {
                    String typeName = dictionaryService.getDictionaryName("exam_question_types",
                            resp.getExamQuestionTypes());
                    if (typeName != null) {
                        resp.setExamQuestionTypesName(typeName);
                    }
                }
                if (resp.getKemuTypes() != null) {
                    String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                    if (kemuName != null) {
                        resp.setKemuTypesName(kemuName);
                    }
                }
            } catch (Exception e) {
                System.err.println("填充字典名称失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return resp;
    }

    @Override
    public Map<String, Object> getCurrentQuestionCounts(String kemuTypes) {
        Map<String, Object> counts = new HashMap<>(8);

        counts.put("danXisting",
                this.count(new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamQuestionTypes, 1)
                        .eq(ExamQuestion::getKemuTypes, kemuTypes)));

        counts.put("duoXisting",
                this.count(new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamQuestionTypes, 2)
                        .eq(ExamQuestion::getKemuTypes, kemuTypes)));

        counts.put("panXisting",
                this.count(new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamQuestionTypes, 3)
                        .eq(ExamQuestion::getKemuTypes, kemuTypes)));

        counts.put("tianXisting",
                this.count(new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamQuestionTypes, 4)
                        .eq(ExamQuestion::getKemuTypes, kemuTypes)));

        counts.put("jianXisting",
                this.count(new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamQuestionTypes, 5)
                        .eq(ExamQuestion::getKemuTypes, kemuTypes)));

        counts.put("totalCount", this.count());

        return counts;
    }

    @Override
    public byte[] exportExcel(ExamQuestionReq req) {
        try {
            // 查询所有符合条件的试题
            List<ExamQuestion> examQuestions = baseMapper.selectList(buildQueryWrapper(req));

            // 转换为 DTO
            List<ExamQuestionExcelDto> dataList = new ArrayList<>();
            for (ExamQuestion question : examQuestions) {
                ExamQuestionExcelDto dto = new ExamQuestionExcelDto();
                BeanUtil.copyProperties(question, dto);
                dataList.add(dto);
            }

            // 写入 Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            EasyExcel.write(outputStream, ExamQuestionExcelDto.class)
                    .sheet("试题数据")
                    .doWrite(dataList);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出 Excel 失败：{}", e.getMessage(), e);
            throw new RuntimeException("导出 Excel 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ExamQuestion> buildQueryWrapper(ExamQuestionReq req) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();

        if (req != null) {
            // 试题名称模糊查询
            if (StrUtil.isNotBlank(req.getExamQuestionName())) {
                wrapper.like(ExamQuestion::getExamQuestionName, req.getExamQuestionName());
            }

            // 科目类型精确查询
            if (req.getKemuTypes() != null) {
                wrapper.eq(ExamQuestion::getKemuTypes, req.getKemuTypes());
            }

            // 试题类型精确查询
            if (req.getExamQuestionTypes() != null) {
                wrapper.eq(ExamQuestion::getExamQuestionTypes, req.getExamQuestionTypes());
            }
        }

        // 默认按排序字段降序排列
        wrapper.orderByDesc(ExamQuestion::getExamQuestionSequence);

        return wrapper;
    }

    @Override
    public Map<String, Object> importExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try {
            // 解析 Excel 文件
            List<ExamQuestionExcelDto> dataList = EasyExcel.read(file.getInputStream())
                    .head(ExamQuestionExcelDto.class)
                    .sheet()
                    .doReadSync();

            if (dataList == null || dataList.isEmpty()) {
                result.put("success", false);
                result.put("message", "Excel 文件为空或格式不正确");
                return result;
            }

            // 批量转换为 ExamQuestion 对象
            List<ExamQuestion> examQuestions = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                ExamQuestionExcelDto dto = dataList.get(i);
                try {
                    // 数据校验
                    String errorMsg = validateExamQuestion(dto, i + 1);
                    if (errorMsg != null) {
                        errorMessages.add(errorMsg);
                        failCount++;
                        continue;
                    }

                    // 转换为实体对象
                    ExamQuestion examQuestion = new ExamQuestion();
                    examQuestion.setExamQuestionName(dto.getExamQuestionName());
                    examQuestion.setExamQuestionOptions(dto.getExamQuestionOptions());
                    examQuestion.setExamQuestionAnswer(dto.getExamQuestionAnswer());
                    examQuestion.setExamQuestionAnalysis(dto.getExamQuestionAnalysis());
                    examQuestion.setExamQuestionTypes(dto.getExamQuestionTypes());
                    examQuestion.setDifficultyLevel(dto.getDifficultyLevel() != null ? dto.getDifficultyLevel() : 2);
                    examQuestion.setKnowledgePoint(dto.getKnowledgePoint());
                    examQuestion.setKemuTypes(dto.getKemuTypes());
                    examQuestion.setExamQuestionSequence(
                            dto.getExamQuestionSequence() != null ? dto.getExamQuestionSequence() : 0);

                    examQuestions.add(examQuestion);
                    successCount++;
                } catch (Exception e) {
                    log.error("第{}行数据转换失败：{}", i + 1, e.getMessage());
                    errorMessages.add("第" + (i + 1) + "行：数据转换失败 - " + e.getMessage());
                    failCount++;
                }
            }

            // 批量保存
            if (!examQuestions.isEmpty()) {
                this.saveBatch(examQuestions);
            }

            result.put("success", true);
            result.put("message", String.format("导入完成！成功：%d 条，失败：%d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errorMessages);

        } catch (IOException e) {
            log.error("读取 Excel 文件失败：{}", e.getMessage());
            result.put("success", false);
            result.put("message", "读取 Excel 文件失败：" + e.getMessage());
            result.put("errors", errorMessages);
        } catch (Exception e) {
            log.error("导入试题失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "导入试题失败：" + e.getMessage());
            result.put("errors", errorMessages);
        }

        return result;
    }

    /**
     * 验证试题数据
     * 
     * @param dto    试题 DTO
     * @param rowNum 行号
     * @return 错误信息，验证通过返回 null
     */
    private String validateExamQuestion(ExamQuestionExcelDto dto, int rowNum) {
        StringBuilder errorMsg = new StringBuilder();

        // 题干不能为空
        if (dto.getExamQuestionName() == null || dto.getExamQuestionName().trim().isEmpty()) {
            return "第" + rowNum + "行：试题题干不能为空";
        }

        // 试题类型不能为空
        if (dto.getExamQuestionTypes() == null) {
            return "第" + rowNum + "行：试题类型不能为空";
        }

        // 验证试题类型范围（支持简答题）
        if (dto.getExamQuestionTypes() < 1 || dto.getExamQuestionTypes() > 5) {
            return "第" + rowNum + "行：试题类型必须在 1-5 之间（1:单选 2:多选 3:判断 4:填空 5:简答）";
        }

        // 科目类型不能为空
        if (dto.getKemuTypes() == null) {
            return "第" + rowNum + "行：科目类型不能为空";
        }

        // 验证科目类型范围
        if (dto.getKemuTypes() < 1 || dto.getKemuTypes() > 9) {
            return "第" + rowNum + "行：科目类型必须在 1-9 之间";
        }

        // 知识点标签不能为空
        if (dto.getKnowledgePoint() == null || dto.getKnowledgePoint().trim().isEmpty()) {
            return "第" + rowNum + "行：知识点标签不能为空";
        }

        // 正确答案校验：简答题参考答案选填，其他题型必填
        if (dto.getExamQuestionTypes() != 5) {
            // 非简答题必须填写答案
            if (dto.getExamQuestionAnswer() == null || dto.getExamQuestionAnswer().trim().isEmpty()) {
                return "第" + rowNum + "行：正确答案不能为空";
            }
        } else {
            // 简答题：答案可以为空（人工批改）
            // 如果有答案，记录日志但不阻止导入
            if (dto.getExamQuestionAnswer() != null && !dto.getExamQuestionAnswer().trim().isEmpty()) {
                log.info("第{}行：简答题包含参考答案，将用于人工批改参考", rowNum);
            }
        }

        // 对于选择题和填空题，验证选项或答案格式
        if (dto.getExamQuestionTypes() == 1 || dto.getExamQuestionTypes() == 2) {
            // 单选题和多选题必须有选项
            if (dto.getExamQuestionOptions() == null || dto.getExamQuestionOptions().trim().isEmpty()) {
                return "第" + rowNum + "行：选择题必须填写选项（JSON 格式）";
            }
        } else if (dto.getExamQuestionTypes() == 3) {
            // 判断题：答案必须是 A 或 B
            if (dto.getExamQuestionAnswer() != null) {
                String answer = dto.getExamQuestionAnswer().trim();
                if (!"A".equals(answer) && !"B".equals(answer)) {
                    return "第" + rowNum + "行：判断题答案只能是 A 或 B";
                }
            }
        } else if (dto.getExamQuestionTypes() == 4) {
            // 填空题：答案必填且不能有选项
            if (dto.getExamQuestionOptions() != null && !dto.getExamQuestionOptions().trim().isEmpty()) {
                return "第" + rowNum + "行：填空题不应填写选项字段";
            }
        } else if (dto.getExamQuestionTypes() == 5) {
            // 简答题：不应有选项
            if (dto.getExamQuestionOptions() != null && !dto.getExamQuestionOptions().trim().isEmpty()) {
                return "第" + rowNum + "行：简答题不应填写选项字段";
            }
        }

        // 验证选项 JSON 格式
        if (dto.getExamQuestionOptions() != null && !dto.getExamQuestionOptions().trim().isEmpty()) {
            try {
                JSONUtil.parseObj(dto.getExamQuestionOptions());
            } catch (Exception e) {
                return "第" + rowNum + "行：选项格式不正确，必须是有效的 JSON 格式";
            }
        }

        return null;
    }

    @Override
    public boolean hasQuestionsForSubject(Integer kemuTypes) {
        if (kemuTypes == null) {
            return false;
        }
        try {
            long count = this.lambdaQuery()
                    .eq(ExamQuestion::getKemuTypes, kemuTypes)
                    .count();
            return count > 0;
        } catch (Exception e) {
            log.error("检查科目是否有试题失败: kemuTypes={}, error={}", kemuTypes, e.getMessage());
            return false;
        }
    }
}
