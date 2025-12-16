// pages/message/message.js
// 消息列表页 - 显示所有会话，点击进入聊天详情
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const socket = require('../../utils/socket.js');
const app = getApp();

Page({
  data: {
    messageList: [],   // 消息列表
    loading: false     // 加载状态
  },

  onLoad() {
    this.loadMessageList();
    
    // 建立WebSocket连接
    if (app.globalData.isLogin) {
      socket.connect();
    }
  },

  onShow() {
    // 每次显示页面时刷新列表，确保看到最新消息和未读数
    if (app.globalData.isLogin) {
      this.loadMessageList();
      
      // 确保WebSocket已连接
      const status = socket.getStatus();
      if (!status.isConnected) {
        socket.connect();
      }
    } else {
      // 未登录时清空列表
      this.setData({ messageList: [] });
    }
  },

  onHide() {
    // 页面隐藏时不断开连接，保持后台接收消息
  },

  onUnload() {
    // 页面卸载时关闭WebSocket
    // socket.close();
  },

  /**
   * 加载消息列表
   */
  loadMessageList() {
    if (!app.globalData.isLogin) {
      this.setData({
        messageList: []
      });
      return;
    }

    this.setData({ loading: true });

    api.message.getList().then(res => {
      // 处理时间格式，例如 "刚刚", "10分钟前"
      let list = [];
      
      // 兼容多种返回格式
      if (res && res.code === 200 && res.data) {
        list = res.data;
      } else if (Array.isArray(res)) {
        list = res;
      } else if (res && Array.isArray(res.list)) {
        list = res.list;
      }
      
      // 格式化时间
      if (list && list.length > 0) {
        list.forEach(item => {
          if (item.lastMessageTime) {
            item.time = util.timeAgo(item.lastMessageTime);
          }
        });
      }
      
      this.setData({
        messageList: list || [],
        loading: false
      });
    }).catch(err => {
      console.error('获取消息列表失败:', err);
      this.setData({
        messageList: [],
        loading: false
      });
    });
  },

  /**
   * 打开聊天页面
   * 从WXML获取 data-user-id 和 data-user-name
   */
  openChat(e) {
    // 1. 获取参数
    const userId = e.currentTarget.dataset.userId;
    const userName = e.currentTarget.dataset.userName;
    const avatarUrl = e.currentTarget.dataset.avatar;

    // 2. 参数校验
    if (!userId) {
      wx.showToast({
        title: '用户ID无效',
        icon: 'none'
      });
      return;
    }

    // 3. 跳转到聊天页面
    let url = `/pages/chat/chat?userId=${userId}`;
    
    if (userName) {
      url += `&userName=${encodeURIComponent(userName)}`;
    }
    if (avatarUrl) {
      url += `&avatarUrl=${encodeURIComponent(avatarUrl)}`;
    }

    wx.navigateTo({
      url: url,
      fail: (err) => {
        console.error('跳转失败，请检查 app.json 中是否注册了 pages/chat/chat', err);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadMessageList();
    wx.stopPullDownRefresh();
  }
});
