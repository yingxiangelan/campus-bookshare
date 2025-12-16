// pages/publish/publish.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    images: [],
    formData: {
      bookName: '',
      author: '',
      publisher: '',
      isbn: '',
      price: '',
      originalPrice: '',
      condition: '',
      campus: '',
      major: '',
      courseName: '',
      description: ''
    },
    
    conditionOptions: ['全新', '9成新', '8成新', '7成新', '6成新及以下'],
    conditionIndex: -1,
    
    campusOptions: ['国际校区', '五山校区', '大学城校区'],
    campusIndex: -1,
    
    majorOptions: ['人工智能','大数据','计算机科学与技术', '软件工程', '电子信息工程', '机械工程', '金融学', '会计学', '英语', '汉语言文学'],
    majorIndex: -1,
    
    suggestedPrice: ''
  },

  onLoad() {
    // 检查登录状态
    if (!app.globalData.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        showCancel: false,
        success: () => {
          wx.switchTab({
            url: '/pages/profile/profile'
          });
        }
      });
    }
  },

  // 扫描ISBN码
  scanISBN() {
    wx.scanCode({
      onlyFromCamera: true,
      scanType: ['barCode'],
      success: (res) => {
        const isbn = res.result;
        
        if (util.validateISBN(isbn)) {
          this.loadBookByISBN(isbn);
        } else {
          wx.showToast({
            title: 'ISBN码无效',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '扫描取消',
          icon: 'none'
        });
      }
    });
  },

  // 根据ISBN加载书籍信息
  loadBookByISBN(isbn) {
    util.showLoading('识别中...');
    
    api.book.getByISBN(isbn).then(res => {
      this.setData({
        'formData.bookName': res.bookName,
        'formData.author': res.author,
        'formData.publisher': res.publisher,
        'formData.isbn': res.isbn,
        'formData.originalPrice': res.price,
        suggestedPrice: (res.price * 0.6).toFixed(2)
      });
      
      util.hideLoading();
      wx.showToast({
        title: '识别成功',
        icon: 'success'
      });
    }).catch(() => {
      util.hideLoading();
      wx.showToast({
        title: '识别失败，请手动填写',
        icon: 'none'
      });
    });
  },

  // 拍照识别
  recognizeImage() {
    util.chooseImage(1).then(paths => {
      util.showLoading('上传中...');
      
      return api.file.upload(paths[0]);
    }).then(imageUrl => {
      util.showLoading('识别中...');
      
      return api.book.recognizeImage(imageUrl);
    }).then(res => {
      this.setData({
        'formData.bookName': res.bookName || '',
        'formData.author': res.author || '',
        'formData.isbn': res.isbn || ''
      });
      
      util.hideLoading();
      wx.showToast({
        title: '识别成功',
        icon: 'success'
      });
    }).catch(() => {
      util.hideLoading();
      wx.showToast({
        title: '识别失败，请手动填写',
        icon: 'none'
      });
    });
  },

  // 选择图片
  chooseImage() {
    const maxCount = 9 - this.data.images.length;
    
    util.chooseImage(maxCount).then(paths => {
      this.setData({
        images: [...this.data.images, ...paths]
      });
    });
  },

  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images;
    images.splice(index, 1);
    
    this.setData({
      images
    });
  },

  // 输入框变化
  onInputChange(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    
    this.setData({
      [`formData.${field}`]: value
    });
    
    // 如果是价格变化，更新建议价格
    if (field === 'originalPrice' && value) {
      this.setData({
        suggestedPrice: (parseFloat(value) * 0.6).toFixed(2)
      });
    }
  },

  // 选择新旧程度
  onConditionChange(e) {
    const index = e.detail.value;
    this.setData({
      conditionIndex: index,
      'formData.condition': this.data.conditionOptions[index]
    });
  },

  // 选择校区
  onCampusChange(e) {
    const index = e.detail.value;
    this.setData({
      campusIndex: index,
      'formData.campus': this.data.campusOptions[index]
    });
  },

  // 选择专业
  onMajorChange(e) {
    const index = e.detail.value;
    this.setData({
      majorIndex: index,
      'formData.major': this.data.majorOptions[index]
    });
  },

  // 验证表单
  validateForm() {
    const { bookName, price, condition, campus } = this.data.formData;
    
    if (!bookName) {
      wx.showToast({ title: '请输入书名', icon: 'none' });
      return false;
    }
    
    if (!price || parseFloat(price) <= 0) {
      wx.showToast({ title: '请输入正确的价格', icon: 'none' });
      return false;
    }
    
    if (!condition) {
      wx.showToast({ title: '请选择新旧程度', icon: 'none' });
      return false;
    }
    
    if (!campus) {
      wx.showToast({ title: '请选择校区', icon: 'none' });
      return false;
    }
    
    if (this.data.images.length === 0) {
      wx.showToast({ title: '请至少上传一张图片', icon: 'none' });
      return false;
    }
    
    return true;
  },

  // 提交发布
  submitPublish() {
    if (!this.validateForm()) {
      return;
    }
    
    util.showLoading('发布中...');
    
    // 先上传所有图片
    const uploadPromises = this.data.images.map(path => {
      return api.file.upload(path);
    });
    
    Promise.all(uploadPromises).then(imageUrls => {
      // 准备数据
      const data = {
        ...this.data.formData,
        images: imageUrls
      };
      
      // 发布商品
      return api.goods.publish(data);
    }).then(() => {
      util.hideLoading();
      
      wx.showToast({
        title: '发布成功',
        icon: 'success'
      });
      
      // 重置表单
      setTimeout(() => {
        this.resetForm();
        wx.switchTab({
          url: '/pages/index/index'
        });
      }, 1500);
    }).catch(() => {
      util.hideLoading();
      wx.showToast({
        title: '发布失败',
        icon: 'none'
      });
    });
  },

  // 重置表单
  resetForm() {
    this.setData({
      images: [],
      formData: {
        bookName: '',
        author: '',
        publisher: '',
        isbn: '',
        price: '',
        originalPrice: '',
        condition: '',
        campus: '',
        major: '',
        courseName: '',
        description: ''
      },
      conditionIndex: -1,
      campusIndex: -1,
      majorIndex: -1,
      suggestedPrice: ''
    });
  }
});
