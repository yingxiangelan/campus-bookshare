import cv2
import numpy as np
import sqlite3
import asyncio
import json 
import time 
import os  
from concurrent.futures import ThreadPoolExecutor
from fastapi import FastAPI, UploadFile, File
from pyzbar.pyzbar import decode
from pydantic import BaseModel
from typing import List

# --- 配置 ---
app = FastAPI()
DB_PATH = "campus_books.db"
RESULT_DIR = "scan_results" # 新增：定义保存 JSON 的文件夹名称

# 确保结果文件夹存在
if not os.path.exists(RESULT_DIR):
    os.makedirs(RESULT_DIR)

executor = ThreadPoolExecutor(max_workers=4)

# --- 数据模型 ---
class ItemData(BaseModel):
    code: str
    bookName: str
    author: str
    isbn: str

class ResponseModel(BaseModel):
    code: str
    msg: str
    data: List[ItemData]
    saved_file: str = "" # 可选：在返回给前端的信息里，也告诉它文件保存在哪了

# --- 核心功能函数 ---

def query_book_by_isbn(isbn: str):
    if not os.path.exists(DB_PATH):
        return None
    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute("SELECT title, author FROM books WHERE isbn=?", (isbn,))
        row = cursor.fetchone()
        conn.close()
        if row:
            return {"title": row[0], "author": row[1]}
        return None
    except Exception as e:
        print(f"DB Error: {e}")
        return None

def process_image_task(image_bytes: bytes) -> List[dict]:
    results = []
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None: return []

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    decoded_objects = decode(gray)

    for obj in decoded_objects:
        raw_content = obj.data.decode("utf-8")
        obj_type = obj.type
        
        item = {
            "code": "2",
            "bookName": "",
            "author": "",
            "isbn": raw_content
        }

        if obj_type == 'EAN13':
            item["code"] = "1"
            book_info = query_book_by_isbn(raw_content)
            if book_info:
                item["bookName"] = book_info["title"]
                item["author"] = book_info["author"]
            else:
                item["bookName"] = "未录入系统"
                item["author"] = "未知"     
        elif obj_type == 'QRCODE':
            item["code"] = "0"
            item["bookName"] = "二维码信息"
            item["author"] = "N/A"
        else:
            item["code"] = "2"
            item["bookName"] = "未知条码类型"
            item["author"] = "N/A"

        results.append(item)
    return results

# --- 保存 JSON 到本地文件 ---
def save_result_to_file(response_data: dict) -> str:
    """
    将字典数据保存为 JSON 文件
    返回保存的文件路径
    """
    # 1. 生成唯一文件名 (例如: result_1734234567.json)
    timestamp = int(time.time())
    filename = f"result_{timestamp}.json"
    file_path = os.path.join(RESULT_DIR, filename)

    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            # ensure_ascii=False 保证中文字符正常显示，而不是 \uXXXX
            # indent=4 让生成的 JSON 文件有缩进，方便人类阅读
            json.dump(response_data, f, ensure_ascii=False, indent=4)
        print(f"✅ 文件已保存: {file_path}")
        return file_path
    except Exception as e:
        print(f"❌ 保存文件失败: {e}")
        return ""

# --- API 接口 ---

@app.post("/api/scan", response_model=ResponseModel)
async def scan_codes(file: UploadFile = File(...)):
    try:
        content = await file.read()
        loop = asyncio.get_event_loop()
        scan_data = await loop.run_in_executor(executor, process_image_task, content)

        # 构造响应字典
        if not scan_data:
            response_dict = {
                "code": "404",
                "msg": "未能识别出任何有效的 ISBN 或二维码",
                "data": []
            }
        else:
            response_dict = {
                "code": "200",
                "msg": "操作成功",
                "data": scan_data
            }

        # --- 在返回前保存到本地 ---
        saved_path = save_result_to_file(response_dict)
        
        # 将文件路径也加入到返回信息中（可选，方便你调试）
        response_dict["saved_file"] = saved_path

        return response_dict

    except Exception as e:
        return {
            "code": "500",
            "msg": f"服务器内部错误: {str(e)}",
            "data": [],
            "saved_file": ""
        }

if __name__ == "__main__":
    import uvicorn
    print(f"🚀 服务启动中... 结果将保存在 ./{RESULT_DIR}/ 目录下")
    uvicorn.run(app, host="0.0.0.0", port=8000)