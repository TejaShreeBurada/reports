package com.cavium.forecast.helpers;
import com.cavium.forecast.beans.PriceListBean;
import com.cavium.forecast.beans.SetUpDataBean;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.json.simple.JSONObject;

import com.cavium.forecast.logger.Logger;
import com.cavium.forecast.main.PriceListHandler;
import com.cavium.forecast.main.Users;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class PriceListPDFHelper extends PdfPageEventHelper{
	public Users users 									 = null;
	public PriceListHandler	priceListHandler			 = null;
	private PriceListHelper priceListHelper	             = null;
	
	private static String className 					 = PriceListPDFHelper.class.getName();

	private static Font cavmFont      = new Font(Font.HELVETICA,9,Font.BOLD);
	private static Font attrFont  =  new Font(Font.HELVETICA,10,Font.NORMAL);
	private static Font tableData    = new Font(Font.HELVETICA,8,Font.NORMAL);
	private static Font rangeMoqFount    = new Font(Font.HELVETICA,7,Font.BOLD);
	//private static Font underLine    = new Font(Font.HELVETICA,8,Font.UNDERLINE);
	
	public static float headerwidths[];
	public int totalNoOfPages=0;
	
	private final static int fixedCols = 3;  //-----For Item,DistiMoq,Disti--------- added by narendar on 10/29/2012	

	public PriceListPDFHelper() throws Exception{
		this.priceListHandler       = new PriceListHandler();
		this.users 					 = new Users();
		this.priceListHelper 			 = new PriceListHelper();	   
	}
	public  /*String*/ void generatePriceListPDFReport(Document document, String priceList,String productFamily,String productSubFamily,String customerPart,String realPath/*,int totalNoOfPages*/) throws Exception{

		Iterator itrSub = null;
		String family = "";
		String subFamily = "";
		Set subfamily = null;
	
	
		ArrayList rngPriceMap = null;
		LinkedHashMap subFamilyMap = null;
		PriceListBean beanData = null;
		ArrayList priceListRecord = null;
		String value = "";
		boolean flag = false; //-------to ensure that only one row space between the table and image/headers & sub-header-----added by narendar on 10/29/2012 	

		try{
			
			JSONObject obj = this.priceListHelper.getMaxRangeCount(priceList, productFamily, productSubFamily);
			log("rangeCount ::::::::"+obj.get("rangeValue"));
			int rangeCount = ((Integer)obj.get("rangeValue")).intValue();
			log("range count :::::::"+rangeCount);

			/*SimpleDateFormat toDay = new SimpleDateFormat("MM/dd/yyyy",Locale.US);*/
			if(!priceListHandler.getInstanceName().equalsIgnoreCase("PROD")){
				
			PdfPTable tittleTable = new PdfPTable(fixedCols+rangeCount);
			Paragraph tittle=new Paragraph("THIS IS NOT A VALID PRODUCTION PRICE LIST",new Font(tableData));
			PdfPCell cell = new PdfPCell(tittle);
			cell.setBorderColor(Color.WHITE);
			cell.setColspan(fixedCols+rangeCount);	
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			tittleTable.addCell(cell);			
			tittleTable.setTotalWidth(540f); 
			tittleTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			document.add(tittleTable);
			
			tittle=new Paragraph("THIS IS NOT A VALID PRODUCTION PRICE LIST",new Font(tableData));
	        HeaderFooter header = new HeaderFooter(tittle, false);
	        header.setBorder(0);
	        header.setAlignment(Paragraph.ALIGN_CENTER);
	        document.setHeader(header);
			
			}			
			//document.addTitle("Tittle");
			//document.addHeader("Header1", "Header2");
			
			//Example
			Paragraph tittle=new Paragraph("Cavium Confidential",cavmFont);
			tittle.setAlignment(Element.ALIGN_LEFT);			
			//document.add(tittle);	

		
			//data from DB 
			LinkedHashMap exportdata = this.priceListHandler.getPriceListReportGenerationData(rangeCount, priceList, productFamily, productSubFamily,customerPart);
						
			//log("PDF Report Data ........... :"+exportdata.toString());
			
			if(exportdata!=null){
				Set familySet = exportdata.keySet();
				Iterator famItr = familySet.iterator();

				while(famItr.hasNext()){		 
					family =  (String) famItr.next();		 
					log("Family :::::::"+family);

					subFamilyMap=(LinkedHashMap) exportdata.get(family);
					subfamily = subFamilyMap.keySet();
					itrSub = subfamily.iterator();
					while(itrSub.hasNext()){

						subFamily = (String) itrSub.next();
						log("subFamily :::::::"+subFamily);

						SetUpDataBean setupInfo = this.priceListHandler.getPriceListSetUpInfo(priceList, family, subFamily);
						
						if(!"".equals(setupInfo.getRangeInfo())){
							
						int tempRangeCount = Integer.valueOf(setupInfo.getRangeCount()).intValue();
						
						//table widths
						//should be before Attributes 
						headerwidths = new float[fixedCols+tempRangeCount];
						headerwidths[0]=2f;
						for(int index=1;index<fixedCols+tempRangeCount;index++){
							headerwidths[index]=.8f;
						}

						flag = false;
						
						//Add Image to Table						
						PdfPTable imgTable = new PdfPTable(5); // 3 columns.
						Image image = Image.getInstance(realPath);
						PdfPCell PdfPCell3 = new PdfPCell(image, true);
						PdfPCell3.setBorderColor(Color.white);	
						PdfPCell3.setColspan(1);						
						PdfPCell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
						imgTable.addCell(PdfPCell3);

						PdfPCell3 = new PdfPCell(new Paragraph(" ",cavmFont));
						PdfPCell3.setBorderColor(Color.white);
						PdfPCell3.setColspan(1);						
						imgTable.addCell(PdfPCell3);

						//Add emty space to Table
						PdfPCell3 = new PdfPCell(tittle);
						PdfPCell3.setColspan(fixedCols);
						PdfPCell3.setBorderColor(Color.WHITE);
						PdfPCell3.setHorizontalAlignment(Element.ALIGN_LEFT);
						imgTable.addCell(PdfPCell3);
						
						imgTable.setSplitRows(true);
						imgTable.setHorizontalAlignment(Element.ALIGN_CENTER);
						imgTable.setWidthPercentage(100);
						imgTable.getDefaultCell().setPadding(5);
						imgTable.setTotalWidth(540f);               //540f is good
						imgTable.getDefaultCell().setPadding(2f);
						imgTable.getDefaultCell().setPaddingLeft(4);
						imgTable.getDefaultCell().setPaddingTop(0);
						imgTable.getDefaultCell().setPaddingBottom(4);
						imgTable.getDefaultCell().setMinimumHeight(20); 
						//Add imageTable to doc object
						document.add(imgTable);	
						
						//----------------------------------
						//Price List Data start From here 					
						PdfPTable table = new PdfPTable(fixedCols+tempRangeCount);
						table.setWidths(headerwidths);
						table.setWidthPercentage(100);
						table.getDefaultCell().setPadding(5);
						//table.setLockedWidth(true);
						table.setTotalWidth(540f);               //540f is good
						table.getDefaultCell().setPadding(2f);
						table.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table.getDefaultCell().setPaddingLeft(4);
						table.getDefaultCell().setPaddingTop(0);
						table.getDefaultCell().setPaddingBottom(4);
						table.getDefaultCell().setMinimumHeight(15); 
						//---------------------------------

						//Empty cell before each table 
						PdfPCell addEmty=new PdfPCell(new Paragraph("\t",tableData));
						addEmty.setColspan(fixedCols+rangeCount);
						addEmty.setBorder(Rectangle.NO_BORDER);
						table.addCell(addEmty);	
						
						PdfPCell PdfPCell = null;
						if(!setupInfo.getAttribute1().equalsIgnoreCase(""))
						{
							//PdfPTable attrTable1 = new PdfPTable(3+tempRangeCount);
							table = this.AttrOutSideTable(" "+setupInfo.getAttribute1(),tempRangeCount, table, PdfPCell);
							//document.add(attrTable1);//-----------commented----- by Narendar 10/29/2012
							log("Added attribute1 information............");
						}

						if(!setupInfo.getAttribute2().equalsIgnoreCase("")){
							//PdfPTable attrTable2 = new PdfPTable(3+rangeCount); // 3 columns.	//-----------commented----- by Narendar 10/29/2012
							table=this.AttrOutSideTable(" "+setupInfo.getAttribute2(), tempRangeCount, table, PdfPCell);
							//document.add(attrTable2);//-----------commented----- by Narendar 10/29/2012
							log("Added attribute2 information............");
						}
						
						//Added Registration one time for family(by using boolean flag)
						priceListRecord  = (ArrayList)subFamilyMap.get(subFamily);
						boolean print=true;
						for(int indexArray = 0;indexArray<priceListRecord.size();indexArray++){
							beanData = (PriceListBean) priceListRecord.get(indexArray);
							if(print){
								table=this.AttrOutSideTable(" Registration = "+beanData.getRegistration(), tempRangeCount, table, PdfPCell);
							}
							print=false;
						}
						
						//space between Attr2 and Table
						if(setupInfo.getAttribute1().equalsIgnoreCase("") && setupInfo.getAttribute2().equalsIgnoreCase("")){
							//Empty cell before each table 
							addEmty=new PdfPCell(new Paragraph("\t",tableData));
							addEmty.setColspan(fixedCols+rangeCount);
							addEmty.setBorder(Rectangle.NO_BORDER);
							table.addCell(addEmty);	
							flag = true;
							
						}					

						
						//-------------------attribute style is changed ---------------By Narendar on Date:10-26-2012
						
						if(!setupInfo.getAttribute3().equalsIgnoreCase("")){
							
							PdfPCell=new PdfPCell(new Paragraph("\t",rangeMoqFount));
							PdfPCell.setBorder(Rectangle.NO_BORDER);
							table.addCell(PdfPCell);

							PdfPCell=new PdfPCell(new Paragraph(setupInfo.getAttribute3(),rangeMoqFount));		
							PdfPCell=this.attribute4Style(PdfPCell);
							PdfPCell.setColspan(fixedCols+rangeCount);	
							table.addCell(PdfPCell);
						}
						
						if(!setupInfo.getAttribute4().equalsIgnoreCase("")){
							PdfPCell=new PdfPCell(new Paragraph("\t",rangeMoqFount));
							PdfPCell.setBorder(Rectangle.NO_BORDER);		
							table.addCell(PdfPCell);
							
							PdfPCell=new PdfPCell(new Paragraph(setupInfo.getAttribute4(),rangeMoqFount));		
							PdfPCell=this.attribute4Style(PdfPCell);
							PdfPCell.setColspan(fixedCols+rangeCount);	
							table.addCell(PdfPCell);
							
						}
						
						
						log("flag............"+flag);
						if(setupInfo.getAttribute3().equalsIgnoreCase("") && setupInfo.getAttribute4().equalsIgnoreCase("") && flag==false){
							//Empty cell before each table 
							addEmty=new PdfPCell(new Paragraph("\t",tableData));
							addEmty.setColspan(fixedCols+rangeCount);
							addEmty.setBorder(Rectangle.NO_BORDER);
							table.addCell(addEmty);	
							}
						
						
						table = this.addRangeInfoCells(tempRangeCount,setupInfo.getRangeInfo(),table,PdfPCell);
						log("SubFamily...."+subFamily);
						priceListRecord  = (ArrayList)subFamilyMap.get(subFamily);
						log("priceListRecord :"+priceListRecord.toString());
						for(int indexArray = 0;indexArray<priceListRecord.size();indexArray++){
							beanData = (PriceListBean) priceListRecord.get(indexArray);
							int baseRangeCount = beanData.getCount();							
							PdfPCell PdfPCellItem=new PdfPCell(new Paragraph(beanData.getItemName(),tableData));
							log("Item :"+beanData.getItemName());
							log("TempRange:"+tempRangeCount+" and BaseRange:"+baseRangeCount);
							if(tempRangeCount != baseRangeCount){
								log("TempRange:"+tempRangeCount+" and BaseRange:"+baseRangeCount +"are Diffrent");
								PdfPCellItem.setBackgroundColor(Color.decode("#CCCCFF"));	
							}
							PdfPCellItem=this.addValue(PdfPCellItem);
						//	PdfPCellItem.setHorizontalAlignment(Element.ALIGN_LEFT);
							table.addCell(PdfPCellItem);

							rngPriceMap = beanData.getRngPriceMap();
							log("rngPriceMap:"+rngPriceMap.toString());

							double stringDistribution ;
							DecimalFormat df;
							//double valueDouble;
							//String distribution;

							for(int index = 0;index < rngPriceMap.size();index++)
							{
								PdfPCell PdfPCellPrice=null;
								value=(String) rngPriceMap.get(index);
								try {
									if(!value.equalsIgnoreCase("")){

										df = new DecimalFormat("#.##");
										df.setMinimumFractionDigits(2);
										df.setMaximumFractionDigits(2);
										stringDistribution = Double.valueOf((String) rngPriceMap.get(index)).doubleValue();
										value = "$" + df.format(stringDistribution);
										log("PDF Price value................................:"+value);
									}
								} catch (Exception e) {									
									value=(String) rngPriceMap.get(index);
									log("PDF DFF value................................:"+value);
								}								
								log("PDF Price value................................:"+value);
								PdfPCellPrice=new PdfPCell(new Paragraph(value,tableData));
								PdfPCellPrice=this.addValue(PdfPCellPrice);						
								table.addCell(PdfPCellPrice);							
							}//for

							if(tempRangeCount != rngPriceMap.size()){
								for(int range=0;range < tempRangeCount-rngPriceMap.size();range++){
									PdfPCell PdfPCellPrice=null;
									//PdfPCellPrice=new PdfPCell(new Paragraph("N/A",tableData));
									PdfPCellPrice=new PdfPCell(new Paragraph("",tableData));
									PdfPCellPrice = this.addValue(PdfPCellPrice);
									table.addCell(PdfPCellPrice);
								}
							}

							String distiMoq  = setupInfo.getDistiMoq();
							log("distiMoq:"+distiMoq);
							PdfPCell=new PdfPCell(new Paragraph(distiMoq,tableData));
							PdfPCell = this.addValue(PdfPCell);
							table.addCell(PdfPCell);

							String distibution="";
							if(!beanData.getDistribution().equalsIgnoreCase(""))
							{
								
								df = new DecimalFormat("#.##");
								df.setMinimumFractionDigits(2);
								df.setMaximumFractionDigits(2);
								stringDistribution = Double.valueOf(beanData.getDistribution()).doubleValue();
								distibution = "$" + df.format(stringDistribution);
								
							}	

							log("distibution:"+distibution);
							PdfPCell=new PdfPCell(new Paragraph(distibution,tableData));
							PdfPCell = this.addValue(PdfPCell);
							table.addCell(PdfPCell);							
						} 
						
						//Empty cell After each table 
						addEmty=new PdfPCell(new Paragraph("\t",tableData));
						addEmty.setColspan(fixedCols+rangeCount);
						addEmty.setBorder(Rectangle.NO_BORDER);
						table.addCell(addEmty);	
											
						
						if(!setupInfo.getAttribute5().equalsIgnoreCase("")){
							//PdfPTable attrTable5 = new PdfPTable(3+tempRangeCount); // 3 columns.	//-----------commented----- by Narendar 10/29/2012
						//	table=this.addEmptyRows(rangeCount, table, PdfPCell);
							table=this.AttrOutSideTable(setupInfo.getAttribute5(), tempRangeCount, table, PdfPCell);
							//document.add(attrTable5);
							log("Added attribute5 information............");
						}
						if(!setupInfo.getAttribute6().equalsIgnoreCase("")){
							//PdfPTable attrTable6 = new PdfPTable(3+tempRangeCount); // 3 columns.	//-----------commented----- by Narendar 10/29/2012 
							table=this.AttrOutSideTable(setupInfo.getAttribute6(), tempRangeCount, table, PdfPCell);
							//document.add(attrTable6);
							log("Added attribute6 information............");
						}
						if(!setupInfo.getAttribute7().equalsIgnoreCase("")){
							//PdfPTable attrTable7 = new PdfPTable(3+tempRangeCount); // 3 columns.	//-----------commented----- by Narendar 10/29/2012
							table=this.AttrOutSideTable(setupInfo.getAttribute7(), tempRangeCount, table, PdfPCell);
							//document.add(attrTable7);
							log("Added attribute7 information............");	
						}
						
						/*//Empty cell After each table 
						addEmty=new PdfPCell(new Paragraph("\t",tableData));
						addEmty.setColspan(fixedCols+rangeCount);
						addEmty.setBorder(Rectangle.CCITT_ENDOFLINE);
						table.addCell(addEmty);	*/
											
						//Add all data
						document.add(table);
						
						log("new Page information............");	
						document.newPage();
						
						//need
						
						}//if RangeInfo
						
					}//while subfamily

				}//while fmily
				log("completed............");	
			}else{
				//Add Image to Table						
				log("Else.........................");
				PdfPTable imgTable = new PdfPTable(5); // 3 columns.
				Image image = Image.getInstance(realPath);
				PdfPCell PdfPCell3 = new PdfPCell(image, true);
				PdfPCell3.setBorderColor(Color.white);	
				PdfPCell3.setColspan(1);						
				PdfPCell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
				imgTable.addCell(PdfPCell3);

				PdfPCell3 = new PdfPCell(new Paragraph(" ",cavmFont));
				PdfPCell3.setBorderColor(Color.white);
				PdfPCell3.setColspan(1);						
				imgTable.addCell(PdfPCell3);

				//Add emty space to Table
				PdfPCell3 = new PdfPCell(tittle);
				PdfPCell3.setColspan(3);
				PdfPCell3.setBorderColor(Color.WHITE);
				PdfPCell3.setHorizontalAlignment(Element.ALIGN_LEFT);
				imgTable.addCell(PdfPCell3);						
				
				imgTable.setSplitRows(true);
				imgTable.setHorizontalAlignment(Element.ALIGN_CENTER);
				imgTable.setWidthPercentage(100);
				imgTable.getDefaultCell().setPadding(5);
				imgTable.setTotalWidth(540f);               //540f is good
				imgTable.getDefaultCell().setPadding(2f);
				imgTable.getDefaultCell().setPaddingLeft(4);
				imgTable.getDefaultCell().setPaddingTop(0);
				imgTable.getDefaultCell().setPaddingBottom(4);
				imgTable.getDefaultCell().setMinimumHeight(20); 
				//Add imageTable to doc object
				document.add(imgTable);	

				PdfPTable table = new PdfPTable(1);
				table.setWidthPercentage(100);
				table.getDefaultCell().setPadding(5);
				table.getDefaultCell().setPadding(2f);
				table.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.getDefaultCell().setPaddingLeft(4);
				table.getDefaultCell().setPaddingTop(0);
				table.getDefaultCell().setPaddingBottom(4);
				table.getDefaultCell().setMinimumHeight(15); 
				PdfPCell addEmty=new PdfPCell(new Paragraph("Selected Family and SubFamily doesn't have any prices to display.",tableData));
				addEmty.setColspan(fixedCols+rangeCount);
				table.addCell(addEmty);	
				document.add(table);
			}		
			log("completed............");	
		}catch(Exception exp){
			String errorMessage = "Error occured in getPriceListReportGenerationData method "+exp;
			Logger.logErrorMessage(className, errorMessage);
			Logger.logExceptionMessage(exp);
			throw exp;
		}
		return ;
	}


	
	
	public void onEndPage(PdfWriter writer, Document document) {
				
	    Rectangle page = document.getPageSize();
	 //   Font font = FontFactory.getFont("SERIFE", BaseFont.WINANSI, 8, Font.BOLD);	
	    Font font = FontFactory.getFont("SERIFE", "Cp1252", 8.0F, 1);
	    
	    PdfPTable foot = new PdfPTable(3);
	    PdfPCell pdfCell = null;	    
	    SimpleDateFormat toDay = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
	    pdfCell = new PdfPCell(new Phrase("Date : "+toDay.format(new java.util.Date()), font ) );
	    pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	    pdfCell.setBorderWidth(0);
	    foot.addCell(pdfCell);	 
	    
	    pdfCell = new PdfPCell(new Phrase("PR Devices are NCNR", font ));
	    pdfCell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
	    pdfCell.setBorderWidth(0);
	    foot.addCell(pdfCell);
	    
	    pdfCell = new PdfPCell(new Phrase("Page No."+String.valueOf(writer.getPageNumber()).toString()/*+" of "+this.totalNoOfPages*/ , font ));
	    pdfCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	    pdfCell.setBorderWidth(0);
	    foot.addCell(pdfCell);
	    
	    foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
	    foot.writeSelectedRows(0, 5, document.leftMargin(),document.bottomMargin(),writer.getDirectContent());
  
	  }
	
	private PdfPCell addValue(PdfPCell PdfPCellPrice){
		PdfPCellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
		return PdfPCellPrice;
	}
	/*private PdfPCell addEmptyBoarder(PdfPCell emptyBoarderStyle){
		emptyBoarderStyle.setHorizontalAlignment(Element.ALIGN_LEFT);
		emptyBoarderStyle.setVerticalAlignment(Element.ALIGN_LEFT);		
		emptyBoarderStyle.setBorderWidthLeft(.5f);
		emptyBoarderStyle.setBorderColorLeft(Color.WHITE);		
		emptyBoarderStyle.setBorderWidthRight(.5f);
		emptyBoarderStyle.setBorderColorRight(Color.WHITE);		
		emptyBoarderStyle.setBorderWidthTop(.5f);
		emptyBoarderStyle.setBorderColorTop(Color.WHITE);		
		emptyBoarderStyle.setBorderWidthBottom(.5f);
		emptyBoarderStyle.setBorderColorBottom(Color.WHITE);
		return emptyBoarderStyle;
	}*/

/*	private PdfPCell attribute3Style(PdfPCell attribute3){
		attribute3.setHorizontalAlignment(Element.ALIGN_CENTER);
		attribute3.setVerticalAlignment(Element.ALIGN_CENTER);
		attribute3.setBorderWidthLeft(.5f);
		attribute3.setBorderColorLeft(Color.black);
		attribute3.setBorderWidthBottom(.5f);
		attribute3.setBorderColorBottom(Color.WHITE);
		return attribute3;		
	}
	private PdfPCell moq(PdfPCell moq){
		moq.setHorizontalAlignment(Element.ALIGN_RIGHT);
		moq.setVerticalAlignment(Element.ALIGN_RIGHT);
		moq.setBorderWidthLeft(.5f);
		moq.setBorderColorLeft(Color.black);
		moq.setBorderWidthRight(.5f);
		moq.setBorderColorRight(Color.WHITE);
		moq.setBorderWidthTop(.5f);
		moq.setBorderColorTop(Color.WHITE);
		moq.setBorderWidthBottom(.5f);
		moq.setBorderColorBottom(Color.WHITE);
		return moq;		
	}*/
	private PdfPCell range(PdfPCell range){
		range.setHorizontalAlignment(Element.ALIGN_RIGHT);
		range.setVerticalAlignment(Element.ALIGN_RIGHT);
		 //commented --------------------By Narendar on Date:10-26-2012-----------------------------
		/*range.setBorderWidthTop(.5f);
		range.setBorderColorTop(Color.BLACK);		
		range.setBorderWidthLeft(.5f);
		range.setBorderColorLeft(Color.black);
		range.setBorderWidthRight(.5f);
		range.setBorderColorRight(Color.WHITE);
		range.setBorderWidthBottom(.5f);
		range.setBorderColorBottom(Color.WHITE);*/
		return range;		
	}	
	private PdfPCell distibution(PdfPCell disti){
		disti.setHorizontalAlignment(Element.ALIGN_RIGHT);
		disti.setVerticalAlignment(Element.ALIGN_RIGHT);
		 //commented --------------------By Narendar on Date:10-26-2012-----------------------------
		/*disti.setBorderWidthTop(.5f);
		disti.setBorderColorTop(Color.BLACK);		
		disti.setBorderWidthLeft(.5f);
		disti.setBorderColorLeft(Color.black);
		disti.setBorderWidthRight(.5f);
		disti.setBorderColorRight(Color.BLACK); 
		disti.setBorderWidthBottom(.5f);
		disti.setBorderColorBottom(Color.WHITE);*/
		return disti;
	}
	
	//--------------------attribute4style is changed
	private PdfPCell attribute4Style(PdfPCell attribute4){
		attribute4.setHorizontalAlignment(Element.ALIGN_CENTER);
		attribute4.setVerticalAlignment(Element.ALIGN_CENTER);		
		 //commented --------------------By Narendar on Date:10-26-2012-----------------------------
		/*
		attribute4.setBorderWidthBottom(.5f);
		attribute4.setBorderColorBottom(Color.WHITE);
		
		attribute4.setBorderWidthTop(.5f);
		attribute4.setBorderColorTop(Color.WHITE);
		
		attribute4.setBorderWidthLeft(.5f);
		attribute4.setBorderColorLeft(Color.black);*/
		
		return attribute4;		
	}
	
	
	
	private PdfPTable addRangeInfoCells(int rangeCount,String rangeInfo,PdfPTable table,PdfPCell PdfPCell) throws BadElementException{
	    try{
	    	//rangeInfo="1-99/21;100-999/63;1000-4999/294;5000-9999999/504;";	    	
	    	PdfPCell=new PdfPCell(new Paragraph("\t",rangeMoqFount));
			PdfPCell.setBorder(Rectangle.NO_BORDER);						
			table.addCell(PdfPCell);
	    	
			String[] rangeValue = rangeInfo.split(";");
			
	    	//log("rangeVal:"+rangeValue.toString());
			
	    	for(int index=0;index < rangeCount;index++){
	    		 String range_moq = rangeValue[index];
	    		 
	    		 log("range value["+index+"] :"+range_moq);
	    		 /*String[] range = range_moq.split("/");	
				 log("range range[0] :"+range[0]);*/
	    		 
	    		 //---------------------------------add 0-499\nMOQ:45 -------------------By Narendar on Date:10-26-2012
	    		 if(range_moq.indexOf("/")!= -1)
	    		 range_moq=range_moq.replaceAll("/", "\nMOQ:");	
	    		 log(" Range & MOQ :" + range_moq);
	    		 
	    		 //---------------------------------for First Range 0-11111 as blank displayed-----------By Narendar on Date:10-26-2012
	    		 if(rangeCount == 1){	    			  
	    			  range_moq = range_moq.substring(range_moq.indexOf("\n")+1);
	    			  log(" Only MOQ :" + range_moq);
	    		 }
	    		  
	    		  
				 PdfPCell = new PdfPCell(new Paragraph(range_moq,rangeMoqFount));
				 PdfPCell = this.range(PdfPCell);
				 table.addCell(PdfPCell);
			}
	    	
	    	
			 PdfPCell = new PdfPCell(new Paragraph("Distribution \nMOQ",rangeMoqFount)); //---------- added \n--------------By Narendar on Date:10-26-2012
			 PdfPCell = this.range(PdfPCell);
			 PdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			 table.addCell(PdfPCell);
			 
			 
			 PdfPCell = new PdfPCell(new Paragraph("Distribution \n",rangeMoqFount));  //----------- added \n--------------By Narendar on Date:10-26-2012
			 PdfPCell = this.distibution(PdfPCell);
			 PdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			 table.addCell(PdfPCell);
			 
			 //commented --------------------By Narendar on Date:10-26-2012-----------------------------
			/* 
			 PdfPCell=new PdfPCell(new Paragraph("    ",rangeMoqFount));
			 PdfPCell.setBorderColor(Color.white);		
			 table.addCell(PdfPCell);
			for(int index=0;index < rangeCount;index++){
				 String range_moq = rangeValue[index];
				 
				 String range[] = range_moq.split("/");
				 log("rangeMOQ range[1] :"+range[1]);
				 PdfPCell = new PdfPCell(new Paragraph("MOQ:"+range[1],rangeMoqFount));
				 PdfPCell = this.moq(PdfPCell);
				 table.addCell(PdfPCell);
			}
			
			PdfPCell = new PdfPCell(new Paragraph("MOQ",rangeMoqFount));
			PdfPCell = this.moq(PdfPCell);
			table.addCell(PdfPCell);
			
			PdfPCell = new PdfPCell(new Paragraph("",rangeMoqFount));
			PdfPCell = this.moq(PdfPCell);
			PdfPCell.setBorderWidthRight(.5f);
			PdfPCell.setBorderColorRight(Color.black);
			table.addCell(PdfPCell)*/
			
			
	    }catch(Exception e){
	    	Logger.logExceptionMessage(e);
	    }
	    return table;		
	}
	
	/*private PdfPTable addAttribute(String attribute,int rangeCount,PdfPTable table,PdfPCell PdfPCell) throws DocumentException{
		PdfPCell = new PdfPCell(new Paragraph(attribute,rangeMoqFount));//,emtyPdfPCell));
		    table.setWidths(headerwidths);
		    table.setWidthPercentage(100);
		    table.getDefaultCell().setPadding(5);
			//table.setLockedWidth(true);
	        table.setTotalWidth(540f);               //540f is good
	        table.getDefaultCell().setPadding(2f);
	        //table.setHorizontalAlignment(Element);
	        table.setHorizontalAlignment(Element.ALIGN_CENTER);
	        table.getDefaultCell().setPaddingLeft(4);
	        table.getDefaultCell().setPaddingTop(0);
	        table.getDefaultCell().setPaddingBottom(4);
	        table.getDefaultCell().setMinimumHeight(15); 
	    
	    PdfPCell= this.addEmptyBoarder(PdfPCell);
		PdfPCell.setColspan(2+rangeCount);
		table.addCell(PdfPCell);		
		//table.setBorderColor(Color.WHITE);
		return table;	
	}*/
	
	private PdfPTable AttrOutSideTable(String attribute,int rangeCount,PdfPTable table,PdfPCell pdfPCell) throws DocumentException{
		
		pdfPCell = new PdfPCell(new Paragraph(attribute,attrFont));
		//pdfPCell= this.addEmptyBoarder(pdfPCell);
		pdfPCell.setBorder(Rectangle.NO_BORDER);
		pdfPCell.setColspan(fixedCols+rangeCount);
		table.addCell(pdfPCell);
		
		//---------------------------added -------------------- by Narendar  on 10/29/2012
		PdfPCell cell=new PdfPCell(new Paragraph(""));
		cell.setColspan(fixedCols+rangeCount);
		log("Added emty space bettween attribute -------------");
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		//---------------------------commented -------------------- by Narenar  on 10/29/2012
		/*table=this.addEmptyRows(rangeCount, table, pdfPCell);		
		    table.setWidths(headerwidths);
		    table.setWidthPercentage(100);
		    table.getDefaultCell().setPadding(5);
			//table.setLockedWidth(true);
	        table.setTotalWidth(540f);               //540f is good
	        table.getDefaultCell().setPadding(2f);
	        table.setHorizontalAlignment(Element.ALIGN_LEFT);
	        table.getDefaultCell().setPaddingLeft(4);
	        table.getDefaultCell().setPaddingTop(0);
	        table.getDefaultCell().setPaddingBottom(4);
	        table.getDefaultCell().setMinimumHeight(15); */
	    
		return table;	
	}
	
	/*private PdfPTable addEmptyRows(int rangeCount,PdfPTable table,PdfPCell pdfPCell){
		try{
	    	for(int index=0;index < fixedCols+rangeCount;index++){
				pdfPCell=new PdfPCell(new Paragraph(" ",tableData));
				pdfPCell=this.addEmptyBoarder(pdfPCell);
				table.addCell(pdfPCell);
			}
	    }catch(Exception e){
	    	Logger.logExceptionMessage(e);
	    }
	    return table;
	}*/

	public static void log(String message){
		Logger.logDebugMessage(className, message);
	}
}
