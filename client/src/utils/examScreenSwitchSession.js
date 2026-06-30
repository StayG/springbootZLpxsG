import request from '@/utils/request.js';

/**
 * 考试中「禁止切屏」会话：供答题页与路由守卫共用同一套上报与去抖。
 * @type {null | {
 *   getExamRecordUuid: () => string,
 *   getAllowScreenSwitch: () => number,
 *   getScreenSwitchCount: () => number,
 *   isSubmitted: () => boolean,
 *   saveAnswersToServer: () => Promise<void>,
 *   onReported: (p: { screenSwitchCount: number, exceeded: boolean }) => Promise<void>
 * }}
 */
let ctx = null;

let lastReportAt = 0;
/** 同一瞬间 visibility + fullscreen + 路由 可能连续触发，合并为一次上报 */
const DEDUP_MS = 550;

export function registerExamScreenSwitchContext(c) {
  ctx = c;
}

export function unregisterExamScreenSwitchContext() {
  ctx = null;
  lastReportAt = 0;
}

/** 当前是否处于「禁止切屏」且未交卷的考试会话（供路由守卫判断） */
export function isActiveStrictScreenSwitchSession() {
  if (!ctx) return false;
  if (ctx.isSubmitted()) return false;
  return ctx.getAllowScreenSwitch() === 0;
}

/**
 * 上报一次切屏（服务端累计）。各入口（全屏退出 / visibility / 路由离开）共用。
 * @param {string} [_trigger] 调试用途
 * @returns {Promise<boolean>} 是否已发起并成功走完 onReported（去重未上报则为 false）
 */
export async function tryReportExamScreenSwitch(_trigger) {
  if (!isActiveStrictScreenSwitchSession()) return false;
  const uuid = ctx.getExamRecordUuid();
  if (!uuid) {
    console.warn('⚠️ 缺少 examRecordUuid，无法上报切屏记录');
    return false;
  }
  const now = Date.now();
  if (now - lastReportAt < DEDUP_MS) return false;
  lastReportAt = now;

  try {
    await ctx.saveAnswersToServer();
    const result = await request.post('examInfo/reportScreenSwitch', null, {
      params: { examRecordUuid: uuid }
    });
    const prev = ctx.getScreenSwitchCount();
    const count =
      result?.data?.screenSwitchCount ??
      result?.screenSwitchCount ??
      prev + 1;
    const exceeded = result?.data?.exceeded === true || result?.exceeded === true;
    await ctx.onReported({ screenSwitchCount: count, exceeded });
    return true;
  } catch (e) {
    console.error('上报切屏失败', e);
    return false;
  }
}
