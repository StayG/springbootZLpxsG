import { defineStore } from "pinia";

export const useUserStore = defineStore("adminUser", {
  state: () => {
    return {
      token: "",
      userInfo: {},
    };
  },
  actions: {
    setToken(token) {
      this.token = token;
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
    },
    reset() {
      this.token = "";
      this.userInfo = {};
    },
    getToken() {
    

      return this.token;
    },
    getUserInfo() {
      return this.userInfo;
    },
  },
  persist: {
    enabled: true,
    strategies: [
      {
        key: "adminUser",
        storage: localStorage,
      },
    ],
  },
});
