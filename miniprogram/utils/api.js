// utils/api.js - API接口定义
const request = require('./request.js');

module.exports = {
  // 用户相关
  user: {
    login: (code) => request.post('/user/login', { code }),
    getUserInfo: () => request.get('/user/info'),
    updateProfile: (data) => request.put('/user/profile', data),
    certify: (data) => request.post('/user/certify', data)
  },

  // 书籍相关
  book: {
    getList: (params) => request.get('/book/list', params),
    getDetail: (id) => request.get(`/book/detail/${id}`),
    search: (keyword) => request.get('/book/search', { keyword }),
    getByISBN: (isbn) => request.get(`/book/isbn/${isbn}`),
    recognizeImage: (imageUrl) => request.post('/book/recognize', { imageUrl })
  },

  // 商品相关
  goods: {
    publish: (data) => request.post('/goods/publish', data),
    getList: (params) => request.get('/goods/list', params),
    getDetail: (id) => request.get(`/goods/detail/${id}`),
    getMyGoods: () => request.get('/goods/my'),
    updateStatus: (id, status) => request.put(`/goods/${id}/status`, { status }),
    delete: (id) => request.delete(`/goods/${id}`)
  },

  // 消息相关
  message: {
    getList: () => request.get('/message/list'),
    getConversation: (userId) => request.get(`/message/conversation/${userId}`),
    send: (data) => request.post('/message/send', data),
    markRead: (ids) => request.put('/message/read', { ids })
  },

  // 订单相关
  order: {
    create: (data) => request.post('/order/create', data),
    getList: (type) => request.get('/order/list', { type }),
    getDetail: (id) => request.get(`/order/detail/${id}`),
    confirm: (id) => request.put(`/order/${id}/confirm`),
    cancel: (id) => request.put(`/order/${id}/cancel`)
  },

  // 评价相关
  review: {
    create: (data) => request.post('/review/create', data),
    getList: (userId) => request.get(`/review/list/${userId}`)
  },

  // 文件上传
  file: {
    upload: (filePath) => request.uploadFile(filePath)
  }
};
