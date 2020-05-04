package org.triniti.wbhf;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.xssf.usermodel.*;
import org.triniti.wbhf.utils.*;



public class ModifyExcelSheet {
	
	public String lotId="";
	public String lotDate="";
	public String wbhflocation;
	public float T1WAFERS_LITMIT=340;
	public float NT1WAFERS_LITMIT=65;
	public String ProductType;
	public double d1=0.0;
	public Date d=null;
	public SimpleDateFormat s=null;
	Map<String, String> excelData;
	//extraxt the data from excel 
	public  String modifyExcelData(String path,String fileName,String SubFamily,String FabLot) throws Exception {
		String newFile = "";
		try{
			newFile = writeFile(path,new File(path+"\\"+fileName)); 
			int sheetNo = 0;
			if(newFile.toLowerCase().endsWith("xlsx")){
				Workbook workBook = new XSSFWorkbook(new FileInputStream(newFile));
				workBook.removeSheetAt(2);
				sheetNo=workBook.getSheetIndex("Reg_VT Data");
				System.out.println("Sheet No :"+sheetNo);
				
				excelData=this.ReadExcel(workBook,1);
				workBook=createTabWBHF(workBook,excelData);
				writeWorkBook(newFile,workBook);
			}else if(newFile.toLowerCase().endsWith("xls")){
				Workbook workBook = new HSSFWorkbook(new FileInputStream(newFile));
				workBook.removeSheetAt(2);
				
				sheetNo=workBook.getSheetIndex("Reg_VT Data");
				System.out.println("Sheet No :"+sheetNo);
				
				excelData=this.ReadExcel(workBook,1);
				workBook=createTabWBHF(workBook,excelData);
				writeWorkBook(newFile,workBook);
			}
		}catch(Exception e){
			throw e;
		}
		return newFile;
	}
	
	//Read Data from excel using POI API
	public Map<String, String> ReadExcel(Workbook workBook,int sheetNo) throws Exception{
		ArrayList<String> Key=new ArrayList<String>();
		Map<String, String> hm=null;
		Row exlRow=null;
		Cell cell=null;
		try{
			String headerMessage=Resource.getMessage("Columns");
			//info("headerMessage from config : "+headerMessage);
			String[] header3 =headerMessage.split(":");
			
			Sheet mySheet = workBook.getSheetAt(sheetNo);
			Iterator<Row> rowIter = mySheet.rowIterator();

			boolean ignoreExcel=true;
			boolean excelHeaders=false;
			String cellValue="";
			int countBlankLines=0;
			while (rowIter.hasNext()) {
				exlRow = (Row) rowIter.next();
				excelHeaders = false;
				if(hm==null) hm=new LinkedHashMap<String, String>();
				
				System.out.println();
				
				for(int index=0;index < exlRow.getLastCellNum(); index++) {
					cell = exlRow.getCell(index,Row.CREATE_NULL_AS_BLANK);
					cellValue=this.getCellType_bkp(cell).trim();
					
					System.out.print(cellValue+"\t");
					
					if(cellValue!=null && !"".equals(cellValue)){
						//check weather reading row is excel Header are not. i.e., comparing Wafer#=header3[0] 
						if(cellValue!=null && !cellValue.equals("") && (cellValue.equalsIgnoreCase(header3[0]) || cellValue.equalsIgnoreCase("WAFER") || cellValue.indexOf("WAFER")!=-1)){
							ignoreExcel=false;
							excelHeaders=true;
							cellValue="WAFER";
						}
						if(!ignoreExcel && excelHeaders && headerMessage.lastIndexOf(cellValue.toUpperCase())!=-1 ){
							Key.add(cellValue.toUpperCase());
						}else if (!ignoreExcel && !excelHeaders && Key.size()>index){
							    if(index==0 && Key.toString().indexOf(cellValue)==-1)
								hm.put(Double.valueOf(cellValue).intValue()+"",(cell.getRowIndex()+1)+"");
							    else
							    hm.put(Key.get(1),Double.valueOf(cellValue).intValue()+"");
								countBlankLines=0;
						}
					}else if("".equals(cellValue) && index==0){
						countBlankLines++;
						if(countBlankLines>5)
						break;
					}
				}//for loop
				
				System.out.println("Key : "+Key.toString());
				
			}
			//info("data read from excel : "+((hm!=null && hm.size()>0)?hm.toString():"null"));
		}catch(Exception err){
			String errorMessage=err.getMessage();
			errorMessage+=" at - <span style=\"color:red;\">"+"(Row,Column)->("+exlRow.getRowNum()+","+cell.getColumnIndex()+")</span><BR><BR>";
			Logger.logExceptionMessage(err);
			Logger.logDebugMessage(ModifyExcelSheet.class.getName(), errorMessage);
			//UserNotifier.notifyException(err);
			//System.exit(0);
			throw err;
		}
		return hm;
	}
	
private Workbook createTabWBHF(Workbook workBook,Map<String, String> mp) {
	//get Headers from property file
	String headerMessage=Resource.getMessage("WBHF");
	String[] header1 =headerMessage.split(":");
	
	
	String Formulas=Resource.getMessage("Formula");
	String[] Formula =Formulas.split(";");
	
	int rowNum=0;
	int columnNum = 0;
	Row row=null;
	Cell cell=null;	
	
	//create sheet
	Sheet sheet = workBook.createSheet("WBHF");
	
	//Row0
	row = sheet.createRow(rowNum++);
	addLinesHeader(workBook,header1, row.getRowNum(), 0, sheet);
	CellStyle betaStyle = getStyle(workBook,false, null, null,"0.00");
	
	List keyList = new ArrayList(mp.keySet());
	int site=Integer.valueOf(mp.get("SITE")).intValue();
	//Row1
	StringBuffer strFormula= null;
	int getIndex = 0;
	for (Object object : keyList) {
		String key=object.toString();
		if(!key.equals("SITE")){
			row=sheet.createRow(rowNum++);
			columnNum=0;
			for (int i = 0; i < Formula.length; i++) {
				cell = row.createCell(columnNum++);
				if(!"".equals(Formula[i])){
					strFormula= new StringBuffer(Formula[i]);
					getIndex = strFormula.toString().indexOf("$");
					strFormula=strFormula.replace(getIndex, getIndex+1,(Integer.valueOf(mp.get(key)).intValue()-(site-1))+"");
					getIndex = strFormula.toString().lastIndexOf("$");
					strFormula=strFormula.replace(getIndex, getIndex+1, mp.get(key));
					//info("strFormula :"+strFormula.toString());
					cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
					//cell.setCellStyle(betaStyle);
					cell.setCellFormula(strFormula.toString().replaceAll("SITE", mp.get("SITE")));
				}else{
					//cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
					//cell.setCellStyle(betaStyle);
					cell.setCellValue(key);
				}
			}
		}
	}
	return workBook;
}

	
	
	/**
	 * Write excel data into files as a outputstream 
	 */
	public  void writeWorkBook(String wbhflocation,Workbook workBook)throws Exception {
		FileOutputStream fileOut=null;
		try {
			fileOut = new FileOutputStream(new File(wbhflocation));
			workBook.write(fileOut);
		}  finally {
			fileOut.close();
		}
	}
	
	public String writeFile(String fileLocation,File file) throws Exception{
		String location = fileLocation +"\\"+Resource.getMessage("FILE_CREATED_WITH")+" "+file.getName();
		try {
			info("New modified file location :"+location);
			FileOutputStream fileOut=new FileOutputStream(location);
			FileInputStream fin=new FileInputStream(file);
			 int c;
	         while ((c = fin.read()) != -1) 
	         {
	        	 fileOut.write(c);
	         }
	         fin.close();
	         fileOut.close();
	      
		} catch (Exception e) {
			throw e;
		}
		return   location;
	}
	
	
	public  void addCell(Row row, int cellIndex, HSSFCellStyle style,
			Float value) {
		Cell cell = row.createCell(cellIndex);
		if(style!=null){
			cell.setCellStyle(style);
		}
		cell.setCellValue(value);
	}

	public  Cell setCell(Row row, int cellIndex, HSSFCellStyle style,double e) {
		Cell cell = row.createCell(cellIndex);
		if(style!=null){
			cell.setCellStyle(style);
		}
		cell.setCellValue(e);
		return cell;
	}

	public  CellStyle getStyle(Workbook workBook,boolean bold,HSSFColor backGroundColor, HSSFColor color,String dataFormat) {
		CellStyle style = workBook.createCellStyle();
		Font font = workBook.createFont();
		if (bold) {
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		}
		if (color != null) {
			font.setColor(color.getIndex());
		}
		if (backGroundColor != null) {
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			style.setFillForegroundColor(backGroundColor.getIndex());
		}
		if(dataFormat!=null){
			DataFormat dataFormatStyle = workBook.createDataFormat();			
			style.setDataFormat(dataFormatStyle.getFormat(dataFormat));
		}
		style.setFont(font);
		return style;
	}
	
	public  void addLinesHeader(Workbook workBook,String headerArray[], int sheetRowNum,
			int fromSheetCol, Sheet sheet) {
		Row row = sheet.createRow(sheetRowNum);
		for (int i = 0; i < headerArray.length; i++) {
			Cell cell = row.createCell(fromSheetCol);
			cell.setCellStyle(getStyle(workBook,true, null, null,null));
			cell.setCellValue(headerArray[i]);
			fromSheetCol++;
		}
	}
	
	public String getCellType_new(CellValue cellValue){
		String str="";
		switch (cellValue.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			str=cellValue.getBooleanValue()+"";
			break;
		case Cell.CELL_TYPE_NUMERIC:
			str=cellValue.getNumberValue()+"";
			break;
		case Cell.CELL_TYPE_STRING:
			str=cellValue.getStringValue()+"";
			break;
		case Cell.CELL_TYPE_BLANK:
			str="";
			break;
		case Cell.CELL_TYPE_ERROR:
			str=cellValue.getErrorValue()+"";
			break;
			// CELL_TYPE_FORMULA will never happen
		case Cell.CELL_TYPE_FORMULA:
			break;
		}
		return str;   
	}

	public String getCellType_bkp(Cell cell){
		String str="";
		if(cell!=null)
			switch(cell.getCellType()) {
			case  Cell.CELL_TYPE_NUMERIC : {
				if(HSSFDateUtil.isCellDateFormatted(cell)){
					d1 = cell.getNumericCellValue();
					d = HSSFDateUtil.getJavaDate(d1);
					s = new SimpleDateFormat("MM/dd/yyyy");
					str=s.format(d);
				}else{
					str=cell.getNumericCellValue()+"";
				}	
				break;
			}
			case Cell.CELL_TYPE_FORMULA :
				 switch(cell.getCachedFormulaResultType()) {
		            case Cell.CELL_TYPE_NUMERIC:
		            	str= cell.getNumericCellValue()+"";
		                break;
		            case Cell.CELL_TYPE_STRING:
		            	str= cell.getRichStringCellValue() +"";
		                break;
		        }
				break;  
			case  Cell.CELL_TYPE_BOOLEAN  : 
				str=cell.getBooleanCellValue()+"";
				break;
			case Cell.CELL_TYPE_ERROR  : 
				str=""+cell.getErrorCellValue();
				break;
			case Cell.CELL_TYPE_STRING : 
				str=""+cell.getStringCellValue();
				break ;
			case Cell.CELL_TYPE_BLANK : 
				str="";
				break ;	
			default :
				str="";
				break ;
			} //switch

		return str;
	}

	
	public String getCellType(Cell cell){
		String str="";
		if(cell!=null)
		switch(cell.getCellType()) {
		case  Cell.CELL_TYPE_NUMERIC : {
				if(HSSFDateUtil.isCellDateFormatted(cell)){
					 double d1 = cell.getNumericCellValue();
					 Date d = HSSFDateUtil.getJavaDate(d1);
					 SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy");
					 str=s.format(d);
				}else{
					
					    if(cell.getCellType() == cell.CELL_TYPE_NUMERIC) {
							int i = (int)cell.getNumericCellValue();
							str = String.valueOf(i);
						} else {
							str = cell.toString();
						} 
					
					//str=cell.getNumericCellValue()+"";
				}	
			 break;
		}
		case Cell.CELL_TYPE_FORMULA  :
			str=""+cell.getCellFormula(); 
			break;  
		case  Cell.CELL_TYPE_BOOLEAN  : 
			str=cell.getBooleanCellValue()+"";
			break;
		case Cell.CELL_TYPE_ERROR  : 
			str=""+cell.getErrorCellValue();
			break;
		case Cell.CELL_TYPE_STRING : 
			str=""+cell.getStringCellValue();
			break ;
		case Cell.CELL_TYPE_BLANK : 
			str="";
			break ;	
		default :
			str="";
			break ;
		} //switch
	
		return str;
	}
	
	public Cell setCellType(Cell cell1,Cell cell){
		if(cell!=null)
			switch(cell.getCellType()) {
			case  Cell.CELL_TYPE_NUMERIC : {
				if(HSSFDateUtil.isCellDateFormatted(cell)){
					d1 = cell.getNumericCellValue();
					d = HSSFDateUtil.getJavaDate(d1);
					s = new SimpleDateFormat("MM/dd/yyyy");
					cell1.setCellValue(s.format(d));
				}else{
					cell1.setCellValue(cell.getNumericCellValue());
				}	
				break;
			}
			case Cell.CELL_TYPE_FORMULA  :
				cell1.setCellValue(cell.getCellFormula()); 
				break;  
			case  Cell.CELL_TYPE_BOOLEAN  : 
				cell1.setCellValue(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_ERROR  : 
				cell1.setCellValue(cell.getErrorCellValue());
				break;
			case Cell.CELL_TYPE_STRING : 
				cell1.setCellValue(cell.getStringCellValue());
				break ;
			case Cell.CELL_TYPE_BLANK : 
				//cell.setCellValue("");
				break ;	
			default :
				//cell.setCellValue("");
				break ;
			} //switch

		return cell1;
	}

	private Cell setCellValueToDecimal(String fileLocation,Row row,Cell cell, String value) throws Exception {
		try{
			s = new SimpleDateFormat("MM/dd/yyyy");
			cell.setCellValue(s.format(Date.parse(value.trim())));
		}catch (Exception ex) {
			try {
				double d = Double.parseDouble(value.trim());
				cell.setCellValue(d);
			} catch (Exception e) {
				try{
					int d = Integer.parseInt(value.trim());
					cell.setCellValue(d);
				}catch (Exception err) {
					if(!value.equals("null")){
						cell.setCellValue(new HSSFRichTextString(value));
					}else{
						String errorMessage=err.getMessage();
						errorMessage+=" at - <span style=\"color:blue;\">"+"<BR>(Row,Column)->("+row.getRowNum()+","+cell.getColumnIndex()+")</span><BR><BR>";
						throw new Exception(errorMessage);
					}
					//cell.setCellValue(!value.equals("null")?new HSSFRichTextString(value):new HSSFRichTextString(""));
				}
			}
		}
		//cell.setCellValue(new HSSFRichTextString(value));
		return cell;
	}

	
	public static void info(String message){
		Logger.logNormalMessage(ModifyWBHFFiles.class.getName(), message);
	}
	
	public static void error(Object exe){
		Logger.logDebugMessage(ModifyWBHFFiles.class.getName(), exe);
	}


}
