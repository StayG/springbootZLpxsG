export const formatCountdownHms = (totalSeconds) => {
  const safeSeconds = Math.max(Number(totalSeconds) || 0, 0);
  const hours = Math.floor(safeSeconds / 3600);
  const minutes = Math.floor((safeSeconds % 3600) / 60);
  const seconds = Math.floor(safeSeconds % 60);

  return `${String(hours).padStart(2, "0")} : ${String(minutes).padStart(2, "0")} : ${String(seconds).padStart(2, "0")}`;
};

export const formatCountdownHmsFromDiff = (diffMs) => {
  if (!diffMs || diffMs <= 0) return "00 : 00 : 00";
  const totalSeconds = Math.floor(diffMs / 1000);
  return formatCountdownHms(totalSeconds);
};

/**
 * 与后端 ExamInfoController.enterExam / computeSessionCapMinutesFromStart 一致：
 * 截止 = min(起点 + sessionCap 分钟, endMs)；sessionCap = min(ceil((endMs-起点)/60000), duration)，仅用起点与结束，不用「当前距结束」以免断点续考列表倒计时漂移。
 * @param {object} p
 * @param {number} p.nowMs 当前时间戳（未开考次时作 session 起点）
 * @param {number} p.endMs 考试结束时间戳
 * @param {number|null|undefined} p.durationMin 单次考试时长（分钟）
 * @param {number|null|undefined} p.recordStartMs 当前有效考试记录的 insertTime；未开考次传 null（以 nowMs 为起点）
 */
export const computeStudentAnswerDeadlineMs = ({ nowMs, endMs, durationMin, recordStartMs }) => {
  if (endMs == null || !Number.isFinite(endMs)) return nowMs;
  if (nowMs >= endMs) return endMs;
  const baseStart =
    recordStartMs != null && Number.isFinite(recordStartMs) ? recordStartMs : nowMs;
  const spanMs = endMs - baseStart;
  const maxMinFromStart = Math.max(0, Math.ceil(spanMs / (1000 * 60)));
  const sessionCapMin =
    durationMin != null && durationMin > 0 && Number.isFinite(durationMin)
      ? Math.min(maxMinFromStart, durationMin)
      : maxMinFromStart;
  const recordDeadline = baseStart + sessionCapMin * 60 * 1000;
  return Math.min(recordDeadline, endMs);
};
