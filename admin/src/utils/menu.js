const menu = {
  list() {
    return [
      // 管理员角色配置
      {
        backMenu: [
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "公告信息",
                tableName: "notices",
              },
            ],
            menu: "公告信息",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "用户管理",
                menuJump: "列表",
                tableName: "users",
              },
            ],
            menu: "用户管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "教师管理",
                menuJump: "列表",
                tableName: "teachers",
              },
            ],
            menu: "教师管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "科目管理",
                menuJump: "列表",
                tableName: "subject",
              },
            ],
            menu: "科目管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "试题管理",
                menuJump: "列表",
                tableName: "examQuestion",
              },
            ],
            menu: "试题管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "试卷管理",
                menuJump: "列表",
                tableName: "examPaper",
              },
            ],
            menu: "试卷管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除", "发布", "取消发布"],
                menu: "考试管理",
                menuJump: "列表",
                tableName: "examInfo",
              },
            ],
            menu: "考试管理",
          },
          {
            child: [
              {
                buttons: ["查看", "删除"],
                menu: "考试记录",
                menuJump: "列表",
                tableName: "examRecord",
              },
            ],
            menu: "考试记录",
          },
          {
            child: [
              {
                buttons: ["查看", "批阅"],
                menu: "阅卷管理",
                menuJump: "列表",
                tableName: "examGrading",
              },
            ],
            menu: "阅卷管理",
          },
          {
            child: [
              {
                buttons: ["查看", "删除"],
                menu: "错题本",
                menuJump: "列表",
                tableName: "examWrongQuestion",
              },
            ],
            menu: "错题本",
          },
          {
            child: [
              {
                buttons: ["查看", "导出"],
                menu: "成绩与统计",
                menuJump: "列表",
                tableName: "gradesStatistics",
              },
            ],
            menu: "成绩与统计",
          },
          {
            child: [
              {
                buttons: ["查看", "导出"],
                menu: "操作日志",
                menuJump: "列表",
                tableName: "operationLog",
              },
            ],
            menu: "系统设置",
          },
        ],

        hasBackLogin: "是",
        hasBackRegister: "否",
        hasFrontLogin: "否",
        hasFrontRegister: "否",
        roleName: "管理员",
        tableName: "managers",
      },
      // 教师角色配置（管理端）
      {
        backMenu: [
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "公告信息",
                tableName: "notices",
              },
            ],
            menu: "公告信息",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "试题管理",
                menuJump: "列表",
                tableName: "examQuestion",
              },
            ],
            menu: "试题管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除"],
                menu: "试卷管理",
                menuJump: "列表",
                tableName: "examPaper",
              },
            ],
            menu: "试卷管理",
          },
          {
            child: [
              {
                buttons: ["新增", "查看", "编辑", "删除", "发布", "取消发布"],
                menu: "考试管理",
                menuJump: "列表",
                tableName: "examInfo",
              },
            ],
            menu: "考试管理",
          },
          {
            child: [
              {
                buttons: ["查看", "删除"],
                menu: "考试记录",
                menuJump: "列表",
                tableName: "examRecord",
              },
            ],
            menu: "考试记录",
          },
          {
            child: [
              {
                buttons: ["查看", "批阅"],
                menu: "阅卷管理",
                menuJump: "列表",
                tableName: "examGrading",
              },
            ],
            menu: "阅卷管理",
          },
          {
            child: [
              {
                buttons: ["查看", "删除"],
                menu: "错题本",
                menuJump: "列表",
                tableName: "examWrongQuestion",
              },
            ],
            menu: "错题本",
          },
          {
            child: [
              {
                buttons: ["查看", "导出"],
                menu: "成绩与统计",
                menuJump: "列表",
                tableName: "gradesStatistics",
              },
            ],
            menu: "成绩与统计",
          },
          {
            child: [
              {
                buttons: ["查看", "导出"],
                menu: "操作日志",
                menuJump: "列表",
                tableName: "operationLog",
              },
            ],
            menu: "系统设置",
          },
        ],

        hasBackLogin: "是",
        hasBackRegister: "否",
        hasFrontLogin: "否",
        hasFrontRegister: "否",
        roleName: "教师",
        tableName: "teachers",
      },
    ];
  },
};
export default menu;
