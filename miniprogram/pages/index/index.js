// pages/index/index.js
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

Page({
  data: {
    bookList: [],
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 10,
    
    // 筛选条件
    currentCampus: '',
    currentMajor: '',
    currentSort: '最新发布',
    
    // 筛选弹窗
    showFilter: false,
    filterType: '',
    filterTitle: '',
    filterOptions: [],
    filterValue: '',
    
    // 校区选项
    campusOptions: ['全部', '国际校区', '五山校区', '大学城校区'],
    // 专业选项
    majorOptions: ['全部', '人工智能','大数据','计算机科学与技术', '软件工程', '电子信息工程', '机械工程', '金融学', '会计学', '英语', '汉语言文学'],
    // 排序选项
    sortOptions: ['最新发布', '价格最低', '价格最高']
  },

  onLoad() {
    this.loadBookList();
  },

  onShow() {
    // 页面显示时刷新数据（添加安全检查）
    if (this.data.bookList && this.data.bookList.length > 0) {
      this.refreshList();
    }
  },

  onPullDownRefresh() {
    this.refreshList();
  },

  onReachBottom() {
    this.loadMore();
  },

  // 加载书籍列表
loadBookList() {
  if (this.data.loading || !this.data.hasMore) return;
  
  this.setData({ loading: true });
  
  const params = {
    page: this.data.page,
    pageSize: this.data.pageSize,
    campus: this.data.currentCampus === '全部' ? '' : this.data.currentCampus,
    major: this.data.currentMajor === '全部' ? '' : this.data.currentMajor,
    sortType: this.getSortType(this.data.currentSort)
  };
  
  api.goods.getList(params).then(res => {
    console.log('接口返回数据:', JSON.stringify(res));  // 调试日志
    
    // 初始化默认值
    let list = [];
    let hasMore = false;
    
    // 解析数据 - 兼容多种格式
    if (res && res.code === 200 && res.data) {
      // 格式1: {code: 200, data: {list: [], hasMore: true}}
      if (res.data.list && Array.isArray(res.data.list)) {
        list = res.data.list;
        hasMore = res.data.hasMore === true;
      } 
      // 格式2: {code: 200, data: []}
      else if (Array.isArray(res.data)) {
        list = res.data;
        hasMore = list.length >= this.data.pageSize;
      }
    } 
    // 格式3: {list: [], hasMore: true}
    else if (res && res.list && Array.isArray(res.list)) {
      list = res.list;
      hasMore = res.hasMore === true;
    }
    // 格式4: 直接是数组
    else if (Array.isArray(res)) {
      list = res;
      hasMore = list.length >= this.data.pageSize;
    }
    
    // 确保 list 是数组
    if (!Array.isArray(list)) {
      list = [];
    }
    
    // 合并列表
    const currentList = this.data.bookList || [];
    const newList = this.data.page === 1 ? list : [...currentList, ...list];
    
    console.log('解析后的列表:', newList.length, '条');  // 调试日志
    
    this.setData({
      bookList: newList,
      loading: false,
      hasMore: hasMore,
      page: this.data.page + 1
    });
    
    wx.stopPullDownRefresh();
  }).catch((err) => {
    console.error('加载失败:', err);
    this.setData({ 
      loading: false,
      bookList: this.data.bookList || [],
      hasMore: false
    });
    wx.stopPullDownRefresh();
  });
},

  // 刷新列表
  refreshList() {
    this.setData({
      bookList: [],
      page: 1,
      hasMore: true
    });
    this.loadBookList();
  },

  // 加载更多
  loadMore() {
    this.loadBookList();
  },

  // 获取排序类型
  getSortType(sortText) {
    const sortMap = {
      '最新发布': 'time',
      '价格最低': 'price_asc',
      '价格最高': 'price_desc'
    };
    return sortMap[sortText] || 'time';
  },

  // 搜索
  onSearchTap() {
    wx.showToast({
      title: '搜索功能开发中',
      icon: 'none'
    });
  },

  // 显示校区筛选
  showCampusFilter() {
    this.setData({
      showFilter: true,
      filterType: 'campus',
      filterTitle: '选择校区',
      filterOptions: this.data.campusOptions,
      filterValue: this.data.currentCampus || '全部'
    });
  },

  // 显示专业筛选
  showMajorFilter() {
    this.setData({
      showFilter: true,
      filterType: 'major',
      filterTitle: '选择专业',
      filterOptions: this.data.majorOptions,
      filterValue: this.data.currentMajor || '全部'
    });
  },

  // 显示排序筛选
  showSortFilter() {
    this.setData({
      showFilter: true,
      filterType: 'sort',
      filterTitle: '排序方式',
      filterOptions: this.data.sortOptions,
      filterValue: this.data.currentSort
    });
  },

  // 选择筛选项
  selectFilter(e) {
    const value = e.currentTarget.dataset.value;
    const filterType = this.data.filterType;
    
    if (filterType === 'campus') {
      this.setData({
        currentCampus: value === '全部' ? '' : value,
        showFilter: false
      });
    } else if (filterType === 'major') {
      this.setData({
        currentMajor: value === '全部' ? '' : value,
        showFilter: false
      });
    } else if (filterType === 'sort') {
      this.setData({
        currentSort: value,
        showFilter: false
      });
    }
    
    // 重新加载列表
    this.refreshList();
  },

  // 隐藏筛选弹窗
  hideFilter() {
    this.setData({
      showFilter: false
    });
  },

  // 阻止冒泡
  stopPropagation() {},

  // 跳转到详情页
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    });
  }
});
