// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080/api', // 后端API地址，实际使用时需修改
    isLogin: false
  },

  onLaunch() {
    // 检查登录状态
    this.checkLogin();
    
    // 获取系统信息
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res;
      }
    });
  },

  // 检查登录状态
  checkLogin() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.isLogin = true;
    }
  },

  // 用户登录
  login(callback) {
    wx.login({
      success: (res) => {
        if (res.code) {
          // 调用后端接口
          wx.request({
            url: `${this.globalData.baseUrl}/user/login`,
            method: 'POST',
            data: {
              code: res.code
            },
            success: (result) => {
              if (result.data.code === 200) {
                const { token, userInfo } = result.data.data;
                
                // 保存登录信息
                wx.setStorageSync('token', token);
                wx.setStorageSync('userInfo', userInfo);
                
                this.globalData.token = token;
                this.globalData.userInfo = userInfo;
                this.globalData.isLogin = true;
                
                if (callback) callback(true);
              } else {
                wx.showToast({
                  title: '登录失败',
                  icon: 'none'
                });
                if (callback) callback(false);
              }
            },
            fail: () => {
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              });
              if (callback) callback(false);
            }
          });
        }
      }
    });
  },

  // 退出登录
  logout() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLogin = false;
  },

  // 获取用户信息
  getUserInfo(callback) {
    if (this.globalData.userInfo) {
      callback(this.globalData.userInfo);
    } else {
      // 需要重新登录
      this.login((success) => {
        if (success) {
          callback(this.globalData.userInfo);
        } else {
          callback(null);
        }
      });
    }
  }
});
