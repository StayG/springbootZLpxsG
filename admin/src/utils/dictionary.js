import request from "@/utils/request";

/**
 * 字典工具类
 * 用于从后端字典接口获取字典选项
 */

/**
 * 获取字典选项
 * @param {string} dicCode - 字典代码
 * @returns {Promise<Array>} 返回选项数组，格式: [{label: string, value: any}]
 */
export const getDictionaryOptions = async (dicCode) => {
  try {
    const response = await request.post("/dictionary/list", {
      dicCode: dicCode
    });
    
    // 只在开发环境显示详细日志
    if (import.meta.env.DEV) {
      console.debug(`字典接口返回 [${dicCode}]:`, response);
      console.debug(`字典接口返回 data 结构 [${dicCode}]:`, JSON.stringify(response.data, null, 2));
    }
    
    // 检查响应状态
    if (response.code !== 200) {
      console.warn(`字典接口返回非200状态码 [${dicCode}]:`, response.code, response.msg || response.message);
      return [];
    }
    
    if (!response.data) {
      if (import.meta.env.DEV) {
        console.warn(`字典接口返回数据为空 [${dicCode}]:`, response);
      }
      return [];
    }
    
    // 根据后端返回的数据结构转换
    // 支持多种可能的返回格式：
    // 1. { data: { records: [...] } }
    // 2. { data: { list: [...] } }
    // 3. { data: [...] }
    // 4. { data: { data: [...] } }
    let records = [];
    
    if (Array.isArray(response.data)) {
      records = response.data;
    } else if (response.data.records && Array.isArray(response.data.records)) {
      records = response.data.records;
    } else if (response.data.list && Array.isArray(response.data.list)) {
      records = response.data.list;
    } else if (response.data.data && Array.isArray(response.data.data)) {
      records = response.data.data;
    } else {
      // 尝试查找所有可能的数组字段
      if (import.meta.env.DEV) {
        console.warn(`字典数据格式无法识别 [${dicCode}]:`, response.data);
        console.debug(`尝试查找所有数组字段 [${dicCode}]:`, Object.keys(response.data));
      }
      // 遍历 data 对象，查找数组类型的值
      for (const key in response.data) {
        if (Array.isArray(response.data[key])) {
          if (import.meta.env.DEV) {
            console.debug(`找到数组字段 [${dicCode}]:`, key, response.data[key]);
          }
          records = response.data[key];
          break;
        }
      }
      if (records.length === 0) {
        return [];
      }
    }
    
    if (import.meta.env.DEV) {
      console.debug(`字典原始数据 [${dicCode}]:`, records, `共 ${records.length} 条`);
    }
    
    if (records.length === 0) {
      // 改为 debug 级别，因为会使用默认选项
      console.debug(`字典数据为空 [${dicCode}]: 将使用默认选项`);
      return [];
    }
    
    // 检查是否有 null 值（静默处理，不显示警告，因为会使用默认选项）
    const nullCount = records.filter(item => item === null || item === undefined).length;
    if (nullCount > 0 && nullCount === records.length) {
      // 只有在所有数据都是 null 时才记录（开发调试用）
      console.debug(`字典数据全部为 null [${dicCode}]: 将使用默认选项`);
    }
    
    // 过滤掉 null 或 undefined 的项，并转换格式
    const options = records
      .filter(item => {
        if (item === null || item === undefined) {
          return false;
        }
        // 检查是否有必要的字段
        const hasLabel = item.indexName || item.name || item.label || item.text || item.dictLabel;
        const hasValue = item.codeIndex !== undefined || item.value !== undefined || item.code !== undefined || item.dictValue !== undefined;
        return hasLabel && hasValue;
      })
      .map(item => {
        // 尝试多种可能的字段名
        const label = item.indexName || item.name || item.label || item.text || item.dictLabel || '';
        const value = item.codeIndex !== undefined ? item.codeIndex : 
                     (item.value !== undefined ? item.value : 
                     (item.code !== undefined ? item.code : 
                     (item.dictValue !== undefined ? item.dictValue : '')));
        
        return { label, value };
      })
      .filter(item => item.label && item.value !== undefined && item.value !== null && item.value !== '') // 过滤无效项
      .sort((a, b) => {
        // 按值排序
        if (typeof a.value === 'number' && typeof b.value === 'number') {
          return a.value - b.value;
        }
        return String(a.value).localeCompare(String(b.value));
      });
    
    console.debug(`字典转换后选项 [${dicCode}]:`, options, `共 ${options.length} 条有效选项`);
    
    if (options.length === 0) {
      // 改为 debug 级别，因为会使用默认选项，不是错误
      console.debug(`字典数据转换后为空 [${dicCode}]: 将使用默认选项`);
    }
    
    return options;
  } catch (error) {
    console.error(`获取字典选项失败 [${dicCode}]:`, error);
    console.error('错误详情:', error.response || error.message);
    return [];
  }
};

/**
 * 获取试题类型选项
 * @returns {Promise<Array>}
 */
export const getExamQuestionTypeOptions = () => {
  return getDictionaryOptions("exam_question_types");
};

/**
 * 获取性别选项
 * @returns {Promise<Array>}
 */
export const getGenderOptions = () => {
  return getDictionaryOptions("sex_types");
};

/**
 * 获取科目选项
 * @returns {Promise<Array>}
 */
export const getKemuOptions = () => {
  return getDictionaryOptions("kemu_types");
};

/**
 * 获取组卷方式选项
 * @returns {Promise<Array>}
 */
export const getZujuanOptions = () => {
  return getDictionaryOptions("zujuan_types");
};

/**
 * 获取试卷状态选项
 * @returns {Promise<Array>}
 */
export const getExamPaperStatusOptions = () => {
  return getDictionaryOptions("exam_paper_types");
};

export default {
  getDictionaryOptions,
  getExamQuestionTypeOptions,
  getGenderOptions,
  getKemuOptions,
  getZujuanOptions,
  getExamPaperStatusOptions
};

