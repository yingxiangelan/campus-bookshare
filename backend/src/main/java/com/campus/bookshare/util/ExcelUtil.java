package com.campus.bookshare.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Excel工具类
 */
public class ExcelUtil {
    
    /**
     * 读取Excel文件并转换为Map列表
     */
    public static List<Map<String, Object>> readExcel(InputStream inputStream, String fileName) throws Exception {
        Workbook workbook;
        
        // 根据文件扩展名创建不同的Workbook
        if (fileName.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new Exception("不支持的文件格式");
        }
        
        List<Map<String, Object>> dataList = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            workbook.close();
            return dataList;
        }
        
        // 读取表头
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            headers.add(getCellValue(cell));
        }
        
        // 读取数据行
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            Map<String, Object> rowData = new HashMap<>();
            boolean hasData = false;
            
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                String value = getCellValue(cell);
                
                if (value != null && !value.trim().isEmpty()) {
                    hasData = true;
                }
                
                rowData.put(headers.get(j), value);
            }
            
            if (hasData) {
                dataList.add(rowData);
            }
        }
        
        workbook.close();
        return dataList;
    }
    
    /**
     * 获取单元格值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 将Excel数据转换为商品数据格式
     */
    public static List<Map<String, Object>> convertToGoodsData(List<Map<String, Object>> excelData, Long defaultUserId) {
        List<Map<String, Object>> goodsList = new ArrayList<>();
        
        // 字段映射（Excel列名 -> 数据库字段名）
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("书名", "bookName");
        fieldMapping.put("bookName", "bookName");
        fieldMapping.put("作者", "author");
        fieldMapping.put("author", "author");
        fieldMapping.put("出版社", "publisher");
        fieldMapping.put("publisher", "publisher");
        fieldMapping.put("ISBN", "isbn");
        fieldMapping.put("isbn", "isbn");
        fieldMapping.put("原价", "originalPrice");
        fieldMapping.put("originalPrice", "originalPrice");
        fieldMapping.put("售价", "price");
        fieldMapping.put("price", "price");
        fieldMapping.put("新旧程度", "condition");
        fieldMapping.put("condition", "condition");
        fieldMapping.put("校区", "campus");
        fieldMapping.put("campus", "campus");
        fieldMapping.put("专业", "major");
        fieldMapping.put("major", "major");
        fieldMapping.put("适用课程", "courseName");
        fieldMapping.put("courseName", "courseName");
        fieldMapping.put("商品描述", "description");
        fieldMapping.put("description", "description");
        
        for (Map<String, Object> row : excelData) {
            Map<String, Object> goods = new HashMap<>();
            goods.put("userId", defaultUserId);
            
            // 转换字段
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String excelKey = entry.getKey();
                String dbKey = fieldMapping.get(excelKey);
                
                if (dbKey != null && entry.getValue() != null) {
                    Object value = entry.getValue();
                    
                    // 处理数字字段
                    if ("price".equals(dbKey) || "originalPrice".equals(dbKey)) {
                        try {
                            if (value instanceof String) {
                                value = Double.parseDouble((String) value);
                            }
                            goods.put(dbKey, value);
                        } catch (NumberFormatException e) {
                            // 忽略无效的数字
                        }
                    } else {
                        goods.put(dbKey, value);
                    }
                }
            }
            
            goodsList.add(goods);
        }
        
        return goodsList;
    }
    
    /**
     * 生成Excel模板文件
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void generateTemplate(OutputStream outputStream) throws Exception {
        // 创建Workbook（使用xlsx格式）
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品导入模板");
        
        // 创建样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "书名", "作者", "出版社", "ISBN", "原价", "售价", 
            "新旧程度", "校区", "专业", "适用课程", "商品描述"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 创建示例数据行
        Row exampleRow = sheet.createRow(1);
        Object[] exampleData = {
            "深入理解计算机系统",
            "Randal E.Bryant",
            "机械工业出版社",
            "9787111544937",
            139.00,
            80.00,
            "9成新",
            "东校区",
            "计算机科学与技术",
            "计算机组成原理",
            "书籍保存完好，无笔记"
        };
        
        for (int i = 0; i < exampleData.length; i++) {
            Cell cell = exampleRow.createCell(i);
            Object value = exampleData[i];
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            }
            cell.setCellStyle(dataStyle);
        }
        
        // 设置列宽（自动调整）
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小宽度
            int width = sheet.getColumnWidth(i);
            if (width < 2000) {
                sheet.setColumnWidth(i, 2000);
            } else if (width > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
        
        // 设置行高
        headerRow.setHeightInPoints(20);
        exampleRow.setHeightInPoints(18);
        
        // 写入输出流
        workbook.write(outputStream);
        workbook.close();
    }
}

