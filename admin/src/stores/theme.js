import { defineStore } from "pinia";


/**
 * 主题配置 Store
 * 管理所有样式配置，包括布局、颜色、字体等
 * 
 * 注意：初始值会从 theme.scss 中读取，确保与 CSS 变量保持同步
 */
export const useThemeStore = defineStore("theme", {
  state: () => {
    return {
      "layout": {
        "sidebarWidth": "240px",
        "headerHeight": "64px",
        "footerHeight": "56px",
        "showFooter": true,
        "showBreadcrumb": true,
        "fixedHeader": true,
        "fixedSidebar": true,
        "icon": "Management",
        "logoText": "在线考试系统",
        "logoSubtext": ""
      },
      "header": {
        "background": "#fdf2f8",
        "fontColor": "#303133",
        "borderRadiusTopLeft": 0,
        "borderRadiusTopRight": 0,
        "borderRadiusBottomRight": 0,
        "borderRadiusBottomLeft": 0,
        "borderTopWidth": 1,
        "borderTopStyle": "solid",
        "borderTopColor": "transparent",
        "borderRightWidth": 1,
        "borderRightStyle": "solid",
        "borderRightColor": "transparent",
        "borderBottomWidth": 1,
        "borderBottomStyle": "solid",
        "borderBottomColor": "#e4e7ed",
        "borderLeftWidth": 1,
        "borderLeftStyle": "solid",
        "borderLeftColor": "transparent",
        "paddingTop": 0,
        "paddingRight": 20,
        "paddingBottom": 0,
        "paddingLeft": 20,
        "marginTop": 0,
        "marginRight": 0,
        "marginBottom": 0,
        "marginLeft": 0,
        "breadcrumbBackground": "#fce7f3",
        "breadcrumbPaddingTop": 8,
        "breadcrumbPaddingRight": 16,
        "breadcrumbPaddingBottom": 8,
        "breadcrumbPaddingLeft": 16,
        "breadcrumbMarginTop": 0,
        "breadcrumbMarginRight": 0,
        "breadcrumbMarginBottom": 0,
        "breadcrumbMarginLeft": 0,
        "breadcrumbBorderRadiusTopLeft": 4,
        "breadcrumbBorderRadiusTopRight": 4,
        "breadcrumbBorderRadiusBottomRight": 4,
        "breadcrumbBorderRadiusBottomLeft": 4,
        "breadcrumbBorderTopWidth": 1,
        "breadcrumbBorderTopStyle": "solid",
        "breadcrumbBorderTopColor": "transparent",
        "breadcrumbBorderRightWidth": 1,
        "breadcrumbBorderRightStyle": "solid",
        "breadcrumbBorderRightColor": "transparent",
        "breadcrumbBorderBottomWidth": 1,
        "breadcrumbBorderBottomStyle": "solid",
        "breadcrumbBorderBottomColor": "transparent",
        "breadcrumbBorderLeftWidth": 1,
        "breadcrumbBorderLeftStyle": "solid",
        "breadcrumbBorderLeftColor": "transparent",
        "breadcrumbFontSize": 14,
        "breadcrumbFontColor": "#303133"
      },
      "sidebar": {
        "showLogo": true,
        "showLogoIcon": true,
        "headerBackground": "#fce7f3",
        "headerIconColor": "#EC4899",
        "headerFontColor": "#303133",
        "headerPaddingTop": 20,
        "headerPaddingRight": 20,
        "headerPaddingBottom": 20,
        "headerPaddingLeft": 20,
        "headerMarginTop": 0,
        "headerMarginRight": 0,
        "headerMarginBottom": 0,
        "headerMarginLeft": 0,
        "headerBorderTopStyle": "none",
        "headerBorderTopWidth": 1,
        "headerBorderTopColor": "#e4e7ed",
        "headerBorderRightStyle": "none",
        "headerBorderRightWidth": 1,
        "headerBorderRightColor": "#e4e7ed",
        "headerBorderBottomStyle": "solid",
        "headerBorderBottomWidth": 1,
        "headerBorderBottomColor": "#e4e7ed",
        "headerBorderLeftStyle": "none",
        "headerBorderLeftWidth": 1,
        "headerBorderLeftColor": "#e4e7ed",
        "menuAreaBackground": "#fdf2f8",
        "menuAreaPaddingTop": 10,
        "menuAreaPaddingRight": 10,
        "menuAreaPaddingBottom": 10,
        "menuAreaPaddingLeft": 10,
        "menuAreaMarginTop": 0,
        "menuAreaMarginRight": 0,
        "menuAreaMarginBottom": 0,
        "menuAreaMarginLeft": 0,
        "menuAreaBorderTopStyle": "none",
        "menuAreaBorderTopWidth": 1,
        "menuAreaBorderTopColor": "#e4e7ed",
        "menuAreaBorderRightStyle": "none",
        "menuAreaBorderRightWidth": 1,
        "menuAreaBorderRightColor": "#e4e7ed",
        "menuAreaBorderBottomStyle": "none",
        "menuAreaBorderBottomWidth": 1,
        "menuAreaBorderBottomColor": "#e4e7ed",
        "menuAreaBorderLeftStyle": "none",
        "menuAreaBorderLeftWidth": 1,
        "menuAreaBorderLeftColor": "#e4e7ed",
        "menuItemBackground": "",
        "menuItemHeight": 42,
        "showMenuIcon": true,
        "menuItemBorderRadiusTopLeft": 6,
        "menuItemBorderRadiusTopRight": 6,
        "menuItemBorderRadiusBottomRight": 6,
        "menuItemBorderRadiusBottomLeft": 6,
        "menuItemBorderTopStyle": "none",
        "menuItemBorderTopWidth": 1,
        "menuItemBorderTopColor": "transparent",
        "menuItemBorderRightStyle": "none",
        "menuItemBorderRightWidth": 1,
        "menuItemBorderRightColor": "transparent",
        "menuItemBorderBottomStyle": "none",
        "menuItemBorderBottomWidth": 1,
        "menuItemBorderBottomColor": "transparent",
        "menuItemBorderLeftStyle": "none",
        "menuItemBorderLeftWidth": 1,
        "menuItemBorderLeftColor": "transparent",
        "menuItemPaddingTop": 0,
        "menuItemPaddingRight": 16,
        "menuItemPaddingBottom": 0,
        "menuItemPaddingLeft": 16,
        "menuItemMarginTop": 0,
        "menuItemMarginRight": 0,
        "menuItemMarginBottom": 4,
        "menuItemMarginLeft": 0,
        "menuItemFontSize": 14,
        "menuItemFontWeight": 500,
        "textColor": "#303133",
        "menuItemHoverBackground": "#fce7f3",
        "hoverTextColor": "#EC4899",
        "menuItemActiveBackground": "#fce7f3",
        "homeMenuActiveIndicatorStyle": "none",
        "menuItemActiveBorderRadiusTopLeft": 6,
        "menuItemActiveBorderRadiusTopRight": 6,
        "menuItemActiveBorderRadiusBottomRight": 6,
        "menuItemActiveBorderRadiusBottomLeft": 6,
        "menuItemActiveBorderTopStyle": "none",
        "menuItemActiveBorderTopWidth": 2,
        "menuItemActiveBorderTopColor": "#EC4899",
        "menuItemActiveBorderRightStyle": "none",
        "menuItemActiveBorderRightWidth": 2,
        "menuItemActiveBorderRightColor": "#EC4899",
        "menuItemActiveBorderBottomStyle": "none",
        "menuItemActiveBorderBottomWidth": 2,
        "menuItemActiveBorderBottomColor": "#EC4899",
        "menuItemActiveBorderLeftStyle": "none",
        "menuItemActiveBorderLeftWidth": 2,
        "menuItemActiveBorderLeftColor": "#EC4899",
        "activeMenuItemFontWeight": 600,
        "activeTextColor": "#EC4899",
        "subMenuItemBackground": "",
        "subMenuItemHeight": 38,
        "subMenuItemBorderRadiusTopLeft": 6,
        "subMenuItemBorderRadiusTopRight": 6,
        "subMenuItemBorderRadiusBottomRight": 6,
        "subMenuItemBorderRadiusBottomLeft": 6,
        "subMenuItemBorderTopStyle": "none",
        "subMenuItemBorderTopWidth": 1,
        "subMenuItemBorderTopColor": "transparent",
        "subMenuItemBorderRightStyle": "none",
        "subMenuItemBorderRightWidth": 1,
        "subMenuItemBorderRightColor": "transparent",
        "subMenuItemBorderBottomStyle": "none",
        "subMenuItemBorderBottomWidth": 1,
        "subMenuItemBorderBottomColor": "transparent",
        "subMenuItemBorderLeftStyle": "none",
        "subMenuItemBorderLeftWidth": 1,
        "subMenuItemBorderLeftColor": "transparent",
        "subMenuItemPaddingTop": 0,
        "subMenuItemPaddingRight": 16,
        "subMenuItemPaddingBottom": 0,
        "subMenuItemPaddingLeft": 48,
        "subMenuItemMarginTop": 0,
        "subMenuItemMarginRight": 8,
        "subMenuItemMarginBottom": 2,
        "subMenuItemMarginLeft": 8,
        "subMenuItemFontSize": 13,
        "subMenuItemFontWeight": 500,
        "subMenuTextColor": "#606266",
        "subMenuItemHoverBackground": "#fce7f3",
        "subMenuHoverTextColor": "#EC4899",
        "subMenuItemActiveBackground": "#EC4899",
        "subMenuActiveIndicatorStyle": "dot",
        "subMenuItemActiveBorderRadiusTopLeft": 6,
        "subMenuItemActiveBorderRadiusTopRight": 6,
        "subMenuItemActiveBorderRadiusBottomRight": 6,
        "subMenuItemActiveBorderRadiusBottomLeft": 6,
        "subMenuItemActiveBorderTopStyle": "none",
        "subMenuItemActiveBorderTopWidth": 2,
        "subMenuItemActiveBorderTopColor": "#EC4899",
        "subMenuItemActiveBorderRightStyle": "none",
        "subMenuItemActiveBorderRightWidth": 2,
        "subMenuItemActiveBorderRightColor": "#EC4899",
        "subMenuItemActiveBorderBottomStyle": "none",
        "subMenuItemActiveBorderBottomWidth": 2,
        "subMenuItemActiveBorderBottomColor": "#EC4899",
        "subMenuItemActiveBorderLeftStyle": "none",
        "subMenuItemActiveBorderLeftWidth": 2,
        "subMenuItemActiveBorderLeftColor": "#EC4899",
        "activeSubMenuItemFontWeight": 600,
        "subMenuActiveTextColor": "#ffffff"
      },
      "content": {
        "background": "#fdf2f8",
        "padding": "20px",
        "homeThemeColor": "#EC4899",
        "card": {
          "background": "#ffffff",
          "borderRadius": 8,
          "shadowColor": "rgba(0, 0, 0, 0.06)",
          "paddingTop": 20,
          "paddingRight": 20,
          "paddingBottom": 20,
          "paddingLeft": 20,
          "borderTopStyle": "solid",
          "borderTopWidth": 1,
          "borderTopColor": "#e4e7ed",
          "borderRightStyle": "solid",
          "borderRightWidth": 1,
          "borderRightColor": "#e4e7ed",
          "borderBottomStyle": "solid",
          "borderBottomWidth": 1,
          "borderBottomColor": "#e4e7ed",
          "borderLeftStyle": "solid",
          "borderLeftWidth": 1,
          "borderLeftColor": "#e4e7ed",
          "tableHeaderBackground": "#fce7f3",
          "tableHeaderTextColor": "#EC4899",
          "tableHeaderBorderColor": "#f9a8d4",
          "tableRowHoverBackground": "#f0f9ff",
          "tableRowStripeBackground": "#fafafa"
        },
        "button": {
          "primary": {
            "background": "#1890ff",
            "color": "#ffffff",
            "borderColor": "#1890ff",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#40a9ff",
            "hoverColor": "#ffffff",
            "hoverBorderColor": "#40a9ff"
          },
          "default": {
            "background": "#ffffff",
            "color": "#606266",
            "borderColor": "#dcdfe6",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#f0f9ff",
            "hoverColor": "#1890ff",
            "hoverBorderColor": "#1890ff"
          },
          "success": {
            "background": "#67c23a",
            "color": "#ffffff",
            "borderColor": "#67c23a",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#85ce61",
            "hoverColor": "#ffffff",
            "hoverBorderColor": "#85ce61"
          },
          "danger": {
            "background": "#f56c6c",
            "color": "#ffffff",
            "borderColor": "#f56c6c",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#f78989",
            "hoverColor": "#ffffff",
            "hoverBorderColor": "#f78989"
          },
          "warning": {
            "background": "#e6a23c",
            "color": "#ffffff",
            "borderColor": "#e6a23c",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#ebb563",
            "hoverColor": "#ffffff",
            "hoverBorderColor": "#ebb563"
          },
          "info": {
            "background": "#909399",
            "color": "#ffffff",
            "borderColor": "#909399",
            "borderWidth": 1,
            "borderStyle": "solid",
            "borderRadiusTopLeft": 6,
            "borderRadiusTopRight": 6,
            "borderRadiusBottomRight": 6,
            "borderRadiusBottomLeft": 6,
            "hoverBackground": "#a6a9ad",
            "hoverColor": "#ffffff",
            "hoverBorderColor": "#a6a9ad"
          }
        }
      },
      "footer": {
        "background": "#fce7f3",
        "textColor": "#303133",
        "borderRadiusTopLeft": 0,
        "borderRadiusTopRight": 0,
        "borderRadiusBottomRight": 0,
        "borderRadiusBottomLeft": 0,
        "borderTopWidth": 4,
        "borderTopStyle": "solid",
        "borderTopColor": "#EC4899",
        "borderRightWidth": 0,
        "borderRightStyle": "solid",
        "borderRightColor": "transparent",
        "borderBottomWidth": 0,
        "borderBottomStyle": "solid",
        "borderBottomColor": "transparent",
        "borderLeftWidth": 0,
        "borderLeftStyle": "solid",
        "borderLeftColor": "transparent",
        "paddingTop": 0,
        "paddingRight": 24,
        "paddingBottom": 0,
        "paddingLeft": 24,
        "marginTop": 0,
        "marginRight": 0,
        "marginBottom": 0,
        "marginLeft": 0
      },
      "login": {
        "backgroundImage": "https://img.tukuppt.com/ad_preview/10/67/04/64f13796f0180.jpg!/fw/780",
        "title": "",
        "subtitle": "在线考试系统",
        "showIcon": false,
        "iconSize": 60,
        "iconColor": "#EC4899",
        "cardBackground": "rgba(255, 255, 255, 0.98)",
        "cardPaddingTop": 50,
        "cardPaddingRight": 45,
        "cardPaddingBottom": 50,
        "cardPaddingLeft": 45,
        "cardBorderRadiusTopLeft": 16,
        "cardBorderRadiusTopRight": 16,
        "cardBorderRadiusBottomRight": 16,
        "cardBorderRadiusBottomLeft": 16,
        "cardBorderTopStyle": "none",
        "cardBorderTopWidth": 1,
        "cardBorderTopColor": "transparent",
        "cardBorderRightStyle": "none",
        "cardBorderRightWidth": 1,
        "cardBorderRightColor": "transparent",
        "cardBorderBottomStyle": "none",
        "cardBorderBottomWidth": 1,
        "cardBorderBottomColor": "transparent",
        "cardBorderLeftStyle": "none",
        "cardBorderLeftWidth": 1,
        "cardBorderLeftColor": "transparent",
        "buttonBackground": "#EC4899",
        "buttonColor": "#ffffff",
        "buttonHeight": 48,
        "buttonBorderRadiusTopLeft": 10,
        "buttonBorderRadiusTopRight": 10,
        "buttonBorderRadiusBottomRight": 10,
        "buttonBorderRadiusBottomLeft": 10,
        "buttonBorderTopStyle": "none",
        "buttonBorderTopWidth": 1,
        "buttonBorderTopColor": "transparent",
        "buttonBorderRightStyle": "none",
        "buttonBorderRightWidth": 1,
        "buttonBorderRightColor": "transparent",
        "buttonBorderBottomStyle": "none",
        "buttonBorderBottomWidth": 1,
        "buttonBorderBottomColor": "transparent",
        "buttonBorderLeftStyle": "none",
        "buttonBorderLeftWidth": 1,
        "buttonBorderLeftColor": "transparent"
      }
    }
  },

  getters: {

    /**
     * 获取所有配置（用于导出）
     */
    getAllConfig: (state) => {
      return {
        layout: state.layout,
        header: state.header,
        sidebar: state.sidebar,
        content: state.content,
        footer: state.footer,
        login: state.login,
      };
    },
  },

  actions: {
    /**
     * 更新布局配置
     */
    updateLayout(config) {
      this.layout = { ...this.layout, ...config };
      this.applyTheme();
    },

    /**
     * 更新头部配置
     */
    updateHeader(config) {
      this.header = { ...this.header, ...config };
      this.applyTheme();
    },

    /**
     * 更新侧边栏配置
     */
    updateSidebar(config) {
      this.sidebar = { ...this.sidebar, ...config };
      this.applyTheme();
    },

    /**
     * 更新内容区域配置
     */
    updateContent(config) {
      // 深度合并 card 配置
      if (config.card) {
        this.content.card = { ...this.content.card, ...config.card };
        delete config.card;
      }
      // 深度合并 button 配置
      if (config.button) {
        // 合并每个按钮类型的配置
        Object.keys(config.button).forEach(type => {
          if (this.content.button[type]) {
            this.content.button[type] = { ...this.content.button[type], ...config.button[type] };
          }
        });
        delete config.button;
      }
      this.content = { ...this.content, ...config };
      this.applyTheme();
    },

    /**
     * 更新页脚配置
     */
    updateFooter(config) {
      this.footer = { ...this.footer, ...config };
      this.applyTheme();
    },

    /**
     * 更新登录页面配置
     */
    updateLogin(config) {
      this.login = { ...this.login, ...config };
      this.applyTheme();
    },

    /**
     * 更新其他配置
     */
    updateOther(config) {
      this.other = { ...this.other, ...config };
      this.applyTheme();
    },


    /**
     * 应用主题配置到 CSS 变量
     */
    applyTheme() {
      const root = document.documentElement;

      // 应用布局配置
      root.style.setProperty("--aside-width", this.layout.sidebarWidth);
      root.style.setProperty("--header-height", this.layout.headerHeight);
      root.style.setProperty("--footer-height", this.layout.footerHeight);

      // 应用Header区域样式
      root.style.setProperty("--sidebar-header-bg", this.sidebar.headerBackground);
      root.style.setProperty("--sidebar-header-icon-color", this.sidebar.headerIconColor || "rgba(64, 158, 255, 1)");
      root.style.setProperty("--sidebar-header-font-color", this.sidebar.headerFontColor || "#303133");
      const headerPadding = `${this.sidebar.headerPaddingTop}px ${this.sidebar.headerPaddingRight}px ${this.sidebar.headerPaddingBottom}px ${this.sidebar.headerPaddingLeft}px`;
      root.style.setProperty("--sidebar-header-padding", headerPadding);
      const headerMargin = `${this.sidebar.headerMarginTop}px ${this.sidebar.headerMarginRight}px ${this.sidebar.headerMarginBottom}px ${this.sidebar.headerMarginLeft}px`;
      root.style.setProperty("--sidebar-header-margin", headerMargin);
      root.style.setProperty("--sidebar-header-border-top-style", this.sidebar.headerBorderTopStyle);
      root.style.setProperty("--sidebar-header-border-top-width", `${this.sidebar.headerBorderTopWidth}px`);
      root.style.setProperty("--sidebar-header-border-top-color", this.sidebar.headerBorderTopColor);
      root.style.setProperty("--sidebar-header-border-right-style", this.sidebar.headerBorderRightStyle);
      root.style.setProperty("--sidebar-header-border-right-width", `${this.sidebar.headerBorderRightWidth}px`);
      root.style.setProperty("--sidebar-header-border-right-color", this.sidebar.headerBorderRightColor);
      root.style.setProperty("--sidebar-header-border-bottom-style", this.sidebar.headerBorderBottomStyle);
      root.style.setProperty("--sidebar-header-border-bottom-width", `${this.sidebar.headerBorderBottomWidth}px`);
      root.style.setProperty("--sidebar-header-border-bottom-color", this.sidebar.headerBorderBottomColor);
      root.style.setProperty("--sidebar-header-border-left-style", this.sidebar.headerBorderLeftStyle);
      root.style.setProperty("--sidebar-header-border-left-width", `${this.sidebar.headerBorderLeftWidth}px`);
      root.style.setProperty("--sidebar-header-border-left-color", this.sidebar.headerBorderLeftColor);

      // 应用Menu区域样式
      root.style.setProperty("--sidebar-menu-area-bg", this.sidebar.menuAreaBackground || "transparent");
      const menuAreaPadding = `${this.sidebar.menuAreaPaddingTop}px ${this.sidebar.menuAreaPaddingRight}px ${this.sidebar.menuAreaPaddingBottom}px ${this.sidebar.menuAreaPaddingLeft}px`;
      root.style.setProperty("--sidebar-menu-area-padding", menuAreaPadding);
      const menuAreaMargin = `${this.sidebar.menuAreaMarginTop}px ${this.sidebar.menuAreaMarginRight}px ${this.sidebar.menuAreaMarginBottom}px ${this.sidebar.menuAreaMarginLeft}px`;
      root.style.setProperty("--sidebar-menu-area-margin", menuAreaMargin);
      root.style.setProperty("--sidebar-menu-area-border-top-style", this.sidebar.menuAreaBorderTopStyle);
      root.style.setProperty("--sidebar-menu-area-border-top-width", `${this.sidebar.menuAreaBorderTopWidth}px`);
      root.style.setProperty("--sidebar-menu-area-border-top-color", this.sidebar.menuAreaBorderTopColor);
      root.style.setProperty("--sidebar-menu-area-border-right-style", this.sidebar.menuAreaBorderRightStyle);
      root.style.setProperty("--sidebar-menu-area-border-right-width", `${this.sidebar.menuAreaBorderRightWidth}px`);
      root.style.setProperty("--sidebar-menu-area-border-right-color", this.sidebar.menuAreaBorderRightColor);
      root.style.setProperty("--sidebar-menu-area-border-bottom-style", this.sidebar.menuAreaBorderBottomStyle);
      root.style.setProperty("--sidebar-menu-area-border-bottom-width", `${this.sidebar.menuAreaBorderBottomWidth}px`);
      root.style.setProperty("--sidebar-menu-area-border-bottom-color", this.sidebar.menuAreaBorderBottomColor);
      root.style.setProperty("--sidebar-menu-area-border-left-style", this.sidebar.menuAreaBorderLeftStyle);
      root.style.setProperty("--sidebar-menu-area-border-left-width", `${this.sidebar.menuAreaBorderLeftWidth}px`);
      root.style.setProperty("--sidebar-menu-area-border-left-color", this.sidebar.menuAreaBorderLeftColor);

      // 应用一级菜单背景
      root.style.setProperty("--menu-item-bg", this.sidebar.menuItemBackground || "transparent");
      root.style.setProperty("--menu-hover-bg", this.sidebar.menuItemHoverBackground);
      root.style.setProperty("--menu-active-bg", this.sidebar.menuItemActiveBackground);

      // 应用二级菜单背景
      root.style.setProperty("--sub-menu-item-bg", this.sidebar.subMenuItemBackground || "transparent");
      root.style.setProperty("--sub-menu-hover-bg", this.sidebar.subMenuItemHoverBackground);
      root.style.setProperty("--sub-menu-active-bg", this.sidebar.subMenuItemActiveBackground);

      // 应用一级菜单文字颜色
      root.style.setProperty("--menu-text-color", this.sidebar.textColor);
      root.style.setProperty("--menu-icon-color", this.sidebar.textColor);
      root.style.setProperty("--menu-hover-text-color", this.sidebar.hoverTextColor);
      root.style.setProperty("--menu-active-text-color", this.sidebar.activeTextColor);

      // 应用二级菜单文字颜色
      root.style.setProperty("--sub-menu-text-color", this.sidebar.subMenuTextColor);
      root.style.setProperty("--sub-menu-hover-text-color", this.sidebar.subMenuHoverTextColor);
      root.style.setProperty("--sub-menu-active-text-color", this.sidebar.subMenuActiveTextColor);

      // 应用一级菜单项样式
      root.style.setProperty("--menu-item-height", `${this.sidebar.menuItemHeight}px`);

      // 应用一级菜单内边距
      const menuItemPadding = `${this.sidebar.menuItemPaddingTop}px ${this.sidebar.menuItemPaddingRight}px ${this.sidebar.menuItemPaddingBottom}px ${this.sidebar.menuItemPaddingLeft}px`;
      root.style.setProperty("--menu-item-padding", menuItemPadding);
      root.style.setProperty("--menu-item-padding-top", `${this.sidebar.menuItemPaddingTop}px`);
      root.style.setProperty("--menu-item-padding-right", `${this.sidebar.menuItemPaddingRight}px`);
      root.style.setProperty("--menu-item-padding-bottom", `${this.sidebar.menuItemPaddingBottom}px`);
      root.style.setProperty("--menu-item-padding-left", `${this.sidebar.menuItemPaddingLeft}px`);

      // 应用一级菜单外边距
      const menuItemMargin = `${this.sidebar.menuItemMarginTop}px ${this.sidebar.menuItemMarginRight}px ${this.sidebar.menuItemMarginBottom}px ${this.sidebar.menuItemMarginLeft}px`;
      root.style.setProperty("--menu-item-margin", menuItemMargin);
      root.style.setProperty("--menu-item-margin-top", `${this.sidebar.menuItemMarginTop}px`);
      root.style.setProperty("--menu-item-margin-right", `${this.sidebar.menuItemMarginRight}px`);
      root.style.setProperty("--menu-item-margin-bottom", `${this.sidebar.menuItemMarginBottom}px`);
      root.style.setProperty("--menu-item-margin-left", `${this.sidebar.menuItemMarginLeft}px`);

      // 应用菜单项边框样式 - 分别设置上下左右
      // 默认状态 - 上边框
      root.style.setProperty("--menu-item-border-top-style", this.sidebar.menuItemBorderTopStyle);
      root.style.setProperty("--menu-item-border-top-width", `${this.sidebar.menuItemBorderTopWidth}px`);
      root.style.setProperty("--menu-item-border-top-color", this.sidebar.menuItemBorderTopColor);
      // 默认状态 - 右边框
      root.style.setProperty("--menu-item-border-right-style", this.sidebar.menuItemBorderRightStyle);
      root.style.setProperty("--menu-item-border-right-width", `${this.sidebar.menuItemBorderRightWidth}px`);
      root.style.setProperty("--menu-item-border-right-color", this.sidebar.menuItemBorderRightColor);
      // 默认状态 - 下边框
      root.style.setProperty("--menu-item-border-bottom-style", this.sidebar.menuItemBorderBottomStyle);
      root.style.setProperty("--menu-item-border-bottom-width", `${this.sidebar.menuItemBorderBottomWidth}px`);
      root.style.setProperty("--menu-item-border-bottom-color", this.sidebar.menuItemBorderBottomColor);
      // 默认状态 - 左边框
      root.style.setProperty("--menu-item-border-left-style", this.sidebar.menuItemBorderLeftStyle);
      root.style.setProperty("--menu-item-border-left-width", `${this.sidebar.menuItemBorderLeftWidth}px`);
      root.style.setProperty("--menu-item-border-left-color", this.sidebar.menuItemBorderLeftColor);
      // 圆角 - 四个方位
      const menuBorderRadius = `${this.sidebar.menuItemBorderRadiusTopLeft}px ${this.sidebar.menuItemBorderRadiusTopRight}px ${this.sidebar.menuItemBorderRadiusBottomRight}px ${this.sidebar.menuItemBorderRadiusBottomLeft}px`;
      root.style.setProperty("--menu-item-border-radius", menuBorderRadius);

      // 应用激活状态边框样式 - 分别设置上下左右
      // 激活状态 - 上边框
      root.style.setProperty("--menu-item-active-border-top-style", this.sidebar.menuItemActiveBorderTopStyle);
      root.style.setProperty("--menu-item-active-border-top-width", `${this.sidebar.menuItemActiveBorderTopWidth}px`);
      root.style.setProperty("--menu-item-active-border-top-color", this.sidebar.menuItemActiveBorderTopColor);
      // 激活状态 - 右边框
      root.style.setProperty("--menu-item-active-border-right-style", this.sidebar.menuItemActiveBorderRightStyle);
      root.style.setProperty("--menu-item-active-border-right-width", `${this.sidebar.menuItemActiveBorderRightWidth}px`);
      root.style.setProperty("--menu-item-active-border-right-color", this.sidebar.menuItemActiveBorderRightColor);
      // 激活状态 - 下边框
      root.style.setProperty("--menu-item-active-border-bottom-style", this.sidebar.menuItemActiveBorderBottomStyle);
      root.style.setProperty("--menu-item-active-border-bottom-width", `${this.sidebar.menuItemActiveBorderBottomWidth}px`);
      root.style.setProperty("--menu-item-active-border-bottom-color", this.sidebar.menuItemActiveBorderBottomColor);
      // 激活状态 - 左边框
      root.style.setProperty("--menu-item-active-border-left-style", this.sidebar.menuItemActiveBorderLeftStyle);
      root.style.setProperty("--menu-item-active-border-left-width", `${this.sidebar.menuItemActiveBorderLeftWidth}px`);
      root.style.setProperty("--menu-item-active-border-left-color", this.sidebar.menuItemActiveBorderLeftColor);
      // 激活圆角 - 四个方位
      const menuActiveBorderRadius = `${this.sidebar.menuItemActiveBorderRadiusTopLeft}px ${this.sidebar.menuItemActiveBorderRadiusTopRight}px ${this.sidebar.menuItemActiveBorderRadiusBottomRight}px ${this.sidebar.menuItemActiveBorderRadiusBottomLeft}px`;
      root.style.setProperty("--menu-item-active-border-radius", menuActiveBorderRadius);

      // 应用二级菜单样式
      root.style.setProperty("--sub-menu-item-height", `${this.sidebar.subMenuItemHeight}px`);

      // 应用二级菜单内边距
      const subMenuItemPadding = `${this.sidebar.subMenuItemPaddingTop}px ${this.sidebar.subMenuItemPaddingRight}px ${this.sidebar.subMenuItemPaddingBottom}px ${this.sidebar.subMenuItemPaddingLeft}px`;
      root.style.setProperty("--sub-menu-item-padding", subMenuItemPadding);
      root.style.setProperty("--sub-menu-item-padding-top", `${this.sidebar.subMenuItemPaddingTop}px`);
      root.style.setProperty("--sub-menu-item-padding-right", `${this.sidebar.subMenuItemPaddingRight}px`);
      root.style.setProperty("--sub-menu-item-padding-bottom", `${this.sidebar.subMenuItemPaddingBottom}px`);
      root.style.setProperty("--sub-menu-item-padding-left", `${this.sidebar.subMenuItemPaddingLeft}px`);

      // 应用二级菜单外边距
      const subMenuItemMargin = `${this.sidebar.subMenuItemMarginTop}px ${this.sidebar.subMenuItemMarginRight}px ${this.sidebar.subMenuItemMarginBottom}px ${this.sidebar.subMenuItemMarginLeft}px`;
      root.style.setProperty("--sub-menu-item-margin", subMenuItemMargin);
      root.style.setProperty("--sub-menu-item-margin-top", `${this.sidebar.subMenuItemMarginTop}px`);
      root.style.setProperty("--sub-menu-item-margin-right", `${this.sidebar.subMenuItemMarginRight}px`);
      root.style.setProperty("--sub-menu-item-margin-bottom", `${this.sidebar.subMenuItemMarginBottom}px`);
      root.style.setProperty("--sub-menu-item-margin-left", `${this.sidebar.subMenuItemMarginLeft}px`);

      // 应用二级菜单边框样式 - 默认状态
      root.style.setProperty("--sub-menu-item-border-top-style", this.sidebar.subMenuItemBorderTopStyle);
      root.style.setProperty("--sub-menu-item-border-top-width", `${this.sidebar.subMenuItemBorderTopWidth}px`);
      root.style.setProperty("--sub-menu-item-border-top-color", this.sidebar.subMenuItemBorderTopColor);
      root.style.setProperty("--sub-menu-item-border-right-style", this.sidebar.subMenuItemBorderRightStyle);
      root.style.setProperty("--sub-menu-item-border-right-width", `${this.sidebar.subMenuItemBorderRightWidth}px`);
      root.style.setProperty("--sub-menu-item-border-right-color", this.sidebar.subMenuItemBorderRightColor);
      root.style.setProperty("--sub-menu-item-border-bottom-style", this.sidebar.subMenuItemBorderBottomStyle);
      root.style.setProperty("--sub-menu-item-border-bottom-width", `${this.sidebar.subMenuItemBorderBottomWidth}px`);
      root.style.setProperty("--sub-menu-item-border-bottom-color", this.sidebar.subMenuItemBorderBottomColor);
      root.style.setProperty("--sub-menu-item-border-left-style", this.sidebar.subMenuItemBorderLeftStyle);
      root.style.setProperty("--sub-menu-item-border-left-width", `${this.sidebar.subMenuItemBorderLeftWidth}px`);
      root.style.setProperty("--sub-menu-item-border-left-color", this.sidebar.subMenuItemBorderLeftColor);

      // 应用二级菜单圆角 - 默认状态
      const subMenuItemBorderRadius = `${this.sidebar.subMenuItemBorderRadiusTopLeft}px ${this.sidebar.subMenuItemBorderRadiusTopRight}px ${this.sidebar.subMenuItemBorderRadiusBottomRight}px ${this.sidebar.subMenuItemBorderRadiusBottomLeft}px`;
      root.style.setProperty("--sub-menu-item-border-radius", subMenuItemBorderRadius);

      // 应用二级菜单边框样式 - 激活状态
      root.style.setProperty("--sub-menu-item-active-border-top-style", this.sidebar.subMenuItemActiveBorderTopStyle);
      root.style.setProperty("--sub-menu-item-active-border-top-width", `${this.sidebar.subMenuItemActiveBorderTopWidth}px`);
      root.style.setProperty("--sub-menu-item-active-border-top-color", this.sidebar.subMenuItemActiveBorderTopColor);
      root.style.setProperty("--sub-menu-item-active-border-right-style", this.sidebar.subMenuItemActiveBorderRightStyle);
      root.style.setProperty("--sub-menu-item-active-border-right-width", `${this.sidebar.subMenuItemActiveBorderRightWidth}px`);
      root.style.setProperty("--sub-menu-item-active-border-right-color", this.sidebar.subMenuItemActiveBorderRightColor);
      root.style.setProperty("--sub-menu-item-active-border-bottom-style", this.sidebar.subMenuItemActiveBorderBottomStyle);
      root.style.setProperty("--sub-menu-item-active-border-bottom-width", `${this.sidebar.subMenuItemActiveBorderBottomWidth}px`);
      root.style.setProperty("--sub-menu-item-active-border-bottom-color", this.sidebar.subMenuItemActiveBorderBottomColor);
      root.style.setProperty("--sub-menu-item-active-border-left-style", this.sidebar.subMenuItemActiveBorderLeftStyle);
      root.style.setProperty("--sub-menu-item-active-border-left-width", `${this.sidebar.subMenuItemActiveBorderLeftWidth}px`);
      root.style.setProperty("--sub-menu-item-active-border-left-color", this.sidebar.subMenuItemActiveBorderLeftColor);

      // 应用二级菜单圆角 - 激活状态
      const subMenuItemActiveBorderRadius = `${this.sidebar.subMenuItemActiveBorderRadiusTopLeft}px ${this.sidebar.subMenuItemActiveBorderRadiusTopRight}px ${this.sidebar.subMenuItemActiveBorderRadiusBottomRight}px ${this.sidebar.subMenuItemActiveBorderRadiusBottomLeft}px`;
      root.style.setProperty("--sub-menu-item-active-border-radius", subMenuItemActiveBorderRadius);

      // 应用系统首页激活指示器样式
      root.style.setProperty("--home-menu-active-indicator", this.sidebar.homeMenuActiveIndicatorStyle);

      // 应用二级菜单激活指示器样式
      root.style.setProperty("--sub-menu-active-indicator", this.sidebar.subMenuActiveIndicatorStyle);

      // 应用一级菜单文字样式
      root.style.setProperty("--menu-item-font-size", `${this.sidebar.menuItemFontSize}px`);
      root.style.setProperty("--menu-item-font-weight", this.sidebar.menuItemFontWeight);
      root.style.setProperty("--active-menu-item-font-weight", this.sidebar.activeMenuItemFontWeight);

      // 应用二级菜单文字样式
      root.style.setProperty("--sub-menu-item-font-size", `${this.sidebar.subMenuItemFontSize}px`);
      root.style.setProperty("--sub-menu-item-font-weight", this.sidebar.subMenuItemFontWeight);
      root.style.setProperty("--active-sub-menu-item-font-weight", this.sidebar.activeSubMenuItemFontWeight);

      // 应用头部配置
      root.style.setProperty("--header-bg-color", this.header.background);
      root.style.setProperty("--header-font-color", this.header.fontColor || "#303133");

      // 应用头部圆角
      const headerBorderRadius = `${this.header.borderRadiusTopLeft}px ${this.header.borderRadiusTopRight}px ${this.header.borderRadiusBottomRight}px ${this.header.borderRadiusBottomLeft}px`;
      root.style.setProperty("--header-border-radius", headerBorderRadius);

      // 应用头部边框
      root.style.setProperty("--header-border-top-width", `${this.header.borderTopWidth}px`);
      root.style.setProperty("--header-border-top-style", this.header.borderTopStyle);
      root.style.setProperty("--header-border-top-color", this.header.borderTopColor);
      root.style.setProperty("--header-border-right-width", `${this.header.borderRightWidth}px`);
      root.style.setProperty("--header-border-right-style", this.header.borderRightStyle);
      root.style.setProperty("--header-border-right-color", this.header.borderRightColor);
      root.style.setProperty("--header-border-bottom-width", `${this.header.borderBottomWidth}px`);
      root.style.setProperty("--header-border-bottom-style", this.header.borderBottomStyle);
      root.style.setProperty("--header-border-bottom-color", this.header.borderBottomColor);
      root.style.setProperty("--header-border-left-width", `${this.header.borderLeftWidth}px`);
      root.style.setProperty("--header-border-left-style", this.header.borderLeftStyle);
      root.style.setProperty("--header-border-left-color", this.header.borderLeftColor);

      // 应用头部内边距
      const topHeaderPadding = `${this.header.paddingTop}px ${this.header.paddingRight}px ${this.header.paddingBottom}px ${this.header.paddingLeft}px`;
      root.style.setProperty("--header-padding", topHeaderPadding);

      // 应用头部外边距
      const topHeaderMargin = `${this.header.marginTop}px ${this.header.marginRight}px ${this.header.marginBottom}px ${this.header.marginLeft}px`;
      root.style.setProperty("--header-margin", topHeaderMargin);

      // 应用面包屑配置
      root.style.setProperty("--breadcrumb-bg", this.header.breadcrumbBackground);
      const breadcrumbPadding = `${this.header.breadcrumbPaddingTop}px ${this.header.breadcrumbPaddingRight}px ${this.header.breadcrumbPaddingBottom}px ${this.header.breadcrumbPaddingLeft}px`;
      root.style.setProperty("--breadcrumb-padding", breadcrumbPadding);
      const breadcrumbMargin = `${this.header.breadcrumbMarginTop}px ${this.header.breadcrumbMarginRight}px ${this.header.breadcrumbMarginBottom}px ${this.header.breadcrumbMarginLeft}px`;
      root.style.setProperty("--breadcrumb-margin", breadcrumbMargin);

      // 应用面包屑圆角
      const breadcrumbBorderRadius = `${this.header.breadcrumbBorderRadiusTopLeft}px ${this.header.breadcrumbBorderRadiusTopRight}px ${this.header.breadcrumbBorderRadiusBottomRight}px ${this.header.breadcrumbBorderRadiusBottomLeft}px`;
      root.style.setProperty("--breadcrumb-border-radius", breadcrumbBorderRadius);

      // 应用面包屑边框
      root.style.setProperty("--breadcrumb-border-top-width", `${this.header.breadcrumbBorderTopWidth}px`);
      root.style.setProperty("--breadcrumb-border-top-style", this.header.breadcrumbBorderTopStyle);
      root.style.setProperty("--breadcrumb-border-top-color", this.header.breadcrumbBorderTopColor);
      root.style.setProperty("--breadcrumb-border-right-width", `${this.header.breadcrumbBorderRightWidth}px`);
      root.style.setProperty("--breadcrumb-border-right-style", this.header.breadcrumbBorderRightStyle);
      root.style.setProperty("--breadcrumb-border-right-color", this.header.breadcrumbBorderRightColor);
      root.style.setProperty("--breadcrumb-border-bottom-width", `${this.header.breadcrumbBorderBottomWidth}px`);
      root.style.setProperty("--breadcrumb-border-bottom-style", this.header.breadcrumbBorderBottomStyle);
      root.style.setProperty("--breadcrumb-border-bottom-color", this.header.breadcrumbBorderBottomColor);
      root.style.setProperty("--breadcrumb-border-left-width", `${this.header.breadcrumbBorderLeftWidth}px`);
      root.style.setProperty("--breadcrumb-border-left-style", this.header.breadcrumbBorderLeftStyle);
      root.style.setProperty("--breadcrumb-border-left-color", this.header.breadcrumbBorderLeftColor);

      // 应用面包屑文字样式
      root.style.setProperty("--breadcrumb-font-size", `${this.header.breadcrumbFontSize}px`);
      root.style.setProperty("--breadcrumb-font-color", this.header.breadcrumbFontColor);

      // 应用内容配置
      root.style.setProperty("--content-bg-color", this.content.background);
      root.style.setProperty("--content-padding", this.content.padding);
      root.style.setProperty("--home-theme-color", this.content.homeThemeColor || "rgba(64, 158, 255, 1)");

      // 应用统一卡片样式
      root.style.setProperty("--card-bg", this.content.card.background);
      root.style.setProperty("--card-border-radius", `${this.content.card.borderRadius}px`);
      root.style.setProperty("--card-shadow", `0 2px 12px ${this.content.card.shadowColor}`);

      // 应用卡片内边距（四个方向）
      const cardPadding = `${this.content.card.paddingTop}px ${this.content.card.paddingRight}px ${this.content.card.paddingBottom}px ${this.content.card.paddingLeft}px`;
      root.style.setProperty("--card-padding", cardPadding);
      root.style.setProperty("--card-padding-top", `${this.content.card.paddingTop}px`);
      root.style.setProperty("--card-padding-right", `${this.content.card.paddingRight}px`);
      root.style.setProperty("--card-padding-bottom", `${this.content.card.paddingBottom}px`);
      root.style.setProperty("--card-padding-left", `${this.content.card.paddingLeft}px`);

      // 应用卡片边框样式（组合值）
      const cardBorderTop = `${this.content.card.borderTopWidth}px ${this.content.card.borderTopStyle} ${this.content.card.borderTopColor}`;
      const cardBorderRight = `${this.content.card.borderRightWidth}px ${this.content.card.borderRightStyle} ${this.content.card.borderRightColor}`;
      const cardBorderBottom = `${this.content.card.borderBottomWidth}px ${this.content.card.borderBottomStyle} ${this.content.card.borderBottomColor}`;
      const cardBorderLeft = `${this.content.card.borderLeftWidth}px ${this.content.card.borderLeftStyle} ${this.content.card.borderLeftColor}`;

      root.style.setProperty("--card-border-top", cardBorderTop);
      root.style.setProperty("--card-border-right", cardBorderRight);
      root.style.setProperty("--card-border-bottom", cardBorderBottom);
      root.style.setProperty("--card-border-left", cardBorderLeft);

      // 应用表格特有样式
      root.style.setProperty("--table-header-bg", this.content.card.tableHeaderBackground);
      root.style.setProperty("--table-header-text-color", this.content.card.tableHeaderTextColor);
      root.style.setProperty("--table-header-border-color", this.content.card.tableHeaderBorderColor);
      root.style.setProperty("--table-row-hover-bg", this.content.card.tableRowHoverBackground);
      root.style.setProperty("--table-row-stripe-bg", this.content.card.tableRowStripeBackground);

      // 应用按钮样式配置
      // Primary 按钮
      root.style.setProperty("--btn-primary-bg", this.content.button.primary.background);
      root.style.setProperty("--btn-primary-color", this.content.button.primary.color);
      root.style.setProperty("--btn-primary-border-color", this.content.button.primary.borderColor);
      root.style.setProperty("--btn-primary-border-width", `${this.content.button.primary.borderWidth}px`);
      root.style.setProperty("--btn-primary-border-style", this.content.button.primary.borderStyle);
      const btnPrimaryBorderRadius = `${this.content.button.primary.borderRadiusTopLeft}px ${this.content.button.primary.borderRadiusTopRight}px ${this.content.button.primary.borderRadiusBottomRight}px ${this.content.button.primary.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-primary-border-radius", btnPrimaryBorderRadius);
      root.style.setProperty("--btn-primary-hover-bg", this.content.button.primary.hoverBackground);
      root.style.setProperty("--btn-primary-hover-color", this.content.button.primary.hoverColor);
      root.style.setProperty("--btn-primary-hover-border-color", this.content.button.primary.hoverBorderColor);

      // Default 按钮
      root.style.setProperty("--btn-default-bg", this.content.button.default.background);
      root.style.setProperty("--btn-default-color", this.content.button.default.color);
      root.style.setProperty("--btn-default-border-color", this.content.button.default.borderColor);
      root.style.setProperty("--btn-default-border-width", `${this.content.button.default.borderWidth}px`);
      root.style.setProperty("--btn-default-border-style", this.content.button.default.borderStyle);
      const btnDefaultBorderRadius = `${this.content.button.default.borderRadiusTopLeft}px ${this.content.button.default.borderRadiusTopRight}px ${this.content.button.default.borderRadiusBottomRight}px ${this.content.button.default.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-default-border-radius", btnDefaultBorderRadius);
      root.style.setProperty("--btn-default-hover-bg", this.content.button.default.hoverBackground);
      root.style.setProperty("--btn-default-hover-color", this.content.button.default.hoverColor);
      root.style.setProperty("--btn-default-hover-border-color", this.content.button.default.hoverBorderColor);

      // Success 按钮
      root.style.setProperty("--btn-success-bg", this.content.button.success.background);
      root.style.setProperty("--btn-success-color", this.content.button.success.color);
      root.style.setProperty("--btn-success-border-color", this.content.button.success.borderColor);
      root.style.setProperty("--btn-success-border-width", `${this.content.button.success.borderWidth}px`);
      root.style.setProperty("--btn-success-border-style", this.content.button.success.borderStyle);
      const btnSuccessBorderRadius = `${this.content.button.success.borderRadiusTopLeft}px ${this.content.button.success.borderRadiusTopRight}px ${this.content.button.success.borderRadiusBottomRight}px ${this.content.button.success.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-success-border-radius", btnSuccessBorderRadius);
      root.style.setProperty("--btn-success-hover-bg", this.content.button.success.hoverBackground);
      root.style.setProperty("--btn-success-hover-color", this.content.button.success.hoverColor);
      root.style.setProperty("--btn-success-hover-border-color", this.content.button.success.hoverBorderColor);

      // Danger 按钮
      root.style.setProperty("--btn-danger-bg", this.content.button.danger.background);
      root.style.setProperty("--btn-danger-color", this.content.button.danger.color);
      root.style.setProperty("--btn-danger-border-color", this.content.button.danger.borderColor);
      root.style.setProperty("--btn-danger-border-width", `${this.content.button.danger.borderWidth}px`);
      root.style.setProperty("--btn-danger-border-style", this.content.button.danger.borderStyle);
      const btnDangerBorderRadius = `${this.content.button.danger.borderRadiusTopLeft}px ${this.content.button.danger.borderRadiusTopRight}px ${this.content.button.danger.borderRadiusBottomRight}px ${this.content.button.danger.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-danger-border-radius", btnDangerBorderRadius);
      root.style.setProperty("--btn-danger-hover-bg", this.content.button.danger.hoverBackground);
      root.style.setProperty("--btn-danger-hover-color", this.content.button.danger.hoverColor);
      root.style.setProperty("--btn-danger-hover-border-color", this.content.button.danger.hoverBorderColor);

      // Warning 按钮
      root.style.setProperty("--btn-warning-bg", this.content.button.warning.background);
      root.style.setProperty("--btn-warning-color", this.content.button.warning.color);
      root.style.setProperty("--btn-warning-border-color", this.content.button.warning.borderColor);
      root.style.setProperty("--btn-warning-border-width", `${this.content.button.warning.borderWidth}px`);
      root.style.setProperty("--btn-warning-border-style", this.content.button.warning.borderStyle);
      const btnWarningBorderRadius = `${this.content.button.warning.borderRadiusTopLeft}px ${this.content.button.warning.borderRadiusTopRight}px ${this.content.button.warning.borderRadiusBottomRight}px ${this.content.button.warning.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-warning-border-radius", btnWarningBorderRadius);
      root.style.setProperty("--btn-warning-hover-bg", this.content.button.warning.hoverBackground);
      root.style.setProperty("--btn-warning-hover-color", this.content.button.warning.hoverColor);
      root.style.setProperty("--btn-warning-hover-border-color", this.content.button.warning.hoverBorderColor);

      // Info 按钮
      root.style.setProperty("--btn-info-bg", this.content.button.info.background);
      root.style.setProperty("--btn-info-color", this.content.button.info.color);
      root.style.setProperty("--btn-info-border-color", this.content.button.info.borderColor);
      root.style.setProperty("--btn-info-border-width", `${this.content.button.info.borderWidth}px`);
      root.style.setProperty("--btn-info-border-style", this.content.button.info.borderStyle);
      const btnInfoBorderRadius = `${this.content.button.info.borderRadiusTopLeft}px ${this.content.button.info.borderRadiusTopRight}px ${this.content.button.info.borderRadiusBottomRight}px ${this.content.button.info.borderRadiusBottomLeft}px`;
      root.style.setProperty("--btn-info-border-radius", btnInfoBorderRadius);
      root.style.setProperty("--btn-info-hover-bg", this.content.button.info.hoverBackground);
      root.style.setProperty("--btn-info-hover-color", this.content.button.info.hoverColor);
      root.style.setProperty("--btn-info-hover-border-color", this.content.button.info.hoverBorderColor);


      // 应用页脚配置
      root.style.setProperty("--footer-bg-color", this.footer.background);
      root.style.setProperty("--footer-text-color", this.footer.textColor);

      // 应用页脚圆角
      const footerBorderRadius = `${this.footer.borderRadiusTopLeft}px ${this.footer.borderRadiusTopRight}px ${this.footer.borderRadiusBottomRight}px ${this.footer.borderRadiusBottomLeft}px`;
      root.style.setProperty("--footer-border-radius", footerBorderRadius);

      // 应用页脚边框
      root.style.setProperty("--footer-border-top-width", `${this.footer.borderTopWidth}px`);
      root.style.setProperty("--footer-border-top-style", this.footer.borderTopStyle);
      root.style.setProperty("--footer-border-top-color", this.footer.borderTopColor);
      root.style.setProperty("--footer-border-right-width", `${this.footer.borderRightWidth}px`);
      root.style.setProperty("--footer-border-right-style", this.footer.borderRightStyle);
      root.style.setProperty("--footer-border-right-color", this.footer.borderRightColor);
      root.style.setProperty("--footer-border-bottom-width", `${this.footer.borderBottomWidth}px`);
      root.style.setProperty("--footer-border-bottom-style", this.footer.borderBottomStyle);
      root.style.setProperty("--footer-border-bottom-color", this.footer.borderBottomColor);
      root.style.setProperty("--footer-border-left-width", `${this.footer.borderLeftWidth}px`);
      root.style.setProperty("--footer-border-left-style", this.footer.borderLeftStyle);
      root.style.setProperty("--footer-border-left-color", this.footer.borderLeftColor);

      // 应用页脚内边距
      const footerPadding = `${this.footer.paddingTop}px ${this.footer.paddingRight}px ${this.footer.paddingBottom}px ${this.footer.paddingLeft}px`;
      root.style.setProperty("--footer-padding", footerPadding);

      // 应用页脚外边距
      const footerMargin = `${this.footer.marginTop}px ${this.footer.marginRight}px ${this.footer.marginBottom}px ${this.footer.marginLeft}px`;
      root.style.setProperty("--footer-margin", footerMargin);

      // 应用登录页样式
      root.style.setProperty("--login-icon-color", this.login.iconColor);
      root.style.setProperty("--login-button-bg", this.login.buttonBackground);
      root.style.setProperty("--login-button-color", this.login.buttonColor || "#ffffff");

    },

    /**
     * 重置为默认配置
     */
    resetToDefault() {
      this.$reset();
      this.applyTheme();
    },

    /**
     * 导入配置
     */
    importConfig(config) {
      Object.keys(config).forEach((key) => {
        if (this[key]) {
          this[key] = { ...this[key], ...config[key] };
        }
      });
      this.applyTheme();
    },

    /**
     * 导出配置为 JSON
     */
    exportConfig() {
      return JSON.stringify(this.getAllConfig, null, 2);
    },
  },

  // 持久化配置
  persist: {
    key: "theme-config",
    storage: localStorage,
  },
});

