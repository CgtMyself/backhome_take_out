<template>
  <div class="multi-order-page">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <van-icon name="arrow-left" class="header-icon" @click="onBack" />
      <div class="header-title">处理流程</div>
      <van-icon name="cross" class="header-icon" @click="onClose" />
    </div>

    <!-- 查询条件区域 -->
    <div class="query-card">
      <div class="query-row">
        <van-field
          v-model="queryForm.serviceNumber"
          label="业务号码"
          clearable
          placeholder="请输入业务号码"
        />
      </div>
      <div class="query-row">
        <van-field
          v-model="queryForm.orderId"
          label="订单编号"
          clearable
          placeholder="请输入订单编号"
        >
          <template #right-icon>
            <van-icon name="friends-o" class="idcard-icon" @click="onReadIdCard" />
          </template>
        </van-field>
      </div>
      <div class="query-actions">
        <van-button type="info" block :loading="loading" loading-text="查询中..." @click="onSearch">查询</van-button>
      </div>
    </div>

    <!-- 查询结果区域 -->
    <div class="result-wrapper">
      <!-- 无数据提示 -->
      <div v-if="searched && !orderInfo && !loading" class="empty-tip">
        <van-empty description="未查询到对应订单信息" />
      </div>

      <!-- 订单结果展示 -->
      <div v-if="orderInfo" class="track-wrapper">
        <!-- 订单基本信息 -->
        <div class="order-info-card">
          <div class="info-row">
            <span class="info-label">业务号码</span>
            <span class="info-value">{{ orderInfo.serial_number || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ orderInfo.order_id || '-' }}</span>
          </div>
          <div v-if="orderInfo.name" class="info-row">
            <span class="info-label">客户名称</span>
            <span class="info-value">{{ orderInfo.name }}</span>
          </div>
          <div v-if="orderInfo.goods_names" class="info-row">
            <span class="info-label">商品名称</span>
            <span class="info-value">{{ orderInfo.goods_names }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">订单状态</span>
            <span class="info-value status" :class="!isInTransit ? 'status-done' : 'status-doing'">
              <span class="status-icon" :class="{ 'is-rotating': isInTransit }">{{ !isInTransit ? '✓' : '↻' }}</span>
              {{ orderInfo.status_name || '-' }}
            </span>
          </div>
        </div>

        <!-- 轨迹时间轴 -->
        <div class="timeline-wrapper">

          <!-- 节点0（顶部）：B域竣工 -->
          <div class="tl-item" :class="getNodeClass(0)">
            <div class="tl-left">
              <div class="tl-badge-wrap">
                <div class="tl-badge">B域</div>
                <div class="tl-status-icon" :class="'status-' + getNodeStatus(0)">
                  <span v-if="getNodeStatus(0) === 'done'" class="icon-glyph">✓</span>
                  <span v-else-if="getNodeStatus(0) === 'active'" class="icon-glyph is-rotating">↻</span>
                  <span v-else-if="getNodeStatus(0) === 'pending'" class="icon-glyph">−</span>
                </div>
              </div>
              <div class="tl-connector" :class="getConnectorClass(0)"></div>
            </div>
            <div class="tl-body">
              <template v-if="isNonTransit"></template>
              <template v-else-if="getNodeStatus(0) === 'done'">
                <div class="tl-node-title">已竣工</div>
                <div class="tl-node-time">{{ formatTime(orderInfo.finish_time) }}</div>
              </template>
              <template v-else-if="getNodeStatus(0) === 'active'">
                <div class="tl-node-title">{{ currentNodeName }}</div>
                <div class="tl-node-sub">{{ currentDealInfo }}</div>
              </template>
              <template v-else>
                <div class="tl-node-title tl-empty-title">待处理</div>
              </template>
            </div>
          </div>

          <!-- 节点1：M域 -->
          <div class="tl-item" :class="getNodeClass(1)">
            <div class="tl-left">
              <div class="tl-badge-wrap">
                <div class="tl-badge">M域</div>
                <div class="tl-status-icon" :class="'status-' + getNodeStatus(1)">
                  <span v-if="getNodeStatus(1) === 'done'" class="icon-glyph">✓</span>
                  <span v-else-if="getNodeStatus(1) === 'active'" class="icon-glyph is-rotating">↻</span>
                  <span v-else-if="getNodeStatus(1) === 'pending'" class="icon-glyph">−</span>
                </div>
              </div>
              <div class="tl-connector" :class="getConnectorClass(1)"></div>
            </div>
            <div class="tl-body">
              <template v-if="isNonTransit"></template>
              <template v-else-if="getNodeStatus(1) === 'done'">
                <div class="tl-node-title">已完成</div>
              </template>
              <template v-else-if="getNodeStatus(1) === 'active'">
                <div class="tl-node-title">{{ currentNodeName }}</div>
                <div class="tl-node-sub">{{ currentDealInfo }}</div>
              </template>
              <template v-else>
                <div class="tl-node-title tl-empty-title">待处理</div>
              </template>
            </div>
          </div>

          <!-- 节点2：O域 -->
          <div class="tl-item" :class="getNodeClass(2)">
            <div class="tl-left">
              <div class="tl-badge-wrap">
                <div class="tl-badge">O域</div>
                <div class="tl-status-icon" :class="'status-' + getNodeStatus(2)">
                  <span v-if="getNodeStatus(2) === 'done'" class="icon-glyph">✓</span>
                  <span v-else-if="getNodeStatus(2) === 'active'" class="icon-glyph is-rotating">↻</span>
                  <span v-else-if="getNodeStatus(2) === 'pending'" class="icon-glyph">−</span>
                </div>
              </div>
              <div class="tl-connector" :class="getConnectorClass(2)"></div>
            </div>
            <div class="tl-body">
              <template v-if="isNonTransit"></template>
              <template v-else-if="getNodeStatus(2) === 'done'">
                <div class="tl-node-title">已完成</div>
              </template>
              <template v-else-if="getNodeStatus(2) === 'active'">
                <div class="tl-node-title">{{ currentNodeName }}</div>
                <div class="tl-node-sub">{{ currentDealInfo }}</div>
              </template>
              <template v-else>
                <div class="tl-node-title tl-empty-title">待处理</div>
              </template>
            </div>
          </div>

          <!-- 节点3（底部）：B域受理，无连线 -->
          <div class="tl-item" :class="getNodeClass(3)">
            <div class="tl-left">
              <div class="tl-badge-wrap">
                <div class="tl-badge">B域</div>
                <div class="tl-status-icon" :class="'status-' + getNodeStatus(3)">
                  <span v-if="getNodeStatus(3) === 'done'" class="icon-glyph">✓</span>
                  <span v-else-if="getNodeStatus(3) === 'active'" class="icon-glyph is-rotating">↻</span>
                  <span v-else-if="getNodeStatus(3) === 'pending'" class="icon-glyph">−</span>
                </div>
              </div>
              <!-- 最后节点不加 tl-connector -->
            </div>
            <div class="tl-body">
              <div class="tl-node-title">订单已受理</div>
              <div class="tl-node-time">{{ formatTime(orderInfo.start_time) }}</div>
              <template v-if="isNonTransit">
                <div class="tl-node-title">{{ orderInfo.status_name || '订单取消' }}</div>
                <div class="tl-node-time">{{ formatTime(orderInfo.finish_time) }}</div>
              </template>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Toast } from 'vant'
import { mapMutations } from 'vuex'

export default {
  name: 'Main',
  data () {
    return {
      queryForm: {
        serviceNumber: '',
        orderId: ''
      },
      orderInfo: null,
      currentNode: null,
      searched: false,
      loading: false,
      loginMsg: null
    }
  },
  computed: {
    /** 已竣工状态 status=1 */
    isCompleted () {
      return this.orderInfo && String(this.orderInfo.status) === '1'
    },
    /** 在途状态 status in (0,5) */
    isInTransit () {
      if (!this.orderInfo) return false
      const status = String(this.orderInfo.status)
      return ['0', '5'].includes(status)
    },
    /** 非在途状态 status not in (0,1,5)，如订单取消 */
    isNonTransit () {
      return !!this.orderInfo && !this.isCompleted && !this.isInTransit
    },
    /** 当前节点名称 */
    currentNodeName () {
      return this.currentNode ? (this.currentNode.nodeName || '') : ''
    },
    /** 当前处理人（后端已脱敏） */
    currentDealMan () {
      return this.currentNode ? (this.currentNode.dealMan || '-') : '-'
    },
    /** 当前联系电话（后端已脱敏） */
    currentDealPhone () {
      return this.currentNode ? (this.currentNode.dealPhone || '-') : '-'
    },
    /** 当前处理人工号（后端已脱敏） */
    currentDealWorkNo () {
      return this.currentNode ? (this.currentNode.dealWorkNo || '-') : '-'
    },
    /** 当前处理人信息（单行格式） */
    currentDealInfo () {
      const man = this.currentDealMan || '-'
      const phone = this.currentDealPhone || '-'
      const workNo = this.currentDealWorkNo || '-'
      return `处理人:${man} | ${phone} | ${workNo}`
    },
    /**
     * 当前环节在时间轴中的位置索引（顶部为0，底部为3）
     * nodeType: B→0(竣工), M→1, O→2, B受理→3
     */
    currentNodeIndex () {
      if (!this.currentNode) return -1
      const type = this.currentNode.nodeType
      if (type === 'B') return 0
      if (type === 'M') return 1
      if (type === 'O') return 2
      return -1
    }
  },
  mounted () {
    this.initLoginMsg()
  },
  methods: {
    ...mapMutations(['updateLogInfo']),

    /** 返回上一页 */
    onBack () {
      if (this.$router) {
        this.$router.back()
      } else {
        window.history.back()
      }
    },

    /** 关闭页面 */
    onClose () {
      window.history.back()
    },

    /** 查询按钮点击 */
    onSearch () {
      const { serviceNumber, orderId } = this.queryForm
      if (!serviceNumber && !orderId) {
        Toast('业务号码和订单编号至少填写一个')
        return
      }
      this.doSearch()
    },

    /** 执行查询 */
    async doSearch () {
      this.loading = true
      this.searched = true
      this.orderInfo = null
      this.currentNode = null

      const params = {
        serviceNumber: this.queryForm.serviceNumber,
        orderId: this.queryForm.orderId
      }
      if (this.loginMsg && this.loginMsg.srcCity && this.loginMsg.srcCity !== '75') {
        params.cityCode = this.loginMsg.srcCity
      }

      try {
        const url = '/jxzqzt/api/multiOrderController/qryMultiOrderList'
        const resp = await this.$http.commonPostRequest(url, params)
        const respData = resp.data || {}

        if (respData.code === '0000') {
          this.orderInfo = respData.orderInfo || null
          this.currentNode = respData.currentNode || null
          if (!this.orderInfo) {
            Toast('暂无数据')
          }
        } else {
          Toast(respData.msg || '查询失败')
          this.orderInfo = null
          this.currentNode = null
        }
      } catch (e) {
        console.error('查询异常:', e)
        Toast('查询异常，请稍后重试')
        this.orderInfo = null
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取节点样式类
     * @param {number} index 节点索引（0=B域竣工，1=M域，2=O域，3=B域受理）
     */
    getNodeClass (index) {
      if (this.isCompleted) {
        return 'node-done'
      }

      if (this.isInTransit) {
        if (index === 3) return 'node-done'

        if (this.currentNodeIndex >= 0) {
          if (index === this.currentNodeIndex) return 'node-active'
          if (index > this.currentNodeIndex) return 'node-passed'
          return 'node-pending'
        }
        return 'node-pending'
      }

      // 非在途状态（如订单取消）：所有节点图标均显示为已完成
      return 'node-done'
    },

    /**
     * 获取节点状态关键字，用于渲染状态图标
     * @param {number} index 节点索引
     * @returns {string} done | active | pending | none
     */
    getNodeStatus (index) {
      const cls = this.getNodeClass(index)
      if (cls === 'node-done' || cls === 'node-passed') return 'done'
      if (cls === 'node-active') return 'active'
      if (cls === 'node-pending') return 'pending'
      return 'none'
    },

    /**
     * 获取连线样式类：若下方节点已完成则视为已流经（高亮）
     * @param {number} index 当前节点索引
     */
    getConnectorClass (index) {
      return this.getNodeStatus(index + 1) === 'done' ? 'connector-active' : 'connector-idle'
    },

    /** 判断节点是否显示勾选图标 */
    isNodeChecked (index) {
      const cls = this.getNodeClass(index)
      return cls === 'node-done' || cls === 'node-passed'
    },

    /** 格式化时间 */
    formatTime (time) {
      if (!time) return ''
      return time
    },

    /** 初始化登录信息 */
    async initLoginMsg (info) {
      const url = '/jxwslRds/jxwslCommonService/v1.0/initLoignInfo'
      let jkInfo = ''
      if (info) {
        jkInfo = info
      } else {
        jkInfo = this.getUrlParamVal('info')
      }
      if (!jkInfo) return
      if (jkInfo.slice(jkInfo.length - 1) === '/') {
        jkInfo = jkInfo.slice(0, jkInfo.length - 1)
      }
      jkInfo = decodeURIComponent(jkInfo)

      const params = {
        jkInfo: jkInfo,
        roleId: 'jk',
        opType: '',
        funCode: 'm236',
        getOrderId: 'Y'
      }

      try {
        const resp = await this.$http.commonPostRequest(url, params)
        const respData = resp.data
        const jkStaff = respData.data.jkStaff
        const jkTokenUserInfo = respData.data.jkTokenUserInfo

        if (respData.result === 'success' && Object.keys(jkStaff).length > 0) {
          if (this.isEmpty(jkTokenUserInfo)) {
            Toast('获取账号登录信息失败!')
            return
          }
          let oaInfo = jkTokenUserInfo.oaInfo
          if (this.isEmpty(oaInfo) || this.isEmpty(oaInfo.login)) {
            Toast('获取OA信息为空!')
            return
          }

          this.loginMsg = {}
          this.loginMsg.appLogin = oaInfo.login
          this.loginMsg.oaStaffId = oaInfo.login
          const city = jkTokenUserInfo.jzjkRelation.staffInfo[0].eparchy_code
          this.loginMsg.srcCity = city === '000' ? '75' : city
          this.loginMsg.city = city === '000' ? '75' : city
          this.loginMsg.contactPhone = oaInfo.phone
          this.loginMsg.operatorId = oaInfo.login
          this.loginMsg.operatorName = oaInfo.name

          await this.updateLogInfo({ logInfos: JSON.parse(JSON.stringify(this.loginMsg)) })
        } else {
          Toast('获取账号登录信息失败!')
        }
      } catch (e) {
        Toast('获取账号登录信息发生异常:' + e)
        console.error('initLoginMsg error:', e)
      }
    },

    getUrlParamVal (name, url) {
      let str = null
      if (typeof (url) === 'string' && url !== '') {
        const idx = url.indexOf('?')
        if (idx > -1) {
          str = url.substring(idx + 1)
        }
      } else {
        str = window.location.search.substr(1)
      }
      const reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
      const r = decodeURI(str).match(reg)
      if (r != null) return unescape(r[2])
      return null
    },

    /** 读取身份证（占位） */
    onReadIdCard () {
      Toast('身份证读取功能待接入')
    },

    isEmpty (param) {
      return param === '' || param === null || param === undefined ||
        param === 'undefined' || param === 'null'
    }
  }
}
</script>

<style scoped>
.multi-order-page {
  padding: 0 0.2rem 0.6rem;
  background: #f5f6f8;
  min-height: 100vh;
}

/* ===================== 顶部导航栏 ===================== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 0.88rem;
  margin: 0 -0.2rem;
  padding: 0 0.28rem;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  font-size: 0.36rem;
  font-weight: 600;
  color: #222;
}

.header-icon {
  font-size: 0.4rem;
  color: #333;
}

.query-card {
  background: #ffffff;
  border-radius: 0.16rem;
  padding: 0.2rem 0.2rem 0.3rem;
  margin-top: 0.2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.query-row {
  margin-bottom: 0.1rem;
}

.idcard-icon {
  font-size: 0.4rem;
  color: #9aa4b2;
}

.query-actions {
  margin-top: 0.2rem;
}

.result-wrapper {
  margin-top: 0.2rem;
}

.empty-tip {
  padding: 1rem 0;
}

/* 订单基本信息卡片 */
.order-info-card {
  background: #ffffff;
  border-radius: 0.16rem;
  padding: 0.2rem 0.24rem;
  margin-bottom: 0.2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  font-size: 0.28rem;
  line-height: 1.8;
}

.info-label {
  flex: 0 0 1.8rem;
  color: #888;
}

.info-value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.info-value.status {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.info-value.status-doing {
  color: #1989fa;
}

.info-value.status-done {
  color: #07c160;
}

.status-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 0.32rem;
  height: 0.32rem;
  margin-right: 0.08rem;
  border-radius: 50%;
  font-size: 0.22rem;
  color: #fff;
}

.status-doing .status-icon {
  background: #1989fa;
}

.status-done .status-icon {
  background: #07c160;
}

.is-rotating {
  animation: tl-spin 1.4s linear infinite;
}

@keyframes tl-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ===================== 时间轴容器 ===================== */
.timeline-wrapper {
  background: #ffffff;
  border-radius: 0.16rem;
  padding: 0.32rem 0.24rem 0.1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ===================== 单个节点行 ===================== */
.tl-item {
  display: flex;
  align-items: stretch;
  position: relative;
}

/* ---- 左列（域标签 + 图标 + 连线） ---- */
.tl-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 1.0rem;
  flex-shrink: 0;
}

/* 域标签包裹（用于定位状态图标） */
.tl-badge-wrap {
  position: relative;
  flex-shrink: 0;
}

/* 域标签 */
.tl-badge {
  width: 0.8rem;
  height: 0.8rem;
  line-height: 0.8rem;
  text-align: center;
  border-radius: 0.12rem;
  font-size: 0.26rem;
  font-weight: 500;
  color: #fff;
}

/* 状态图标（叠加在域标签右下角的小圆点） */
.tl-status-icon {
  position: absolute;
  right: -0.08rem;
  bottom: -0.08rem;
  width: 0.32rem;
  height: 0.32rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 0.04rem #fff;
}

.tl-status-icon .icon-glyph {
  font-size: 0.22rem;
  color: #fff;
  line-height: 1;
}

.tl-status-icon.status-done {
  background: #07c160;
}

.tl-status-icon.status-active {
  background: #faad14;
}

.tl-status-icon.status-pending {
  background: #ff8c69;
}

.tl-status-icon.status-none {
  background: #c8c9cc;
}

/* 竖线连接器（撑满到下一节点顶部，顶端带向上箭头） */
.tl-connector {
  position: relative;
  flex: 1;
  width: 0.04rem;
  min-height: 0.36rem;
  margin: 0.1rem 0;
  border-radius: 0.02rem;
}

.tl-connector::before {
  content: '';
  position: absolute;
  top: -0.02rem;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 0.09rem solid transparent;
  border-right: 0.09rem solid transparent;
  border-bottom: 0.12rem solid currentColor;
}

.tl-connector.connector-idle {
  background: #dcdee0;
  color: #dcdee0;
}

.tl-connector.connector-active {
  background: #4a5b7a;
  color: #4a5b7a;
}

/* ---- 右侧内容区 ---- */
.tl-body {
  flex: 1;
  padding: 0 0 0.36rem 0.2rem;
  min-height: 0.8rem;
  padding-top: 0.04rem;
}

.tl-node-title {
  font-size: 0.3rem;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

.tl-empty-title {
  color: #c0c4cc;
  font-weight: 400;
}

.tl-node-time {
  font-size: 0.24rem;
  color: #999;
  margin-top: 0.06rem;
  line-height: 1.5;
}

/* 处理人信息（单行） */
.tl-node-sub {
  margin-top: 0.08rem;
  font-size: 0.24rem;
  color: #999;
  line-height: 1.6;
  word-break: break-all;
}

/* ===================== 各状态配色 ===================== */

/* 已完成 / 已流经节点：实心蓝标签 */
.node-done .tl-badge,
.node-passed .tl-badge {
  background: #2a7bf0;
}
.node-done .tl-node-title,
.node-passed .tl-node-title {
  color: #333;
}

/* 当前进行中节点：实心蓝标签 */
.node-active .tl-badge {
  background: #2a7bf0;
}
.node-active .tl-node-title {
  color: #333;
}

/* 待处理节点（未到达）：浅蓝标签 */
.node-pending .tl-badge {
  background: #cfe2fb;
  color: #6fa1e0;
}
.node-pending .tl-node-title {
  color: #c0c4cc;
}

/* 不可见节点（非在途非竣工）：浅蓝标签 */
.node-none .tl-badge {
  background: #cfe2fb;
  color: #6fa1e0;
}
.node-none .tl-node-title {
  color: #c0c4cc;
}
</style>
