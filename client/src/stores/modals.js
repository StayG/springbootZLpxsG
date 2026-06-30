import { defineStore } from "pinia";

export const useModalsStore = defineStore("modals", {
  state: () => ({
    changePasswordVisible: false,
    changePasswordKey: 0,
  }),
  actions: {
    openChangePassword() {
      this.changePasswordVisible = true;
      this.changePasswordKey += 1; // re-mount form each time
    },
    closeChangePassword() {
      this.changePasswordVisible = false;
    },
  },
});

