package com.dechub.tanishq.gsheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Iterator;

@Component
public class ExcelEventsUtil {
    public int uploadExcelFile(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = countRows(sheet);
            return rowCount;
        } catch (IOException e) {
            return 0;
        }
    }

    private int countRows(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();
        int rowCount = 0;

        // Skip the header row
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        // Count the remaining rows
        while (rowIterator.hasNext()) {
            rowIterator.next();
            rowCount++;
        }

        return rowCount;
    }
}
