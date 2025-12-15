// utils/request.js - API请求封装
const app = getApp();

/**
 * 核心请求函数
 * @param {String} url 请求路径 (如 /user/login)
 * @param {String} method 请求方法 (GET, POST等)
 * @param {Object} data 请求参数
 */
function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    // 1. 处理 URL：如果传入的是相对路径，拼接上 BaseUrl
    let fullUrl = url;
    if (!url.startsWith('http')) {
      // 确保 baseUrl 后面没有 /，或者 url 前面没有 /，防止双斜杠 (虽然 http://a//b 通常也能通过，但最好处理一下)
      const baseUrl = app.globalData.baseUrl;
      fullUrl = `${baseUrl}${url}`;
    }

    // 2. 处理 Token
    const header = {
      'content-type': 'application/json'
    };
    
    const token = wx.getStorageSync('token');
    if (token) {
      // 【注意】这里我们不加 Bearer 前缀，直接发 token，配合我们写的简易后端
      header['Authorization'] = token;
    }

    // 3. 发起请求
    wx.request({
      url: fullUrl,
      method: method,
      data: data,
      header: header,
      success: (res) => {
        // 4. 统一处理结果
        // 我们直接返回 res.data，让调用的页面自己判断 code === 200
        // 这样更灵活，防止原本的项目结构 Result 包装不一样
        resolve(res.data);

        // 如果后端返回 401 (未登录)，可以加一个自动跳转逻辑
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
           wx.removeStorageSync('token'); // 清除过期token
           // 只有不在登录页才跳转，防止死循环
           const pages = getCurrentPages();
           const currentPage = pages[pages.length - 1];
           if (currentPage.route.indexOf('login') === -1) {
              wx.showToast({ title: '登录已过期', icon: 'none' });
           }
        }
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
 * 上传文件 (保留原本的功能，但修改 Token 逻辑)
 */
function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/file/upload`,
      filePath,
      name: 'file',
      header: {
        // 同样去掉 Bearer，直接传 token
        'Authorization': token || ''
      },
      success: (res) => {
        // uploadFile 返回的 data 是字符串，必须 parse
        const data = JSON.parse(res.data);
        if (data.code === 200 || data.success) {
          resolve(data.data); // 返回图片路径
        } else {
          wx.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          });
          reject(data);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
}

// 导出方法
module.exports = {
  // 1. 导出 request 供 login.js 和 profile.js 使用
  request, 
  
  // 2. 导出 uploadFile 供 login.js 上传头像使用
  uploadFile,

  // 3. 兼容原本项目里的 api.js (如果 api.js 里用了 request.get)
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  delete: (url, data) => request(url, 'DELETE', data)
};