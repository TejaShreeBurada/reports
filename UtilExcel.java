/**
 * 
 */
package com.cavium.forecast.util;


import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * @author nburada
 *
 */
public class UtilExcel {

	HSSFWorkbook workBook;
	HSSFFont cavFont;
	HSSFFont Headerfont;
	HSSFFont font;
	
	HSSFCellStyle data;
	HSSFCellStyle Distdata;
	HSSFCellStyle itemData;
	HSSFCellStyle tableLeftBorder;
	HSSFCellStyle tableRightBorder;
	HSSFCellStyle header;
	HSSFCellStyle cavium ;
	Color c;

	public int getPicIndex(HSSFWorkbook wb,String pathname){
		int index = -1;
		try {
			byte[] picData    = null;
			File pic=new File(pathname);
			long length       = pic.length(  );
			picData           = new byte[ ( int ) length ];
			FileInputStream picIn = new FileInputStream( pic );
			picIn.read( picData );
			index             = wb.addPicture( picData, HSSFWorkbook.PICTURE_TYPE_JPEG );
		} catch (IOException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
		} 
		return index;
	}


	public HSSFCellStyle header()  {
		header = workBook.createCellStyle();
		header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		header.setFillForegroundColor(HSSFColor.BLUE.index);
		header.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		header.setFont(Headerfont);
		header.setWrapText(false);
		return header;
	}
	
	

	public HSSFCellStyle cavium()  {
		cavium = workBook.createCellStyle();
		cavium.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cavium.setFillForegroundColor(HSSFColor.BLUE.index);
		cavium.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cavium.setFont(cavFont);
		cavium.setWrapText(false);
		return cavium;
	}

	public HSSFCellStyle data(){
	data = workBook.createCellStyle();
	data.setAlignment(HSSFCellStyle.ALIGN_LEFT);
	data.setFillForegroundColor(HSSFColor.WHITE.index);
	data.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	data.setFont(font);
	data.setLocked(false);
	data.setWrapText(false);
	return data;
	}
	
	public HSSFCellStyle itemData(){
		itemData = workBook.createCellStyle();
		itemData.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		itemData.setFillForegroundColor(HSSFColor.WHITE.index);
		itemData.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		itemData.setFont(font);
		itemData.setLocked(false);
		itemData.setWrapText(false);
		return data;
		}
	
	public HSSFCellStyle distData(){
		Distdata = workBook.createCellStyle();
		Distdata.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Distdata.setFillForegroundColor(HSSFColor.WHITE.index);
		Distdata.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		Distdata.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
		Distdata.setLocked(false);
		Distdata.setFont(font);
		Distdata.setWrapText(false);
		return data;
		}
	
	
	public HSSFCellStyle tableLeftBorder(){
		tableLeftBorder = workBook.createCellStyle();
		tableLeftBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		tableLeftBorder.setFillForegroundColor(HSSFColor.WHITE.index);
		tableLeftBorder.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		tableLeftBorder.setBorderLeft((short)25);
		tableLeftBorder.setLeftBorderColor(HSSFColor.BLUE.index);
		tableLeftBorder.setFont(font);
		tableLeftBorder.setWrapText(false);
		return tableLeftBorder;
		}
	
	public HSSFCellStyle tableRightBorder(){
		tableRightBorder = workBook.createCellStyle();
		tableRightBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		tableRightBorder.setFillForegroundColor(HSSFColor.WHITE.index);
		tableRightBorder.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		tableRightBorder.setBorderRight((short)25);
		tableRightBorder.setRightBorderColor(HSSFColor.BLUE.index);
		tableRightBorder.setFont(font);
		tableRightBorder.setWrapText(false);
		return tableRightBorder;
		}
	
	/*HSSFPalette palette = wb.getCustomPalette();
	//replacing the standard red with freebsd.org red
	palette.setColorAtIndex(HSSFColor.RED.index,
	(byte) 153, //RGB red (0-255)
	(byte) 0, //RGB green
	(byte) 0 //RGB blue
	);
	//replacing lime with freebsd.org gold
	palette.setColorAtIndex(HSSFColor.LIME.index, (byte) 255, (byte) 204, (byte) 102);*/

	public UtilExcel(HSSFWorkbook workBook) {
		this.workBook=workBook;	

		font = workBook.createFont();
		font.setColor(HSSFColor.BLUE.index);

		Headerfont= workBook.createFont();
		Headerfont.setColor(HSSFColor.WHITE.index);

		cavFont = workBook.createFont();
		cavFont.setFontHeightInPoints((short) 12);
		cavFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cavFont.setColor(HSSFColor.WHITE.index);
		cavFont.setBoldweight((short)100);

	}

}
