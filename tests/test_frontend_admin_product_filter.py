import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PRODUCT_PAGE = ROOT / "frontend-admin" / "src" / "views" / "product" / "index.vue"


class FrontendAdminProductFilterTest(unittest.TestCase):
    def test_status_tab_change_uses_selected_tab_value_for_request(self):
        page = PRODUCT_PAGE.read_text(encoding="utf-8")

        self.assertIn('@tab-change="handleTabChange"', page)
        self.assertNotIn('@tab-click="handleTabClick"', page)
        self.assertIn("const loadData = async (status = activeTab.value) =>", page)
        self.assertIn("if (status !== 'all')", page)
        self.assertIn("params.status = status", page)
        self.assertIn("const handleTabChange = (tabName) =>", page)
        self.assertIn("activeTab.value = tabName", page)
        self.assertIn("loadData(tabName)", page)
        self.assertIn('@size-change="handlePageChange"', page)
        self.assertIn('@current-change="handlePageChange"', page)
        self.assertIn("const handlePageChange = () =>", page)
        self.assertIn("let latestProductListRequestId = 0", page)
        self.assertIn("const requestId = ++latestProductListRequestId", page)
        self.assertIn("if (requestId !== latestProductListRequestId) return", page)
        self.assertIn("if (requestId === latestProductListRequestId)", page)


if __name__ == "__main__":
    unittest.main()
