package com.web.utils;

/**
 * 考试相关状态码常量（统一状态语义）
 */
public final class ExamStatusConstants {

    private ExamStatusConstants() {
    }

    // exam_info.status
    public static final int EXAM_NOT_STARTED = 0;
    public static final int EXAM_ONGOING = 1;
    public static final int EXAM_ENDED = 2;
    public static final int EXAM_FINISHED = 3;

    // exam_record.status
    public static final int RECORD_IN_PROGRESS = 0;
    public static final int RECORD_SUBMITTED = 1;
    public static final int RECORD_FORCED_SUBMIT = 2;
    public static final int RECORD_COMPLETED = 3;

    // exam_details.review_status
    public static final int REVIEW_PENDING = 0;
    public static final int REVIEW_DONE = 1;
}
