<template>
  <div id="crawlerCreator" class="page-container">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="header">
      <div class="header-left">
        <img class="header-logo" src="@/assets/crawler/logo.png" alt="logo" />
        <h1>校园新媒体线上资源智能监控</h1>
      </div>
      <div class="header-right">
        <span class="monitor-tag">监控平台</span>
        <span class="header-info">数据更新时间：<em>{{ updateTime }}</em></span>
        <span class="header-info">覆盖地市：11个 | 覆盖高校：{{ indicators.schoolCnt || 35 }}所</span>
      </div>
    </div>

    <!-- ===== 筛选条件区域 ===== -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-group">
          <label>地市：</label>
          <el-select v-model="qryForm.cityCode" :clearable="clearableCity"
            placeholder="请选择地市" size="small" @change="onFilterChange">
            <el-option
              v-for="item in areaList"
              :key="item.city_code"
              :label="item.city_name"
              :value="item.city_code"
            ></el-option>
          </el-select>
        </div>
        <div class="filter-group">
          <label>平台：</label>
          <el-select v-model="qryForm.platform" clearable placeholder="请选择平台" size="small" @change="onFilterChange">
            <el-option v-for="item in platformList" :key="item.value"
              :label="item.label" :value="item.value"></el-option>
          </el-select>
        </div>
        <div class="filter-group">
          <label>日期：</label>
          <el-date-picker
            v-model="qryForm.statDateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            size="small"
            @change="onFilterChange">
          </el-date-picker>
        </div>
        <div class="filter-btns">
          <el-button type="danger" size="small" icon="el-icon-search" @click="onFilterChange">查询</el-button>
          <el-button size="small" @click="resetFilter">重置</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 数据异常预警条 ===== -->
    <div class="warning-bar" :class="{'warning-empty': warningList.length === 0}">
      <div class="warning-icon-wrap">
        <img src="@/assets/crawler/warn.png" alt="预警" />
      </div>
      <span class="warning-label">数据异常预警</span>
      <div class="warning-scroll">
        <div class="warning-items" v-if="warningList.length > 0">
          <template v-for="(item, idx) in warningList.concat(warningList)">
            <span class="warning-item" :key="idx"
              @click="showWarningDetail(item)">
              {{ item.warningName || '预警' }}：{{ item.summary || item.warningDesc || '' }}
            </span>
          </template>
        </div>
        <span class="warning-empty-text" v-else>暂无预警信息</span>
      </div>
      <span class="warning-count" v-if="warningList.length > 0">{{ warningList.length }}条预警</span>
    </div>

    <!-- ===== 核心指标卡片区 ===== -->
    <div class="core-cards" v-loading="indicator_loading">
      <!-- 触点规模 -->
      <div class="core-card">
        <div class="core-card-header">
          <span class="core-card-icon icon-contact"></span>
          <span class="core-card-title">触点规模</span>
        </div>
        <div class="core-card-body">
          <div class="core-card-metric">
            <span class="metric-value">{{ indicators.contactTotal || 0 }}</span>
            <span class="metric-label">运营触点数</span>
          </div>
          <div class="core-card-divider"></div>
          <div class="core-card-metric">
            <span class="metric-value">{{ indicators.validContact || 0 }}</span>
            <span class="metric-label">有效触点数</span>
          </div>
        </div>
      </div>
      <!-- 内容产能 -->
      <div class="core-card">
        <div class="core-card-header">
          <span class="core-card-icon icon-content"></span>
          <span class="core-card-title">内容产能</span>
        </div>
        <div class="core-card-body single-col">
          <div class="core-card-metric">
            <span class="metric-value large">{{ formatNumber(indicators.contentOutput) }}</span>
            <span class="metric-label">累计发布作品数</span>
            <span class="metric-sub">抖音{{ indicators.dyWorks || 0 }} | 小红书{{ indicators.xhsWorks || 0 }}</span>
          </div>
        </div>
      </div>
      <!-- 用户资产 -->
      <div class="core-card">
        <div class="core-card-header">
          <span class="core-card-icon icon-user"></span>
          <span class="core-card-title">用户资产</span>
        </div>
        <div class="core-card-body single-col">
          <div class="core-card-metric">
            <span class="metric-value large">{{ formatNumber(indicators.userAsset) }}</span>
            <span class="metric-label">累计粉丝量</span>
          </div>
        </div>
      </div>
      <!-- 传播热度 -->
      <div class="core-card">
        <div class="core-card-header">
          <span class="core-card-icon icon-heat"></span>
          <span class="core-card-title">传播热度</span>
        </div>
        <div class="core-card-body single-col">
          <div class="core-card-metric">
            <span class="metric-value large">{{ formatNumber(indicators.spreadHeat) }}</span>
            <span class="metric-label">互动总量</span>
          </div>
        </div>
      </div>
      <!-- 转化成效 -->
      <div class="core-card">
        <div class="core-card-header">
          <span class="core-card-icon icon-convert"></span>
          <span class="core-card-title">转化成效</span>
        </div>
        <div class="core-card-body">
          <div class="core-card-metric">
            <span class="metric-value">{{ formatNum(indicators.drainage) }}</span>
            <span class="metric-label">下单数</span>
          </div>
          <div class="core-card-divider"></div>
          <div class="core-card-metric">
            <span class="metric-value">{{ formatNum(indicators.activation) }}</span>
            <span class="metric-label">激活数</span>
          </div>
          <div class="core-card-divider"></div>
          <div class="core-card-metric">
            <span class="metric-value pct">{{ indicators.conversionRate || 0 }}<small>%</small></span>
            <span class="metric-label">转化率</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 三栏主内容区域 ===== -->
    <div class="main-content">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 平台数据对比分析 -->
        <div class="panel-card">
          <div class="card-title">平台数据对比分析</div>
          <div class="chart-box" ref="platformChart"></div>
        </div>
        <!-- 时间趋势走势分析 -->
        <div class="panel-card">
          <div class="card-title">时间趋势走势分析</div>
          <div class="time-filter">
            <el-radio-group v-model="trendType" size="mini" @change="loadTrendData">
              <el-radio-button label="7d">近7天</el-radio-button>
              <el-radio-button label="30d">近30天</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="semester">本学期</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-box" ref="trendChart"></div>
        </div>
      </div>

      <!-- 中间面板 - 全省地市运营详情 -->
      <div class="center-panel">
        <div class="panel-card full">
          <div class="card-title">
            {{ isByUnit ? getCityName(qryForm.cityCode) + '运营详情' : '全省地市运营详情' }}
          </div>
          <div class="city-table-wrap" v-loading="cityDetail_loading">
            <el-table :data="cityDetailList" stripe size="small" style="width:100%"
              @row-click="showGridDetail" :row-style="{cursor:'pointer'}">
              <el-table-column prop="dimName" label="地市" min-width="100">
                <template slot-scope="scope">
                  {{ scope.row.dimName || scope.row.cityName || '' }}
                </template>
              </el-table-column>
              <el-table-column prop="contactTotal" label="运营触点数" min-width="100" align="center"></el-table-column>
              <el-table-column prop="validContact" label="有效触点数" min-width="90" align="center"></el-table-column>
              <el-table-column prop="worksTotal" label="发布作品数" min-width="100" align="center"></el-table-column>
              <el-table-column prop="drainageTotal" label="下单数" min-width="80" align="center"></el-table-column>
              <el-table-column prop="activationTotal" label="激活数" min-width="80" align="center"></el-table-column>
              <el-table-column prop="conversionRate" label="转化率(%)" min-width="100" align="center">
                <template slot-scope="scope">{{ scope.row.conversionRate }}%</template>
              </el-table-column>
            </el-table>
          </div>
          <div class="table-hint">点击地市行可查看网格 / 营服级详情</div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 地市运营排行榜 -->
        <div class="panel-card">
          <div class="card-title">地市运营排行榜</div>
          <div class="rank-tabs">
            <div class="tab-row">
              <span class="tab-item" :class="{active: rankTab === 'drainage'}" @click="rankTab='drainage';loadRankData()">下单数</span>
              <span class="tab-item" :class="{active: rankTab === 'activation'}" @click="rankTab='activation';loadRankData()">激活数</span>
              <span class="tab-item" :class="{active: rankTab === 'conversion'}" @click="rankTab='conversion';loadRankData()">转化率</span>
            </div>
          </div>
          <div class="rank-list">
            <div class="rank-item" v-for="(item, idx) in rankDataList.slice(0, 10)" :key="idx" :class="{top: idx < 3}">
              <span class="rank-num" :class="'medal-'+(idx+1)">{{ idx + 1 }}</span>
              <span class="rank-name">{{ item.cityName || item.dimName || '' }}</span>
              <span class="rank-value">
                {{ rankTab === 'conversion' ? (item.conversionRate || 0) + '%' :
                   rankTab === 'activation' ? formatNum(item.activationTotal) :
                   formatNum(item.drainageTotal) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 青创社触点明细监控 -->
        <div class="panel-card">
          <div class="card-title">青创社触点明细监控</div>
          <div class="account-table-wrap">
            <el-table :data="accountMonitorTop5" size="small" style="width:100%"
              @row-click="showContactReport" :row-style="{cursor:'pointer'}">
              <el-table-column prop="accountName" label="触点昵称" min-width="80" show-overflow-tooltip></el-table-column>
              <el-table-column prop="cityName" label="所属地市" min-width="70"></el-table-column>
              <el-table-column prop="worksCnt" label="作品数" min-width="60" align="center"></el-table-column>
              <el-table-column prop="interactionCnt" label="互动量" min-width="60" align="center"></el-table-column>
              <el-table-column label="状态" min-width="65">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.statusName === 'active' ? 'success' : scope.row.statusName === 'low_interaction' ? 'warning' : 'danger'" size="mini" effect="plain">
                    {{ scope.row.statusName === 'active' ? '正常' : scope.row.statusName === 'low_interaction' ? '低效' : '休眠' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="account-footer">
            <span class="more-link" @click="showAccountMonitorFull">查看更多 →</span>
            <span class="table-hint-small">点击行查看统计报表</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 底部 - 数据排行榜 + 智能分析 ===== -->
    <div class="bottom-content">
      <!-- 数据排行榜 -->
      <div class="panel-card">
        <div class="card-title">数据排行榜</div>
        <div class="ranking-dimension-tabs">
          <el-radio-group v-model="rankingDimTab" size="small" @change="onRankingDimChange">
            <el-radio-button label="city">地市维度</el-radio-button>
            <el-radio-button label="personal">个人维度</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 地市维度 -->
        <div v-show="rankingDimTab === 'city'">
          <div class="ranking-container">
            <div class="ranking-item-col">
              <div class="ranking-col-title">作品量 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in cityWorksTop10" :key="idx" @click="rankingCityDrill(item)">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.dimName }}</span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.worksTotal || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
            <div class="ranking-item-col">
              <div class="ranking-col-title">点赞收藏量 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in cityInteractionTop10" :key="idx" @click="rankingCityDrill(item)">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.dimName }}</span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.interactionTotal || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
            <div class="ranking-item-col">
              <div class="ranking-col-title">粉丝榜 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in cityFansTop10" :key="idx" @click="rankingCityDrill(item)">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.dimName }}</span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.fansTotal || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          <!-- 下钻区域 -->
          <div v-if="rankingDrillList.length > 0" class="drilldown-section">
            <div class="drilldown-header">
              <el-button size="mini" icon="el-icon-back" @click="rankingBack" class="back-btn">返回</el-button>
              <span class="drilldown-title">{{ drillTitle }}</span>
            </div>
            <div class="drilldown-content">
              <div class="drilldown-item">
                <div class="drilldown-item-title">作品列表</div>
                <ul class="drilldown-list">
                  <li v-for="(item, idx) in rankingDrillList" :key="idx" @click="rankingDeepDrill(item)" style="cursor:pointer;">
                    <span class="ranking-name">{{ item.dimName }}</span>
                    <span class="ranking-value">{{ item.worksValue || item.rankValue || 0 }}</span>
                  </li>
                </ul>
              </div>
              <div class="drilldown-item">
                <div class="drilldown-item-title">互动量</div>
                <ul class="drilldown-list">
                  <li v-for="(item, idx) in rankingDrillList" :key="idx">
                    <span class="ranking-name">{{ item.dimName }}</span>
                    <span class="ranking-value">{{ formatNum(item.interactionValue || 0) }}</span>
                  </li>
                </ul>
              </div>
              <div class="drilldown-item">
                <div class="drilldown-item-title">粉丝榜</div>
                <ul class="drilldown-list">
                  <li v-for="(item, idx) in rankingDrillList" :key="idx">
                    <span class="ranking-name">{{ item.dimName }}</span>
                    <span class="ranking-value">{{ formatNum(item.fansValue || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 个人维度 -->
        <div v-show="rankingDimTab === 'personal'">
          <div class="ranking-container">
            <div class="ranking-item-col">
              <div class="ranking-col-title">作品量 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in personalWorksTop10" :key="idx">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.accountName }} <small>({{ item.cityName }})</small></span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.worksCnt || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
            <div class="ranking-item-col">
              <div class="ranking-col-title">点赞收藏量 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in personalInteractionTop10" :key="idx">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.accountName }} <small>({{ item.cityName }})</small></span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.interactionCnt || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
            <div class="ranking-item-col">
              <div class="ranking-col-title">粉丝榜 TOP10</div>
              <div class="ranking-list-scroll">
                <ul class="ranking-ul">
                  <li v-for="(item, idx) in personalFansTop10" :key="idx">
                    <span class="rank-left">
                      <span class="rank-badge" :class="'top-'+(idx+1)">{{ idx+1 }}</span>
                      <span class="rank-text">{{ item.accountName }} <small>({{ item.cityName }})</small></span>
                    </span>
                    <span class="rank-val">{{ formatNum(item.fansAdd || 0) }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部三列信息卡片 -->
      <div class="bottom-info-cards">
        <div class="info-card">
          <div class="info-card-title">营销成效</div>
          <div class="info-card-content">
            <p>
              <strong>优质平台：</strong>
              <span v-for="(p, i) in smartAnalysis.platformDist" :key="i">
                {{ getPlatformName(p.platform) }}占比{{ p.pct }}%{{ i < (smartAnalysis.platformDist || []).length - 1 ? ' | ' : '' }}
              </span>
              <br />
              <strong>高效地市：</strong>
              <span v-for="(c, i) in (smartAnalysis.topCities || [])" :key="i">
                {{ c.cityName }}({{ c.drainageTotal }}单){{ i < (smartAnalysis.topCities || []).length - 1 ? ' | ' : '' }}
              </span>
              <br />
              <strong>活跃社员：</strong>{{ (smartAnalysis.activeMember || {}).activeMemberCnt || 0 }}人
            </p>
          </div>
        </div>
        <div class="info-card">
          <div class="info-card-title">现存不足</div>
          <div class="info-card-content">
            <p>
              <span v-if="smartAnalysis.deficiency && smartAnalysis.deficiency.sleepPct > 0">
                · 休眠触点占比 {{ smartAnalysis.deficiency.sleepPct }}%（连续{{ smartAnalysis.deficiency.sleepThreshold }}天无新作品）<br />
              </span>
              · 部分地市覆盖率偏低<br />
              · 互动转化率待提升
            </p>
          </div>
        </div>
        <div class="info-card">
          <div class="info-card-title">改进方向</div>
          <div class="info-card-content">
            <p>
              · 推荐发布时段：12:00-14:00、18:00-22:00<br />
              · 优化内容方向：短视频+直播<br />
              · 推动薄弱地市帮扶计划<br />
              · 低效触点整改督导
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 弹窗 ===== -->
    <!-- 预警详情弹窗 -->
    <el-dialog :title="warningDialogTitle" :visible.sync="warningDialogVisible" width="70%">
      <el-table :data="warningDetailList" style="width: 100%;">
        <el-table-column v-if="warningDialogType === 'flow_drop'" prop="accountName" label="触点名称" width="150"></el-table-column>
        <el-table-column v-if="warningDialogType !== 'flow_drop'" prop="accountName" label="触点昵称" width="150"></el-table-column>
        <el-table-column prop="cityName" label="地市" width="100"></el-table-column>
        <el-table-column v-if="warningDialogType !== 'flow_drop'" prop="platform" label="平台" width="80"></el-table-column>
        <el-table-column v-if="warningDialogType === 'flow_drop'" prop="todayCnt" label="今日下单" width="100" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'flow_drop'" prop="yesterdayCnt" label="昨日下单" width="100" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'flow_drop'" prop="dropPct" label="下降比例(%)" width="110" align="right">
          <template slot-scope="scope">{{ scope.row.dropPct }}%</template>
        </el-table-column>
        <el-table-column v-if="warningDialogType === 'low_efficiency'" prop="interactionVal" label="互动量" width="100" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'low_efficiency'" prop="thresholdVal" label="阈值" width="80" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'low_efficiency'" prop="belowPct" label="低于阈值比例(%)" width="130" align="right">
          <template slot-scope="scope">{{ scope.row.belowPct }}%</template>
        </el-table-column>
        <el-table-column v-if="warningDialogType === 'low_efficiency'" prop="statusName" label="状态" width="100"></el-table-column>
        <el-table-column v-if="warningDialogType === 'sleep'" prop="sleepDays" label="休眠天数" width="100" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'sleep'" prop="lastPostDate" label="最后发布" width="120"></el-table-column>
        <el-table-column v-if="warningDialogType === 'fan_spike'" prop="curFans" label="当前粉丝" width="110" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'fan_spike'" prop="prevFans" label="昨日粉丝" width="110" align="right"></el-table-column>
        <el-table-column v-if="warningDialogType === 'fan_spike'" prop="fansGrowth" label="增长量" width="100" align="right"></el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="warningDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 网格运营详情弹窗 -->
    <el-dialog :title="gridDialogTitle" :visible.sync="gridDialogVisible" width="80%">
      <el-table :data="gridDetailList" style="width: 100%;" v-loading="gridDetail_loading">
        <el-table-column prop="yfName" label="营服" width="150"></el-table-column>
        <el-table-column prop="schoolName" label="校园" width="150"></el-table-column>
        <el-table-column prop="contactTotal" label="运营触点总数" width="120" align="right"></el-table-column>
        <el-table-column prop="validContact" label="有效触点数" width="110" align="right"></el-table-column>
        <el-table-column prop="worksTotal" label="发布作品数" width="110" align="right"></el-table-column>
        <el-table-column prop="drainageTotal" label="下单数" width="100" align="right"></el-table-column>
        <el-table-column prop="activationTotal" label="激活数" width="100" align="right"></el-table-column>
        <el-table-column prop="conversionRate" label="转化率(%)" width="110" align="right">
          <template slot-scope="scope">{{ scope.row.conversionRate }}%</template>
        </el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="gridDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 触点统计报表弹窗 -->
    <el-dialog title="触点统计报表" :visible.sync="contactReportVisible" width="90%">
      <el-table :data="contactReportList" style="width: 100%;" v-loading="contactReport_loading"
        @row-click="revealRow">
        <el-table-column prop="cityCode" label="地市" width="120">
          <template slot-scope="scope">{{ getCityName(scope.row.cityCode) }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="phoneNumber" label="手机号" width="140"></el-table-column>
        <el-table-column prop="platform" label="平台" width="80">
          <template slot-scope="scope">{{ getPlatformName(scope.row.platform) }}</template>
        </el-table-column>
        <el-table-column prop="accountName" label="昵称" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="proprieterName" label="社长姓名" width="100"></el-table-column>
        <el-table-column prop="proprieterPhone" label="社长手机号" width="140"></el-table-column>
        <el-table-column prop="vidoCnt" label="累计作品数" width="110" align="right"></el-table-column>
        <el-table-column prop="yinliuAllCnt" label="总下单数" width="100" align="right"></el-table-column>
        <el-table-column prop="devAllCnt" label="总激活数" width="100" align="right"></el-table-column>
      </el-table>
      <span slot="footer">
        <el-button @click="contactReportVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 触点明细监控 查看更多 弹窗 -->
    <el-dialog title="青创社触点明细监控" :visible.sync="accountMonitorFullVisible" width="90%">
      <el-table :data="accountMonitorPaged" stripe style="width: 100%; font-size: 13px;"
        @row-click="showContactReport" :row-style="{cursor: 'pointer'}">
        <el-table-column prop="accountName" label="触点昵称" min-width="140" show-overflow-tooltip></el-table-column>
        <el-table-column prop="cityName" label="地市" min-width="90"></el-table-column>
        <el-table-column prop="worksCnt" label="作品数" min-width="80" align="right"></el-table-column>
        <el-table-column prop="interactionCnt" label="互动量" min-width="80" align="right"></el-table-column>
        <el-table-column prop="yinliuAddCnt" label="下单数" min-width="80" align="right"></el-table-column>
        <el-table-column prop="devAddCnt" label="激活数" min-width="80" align="right"></el-table-column>
        <el-table-column prop="followsAdd" label="关注数" min-width="80" align="right"></el-table-column>
        <el-table-column prop="fansAdd" label="粉丝数" min-width="80" align="right"></el-table-column>
        <el-table-column prop="statusName" label="状态" min-width="70">
          <template slot-scope="scope">
            <el-tag :type="scope.row.statusName === 'active' ? 'success' : 'warning'" size="mini">
              {{ scope.row.statusName === 'active' ? '正常' : scope.row.statusName === 'low_interaction' ? '低效' : '休眠' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 15px; text-align: right;">
        <el-pagination
          layout="total, prev, pager, next"
          :total="accountMonitorList.length"
          :page-size="accountMonitorPageSize"
          :current-page.sync="accountMonitorPage"
          @current-change="onAccountMonitorPageChange">
        </el-pagination>
      </div>
      <span slot="footer">
        <el-button @click="accountMonitorFullVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  queryLoginInfo,
  qryOperationIndicators,
  qryWarningList,
  qryWarningDetail,
  qryPlatformComparison,
  qryTrendData,
  qryCityOperationDetail,
  qryGridOperationDetail,
  qryRankData,
  qryAccountMonitor,
  qryContactReport,
  qryRankingData,
  qryRankingDrill,
  qrySmartAnalysis
} from '@/api/Api'

export default {
  name: 'CrawlerCreator',
  data() {
    return {
      staffId: '',
      staffProvince: '',

      qryForm: {
        cityCode: '',
        platform: '',
        statDateRange: []
      },
      clearableCity: false,

      updateTime: '',
      updateTimer: null,

      areaList: [
        { city_code: '750', city_name: '南昌市' },
        { city_code: '740', city_name: '景德镇市' },
        { city_code: '751', city_name: '吉安市' },
        { city_code: '752', city_name: '赣州市' },
        { city_code: '753', city_name: '新余市' },
        { city_code: '754', city_name: '鹰潭市' },
        { city_code: '755', city_name: '九江市' },
        { city_code: '756', city_name: '宜春市' },
        { city_code: '757', city_name: '上饶市' },
        { city_code: '758', city_name: '萍乡市' },
        { city_code: '759', city_name: '抚州市' }
      ],
      platformList: [
        { value: 'dy', label: '抖音' },
        { value: 'xhs', label: '小红书' }
      ],

      indicators: { contactTotal: 0, validContact: 0, contentOutput: 0, userAsset: 0, spreadHeat: 0, drainage: 0, activation: 0, conversionRate: 0, dyWorks: 0, xhsWorks: 0, schoolCnt: 0 },
      indicator_loading: false,

      warningList: [],
      warningDialogVisible: false,
      warningDialogTitle: '',
      warningDialogType: '',
      warningDetailList: [],

      platformChart: null,
      trendChart: null,

      trendType: '7d',

      cityDetailList: [],
      cityDetail_loading: false,
      gridDialogVisible: false,
      gridDialogTitle: '',
      gridDetailList: [],
      gridDetail_loading: false,
      currentGridCity: '',
      currentGridUnit: '',

      rankTab: 'drainage',
      rankDataList: [],

      accountMonitorList: [],
      contactReportVisible: false,
      contactReportList: [],
      contactReport_loading: false,
      revealedRowIdx: -1,
      accountMonitorFullVisible: false,
      accountMonitorPage: 1,
      accountMonitorPageSize: 10,

      rankingDimTab: 'city',
      rankingCityList: [],
      rankingDrillList: [],
      rankingDrillLevel: '',
      rankingDrillParent: '',
      drillTitle: '',

      smartAnalysis: {}
    }
  },

  computed: {
    isByUnit() {
      return this.qryForm.cityCode !== ''
    },
    accountMonitorTop5() {
      return this.accountMonitorList.slice(0, 5)
    },
    accountMonitorPaged() {
      var start = (this.accountMonitorPage - 1) * this.accountMonitorPageSize
      return this.accountMonitorList.slice(start, start + this.accountMonitorPageSize)
    },
    personalWorksTop10() {
      return [...this.accountMonitorList].sort(function(a, b) {
        return (b.worksCnt || 0) - (a.worksCnt || 0)
      }).slice(0, 10)
    },
    personalInteractionTop10() {
      return [...this.accountMonitorList].sort(function(a, b) {
        return (b.interactionCnt || 0) - (a.interactionCnt || 0)
      }).slice(0, 10)
    },
    personalFansTop10() {
      return [...this.accountMonitorList].sort(function(a, b) {
        return (b.fansAdd || 0) - (a.fansAdd || 0)
      }).slice(0, 10)
    },
    cityWorksTop10() {
      return [...this.rankingCityList].sort(function(a, b) {
        return (b.worksTotal || 0) - (a.worksTotal || 0)
      }).slice(0, 10)
    },
    cityInteractionTop10() {
      return [...this.rankingCityList].sort(function(a, b) {
        return (b.interactionTotal || 0) - (a.interactionTotal || 0)
      }).slice(0, 10)
    },
    cityFansTop10() {
      return [...this.rankingCityList].sort(function(a, b) {
        return (b.fansTotal || 0) - (a.fansTotal || 0)
      }).slice(0, 10)
    }
  },

  mounted() {
    this.queryStaff()
    this.updateTimeNow()
    this.updateTimer = setInterval(this.updateTimeNow, 30000)
    window.addEventListener('resize', this.handleResize)
  },

  beforeDestroy() {
    if (this.updateTimer) {
      clearInterval(this.updateTimer)
    }
    window.removeEventListener('resize', this.handleResize)
    if (this.platformChart) {
      this.platformChart.dispose()
    }
    if (this.trendChart) {
      this.trendChart.dispose()
    }
  },

  methods: {
    updateTimeNow() {
      var now = new Date()
      var y = now.getFullYear()
      var m = ('0' + (now.getMonth() + 1)).slice(-2)
      var d = ('0' + now.getDate()).slice(-2)
      var h = ('0' + now.getHours()).slice(-2)
      var min = ('0' + now.getMinutes()).slice(-2)
      var s = ('0' + now.getSeconds()).slice(-2)
      this.updateTime = y + '/' + m + '/' + d + ' ' + h + ':' + min + ':' + s
    },

    handleResize() {
      if (this.platformChart) this.platformChart.resize()
      if (this.trendChart) this.trendChart.resize()
    },

    async queryStaff() {
      try {
        let res = await queryLoginInfo({ timestamp: Date.now() })
        let data = res.data
        if (data.code == '200') {
          let loginDataList = data.data
          let mainLoginData = loginDataList[0]
          this.staffId = mainLoginData.loginId || ''

          if (mainLoginData.cityCode) {
            this.areaList = this.areaList.filter(function(item) { return item.city_code == mainLoginData.cityCode })
            this.qryForm.cityCode = mainLoginData.cityCode
            this.clearableCity = false
          } else {
            this.staffProvince = mainLoginData.province || ''
            this.qryForm.cityCode = ''
            this.clearableCity = true
          }
        }
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
      this.refreshAll()
    },

    onFilterChange() {
      this.refreshAll()
    },

    resetFilter() {
      this.qryForm.cityCode = this.clearableCity ? '' : this.qryForm.cityCode
      this.qryForm.platform = ''
      this.qryForm.statDateRange = []
      this.refreshAll()
    },

    refreshAll() {
      this.loadIndicators()
      this.loadWarnings()
      this.loadPlatformComparison()
      this.loadTrendData()
      this.loadCityOperationDetail()
      this.loadRankData()
      this.loadAccountMonitor()
      this.loadRankingData()
      this.loadSmartAnalysis()
    },

    getQueryParams() {
      var params = {
        cityCode: this.qryForm.cityCode || '',
        platform: this.qryForm.platform || ''
      }
      if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
        params.statDateStart = this.qryForm.statDateRange[0]
        params.statDateEnd = this.qryForm.statDateRange[1]
      }
      return params
    },

    async loadIndicators() {
      this.indicator_loading = true
      try {
        var { data } = await qryOperationIndicators(this.getQueryParams())
        if (data.code == '0000' && data.data) {
          this.indicators = data.data
        }
      } catch (e) {
        console.error('加载运营指标失败', e)
      }
      this.indicator_loading = false
    },

    async loadWarnings() {
      try {
        var params = { cityCode: this.qryForm.cityCode || '' }
        var { data } = await qryWarningList(params)
        if (data.code == '0000') {
          this.warningList = data.rows || []
        }
      } catch (e) {
        console.error('加载预警失败', e)
      }
    },

    async showWarningDetail(item) {
      this.warningDialogType = item.warningType
      this.warningDialogTitle = item.warningName + '详情'
      this.warningDialogVisible = true
      try {
        var params = {
          warningType: item.warningType,
          cityCode: this.qryForm.cityCode || ''
        }
        var { data } = await qryWarningDetail(params)
        if (data.code == '0000') {
          this.warningDetailList = data.rows || []
        }
      } catch (e) {
        console.error('加载预警详情失败', e)
      }
    },

    async loadPlatformComparison() {
      try {
        var params = this.getQueryParams()
        var { data } = await qryPlatformComparison(params)
        if (data.code == '0000') {
          this.renderPlatformChart(data.rows || [])
        }
      } catch (e) {
        console.error('加载平台对比失败', e)
      }
    },

    renderPlatformChart(rows) {
      var dom = this.$refs.platformChart
      if (!dom) return
      if (!this.platformChart) {
        this.platformChart = echarts.init(dom)
      }
      var platforms = []
      var contacts = []
      var works = []
      var interactions = []
      rows.forEach(function(r) {
        platforms.push(r.platform === 'dy' ? '抖音' : r.platform === 'xhs' ? '小红书' : r.platform)
        contacts.push(r.contactTotal || 0)
        works.push(r.worksTotal || 0)
        interactions.push(r.interactionTotal || 0)
      })
      var option = {
        tooltip: { trigger: 'axis' },
        legend: {
          data: ['运营触点数', '作品发布量', '互动总量'],
          textStyle: { color: '#666' }
        },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: platforms,
          axisLabel: { color: '#666' },
          axisLine: { lineStyle: { color: '#ddd' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#999' },
          splitLine: { lineStyle: { color: '#f0f0f0' } }
        },
        series: [
          { name: '运营触点数', type: 'bar', data: contacts, itemStyle: { color: '#4a7cf7' }, barMaxWidth: 30 },
          { name: '作品发布量', type: 'bar', data: works, itemStyle: { color: '#6ea8fe' }, barMaxWidth: 30 },
          { name: '互动总量', type: 'bar', data: interactions, itemStyle: { color: '#9ec5fe' }, barMaxWidth: 30 }
        ]
      }
      this.platformChart.setOption(option)
    },

    async loadTrendData() {
      try {
        var params = this.getQueryParams()
        params.trendType = this.trendType
        var { data } = await qryTrendData(params)
        if (data.code == '0000') {
          this.renderTrendChart(data.rows || [])
        }
      } catch (e) {
        console.error('加载趋势失败', e)
      }
    },

    renderTrendChart(rows) {
      var dom = this.$refs.trendChart
      if (!dom) return
      if (!this.trendChart) {
        this.trendChart = echarts.init(dom)
      }
      var dates = []
      var fans = []
      var works = []
      var drainage = []
      rows.forEach(function(r) {
        dates.push(r.statDate || '')
        fans.push(r.fansTotal || 0)
        works.push(r.worksTotal || 0)
        drainage.push(r.drainageTotal || 0)
      })
      var option = {
        tooltip: { trigger: 'axis' },
        legend: {
          data: ['下单数', '作品发布量', '粉丝量'],
          textStyle: { color: '#666' }
        },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { rotate: 45, fontSize: 10, color: '#999' },
          axisLine: { lineStyle: { color: '#ddd' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#999' },
          splitLine: { lineStyle: { color: '#f0f0f0' } }
        },
        series: [
          { name: '下单数', type: 'line', data: drainage, smooth: true, itemStyle: { color: '#ff6b6b' }, lineStyle: { color: '#ff6b6b' } },
          { name: '作品发布量', type: 'line', data: works, smooth: true, itemStyle: { color: '#4a7cf7' }, lineStyle: { color: '#4a7cf7' } },
          { name: '粉丝量', type: 'line', data: fans, smooth: true, itemStyle: { color: '#26c6da' }, lineStyle: { color: '#26c6da' } }
        ]
      }
      this.trendChart.setOption(option)
    },

    async loadCityOperationDetail() {
      this.cityDetail_loading = true
      try {
        var { data } = await qryCityOperationDetail(this.getQueryParams())
        if (data.code == '0000') {
          this.cityDetailList = data.rows || []
        }
      } catch (e) {
        console.error('加载地市运营详情失败', e)
      }
      this.cityDetail_loading = false
    },

    async showGridDetail(row) {
      this.currentGridCity = row.cityCode || this.qryForm.cityCode || ''
      this.currentGridUnit = row.unitCde || ''
      var name = this.isByUnit ? (row.unitName || '') : (row.cityName || '')
      this.gridDialogTitle = name + ' - 网格运营详情'
      this.gridDialogVisible = true
      this.gridDetail_loading = true
      try {
        var params = {
          cityCode: this.currentGridCity,
          unitCde: this.currentGridUnit,
          platform: this.qryForm.platform || ''
        }
        if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
          params.statDateStart = this.qryForm.statDateRange[0]
          params.statDateEnd = this.qryForm.statDateRange[1]
        }
        var { data } = await qryGridOperationDetail(params)
        if (data.code == '0000') {
          this.gridDetailList = data.rows || []
        }
      } catch (e) {
        console.error('加载网格详情失败', e)
      }
      this.gridDetail_loading = false
    },

    async loadRankData() {
      try {
        var params = this.getQueryParams()
        var { data } = await qryRankData(params)
        if (data.code == '0000') {
          var list = data.rows || []
          if (this.rankTab === 'drainage') {
            list.sort(function(a, b) { return (b.drainageTotal || 0) - (a.drainageTotal || 0) })
          } else if (this.rankTab === 'activation') {
            list.sort(function(a, b) { return (b.activationTotal || 0) - (a.activationTotal || 0) })
          } else if (this.rankTab === 'conversion') {
            list.sort(function(a, b) { return (b.conversionRate || 0) - (a.conversionRate || 0) })
          }
          this.rankDataList = list
        }
      } catch (e) {
        console.error('加载排行榜失败', e)
      }
    },

    async loadAccountMonitor() {
      try {
        var params = {
          cityCode: this.qryForm.cityCode || '',
          platform: this.qryForm.platform || ''
        }
        if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
          params.statDateStart = this.qryForm.statDateRange[0]
          params.statDateEnd = this.qryForm.statDateRange[1]
        }
        var { data } = await qryAccountMonitor(params)
        if (data.code == '0000') {
          this.accountMonitorList = data.rows || []
        }
      } catch (e) {
        console.error('加载触点监控失败', e)
      }
    },

    showAccountMonitorFull() {
      this.accountMonitorPage = 1
      this.accountMonitorFullVisible = true
    },

    onAccountMonitorPageChange(page) {
      this.accountMonitorPage = page
    },

    async showContactReport(item) {
      this.contactReportVisible = true
      this.contactReport_loading = true
      this.revealedRowIdx = -1
      try {
        var params = { userId: item.userId || '' }
        if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
          params.statDateStart = this.qryForm.statDateRange[0]
          params.statDateEnd = this.qryForm.statDateRange[1]
        }
        var { data } = await qryContactReport(params)
        if (data.code == '0000') {
          this.contactReportList = data.rows || []
        }
      } catch (e) {
        console.error('加载触点统计报表失败', e)
      }
      this.contactReport_loading = false
    },

    revealRow(row) {
      var idx = this.contactReportList.indexOf(row)
      if (this.revealedRowIdx === idx) {
        this.revealedRowIdx = -1
      } else {
        this.revealedRowIdx = idx
      }
    },

    onRankingDimChange(val) {
      if (val === 'city') {
        this.loadRankingData()
      }
      this.rankingDrillList = []
    },

    async loadRankingData() {
      try {
        var params = this.getQueryParams()
        var { data } = await qryRankData(params)
        if (data.code == '0000') {
          this.rankingCityList = data.rows || []
        }
        this.rankingDrillList = []
      } catch (e) {
        console.error('加载数据排行榜失败', e)
      }
    },

    async rankingCityDrill(row) {
      var level = row.dimType === 'city' ? 'unit' : 'yf'
      var parentCode = row.dimCode
      this.rankingDrillLevel = level
      this.rankingDrillParent = parentCode
      this.drillTitle = row.dimName + ' -> 区县'
      try {
        var params = {
          level: level,
          parentCode: parentCode,
          platform: this.qryForm.platform || ''
        }
        if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
          params.statDateStart = this.qryForm.statDateRange[0]
          params.statDateEnd = this.qryForm.statDateRange[1]
        }
        var { data } = await qryRankingDrill(params)
        if (data.code == '0000') {
          this.rankingDrillList = data.rows || []
        }
      } catch (e) {
        console.error('排行榜下钻失败', e)
      }
    },

    async rankingDeepDrill(row) {
      if (this.rankingDrillLevel === 'unit') {
        try {
          var params = {
            level: 'yf',
            parentCode: row.dimCode,
            platform: this.qryForm.platform || ''
          }
          if (this.qryForm.statDateRange && this.qryForm.statDateRange.length === 2) {
            params.statDateStart = this.qryForm.statDateRange[0]
            params.statDateEnd = this.qryForm.statDateRange[1]
          }
          var { data } = await qryRankingDrill(params)
          if (data.code == '0000') {
            this.rankingDrillLevel = 'yf'
            this.rankingDrillParent = row.dimCode
            this.drillTitle = row.dimName + ' -> 营服'
            this.rankingDrillList = data.rows || []
          }
        } catch (e) {
          console.error('排行榜深度下钻失败', e)
        }
      }
    },

    rankingBack() {
      this.rankingDrillList = []
      this.rankingDrillLevel = ''
    },

    async loadSmartAnalysis() {
      try {
        var { data } = await qrySmartAnalysis(this.getQueryParams())
        if (data.code == '0000' && data.data) {
          this.smartAnalysis = data.data
        }
      } catch (e) {
        console.error('加载智能分析失败', e)
      }
    },

    getCityName(cityCode) {
      var city = this.areaList.find(function(item) { return item.city_code == cityCode })
      return city ? city.city_name : cityCode
    },

    getPlatformName(platform) {
      var p = this.platformList.find(function(item) { return item.value == platform })
      return p ? p.label : platform
    },

    formatNumber(num) {
      if (!num) return '0'
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万'
      }
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },

    formatNum(num) {
      if (!num && num !== 0) return '0'
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
  }
}
</script>

<style scoped>
/* ===== 全局 ===== */
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f2f5;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', sans-serif;
  color: #333;
  overflow-x: hidden;
}

/* ===== 顶部标题栏 ===== */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-logo {
  height: 36px;
  width: auto;
}
.header-left h1 {
  font-size: 19px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: 1px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 22px;
}
.monitor-tag {
  background: #4a7cf7;
  color: #fff;
  padding: 4px 16px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.header-info {
  font-size: 13px;
  color: #666;
}
.header-info em {
  font-style: normal;
  color: #333;
  font-weight: 600;
}

/* ===== 筛选条件区域 ===== */
.filter-bar {
  padding: 14px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-group label {
  font-size: 13px;
  color: #555;
  white-space: nowrap;
  font-weight: 500;
}
.filter-group .el-select,
.filter-group .el-date-picker {
  width: 160px;
}
.filter-btns {
  display: flex;
  gap: 10px;
}

/* ===== 数据异常预警条 ===== */
.warning-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 24px;
  background: #fff7e6;
  border-bottom: 1px solid #ffe7ba;
}
.warning-bar.warning-empty {
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}
.warning-icon-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.warning-icon-wrap img {
  width: 18px;
  height: 18px;
}
.warning-label {
  font-size: 13px;
  font-weight: 700;
  color: #d4380d;
  white-space: nowrap;
  flex-shrink: 0;
}
.warning-scroll {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: 22px;
  display: flex;
  align-items: center;
}
.warning-items {
  display: flex;
  gap: 56px;
  animation: scrollWarning 35s linear infinite;
  align-items: center;
  height: 100%;
  width: max-content;
}
.warning-scroll:hover .warning-items { animation-play-state: paused; }
@keyframes scrollWarning {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
.warning-item {
  white-space: nowrap;
  color: #d4380d;
  font-size: 12px;
  cursor: pointer;
  padding: 2px 8px;
  background: #fff1cc;
  border-radius: 3px;
  transition: all 0.2s;
}
.warning-item:hover {
  color: #a8071a;
  background: #ffe7ba;
}
.warning-empty-text {
  color: #999;
  font-size: 12px;
}
.warning-count {
  font-size: 12px;
  color: #d4380d;
  font-weight: 600;
  flex-shrink: 0;
}

/* ===== 核心指标卡片区 ===== */
.core-cards {
  display: flex;
  gap: 14px;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  overflow-x: auto;
}
.core-card {
  flex: 1;
  min-width: 170px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 14px 16px 12px;
  transition: box-shadow 0.2s;
}
.core-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.core-card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.core-card-icon {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.icon-contact { background: #4a7cf7; }
.icon-content { background: #6ea8fe; }
.icon-user { background: #26c6da; }
.icon-heat { background: #fa8c16; }
.icon-convert { background: #52c41a; }
.core-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.core-card-body {
  display: flex;
  align-items: center;
  justify-content: space-around;
}
.core-card-body.single-col {
  justify-content: center;
}
.core-card-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.metric-value {
  font-size: 22px;
  font-weight: 700;
  color: #1a56db;
  line-height: 1.3;
}
.metric-value.large {
  font-size: 26px;
}
.metric-value.pct {
  font-size: 24px;
}
.metric-value small {
  font-size: 14px;
  font-weight: 400;
}
.metric-label {
  font-size: 11px;
  color: #888;
  margin-top: 2px;
  white-space: nowrap;
}
.metric-sub {
  font-size: 10px;
  color: #aaa;
  margin-top: 3px;
  white-space: nowrap;
}
.core-card-divider {
  width: 1px;
  height: 34px;
  background: #e8e8e8;
  flex-shrink: 0;
}

/* ===== 主内容区域 ===== */
.main-content {
  display: flex;
  padding: 14px;
  gap: 14px;
  flex: 1;
}

/* 左侧面板 */
.left-panel {
  width: 330px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex-shrink: 0;
}

/* 中间面板 */
.center-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 右侧面板 */
.right-panel {
  width: 340px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex-shrink: 0;
}

/* ===== 卡片通用 ===== */
.panel-card {
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 14px;
}
.panel-card.full {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}
.card-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  background: #4a7cf7;
  border-radius: 2px;
  margin-right: 8px;
}

/* 图表 */
.chart-box {
  height: 200px;
  width: 100%;
}
.time-filter {
  margin-bottom: 6px;
}

/* 地市表格 */
.city-table-wrap {
  flex: 1;
  overflow: auto;
}
.table-hint {
  text-align: center;
  font-size: 11px;
  color: #999;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
.table-hint-small {
  font-size: 11px;
  color: #999;
}

/* ===== 右侧排行榜 ===== */
.rank-tabs {
  margin-bottom: 8px;
}
.tab-row {
  display: flex;
  background: #f5f6fa;
  border-radius: 5px;
  padding: 2px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 4px 0;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}
.tab-item.active {
  background: #4a7cf7;
  color: #fff;
  font-weight: 600;
}
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
  max-height: 340px;
  overflow-y: auto;
}
.rank-item {
  display: flex;
  align-items: center;
  padding: 5px 8px;
  background: #fafbfc;
  border-radius: 5px;
  font-size: 12px;
  transition: background 0.2s;
}
.rank-item:hover { background: #f0f5ff; }
.rank-item.top { background: #fffbe6; }
.rank-num {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8e8e8;
  border-radius: 50%;
  font-size: 10px;
  font-weight: 700;
  margin-right: 8px;
  color: #999;
  flex-shrink: 0;
}
.rank-num.medal-1 { background: #fa8c16; color: #fff; }
.rank-num.medal-2 { background: #8c8c8c; color: #fff; }
.rank-num.medal-3 { background: #d46b08; color: #fff; }
.rank-name { flex: 1; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-value { font-size: 12px; font-weight: 700; color: #1a56db; }

/* 触点监控表格 */
.account-table-wrap {
  max-height: 250px;
  overflow-y: auto;
}
.account-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
.more-link {
  font-size: 12px;
  color: #4a7cf7;
  cursor: pointer;
}
.more-link:hover { color: #1a56db; text-decoration: underline; }

/* ===== 底部区域 ===== */
.bottom-content {
  padding: 0 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 数据排行榜 */
.ranking-dimension-tabs {
  margin-bottom: 10px;
}
.ranking-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.ranking-item-col {
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
}
.ranking-col-title {
  font-size: 13px;
  font-weight: 700;
  color: #1a56db;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e8e8e8;
  text-align: center;
}
.ranking-list-scroll {
  max-height: 260px;
  overflow-y: auto;
}
.ranking-ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.ranking-ul li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f0f0;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.ranking-ul li:hover { background: #f0f5ff; border-radius: 3px; padding-left: 4px; padding-right: 4px; }
.ranking-ul li:last-child { border-bottom: none; }
.rank-left {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
}
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  min-width: 18px;
  background: #e8e8e8;
  color: #999;
  border-radius: 50%;
  font-weight: 700;
  font-size: 10px;
  margin-right: 6px;
}
.rank-badge.top-1 { background: #fa8c16; color: #fff; }
.rank-badge.top-2 { background: #8c8c8c; color: #fff; }
.rank-badge.top-3 { background: #d46b08; color: #fff; }
.rank-text {
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-text small {
  color: #999;
  font-size: 11px;
}
.rank-val {
  color: #1a56db;
  font-weight: 700;
  flex-shrink: 0;
  margin-left: 8px;
}

/* 下钻区域 */
.drilldown-section {
  margin-top: 14px;
  padding: 14px;
  background: #fafbfc;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
}
.drilldown-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}
.back-btn {
  font-size: 12px;
}
.drilldown-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a56db;
  margin: 0;
}
.drilldown-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.drilldown-item {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 10px;
}
.drilldown-item-title {
  font-size: 12px;
  font-weight: 700;
  color: #1a56db;
  margin-bottom: 6px;
  padding-bottom: 5px;
  border-bottom: 1px solid #f0f0f0;
}
.drilldown-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 160px;
  overflow-y: auto;
}
.drilldown-list li {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px dashed #f0f0f0;
  font-size: 12px;
  color: #333;
}
.drilldown-list li:last-child { border-bottom: none; }
.drilldown-list li:hover { background: #f0f5ff; }
.drilldown-list .ranking-name { font-size: 12px; }
.drilldown-list .ranking-value { font-size: 12px; }

/* ===== 底部三列信息卡片 ===== */
.bottom-info-cards {
  display: flex;
  gap: 14px;
}
.info-card {
  flex: 1;
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 14px 16px;
}
.info-card-title {
  font-size: 14px;
  font-weight: 700;
  color: #1a56db;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}
.info-card-content {
  font-size: 13px;
  color: #555;
  line-height: 1.8;
}
.info-card-content p {
  margin: 0;
}
.info-card-content strong {
  color: #333;
}

/* ===== Element UI 表格浅色覆盖 ===== */
.page-container >>> .el-table {
  font-size: 12px !important;
}
.page-container >>> .el-table th {
  background: #f5f7fa !important;
  color: #333 !important;
  font-weight: 700 !important;
  font-size: 12px !important;
  padding: 8px 0 !important;
  border-color: #e8e8e8 !important;
}
.page-container >>> .el-table td {
  font-size: 12px !important;
  padding: 6px 0 !important;
  border-color: #f0f0f0 !important;
  color: #333 !important;
}
.page-container >>> .el-table .el-table__row {
  cursor: pointer;
  transition: background 0.2s;
}
.page-container >>> .el-table .el-table__row:hover {
  background: #f0f5ff !important;
}
.page-container >>> .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fafbfc !important;
}
.page-container >>> .el-table__empty-text {
  color: #999 !important;
}

/* ===== Element UI 筛选组件微调 ===== */
.page-container >>> .el-select .el-input__inner {
  border-color: #d9d9d9;
}
.page-container >>> .el-date-editor .el-range-input {
  font-size: 12px;
}
.page-container >>> .el-date-editor .el-range-separator {
  font-size: 12px;
  color: #999;
}
.page-container >>> .el-radio-button__inner {
  font-size: 12px;
  padding: 6px 14px;
}

/* 滚动条 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: #f5f5f5; border-radius: 3px; }
::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }

</style>
