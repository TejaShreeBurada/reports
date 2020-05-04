package com.cavium.forecast.helpers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.hssf.util.HSSFColor;

import org.json.simple.JSONObject;
import com.cavium.forecast.logger.Logger;
import com.cavium.forecast.main.PriceListHandler;
import com.cavium.forecast.main.Users;
import com.cavium.forecast.util.Encrypt;

public class PriceListExcelHelper {
	public Users users = null;
	public PriceListHandler priceListHandler = null;
	private PriceListHelper priceListHelper = null;
	private Encrypt encrypt = null;
	private static String className = PriceListExcelHelper.class.getName();

	public PriceListExcelHelper() throws Exception {
		this.priceListHandler = new PriceListHandler();
		this.users = new Users();
		this.priceListHelper = new PriceListHelper();
		this.encrypt = new Encrypt();
	}

	public HSSFWorkbook generatePriceListExcel(String priceList,String productFamily, String productSubFamily/*, String customerpart*/) {
		int postPricesIndex = 0;
		int baseRangeCount = 0;
		String temp = "";
		HSSFWorkbook workBook=null;
		// SecretKeySpec priceKey = "";
		try {
			log("in excel file generation");
			JSONObject obj = this.priceListHelper.getMaxRangeCount(priceList,productFamily, productSubFamily);

			log("rangeCount ::::::::" + obj.get("rangeValue"));
			int rangeCount = ((Integer) obj.get("rangeValue")).intValue();

			log("range count :::::::" + rangeCount);
			String priceListName = this.priceListHandler
					.getPriceListName(priceList);
			
			String fileUploadId = this.priceListHandler.getNextUploadFileId(); 		
			
			//Expot Date View
			
			ArrayList exportdata = this.priceListHandler
					.getPriceListTemplateExportData(rangeCount, priceList,
							productFamily, productSubFamily);
			ArrayList date = this.priceListHandler.getMaxPriceListLastUpdateDate(priceList);
			
			String lastUploadTimeStamp ="";
			if(date.size()>0){
			lastUploadTimeStamp = (String)date.get(1);
			}
			
			log("Last Excel Uploded TimeStamp :::::::: for verification any changes to excel" + lastUploadTimeStamp);
			String encryptPriceListId = this.encrypt.getHash(priceList);

			workBook = new HSSFWorkbook();
			HSSFSheet spreadSheet = workBook.createSheet("PriceList Export");
			spreadSheet.createFreezePane(0, 3);

			// Create new colors
			Color lightPinkColor = new Color(252, 189, 255);
			HSSFPalette palette = workBook.getCustomPalette();
			short lightPinkColorIndex = 10;
			palette.setColorAtIndex(lightPinkColorIndex,
					(byte) lightPinkColor.getRed(),
					(byte) lightPinkColor.getGreen(),
					(byte) lightPinkColor.getBlue());

			Color veryLightYellowColor = new Color(255, 254, 223);
			palette = workBook.getCustomPalette();
			short veryLightYellowColorIndex = 11;
			palette.setColorAtIndex(veryLightYellowColorIndex,
					(byte) veryLightYellowColor.getRed(),
					(byte) veryLightYellowColor.getGreen(),
					(byte) veryLightYellowColor.getBlue());

			Color veryLightGreyColor = new Color(245, 245, 245);
			palette = workBook.getCustomPalette();
			short veryLightGreyColorIndex = 12;
			palette.setColorAtIndex(veryLightGreyColorIndex,
					(byte) veryLightGreyColor.getRed(),
					(byte) veryLightGreyColor.getGreen(),
					(byte) veryLightGreyColor.getBlue());

			// Get Styles
			
			HSSFFont font_normal = workBook.createFont();
			font_normal.setFontName(HSSFFont.FONT_ARIAL);
			font_normal.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			
			HSSFFont font_bold = workBook.createFont();
			font_bold.setFontName(HSSFFont.FONT_ARIAL);
			font_bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			HSSFFont font_info = workBook.createFont();
			font_info.setFontName(HSSFFont.FONT_ARIAL);
			font_info.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			font_info.setColor(HSSFColor.INDIGO.index);
			HSSFCellStyle headerStyle = setHeaderStyle(workBook, font_bold);
			
			HSSFCellStyle priceEntryStyle = setPriceEntryStyle(workBook, font_normal);
			
			HSSFCellStyle priceDisabledStyle = setPriceDisabledStyle(workBook,
					font_normal, veryLightGreyColorIndex);
			
			HSSFCellStyle dateEntryStyle = setDateEntryStyle(workBook, font_normal);
			
			HSSFCellStyle dateDisabledStyle = setDateDisabledStyle(workBook,
					font_normal, veryLightGreyColorIndex);
			
			HSSFCellStyle dateOptionalStyle = setDateOptionalStyle(workBook,
					font_normal, veryLightYellowColorIndex);
			
			HSSFCellStyle textOptionalStyle = setTextOptionalStyle(workBook,
					font_normal, veryLightYellowColorIndex);
			
			HSSFCellStyle textDisabledStyle = setTextDisabledStyle(workBook,
					font_normal, veryLightGreyColorIndex);
			
			HSSFCellStyle xStyle = setXStyle(workBook, font_normal,
					veryLightYellowColorIndex);
			
			HSSFCellStyle xEntryStyle = setXEntryStyle(workBook, font_normal);
			
			HSSFCellStyle infoStyle = setInfoStyle(workBook, font_info);
			
			HSSFCellStyle iStyle = setIStyle(workBook, font_normal);
			
			HSSFCellStyle rStyle = setRStyle(workBook, font_normal,
					lightPinkColorIndex);
			
			HSSFCellStyle nStyle = setNStyle(workBook, font_normal);
			
			HSSFCellStyle hiddenStyle = setHiddenStyle(workBook);

			// First Row
			HSSFRow row = spreadSheet.createRow(0);
			HSSFCell cell = row.createCell(0);
			if (productFamily.equalsIgnoreCase("%")) {
				productFamily = "All";
			}
			if (productSubFamily.equalsIgnoreCase("%")) {
				productSubFamily = "All";
			}
			cell.setCellValue(new HSSFRichTextString("PriceList:"
					+ priceListName + /*"; Exported:" + currentDate +*/ "; Family:" + productFamily
					+ "; SubFamily:" + productSubFamily));

			// Second Row: Hidden
			row = spreadSheet.createRow(1);
			row.setRowStyle(hiddenStyle);  //Not working in POI 3.2
			row.setHeight((short) 0);       //Use this until we are on a newer Java platform that support POI3.7
			cell = row.createCell(0);
			cell.setCellValue(new HSSFRichTextString("Exported:"+ lastUploadTimeStamp + "; Signature:"+ fileUploadId+"/"+encryptPriceListId	+ " (Do not remove.Required for Upload into Oracle!)"));


			// Third Row - Header Row
			row = spreadSheet.createRow(2);
			row.setHeight((short) 500);
			cell = row.createCell(0);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(""));

			cell = row.createCell(1);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Item"));

			cell = row.createCell(2);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Family.Sub-Family"));

			int headerCellIndex = 3;
			for (int index = 1; index <= rangeCount; index++) {
				cell = row.createCell(headerCellIndex);
				cell.setCellStyle(headerStyle);
				cell.setCellValue(new HSSFRichTextString("R" + index + "[USD]"));
				headerCellIndex++;
			}

			cell = row.createCell(headerCellIndex);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Distribution"));

			cell = row.createCell(headerCellIndex + 1);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"Old Start-Date \n[MM/DD/YYYY]"));

			cell = row.createCell(headerCellIndex + 2);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"Old End-Date \n[MM/DD/YYYY]"));

			cell = row.createCell(headerCellIndex + 3);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"New Start-Date \n[MM/DD/YYYY]"));

			cell = row.createCell(headerCellIndex + 4);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"New End-Date \n[MM/DD/YYYY]"));

			cell = row.createCell(headerCellIndex + 5);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("No New \nDesigns[X]"));

			cell = row.createCell(headerCellIndex + 6);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Is EOL [X]"));

			cell = row.createCell(headerCellIndex + 7);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Customer \nParts [X]"));

			cell = row.createCell(headerCellIndex + 8);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"Last EOL Order \n[MM/DD/YYYY]"));
						
			//-------------------------------------------------------------------------
			cell = row.createCell(headerCellIndex + 9);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"Price Substitue Text"));			
			
			
			cell = row.createCell(headerCellIndex + 10);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString(
					"Only update \nDFFs [X]"));

			cell = row.createCell(headerCellIndex + 11);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(new HSSFRichTextString("Comments"));

			log("Header row Created");

			// Price-List Data Rows
			int rowCount = 3;
			String status = null;
			log("exportdata.size()................ :" + exportdata.size());

			String comment = "";

			for (int flag = 0, rang = 0; flag < exportdata.size(); flag++) {

				HashMap exportMap = (HashMap) exportdata.get(flag);
				status = (String) exportMap.get("existsFlag");
				row = spreadSheet.createRow(rowCount);

				HSSFCell cell1 = row.createCell(0);
				cell1.setCellValue(new HSSFRichTextString(status));

				HSSFCell cell2 = row.createCell(1);
				cell2.setCellValue(new HSSFRichTextString((String) exportMap
						.get("itemName")));

				HSSFCell cell3 = row.createCell(2);
				cell3.setCellValue(new HSSFRichTextString((String) exportMap
						.get("productFamily")
						+ "."
						+ (String) exportMap.get("productSubFamily")));

				// Look in Comments and find the last Range (R:X); X is the
				// number of ranges for the current record.
				comment = (String) exportMap.get("comments");
				rang = comment.lastIndexOf('R');
				baseRangeCount = Integer.parseInt(comment.charAt(++rang) + "");

				int cellIndex = 3;
				
				for (int index = 1; index <= rangeCount; index++) {
					cell = row.createCell(cellIndex);		
					if (index <= baseRangeCount) {
						cell.setCellStyle(priceEntryStyle);
					} else {
						cell.setCellStyle(priceDisabledStyle);
					}
					cell = setCellValueToDecimal(cell,(String) exportMap.get("Range" + index));		
					
					
					cellIndex++;
				}

				postPricesIndex = cellIndex;
				HSSFCell cell4 = row.createCell(cellIndex);
				cell4.setCellStyle(priceEntryStyle);
				cell4 = setCellValueToDecimal(cell4,(String) exportMap.get("distribution"));
				
				
				HSSFCell cell5 = row.createCell(cellIndex + 1);
				cell5.setCellStyle(dateDisabledStyle);
				cell5.setCellValue(new HSSFRichTextString((String) exportMap.get("currentPirceStartDate")));

				HSSFCell cell6 = row.createCell(cellIndex + 2);
				cell6.setCellStyle(dateOptionalStyle);
				cell6.setCellValue(new HSSFRichTextString((String) exportMap
						.get("currentPirceEndDate")));

				cell = row.createCell(cellIndex + 3);
				cell.setCellStyle(dateEntryStyle);
				cell.setCellValue(new HSSFRichTextString(""));

				cell = row.createCell(cellIndex + 4);
				cell.setCellStyle(dateOptionalStyle);
				cell.setCellValue(new HSSFRichTextString((String) exportMap
						.get("newPriceEndDate")));

				cell = row.createCell(cellIndex + 5);
				cell.setCellStyle(xStyle);

				temp = (String) exportMap.get("recommendNewDesigns");
				if(temp.equalsIgnoreCase("N")){temp="";}
				cell.setCellValue(new HSSFRichTextString(temp));

				cell = row.createCell(cellIndex + 6);
				cell.setCellStyle(xStyle);
				temp = (String) exportMap.get("isProductEol");
				if(temp.equalsIgnoreCase("N")){temp="";}
				cell.setCellValue(new HSSFRichTextString(temp));

				cell = row.createCell(cellIndex + 7);
				cell.setCellStyle(xStyle);
				temp = (String) exportMap.get("includeCustomerPart");
				if(temp.equalsIgnoreCase("N")){temp="";}
				cell.setCellValue(new HSSFRichTextString(temp));

				cell = row.createCell(cellIndex + 8);
				cell.setCellStyle(dateOptionalStyle);
				cell.setCellValue(new HSSFRichTextString((String) exportMap
						.get("lastDateToOrderEol")));
				
				
				cell = row.createCell(cellIndex + 9);//change index
				cell.setCellStyle(textOptionalStyle);
				cell.setCellValue(new HSSFRichTextString((String) exportMap
						.get("priceLineDff")));
							
				
								cell = row.createCell(cellIndex + 10);
				cell.setCellStyle(xEntryStyle);
				cell.setCellValue(new HSSFRichTextString(""));

				cell = row.createCell(cellIndex + 11);
				cell.setCellStyle(textDisabledStyle);
				cell.setCellValue(new HSSFRichTextString((String) exportMap
						.get("comments")));

				if (status.equalsIgnoreCase("N")) {
					cell1.setCellStyle(nStyle);
					cell2.setCellStyle(nStyle);
					cell3.setCellStyle(nStyle);
				} else if (status.equalsIgnoreCase("I")) {
					cell1.setCellStyle(iStyle);
					cell2.setCellStyle(iStyle);
					cell3.setCellStyle(iStyle);
				} else if (status.equalsIgnoreCase("R")) {
					cell1.setCellStyle(rStyle);
					cell2.setCellStyle(rStyle);
					cell3.setCellStyle(rStyle);
				}

				rowCount++;
			}
			setColumnWidth(spreadSheet, postPricesIndex);

			// Render Info Footer
			rowCount += 2;
			row = spreadSheet.createRow(rowCount++);
			cell = row.createCell(0);
			cell.setCellStyle(iStyle);
			cell.setCellValue(new HSSFRichTextString(
					"I: Item is in PriceList with ranges matching setups for the family"));
			row = spreadSheet.createRow(rowCount++);
			cell = row.createCell(0);
			cell.setCellStyle(rStyle);
			cell.setCellValue(new HSSFRichTextString(
					"R: Item is in PriceList but ranges in price-list differ form ranges in setup"));
			row = spreadSheet.createRow(rowCount++);
			cell = row.createCell(0);
			cell.setCellStyle(nStyle);
			cell.setCellValue(new HSSFRichTextString(
					"N: Item is current not in price-list"));
			
			row = spreadSheet.createRow(++rowCount);
			cell = row.createCell(0);
			cell.setCellStyle(infoStyle);
			cell.setCellValue(new HSSFRichTextString("For new prices to be imported, 'New Start-Date' needs to be entered."));

			row = spreadSheet.createRow(++rowCount);
			cell = row.createCell(0);
			cell.setCellStyle(infoStyle);
			cell.setCellValue(new HSSFRichTextString("To only update DFFs columns('New End-Date','No New Design','Is EOL','Customer Parts','Last EOL' and 'Price Substitue Text') of an existing price-list entry without updating the list-price,column 'Only Update DFFs [X]' needs to bet set to 'X'."));
			
		} catch (Exception exp) {
			String errorMessage = "error occured in creating xsl file in generatePriceListExcel method "
					+ exp;
			Logger.logErrorMessage(className, errorMessage);
			Logger.logExceptionMessage(exp);
		}
		return workBook;
	}

	private HSSFCell setCellValueToDecimal(HSSFCell cell, String value) {
		try {
			if (value != null && !value.equals("")) {
				double d = Double.parseDouble(value);
				cell.setCellValue(d);
			}
		} catch (Exception e) {
			cell.setCellValue(new HSSFRichTextString("NAN"));
		}
		return cell;
	}

	private void setColumnWidth(HSSFSheet spreadSheet, int postPricesIdx) {
		spreadSheet.setColumnWidth(0, 2 * 256);
		spreadSheet.setColumnWidth(1, 28 * 256);
		spreadSheet.setColumnWidth(2, 28 * 256);
		spreadSheet.setColumnWidth(3, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 1, 16 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 2, 16 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 3, 16 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 4, 16 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 5, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 6, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 7, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 8, 16 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 9, 30 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 10, 12 * 256);
		spreadSheet.setColumnWidth(postPricesIdx + 11, 60 * 256);
	}

	private HSSFCellStyle setIStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle iStyle = workBook.createCellStyle();
		iStyle.setFont(font);
		setBorderDottedStyle(iStyle);
		iStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
		iStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		return (iStyle);
	}

	private HSSFCellStyle setRStyle(HSSFWorkbook workBook, HSSFFont font,
			short lightPinkColorIndex) {
		HSSFCellStyle rStyle = workBook.createCellStyle();
		rStyle.setFont(font);
		setBorderDottedStyle(rStyle);
		rStyle.setFillForegroundColor(lightPinkColorIndex);
		rStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		return (rStyle);
	}

	private HSSFCellStyle setXStyle(HSSFWorkbook workBook, HSSFFont font,
			short veryLightYellowColorIndex) {
		HSSFCellStyle xStyle = workBook.createCellStyle();
		xStyle.setFont(font);
		setBorderDottedStyle(xStyle);
		xStyle.setFillForegroundColor(veryLightYellowColorIndex);
		xStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		xStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		xStyle.setLocked(false);
		return (xStyle);
	}
	
	private HSSFCellStyle setXEntryStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle xEntryStyle = workBook.createCellStyle();
		xEntryStyle.setFont(font);
		setBorderDottedStyle(xEntryStyle);
		//--------------------
		//xEntryStyle.getDataFormatString();
		//--------------------
		xEntryStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		xEntryStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		xEntryStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		xEntryStyle.setLocked(false);
		return (xEntryStyle);
	}
	
	private HSSFCellStyle setInfoStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle infoStyle = workBook.createCellStyle();
		infoStyle.setFont(font);
		infoStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		infoStyle.setLocked(false);
		return (infoStyle);
	}

	private HSSFCellStyle setNStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle nStyle = workBook.createCellStyle();
		nStyle.setFont(font);
		setBorderDottedStyle(nStyle);
		nStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		nStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		return (nStyle);
	}

	private HSSFCellStyle setHeaderStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle headerStyle = workBook.createCellStyle();
		headerStyle.setFont(font);
		headerStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		headerStyle.setWrapText(true);
		return (headerStyle);
	}

	private HSSFCellStyle setPriceEntryStyle(HSSFWorkbook workBook,HSSFFont font) {
		HSSFCellStyle priceEntryStyle = workBook.createCellStyle();
		priceEntryStyle.setFont(font);
		priceEntryStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		setBorderDottedStyle(priceEntryStyle);
		priceEntryStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		priceEntryStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		priceEntryStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
		priceEntryStyle.setLocked(false);
		return (priceEntryStyle);
	}

	private HSSFCellStyle setPriceDisabledStyle(HSSFWorkbook workBook,
			HSSFFont font, short veryLightGrayColorIndex) {
		HSSFCellStyle priceDisabledStyle = workBook.createCellStyle();
		priceDisabledStyle.setFont(font);
		setBorderDottedStyle(priceDisabledStyle);
		priceDisabledStyle.setFillForegroundColor(veryLightGrayColorIndex);
		priceDisabledStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		priceDisabledStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
		priceDisabledStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		priceDisabledStyle.setLocked(false);
		return (priceDisabledStyle);
	}

	private HSSFCellStyle setDateEntryStyle(HSSFWorkbook workBook, HSSFFont font) {
		HSSFCellStyle dateEntryStyle = workBook.createCellStyle();
		dateEntryStyle.setFont(font);
		dateEntryStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		setBorderDottedStyle(dateEntryStyle);
		dateEntryStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		dateEntryStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dateEntryStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("MM/DD/YYYY"));
		dateEntryStyle.setLocked(false);
		return (dateEntryStyle);
	}
	
	private HSSFCellStyle setDateDisabledStyle(HSSFWorkbook workBook,
			HSSFFont font, short veryLightGrayColorIndex) {
		HSSFCellStyle dateDisabledStyle = workBook.createCellStyle();
		dateDisabledStyle.setFont(font);
		setBorderDottedStyle(dateDisabledStyle);
		dateDisabledStyle.setFillForegroundColor(veryLightGrayColorIndex);
		dateDisabledStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		dateDisabledStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dateDisabledStyle.setDataFormat(HSSFDataFormat
				.getBuiltinFormat("MM/DD/YYYY"));
		dateDisabledStyle.setLocked(false);
		return (dateDisabledStyle);
	}

	private HSSFCellStyle setDateOptionalStyle(HSSFWorkbook workBook,
			HSSFFont font, short veryLightYellowColorIndex) {
		HSSFCellStyle dateOptionalStyle = workBook.createCellStyle();
		dateOptionalStyle.setFont(font);
		setBorderDottedStyle(dateOptionalStyle);
		dateOptionalStyle.setFillForegroundColor(veryLightYellowColorIndex);
		dateOptionalStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		dateOptionalStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dateOptionalStyle.setDataFormat(HSSFDataFormat
				.getBuiltinFormat("MM/DD/YYYY"));
		dateOptionalStyle.setLocked(false);
		return (dateOptionalStyle);
	}
	
	private HSSFCellStyle setTextOptionalStyle(HSSFWorkbook workBook,
			HSSFFont font, short veryLightYellowColorIndex) {
		HSSFCellStyle textOptionalStyle = workBook.createCellStyle();
		textOptionalStyle.setFont(font);
		setBorderDottedStyle(textOptionalStyle);
		textOptionalStyle.setFillForegroundColor(veryLightYellowColorIndex);
		textOptionalStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		textOptionalStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		textOptionalStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		textOptionalStyle.setLocked(false);
		return (textOptionalStyle);
	}

	private HSSFCellStyle setTextDisabledStyle(HSSFWorkbook workBook,
			HSSFFont font, short veryLightGrayColorIndex) {
		HSSFCellStyle textDisabledStyle = workBook.createCellStyle();
		textDisabledStyle.setFont(font);
		setBorderDottedStyle(textDisabledStyle);
		textDisabledStyle.setFillForegroundColor(veryLightGrayColorIndex);
		textDisabledStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		textDisabledStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		textDisabledStyle.setLocked(false);
		return (textDisabledStyle);
	}
	
	private HSSFCellStyle setHiddenStyle(HSSFWorkbook workBook) {
		HSSFCellStyle hiddenStyle = workBook.createCellStyle();;
		hiddenStyle.setHidden(true);
		return (hiddenStyle);
	}

	private void setBorderDottedStyle(HSSFCellStyle tableStyle) {
		tableStyle.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
		tableStyle.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
		tableStyle.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
		tableStyle.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
	}
	

	public void setNumericConstraints(HSSFSheet spreadSheet, int startRow,
			int endRow, int startCell, int endCell) {
		CellRangeAddressList addressList = new CellRangeAddressList(startRow,
				endRow, 0, 0);
		DVConstraint dvConstraint = DVConstraint.createNumericConstraint(
				DVConstraint.ValidationType.INTEGER,
				DVConstraint.OperatorType.BETWEEN, "0",
				"9999999999999999999999999");
		HSSFDataValidation dataValidation = new HSSFDataValidation(addressList,
				dvConstraint);
		dataValidation.setSuppressDropDownArrow(true);
		spreadSheet.addValidationData(dataValidation);
	}

	public static void log(String message) {
		Logger.logDebugMessage(className, message);
	}

}
