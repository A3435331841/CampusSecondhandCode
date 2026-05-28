import json
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PAGES_JSON = ROOT / "frontend-app" / "src" / "pages.json"
HOME_PAGE = ROOT / "frontend-app" / "src" / "pages" / "index" / "index.vue"
USER_VISIBLE_FILES = [
    ROOT / "frontend-app" / "src" / "pages" / "login" / "login.vue",
    ROOT / "frontend-app" / "src" / "pages" / "location-picker" / "location-picker.vue",
    ROOT / "frontend-app" / "src" / "pages" / "publish" / "publish.vue",
    ROOT / "frontend-app" / "src" / "pages" / "my-buy" / "my-buy.vue",
    ROOT / "frontend-app" / "src" / "pages" / "my-sell" / "my-sell.vue",
]


class FrontendAppI18nTest(unittest.TestCase):
    def test_navigation_titles_and_tab_bar_are_chinese(self):
        config = json.loads(PAGES_JSON.read_text(encoding="utf-8"))

        expected_titles = {
            "pages/login/login": "微信登录",
            "pages/verify/verify": "学号认证",
            "pages/location-picker/location-picker": "选择面交地点",
            "pages/index/index": "校园二手",
            "pages/publish/publish": "发布商品",
            "pages/profile/profile": "我的",
            "pages/detail/detail": "商品详情",
            "pages/my-publish/my-publish": "我的发布",
            "pages/chat/chat": "聊天",
            "pages/my-buy/my-buy": "我买到的",
            "pages/my-sell/my-sell": "我卖出的",
        }
        actual_titles = {
            page["path"]: page["style"]["navigationBarTitleText"]
            for page in config["pages"]
        }
        self.assertEqual(expected_titles, actual_titles)

        self.assertEqual("校园二手", config["globalStyle"]["navigationBarTitleText"])
        self.assertEqual(
            ["首页", "发布", "我的"],
            [item["text"] for item in config["tabBar"]["list"]],
        )

    def test_home_page_has_no_english_fallback_labels(self):
        content = HOME_PAGE.read_text(encoding="utf-8")

        self.assertNotIn("Seller_", content)
        self.assertNotIn("'Other'", content)
        self.assertIn("卖家", content)
        self.assertIn("其他", content)

    def test_common_user_visible_messages_are_chinese(self):
        blocked_phrases = [
            "Missing WeChat login code",
            "Enter a keyword",
            "Location search failed",
            "Upload response parse failed",
            "No purchase orders yet",
            "No sold orders yet",
            "Completed",
            "Cancelled",
            "Unknown",
            "Confirm",
            "Order confirmed",
            "Great trade",
            "Unavailable product",
            "Qty x",
            "Submit review",
            "Great buyer",
        ]
        combined = "\n".join(path.read_text(encoding="utf-8") for path in USER_VISIBLE_FILES)

        for phrase in blocked_phrases:
            with self.subTest(phrase=phrase):
                self.assertNotIn(phrase, combined)


if __name__ == "__main__":
    unittest.main()
