// pages/profile/profile.js
const { request } = require('../../utils/request.js');
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: {
      avatarUrl: '',
      nickName: '用户',
      certified: false,
      school: '某某大学'
    },
    myGoods: [],
    // 订单统计
    orderStats: {
      buyCount: 0,
      sellCount: 0,
      pendingCount: 0,
      finishedCount: 0
    }
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    // 每次页面显示都检查登录状态
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    
    if (token) {
      this.setData({ isLogin: true });
      // 获取用户信息
      this.fetchUserInfo();
      // 加载我的商品
      this.loadMyGoods();
      // 加载订单统计
      this.loadOrderStats();
    } else {
      this.setData({ 
        isLogin: false,
        userInfo: {
          avatarUrl: '',
          nickName: '用户',
          certified: false,
          school: '某某大学'
        },
        myGoods: [],
        orderStats: {
          buyCount: 0,
          sellCount: 0,
          pendingCount: 0,
          finishedCount: 0
        }
      });
    }
  },

  // 从后端获取用户信息
  fetchUserInfo() {
    request('/user/info', 'GET').then(res => {
      if (res.code === 200 && res.data) {
        this.setData({ 
          userInfo: res.data 
        });
        // 同步到全局
        app.globalData.userInfo = res.data;
      }
    }).catch(err => {
      console.error('获取用户信息失败:', err);
    });
  },

  // 跳转到登录页
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/index'
    });
  },

  // 加载我的商品
  loadMyGoods() {
    request('/goods/my', 'GET').then(res => {
      if (res.code === 200) {
        let list = res.data || [];
        // 兼容直接返回数组的情况
        if (Array.isArray(res)) {
          list = res;
        }
        
        this.setData({
          myGoods: list.slice(0, 3)  // 只显示前3条
        });
      }
    }).catch(err => {
      console.error('获取我的商品失败:', err);
      this.setData({ myGoods: [] });
    });
  },

  // 加载订单统计
  loadOrderStats() {
    api.order.getStats().then(res => {
      if (res && res.code === 200 && res.data) {
        this.setData({ orderStats: res.data });
      }
    }).catch(err => {
      console.error('获取订单统计失败:', err);
    });
  },

  // 获取状态文本
  getStatusText(status) {
    const map = {
      0: '在售',
      1: '已售',
      2: '已下架'
    };
    return map[status] || '未知';
  },

  // 编辑资料
  editProfile() {
    wx.navigateTo({
      url: '/pages/login/index'  // 复用登录页的头像昵称编辑
    });
  },

  // 跳转到商品详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    });
  },

  // 跳转到发布页
  goToPublish() {
    wx.switchTab({
      url: '/pages/publish/publish'
    });
  },

  // 查看全部我的发布
  goToMyGoods() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 查看订单 - 全部订单
  goToOrders() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/order/order'
    });
  },

  // 查看我买的订单
  goToBuyOrders() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/order/order?type=buy'
    });
  },

  // 查看我卖的订单
  goToSellOrders() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/order/order?type=sell'
    });
  },

  // 查看待处理订单
  goToPendingOrders() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/order/order?status=0'
    });
  },

  // 查看收藏
  goToCollect() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  // 学生认证
  certifyStudent() {
    if (!this.data.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
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
      placeholderText: '请输入8-12位学号',
      success: (res) => {
        if (res.confirm && res.content) {
          const studentId = res.content.trim();
          
          if (!util.validateStudentId(studentId)) {
            wx.showToast({
              title: '学号格式错误',
              icon: 'none'
            });
            return;
          }
          
          request('/user/certify', 'POST', { studentId })
            .then(result => {
              if (result.code === 200) {
                wx.showToast({
                  title: '认证成功',
                  icon: 'success'
                });
                this.setData({
                  'userInfo.certified': true
                });
              } else {
                wx.showToast({
                  title: result.message || '认证失败',
                  icon: 'none'
                });
              }
            })
            .catch(() => {
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              });
            });
        }
      }
    });
  },

  // 关于我们
  goToAbout() {
    wx.showModal({
      title: '关于我们',
      content: '校园二手书交易平台v1.0\n致力于为大学生提供便捷的二手书交易服务',
      showCancel: false
    });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          
          // 清除全局数据
          app.globalData.token = null;
          app.globalData.userInfo = null;
          app.globalData.isLogin = false;
          
          // 更新页面状态
          this.setData({
            isLogin: false,
            userInfo: {
              avatarUrl: '',
              nickName: '用户',
              certified: false,
              school: '某某大学'
            },
            myGoods: [],
            orderStats: {
              buyCount: 0,
              sellCount: 0,
              pendingCount: 0,
              finishedCount: 0
            }
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
