import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ADMIN_SRC = ROOT / "frontend-admin" / "src"


class FrontendAdminI18nTest(unittest.TestCase):
    def test_admin_navigation_uses_chinese_labels(self):
        router = (ADMIN_SRC / "router" / "index.js").read_text(encoding="utf-8")
        layout = (ADMIN_SRC / "views" / "layout" / "index.vue").read_text(encoding="utf-8")

        for label in ["数据看板", "用户管理", "商品管理", "分类管理", "认证信息", "评论管理"]:
            with self.subTest(label=label):
                self.assertIn(label, router + layout)

        for phrase in ["Dashboard", "Users", "Products", "Categories", "Verification", "Comments", "Home", "Logout", "Campus Admin"]:
            with self.subTest(phrase=phrase):
                self.assertNotIn(phrase, router + layout)

    def test_admin_pages_do_not_show_core_english_labels(self):
        files = [
            ADMIN_SRC / "views" / "login" / "index.vue",
            ADMIN_SRC / "views" / "product" / "index.vue",
            ADMIN_SRC / "views" / "category" / "index.vue",
            ADMIN_SRC / "views" / "comment" / "index.vue",
            ADMIN_SRC / "views" / "verification" / "index.vue",
        ]
        combined = "\n".join(path.read_text(encoding="utf-8") for path in files)

        blocked_phrases = [
            "Campus Secondhand Admin",
            "All",
            "Pending",
            "On Sale",
            "Off Shelf",
            "Rejected",
            'label="Image"',
            'label="Title"',
            'label="Price"',
            'label="Category"',
            "Seller ID",
            'label="Status"',
            "Created At",
            'label="Actions"',
            "Approve",
            "Reject",
            "Force Off Shelf",
            "Confirm",
            "Operation completed",
            "Unknown",
            "New Category",
            "Edit Category",
            "Name is required",
            "Saved",
            "Status updated",
            "Product ID",
            "User ID",
            "Content",
            "Nickname",
            "Real Name",
            "Student No",
            "College",
            "Major",
            "Grade",
            "Verify Status",
            "UNVERIFIED",
        ]
        for phrase in blocked_phrases:
            with self.subTest(phrase=phrase):
                self.assertNotIn(phrase, combined)


if __name__ == "__main__":
    unittest.main()
