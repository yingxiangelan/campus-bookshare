// pages/profile/profile.js
// 引入我们封装的请求工具（用来请求后端）
const { request } = require('../../utils/request.js');
// 保留原本的 api，防止"我的发布"报错，但要注意 api.js 可能也需要改造（暂时不动它）
const api = require('../../utils/api.js'); 
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: {
      avatarUrl: '', // 对应后端字段
      nickName: '用户',
      certified: false,
      school: ''
    },
    myGoods: []
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    // 每次页面显示，都重新检查一下登录状态
    this.checkLoginStatus();
  },

  // 【核心修改】检查登录状态：改为检查 Storage 里有没有 Token
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    
    if (token) {
      // 1. 如果有 Token，认为已登录
      this.setData({ isLogin: true });
      
      // 2. 去后端获取最新的用户信息
      this.fetchUserInfo();
      
      // 3. 加载我的商品 (如果 api.js 还没改好，这步可能会报错，暂时保留)
      // this.loadMyGoods(); 
    } else {
      // 没有 Token，就是未登录
      this.setData({ 
        isLogin: false,
        userInfo: {},
        myGoods: []
      });
    }
  },

  // 【新增】从后端获取真实用户信息
  fetchUserInfo() {
    request('/user/info').then(res => {
      // 假设后端 Result.success(data) 返回的数据在 res.data 里
      if(res.code === 200 || res.success) {
         this.setData({ userInfo: res.data });
      }
    });
  },

  // 【核心修改】点击登录按钮 -> 跳转到我们写的登录页
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/index' // 确保你的登录页是在这个路径
    });
  },

  // 【核心修改】退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 1. 清除本地 Token
          wx.removeStorageSync('token');
          // 2. 清除全局数据
          app.globalData.userInfo = null;
          
          // 3. 更新页面状态
          this.setData({
            isLogin: false,
            userInfo: {},
            myGoods: []
          });
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  },

  // ---------------------------------------------------------
  // 下面的代码大部分保留了你原来的逻辑，做了一些微调
  // ---------------------------------------------------------

  loadMyGoods() {
    // 暂时注释掉，防止因为 api.js 没配置好导致报错
    // 等登录跑通了，再去研究 api.js 怎么加 header
    /*
    api.goods.getMyGoods().then(res => {
      res.forEach(item => {
        item.statusText = this.getStatusText(item.status);
      });
      this.setData({ myGoods: res.slice(0, 3) });
    }).catch(() => {
      this.setData({ myGoods: [] });
    });
    */
  },

  getStatusText(status) {
    const map = { 0: '在售', 1: '已售', 2: '已下架' };
    return map[status] || '未知';
  },

  editProfile() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  },

  goToPublish() {
    wx.switchTab({ url: '/pages/publish/publish' });
  },

  goToOrders() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
  },

  goToCollect() {
    wx.showToast({ title: '功能开发中', icon: 'none' });
  },

  certifyStudent() {
    if (this.data.userInfo.certified) {
      wx.showToast({ title: '已完成认证', icon: 'none' });
      return;
    }
    // 这里暂时不改，保持原样
    wx.showModal({
      title: '学生认证',
      content: '请输入学号完成认证',
      editable: true,
      placeholderText: '学号',
      success: (res) => {
        if (res.confirm) {
           wx.showToast({ title: '演示版暂不支持', icon: 'none' });
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
  }
});