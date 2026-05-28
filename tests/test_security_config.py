import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
WEB_MVC_CONFIG = ROOT / "backend" / "src" / "main" / "java" / "com" / "campus" / "secondhand" / "config" / "WebMvcConfig.java"


class SecurityConfigTest(unittest.TestCase):
    def test_file_upload_requires_login_but_public_images_stay_accessible(self):
        content = WEB_MVC_CONFIG.read_text(encoding="utf-8")

        self.assertIn('excludePathPatterns("/api/file/img/**")', content)
        self.assertNotIn('excludePathPatterns("/api/file/**")', content)

    def test_product_comments_are_public_but_comment_mutation_stays_protected(self):
        content = WEB_MVC_CONFIG.read_text(encoding="utf-8")

        self.assertIn('excludePathPatterns("/api/comment/list")', content)
        self.assertNotIn('excludePathPatterns("/api/comment/**")', content)


if __name__ == "__main__":
    unittest.main()
