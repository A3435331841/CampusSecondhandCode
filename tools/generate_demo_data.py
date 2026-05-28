#!/usr/bin/env python3
from __future__ import annotations

from collections import Counter, defaultdict
from datetime import datetime, timedelta
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DATA_SQL = ROOT / "backend" / "src" / "main" / "resources" / "data.sql"
REFERENCE_TIME = datetime(2026, 5, 26, 12, 0, 0)

TOTAL_USERS = 320
TOTAL_PRODUCTS = 3000
TOTAL_ORDERS = 1200
TOTAL_CHAT_SESSIONS = 900
TOTAL_FAVORITES = 6000
TOTAL_COMMENTS = 3000
TOTAL_BEHAVIORS = 12000
GENERATED_USER_START_ID = 1001
GENERATED_PRODUCT_START_ID = 10001
GENERATED_ROW_START_ID = 10001
DEMO_PASSWORD_HASH = "$2a$10$aBOPm14jwMy0uaAMwl53x.hMTYh.GP2nPEhmwu.VhHxPUaGIXnOcK"

CATEGORIES = [
    (1, "数码电子", 1, 1),
    (2, "图书教材", 2, 1),
    (3, "服饰鞋包", 3, 1),
    (4, "交通出行", 4, 1),
    (5, "生活用品", 5, 1),
    (6, "其他闲置", 6, 1),
    (7, "运动健康", 7, 1),
    (8, "美妆护肤", 8, 1),
    (9, "零食食品", 9, 1),
    (10, "乐器音响", 10, 1),
    (11, "家居家具", 11, 1),
    (12, "演出票务", 12, 1),
]

COLLEGE_MAJORS = [
    ("计算机学院", ("软件工程", "人工智能", "网络工程")),
    ("电子信息学院", ("通信工程", "自动化", "电子信息工程")),
    ("经济管理学院", ("市场营销", "会计学", "电子商务")),
    ("外国语学院", ("英语", "翻译", "商务英语")),
    ("艺术设计学院", ("视觉传达设计", "工业设计", "数字媒体艺术")),
    ("材料与环境学院", ("环境工程", "材料科学", "新能源工程")),
    ("法学院", ("法学", "知识产权", "社会工作")),
    ("理学院", ("应用数学", "统计学", "应用物理")),
]
SURNAMES = ["张", "李", "王", "陈", "刘", "杨", "黄", "周", "吴", "徐", "孙", "胡", "朱", "林", "何", "郭"]
GIVEN_NAMES = ["晨", "明", "瑞", "雨桐", "子涵", "嘉怡", "思远", "宇航", "欣然", "浩然", "梓萱", "俊杰", "梦琪", "文博", "晓彤", "一凡"]

PICKUP_POINTS = [
    ("图书馆北门", "校园图书馆北门入口", "22.500000", "113.900000"),
    ("第一教学楼大厅", "第一教学楼一楼大厅", "22.501000", "113.901000"),
    ("三号宿舍区门口", "三号宿舍区入口处", "22.502000", "113.902000"),
    ("学生活动中心", "学生活动中心一楼", "22.503000", "113.903000"),
    ("主食堂东门", "主食堂东门外侧", "22.504000", "113.904000"),
    ("操场正门", "操场主入口旁", "22.505000", "113.905000"),
    ("一号宿舍前台", "一号宿舍楼前台", "22.506000", "113.906000"),
    ("图书馆二楼阅览室", "图书馆二楼阅览室入口", "22.507000", "113.907000"),
    ("创新中心大厅", "创新中心一楼大厅", "22.508000", "113.908000"),
    ("二号宿舍区门口", "二号宿舍区入口处", "22.509000", "113.909000"),
    ("校园南门", "校园南门保安亭旁", "22.510000", "113.910000"),
    ("艺术楼大厅", "艺术楼一楼大厅", "22.511000", "113.911000"),
    ("体育馆服务台", "体育馆首层服务台", "22.512000", "113.912000"),
    ("研究生公寓门口", "研究生公寓值班室旁", "22.513000", "113.913000"),
    ("校医院门口", "校医院入口长椅旁", "22.514000", "113.914000"),
]

CONDITIONS = ["九成新", "八成新", "几乎全新", "自用闲置", "毕业转让", "功能正常"]
PRODUCT_CATALOG = {
    1: [
        ("机械键盘", "按键回弹正常，已清洁消毒，适合宿舍学习使用。", 68, 180, [
            "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?auto=format&fit=crop&w=800&q=80",
        ]),
        ("无线鼠标", "握感舒适，滚轮和按键均正常，附接收器。", 28, 100, [
            "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1586349906584-bff065d2ffb4?auto=format&fit=crop&w=800&q=80",
        ]),
        ("蓝牙耳机", "左右耳播放正常，充电盒续航稳定。", 55, 210, [
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("便携显示器", "屏幕无明显划痕，附连接线，可当场测试。", 280, 420, [
            "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1585792180666-f7347c490ee2?auto=format&fit=crop&w=800&q=80",
        ]),
        ("充电宝", "电量显示正常，适合上课和出行备用。", 30, 95, [
            "https://images.unsplash.com/photo-1609592424824-196e3f78cf33?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1585338447937-7082f8fc763d?auto=format&fit=crop&w=800&q=80",
        ]),
        ("平板电脑", "日常记笔记和看网课流畅，外壳保护良好。", 360, 900, [
            "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1561154464-82e6b0c3ef45?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?auto=format&fit=crop&w=800&q=80",
        ]),
        ("宿舍路由器", "网络连接稳定，适合宿舍多人使用。", 35, 100, [
            "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1606904825846-647eb07f5be2?auto=format&fit=crop&w=800&q=80",
        ]),
        ("移动硬盘", "读写正常，适合课程资料和作业备份。", 90, 260, [
            "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    2: [
        ("高等数学教材", "书页完整，重点章节有少量标注，适合复习。", 10, 42, [
            "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=800&q=80",
        ]),
        ("数据结构教材", "包含课堂笔记和习题标记，知识点整理清楚。", 15, 45, [
            "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80",
        ]),
        ("考研数学真题", "题册完整，附有整理过的错题提示。", 12, 52, [
            "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=800&q=80",
        ]),
        ("六级备考资料", "配套练习和答案解析齐全，适合冲刺复习。", 9, 35, [
            "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1491841550275-ad7854e35ca6?auto=format&fit=crop&w=800&q=80",
        ]),
        ("教师资格证笔记", "知识框架整理清晰，附背诵重点。", 15, 48, [
            "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=800&q=80",
        ]),
        ("专业课复习讲义", "期末整理资料，章节重点覆盖较全。", 8, 38, [
            "https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=80",
        ]),
        ("普通话备考资料", "包含朗读篇目和易错读音整理。", 8, 30, [
            "https://images.unsplash.com/photo-1484415063229-3d6335668531?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=800&q=80",
        ]),
        ("设计基础画册", "画面印刷清晰，适合作业参考和临摹。", 18, 62, [
            "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    3: [
        ("运动鞋", "鞋底磨损较轻，已清洗，适合日常运动。", 45, 230, [
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&w=800&q=80",
        ]),
        ("连帽卫衣", "面料柔软，无明显污渍，适合春秋穿着。", 25, 130, [
            "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1578587018452-892bacefd3f2?auto=format&fit=crop&w=800&q=80",
        ]),
        ("双肩书包", "容量足够装电脑和教材，拉链顺畅。", 35, 150, [
            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1581605405669-fcdf81165b3b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("毕业学士服", "配件齐全，拍毕业照使用一次后闲置。", 22, 80, [
            "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1627556704302-624286467c65?auto=format&fit=crop&w=800&q=80",
        ]),
        ("羽绒服", "保暖性良好，衣面整洁，换季低价转让。", 80, 260, [
            "https://images.unsplash.com/photo-1544923246-77307dd628b7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?auto=format&fit=crop&w=800&q=80",
        ]),
        ("帆布鞋", "穿着次数少，鞋面清洁，尺码标识清楚。", 25, 90, [
            "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1463100099107-aa0980c362e6?auto=format&fit=crop&w=800&q=80",
        ]),
        ("通勤挎包", "内部空间充足，适合日常上课携带。", 30, 120, [
            "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=800&q=80",
        ]),
        ("正装外套", "面试使用过两次，版型挺括，保存良好。", 65, 220, [
            "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    4: [
        ("校园自行车", "刹车灵敏，轮胎气足，适合校内通勤。", 88, 380, [
            "https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571068316344-75bc76f77890?auto=format&fit=crop&w=800&q=80",
        ]),
        ("电动车头盔", "外观完好，卡扣牢固，骑行更加安全。", 18, 70, [
            "https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80",
        ]),
        ("折叠自行车", "折叠机构正常，方便存放在宿舍楼下。", 160, 420, [
            "https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1505705694340-019e0d8a2026?auto=format&fit=crop&w=800&q=80",
        ]),
        ("滑板", "板面结实，轮子转动顺畅，适合校园代步。", 45, 180, [
            "https://images.unsplash.com/photo-1547447134-cd3f5c716030?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1564429238961-bf8e8d171ab1?auto=format&fit=crop&w=800&q=80",
        ]),
        ("自行车锁", "锁具开合顺畅，附两把钥匙。", 8, 36, [
            "https://images.unsplash.com/photo-1471506480208-91b3a4cc78be?auto=format&fit=crop&w=800&q=80",
        ]),
        ("骑行雨披", "防水效果良好，雨天上课方便使用。", 10, 45, [
            "https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=800&q=80",
        ]),
        ("自行车车筐", "安装配件完整，可放书包和快递。", 10, 42, [
            "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80",
        ]),
        ("骑行手套", "掌面防滑，透气性好，清洗后转让。", 10, 55, [
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    5: [
        ("护眼台灯", "亮度可调，适合夜间自习和宿舍阅读。", 22, 120, [
            "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?auto=format&fit=crop&w=800&q=80",
        ]),
        ("迷你电饭煲", "容量适合一到两人使用，内胆干净。", 32, 140, [
            "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&w=800&q=80",
        ]),
        ("宿舍收纳箱", "容量大，箱体无破损，适合换季收纳。", 10, 50, [
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80",
        ]),
        ("桌面小风扇", "运行声音小，三档风速可调。", 18, 65, [
            "https://images.unsplash.com/photo-1565151443833-29bf2ba5dd8d?auto=format&fit=crop&w=800&q=80",
        ]),
        ("保温水杯", "杯盖密封良好，容量适合上课携带。", 15, 68, [
            "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1570831739435-6601aa3fa4fb?auto=format&fit=crop&w=800&q=80",
        ]),
        ("吹风机", "冷热风切换正常，功率适合宿舍使用。", 18, 85, [
            "https://images.unsplash.com/photo-1522338242992-e1a54906a8da?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1585747860036-4cb1c93d5fca?auto=format&fit=crop&w=800&q=80",
        ]),
        ("晾衣架套装", "结实耐用，宿舍阳台可直接使用。", 8, 34, [
            "https://images.unsplash.com/photo-1517677208171-0bc6725a3e60?auto=format&fit=crop&w=800&q=80",
        ]),
        ("全身镜", "镜面清晰，边框完好，适合宿舍摆放。", 25, 95, [
            "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1616627561839-074385245ff6?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    6: [
        ("民谣吉他", "琴弦状态良好，适合社团练习和入门。", 88, 360, [
            "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1525201548942-d8732f6617a0?auto=format&fit=crop&w=800&q=80",
        ]),
        ("羽毛球拍", "拍框无变形，手胶已更换，可直接使用。", 25, 150, [
            "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=800&q=80",
        ]),
        ("瑜伽垫", "垫面清洁，防滑效果良好，收纳方便。", 18, 70, [
            "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("画架", "支架稳固，可调高度，适合美术作业。", 35, 130, [
            "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("宿舍折叠椅", "结构稳固，折叠收纳省空间。", 20, 80, [
            "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=800&q=80",
        ]),
        ("考试收音耳机", "接收清晰，适合语言考试听力使用。", 18, 65, [
            "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1524678606370-a47ad25cb82a?auto=format&fit=crop&w=800&q=80",
        ]),
        ("毕业相框", "木质相框保存良好，适合放毕业照片。", 8, 42, [
            "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80",
        ]),
        ("社团演出服", "舞台使用一次，清洗后收纳，配件齐全。", 30, 120, [
            "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518834107812-67b0b7c58434?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    7: [
        ("羽毛球拍套装", "拍框无变形，附羽毛球若干，适合课余锻炼。", 35, 150, [
            "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?auto=format&fit=crop&w=800&q=80",
        ]),
        ("跑步鞋", "鞋底缓震良好，适合操场跑步和健身。", 60, 280, [
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&w=800&q=80",
        ]),
        ("哑铃一对", "包胶哑铃，适合宿舍力量训练。", 25, 100, [
            "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("健腹轮", "滚轮转动顺畅，附跪垫，适合宿舍健身。", 15, 60, [
            "https://images.unsplash.com/photo-1517963879433-6ad2b056d712?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80",
        ]),
        ("瑜伽垫加厚", "加厚防滑，适合室内运动和拉伸。", 20, 80, [
            "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=800&q=80",
        ]),
        ("游泳镜", "防雾效果好，密封圈完整，附泳帽。", 18, 65, [
            "https://images.unsplash.com/photo-1519315901367-f34ff9154487?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    8: [
        ("护肤套装", "品牌护肤三件套，保质期内，未开封。", 45, 200, [
            "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=800&q=80",
        ]),
        ("口红礼盒", "热门色号组合，全新未拆，适合自用或送人。", 35, 160, [
            "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=800&q=80",
        ]),
        ("面膜囤货", "补水面膜整箱转让，保质期充裕。", 20, 90, [
            "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=800&q=80",
        ]),
        ("香水小样", "大牌香水试用装合集，适合尝试不同香型。", 30, 120, [
            "https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1523293182086-7651a899d37f?auto=format&fit=crop&w=800&q=80",
        ]),
        ("眼影盘", "配色日常实用，粉质细腻，使用次数少。", 28, 130, [
            "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    9: [
        ("零食礼盒", "多口味组合礼盒，保质期内，适合分享。", 25, 100, [
            "https://images.unsplash.com/photo-1621939514649-280e2ee25f60?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1599599810694-b5b37304c041?auto=format&fit=crop&w=800&q=80",
        ]),
        ("茶叶罐装", "品质绿茶或红茶，密封保存良好。", 20, 80, [
            "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=800&q=80",
        ]),
        ("咖啡豆", "中度烘焙咖啡豆，密封袋装，适合手冲。", 30, 110, [
            "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=800&q=80",
        ]),
        ("坚果混合装", "每日坚果独立包装，保质期内。", 22, 85, [
            "https://images.unsplash.com/photo-1606312619070-d48b4c652a52?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=80",
        ]),
        ("蛋白棒", "健身代餐蛋白棒，多口味，保质期充裕。", 18, 70, [
            "https://images.unsplash.com/photo-1599599810694-b5b37304c041?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    10: [
        ("民谣吉他", "琴弦状态良好，适合社团练习和入门。", 88, 360, [
            "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1525201548942-d8732f6617a0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=800&q=80",
        ]),
        ("尤克里里", "音色明亮，附调音器和琴包，适合入门。", 45, 180, [
            "https://images.unsplash.com/photo-1511379938547-c1f69419868d?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?auto=format&fit=crop&w=800&q=80",
        ]),
        ("口琴", "音阶准确，清洁消毒后转让，附教程。", 15, 60, [
            "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?auto=format&fit=crop&w=800&q=80",
        ]),
        ("蓝牙音箱", "音质清晰，电池续航稳定，适合宿舍。", 35, 150, [
            "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&w=800&q=80",
        ]),
        ("耳放", "小型耳放，推力足够日常耳机使用。", 80, 300, [
            "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1558089687-f282ffcbc126?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    11: [
        ("床头灯", "触控调光，暖色光源，适合睡前阅读。", 20, 90, [
            "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?auto=format&fit=crop&w=800&q=80",
        ]),
        ("收纳架", "多层金属收纳架，承重稳固，适合宿舍。", 25, 100, [
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1501127122-f385ca6ddd9d?auto=format&fit=crop&w=800&q=80",
        ]),
        ("折叠小桌", "床上折叠桌，稳固不晃，适合学习和吃饭。", 30, 120, [
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=800&q=80",
        ]),
        ("懒人沙发", "充气懒人沙发，收纳方便，适合休闲。", 35, 140, [
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=800&q=80",
        ]),
        ("小型绿植", "桌面绿植盆栽，附花盆，适合宿舍摆放。", 10, 45, [
            "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
    12: [
        ("音乐节门票", "周末音乐节门票，因行程冲突低价转让。", 80, 350, [
            "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?auto=format&fit=crop&w=800&q=80",
        ]),
        ("话剧票", "校内话剧演出票，座位位置好，单张转让。", 30, 120, [
            "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1518834107812-67b0b7c58434?auto=format&fit=crop&w=800&q=80",
        ]),
        ("演唱会票", "演唱会门票看台区，无法到场急转。", 150, 600, [
            "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=800&q=80",
        ]),
        ("展览票", "当代艺术展门票，有效期内，可多次入场。", 25, 100, [
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80",
        ]),
    ],
}

DESC_TEMPLATES = [
    lambda n, d: f"{d} 因课程结束或宿舍整理转让，支持当面查看实物。",
    lambda n, d: f"毕业季清仓，{n}，{d}，有意者速联，可小刀。",
    lambda n, d: f"有轻微使用痕迹，功能完全正常，{d}，可验货后付款。",
    lambda n, d: f"朋友赠送，个人用不上，{n}全新未拆封，原价购入，亏本转让。",
    lambda n, d: f"临近考试季急售，{n}，{d}，价格可议，诚心要的来。",
]

BASE_STUDENT_ROSTER = [
    (1, "2023123401", "张晨", "计算机学院", "软件工程", "2023", "1234", 1),
    (2, "2023123402", "李明", "计算机学院", "人工智能", "2023", "8888", 1),
    (3, "2022121876", "王瑞", "经济管理学院", "市场营销", "2022", "5678", 1),
]
BASE_USERS = [
    (1, "admin", DEMO_PASSWORD_HASH, None, None, "平台管理员", "https://api.dicebear.com/7.x/avataaars/svg?seed=admin", None, None, None, None, None, None, "UNVERIFIED", None, "2026-05-26 09:00:00", 100, 0, "0.00", "0.00", "admin", 1, "2026-05-01 09:00:00"),
    (2, "user001", DEMO_PASSWORD_HASH, "demo-openid-user001", "demo-union-user001", "张同学", "https://api.dicebear.com/7.x/avataaars/svg?seed=user001", "张晨", "2023123401", "计算机学院", "软件工程", "2023", "1234", "VERIFIED", "2026-05-20 10:00:00", "2026-05-26 09:10:00", 115, 3, "5.00", "4.75", "user", 1, "2026-05-02 09:00:00"),
    (3, "user002", DEMO_PASSWORD_HASH, "demo-openid-user002", "demo-union-user002", "李同学", "https://api.dicebear.com/7.x/avataaars/svg?seed=user002", "李明", "2023123402", "计算机学院", "人工智能", "2023", "8888", "VERIFIED", "2026-05-20 11:00:00", "2026-05-26 09:12:00", 108, 4, "4.50", "5.00", "user", 1, "2026-05-02 09:30:00"),
    (4, "user003", DEMO_PASSWORD_HASH, "demo-openid-user003", "demo-union-user003", "王同学（禁用）", "https://api.dicebear.com/7.x/avataaars/svg?seed=user003", "王瑞", "2022121876", "经济管理学院", "市场营销", "2022", "5678", "VERIFIED", "2026-05-21 10:00:00", "2026-05-24 15:00:00", 70, 0, "3.00", "3.00", "user", 0, "2026-05-03 09:00:00"),
    (5, "user004", DEMO_PASSWORD_HASH, "demo-openid-user004", "demo-union-user004", "陈同学", "https://api.dicebear.com/7.x/avataaars/svg?seed=user004", None, None, None, None, None, None, "UNVERIFIED", None, "2026-05-25 13:00:00", 100, 1, "0.00", "0.00", "user", 1, "2026-05-04 09:00:00"),
]
BASE_PRODUCTS = [
    (1, 2, 1, "机械键盘", "青轴机械键盘，已清洁，按键和灯光都正常。", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", "129.00", 1, "图书馆北门", "校园图书馆北门入口", "22.500000", "113.900000", 36, 0, 0, 1, "2026-05-20 09:00:00"),
    (2, 2, 2, "数据结构教材", "二手教材，带少量笔记，适合期末复习。", "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=800&q=80", "35.00", 2, "第一教学楼大厅", "第一教学楼一楼大厅", "22.501000", "113.901000", 28, 0, 0, 1, "2026-05-20 10:00:00"),
    (3, 2, 4, "校园自行车", "轻便自行车，刚做过简单保养，适合校内通勤。", "https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?auto=format&fit=crop&w=800&q=80", "260.00", 1, "三号宿舍区门口", "三号宿舍区入口处", "22.502000", "113.902000", 42, 0, 0, 1, "2026-05-21 09:30:00"),
    (4, 3, 1, "蓝牙耳机", "低延迟蓝牙耳机，带充电盒，续航正常。", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80", "88.00", 1, "学生活动中心", "学生活动中心一楼", "22.503000", "113.903000", 31, 0, 0, 1, "2026-05-21 11:00:00"),
    (5, 3, 5, "护眼台灯", "三档亮度护眼台灯，适合宿舍夜间学习。", "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80", "45.00", 2, "主食堂东门", "主食堂东门外侧", "22.504000", "113.904000", 18, 0, 0, 1, "2026-05-22 08:30:00"),
    (6, 3, 3, "运动鞋", "四十二码运动鞋，只穿过两次，鞋面干净。", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=800&q=80", "120.00", 1, "操场正门", "操场主入口旁", "22.505000", "113.905000", 20, 0, 0, 1, "2026-05-22 12:30:00"),
    (7, 2, 6, "宿舍收纳箱", "大号塑料收纳箱，适合整理宿舍杂物。", "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80", "22.00", 3, "一号宿舍前台", "一号宿舍楼前台", "22.506000", "113.906000", 12, 0, 0, 1, "2026-05-23 09:00:00"),
    (8, 2, 2, "六级备考资料", "英语六级备考书，配答案解析。", "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1481627834876-b7833e8f5570?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=800&q=80", "18.00", 4, "图书馆二楼阅览室", "图书馆二楼阅览室入口", "22.507000", "113.907000", 16, 0, 0, 1, "2026-05-23 10:30:00"),
    (9, 3, 1, "便携显示器", "十五点六寸便携显示器，附全功能连接线。", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80", "399.00", 1, "创新中心大厅", "创新中心一楼大厅", "22.508000", "113.908000", 50, 0, 0, 1, "2026-05-23 14:00:00"),
    (10, 2, 5, "迷你电饭煲", "宿舍可用小电饭煲，容量适合一到两人。", "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=800&q=80", "65.00", 1, "二号宿舍区门口", "二号宿舍区入口处", "22.509000", "113.909000", 24, 0, 0, 0, "2026-05-24 09:00:00"),
    (11, 3, 4, "电动车头盔", "适合校内电动车骑行的头盔，外观完好。", "https://images.unsplash.com/photo-1557800636-894a64c1696f?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80", "30.00", 2, "校园南门", "校园南门保安亭旁", "22.510000", "113.910000", 8, 0, 0, 2, "2026-05-24 11:00:00"),
    (12, 2, 6, "毕业相框", "全新木质相框，适合放毕业照。", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80", "15.00", 0, "艺术楼大厅", "艺术楼一楼大厅", "22.511000", "113.911000", 6, 0, 0, 3, "2026-05-24 15:00:00"),
]
BASE_ORDERS = [
    ("ORD20260526001", 3, 2, 1, 1, "129.00", "图书馆北门", "校园图书馆北门入口", "22.500000", "113.900000", "2026-05-25 16:00:00", 1, 1, 1, "2026-05-25 15:00:00"),
    ("ORD20260526002", 2, 3, 4, 1, "88.00", "学生活动中心", "学生活动中心一楼", "22.503000", "113.903000", None, 0, 0, 0, "2026-05-26 09:20:00"),
    ("ORD20260526003", 3, 2, 12, 1, "15.00", "艺术楼大厅", "艺术楼一楼大厅", "22.511000", "113.911000", "2026-05-24 17:30:00", 1, 0, 1, "2026-05-24 16:00:00"),
    ("ORD20260526004", 2, 3, 6, 1, "120.00", "操场正门", "操场主入口旁", "22.505000", "113.905000", None, 0, 0, 2, "2026-05-23 18:00:00"),
]
BASE_CHAT_SESSIONS = [
    (1, "1_2_3", 1, 2, 3, "2026-05-25 09:12:00", "2026-05-25 09:02:00"),
    (2, "4_2_3", 4, 2, 3, "2026-05-26 09:18:00", "2026-05-26 09:08:00"),
    (3, "9_2_3", 9, 2, 3, "2026-05-25 13:08:00", "2026-05-25 13:02:00"),
    (4, "3_2_5", 3, 2, 5, "2026-05-25 11:08:00", "2026-05-25 11:02:00"),
]
BASE_CHAT_MESSAGES = [
    (1, "1_2_3", 1, 3, 2, "你好，键盘还在吗？", "text", "2026-05-25 09:02:00"),
    (2, "1_2_3", 1, 2, 3, "还在，图书馆北门交易可以吗？", "text", "2026-05-25 09:06:00"),
    (3, "1_2_3", 1, 3, 2, "可以，我十五点过去。", "text", "2026-05-25 09:12:00"),
    (4, "4_2_3", 4, 2, 3, "耳机今天可以交易吗？", "text", "2026-05-26 09:08:00"),
    (5, "4_2_3", 4, 3, 2, "可以，学生活动中心见。", "text", "2026-05-26 09:18:00"),
    (6, "9_2_3", 9, 2, 3, "便携显示器包含连接线吗？", "text", "2026-05-25 13:02:00"),
    (7, "9_2_3", 9, 3, 2, "包含连接线，可以现场试一下。", "text", "2026-05-25 13:08:00"),
    (8, "3_2_5", 3, 5, 2, "这辆车适合每天校内通勤吗？", "text", "2026-05-25 11:02:00"),
    (9, "3_2_5", 3, 2, 5, "适合，上周刚保养过。", "text", "2026-05-25 11:08:00"),
]
BASE_FAVORITES = [
    (1, 3, 1, "2026-05-25 09:00:00"),
    (2, 3, 2, "2026-05-25 09:10:00"),
    (3, 3, 3, "2026-05-25 09:20:00"),
    (4, 2, 4, "2026-05-25 10:00:00"),
    (5, 2, 9, "2026-05-25 10:15:00"),
    (6, 5, 1, "2026-05-25 10:20:00"),
]
BASE_COMMENTS = [
    (1, 1, 3, "键盘还在吗？", 1, "2026-05-25 09:05:00"),
    (2, 1, 2, "还在，可以图书馆北门交易。", 1, "2026-05-25 09:15:00"),
    (3, 2, 3, "下午可以看一下教材吗？", 1, "2026-05-25 10:10:00"),
    (4, 3, 5, "自行车看起来不错。", 1, "2026-05-25 11:00:00"),
    (5, 5, 2, "台灯亮度够晚自习用。", 1, "2026-05-25 12:00:00"),
    (6, 9, 2, "显示器包含连接线吗？", 1, "2026-05-25 13:00:00"),
    (7, 9, 3, "包含连接线，可以现场测试。", 1, "2026-05-25 13:10:00"),
    (8, 10, 5, "待审核商品的演示留言。", 1, "2026-05-25 14:00:00"),
]
BASE_REVIEWS = [
    (1, "ORD20260526001", 3, 2, "BUYER_TO_SELLER", 5, "卖家准时，商品和描述一致。", "2026-05-25 16:10:00"),
    (2, "ORD20260526001", 2, 3, "SELLER_TO_BUYER", 5, "买家确认很快，沟通顺畅。", "2026-05-25 16:12:00"),
    (3, "ORD20260526003", 3, 2, "BUYER_TO_SELLER", 4, "交易过程顺利。", "2026-05-24 17:40:00"),
]
BASE_BEHAVIORS = [
    (1, 3, 1, "VIEW", None, "2026-05-25 09:00:00"),
    (2, 3, 1, "FAVORITE", None, "2026-05-25 09:00:10"),
    (3, 3, 1, "COMMENT", None, "2026-05-25 09:05:00"),
    (4, 3, 1, "ORDER", None, "2026-05-25 15:00:00"),
    (5, 2, 4, "VIEW", None, "2026-05-26 09:05:00"),
    (6, 2, 4, "ORDER", None, "2026-05-26 09:20:00"),
    (7, 2, 9, "FAVORITE", None, "2026-05-25 10:15:00"),
    (8, 3, 2, "FAVORITE", None, "2026-05-25 09:10:00"),
    (9, 5, 1, "FAVORITE", None, "2026-05-25 10:20:00"),
    (10, 3, 3, "VIEW", None, "2026-05-25 11:00:00"),
    (11, 3, 3, "FAVORITE", None, "2026-05-25 09:20:00"),
    (12, 3, 12, "ORDER", None, "2026-05-24 16:00:00"),
]


def fmt_datetime(value):
    return value.strftime("%Y-%m-%d %H:%M:%S")


def generated_profile(user_id):
    index = user_id - 2
    real_name = SURNAMES[index % len(SURNAMES)] + GIVEN_NAMES[(index * 5) % len(GIVEN_NAMES)]
    college, majors = COLLEGE_MAJORS[index % len(COLLEGE_MAJORS)]
    major = majors[(index // len(COLLEGE_MAJORS)) % len(majors)]
    grade = str(2022 + index % 4)
    student_no = f"{grade}{700000 + user_id:06d}"
    suffix = f"{(1437 + index * 37) % 10000:04d}"
    return real_name, college, major, grade, student_no, suffix


def build_users():
    roster = list(BASE_STUDENT_ROSTER)
    users = list(BASE_USERS)
    roster_id = GENERATED_USER_START_ID
    for offset in range(TOTAL_USERS - len(users)):
        user_id = GENERATED_USER_START_ID + offset
        username = f"demo_user{offset + 1:03d}"
        real_name, college, major, grade, student_no, suffix = generated_profile(user_id)
        verified = user_id % 11 != 0
        status = 0 if user_id % 53 == 0 else 1
        verify_status = "VERIFIED" if verified else "UNVERIFIED"
        if verified:
            roster.append((roster_id, student_no, real_name, college, major, grade, suffix, 1))
            roster_id += 1
            verify_time = fmt_datetime(REFERENCE_TIME - timedelta(days=(user_id % 24) + 1))
            user_details = (real_name, student_no, college, major, grade, suffix)
        else:
            verify_time = None
            user_details = (None, None, None, None, None, None)
        last_login = fmt_datetime(REFERENCE_TIME - timedelta(minutes=user_id * 7))
        create_time = fmt_datetime(REFERENCE_TIME - timedelta(days=80 - user_id % 70))
        nickname = f"{real_name[0]}同学" if verified else f"访客{user_id - 1:03d}"
        users.append(
            (
                user_id, username, DEMO_PASSWORD_HASH, f"demo-openid-{username}", f"demo-union-{username}",
                nickname, f"https://api.dicebear.com/7.x/avataaars/svg?seed={username}",
                *user_details, verify_status, verify_time, last_login, 90 + user_id % 25,
                0, "0.00", "0.00", "user", status, create_time,
            )
        )
    return roster, users


STUDENT_ROSTER, USERS = build_users()
ACTIVE_USER_IDS = [
    row[0] for row in USERS if row[20] == "user" and row[21] == 1 and row[13] == "VERIFIED"
]


def pick_other_user(seed, excluded_user_id):
    index = seed % len(ACTIVE_USER_IDS)
    candidate = ACTIVE_USER_IDS[index]
    if candidate == excluded_user_id:
        candidate = ACTIVE_USER_IDS[(index + 1) % len(ACTIVE_USER_IDS)]
    return candidate


def pick_images(product_images, ordinal):
    pool = product_images
    if ordinal % 10 == 0:
        count = 1
    elif ordinal % 5 == 0:
        count = min(4, len(pool))
    elif ordinal % 3 == 0:
        count = min(3, len(pool))
    else:
        count = min(2, len(pool))
    start = (ordinal * 7) % len(pool)
    imgs = []
    for i in range(count):
        imgs.append(pool[(start + i) % len(pool)])
    return ",".join(imgs)


def build_products():
    products = list(BASE_PRODUCTS)
    for offset in range(TOTAL_PRODUCTS - len(products)):
        product_id = GENERATED_PRODUCT_START_ID + offset
        ordinal = len(BASE_PRODUCTS) + offset + 1
        category_id = offset % len(CATEGORIES) + 1
        options = PRODUCT_CATALOG[category_id]
        title_name, detail, low_price, price_span, product_images = options[(ordinal // 6) % len(options)]
        condition = CONDITIONS[(ordinal * 5) % len(CONDITIONS)]
        title = f"{condition}{title_name}"
        template = DESC_TEMPLATES[ordinal % len(DESC_TEMPLATES)]
        if ordinal % 17 == 0:
            description = ""
        else:
            description = template(title_name, detail)
        images = pick_images(product_images, ordinal)
        price = f"{low_price + (ordinal * 17) % price_span:.2f}"
        seller_id = ACTIVE_USER_IDS[(ordinal * 13) % len(ACTIVE_USER_IDS)]
        if ordinal % 19 == 0:
            pickup_place_name = None
            pickup_address = None
            pickup_lat = None
            pickup_lng = None
        else:
            pickup_place_name, pickup_address, pickup_lat, pickup_lng = PICKUP_POINTS[(ordinal * 7) % len(PICKUP_POINTS)]
        if ordinal % 31 == 0:
            status = 0
        elif ordinal % 37 == 0:
            status = 4
        elif ordinal % 29 == 0:
            status = 2
        elif ordinal % 23 == 0:
            status = 3
        else:
            status = 1
        stock = 0 if status == 3 else 1 + ordinal % 3
        create_time = fmt_datetime(
            REFERENCE_TIME - timedelta(minutes=(TOTAL_PRODUCTS - ordinal) * 37)
        )
        products.append(
            (
                product_id, seller_id, category_id, title, description, images, price, stock,
                pickup_place_name, pickup_address, pickup_lat, pickup_lng,
                10 + (ordinal * 19) % 490, 0, 0, status, create_time,
            )
        )
    return products


PRODUCTS = build_products()
PRODUCT_BY_ID = {row[0]: row for row in PRODUCTS}
PRODUCT_IDS = [row[0] for row in PRODUCTS]
GENERATED_PRODUCT_IDS = [row[0] for row in PRODUCTS[len(BASE_PRODUCTS):]]


def build_orders():
    orders = list(BASE_ORDERS)
    for sequence in range(5, TOTAL_ORDERS + 1):
        product = PRODUCT_BY_ID[GENERATED_PRODUCT_IDS[(sequence * 37) % len(GENERATED_PRODUCT_IDS)]]
        seller_id = product[1]
        buyer_id = pick_other_user(sequence * 17, seller_id)
        if sequence % 10 in (0, 1):
            status = 0
        elif sequence % 10 == 2:
            status = 2
        else:
            status = 1
        created_at = REFERENCE_TIME - timedelta(days=1 + (sequence * 3) % 60, minutes=(sequence * 11) % 600)
        finish_time = fmt_datetime(created_at + timedelta(hours=2 + sequence % 18)) if status == 1 else None
        buyer_rated = 1 if status == 1 else 0
        seller_rated = 1 if status == 1 and sequence % 5 != 0 else 0
        orders.append(
            (
                f"ORD2026{sequence:08d}", buyer_id, seller_id, product[0], 1, product[6],
                product[8], product[9], product[10], product[11], finish_time,
                buyer_rated, seller_rated, status, fmt_datetime(created_at),
            )
        )
    return orders


ORDERS = build_orders()


def build_chat_sessions():
    sessions = list(BASE_CHAT_SESSIONS)
    for offset in range(TOTAL_CHAT_SESSIONS - len(sessions)):
        session_id = GENERATED_ROW_START_ID + offset
        ordinal = len(BASE_CHAT_SESSIONS) + offset + 1
        product = PRODUCT_BY_ID[GENERATED_PRODUCT_IDS[(offset + 20) % len(GENERATED_PRODUCT_IDS)]]
        seller_id = product[1]
        buyer_id = pick_other_user(ordinal * 29, seller_id)
        created_at = REFERENCE_TIME - timedelta(days=1 + ordinal % 45, minutes=ordinal % 300)
        last_message_time = created_at + timedelta(minutes=12)
        sessions.append(
            (
                session_id, f"{product[0]}_{min(seller_id, buyer_id)}_{max(seller_id, buyer_id)}",
                product[0], min(seller_id, buyer_id), max(seller_id, buyer_id),
                fmt_datetime(last_message_time), fmt_datetime(created_at),
            )
        )
    return sessions


CHAT_SESSIONS = build_chat_sessions()


def build_chat_messages():
    messages = list(BASE_CHAT_MESSAGES)
    message_id = GENERATED_ROW_START_ID
    for session in CHAT_SESSIONS[4:]:
        _, session_key, product_id, user_a_id, user_b_id, _, create_time = session
        seller_id = PRODUCT_BY_ID[product_id][1]
        buyer_id = user_b_id if user_a_id == seller_id else user_a_id
        title = PRODUCT_BY_ID[product_id][3]
        started_at = datetime.strptime(create_time, "%Y-%m-%d %H:%M:%S")
        conversation = [
            (buyer_id, seller_id, f"你好，{title}现在还可以交易吗？"),
            (seller_id, buyer_id, "还在的，商品状态和描述一致，可以当面看看。"),
            (buyer_id, seller_id, "好的，我下课后过去面交，谢谢。"),
        ]
        for offset, (from_user_id, to_user_id, content) in enumerate(conversation):
            messages.append(
                (
                    message_id, session_key, product_id, from_user_id, to_user_id, content,
                    "text", fmt_datetime(started_at + timedelta(minutes=offset * 6)),
                )
            )
            message_id += 1
    return messages


CHAT_MESSAGES = build_chat_messages()


def build_favorites():
    favorites = list(BASE_FAVORITES)
    seen = {(row[1], row[2]) for row in favorites}
    offset = 0
    while len(favorites) < TOTAL_FAVORITES:
        user_id = ACTIVE_USER_IDS[offset % len(ACTIVE_USER_IDS)]
        product_id = PRODUCT_IDS[(user_id * 41 + (offset // len(ACTIVE_USER_IDS)) * 67) % len(PRODUCT_IDS)]
        if PRODUCT_BY_ID[product_id][1] != user_id and (user_id, product_id) not in seen:
            seen.add((user_id, product_id))
            favorites.append(
                (
                    GENERATED_ROW_START_ID + len(favorites) - len(BASE_FAVORITES), user_id, product_id,
                    fmt_datetime(REFERENCE_TIME - timedelta(days=offset % 62, minutes=offset % 500)),
                )
            )
        offset += 1
    return favorites


FAVORITES = build_favorites()


def build_comments():
    comments = list(BASE_COMMENTS)
    prompts = [
        "请问周末可以面交吗？",
        "商品和图片中的状态一致吗？",
        "请问还可以稍微优惠一点吗？",
        "今天傍晚方便当面看一下吗？",
        "同学，请问物品还在吗？",
        "可以在图书馆附近面交吗？",
    ]
    for offset in range(TOTAL_COMMENTS - len(comments)):
        comment_id = GENERATED_ROW_START_ID + offset
        ordinal = len(BASE_COMMENTS) + offset + 1
        product_id = PRODUCT_IDS[(ordinal * 43) % len(PRODUCT_IDS)]
        seller_id = PRODUCT_BY_ID[product_id][1]
        user_id = pick_other_user(ordinal * 7, seller_id)
        content = prompts[ordinal % len(prompts)]
        comments.append(
            (
                comment_id, product_id, user_id, content, 1,
                fmt_datetime(REFERENCE_TIME - timedelta(days=ordinal % 50, minutes=ordinal % 400)),
            )
        )
    return comments


COMMENTS = build_comments()


def build_reviews():
    reviews = list(BASE_REVIEWS)
    review_id = GENERATED_ROW_START_ID
    buyer_reviews = ["商品保存很好，和描述一致，面交顺利。", "卖家回复及时，交易体验很好。", "物品实用，价格合理，推荐交易。"]
    seller_reviews = ["买家准时到达，确认收货很快。", "沟通礼貌顺畅，交易过程愉快。", "买家守时，面交安排很方便。"]
    for sequence, order in enumerate(ORDERS[4:], start=5):
        order_no, buyer_id, seller_id, _, _, _, _, _, _, _, finish_time, buyer_rated, seller_rated, _, _ = order
        if buyer_rated:
            reviews.append(
                (
                    review_id, order_no, buyer_id, seller_id, "BUYER_TO_SELLER",
                    4 + sequence % 2, buyer_reviews[sequence % len(buyer_reviews)], finish_time,
                )
            )
            review_id += 1
        if seller_rated:
            reviews.append(
                (
                    review_id, order_no, seller_id, buyer_id, "SELLER_TO_BUYER",
                    4 + (sequence + 1) % 2, seller_reviews[sequence % len(seller_reviews)], finish_time,
                )
            )
            review_id += 1
    return reviews


REVIEWS = build_reviews()


def build_behaviors():
    behaviors = list(BASE_BEHAVIORS)
    behavior_types = ("VIEW", "FAVORITE", "COMMENT", "ORDER")
    for offset in range(TOTAL_BEHAVIORS - len(behaviors)):
        behavior_id = GENERATED_ROW_START_ID + offset
        ordinal = len(BASE_BEHAVIORS) + offset + 1
        product_id = PRODUCT_IDS[(ordinal * 47) % len(PRODUCT_IDS)]
        user_id = pick_other_user(ordinal * 11, PRODUCT_BY_ID[product_id][1])
        behaviors.append(
            (
                behavior_id, user_id, product_id, behavior_types[ordinal % len(behavior_types)], None,
                fmt_datetime(REFERENCE_TIME - timedelta(days=ordinal % 70, minutes=ordinal % 600)),
            )
        )
    return behaviors


BEHAVIORS = build_behaviors()


def apply_product_counts(products):
    favorite_counts = Counter(row[2] for row in FAVORITES)
    comment_counts = Counter(row[1] for row in COMMENTS)
    updated = []
    for row in products:
        values = list(row)
        values[13] = favorite_counts[row[0]]
        values[14] = comment_counts[row[0]]
        updated.append(tuple(values))
    return updated


def apply_user_counts(users):
    favorite_counts = Counter(row[1] for row in FAVORITES)
    buy_scores = defaultdict(list)
    sell_scores = defaultdict(list)
    for _, _, _, reviewee_id, role_type, score, _, _ in REVIEWS:
        scores = buy_scores if role_type == "SELLER_TO_BUYER" else sell_scores
        scores[reviewee_id].append(score)
    updated = []
    for row in users:
        values = list(row)
        values[17] = favorite_counts[row[0]]
        values[18] = f"{sum(buy_scores[row[0]]) / len(buy_scores[row[0]]):.2f}" if buy_scores[row[0]] else "0.00"
        values[19] = f"{sum(sell_scores[row[0]]) / len(sell_scores[row[0]]):.2f}" if sell_scores[row[0]] else "0.00"
        updated.append(tuple(values))
    return updated


PRODUCTS = apply_product_counts(PRODUCTS)
USERS = apply_user_counts(USERS)


def q(value):
    if value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def insert_block(table, columns, rows, update_columns):
    column_text = ", ".join(f"`{column}`" for column in columns)
    row_text = ",\n  ".join("(" + ", ".join(q(value) for value in row) + ")" for row in rows)
    update_text = ",\n  ".join(f"`{column}` = VALUES(`{column}`)" for column in update_columns)
    return (
        f"INSERT INTO `{table}` ({column_text})\n"
        f"VALUES\n"
        f"  {row_text}\n"
        f"ON DUPLICATE KEY UPDATE\n"
        f"  {update_text};"
    )


def build_sql():
    sections = [
        "-- Demo data generated by tools/generate_demo_data.py.",
        "-- Re-run the script after editing the Python data fixtures.",
        "SET NAMES utf8mb4;",
        insert_block(
            "biz_category",
            ("id", "name", "sort_no", "status"),
            CATEGORIES,
            ("name", "sort_no", "status"),
        ),
        insert_block(
            "biz_student_roster",
            ("id", "student_no", "real_name", "college", "major", "grade", "id_card_suffix", "status"),
            STUDENT_ROSTER,
            ("student_no", "real_name", "college", "major", "grade", "id_card_suffix", "status"),
        ),
        insert_block(
            "sys_user",
            (
                "id", "username", "password", "openid", "unionid", "nickname", "avatar", "real_name",
                "student_no", "college", "major", "grade", "id_card_suffix", "verify_status",
                "verify_time", "last_login_time", "credit_score", "favorite_count", "buy_rating_avg",
                "sell_rating_avg", "role", "status", "create_time",
            ),
            USERS,
            (
                "username", "password", "openid", "unionid", "nickname", "avatar", "real_name",
                "student_no", "college", "major", "grade", "id_card_suffix", "verify_status",
                "verify_time", "last_login_time", "credit_score", "favorite_count", "buy_rating_avg",
                "sell_rating_avg", "role", "status",
            ),
        ),
        insert_block(
            "biz_product",
            (
                "id", "seller_id", "category_id", "title", "description", "images", "price", "stock",
                "pickup_place_name", "pickup_address", "pickup_lat", "pickup_lng", "view_count",
                "favorite_count", "comment_count", "status", "create_time",
            ),
            PRODUCTS,
            (
                "seller_id", "category_id", "title", "description", "images", "price", "stock",
                "pickup_place_name", "pickup_address", "pickup_lat", "pickup_lng", "view_count",
                "favorite_count", "comment_count", "status", "create_time",
            ),
        ),
        insert_block(
            "biz_order",
            (
                "order_no", "buyer_id", "seller_id", "product_id", "buy_count", "total_amount",
                "pickup_place_name", "pickup_address", "pickup_lat", "pickup_lng", "finish_time",
                "buyer_rated", "seller_rated", "status", "create_time",
            ),
            ORDERS,
            (
                "buyer_id", "seller_id", "product_id", "buy_count", "total_amount",
                "pickup_place_name", "pickup_address", "pickup_lat", "pickup_lng", "finish_time",
                "buyer_rated", "seller_rated", "status",
            ),
        ),
        insert_block(
            "biz_chat_session",
            ("id", "session_key", "product_id", "user_a_id", "user_b_id", "last_message_time", "create_time"),
            CHAT_SESSIONS,
            ("session_key", "product_id", "user_a_id", "user_b_id", "last_message_time", "create_time"),
        ),
        insert_block(
            "biz_chat_message",
            ("id", "session_key", "product_id", "from_user_id", "to_user_id", "content", "type", "create_time"),
            CHAT_MESSAGES,
            ("session_key", "product_id", "from_user_id", "to_user_id", "content", "type", "create_time"),
        ),
        insert_block(
            "biz_favorite",
            ("id", "user_id", "product_id", "create_time"),
            FAVORITES,
            ("user_id", "product_id", "create_time"),
        ),
        insert_block(
            "biz_product_comment",
            ("id", "product_id", "user_id", "content", "status", "create_time"),
            COMMENTS,
            ("product_id", "user_id", "content", "status", "create_time"),
        ),
        insert_block(
            "biz_order_review",
            ("id", "order_no", "reviewer_id", "reviewee_id", "role_type", "score", "content", "create_time"),
            REVIEWS,
            ("order_no", "reviewer_id", "reviewee_id", "role_type", "score", "content", "create_time"),
        ),
        insert_block(
            "biz_user_behavior",
            ("id", "user_id", "product_id", "behavior_type", "behavior_value", "create_time"),
            BEHAVIORS,
            ("user_id", "product_id", "behavior_type", "behavior_value", "create_time"),
        ),
    ]
    return "\n\n".join(sections) + "\n"


def main():
    DATA_SQL.write_text(build_sql(), encoding="utf-8")
    print(
        "Wrote "
        f"{DATA_SQL.relative_to(ROOT)}: "
        f"{len(USERS)} users, {len(PRODUCTS)} products, {len(ORDERS)} orders, "
        f"{len(CHAT_MESSAGES)} messages"
    )


if __name__ == "__main__":
    main()
