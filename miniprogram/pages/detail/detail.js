// pages/detail/detail.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    bookId: '',
    bookDetail: {
      images: [],
      bookName: '',
      author: '',
      publisher: '',
      price: 0,
      originalPrice: 0,
      major: '',
      condition: '',
      campus: '',
      sellerAvatar: '',
      sellerName: '',
      sellerCertified: false,
      sellerRate: 0,
      isbn: '',
      courseName: '',
      publishTime: '',
      viewCount: 0,
      description: '',
      isCollected: false,
      sellerId: ''
    },
    canPurchase: true  // 是否可以购买
  },

  onLoad(options) {
    if (options.id) {
      this.setData({
        bookId: options.id
      });
      this.loadBookDetail();
    }
  },

  onShow() {
    // 页面显示时刷新数据
    if (this.data.bookId) {
      this.loadBookDetail();
      this.checkCanPurchase();
    }
  },

  // 加载书籍详情
  loadBookDetail() {
    util.showLoading('加载中...');
    
    api.goods.getDetail(this.data.bookId).then(res => {
      console.log('详情接口返回:', res);  // 调试日志
      
      // 解析数据 - 兼容多种格式
      let detail = null;
      
      if (res && res.code === 200 && res.data) {
        // 格式: {code: 200, data: {...}}
        detail = res.data;
      } else if (res && !res.code) {
        // 直接返回详情对象
        detail = res;
      }
      
      if (!detail) {
        util.hideLoading();
        wx.showToast({
          title: '商品不存在',
          icon: 'none'
        });
        setTimeout(() => wx.navigateBack(), 1500);
        return;
      }
      
      // 格式化发布时间
      if (detail.createTime) {
        detail.publishTime = util.timeAgo(detail.createTime);
      }
      
      // 处理图片（确保是数组）
      if (detail.images) {
        if (typeof detail.images === 'string') {
          // 如果是逗号分隔的字符串，转为数组
          detail.images = detail.images.split(',').map(img => img.trim()).filter(img => img);
        }
      } else {
        detail.images = [];
      }
      
      // 设置默认值
      detail.sellerAvatar = detail.sellerAvatar || '/images/default-avatar.png';
      detail.sellerName = detail.sellerName || '卖家';
      detail.viewCount = detail.viewCount || 0;
      detail.isCollected = detail.isCollected || false;
      
      console.log('解析后的详情:', detail);  // 调试日志
      
      this.setData({
        bookDetail: detail
      });
      
      util.hideLoading();
      
      // 检查是否可购买
      this.checkCanPurchase();
    }).catch((err) => {
      console.error('加载详情失败:', err);
      util.hideLoading();
      wx.showModal({
        title: '提示',
        content: '加载失败，是否重试？',
        success: (res) => {
          if (res.confirm) {
            this.loadBookDetail();
          } else {
            wx.navigateBack();
          }
        }
      });
    });
  },

  // 检查是否可以购买
  checkCanPurchase() {
    api.order.checkPurchase(this.data.bookId).then(res => {
      if (res && res.code === 200 && res.data) {
        this.setData({
          canPurchase: res.data.canPurchase
        });
      }
    }).catch(err => {
      console.error('检查购买状态失败:', err);
    });
  },

  // 预览图片
  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    util.previewImage(url, this.data.bookDetail.images);
  },

  // 收藏/取消收藏
  collectBook() {
    if (!app.globalData.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/profile/profile'
        });
      }, 1500);
      return;
    }

    const isCollected = this.data.bookDetail.isCollected;
    const action = isCollected ? '取消收藏' : '收藏';
    
    // 这里应该调用API，暂时只做UI更新
    this.setData({
      'bookDetail.isCollected': !isCollected
    });
    
    wx.showToast({
      title: action + '成功',
      icon: 'success'
    });
  },

  // 联系卖家
  contactSeller() {
    if (!app.globalData.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/profile/profile'
        });
      }, 1500);
      return;
    }

    // 跳转到聊天页面，并传递卖家ID
    wx.navigateTo({
      url: `/pages/chat/chat?userId=${this.data.bookDetail.sellerId}&goodsId=${this.data.bookId}`
    });
  },

  // 购买书籍
  buyBook() {
    if (!app.globalData.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/profile/profile'
        });
      }, 1500);
      return;
    }

    // 检查是否为自己的商品
    const userInfo = app.globalData.userInfo;
    if (userInfo && userInfo.id === this.data.bookDetail.sellerId) {
      wx.showToast({
        title: '不能购买自己的商品',
        icon: 'none'
      });
      return;
    }

    // 检查是否可购买
    if (!this.data.canPurchase) {
      wx.showToast({
        title: '该商品有订单正在处理中',
        icon: 'none'
      });
      return;
    }

    // 检查商品状态
    if (this.data.bookDetail.status !== 0) {
      wx.showToast({
        title: '商品已下架或已售出',
        icon: 'none'
      });
      return;
    }

    // 显示购买确认弹窗（带留言输入）
    wx.showModal({
      title: '确认购买',
      content: `确认要购买《${this.data.bookDetail.bookName}》吗？\n价格：¥${this.data.bookDetail.price}`,
      editable: true,
      placeholderText: '给卖家留言（可选）',
      success: (res) => {
        if (res.confirm) {
          this.createOrder(res.content);
        }
      }
    });
  },

  // 创建订单
  createOrder(buyerMessage) {
    util.showLoading('提交订单中...');
    
    api.order.create({
      goodsId: this.data.bookId,
      buyerMessage: buyerMessage || ''
    }).then(res => {
      util.hideLoading();
      
      if (res && res.code === 200 && res.data) {
        const orderData = res.data;
        
        wx.showModal({
          title: '下单成功',
          content: `订单号：${orderData.orderNo}\n请等待卖家确认，您可以先与卖家沟通交易细节。`,
          confirmText: '查看订单',
          cancelText: '联系卖家',
          success: (modalRes) => {
            if (modalRes.confirm) {
              // 查看订单详情
              wx.navigateTo({
                url: `/pages/order/detail?id=${orderData.id}`
              });
            } else {
              // 联系卖家
              this.contactSeller();
            }
          }
        });
        
        // 更新购买状态
        this.setData({ canPurchase: false });
      } else {
        wx.showToast({
          title: res.message || '下单失败',
          icon: 'none'
        });
      }
    }).catch(err => {
      util.hideLoading();
      console.error('创建订单失败:', err);
      wx.showToast({
        title: '网络错误，请重试',
        icon: 'none'
      });
    });
  }
});
