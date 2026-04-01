<template>
  <view class="profile-container">
    <!-- 用户信息头部 -->
    <view class="user-header">
      <image class="avatar" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"></image>
      <view class="user-info">
        <text class="nickname">计科小明</text>
        <view class="tags">
          <u-tag text="已实名" type="success" size="mini" plain plainFill></u-tag>
          <u-tag text="信用极好" type="primary" size="mini" plain plainFill style="margin-left: 8px;"></u-tag>
        </view>
      </view>
      <u-icon name="arrow-right" color="#999" size="18"></u-icon>
    </view>
    
    <!-- 订单状态区 -->
    <view class="order-section">
      <view class="section-title">
        <text>我的交易</text>
        <view class="more">
          <text>全部</text>
          <u-icon name="arrow-right" size="12"></u-icon>
        </view>
      </view>
      <view class="status-grid">
        <view class="status-item">
          <u-icon name="bag" size="28" color="#333"></u-icon>
          <text class="text">我发布的</text>
        </view>
        <view class="status-item">
          <u-icon name="shopping-cart" size="28" color="#333"></u-icon>
          <text class="text">我买到的</text>
        </view>
        <view class="status-item">
          <u-icon name="rmb-circle" size="28" color="#333"></u-icon>
          <text class="text">我卖出的</text>
        </view>
        <view class="status-item">
          <u-icon name="star" size="28" color="#333"></u-icon>
          <text class="text">我的收藏</text>
        </view>
      </view>
    </view>

    <!-- 服务列表区 -->
    <view class="service-list">
      <u-cell-group :border="false">
        <u-cell icon="map" title="地址管理" isLink></u-cell>
        <u-cell icon="server-man" title="联系客服" isLink></u-cell>
        <u-cell icon="setting" title="系统设置" isLink></u-cell>
      </u-cell-group>
    </view>
    
    <!-- 退出登录按钮 -->
    <view class="logout-btn">
      <u-button type="error" text="退出登录" plain shape="circle"></u-button>
    </view>
  </view>
</template>

<script setup>
</script>

<style lang="scss">
page {
  background-color: #F3F4F6;
}

.profile-container {
  padding: 15px;
}

.user-header {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 20px 15px;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  
  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    margin-right: 15px;
    background-color: #f5f5f5;
  }
  
  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .nickname {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin-bottom: 8px;
    }
    
    .tags {
      display: flex;
      align-items: center;
    }
  }
}

.order-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 15px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
    padding-bottom: 10px;
    border-bottom: 1px solid #f5f5f5;
    
    .more {
      display: flex;
      align-items: center;
      font-size: 12px;
      font-weight: normal;
      color: #999;
    }
  }
  
  .status-grid {
    display: flex;
    justify-content: space-around;
    padding: 5px 0;
    
    .status-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .text {
        font-size: 12px;
        color: #666;
        margin-top: 8px;
      }
    }
  }
}

.service-list {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.logout-btn {
  padding: 0 20px;
}
</style>
