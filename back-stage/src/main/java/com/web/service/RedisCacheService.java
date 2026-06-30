package com.web.service;

import com.web.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * Redis 缓存服务类
 * 提供统一的缓存管理方法
 */
@Slf4j
@Service
public class RedisCacheService {

    @Resource
    private RedisUtil redisUtil;

    // ==================== 缓存 Key 前缀常量 ====================
    
    private static final String EXAM_LIST_PREFIX = "exam:list:";
    private static final String EXAM_INFO_PREFIX = "exam:info:";
    private static final String QUESTION_INFO_PREFIX = "question:info:";
    private static final String QUESTION_LIST_PREFIX = "question:list:";
    private static final String PAPER_INFO_PREFIX = "paper:info:";
    private static final String PAPER_LIST_PREFIX = "paper:list:";
    private static final String PAPER_TOPICS_PREFIX = "paper:topics:";
    private static final String RECORD_INFO_PREFIX = "record:info:";
    private static final String RECORD_LIST_PREFIX = "record:list:";
    private static final String USER_INFO_PREFIX = "user:info:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String DICTIONARY_PREFIX = "dictionary:";
    private static final String DICTIONARY_LIST_PREFIX = "dictionary:list:";
    private static final String NOTICE_LIST_PREFIX = "notice:list:";
    private static final String NOTICE_INFO_PREFIX = "notice:info:";
    private static final String STATISTICS_PREFIX = "statistics:";
    private static final String SUBMIT_LOCK_PREFIX = "exam:submit:";
    private static final String SCREEN_SWITCH_PREFIX = "exam:screen:switch:";

    // ==================== 缓存过期时间常量（秒） ====================
    
    private static final long EXAM_LIST_EXPIRE = 300;      // 考试列表缓存 5 分钟
    private static final long EXAM_INFO_EXPIRE = 600;      // 考试详情缓存 10 分钟
    private static final long QUESTION_EXPIRE = 3600;      // 题目缓存 1 小时
    private static final long QUESTION_LIST_EXPIRE = 300;  // 题目列表缓存 5 分钟
    private static final long PAPER_EXPIRE = 600;          // 试卷缓存 10 分钟
    private static final long PAPER_LIST_EXPIRE = 300;     // 试卷列表缓存 5 分钟
    private static final long RECORD_EXPIRE = 300;         // 考试记录缓存 5 分钟
    private static final long RECORD_LIST_EXPIRE = 180;    // 考试记录列表缓存 3 分钟
    private static final long USER_EXPIRE = 1800;          // 用户信息缓存 30 分钟
    private static final long USER_SESSION_EXPIRE = 3600;  // 用户会话缓存 1 小时
    private static final long DICTIONARY_EXPIRE = 86400;   // 字典缓存 24 小时
    private static final long NOTICE_EXPIRE = 600;         // 公告缓存 10 分钟
    private static final long STATISTICS_EXPIRE = 300;     // 统计数据缓存 5 分钟
    private static final long SUBMIT_LOCK_EXPIRE = 10;     // 提交锁 10 秒

    // ==================== 考试相关缓存 ====================

    /**
     * 获取考试列表缓存
     */
    public Object getExamListCache(String key) {
        return redisUtil.get(EXAM_LIST_PREFIX + key);
    }

    /**
     * 设置考试列表缓存
     */
    public void setExamListCache(String key, Object value) {
        redisUtil.set(EXAM_LIST_PREFIX + key, value, EXAM_LIST_EXPIRE);
        log.info("缓存考试列表：{}", EXAM_LIST_PREFIX + key);
    }

    /**
     * 清除所有考试列表缓存
     */
    public void clearExamListCache() {
        redisUtil.deleteByPattern(EXAM_LIST_PREFIX + "*");
        log.info("清除考试列表缓存");
    }

    /**
     * 获取考试详情缓存
     */
    public Object getExamInfoCache(Integer examId) {
        return redisUtil.get(EXAM_INFO_PREFIX + examId);
    }

    /**
     * 设置考试详情缓存
     */
    public void setExamInfoCache(Integer examId, Object value) {
        redisUtil.set(EXAM_INFO_PREFIX + examId, value, EXAM_INFO_EXPIRE);
        log.info("缓存考试详情：{}", examId);
    }

    /**
     * 删除考试详情缓存
     */
    public void deleteExamInfoCache(Integer examId) {
        redisUtil.del(EXAM_INFO_PREFIX + examId);
        log.info("删除考试详情缓存：{}", examId);
    }

    // ==================== 题目相关缓存 ====================

    /**
     * 获取题目缓存
     */
    public Object getQuestionCache(Integer questionId) {
        return redisUtil.get(QUESTION_INFO_PREFIX + questionId);
    }

    /**
     * 设置题目缓存
     */
    public void setQuestionCache(Integer questionId, Object value) {
        redisUtil.set(QUESTION_INFO_PREFIX + questionId, value, QUESTION_EXPIRE);
        log.info("缓存题目信息：{}", questionId);
    }

    /**
     * 删除题目缓存
     */
    public void deleteQuestionCache(Integer questionId) {
        redisUtil.del(QUESTION_INFO_PREFIX + questionId);
        log.info("删除题目缓存：{}", questionId);
    }

    /**
     * 获取题目列表缓存
     */
    public Object getQuestionListCache(String key) {
        return redisUtil.get(QUESTION_LIST_PREFIX + key);
    }

    /**
     * 设置题目列表缓存
     */
    public void setQuestionListCache(String key, Object value) {
        redisUtil.set(QUESTION_LIST_PREFIX + key, value, QUESTION_LIST_EXPIRE);
        log.info("缓存题目列表：{}", QUESTION_LIST_PREFIX + key);
    }

    /**
     * 清除所有题目列表缓存（题目更新时调用）
     */
    public void clearQuestionListCache() {
        redisUtil.deleteByPattern(QUESTION_LIST_PREFIX + "*");
        log.info("清除题目列表缓存");
    }

    // ==================== 试卷相关缓存 ====================

    /**
     * 获取试卷信息缓存
     */
    public Object getPaperInfoCache(Integer paperId) {
        return redisUtil.get(PAPER_INFO_PREFIX + paperId);
    }

    /**
     * 设置试卷信息缓存
     */
    public void setPaperInfoCache(Integer paperId, Object value) {
        redisUtil.set(PAPER_INFO_PREFIX + paperId, value, PAPER_EXPIRE);
        log.info("缓存试卷信息：{}", paperId);
    }

    /**
     * 删除试卷信息缓存
     */
    public void deletePaperInfoCache(Integer paperId) {
        redisUtil.del(PAPER_INFO_PREFIX + paperId);
        log.info("删除试卷信息缓存：{}", paperId);
    }

    /**
     * 获取试卷题目列表缓存
     */
    public Object getPaperTopicsCache(Integer paperId) {
        return redisUtil.get(PAPER_TOPICS_PREFIX + paperId);
    }

    /**
     * 设置试卷题目列表缓存
     */
    public void setPaperTopicsCache(Integer paperId, Object value) {
        redisUtil.set(PAPER_TOPICS_PREFIX + paperId, value, PAPER_EXPIRE);
        log.info("缓存试卷题目列表：{}", paperId);
    }

    /**
     * 删除试卷题目列表缓存
     */
    public void deletePaperTopicsCache(Integer paperId) {
        redisUtil.del(PAPER_TOPICS_PREFIX + paperId);
        log.info("删除试卷题目列表缓存：{}", paperId);
    }

    /**
     * 获取试卷列表缓存
     */
    public Object getPaperListCache(String key) {
        return redisUtil.get(PAPER_LIST_PREFIX + key);
    }

    /**
     * 设置试卷列表缓存
     */
    public void setPaperListCache(String key, Object value) {
        redisUtil.set(PAPER_LIST_PREFIX + key, value, PAPER_LIST_EXPIRE);
        log.info("缓存试卷列表：{}", PAPER_LIST_PREFIX + key);
    }

    /**
     * 清除所有试卷列表缓存
     */
    public void clearPaperListCache() {
        redisUtil.deleteByPattern(PAPER_LIST_PREFIX + "*");
        log.info("清除试卷列表缓存");
    }

    // ==================== 考试记录相关缓存 ====================

    /**
     * 获取考试记录缓存
     */
    public Object getRecordInfoCache(String uuid) {
        return redisUtil.get(RECORD_INFO_PREFIX + uuid);
    }

    /**
     * 设置考试记录缓存
     */
    public void setRecordInfoCache(String uuid, Object value) {
        redisUtil.set(RECORD_INFO_PREFIX + uuid, value, RECORD_EXPIRE);
        log.info("缓存考试记录：{}", uuid);
    }

    /**
     * 删除考试记录缓存
     */
    public void deleteRecordInfoCache(String uuid) {
        redisUtil.del(RECORD_INFO_PREFIX + uuid);
        log.info("删除考试记录缓存：{}", uuid);
    }

    /**
     * 获取考试记录列表缓存
     */
    public Object getRecordListCache(String key) {
        return redisUtil.get(RECORD_LIST_PREFIX + key);
    }

    /**
     * 设置考试记录列表缓存
     */
    public void setRecordListCache(String key, Object value) {
        redisUtil.set(RECORD_LIST_PREFIX + key, value, RECORD_LIST_EXPIRE);
        log.info("缓存考试记录列表：{}", RECORD_LIST_PREFIX + key);
    }

    /**
     * 清除所有考试记录列表缓存
     */
    public void clearRecordListCache() {
        redisUtil.deleteByPattern(RECORD_LIST_PREFIX + "*");
        log.info("清除考试记录列表缓存");
    }

    // ==================== 用户相关缓存 ====================

    /**
     * 获取用户信息缓存
     */
    public Object getUserInfoCache(Integer userId) {
        return redisUtil.get(USER_INFO_PREFIX + userId);
    }

    /**
     * 设置用户信息缓存
     */
    public void setUserInfoCache(Integer userId, Object value) {
        redisUtil.set(USER_INFO_PREFIX + userId, value, USER_EXPIRE);
        log.info("缓存用户信息：{}", userId);
    }

    /**
     * 删除用户信息缓存
     */
    public void deleteUserInfoCache(Integer userId) {
        redisUtil.del(USER_INFO_PREFIX + userId);
        log.info("删除用户信息缓存：{}", userId);
    }

    /**
     * 获取用户会话缓存（登录用户）
     */
    public Object getUserSessionCache(Integer userId) {
        return redisUtil.get(USER_SESSION_PREFIX + userId);
    }

    /**
     * 设置用户会话缓存
     */
    public void setUserSessionCache(Integer userId, Object value) {
        redisUtil.set(USER_SESSION_PREFIX + userId, value, USER_SESSION_EXPIRE);
        log.info("缓存用户会话：{}", userId);
    }

    /**
     * 删除用户会话缓存（退出登录时）
     */
    public void deleteUserSessionCache(Integer userId) {
        redisUtil.del(USER_SESSION_PREFIX + userId);
        log.info("删除用户会话缓存：{}", userId);
    }

    // ==================== 字典相关缓存 ====================

    /**
     * 获取字典缓存
     */
    public Object getDictionaryCache(String type) {
        return redisUtil.get(DICTIONARY_PREFIX + type);
    }

    /**
     * 设置字典缓存
     */
    public void setDictionaryCache(String type, Object value) {
        redisUtil.set(DICTIONARY_PREFIX + type, value, DICTIONARY_EXPIRE);
        log.info("缓存字典数据：{}", type);
    }

    /**
     * 删除字典缓存
     */
    public void deleteDictionaryCache(String type) {
        redisUtil.del(DICTIONARY_PREFIX + type);
        log.info("删除字典缓存：{}", type);
    }

    /**
     * 获取字典列表缓存
     */
    public Object getDictionaryListCache(String key) {
        return redisUtil.get(DICTIONARY_LIST_PREFIX + key);
    }

    /**
     * 设置字典列表缓存
     */
    public void setDictionaryListCache(String key, Object value) {
        redisUtil.set(DICTIONARY_LIST_PREFIX + key, value, DICTIONARY_EXPIRE);
        log.info("缓存字典列表：{}", DICTIONARY_LIST_PREFIX + key);
    }

    /**
     * 清除所有字典缓存
     */
    public void clearAllDictionaryCache() {
        redisUtil.deleteByPattern(DICTIONARY_PREFIX + "*");
        redisUtil.deleteByPattern(DICTIONARY_LIST_PREFIX + "*");
        log.info("清除所有字典缓存");
    }

    // ==================== 公告相关缓存 ====================

    /**
     * 获取公告列表缓存
     */
    public Object getNoticeListCache(String key) {
        return redisUtil.get(NOTICE_LIST_PREFIX + key);
    }

    /**
     * 设置公告列表缓存
     */
    public void setNoticeListCache(String key, Object value) {
        redisUtil.set(NOTICE_LIST_PREFIX + key, value, NOTICE_EXPIRE);
        log.info("缓存公告列表：{}", NOTICE_LIST_PREFIX + key);
    }

    /**
     * 清除所有公告列表缓存
     */
    public void clearNoticeListCache() {
        redisUtil.deleteByPattern(NOTICE_LIST_PREFIX + "*");
        log.info("清除公告列表缓存");
    }

    /**
     * 获取公告详情缓存
     */
    public Object getNoticeInfoCache(Integer noticeId) {
        return redisUtil.get(NOTICE_INFO_PREFIX + noticeId);
    }

    /**
     * 设置公告详情缓存
     */
    public void setNoticeInfoCache(Integer noticeId, Object value) {
        redisUtil.set(NOTICE_INFO_PREFIX + noticeId, value, NOTICE_EXPIRE);
        log.info("缓存公告详情：{}", noticeId);
    }

    /**
     * 删除公告详情缓存
     */
    public void deleteNoticeInfoCache(Integer noticeId) {
        redisUtil.del(NOTICE_INFO_PREFIX + noticeId);
        log.info("删除公告详情缓存：{}", noticeId);
    }

    // ==================== 统计数据相关缓存 ====================

    /**
     * 获取统计数据缓存
     */
    public Object getStatisticsCache(String key) {
        return redisUtil.get(STATISTICS_PREFIX + key);
    }

    /**
     * 设置统计数据缓存
     */
    public void setStatisticsCache(String key, Object value) {
        redisUtil.set(STATISTICS_PREFIX + key, value, STATISTICS_EXPIRE);
        log.info("缓存统计数据：{}", STATISTICS_PREFIX + key);
    }

    /**
     * 删除统计数据缓存
     */
    public void deleteStatisticsCache(String key) {
        redisUtil.del(STATISTICS_PREFIX + key);
        log.info("删除统计数据缓存：{}", STATISTICS_PREFIX + key);
    }

    /**
     * 清除所有统计数据缓存
     */
    public void clearAllStatisticsCache() {
        redisUtil.deleteByPattern(STATISTICS_PREFIX + "*");
        log.info("清除所有统计数据缓存");
    }

    // ==================== 防重复提交 ====================

    /**
     * 检查是否正在提交中
     */
    public boolean isSubmitting(Integer userId, Integer examId) {
        String key = SUBMIT_LOCK_PREFIX + userId + ":" + examId;
        return redisUtil.hasKey(key);
    }

    /**
     * 设置提交锁
     */
    public void setSubmitLock(Integer userId, Integer examId) {
        String key = SUBMIT_LOCK_PREFIX + userId + ":" + examId;
        redisUtil.set(key, "1", SUBMIT_LOCK_EXPIRE);
        log.info("设置提交锁：userId={}, examId={}", userId, examId);
    }

    /**
     * 释放提交锁
     */
    public void releaseSubmitLock(Integer userId, Integer examId) {
        String key = SUBMIT_LOCK_PREFIX + userId + ":" + examId;
        redisUtil.del(key);
        log.info("释放提交锁：userId={}, examId={}", userId, examId);
    }

    // ==================== 切屏计数 ====================

    /**
     * 递增切屏次数
     */
    public long incrementScreenSwitch(String examRecordUuid) {
        String key = SCREEN_SWITCH_PREFIX + examRecordUuid;
        long count = redisUtil.incr(key, 1);
        
        // 第一次计数时设置过期时间（考试结束后 1 小时）
        if (count == 1) {
            redisUtil.expire(key, 3600);
        }
        
        log.info("切屏计数：uuid={}, count={}", examRecordUuid, count);
        return count;
    }

    /**
     * 获取切屏次数
     */
    public long getScreenSwitchCount(String examRecordUuid) {
        String key = SCREEN_SWITCH_PREFIX + examRecordUuid;
        Object count = redisUtil.get(key);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 清除切屏计数
     */
    public void clearScreenSwitchCount(String examRecordUuid) {
        String key = SCREEN_SWITCH_PREFIX + examRecordUuid;
        redisUtil.del(key);
        log.info("清除切屏计数：{}", examRecordUuid);
    }
}
