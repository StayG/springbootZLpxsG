import menu from "@/utils/menu.js";
import storage from "@/utils/storage.js";

const utils = {
    /**
     * 权限校验
     * @param tableName
     * @param key
     * @returns {boolean}
     */
    isAuth(tableName,key) {
        let role = storage.get("admin-roleName");
        if(!role){
            role = '管理员';
        }
        let menus = menu.list();
        for(let i=0;i<menus.length;i++){
            if(menus[i].roleName===role){
                for(let j=0;j<menus[i].backMenu.length;j++){
                    for(let k=0;k<menus[i].backMenu[j].child.length;k++){
                        if(tableName===menus[i].backMenu[j].child[k].tableName){
                            let buttons = menus[i].backMenu[j].child[k].buttons.join(',');
                            return buttons.indexOf(key) !== -1 || false
                        }
                    }
                }
            }
        }
        return false;
    }
}
export default utils;