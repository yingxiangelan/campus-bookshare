// pages/login/index.js
const app = getApp();
const { request } = require('../../utils/request.js');

Page({
  data: {
    isAgreed: false,
    showAuth: false,
    userInfo: {
      avatarUrl: '',
      nickName: ''
    }
  },

  onLoad() {
    // 检查是否已登录
    const token = wx.getStorageSync('token');
    if (token) {
      // 已有Token，直接显示授权界面
      this.setData({ showAuth: true });
    }
  },

  // 1. 勾选协议
  handleAgreement(e) {
    this.setData({ 
      isAgreed: e.detail.value.length > 0 
    });
  },

  // 2. 微信登录（获取Token）
  handleLogin() {
    if (!this.data.isAgreed) {
      wx.showToast({ 
        title: '请先同意协议', 
        icon: 'none' 
      });
      return;
    }
    
    wx.showLoading({ title: '登录中...' });
    
    wx.login({
      success: (res) => {
        if (res.code) {
          this.loginToServer(res.code);
        } else {
          wx.hideLoading();
          wx.showToast({ 
            title: '获取code失败', 
            icon: 'none' 
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ 
          title: '微信登录失败', 
          icon: 'none' 
        });
      }
    });
  },

  // 3. 后端换取Token
  loginToServer(code) {
    request('/user/login/wechat', 'POST', { code: code })
      .then(res => {
        wx.hideLoading();
        
        // 处理返回结果
        let token = null;
        if (res.code === 200 && res.data) {
          // 标准格式: { code: 200, data: "token_string" }
          token = res.data;
        } else if (typeof res === 'string') {
          // 直接返回token字符串
          token = res;
        }
        
        if (token) {
          // 保存Token
          wx.setStorageSync('token', token);
          app.globalData.token = token;
          app.globalData.isLogin = true;
          
          // 显示授权界面
          this.setData({ showAuth: true });
          
          wx.showToast({ 
            title: '登录成功', 
            icon: 'success' 
          });
        } else {
          wx.showToast({ 
            title: res.message || '登录失败', 
            icon: 'none' 
          });
        }
      })
      .catch(err => {
        wx.hideLoading();
        console.error('登录失败:', err);
        wx.showToast({ 
          title: '网络错误', 
          icon: 'none' 
        });
      });
  },

  // 4. 选择头像
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    console.log('用户选择了头像:', avatarUrl);
    
    this.setData({
      'userInfo.avatarUrl': avatarUrl
    });
  },

  // 5. 输入昵称
  onNicknameChange(e) {
    this.setData({ 
      'userInfo.nickName': e.detail.value 
    });
  },

  // 6. 提交用户信息
  submitUserInfo() {
    const { nickName, avatarUrl } = this.data.userInfo;
    
    if (!avatarUrl) {
      wx.showToast({ 
        title: '请选择头像', 
        icon: 'none' 
      });
      return;
    }
    
    if (!nickName) {
      wx.showToast({ 
        title: '请输入昵称', 
        icon: 'none' 
      });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    request('/user/updateUserInfo', 'POST', {
      avatarUrl: avatarUrl,
      nickName: nickName
    }).then(res => {
      wx.hideLoading();
      
      if (res.code === 200) {
        // 更新全局用户信息
        app.globalData.userInfo = { 
          nickName, 
          avatarUrl 
        };
        
        wx.showToast({ 
          title: '保存成功', 
          icon: 'success' 
        });
        
        // 延迟跳转
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/profile/profile'
          });
        }, 1000);
      } else {
        wx.showToast({ 
          title: res.message || '保存失败', 
          icon: 'none' 
        });
      }
    }).catch(err => {
      wx.hideLoading();
      console.error('更新失败:', err);
      wx.showToast({ 
        title: '网络错误', 
        icon: 'none' 
      });
    });
  },

  // 7. 跳过授权
  handleCancel() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
