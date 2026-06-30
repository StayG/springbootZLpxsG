import { nextTick } from "vue";

/** 管理端主内容区在 TheMain 的 el-scrollbar 内，进入详情等长页时回到顶部 */
export function scrollAdminLayoutToTop() {
  nextTick(() => {
    const wrap = document.querySelector(".content-main .el-scrollbar__wrap");
    if (wrap) wrap.scrollTop = 0;
  });
}
