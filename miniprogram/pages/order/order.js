// pages/order/order.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    tabs: [
      { key: 'all', name: '全部' },
      { key: 'buy', name: '我买的' },
      { key: 'sell', name: '我卖的' }
    ],
    currentTab: 'all',
    
    statusFilters: [
      { key: null, name: '全部状态' },
      { key: 0, name: '待确认' },
      { key: 1, name: '已确认' },
      { key: 2, name: '已完成' },
      { key: 3, name: '已取消' }
    ],
    currentStatus: null,
    
    orderList: [],
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 10,
    
    // 订单统计
    stats: {
      buyCount: 0,
      sellCount: 0,
      pendingCount: 0,
      finishedCount: 0
    }
  },

  onLoad(options) {
    // 检查登录状态
    if (!app.globalData.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        showCancel: false,
        success: () => {
          wx.switchTab({ url: '/pages/profile/profile' });
        }
      });
      return;
    }
    
    // 根据参数设置初始tab
    if (options.type) {
      this.setData({ currentTab: options.type });
    }
    if (options.status !== undefined) {
      this.setData({ currentStatus: parseInt(options.status) });
    }
    
    this.loadOrderList();
    this.loadStats();
  },

  onShow() {
    // 页面显示时刷新
    if (app.globalData.isLogin && this.data.orderList.length > 0) {
      this.refreshList();
    }
  },

  onPullDownRefresh() {
    this.refreshList();
  },

  onReachBottom() {
    this.loadMore();
  },

  // 切换Tab
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.currentTab) return;
    
    this.setData({
      currentTab: tab,
      orderList: [],
      page: 1,
      hasMore: true
    });
    this.loadOrderList();
  },

  // 切换状态筛选
  switchStatus(e) {
    const status = e.currentTarget.dataset.status;
    if (status === this.data.currentStatus) return;
    
    this.setData({
      currentStatus: status,
      orderList: [],
      page: 1,
      hasMore: true
    });
    this.loadOrderList();
  },

  // 加载订单列表
  loadOrderList() {
    if (this.data.loading || !this.data.hasMore) return;
    
    this.setData({ loading: true });
    
    const params = {
      type: this.data.currentTab,
      page: this.data.page,
      pageSize: this.data.pageSize
    };
    
    if (this.data.currentStatus !== null) {
      params.status = this.data.currentStatus;
    }
    
    api.order.getList(params).then(res => {
      let list = [];
      let hasMore = false;
      
      if (res && res.code === 200 && res.data) {
        list = res.data.list || [];
        hasMore = res.data.hasMore === true;
      } else if (res && res.list) {
        list = res.list;
        hasMore = res.hasMore === true;
      }
      
      // 格式化时间
      list.forEach(item => {
        item.createTimeText = util.timeAgo(new Date(item.createTime).getTime());
      });
      
      const newList = this.data.page === 1 ? list : [...this.data.orderList, ...list];
      
      this.setData({
        orderList: newList,
        loading: false,
        hasMore: hasMore,
        page: this.data.page + 1
      });
      
      wx.stopPullDownRefresh();
    }).catch(err => {
      console.error('加载订单失败:', err);
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    });
  },

  // 刷新列表
  refreshList() {
    this.setData({
      orderList: [],
      page: 1,
      hasMore: true
    });
    this.loadOrderList();
    this.loadStats();
  },

  // 加载更多
  loadMore() {
    this.loadOrderList();
  },

  // 加载统计数据
  loadStats() {
    api.order.getStats().then(res => {
      if (res && res.code === 200 && res.data) {
        this.setData({ stats: res.data });
      }
    }).catch(err => {
      console.error('加载统计失败:', err);
    });
  },

  // 跳转到订单详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order/detail?id=${id}`
    });
  },

  // 确认订单（卖家）
  confirmOrder(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认订单',
      content: '确认接受这笔订单吗？',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.confirm(id).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '已确认', icon: 'success' });
              this.refreshList();
            } else {
              wx.showToast({ title: res.message || '操作失败', icon: 'none' });
            }
          }).catch(() => {
            util.hideLoading();
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  // 完成订单
  finishOrder(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '完成交易',
      content: '确认已完成线下交易吗？',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.finish(id).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '交易完成', icon: 'success' });
              this.refreshList();
            } else {
              wx.showToast({ title: res.message || '操作失败', icon: 'none' });
            }
          }).catch(() => {
            util.hideLoading();
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  // 取消订单
  cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '取消订单',
      content: '确定要取消这笔订单吗？',
      editable: true,
      placeholderText: '请输入取消原因（可选）',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.cancel(id, res.content).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '已取消', icon: 'success' });
              this.refreshList();
            } else {
              wx.showToast({ title: res.message || '操作失败', icon: 'none' });
            }
          }).catch(() => {
            util.hideLoading();
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  // 联系对方
  contactUser(e) {
    const userId = e.currentTarget.dataset.userId;
    const goodsId = e.currentTarget.dataset.goodsId;
    
    wx.navigateTo({
      url: `/pages/chat/chat?userId=${userId}&goodsId=${goodsId}`
    });
  },

  // 查看商品
  goToGoods(e) {
    const goodsId = e.currentTarget.dataset.goodsId;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${goodsId}`
    });
  }
});
