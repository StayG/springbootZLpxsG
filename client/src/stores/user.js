import { defineStore } from "pinia";

export const useUserStore = defineStore("frontUser", {
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
        key: "frontUser",
        storage: localStorage,
      },
    ],
  },
});
