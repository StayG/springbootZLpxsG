/**
 * 学生端公告已读状态（按用户 ID 存在 localStorage，打开详情即标记已读）
 */
const STORAGE_KEY = "student_notice_read_notice_ids";

function loadMap() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

function saveMap(map) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(map));
}

function uidKey(userId) {
  return userId != null && userId !== "" ? String(userId) : "_";
}

export function getNoticeReadIdSet(userId) {
  const map = loadMap();
  const arr = map[uidKey(userId)];
  return new Set(Array.isArray(arr) ? arr.map((x) => Number(x)).filter(Boolean) : []);
}

/** @returns {boolean} true 表示未读 */
export function isNoticeUnread(userId, noticeId) {
  const id = Number(noticeId);
  if (!id) return false;
  return !getNoticeReadIdSet(userId).has(id);
}

export function markNoticeRead(userId, noticeId) {
  const id = Number(noticeId);
  if (!id) return;
  const key = uidKey(userId);
  const map = loadMap();
  const prev = Array.isArray(map[key]) ? map[key].map((x) => Number(x)).filter(Boolean) : [];
  if (prev.includes(id)) return;
  map[key] = [...prev, id];
  saveMap(map);
}
