// utils/request.js - API请求封装（融合版）
const app = getApp();

/**
 * 核心请求函数
 * @param {String} url 请求路径 (如 /user/login)
 * @param {String} method 请求方法 (GET, POST等)
 * @param {Object} data 请求参数
 */
function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    // 1. 处理URL
    let fullUrl = url;
    if (!url.startsWith('http')) {
      const baseUrl = app.globalData.baseUrl;
      fullUrl = `${baseUrl}${url}`;
    }

    // 2. 构建请求头
    const header = {
      'content-type': 'application/json'
    };
    
    // 添加Token（不带Bearer前缀，适配简易后端）
    const token = wx.getStorageSync('token');
    if (token) {
      header['Authorization'] = token;
    }

    // 3. 发起请求
    wx.request({
      url: fullUrl,
      method: method,
      data: data,
      header: header,
      success: (res) => {
        // 处理401未授权
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
          wx.removeStorageSync('token');
          app.globalData.token = null;
          app.globalData.isLogin = false;
          
          // 不在登录页时提示
          const pages = getCurrentPages();
          const currentPage = pages[pages.length - 1];
          if (currentPage && currentPage.route && currentPage.route.indexOf('login') === -1) {
            wx.showToast({ 
              title: '登录已过期', 
              icon: 'none' 
            });
          }
        }
        
        // 返回数据
        resolve(res.data);
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

/**
 * 上传文件
 * @param {String} filePath 文件临时路径
 */
function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/file/upload`,
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token || ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(data.data);
          } else {
            wx.showToast({
              title: data.message || '上传失败',
              icon: 'none'
            });
            reject(data);
          }
        } catch (e) {
          reject(e);
        }
      },
      fail: (err) => {
        wx.showToast({ 
          title: '上传失败', 
          icon: 'none' 
        });
        reject(err);
      }
    });
  });
}

// 导出方法（兼容两种调用方式）
module.exports = {
  // 新版调用方式: request('/url', 'POST', data)
  request,
  uploadFile,
  
  // 兼容原版调用方式: request.get('/url', data)
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  delete: (url, data) => request(url, 'DELETE', data)
};
