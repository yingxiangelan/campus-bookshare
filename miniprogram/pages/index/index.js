// pages/index/index.js
const app = getApp();
// 【修改1】引入我们封装的 request 工具
const { request } = require('../../utils/request.js');

Page({
  data: {
    bookList: [],
    loading: false,
    hasMore: true, // 虽然后端暂时没分页，但保留这个变量以免报错
    page: 1,
    
    // 筛选条件
    currentCampus: '',
    currentMajor: '',
    currentSort: '最新发布',
    
    // 筛选弹窗控制
    showFilter: false,
    filterType: '',
    filterTitle: '',
    filterOptions: [],
    filterValue: '',
    
    // 选项数据
    campusOptions: ['全部', '东校区', '西校区', '南校区', '北校区'],
    majorOptions: ['全部', '计算机科学与技术', '软件工程', '电子信息工程', '金融学', '会计学'],
    sortOptions: ['最新发布', '价格最低', '价格最高']
  },

  onLoad() {
    this.loadBookList();
  },

  onShow() {
    // 页面显示时，如果列表是空的，尝试加载一下
    if (this.data.bookList.length === 0) {
      this.loadBookList();
    }
  },

  onPullDownRefresh() {
    // 下拉刷新时，重置数据
    this.refreshList();
  },

  onReachBottom() {
    // 触底加载更多
    this.loadMore();
  },

  // 【核心修改】加载书籍列表
  loadBookList() {
    // 如果正在加载，或者显示没有更多了（且不是第一页），就停止
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    
    // 这里保留了原本的参数结构，虽然目前后端暂时忽略了它们
    // 等以后后端写了筛选逻辑，这些参数就有用了
    const params = {
      campus: this.data.currentCampus === '全部' ? '' : this.data.currentCampus,
      major: this.data.currentMajor === '全部' ? '' : this.data.currentMajor,
      sort: this.data.currentSort
    };
    
    // 【修改2】调用我们自己的后端接口
    request('/goods/list', 'GET', params).then(res => {
      // 停止下拉刷新
      wx.stopPullDownRefresh();

      if (res.code === 200 || res.success) {
        const list = res.data || [];
        
        // 【修改3】数据处理
        // 因为后端是一次性返回所有数据，所以这里直接覆盖，并告诉页面“没有更多了”
        this.setData({
          bookList: list,
          loading: false,
          hasMore: false // 暂时禁用分页，因为后端还没做分页
        });
      } else {
        this.setData({ loading: false });
      }
    }).catch(err => {
      console.error('获取书籍失败', err);
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
    });
  },

  // 刷新列表
  refreshList() {
    this.setData({
      page: 1,
      hasMore: true,
      loading: false
    });
    this.loadBookList();
  },

  // 加载更多
  loadMore() {
    if (!this.data.hasMore) return;
    // 目前后端是一次性返回，所以这里暂不执行逻辑
    // 后续后端实现了分页接口后，在这里 page + 1
  },

  // ----------------------------------------------------
  // 下面的 UI 交互逻辑（筛选弹窗等）完全保留原样，不需要动
  // ----------------------------------------------------

  onSearchTap() {
    wx.showToast({ title: '搜索功能开发中', icon: 'none' });
  },

  showCampusFilter() {
    this.setData({
      showFilter: true,
      filterType: 'campus',
      filterTitle: '选择校区',
      filterOptions: this.data.campusOptions,
      filterValue: this.data.currentCampus || '全部'
    });
  },

  showMajorFilter() {
    this.setData({
      showFilter: true,
      filterType: 'major',
      filterTitle: '选择专业',
      filterOptions: this.data.majorOptions,
      filterValue: this.data.currentMajor || '全部'
    });
  },

  showSortFilter() {
    this.setData({
      showFilter: true,
      filterType: 'sort',
      filterTitle: '排序方式',
      filterOptions: this.data.sortOptions,
      filterValue: this.data.currentSort
    });
  },

  selectFilter(e) {
    const value = e.currentTarget.dataset.value;
    const filterType = this.data.filterType;
    
    if (filterType === 'campus') {
      this.setData({ currentCampus: value === '全部' ? '' : value, showFilter: false });
    } else if (filterType === 'major') {
      this.setData({ currentMajor: value === '全部' ? '' : value, showFilter: false });
    } else if (filterType === 'sort') {
      this.setData({ currentSort: value, showFilter: false });
    }
    
    // 选完筛选条件后，重新刷新列表
    this.refreshList();
  },

  hideFilter() {
    this.setData({ showFilter: false });
  },

  stopPropagation() {},

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    });
  }
});