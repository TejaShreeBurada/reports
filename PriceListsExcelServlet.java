package com.cavium.forecast.servlet;

import com.cavium.forecast.helpers.PriceListExcelHelper;
import com.cavium.forecast.helpers.PriceListPDFHelper;
import com.cavium.forecast.helpers.PriceListPDFHelperRegister;

import com.cavium.forecast.logger.Logger;
import com.cavium.forecast.main.PriceListHandler;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import com.cavium.forecast.util.AppsBundle;

public class PriceListsExcelServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		/*
		 * response.setHeader( "Content-Disposition",
		 * "attachment; filename=\"CaviumPriceLIst\"" );
		 * response.setContentType("application/vnd.ms-excel");
		 */
		try {
			PriceListHandler handler = new PriceListHandler();

			ServletContext sc = getServletContext();
			String realPath = sc.getRealPath(AppsBundle.getProperty("logo"));
			Logger.logDebugMessage("PriceListsExcelServlet Get real path::::::::::::",realPath);

			String operation = request.getParameter("operation");
			String priceListId = request.getParameter("priceListId");
			String priceListName = handler.getPriceListName(priceListId);

			String productFamily = "";
			String productSubFamily = "";

			StringTokenizer families = null;
			StringTokenizer subFamilies = null;
			String outputFormat = null;
			String includePriceValues=request.getParameter("includePriceValues") == null ? "": request.getParameter("includePriceValues");
			String customerPart = "";
			if (operation.equalsIgnoreCase("GENERATE_REPORT") || operation.equalsIgnoreCase("GENERATE_EXCEL")) {
				families = new StringTokenizer(request.getParameter("productFamily"), ",");
				subFamilies = new StringTokenizer(request.getParameter("productSubFamily"), ",");

				outputFormat = request.getParameter("outputFormat") == null ? "": request.getParameter("outputFormat");
				customerPart = request.getParameter("customerPart") == null ? "": request.getParameter("customerPart");
				
				/*
				 * int subFamilyTokens = subFamilies.countTokens(); int
				 * subFamilyFlag = 0; while(subFamilies.hasMoreElements()){
				 * subFamilyFlag++; if(subFamilyFlag != subFamilyTokens)
				 * productSubFamily += "'"+subFamilies.nextToken()+"',"; else
				 * productSubFamily += "'"+subFamilies.nextToken()+"'"; }
				 */

				int familyTokens = families.countTokens();
				int flag = 0;
				while (families.hasMoreElements()) {
					flag++;
					if (flag != familyTokens)
						productFamily += "'" + families.nextToken() + "',";
					else
						productFamily += "'" + families.nextToken() + "'";
				}

				int subFamilyTokens = subFamilies.countTokens();
				int subFamilyFlag = 0;
				while (subFamilies.hasMoreElements()) {
					subFamilyFlag++;
					if (subFamilyFlag != subFamilyTokens)
						productSubFamily += "'" + subFamilies.nextToken()
						+ "',";
					else
						productSubFamily += "'" + subFamilies.nextToken() + "'";
				}
			} else {
				productFamily = "'" + request.getParameter("productFamily")	+ "'";
				productSubFamily = "'"	+ request.getParameter("productSubFamily") + "'";
			}
			
			Logger.logDebugMessage("PriceListsExcelServlet","Family:::: "	+ productFamily);
			Logger.logDebugMessage("PriceListsExcelServlet","productSubFamily:::: " + productSubFamily);
			Logger.logDebugMessage("PriceListsExcelServlet","outputFormat:::: " + outputFormat);
			
			// Getting Current Date
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy",Locale.US);
			String excelExportDate = sdf.format(calendar.getTime());

			if (outputFormat.equals("PDF")) {
				Logger.logDebugMessage("PriceListsExcelServlet","outputFormat is PDF is selected :::: " + outputFormat);
				Logger.logDebugMessage("PriceListsExcelServlet","in Display content:::: " + outputFormat);
				response.setHeader("Content-Disposition","attachment; filename=\"" + priceListName + "Report-"+ excelExportDate + ".pdf\"");
				response.setContentType("application/pdf");					
			} else if (outputFormat.equals("Excel")) {
				Logger.logDebugMessage("PriceListsExcelServlet","outputFormat is not selected Excelreport have upload option:::: " + outputFormat);
				Logger.logDebugMessage("PriceListsExcelServlet","in Display content:::: " + outputFormat);
				response.setHeader("Content-Disposition","attachment; filename=\"" + priceListName + "Report-"	+ excelExportDate + ".xls\"");
				response.setContentType("application/vnd.ms-excel");
			} else {
				Logger.logDebugMessage("PriceListsExcelServlet","outputFormat is not selected Excelreport have upload option:::: "+ outputFormat);
				Logger.logDebugMessage("PriceListsExcelServlet","in Display content:::: " + outputFormat);
				response.setHeader("Content-Disposition","attachment; filename=\"" + priceListName + "-"+ excelExportDate + ".xls\"");
				response.setContentType("application/vnd.ms-excel");
			}

			Logger.logDebugMessage("PriceListsExcelServlet", "Operation:::: " + operation);
			Logger.logDebugMessage("PriceListsExcelServlet", "priceListId:::: "	+ priceListId);

			if (operation != null && !operation.equalsIgnoreCase("")) {
				if (operation.equalsIgnoreCase("GENERATE_EXCEL") || operation.equalsIgnoreCase("GENERATE_REPORT")) {
					Logger.logDebugMessage("PriceListsExcelServlet","Operation:::: " + operation);
					Logger.logDebugMessage("PriceListsExcelServlet","outputFormat value:::: " + outputFormat);

					HSSFWorkbook workBook=null;
					if (outputFormat.equals("PDF")) {
						PriceListPDFHelper helperPDF = null;
						PriceListPDFHelperRegister helperPDFRegister =null;
						ServletOutputStream out = response.getOutputStream();
						Document document = new Document();
						document.setMargins(72,72, 40, 40);
						PdfWriter pWriter = PdfWriter.getInstance(document, out);					
						int totalNoOfPages = pWriter.getPageNumber();
						 
						Logger.logDebugMessage("PriceListsExcelServlet","totalNoOfPages:::: " + outputFormat);
						Logger.logDebugMessage("PriceListsExcelServlet","customerPart:::: " + outputFormat);
						Logger.logDebugMessage("PriceListsExcelServlet","includePriceValues:::: " + outputFormat);
						
						if(pWriter != null){
							if(includePriceValues.equalsIgnoreCase("NO")){
								helperPDFRegister=new PriceListPDFHelperRegister();
								pWriter.setPageEvent(helperPDFRegister);
								document.open();
								helperPDFRegister.generatePriceListPDFReport(document,priceListId, productFamily, productSubFamily,	customerPart, realPath/*,totalNoOfPages*/);
								document.close();
							}else{
								helperPDF = new PriceListPDFHelper();
								pWriter.setPageEvent(helperPDF);
								document.open();
								helperPDF.generatePriceListPDFReport(document,priceListId, productFamily, productSubFamily,	customerPart, realPath/*,totalNoOfPages*/);
								document.close();
							}	
						}
						out.flush();
						out.close();
					} else if (outputFormat.equals("Excel")) {
						PriceListExcelHelper helperExcel = new PriceListExcelHelper();
						//workBook = helperExcel.generatePriceListExcelReport(priceListId, productFamily, productSubFamily,customerPart, realPath);
						ServletOutputStream out = response.getOutputStream();
						workBook.write(out);
						out.flush();
						out.close();						
					} else {
						PriceListExcelHelper helper = new PriceListExcelHelper();
						workBook = helper.generatePriceListExcel(priceListId, productFamily, productSubFamily/*,customerPart*/);
						ServletOutputStream out = response.getOutputStream();
						workBook.write(out);
						out.flush();
						out.close();
					}
				}
			}// if
		} catch (Exception exception) {
			Logger.logExceptionMessage(exception);
		}		
	}
}// servlet