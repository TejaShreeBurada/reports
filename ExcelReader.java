/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cavium.forecast.beans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 * @author nappana
 */
public class ExcelReader {

      public void saveExcelinDB(String path) throws FileNotFoundException, IOException {

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            System.out.println("File not found in the specified path.");
            e.printStackTrace();
        }
        FileOutputStream fileOut1 = new FileOutputStream("D:\\temp.xls");
        int nextChar;
        while ((nextChar = inputStream.read()) != -1) {
            fileOut1.write((char) nextChar);
        }
        fileOut1.write('\n');
        fileOut1.flush();


        POIFSFileSystem fileSystem = null;


        int i = 0;
        int x = 0;


        try {
            fileSystem = new POIFSFileSystem(inputStream);

            System.out.println("PIO Object is   " + fileSystem);

            HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
            HSSFSheet sheet = workBook.getSheetAt(0);
            Iterator rows = sheet.rowIterator();
            //    HSSFRow row = sheet.getRow(1);
            rows.next();
            while (rows.hasNext()) {

                HSSFRow row = (HSSFRow) rows.next();

                try {
                    String query = " INSERT INTO  dist ( SNo, farmername, Father_Name, Owner_Tanent, Social_Status, Village, Mandal, District, Crop_Name, DamagedArea_Above_50, DamagedArea_Below_50, "
                            + " DamagedArea_Total, DamagedArea_SFMF, DamagedArea_Other_SFMF, Total, Relief_scale, Input_Subsidy_required_SFMF, others, Total_SFmf, SFMF_affected, "
                            + " Aother_farmers_affected, Total_affeted, Bank_name, bank_branch, account_Num, servey_no,calamity,season,dtls_YEAR) VALUES ( ";

                    if (row.getCell(0) != null) {
                        query = query + row.getCell(0).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(1) != null) {
                        query = query + " '" + row.getCell(1).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(2) != null) {
                        query = query + " '" + row.getCell(2).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(3) != null) {
                        query = query + " '" + row.getCell(3).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(4) != null) {
                        query = query + " '" + row.getCell(4).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(5) != null) {
                        query = query + " '" + row.getCell(5).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(6) != null) {
                        query = query + " '" + row.getCell(6).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(7) != null) {
                        query = query + " '" + row.getCell(7).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(8) != null) {
                        query = query + " '" + row.getCell(8).toString().replaceAll("'", "''") + "', ";
                    } else {
                        query = query + " ' ', ";
                    }
                    if (row.getCell(9) != null) {
                        query = query + row.getCell(9).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(10) != null) {
                        query = query + row.getCell(10).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(11) != null) {
                        query = query + row.getCell(11).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(12) != null) {
                        query = query + row.getCell(12).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(13) != null) {
                        query = query + row.getCell(13).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
//                    if (row.getCell(14) != null) {
//                       query = query + row.getCell(14).toString().replaceAll("'", "''") + ",";
//                    } else {
//                        query = query + "  ,";
//                    }
                    if (row.getCell(12) != null && row.getCell(13) != null) {
                        float value = Float.parseFloat(row.getCell(12).toString()) + Float.parseFloat(row.getCell(13).toString());
                        query = query + value + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(15) != null) {
                        query = query + row.getCell(15).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(16) != null) {
                        query = query + row.getCell(16).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(17) != null) {
                        query = query + row.getCell(17).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
//                    if (row.getCell(18) != null) {
//                        query = query + row.getCell(18).toString().replaceAll("'", "''") + ",";
//                    } else {
//                        query = query + "  ,";
//                    }
                    if (row.getCell(16) != null && row.getCell(17) != null) {

                        System.out.println("Formula is " + row.getCell(18));


                        float value = Float.parseFloat(row.getCell(16).toString()) + Float.parseFloat(row.getCell(17).toString());
                        query = query + value + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(19) != null) {
                        query = query + row.getCell(19).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(20) != null) {
                        query = query + row.getCell(20).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(21) != null) {
                        query = query + row.getCell(21).toString().replaceAll("'", "''") + ",";
                    } else {
                        query = query + "  ,";
                    }
                    if (row.getCell(23) != null) {
                        query = query + " '" + row.getCell(23).toString().replaceAll("'", "''") + "',";
                    } else {
                        query = query + " ' ' ,";
                    }
                    if (row.getCell(24) != null) {
                        query = query + " '" + row.getCell(24).toString().replaceAll("'", "''") + "',";
                    } else {
                        query = query + " '' ,";
                    }
                    if (row.getCell(22) != null) {
                        query = query + " '" + row.getCell(22).toString().replaceAll("'", "''") + "',";
                    } else {
                        query = query + " '' ,";
                    }
                    if (row.getCell(25) != null) {
                        query = query + " '" + row.getCell(25).toString().replaceAll("'", "''") + "',";
                    } else {
                        query = query + " '', ";
                    }
                    query = query + " ); ";



                    System.out.println("Query is -------------------------  " + query + "\n");

                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                x++;
            }

            System.out.println("No of insertet queries are   --------   " + i);

        } catch (Exception e) {
            System.out.println("Exception in excel save method");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {


        ExcelReader excelReader = new ExcelReader();
        String xlsPath = "D:\\Cavium_Standard-08-Feb-2012.xls";
        excelReader.saveExcelinDB(xlsPath);

    }
}
