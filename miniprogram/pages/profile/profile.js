// pages/profile/profile.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: {
      avatar: '',
      nickName: '用户',
      certified: false,
      school: '某某大学'
    },
    myGoods: []
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    this.checkLoginStatus();
    if (app.globalData.isLogin) {
      this.loadMyGoods();
    }
  },

  checkLoginStatus() {
    this.setData({
      isLogin: app.globalData.isLogin,
      userInfo: app.globalData.userInfo || this.data.userInfo
    });
  },

  login() {
    app.login((success) => {
      if (success) {
        this.setData({
          isLogin: true,
          userInfo: app.globalData.userInfo
        });
        this.loadMyGoods();
      }
    });
  },

  loadMyGoods() {
    api.goods.getMyGoods().then(res => {
      res.forEach(item => {
        item.statusText = this.getStatusText(item.status);
      });
      
      this.setData({
        myGoods: res.slice(0, 3)
      });
    }).catch(() => {
      this.setData({
        myGoods: []
      });
    });
  },

  getStatusText(status) {
    const map = {
      0: '在售',
      1: '已售',
      2: '已下架'
    };
    return map[status] || '未知';
  },

  editProfile() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    });
  },

  goToPublish() {
    wx.switchTab({
      url: '/pages/publish/publish'
    });
  },

  goToOrders() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  goToCollect() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  certifyStudent() {
    if (this.data.userInfo.certified) {
      wx.showToast({
        title: '已完成认证',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '学生认证',
      content: '请输入学号完成认证',
      editable: true,
      placeholderText: '学号',
      success: (res) => {
        if (res.confirm) {
          const studentId = res.content;
          if (util.validateStudentId(studentId)) {
            api.user.certify({ studentId }).then(() => {
              wx.showToast({
                title: '认证成功',
                icon: 'success'
              });
              
              this.setData({
                'userInfo.certified': true
              });
            }).catch(() => {
              wx.showToast({
                title: '认证失败',
                icon: 'none'
              });
            });
          } else {
            wx.showToast({
              title: '学号格式错误',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  goToAbout() {
    wx.showModal({
      title: '关于我们',
      content: '校园二手书交易平台v1.0\n致力于为大学生提供便捷的二手书交易服务',
      showCancel: false
    });
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
          this.setData({
            isLogin: false,
            myGoods: []
          });
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  }
});
