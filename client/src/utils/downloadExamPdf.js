import axios from "axios";
import config from "@/config/config";
import { useUserStore } from "@/stores/user";

function parseFilenameFromContentDisposition(cd) {
  if (!cd || typeof cd !== "string") return null;
  const mUtf = cd.match(/filename\*=UTF-8''([^;\n]+)/i);
  if (mUtf) {
    try {
      return decodeURIComponent(mUtf[1].trim());
    } catch {
      return mUtf[1].trim();
    }
  }
  const mFn = cd.match(/filename="([^"]+)"/i);
  if (mFn) return mFn[1].trim();
  return null;
}

/**
 * 下载「完整答卷」PDF（与后端 Content-Disposition 文件名一致）
 * @param {string} examRecordUuidNumber
 * @param {string} [fallbackName] 无响应头时的备用文件名（需含 .pdf）
 */
export async function downloadFullAnswerPdf(examRecordUuidNumber, fallbackName) {
  const uuid = String(examRecordUuidNumber || "").trim();
  if (!uuid) {
    throw new Error("缺少成绩记录编号");
  }
  const userStore = useUserStore();
  const token = userStore.getToken();
  const url = `${config.baseUrl}/examRecord/exportFullAnswerPdf/${encodeURIComponent(uuid)}`;
  const res = await axios.get(url, {
    responseType: "blob",
    headers: token ? { Token: token } : {},
    timeout: 120000,
  });
  const ct = (res.headers["content-type"] || "").toLowerCase();
  if (ct.includes("application/json")) {
    const text = await res.data.text();
    let msg = "导出失败";
    try {
      const j = JSON.parse(text);
      msg = j.msg || msg;
    } catch {
      msg = text || msg;
    }
    throw new Error(msg);
  }
  const blob = res.data;
  const fromHeader = parseFilenameFromContentDisposition(res.headers["content-disposition"]);
  const name = fromHeader || fallbackName || `【成绩导出】-${uuid}.pdf`;
  const href = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = href;
  a.download = name;
  a.rel = "noopener";
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(href);
}
