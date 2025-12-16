/**
 * WebSocket工具类 (融合优化版)
 * 
 * 功能特性：
 * - 自动重连机制
 * - 心跳保活检测
 * - 消息队列缓存
 * - 多种事件回调
 * 
 * @author 组员原版 + 融合优化
 */

const app = getApp();

// ============ 状态变量 ============
let socketOpen = false;
let socketConnecting = false;
let socketTask = null;
let socketMsgQueue = [];  // 消息队列
let currentUserId = null;

// ============ 回调函数 ============
let onMessageCallback = null;

// ============ 心跳配置 ============
const HEARTBEAT_INTERVAL = 30000; // 30秒
let heartBeatTimer = null;

// ============ 重连配置 ============
const RECONNECT_INTERVAL = 5000;  // 5秒后重连
const MAX_RECONNECT_TIMES = 5;    // 最大重连次数
let reconnectTimes = 0;
let reconnectTimer = null;

/**
 * 初始化WebSocket连接
 * @param {Number} userId 当前登录用户ID
 */
function connect(userId) {
  if (socketOpen || socketConnecting) {
    console.log('[WS] 已连接或正在连接中');
    return;
  }

  currentUserId = userId;
  socketConnecting = true;

  // 智能构建WebSocket URL
  let wsUrl;
  if (app.globalData && app.globalData.baseUrl) {
    // 从全局配置构建
    const baseUrl = app.globalData.baseUrl;
    wsUrl = baseUrl
      .replace('http://', 'ws://')
      .replace('https://', 'wss://')
      .replace('/api', '') + `/websocket/${userId}`;
  } else {
    // 默认地址
    wsUrl = `ws://localhost:8080/websocket/${userId}`;
  }

  console.log('[WS] 正在连接:', wsUrl);

  socketTask = wx.connectSocket({
    url: wsUrl,
    success: () => console.log('[WS] 连接请求已发送'),
    fail: (err) => {
      console.error('[WS] 连接失败:', err);
      socketConnecting = false;
      scheduleReconnect();
    }
  });

  // 连接成功
  socketTask.onOpen(() => {
    console.log('[WS] ✅ 连接成功!');
    socketOpen = true;
    socketConnecting = false;
    reconnectTimes = 0;

    // 发送队列中积压的消息
    flushMessageQueue();

    // 开启心跳
    startHeartBeat();
  });

  // 收到消息
  socketTask.onMessage((res) => {
    // 处理心跳响应
    if (res.data === 'PONG' || res.data === '{"type":"pong"}') {
      return;
    }

    try {
      const data = JSON.parse(res.data);
      if (onMessageCallback) {
        onMessageCallback(data);
      }
    } catch (e) {
      // 非JSON格式消息，直接传递原始数据
      if (onMessageCallback) {
        onMessageCallback({ raw: res.data });
      }
    }
  });

  // 连接关闭
  socketTask.onClose((res) => {
    console.log('[WS] 连接已关闭');
    socketOpen = false;
    socketConnecting = false;
    stopHeartBeat();
    
    // 尝试重连
    scheduleReconnect();
  });

  // 连接错误
  socketTask.onError((err) => {
    console.error('[WS] 连接错误:', err);
    socketOpen = false;
    socketConnecting = false;
    stopHeartBeat();
  });
}

/**
 * 发送消息
 * @param {Object|String} msg 消息内容
 */
function send(msg) {
  const data = typeof msg === 'string' ? msg : JSON.stringify(msg);

  if (socketOpen && socketTask) {
    socketTask.send({
      data: data,
      fail: (err) => {
        console.error('[WS] 发送失败，加入队列:', err);
        socketMsgQueue.push(data);
      }
    });
  } else {
    // 连接未就绪，消息入队
    socketMsgQueue.push(data);
  }
}

/**
 * 发送队列中的消息
 */
function flushMessageQueue() {
  while (socketMsgQueue.length > 0 && socketOpen) {
    const msg = socketMsgQueue.shift();
    socketTask.send({ data: msg });
  }
}

/**
 * 监听消息
 * @param {Function} callback 回调函数，参数为解析后的消息对象
 */
function onMessage(callback) {
  onMessageCallback = callback;
}

/**
 * 主动关闭连接
 */
function close() {
  stopHeartBeat();
  stopReconnect();

  if (socketTask) {
    socketTask.close({
      success: () => console.log('[WS] 已主动关闭')
    });
  }

  socketOpen = false;
  socketConnecting = false;
  socketTask = null;
  currentUserId = null;
}

/**
 * 获取连接状态
 * @returns {Boolean}
 */
function isConnected() {
  return socketOpen;
}

// ============ 心跳机制 ============

function startHeartBeat() {
  stopHeartBeat();
  heartBeatTimer = setInterval(() => {
    if (socketOpen && socketTask) {
      // 发送心跳包（兼容字符串和JSON两种格式）
      socketTask.send({
        data: 'PING',
        fail: () => console.log('[WS] 心跳发送失败')
      });
    }
  }, HEARTBEAT_INTERVAL);
}

function stopHeartBeat() {
  if (heartBeatTimer) {
    clearInterval(heartBeatTimer);
    heartBeatTimer = null;
  }
}

// ============ 重连机制 ============

function scheduleReconnect() {
  if (reconnectTimer || !currentUserId) return;

  if (reconnectTimes >= MAX_RECONNECT_TIMES) {
    console.log('[WS] ❌ 达到最大重连次数，停止重连');
    wx.showToast({
      title: '连接断开，请检查网络',
      icon: 'none'
    });
    return;
  }

  reconnectTimes++;
  console.log(`[WS] ⏳ ${RECONNECT_INTERVAL/1000}秒后第${reconnectTimes}次重连...`);

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    if (!socketOpen && !socketConnecting) {
      connect(currentUserId);
    }
  }, RECONNECT_INTERVAL);
}

function stopReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  reconnectTimes = 0;
}

// ============ 导出 ============
module.exports = {
  connect,      // 建立连接
  send,         // 发送消息
  onMessage,    // 监听消息
  close,        // 关闭连接
  isConnected,  // 检查状态（函数形式）
  
  // 向后兼容：直接导出状态变量（组员原版风格）
  get socketOpen() { return socketOpen; }
};
