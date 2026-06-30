const menu = {
  list() {
    return [
      // 学生角色配置
      {
        backMenu: [
          {
            child: [
              {
                buttons: ["查看"],
                menu: "公告信息",
                tableName: "notices",
              },
            ],
            menu: "公告信息",
          },
        ],
        hasBackLogin: "是",
        hasBackRegister: "否",
        hasFrontLogin: "是",
        hasFrontRegister: "是",
        roleName: "学生",
        tableName: "users",
      },
    ];
  },
};
export default menu;
