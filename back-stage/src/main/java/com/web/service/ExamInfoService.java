package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamInfo;
import com.web.domain.Req.ExamInfoReq;
import com.web.utils.PageUtils;

/**
 * <p>
 * 考试信息 Service 接口
 * </p>
 */
public interface ExamInfoService extends IService<ExamInfo> {

    /**
     * 分页查询考试信息
     *
     * @param req 查询条件
     * @return 分页结果
     */
    PageUtils selectPage(ExamInfoReq req);

    /**
     * 根据ID查询考试详情（包含关联信息）
     *
     * @param id 考试ID
     * @return 考试详情
     */
    ExamInfo info(Integer id);

    /**
     * 发布考试
     *
     * @param id 考试ID
     * @return 错误信息，null表示成功
     */
    String publish(Integer id);

    /**
     * 取消发布考试
     *
     * @param id 考试ID
     * @return 取消发布结果
     */
    boolean unpublish(Integer id);

    /**
     * 学生端分页查询考试列表
     *
     * @param req 查询条件（包含当前登录用户ID）
     * @return 分页结果
     */
    com.web.utils.PageUtils selectStudentPage(com.web.domain.Req.ExamInfoReq req);

    /**
     * 是否存在状态为「已发布」或「进行中」的考试绑定该试卷
     */
    boolean hasReleasedOrOngoingExamUsingPaper(Integer examPaperId);

    /**
     * 是否存在「已发布/进行中」的考试，其试卷中包含该试题
     */
    boolean hasReleasedOrOngoingExamUsingQuestion(Integer examQuestionId);

    /**
     * 是否存在「已发布/进行中」的考试使用该科目（kemu_types）
     */
    boolean hasReleasedOrOngoingExamUsingKemuTypes(Integer kemuTypes);
}
