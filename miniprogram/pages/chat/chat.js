const app = getApp();
const socket = require('../../utils/socket.js'); // 确保路径正确

Page({
  data: {
    myUserId: null,
    targetUserId: null,
    targetAvatar: '', // 对方头像
    myAvatar: '',     // 我的头像
    msgList: [],
    inputValue: '',
    toView: '',       // 用于自动滚动到底部
    scrollHeight: 0
  },

  onLoad(options) {
    // 1. 获取屏幕高度用于计算滚动区域
    const sysInfo = wx.getSystemInfoSync();
    this.setData({
      scrollHeight: sysInfo.windowHeight - 50 // 减去底部输入框高度
    });

    // 2. 获取参数和用户信息
    const userInfo = wx.getStorageSync('userInfo');
    const targetUserId = options.userId; // 从上个页面传来的参数
    const targetName = options.userName; // 对方名字

    wx.setNavigationBarTitle({ title: targetName || '聊天' });

    this.setData({
      myUserId: userInfo.id,
      myAvatar: userInfo.avatarUrl, // 假设userInfo里有头像
      targetUserId: parseInt(targetUserId),
      // targetAvatar: options.avatarUrl // 如果上个页面传了头像
    });

    // 3. 加载历史消息 (这里暂时用假数据模拟，你需要对接后端API)
    // this.getHistoryMessages();

    // 4. 监听Socket消息
    socket.onMessage((data) => {
      console.log('聊天页收到消息', data);
      if (data.fromUserId == this.data.targetUserId) {
        this.pushMessage(data.content, false);
      }
    });
  },

  // 发送消息
  sendMsg() {
    if (!this.data.inputValue.trim()) return;

    const content = this.data.inputValue;
    
    // 1. WebSocket 发送
    const msgObj = {
      toUserId: this.data.targetUserId,
      content: content,
      type: 1
    };
    socket.send(msgObj);

    // 2. 界面显示
    this.pushMessage(content, true);
    
    // 3. 清空输入框
    this.setData({ inputValue: '' });
  },

  // 将消息推入列表并滚动到底部
  pushMessage(content, isMy) {
    const list = this.data.msgList;
    list.push({
      id: Date.now(),
      content: content,
      isMy: isMy
    });
    
    this.setData({
      msgList: list,
      toView: 'msg-' + (list.length - 1) // 滚动id
    });
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value });
  }
});