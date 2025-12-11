// pages/message/message.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    messageList: []
  },

  onLoad() {
    this.loadMessageList();
  },

  onShow() {
    if (app.globalData.isLogin) {
      this.loadMessageList();
    }
  },

  loadMessageList() {
    if (!app.globalData.isLogin) {
      this.setData({
        messageList: []
      });
      return;
    }

    api.message.getList().then(res => {
      res.forEach(item => {
        item.time = util.timeAgo(item.lastMessageTime);
      });
      
      this.setData({
        messageList: res
      });
    }).catch(() => {
      this.setData({
        messageList: []
      });
    });
  },

  openChat(e) {
    const userId = e.currentTarget.dataset.userId;
    wx.navigateTo({
      url: `/pages/message/chat?userId=${userId}`
    });
  }
});
