// pages/order/detail.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    orderId: '',
    order: null,
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ orderId: options.id });
      this.loadOrderDetail();
    } else {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  onShow() {
    if (this.data.orderId && this.data.order) {
      this.loadOrderDetail();
    }
  },

  // 加载订单详情
  loadOrderDetail() {
    this.setData({ loading: true });
    
    api.order.getDetail(this.data.orderId).then(res => {
      if (res && res.code === 200 && res.data) {
        const order = res.data;
        
        // 格式化时间
        if (order.createTime) {
          order.createTimeText = util.formatTime(new Date(order.createTime));
        }
        if (order.confirmTime) {
          order.confirmTimeText = util.formatTime(new Date(order.confirmTime));
        }
        if (order.finishTime) {
          order.finishTimeText = util.formatTime(new Date(order.finishTime));
        }
        if (order.cancelTime) {
          order.cancelTimeText = util.formatTime(new Date(order.cancelTime));
        }
        
        this.setData({ order, loading: false });
      } else {
        this.setData({ loading: false });
        wx.showToast({ title: '订单不存在', icon: 'none' });
        setTimeout(() => wx.navigateBack(), 1500);
      }
    }).catch(err => {
      console.error('加载订单详情失败:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  // 确认订单（卖家）
  confirmOrder() {
    wx.showModal({
      title: '确认订单',
      content: '确认接受这笔订单吗？确认后请尽快与买家联系交易。',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.confirm(this.data.orderId).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '已确认', icon: 'success' });
              this.loadOrderDetail();
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
  finishOrder() {
    wx.showModal({
      title: '完成交易',
      content: '请确认已完成线下交易，完成后订单将无法取消。',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.finish(this.data.orderId).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '交易完成', icon: 'success' });
              this.loadOrderDetail();
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
  cancelOrder() {
    wx.showModal({
      title: '取消订单',
      content: '确定要取消这笔订单吗？',
      editable: true,
      placeholderText: '请输入取消原因（可选）',
      success: (res) => {
        if (res.confirm) {
          util.showLoading('处理中...');
          
          api.order.cancel(this.data.orderId, res.content).then(res => {
            util.hideLoading();
            if (res && res.code === 200) {
              wx.showToast({ title: '已取消', icon: 'success' });
              this.loadOrderDetail();
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
  contactUser() {
    const order = this.data.order;
    const userId = order.isBuyer ? order.sellerId : order.buyerId;
    
    wx.navigateTo({
      url: `/pages/chat/chat?userId=${userId}&goodsId=${order.goodsId}`
    });
  },

  // 查看商品
  goToGoods() {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${this.data.order.goodsId}`
    });
  },

  // 复制订单号
  copyOrderNo() {
    wx.setClipboardData({
      data: this.data.order.orderNo,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' });
      }
    });
  },

  // 拨打电话（如果有）
  callPhone(e) {
    const phone = e.currentTarget.dataset.phone;
    if (phone) {
      wx.makePhoneCall({ phoneNumber: phone });
    }
  }
});
