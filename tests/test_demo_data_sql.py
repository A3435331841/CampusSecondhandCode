import importlib.util
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA_SQL = ROOT / "backend" / "src" / "main" / "resources" / "data.sql"
DATABASE_SQL = ROOT / "backend" / "src" / "main" / "resources" / "database.sql"
GENERATOR = ROOT / "tools" / "generate_demo_data.py"


def load_generator():
    spec = importlib.util.spec_from_file_location("generate_demo_data", GENERATOR)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class DemoDataSqlTest(unittest.TestCase):
    def test_generated_data_sql_matches_committed_file(self):
        module = load_generator()
        expected = module.build_sql()
        actual = DATA_SQL.read_text(encoding="utf-8")
        self.assertEqual(expected, actual)

    def test_demo_data_contains_acceptance_baseline(self):
        module = load_generator()
        sql = DATA_SQL.read_text(encoding="utf-8")

        self.assertIn("INSERT INTO `sys_user`", sql)
        self.assertIn("'admin'", sql)
        self.assertIn("'user001'", sql)
        self.assertIn("'user002'", sql)
        self.assertIn("'user003'", sql)

        self.assertEqual(sql.count("INSERT INTO `biz_category`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_student_roster`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_product`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_order`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_chat_session`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_chat_message`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_favorite`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_product_comment`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_order_review`"), 1)
        self.assertEqual(sql.count("INSERT INTO `biz_user_behavior`"), 1)

        self.assertGreaterEqual(len(module.USERS), 300)
        self.assertGreaterEqual(len(module.PRODUCTS), 3000)
        self.assertGreaterEqual(len(module.ORDERS), 1000)
        self.assertGreaterEqual(len(module.CHAT_SESSIONS), 800)
        self.assertGreaterEqual(len(module.CHAT_MESSAGES), 2400)
        self.assertGreaterEqual(len(module.FAVORITES), 5000)
        self.assertGreaterEqual(len(module.COMMENTS), 2500)
        self.assertGreaterEqual(len(module.REVIEWS), 1000)
        self.assertGreaterEqual(len(module.BEHAVIORS), 10000)

        self.assertEqual({row[15] for row in module.PRODUCTS}, {0, 1, 2, 3})
        self.assertEqual({row[13] for row in module.ORDERS}, {0, 1, 2})
        self.assertEqual({row[3] for row in module.BEHAVIORS}, {"VIEW", "FAVORITE", "COMMENT", "ORDER"})
        for category_id, _, _, _ in module.CATEGORIES:
            with self.subTest(category_id=category_id):
                self.assertGreaterEqual(
                    sum(1 for product in module.PRODUCTS if product[2] == category_id),
                    300,
                )

        self.assertEqual(sql.count("('ORD20260526001'"), 1)
        self.assertEqual(sql.count("('ORD20260526002'"), 1)
        self.assertEqual(sql.count("('ORD20260526003'"), 1)
        self.assertIn("'1_2_3'", sql)
        self.assertIn("键盘还在吗？", sql)
        self.assertIn("机械键盘", sql)
        self.assertIn("图书馆北门", sql)

    def test_demo_data_uses_chinese_user_facing_copy(self):
        module = load_generator()

        for _, name, _, _ in module.CATEGORIES:
            with self.subTest(category=name):
                self.assertRegex(name, r"[\u4e00-\u9fff]")

        for product in module.PRODUCTS:
            title = product[3]
            description = product[4]
            pickup_place = product[8]
            pickup_address = product[9]
            with self.subTest(product=title):
                self.assertRegex(title, r"[\u4e00-\u9fff]")
                self.assertRegex(description, r"[\u4e00-\u9fff]")
                self.assertRegex(pickup_place, r"[\u4e00-\u9fff]")
                self.assertRegex(pickup_address, r"[\u4e00-\u9fff]")
                self.assertNotRegex(
                    f"{title}{description}{pickup_place}{pickup_address}",
                    r"[A-Za-z]",
                )

        for collection_name, content_index in (("CHAT_MESSAGES", 5), ("COMMENTS", 3), ("REVIEWS", 6)):
            for row in getattr(module, collection_name):
                with self.subTest(collection=collection_name, content=row[content_index]):
                    self.assertRegex(row[content_index], r"[\u4e00-\u9fff]")
                    self.assertNotRegex(row[content_index], r"[A-Za-z]")

    def test_generated_data_targets_existing_schema_tables(self):
        data_sql = DATA_SQL.read_text(encoding="utf-8")
        database_sql = DATABASE_SQL.read_text(encoding="utf-8")

        inserted_tables = set(re.findall(r"INSERT INTO `([^`]+)`", data_sql))
        created_tables = set(re.findall(r"CREATE TABLE IF NOT EXISTS `([^`]+)`", database_sql))

        self.assertTrue(inserted_tables)
        self.assertLessEqual(inserted_tables, created_tables)

        table_columns = {}
        for table, body in re.findall(r"CREATE TABLE IF NOT EXISTS `([^`]+)` \((.*?)\n\) ENGINE", database_sql, re.S):
            table_columns[table] = set(re.findall(r"^\s+`([^`]+)`", body, re.M))

        for table, columns_text in re.findall(r"INSERT INTO `([^`]+)` \(([^)]+)\)", data_sql):
            with self.subTest(table=table):
                inserted_columns = set(re.findall(r"`([^`]+)`", columns_text))
                self.assertLessEqual(inserted_columns, table_columns[table])

    def test_fixture_rows_have_expected_widths(self):
        module = load_generator()
        expected_widths = {
            "CATEGORIES": 4,
            "STUDENT_ROSTER": 8,
            "USERS": 23,
            "PRODUCTS": 17,
            "ORDERS": 15,
            "CHAT_SESSIONS": 7,
            "CHAT_MESSAGES": 8,
            "FAVORITES": 4,
            "COMMENTS": 6,
            "REVIEWS": 8,
            "BEHAVIORS": 6,
        }

        for fixture_name, expected_width in expected_widths.items():
            with self.subTest(fixture_name=fixture_name):
                rows = getattr(module, fixture_name)
                self.assertTrue(rows)
                self.assertTrue(all(len(row) == expected_width for row in rows))

    def test_bulk_fixture_ids_do_not_overwrite_interactive_low_id_rows(self):
        module = load_generator()

        self.assertTrue(all(row[0] >= 1000 for row in module.USERS[5:]))
        self.assertTrue(all(row[0] >= 1000 for row in module.STUDENT_ROSTER[3:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.PRODUCTS[12:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.CHAT_SESSIONS[4:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.CHAT_MESSAGES[9:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.FAVORITES[6:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.COMMENTS[8:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.REVIEWS[3:]))
        self.assertTrue(all(row[0] >= 10000 for row in module.BEHAVIORS[12:]))

    def test_latest_product_page_has_visible_variety(self):
        module = load_generator()
        latest_products = sorted(
            (row for row in module.PRODUCTS if row[15] == 1),
            key=lambda row: (row[16], row[0]),
            reverse=True,
        )[:20]

        self.assertGreaterEqual(len({row[2] for row in latest_products}), 5)
        self.assertGreaterEqual(len({row[3] for row in latest_products}), 10)

    def test_product_import_refreshes_generated_publish_time(self):
        sql = DATA_SQL.read_text(encoding="utf-8")
        product_block = re.search(
            r"INSERT INTO `biz_product` .*?ON DUPLICATE KEY UPDATE\n(.*?);",
            sql,
            re.S,
        )

        self.assertIsNotNone(product_block)
        self.assertIn("`create_time` = VALUES(`create_time`)", product_block.group(1))

    def test_demo_login_passwords_are_importable_without_runtime_rehash(self):
        module = load_generator()

        for user in module.USERS:
            with self.subTest(username=user[1]):
                self.assertRegex(user[2], r"^\$2a\$")

        sql = DATA_SQL.read_text(encoding="utf-8")
        self.assertNotIn("'123456'", sql)


if __name__ == "__main__":
    unittest.main()
