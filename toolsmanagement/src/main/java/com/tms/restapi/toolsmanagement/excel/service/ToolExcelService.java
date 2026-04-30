package com.tms.restapi.toolsmanagement.excel.service;

import com.tms.restapi.toolsmanagement.excel.dto.ExcelResponse;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolExcelService {

    @Autowired
    private ToolRepository toolRepository;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ExcelResponse uploadTools(MultipartFile file) {

        int total = 0;
        int success = 0;
        int failed = 0;
        int duplicate = 0;

        try {

            InputStream is = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<Tool> toolList = new ArrayList<>();

            for (int i = 4; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                total++;

                try {

                    Tool tool = new Tool();

                    String location = getString(row.getCell(0)).trim();
                    String siNo = getString(row.getCell(1)).trim();

                    if (toolRepository.existsByBySiNoAndLocationIgnoreCaseAndTrim(siNo, location)) {
                        duplicate++;
                        continue;
                    }

                    boolean existsInCurrentBatch = toolList.stream()
                            .anyMatch(t -> t.getSiNo().equalsIgnoreCase(siNo)
                                    && t.getLocation().equalsIgnoreCase(location));

                    if (existsInCurrentBatch) {
                        duplicate++;
                        continue;
                    }

                    tool.setSiNo(siNo);
                    tool.setLocation(location);

                    tool.setToolNo(getString(row.getCell(2)));

                    tool.setDescription(getString(row.getCell(3)));

                    tool.setToolLocation(getString(row.getCell(4)));

                    int quantity = 0;
                    Cell qCell = row.getCell(5);
                    if (qCell != null && qCell.getCellType() == CellType.NUMERIC) {
                        quantity = (int) qCell.getNumericCellValue();
                    }

                    tool.setQuantity(quantity);
                    tool.setAvailability(quantity);

                    tool.setCondition(getCondition(row));

                    String calReq = getString(row.getCell(10));

                    if (calReq.equalsIgnoreCase("NA")) {

                        tool.setCalibrationRequired(false);
                        tool.setCalibrationPeriodMonths(null);
                        tool.setNextCalibrationDate(null);

                    } else {

                        tool.setCalibrationRequired(true);

                        if (calReq.contains("12")) {
                            tool.setCalibrationPeriodMonths(12);
                        }

                        if (calReq.contains("24")) {
                            tool.setCalibrationPeriodMonths(24);
                        }

                        String dateStr = getString(row.getCell(11));

                        if (dateStr != null && !dateStr.isEmpty()) {

                            LocalDate lastDate =
                                    LocalDate.parse(dateStr, formatter);

                            tool.setLastCalibrationDate(lastDate);

                            if (tool.getCalibrationPeriodMonths() != null) {

                                tool.setNextCalibrationDate(
                                        lastDate.plusMonths(
                                                tool.getCalibrationPeriodMonths()
                                        )
                                );
                            }
                        }
                    }

                    tool.setRemark(getString(row.getCell(12)));

                    tool.setCreatedBy("System");
                    tool.setLastBorrowedBy(null);
                    tool.setCreatedAt(LocalDateTime.now());

                    toolList.add(tool);

                    success++;

                } catch (Exception e) {
                    failed++;
                    System.out.println("ROW FAILED AT INDEX " + i);
                    e.printStackTrace();
                }
            }

            toolRepository.saveAll(toolList);

            workbook.close();

            return new ExcelResponse(
                    total, success, failed, duplicate,
                    "Excel uploaded successfully"
            );

        } catch (Exception e) {

            return new ExcelResponse(
                    0, 0, 0, 0,
                    "Error while processing file"
            );
        }
    }

    private String getString(Cell cell) {

        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC) {

            if (cell.getNumericCellValue() % 1 == 0) {
                return String.valueOf((long) cell.getNumericCellValue());
            }

            return String.valueOf(cell.getNumericCellValue());
        }

        return cell.getStringCellValue().trim();
    }

    private String getCondition(Row row) {

        try {
            Cell c6 = row.getCell(6);
            if (c6 != null && c6.getCellType() == CellType.NUMERIC
                    && c6.getNumericCellValue() == 1) {
                return "GOOD";
            }

            Cell c7 = row.getCell(7);
            if (c7 != null && c7.getCellType() == CellType.NUMERIC
                    && c7.getNumericCellValue() == 1) {
                return "DAMAGED";
            }

            Cell c8 = row.getCell(8);
            if (c8 != null && c8.getCellType() == CellType.NUMERIC
                    && c8.getNumericCellValue() == 1) {
                return "MISSING";
            }

            Cell c9 = row.getCell(9);
            if (c9 != null && c9.getCellType() == CellType.NUMERIC
                    && c9.getNumericCellValue() == 1) {
                return "OBSOLETE";
            }

        } catch (Exception e) {
            return "GOOD";
        }

        return "GOOD";
    }
}
