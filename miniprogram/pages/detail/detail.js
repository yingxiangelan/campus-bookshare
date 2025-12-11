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
    }
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
    }
  },

  // 加载书籍详情
  loadBookDetail() {
    util.showLoading('加载中...');
    
    api.goods.getDetail(this.data.bookId).then(res => {
      // 格式化数据
      res.publishTime = util.timeAgo(res.createTime);
      
      this.setData({
        bookDetail: res
      });
      
      util.hideLoading();
    }).catch(() => {
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

    // 跳转到消息页面，并传递卖家ID
    wx.navigateTo({
      url: `/pages/message/chat?userId=${this.data.bookDetail.sellerId}&bookId=${this.data.bookId}`
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

    wx.showModal({
      title: '确认购买',
      content: `确认要购买《${this.data.bookDetail.bookName}》吗？`,
      success: (res) => {
        if (res.confirm) {
          // 创建订单
          api.order.create({
            goodsId: this.data.bookId,
            sellerId: this.data.bookDetail.sellerId
          }).then(() => {
            wx.showToast({
              title: '购买成功',
              icon: 'success'
            });
            
            setTimeout(() => {
              // 跳转到消息页面与卖家沟通
              this.contactSeller();
            }, 1500);
          }).catch(() => {
            wx.showToast({
              title: '购买失败',
              icon: 'none'
            });
          });
        }
      }
    });
  }
});
