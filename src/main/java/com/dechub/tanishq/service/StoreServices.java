package com.dechub.tanishq.service;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
public class StoreServices implements CommandLineRunner {



    @Value("${store.details.excel.sheet}")
    private String storeDetailSheet;

    @Value("${google.sheet.dechub.store.details.id}")
    private String storeSheetId;

    @Value("${system.isWindows}")
    private String isWindows;

    @Value("${selfie.upload.dir}")
    private String selfieDirectory;


    private Map<String, List<String>> storeDetails;



    @Override
    public void run(String... args) throws Exception {
        transformToData();
        //createDirectoryForDivision();
    }


    private void createDirectoryForDivision(){
        try{
            File file = new File(selfieDirectory);


            for(Map.Entry<String, List<String>> entry : storeDetails.entrySet()){
                String divisionName = entry.getKey();
                boolean isPresent = checkDivisionDirectoryPresent(divisionName, file);
                if(!isPresent){
                    File newFile = new File(isWindows.equalsIgnoreCase("Y") ? selfieDirectory + "\\" + divisionName : selfieDirectory + "/" + divisionName);
                    if(!newFile.exists()){
                        boolean created = newFile.mkdirs();
                        if(!created){
                            throw new RuntimeException("unable to create directory");
                        }
                    }
                }
            }
            System.out.println("Directory creation success");


        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error creating directory for divisions");
        }
    }

    private void transformToData() {
        try{
            List<List<String>> data = this.readExcel(storeDetailSheet);
            storeDetails = new HashMap<>();
            for(List<String> i : data){
                if(!storeDetails.containsKey(i.get(1))){
                    storeDetails.put(i.get(1), new ArrayList<>());
                }
                List<String> tempVar = storeDetails.get(i.get(1));
                tempVar.add(i.get(0));
                storeDetails.put(i.get(1), tempVar);
            }

            System.out.println("details fetched from excel total division: " + storeDetails.size());

        }catch (Exception e){
            throw new RuntimeException("Unable to read store details from excel");
        }

    }
    private void getDetailofStore(String storeCode) {
        try{
            List<List<String>> data = this.readExcel(storeDetailSheet);
            storeDetails = new HashMap<>();
            for(List<String> i : data){
                if(!storeDetails.containsKey(i.get(1))){
                    storeDetails.put(i.get(1), new ArrayList<>());
                }
                List<String> tempVar = storeDetails.get(i.get(1));
                tempVar.add(i.get(0));
                storeDetails.put(i.get(1), tempVar);
            }

            System.out.println("details fetched from excel total division: " + storeDetails.size());

        }catch (Exception e){
            throw new RuntimeException("Unable to read store details from excel");
        }

    }

    private List<List<String>> readExcel(String filePath) throws IOException {
        List<List<String>> excelData = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(new File(filePath)))) {
            Sheet sheet = workbook.getSheetAt(1);

            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.iterator();

                List<String> rowData = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String tempVar = getCellValue(cell);
                    String removedWhiteSpaces = StringUtils.deleteWhitespace(tempVar);
                    rowData.add(removedWhiteSpaces);
                }

                excelData.add(rowData);
            }
        }

        return excelData;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().toUpperCase();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).toUpperCase();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).toUpperCase();
            case FORMULA:
                return String.valueOf(cell.getCellFormula()).toUpperCase();
            default:
                return "";
        }
    }

    public boolean checkDivisionDirectoryPresent(String divisionName, File rootFolder){
        boolean result = false;
        for(File i : rootFolder.listFiles()){
            if(i.isDirectory()){
                i.getName();
                if(i.getName().equals(divisionName)){
                    result =  true;
                    break;
                }
            }
        }
        return result;
    }


    public Map<String, List<String>> getStoreDetails() {
        return storeDetails;
    }


}
