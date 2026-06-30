/**
 * 根据「科目」字典展示名称，推断填空/简答题默认 formula_type。
 * 与试题表 exam_question.formula_type、学生端工具栏一致。
 * 按「更具体的关键词」优先匹配，避免误伤（如「人文地理」含地理）。
 */

export function suggestFormulaTypeFromKemuLabel(label) {
  if (!label || typeof label !== "string") return "none";
  const s = label.trim();
  if (!s) return "none";

  if (/物理/.test(s)) return "physics";
  if (/化学/.test(s)) return "chemistry";
  if (/生物/.test(s)) return "biology";
  if (/地理/.test(s)) return "geography";
  if (/数学/.test(s)) return "math";

  // 语文、英语、政治、历史及未列出的科目：默认无公式栏
  return "none";
}
