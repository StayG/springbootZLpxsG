<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import conifg from '@/config/config.js'
import { ElMessage } from 'element-plus'
const fileList = ref([])
const fileUrlList = ref([])
const returnList = ref([])

const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const imgAlt = ref('预览')
const emit = defineEmits(['change'])
/**
 * 数据传递
 */
const props = defineProps({
  action: {
    type: String
  },
  multiple: {
    type: Boolean,
    default: false
  },
  limit: {
    type: Number,
    default: 1
  },
  fileUrls: {
    type: String
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
/**
 * 获取上传路径
 */
const getActionUrl = computed(() => {
  console.log('🚀 ~ file: FileUpload.vue:44 ~ getActionUrl ~ props.action:',  conifg.baseUrl )
  return conifg.baseUrl + '/' + props.action
})
/**
 * 上传成功
 * @param {*} response
 * @param {*} uploadFile
 * @param {*} uploadFiles
 */
const successHandle = (response, uploadFile, uploadFiles) => {
  if (response.code == 200) {
    uploadFiles[uploadFiles.length - 1]['url'] = '/upload/' + uploadFile.response.data
    setFileList(uploadFiles)
    emit('change', fileUrlList.value.join(','))
  } else {
    ElMessage.error(response.msg)
    // 如果上传失败，同样移除该文件
    const updatedFileList = uploadFiles.filter(f => f.uid !== uploadFile.uid)
    setFileList(updatedFileList)
    emit('change', fileUrlList.value.join(','))
  }
}
/**
 * 上传失败
 */
const errorHandle = () => {
  ElMessage.error('上传失败!')
}
/**
 * 图片移除
 * @param {*} file
 * @param {*} fileList
 */
const removeHandle = (file, fileList) => {
  setFileList(fileList)
  emit('change', fileUrlList.value.join(','))
}

/**
 * 上传文件超出限制
 */
const exceedHandle = () => {
  ElMessage.warning(`最多上传${props.limit}张图片`)
}

/**
 * 上传之前
 */
const beforeUploadHandle = (file) => {}

/**
 * 赋值
 * @param {*} uploadFiles
 */
const setFileList = (uploadFiles) => {
  var fileArray = []
  var fileUrlArray = []
  uploadFiles.forEach((item) => {
    var url = item.url
    var name = item.name
    var file = {
      name: name,
      url: url.includes('http') ? url : conifg.baseUrl + url
    }
    fileArray.push(file)
    fileUrlArray.push(url)

  })
  fileList.value = fileArray
  fileUrlList.value = fileUrlArray
}

/**
 * 初始化
 */
const init = () => {
// 重置上传文件列表和文件 URL 列表
  fileList.value = []
  fileUrlList.value = []
  if (props.fileUrls) {
    fileUrlList.value = props.fileUrls.split(',')
    let fileArray = []
    fileUrlList.value.forEach((item, index) => {
      var url = item
      var name = index
      var file = {
        name: name,
        url: url
      }
      fileArray.push(file)
    })
    setFileList(fileArray)
  }
}

/**
 * 图片预览
 * @param {*} uploadFile
 */
const pictureCardPreviewHandle = (uploadFile) => {
  dialogVisible.value = true
  dialogImageUrl.value = uploadFile.url
  console.log('🚀 ~ file: FileUpload.vue:127 ~ pictureCardPreviewHandle ~ uploadFile:', uploadFile)
}

watch(
    () => props.fileUrls,
    () => {
      init()
    },
    {
      immediate: true
    }
)

onMounted(() => {
  init()
})
</script>
<template>
  <div>
    <el-upload
        class="avatar-uploader"
        :action="getActionUrl"
        :multiple="multiple"
        ref="uploadRef"
        :limit="limit"
        :on-success="successHandle"
        :auto-upload="true"
        :on-remove="removeHandle"
        :on-exceed="exceedHandle"
        :on-error="errorHandle"
        :before-upload="beforeUploadHandle"
        :on-preview="pictureCardPreviewHandle"
        list-type="picture-card"
        :file-list="fileList"
        :disabled="disabled"
    >
      <el-icon class="avatar-uploader-icon" v-if="!disabled"><Plus /></el-icon>
    </el-upload>
    <el-dialog v-model="dialogVisible">
      <img style="width: 100%" :src="dialogImageUrl" :alt="imgAlt" />
    </el-dialog>
  </div>
</template>
<style scoped lang="scss">
.avatar-uploader {
  :deep() {
    .avatar {
      width: 178px;
      height: 178px;
      display: block;
    }
    .el-upload {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
    }
    .el-upload:hover {
      border-color: var(--el-color-primary);
    }
    .el-icon.avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 178px;
      height: 178px;
      text-align: center;
    }
  }
}
</style>
