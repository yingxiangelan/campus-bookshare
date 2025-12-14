import sqlite3
import os

DB_NAME = "campus_books.db"

def init_db():
    # 如果已存在，先删除旧的，保证数据干净（演示用）
    if os.path.exists(DB_NAME):
        os.remove(DB_NAME)
        print(f"已清理旧数据库: {DB_NAME}")

    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    # 1. 创建书籍表
    cursor.execute('''
    CREATE TABLE books (
        isbn TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        author TEXT NOT NULL
    )
    ''')

    # 2. 预置演示数据
    sample_books = [
        ("9787302423287", "机器学习 (西瓜书)", "周志华"),
        ("9787111370048", "Java核心技术 卷I", "Cay S. Horstmann"),
        ("9787115546081", "Python编程：从入门到实践", "Eric Matthes"),
        ("9787506365437", "活着", "余华"),
        ("9787536692930", "三体", "刘慈欣"),
        ("9787544267038","孙子兵法·三十六计","孙武"), # 对应同路径下的example图片进行测试
    ]

    cursor.executemany('INSERT INTO books VALUES (?,?,?)', sample_books)
    
    conn.commit()
    conn.close()
    print(f"数据库 {DB_NAME} 初始化完成！")
    print(f"已写入 {len(sample_books)} 本测试书籍。")

if __name__ == "__main__":
    init_db()