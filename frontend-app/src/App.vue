<script>
export default {
  onLaunch: function () {},
  onShow: function () {},
  onHide: function () {},
}
</script>

<style lang="scss">
/*每个页面公共css */
@import "uview-plus/index.scss";

:root {
  --primary: #00B578;
  --primary-light: #00D68F;
  --accent: #FF6633;
  --bg: #F7F8FA;
  --card-bg: #FFFFFF;
  --text-primary: #1A1A1A;
  --text-secondary: #666666;
  --text-hint: #999999;
  --border: #EBEDF0;
  --shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

page {
  background-color: #F7F8FA;
}

.card {
  background-color: var(--card-bg);
  border-radius: 20rpx;
  box-shadow: var(--shadow);
}
</style>
