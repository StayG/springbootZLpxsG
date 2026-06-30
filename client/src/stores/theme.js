import { defineStore } from "pinia";

/**
 * 主题配置 Store
 * 管理所有样式配置，包括布局、颜色、字体等
 */
export const useThemeStore = defineStore("theme", {
  state: () => {
    return {
      "layout": {
        "systemName": "在线考试平台",
        "systemIcon": "📝",
        "showSystemIcon": true
      },
      "header": {
        "background": "#0F766E",
        "topBarBackground": "#115E59",
        "textColor": "#ffffff",
        "border": {
          "top": {
            "style": "none",
            "width": 1,
            "color": "transparent"
          },
          "right": {
            "style": "none",
            "width": 1,
            "color": "transparent"
          },
          "bottom": {
            "style": "none",
            "width": 1,
            "color": "transparent"
          },
          "left": {
            "style": "none",
            "width": 1,
            "color": "transparent"
          }
        },
        "borderRadius": {
          "topLeft": 0,
          "topRight": 0,
          "bottomRight": 0,
          "bottomLeft": 0
        },
        "padding": {
          "top": 0,
          "right": 0,
          "bottom": 0,
          "left": 0
        },
        "margin": {
          "top": 0,
          "right": 0,
          "bottom": 0,
          "left": 0
        },
        "menu": {
          "defaultBackground": "transparent",
          "defaultTextColor": "#ffffff",
          "defaultIndicatorColor": "#ffffff",
          "defaultBorder": {
            "top": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            }
          },
          "defaultBorderRadius": {
            "topLeft": 4,
            "topRight": 4,
            "bottomRight": 4,
            "bottomLeft": 4
          },
          "defaultPadding": {
            "top": 12,
            "right": 20,
            "bottom": 12,
            "left": 20
          },
          "defaultMargin": {
            "top": 0,
            "right": 8,
            "bottom": 0,
            "left": 0
          },
          "hoverBackground": "rgba(255, 255, 255, 0.1)",
          "hoverTextColor": "#ffffff",
          "hoverIndicatorColor": "#14B8A6",
          "hoverBorder": {
            "top": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            }
          },
          "hoverBorderRadius": {
            "topLeft": 4,
            "topRight": 4,
            "bottomRight": 4,
            "bottomLeft": 4
          },
          "hoverPadding": {
            "top": 12,
            "right": 20,
            "bottom": 12,
            "left": 20
          },
          "hoverMargin": {
            "top": 0,
            "right": 8,
            "bottom": 0,
            "left": 0
          },
          "activeBackground": "rgba(255, 255, 255, 0.2)",
          "activeTextColor": "#ffffff",
          "activeIndicatorColor": "#14B8A6",
          "activeBorder": {
            "top": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            }
          },
          "activeBorderRadius": {
            "topLeft": 4,
            "topRight": 4,
            "bottomRight": 4,
            "bottomLeft": 4
          },
          "activePadding": {
            "top": 12,
            "right": 20,
            "bottom": 12,
            "left": 20
          },
          "activeMargin": {
            "top": 0,
            "right": 0,
            "bottom": 0,
            "left": 0
          }
        },
        "button": {
          "loginBackground": "rgba(255, 255, 255, 0.2)",
          "loginTextColor": "#ffffff",
          "loginBorder": {
            "top": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 1,
              "color": "transparent"
            }
          },
          "loginBorderRadius": {
            "topLeft": 10,
            "topRight": 10,
            "bottomRight": 10,
            "bottomLeft": 10
          },
          "exitBackground": "rgba(247, 105, 105, 1)",
          "exitTextColor": "#ffffff",
          "exitBorder": {
            "top": {
              "style": "solid",
              "width": 2,
              "color": "#ffffff"
            },
            "right": {
              "style": "solid",
              "width": 2,
              "color": "#ffffff"
            },
            "bottom": {
              "style": "solid",
              "width": 2,
              "color": "#ffffff"
            },
            "left": {
              "style": "solid",
              "width": 2,
              "color": "#ffffff"
            }
          },
          "exitBorderRadius": {
            "topLeft": 10,
            "topRight": 10,
            "bottomRight": 10,
            "bottomLeft": 10
          }
        }
      },
      "personal": {
        "left": {
          "background": "#115E59",
          "border": {
            "top": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            }
          },
          "borderRadius": {
            "topLeft": 4,
            "topRight": 4,
            "bottomRight": 4,
            "bottomLeft": 4
          },
          "padding": {
            "top": 40,
            "right": 0,
            "bottom": 40,
            "left": 0
          },
          "margin": {
            "top": 0,
            "right": 0,
            "bottom": 0,
            "left": 0
          },
          "tab": {
            "defaultBackground": "transparent",
            "defaultBorder": {
              "top": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "right": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "bottom": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "left": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              }
            },
            "defaultBorderRadius": {
              "topLeft": 0,
              "topRight": 0,
              "bottomRight": 0,
              "bottomLeft": 0
            },
            "defaultPadding": {
              "top": 14,
              "right": 40,
              "bottom": 14,
              "left": 40
            },
            "defaultMargin": {
              "top": 0,
              "right": 0,
              "bottom": 0,
              "left": 0
            },
            "activeBackground": "rgba(255, 255, 255, 0.2)",
            "activeBorder": {
              "top": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "right": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "bottom": {
                "style": "none",
                "width": 0,
                "color": "transparent"
              },
              "left": {
                "style": "solid",
                "width": 4,
                "color": "#ffffff"
              }
            },
            "activeBorderRadius": {
              "topLeft": 0,
              "topRight": 0,
              "bottomRight": 0,
              "bottomLeft": 0
            },
            "activePadding": {
              "top": 14,
              "right": 40,
              "bottom": 14,
              "left": 40
            },
            "activeMargin": {
              "top": 0,
              "right": 0,
              "bottom": 0,
              "left": 0
            }
          }
        },
        "right": {
          "background": "#f5f7fa",
          "themeColor": "#14B8A6",
          "border": {
            "top": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            }
          },
          "borderRadius": {
            "topLeft": 0,
            "topRight": 0,
            "bottomRight": 0,
            "bottomLeft": 0
          },
          "padding": {
            "top": 48,
            "right": 24,
            "bottom": 48,
            "left": 24
          },
          "margin": {
            "top": 0,
            "right": 0,
            "bottom": 0,
            "left": 0
          }
        }
      },
      "content": {
        "themeColor": "rgba(144, 147, 153, 1)",
        "homeThemeColor": "#0F766E",
        "list": {},
        "details": {}
      },
      "login": {
        "background": {
          "type": "image",
          "color": "#f5f7fa",
          "image": "https://img.shetu66.com/2023/08/02/1690968972119685.png"
        },
        "showTitle": true,
        "showIcon": true,
        "card": {
          "background": "#ffffff",
          "titleColor": "#0F766E",
          "borderRadius": {
            "topLeft": 16,
            "topRight": 16,
            "bottomRight": 16,
            "bottomLeft": 16
          },
          "border": {
            "top": {
              "style": "solid",
              "width": 1,
              "color": "#e8e8e8"
            },
            "right": {
              "style": "solid",
              "width": 1,
              "color": "#e8e8e8"
            },
            "bottom": {
              "style": "solid",
              "width": 1,
              "color": "#e8e8e8"
            },
            "left": {
              "style": "solid",
              "width": 1,
              "color": "#e8e8e8"
            }
          },
          "shadow": {
            "enabled": true,
            "x": 0,
            "y": 8,
            "blur": 32,
            "spread": 0,
            "color": "rgba(0, 0, 0, 0.1)"
          },
          "padding": {
            "top": 45,
            "right": 40,
            "bottom": 35,
            "left": 40
          },
          "margin": {
            "top": 0,
            "right": 0,
            "bottom": 0,
            "left": 0
          }
        },
        "button": {
          "background": "#14B8A6",
          "textColor": "#ffffff",
          "border": {
            "top": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "right": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "bottom": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            },
            "left": {
              "style": "none",
              "width": 0,
              "color": "transparent"
            }
          },
          "borderRadius": {
            "topLeft": 8,
            "topRight": 8,
            "bottomRight": 8,
            "bottomLeft": 8
          },
          "shadow": {
            "enabled": true,
            "x": 0,
            "y": 4,
            "blur": 12,
            "spread": 0,
            "color": "rgba(52, 152, 219, 0.3)"
          }
        }
      }
    };
  },

  getters: {

    /**
     * 获取所有配置（用于导出）
     */
    getAllConfig: (state) => {
      return {
        layout: state.layout,
        header: state.header,
        personal: state.personal,
        content: state.content,
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
     * 更新头部配置
     */
    updatePersonal(config) {
      this.personal = { ...this.personal, ...config };
      this.applyTheme();
    },

    /**
     * 更新内容区域配置
     */
    updateContent(config) {
      this.content = { ...this.content, ...config };
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
     * 应用主题配置到 CSS 变量
     */
    applyTheme() {
      const root = document.documentElement;

      // 应用头部样式
      root.style.setProperty("--header-bg", this.header.background);
      root.style.setProperty("--header-top-bar-bg", this.header.topBarBackground);
      root.style.setProperty("--header-text", this.header.textColor);

      // 应用头部整体样式
      root.style.setProperty("--header-border-top", `${this.header.border.top.width}px ${this.header.border.top.style} ${this.header.border.top.color}`);
      root.style.setProperty("--header-border-right", `${this.header.border.right.width}px ${this.header.border.right.style} ${this.header.border.right.color}`);
      root.style.setProperty("--header-border-bottom", `${this.header.border.bottom.width}px ${this.header.border.bottom.style} ${this.header.border.bottom.color}`);
      root.style.setProperty("--header-border-left", `${this.header.border.left.width}px ${this.header.border.left.style} ${this.header.border.left.color}`);
      root.style.setProperty("--header-border-radius", `${this.header.borderRadius.topLeft}px ${this.header.borderRadius.topRight}px ${this.header.borderRadius.bottomRight}px ${this.header.borderRadius.bottomLeft}px`);
      root.style.setProperty("--header-padding", `${this.header.padding.top}px ${this.header.padding.right}px ${this.header.padding.bottom}px ${this.header.padding.left}px`);
      root.style.setProperty("--header-margin", `${this.header.margin.top}px ${this.header.margin.right}px ${this.header.margin.bottom}px ${this.header.margin.left}px`);

      // 应用导航菜单默认样式
      root.style.setProperty("--menu-default-bg", this.header.menu.defaultBackground);
      root.style.setProperty("--menu-default-text", this.header.menu.defaultTextColor);
      root.style.setProperty("--menu-default-indicator-color", this.header.menu.defaultIndicatorColor);
      root.style.setProperty("--menu-default-border-top", `${this.header.menu.defaultBorder.top.width}px ${this.header.menu.defaultBorder.top.style} ${this.header.menu.defaultBorder.top.color}`);
      root.style.setProperty("--menu-default-border-right", `${this.header.menu.defaultBorder.right.width}px ${this.header.menu.defaultBorder.right.style} ${this.header.menu.defaultBorder.right.color}`);
      root.style.setProperty("--menu-default-border-bottom", `${this.header.menu.defaultBorder.bottom.width}px ${this.header.menu.defaultBorder.bottom.style} ${this.header.menu.defaultBorder.bottom.color}`);
      root.style.setProperty("--menu-default-border-left", `${this.header.menu.defaultBorder.left.width}px ${this.header.menu.defaultBorder.left.style} ${this.header.menu.defaultBorder.left.color}`);
      root.style.setProperty("--menu-default-border-radius", `${this.header.menu.defaultBorderRadius.topLeft}px ${this.header.menu.defaultBorderRadius.topRight}px ${this.header.menu.defaultBorderRadius.bottomRight}px ${this.header.menu.defaultBorderRadius.bottomLeft}px`);
      root.style.setProperty("--menu-default-padding", `${this.header.menu.defaultPadding.top}px ${this.header.menu.defaultPadding.right}px ${this.header.menu.defaultPadding.bottom}px ${this.header.menu.defaultPadding.left}px`);
      root.style.setProperty("--menu-default-margin", `${this.header.menu.defaultMargin.top}px ${this.header.menu.defaultMargin.right}px ${this.header.menu.defaultMargin.bottom}px ${this.header.menu.defaultMargin.left}px`);

      // 应用导航菜单选中样式
      root.style.setProperty("--menu-hover-bg", this.header.menu.hoverBackground);
      root.style.setProperty("--menu-hover-text", this.header.menu.hoverTextColor);
      root.style.setProperty("--menu-hover-indicator-color", this.header.menu.hoverIndicatorColor);
      root.style.setProperty("--menu-hover-border-top", `${this.header.menu.hoverBorder.top.width}px ${this.header.menu.hoverBorder.top.style} ${this.header.menu.hoverBorder.top.color}`);
      root.style.setProperty("--menu-hover-border-right", `${this.header.menu.hoverBorder.right.width}px ${this.header.menu.hoverBorder.right.style} ${this.header.menu.hoverBorder.right.color}`);
      root.style.setProperty("--menu-hover-border-bottom", `${this.header.menu.hoverBorder.bottom.width}px ${this.header.menu.hoverBorder.bottom.style} ${this.header.menu.hoverBorder.bottom.color}`);
      root.style.setProperty("--menu-hover-border-left", `${this.header.menu.hoverBorder.left.width}px ${this.header.menu.hoverBorder.left.style} ${this.header.menu.hoverBorder.left.color}`);
      root.style.setProperty("--menu-hover-border-radius", `${this.header.menu.hoverBorderRadius.topLeft}px ${this.header.menu.hoverBorderRadius.topRight}px ${this.header.menu.hoverBorderRadius.bottomRight}px ${this.header.menu.hoverBorderRadius.bottomLeft}px`);
      root.style.setProperty("--menu-hover-padding", `${this.header.menu.hoverPadding.top}px ${this.header.menu.hoverPadding.right}px ${this.header.menu.hoverPadding.bottom}px ${this.header.menu.hoverPadding.left}px`);
      root.style.setProperty("--menu-hover-margin", `${this.header.menu.hoverMargin.top}px ${this.header.menu.hoverMargin.right}px ${this.header.menu.hoverMargin.bottom}px ${this.header.menu.hoverMargin.left}px`);

      // 应用导航菜单激活样式
      root.style.setProperty("--menu-active-bg", this.header.menu.activeBackground);
      root.style.setProperty("--menu-active-text", this.header.menu.activeTextColor);
      root.style.setProperty("--menu-active-indicator-color", this.header.menu.activeIndicatorColor);
      root.style.setProperty("--menu-active-border-top", `${this.header.menu.activeBorder.top.width}px ${this.header.menu.activeBorder.top.style} ${this.header.menu.activeBorder.top.color}`);
      root.style.setProperty("--menu-active-border-right", `${this.header.menu.activeBorder.right.width}px ${this.header.menu.activeBorder.right.style} ${this.header.menu.activeBorder.right.color}`);
      root.style.setProperty("--menu-active-border-bottom", `${this.header.menu.activeBorder.bottom.width}px ${this.header.menu.activeBorder.bottom.style} ${this.header.menu.activeBorder.bottom.color}`);
      root.style.setProperty("--menu-active-border-left", `${this.header.menu.activeBorder.left.width}px ${this.header.menu.activeBorder.left.style} ${this.header.menu.activeBorder.left.color}`);
      root.style.setProperty("--menu-active-border-radius", `${this.header.menu.activeBorderRadius.topLeft}px ${this.header.menu.activeBorderRadius.topRight}px ${this.header.menu.activeBorderRadius.bottomRight}px ${this.header.menu.activeBorderRadius.bottomLeft}px`);
      root.style.setProperty("--menu-active-padding", `${this.header.menu.activePadding.top}px ${this.header.menu.activePadding.right}px ${this.header.menu.activePadding.bottom}px ${this.header.menu.activePadding.left}px`);
      root.style.setProperty("--menu-active-margin", `${this.header.menu.activeMargin.top}px ${this.header.menu.activeMargin.right}px ${this.header.menu.activeMargin.bottom}px ${this.header.menu.activeMargin.left}px`);

      // 应用头部登录/注册按钮样式
      root.style.setProperty("--header-buttonLogin-bg", this.header.button.loginBackground);
      root.style.setProperty("--header-buttonLogin-text", this.header.button.loginTextColor);
      root.style.setProperty("--header-buttonLogin-border-top", `${this.header.button.loginBorder.top.width}px ${this.header.button.loginBorder.top.style} ${this.header.button.loginBorder.top.color}`);
      root.style.setProperty("--header-buttonLogin-border-right", `${this.header.button.loginBorder.right.width}px ${this.header.button.loginBorder.right.style} ${this.header.button.loginBorder.right.color}`);
      root.style.setProperty("--header-buttonLogin-border-bottom", `${this.header.button.loginBorder.bottom.width}px ${this.header.button.loginBorder.bottom.style} ${this.header.button.loginBorder.bottom.color}`);
      root.style.setProperty("--header-buttonLogin-border-left", `${this.header.button.loginBorder.left.width}px ${this.header.button.loginBorder.left.style} ${this.header.button.loginBorder.left.color}`);
      root.style.setProperty("--header-buttonLogin-border-radius", `${this.header.button.loginBorderRadius.topLeft}px ${this.header.button.loginBorderRadius.topRight}px ${this.header.button.loginBorderRadius.bottomRight}px ${this.header.button.loginBorderRadius.bottomLeft}px`);

      // 应用头部退出按钮样式
      root.style.setProperty("--header-buttonExit-bg", this.header.button.exitBackground);
      root.style.setProperty("--header-buttonExit-text", this.header.button.exitTextColor);
      root.style.setProperty("--header-buttonExit-border-top", `${this.header.button.exitBorder.top.width}px ${this.header.button.exitBorder.top.style} ${this.header.button.exitBorder.top.color}`);
      root.style.setProperty("--header-buttonExit-border-right", `${this.header.button.exitBorder.right.width}px ${this.header.button.exitBorder.right.style} ${this.header.button.exitBorder.right.color}`);
      root.style.setProperty("--header-buttonExit-border-bottom", `${this.header.button.exitBorder.bottom.width}px ${this.header.button.exitBorder.bottom.style} ${this.header.button.exitBorder.bottom.color}`);
      root.style.setProperty("--header-buttonExit-border-left", `${this.header.button.exitBorder.left.width}px ${this.header.button.exitBorder.left.style} ${this.header.button.exitBorder.left.color}`);
      root.style.setProperty("--header-buttonExit-border-radius", `${this.header.button.exitBorderRadius.topLeft}px ${this.header.button.exitBorderRadius.topRight}px ${this.header.button.exitBorderRadius.bottomRight}px ${this.header.button.exitBorderRadius.bottomLeft}px`);

      // 个人中心卡片 左侧样式
      root.style.setProperty("--personal-left-bg", this.personal.left.background);
      root.style.setProperty("--personal-left-border-top", `${this.personal.left.border.top.width}px ${this.personal.left.border.top.style} ${this.personal.left.border.top.color}`);
      root.style.setProperty("--personal-left-border-right", `${this.personal.left.border.right.width}px ${this.personal.left.border.right.style} ${this.personal.left.border.right.color}`);
      root.style.setProperty("--personal-left-border-bottom", `${this.personal.left.border.bottom.width}px ${this.personal.left.border.bottom.style} ${this.personal.left.border.bottom.color}`);
      root.style.setProperty("--personal-left-border-left", `${this.personal.left.border.left.width}px ${this.personal.left.border.left.style} ${this.personal.left.border.left.color}`);
      root.style.setProperty("--personal-left-border-radius", `${this.personal.left.borderRadius.topLeft}px ${this.personal.left.borderRadius.topRight}px ${this.personal.left.borderRadius.bottomRight}px ${this.personal.left.borderRadius.bottomLeft}px`);
      root.style.setProperty("--personal-left-padding", `${this.personal.left.padding.top}px ${this.personal.left.padding.right}px ${this.personal.left.padding.bottom}px ${this.personal.left.padding.left}px`);
      root.style.setProperty("--personal-left-margin", `${this.personal.left.margin.top}px ${this.personal.left.margin.right}px ${this.personal.left.margin.bottom}px ${this.personal.left.margin.left}px`);

      // 个人中心左侧导航标签默认样式
      root.style.setProperty("--personal-tab-default-bg", this.personal.left.tab.defaultBackground);
      root.style.setProperty("--personal-tab-default-border-top", `${this.personal.left.tab.defaultBorder.top.width}px ${this.personal.left.tab.defaultBorder.top.style} ${this.personal.left.tab.defaultBorder.top.color}`);
      root.style.setProperty("--personal-tab-default-border-right", `${this.personal.left.tab.defaultBorder.right.width}px ${this.personal.left.tab.defaultBorder.right.style} ${this.personal.left.tab.defaultBorder.right.color}`);
      root.style.setProperty("--personal-tab-default-border-bottom", `${this.personal.left.tab.defaultBorder.bottom.width}px ${this.personal.left.tab.defaultBorder.bottom.style} ${this.personal.left.tab.defaultBorder.bottom.color}`);
      root.style.setProperty("--personal-tab-default-border-left", `${this.personal.left.tab.defaultBorder.left.width}px ${this.personal.left.tab.defaultBorder.left.style} ${this.personal.left.tab.defaultBorder.left.color}`);
      root.style.setProperty("--personal-tab-default-border-radius", `${this.personal.left.tab.defaultBorderRadius.topLeft}px ${this.personal.left.tab.defaultBorderRadius.topRight}px ${this.personal.left.tab.defaultBorderRadius.bottomRight}px ${this.personal.left.tab.defaultBorderRadius.bottomLeft}px`);
      root.style.setProperty("--personal-tab-default-padding", `${this.personal.left.tab.defaultPadding.top}px ${this.personal.left.tab.defaultPadding.right}px ${this.personal.left.tab.defaultPadding.bottom}px ${this.personal.left.tab.defaultPadding.left}px`);
      root.style.setProperty("--personal-tab-default-margin", `${this.personal.left.tab.defaultMargin.top}px ${this.personal.left.tab.defaultMargin.right}px ${this.personal.left.tab.defaultMargin.bottom}px ${this.personal.left.tab.defaultMargin.left}px`);

      // 个人中心左侧导航标签激活样式
      root.style.setProperty("--personal-tab-active-bg", this.personal.left.tab.activeBackground);
      root.style.setProperty("--personal-tab-active-border-top", `${this.personal.left.tab.activeBorder.top.width}px ${this.personal.left.tab.activeBorder.top.style} ${this.personal.left.tab.activeBorder.top.color}`);
      root.style.setProperty("--personal-tab-active-border-right", `${this.personal.left.tab.activeBorder.right.width}px ${this.personal.left.tab.activeBorder.right.style} ${this.personal.left.tab.activeBorder.right.color}`);
      root.style.setProperty("--personal-tab-active-border-bottom", `${this.personal.left.tab.activeBorder.bottom.width}px ${this.personal.left.tab.activeBorder.bottom.style} ${this.personal.left.tab.activeBorder.bottom.color}`);
      root.style.setProperty("--personal-tab-active-border-left", `${this.personal.left.tab.activeBorder.left.width}px ${this.personal.left.tab.activeBorder.left.style} ${this.personal.left.tab.activeBorder.left.color}`);
      root.style.setProperty("--personal-tab-active-border-radius", `${this.personal.left.tab.activeBorderRadius.topLeft}px ${this.personal.left.tab.activeBorderRadius.topRight}px ${this.personal.left.tab.activeBorderRadius.bottomRight}px ${this.personal.left.tab.activeBorderRadius.bottomLeft}px`);
      root.style.setProperty("--personal-tab-active-padding", `${this.personal.left.tab.activePadding.top}px ${this.personal.left.tab.activePadding.right}px ${this.personal.left.tab.activePadding.bottom}px ${this.personal.left.tab.activePadding.left}px`);
      root.style.setProperty("--personal-tab-active-margin", `${this.personal.left.tab.activeMargin.top}px ${this.personal.left.tab.activeMargin.right}px ${this.personal.left.tab.activeMargin.bottom}px ${this.personal.left.tab.activeMargin.left}px`);

      // 个人中心右侧内容区样式
      root.style.setProperty("--personal-right-bg", this.personal.right.background);
      root.style.setProperty("--personal-right-border-top", `${this.personal.right.border.top.width}px ${this.personal.right.border.top.style} ${this.personal.right.border.top.color}`);
      root.style.setProperty("--personal-right-border-right", `${this.personal.right.border.right.width}px ${this.personal.right.border.right.style} ${this.personal.right.border.right.color}`);
      root.style.setProperty("--personal-right-border-bottom", `${this.personal.right.border.bottom.width}px ${this.personal.right.border.bottom.style} ${this.personal.right.border.bottom.color}`);
      root.style.setProperty("--personal-right-border-left", `${this.personal.right.border.left.width}px ${this.personal.right.border.left.style} ${this.personal.right.border.left.color}`);
      root.style.setProperty("--personal-right-border-radius", `${this.personal.right.borderRadius.topLeft}px ${this.personal.right.borderRadius.topRight}px ${this.personal.right.borderRadius.bottomRight}px ${this.personal.right.borderRadius.bottomLeft}px`);
      root.style.setProperty("--personal-right-padding", `${this.personal.right.padding.top}px ${this.personal.right.padding.right}px ${this.personal.right.padding.bottom}px ${this.personal.right.padding.left}px`);
      root.style.setProperty("--personal-right-margin", `${this.personal.right.margin.top}px ${this.personal.right.margin.right}px ${this.personal.right.margin.bottom}px ${this.personal.right.margin.left}px`);

      // 个人中心右侧内容区主题色
      root.style.setProperty("--personal-right-theme-color", this.personal.right.themeColor);

      // 内容页面列表主题色
      root.style.setProperty("--content-theme-color", this.content.themeColor);

      // 首页主题色
      root.style.setProperty("--home-theme-color", this.content.homeThemeColor);

      // 登录页面背景
      if (this.login.background.type === "color") {
        root.style.setProperty("--login-bg", this.login.background.color);
        root.style.setProperty("--login-bg-image", "none");
      } else {
        root.style.setProperty("--login-bg", "transparent");
        root.style.setProperty("--login-bg-image", `url(${this.login.background.image})`);
      }

      // 登录卡片样式
      root.style.setProperty("--login-card-bg", this.login.card.background);
      root.style.setProperty("--login-card-title-color", this.login.card.titleColor);
      root.style.setProperty("--login-card-border-radius", `${this.login.card.borderRadius.topLeft}px ${this.login.card.borderRadius.topRight}px ${this.login.card.borderRadius.bottomRight}px ${this.login.card.borderRadius.bottomLeft}px`);
      root.style.setProperty("--login-card-border-top", `${this.login.card.border.top.width}px ${this.login.card.border.top.style} ${this.login.card.border.top.color}`);
      root.style.setProperty("--login-card-border-right", `${this.login.card.border.right.width}px ${this.login.card.border.right.style} ${this.login.card.border.right.color}`);
      root.style.setProperty("--login-card-border-bottom", `${this.login.card.border.bottom.width}px ${this.login.card.border.bottom.style} ${this.login.card.border.bottom.color}`);
      root.style.setProperty("--login-card-border-left", `${this.login.card.border.left.width}px ${this.login.card.border.left.style} ${this.login.card.border.left.color}`);
      root.style.setProperty("--login-card-padding", `${this.login.card.padding.top}px ${this.login.card.padding.right}px ${this.login.card.padding.bottom}px ${this.login.card.padding.left}px`);
      root.style.setProperty("--login-card-margin", `${this.login.card.margin.top}px ${this.login.card.margin.right}px ${this.login.card.margin.bottom}px ${this.login.card.margin.left}px`);

      if (this.login.card.shadow.enabled) {
        root.style.setProperty("--login-card-shadow", `${this.login.card.shadow.x}px ${this.login.card.shadow.y}px ${this.login.card.shadow.blur}px ${this.login.card.shadow.spread}px ${this.login.card.shadow.color}`);
      } else {
        root.style.setProperty("--login-card-shadow", "none");
      }

      // 登录按钮样式
      root.style.setProperty("--login-button-bg", this.login.button.background);
      root.style.setProperty("--login-button-text", this.login.button.textColor);
      root.style.setProperty("--login-button-border-radius", `${this.login.button.borderRadius.topLeft}px ${this.login.button.borderRadius.topRight}px ${this.login.button.borderRadius.bottomRight}px ${this.login.button.borderRadius.bottomLeft}px`);
      root.style.setProperty("--login-button-border-top", `${this.login.button.border.top.width}px ${this.login.button.border.top.style} ${this.login.button.border.top.color}`);
      root.style.setProperty("--login-button-border-right", `${this.login.button.border.right.width}px ${this.login.button.border.right.style} ${this.login.button.border.right.color}`);
      root.style.setProperty("--login-button-border-bottom", `${this.login.button.border.bottom.width}px ${this.login.button.border.bottom.style} ${this.login.button.border.bottom.color}`);
      root.style.setProperty("--login-button-border-left", `${this.login.button.border.left.width}px ${this.login.button.border.left.style} ${this.login.button.border.left.color}`);

      if (this.login.button.shadow.enabled) {
        root.style.setProperty("--login-button-shadow", `${this.login.button.shadow.x}px ${this.login.button.shadow.y}px ${this.login.button.shadow.blur}px ${this.login.button.shadow.spread}px ${this.login.button.shadow.color}`);
      } else {
        root.style.setProperty("--login-button-shadow", "none");
      }

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

