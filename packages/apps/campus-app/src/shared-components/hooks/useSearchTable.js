/**
 * 分页搜索 Hook（参考 dtp_web commonmodule/hooks/search-table.js）
 * @param {Function} getData - 获取数据的异步函数 (params) => Promise
 * @param {Object} config - 配置项
 * @param {Object} config.searchForm - 搜索表单初始值
 * @param {Object} config.pagination - 分页初始配置（覆盖默认值）
 * @param {boolean} config.remainSelectedKeys - 翻页是否保留选中
 * @returns {{ searchForm, tableData, loading, pagination, selectedKeys, pageChange, pageSizeChange, sorterChange, search, reset }}
 */
import { ref } from "vue";

export default function useSearchTable(_getData, _config = {}) {
  const configStr = JSON.stringify(_config);
  const searchForm = ref({});
  const tableData = ref([]);
  const loading = ref(false);
  const pagination = ref({});
  const sorter = ref({});
  const selectedKeys = ref([]);
  let remainSelectedKeys = false;
  let getData = async () => ({});

  const setDefault = () => {
    const config = JSON.parse(configStr);
    searchForm.value = config.searchForm || {};
    pagination.value = {
      showTotal: true,
      showJumper: true,
      showPageSize: true,
      current: 1,
      pageSize: 10,
      total: 0,
      ...config.pagination,
    };
    remainSelectedKeys = config.remainSelectedKeys || false;
    getData = _getData || (async () => ({}));
  };
  setDefault();

  const pageChange = async (p) => {
    loading.value = true;
    pagination.value.current = p;
    if (!remainSelectedKeys) selectedKeys.value = [];
    const params = {
      ...searchForm.value,
      ...sorter.value,
      pageSize: pagination.value.pageSize,
      pageNo: pagination.value.current,
    };
    try {
      const res = await getData(params);
      tableData.value = res?.rows || res?.data?.rows || [];
      pagination.value.total = res?.totalCount ?? res?.data?.totalCount ?? 0;
    } catch (e) {
      console.error("[useSearchTable] pageChange error:", e);
    } finally {
      loading.value = false;
    }
  };

  const pageSizeChange = (s) => {
    pagination.value.pageSize = s;
    pageChange(1);
  };

  const sorterChange = (dataIndex, sortOrder) => {
    sorter.value = {
      orderBy: dataIndex,
      sort: { ascend: "asc", descend: "desc" }[sortOrder] || null,
    };
    pageChange(1);
  };

  const search = () => {
    selectedKeys.value = [];
    pageChange(1);
  };

  const reset = (cb) => {
    setDefault();
    selectedKeys.value = [];
    cb?.();
  };

  return {
    searchForm,
    tableData,
    loading,
    pagination,
    selectedKeys,
    pageChange,
    pageSizeChange,
    sorterChange,
    search,
    reset,
  };
}
