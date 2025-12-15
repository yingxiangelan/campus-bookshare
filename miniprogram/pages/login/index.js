const app = getApp();
const { request, uploadFile } = require('../../utils/request.js');

Page({
  data: {
    isAgreed: false,
    showAuth: false,
    userInfo: {
      avatarUrl: '', // 微信选择的临时路径
      serverAvatarUrl: '', // 上传后的路径 (目前暂时不用)
      nickName: ''
    }
  },

  // 1. 勾选协议
  handleAgreement(e) {
    this.setData({ isAgreed: e.detail.value.length > 0 });
  },

  // 2. 微信登录 (获取 Token)
  handleLogin() {
    if (!this.data.isAgreed) {
      wx.showToast({ title: '请先同意协议', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '登录中...' });
    
    wx.login({
      success: (res) => {
        if (res.code) {
          this.loginToServer(res.code);
        }
      }
    });
  },

  // 3. 后端换取 Token
  loginToServer(code) {
    request('/user/login/wechat', 'POST', { code: code }).then(res => {
      wx.hideLoading();
      if (res && res.data) { // 假设后端直接返回 token 字符串，或者 res.data 是 token
        const token = typeof res.data === 'string' ? res.data : res;
        wx.setStorageSync('token', token);
        this.setData({ showAuth: true });
      } else {
        // 容错：如果后端返回的是 standard Result对象
        const token = res; 
        wx.setStorageSync('token', token);
        this.setData({ showAuth: true });
      }
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: '登录失败', icon: 'none' });
    });
  },

  // 4. 【核心】当用户点击头像按钮选择头像时
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    console.log('用户选择了头像:', avatarUrl); // 调试日志
    
    this.setData({
      'userInfo.avatarUrl': avatarUrl 
      // 这里的 avatarUrl 应该是 wxfile://tmp_... 开头的
    });
  },

  // 5. 输入昵称
  onNicknameChange(e) {
    this.setData({ 'userInfo.nickName': e.detail.value });
  },

  // 6. 【核心】提交数据 (覆盖旧的 dummyimage)
  submitUserInfo() {
    const { nickName, avatarUrl } = this.data.userInfo;
    
    if (!avatarUrl || !nickName) {
      wx.showToast({ title: '请选择头像并填昵称', icon: 'none' });
      return;
    }

    // 打印我们要发给后端的数据，确保不是 dummyimage
    console.log('准备提交更新:', { avatarUrl, nickName });

    request('/user/updateUserInfo', 'POST', {
      avatarUrl: avatarUrl, // 直接把 wxfile:// 发给后端存入数据库
      nickName: nickName
    }).then(res => {
      if (res.code === 200 || res.success) {
        wx.showToast({ title: '更新成功' });
        
        // 强制更新全局变量，防止 switchTab 后页面不刷新
        const app = getApp();
        app.globalData.userInfo = { nickName, avatarUrl };

        setTimeout(() => {
          // 跳转回我的页面
          wx.switchTab({
            url: '/pages/profile/profile',
            success: function(e) {
              var page = getCurrentPages().pop();
              if (page == undefined || page == null) return;
              page.onShow(); // 强制刷新我的页面
            }
          });
        }, 1000);
      }
    });
  },

  handleCancel() {
    wx.navigateBack();
  }
});