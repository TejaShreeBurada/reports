package com.cavium.forecast.helpers;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;


import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cavium.forecast.db.ConnectionPool;
import com.cavium.forecast.logger.Logger;
import com.cavium.forecast.main.PriceListHandler;
import com.cavium.forecast.main.Users;
import com.cavium.forecast.util.MailClient;
import com.cavium.forecast.util.Utils;
import com.cavium.forecast.helpers.*;
/**
 * A simple POI example of opening an Excel spreadsheet
 * and writing its contents to the command line.
 * @author  Tony Sintes
 */
public class PriceListUploadExcelHelper {
	
	private ConnectionPool pool 						= null;
	private Utils utils 								= null;
	public Users users 									= null;
	public PriceListHandler	priceListHandler			= null;
	private MailClient mailClient 						= null;
	private static String className 					= PriceListUploadExcelHelper.class.getName();
	private PriceListHelper priceListHelper	            = null;
	
	public PriceListUploadExcelHelper() throws Exception{
		this.pool 					= ConnectionPool.getInstance();
		this.utils 					= new Utils();
		this.priceListHandler       = new PriceListHandler();
		this.users 					= new Users();
		this.mailClient 			= new MailClient();
		this.priceListHelper	    = new PriceListHelper();
	}
	//Upload Excel data into Database and called from ExcelUploadTemp.jsp	
	
	public HashMap uploadPriceListExcelData(String userId,ArrayList uploadList,int rangeCount,String priceListId, String priceListName, String fileName,String fileUploadId) throws Exception
	{
		//JSONObject obj = new JSONObject();
		
		
		boolean status = true;
		String setUpSeqId="";
		HashMap Results = new HashMap();
		try{
			ArrayList insertList = new ArrayList();
			//ArrayList updateList = new ArrayList();
			HashMap uploadMap = new HashMap();
			ArrayList sqlQueries = new ArrayList();


			if(uploadList.size() > 0){
				log("uploadList values :::::"+uploadList.toString());

				//Iterator uploadListIterator = uploadList.iterator();
				//for(int index=0;uploadList.size()>index;index++){		
				for(int index=0;index<uploadList.size();index++){
					HashMap dataMap = (HashMap)uploadList.get(index);					
					log("new price start date :::::::::"+(String)dataMap.get("NEW_PRICE_START_DATE"));
					log("only Dff update flag :::::::::"+(String)dataMap.get("UPDATE_DFFS"));
					if(!((String)dataMap.get("NEW_PRICE_START_DATE")).equalsIgnoreCase("") || !((String)dataMap.get("UPDATE_DFFS")).equalsIgnoreCase("") ){
						//if(((String)dataMap.get("TEMP_ID")).equalsIgnoreCase("")){
						insertList.add(dataMap);
						/*}else{
							updateList.add(dataMap);
						}*/
					}
				}
			}
			//uploadMap.put("UpdateList",updateList);
			uploadMap.put("InsertList",insertList);

			log("insert list size :::::"+insertList.size());
			log("insert list values :::::"+insertList.toString());		
			//log("updateList size :::::"+updateList.size());			

			String family="";
			String subFamily="";
			String tempFamily=""; 
			String batchId = "";
			if(insertList.size() > 0){

				batchId = this.priceListHandler.getNextBatchId();
				log("BatchId::::::::::"+batchId);

				Iterator insertIterator = insertList.iterator();
				int count =1;
				while(insertIterator.hasNext()){
					String tempId = this.priceListHandler.getNextTempId();
					HashMap insertMap = (HashMap)insertIterator.next();

					String currentPriceStartDate = (String)insertMap.get("CURRENT_PRICE_START_DATE");
					log("String currentPriceStartDate = "+currentPriceStartDate);

					String currentPriceEndDate = (String)insertMap.get("CURRENT_PRICE_END_DATE");
					log("String currentPriceEndDate = "+currentPriceEndDate);

					String newPriceStartDate = (String)insertMap.get("NEW_PRICE_START_DATE");
					log("String newPriceStartDate ="+ newPriceStartDate);

					String newPriceEndDate = (String)insertMap.get("NEW_PRICE_END_DATE");
					log("String newPriceEndDate ="+ newPriceEndDate);
					/*	
				    String setUpSeqId = (String)insertMap.get("SETUP_SEQ_ID");
					log("String setUpSeqId = "+(String)insertMap.get("SETUP_SEQ_ID"));
					 */
					String customerPart = (String)insertMap.get("INCLUDE_CUSTOMER_PART");
					log("String customerPart ="+ customerPart);
					
					String lastDateEOL = (String)insertMap.get("LAST_ORDER_DATE");
					log("String lastDateEOL ="+lastDateEOL);

					String price_line_dff = (String)insertMap.get("PRICE_LINE");
					log("String price_line_dff ="+ price_line_dff);
					
					String updateDffs = (String)insertMap.get("UPDATE_DFFS");
					log("String UPDATE_DFFS ="+ updateDffs);


					String distribution = (String)insertMap.get("Distribution");
					log("String distribution = "+distribution);

					String item_name=(String)insertMap.get("ITEM_NAME"); 
					String family_subfamily = (String)insertMap.get("FAMILY_NAME").toString().trim();
					String family_subfamily2 = (String)insertMap.get("FAMILY_NAME").toString().trim()+"$";
					log("String family_subfamily ="+ family_subfamily  );

					if(family_subfamily2.equalsIgnoreCase("$")){
						//User Define Exception here if you have family_subfamily==null from excel
						log("family_subfamily :"+family_subfamily2);
						//Results="File Upload Error: "+"Items: " + item_name + " that are in Template does not have Setup...";
						//File Upload Error: Item: "+item_name+” in the Template has no Family/Sub-Family.
						Results.put("Results","File Upload Error: Item: "+item_name+" in the Template has no Family/Sub-Family.");
						Results.put("Status", "false");
						return Results;
					}

					if(family_subfamily!=null && !family_subfamily.equalsIgnoreCase(tempFamily))
					{
						tempFamily=family_subfamily;
						try{
							//StringTokenizer token = new StringTokenizer(family_subfamily,".");
							/*if(token.hasMoreTokens()){
							family=token.nextToken();

							log("Family....................................."+family);
							subFamily=token.nextToken();

							log("SubFamily.................................."+subFamily);
						}*/		
							String str=family_subfamily;
							family=str.substring(0, str.indexOf('.'));
							subFamily=str.substring(str.indexOf('.')+1, str.length());
							setUpSeqId = this.priceListHelper.getSetupSequenceId(priceListId,family,subFamily);
							log("setUpSeqId"+count+".................................."+setUpSeqId);
							if(setUpSeqId.equalsIgnoreCase(""))
							{
								log("family_subfamily :"+family_subfamily2);
								//Results="File Upload Error: "+"Setup is not exist for fallowing Family:"+family+" and SubFamily:"+subFamily;
								//File Upload Error: Item: "+item_name+" in the Template has an invalid Family/Sub-Family.
								Results.put("Results","File Upload Error: Item: "+item_name+" in the Template has an invalid Family/Sub-Family.");
								Results.put("Status", "false");
								return Results;	
							}
							count++;
						}
						catch (NoSuchElementException exception) {
							//Results = "File Upload Error: "+"For the Item: " + item_name + " family and subfamily in the Template is invalied ...";
							//File Upload Error: Item: "+item_name+" in the Template has an invalid Family/Sub-Family.
							
							Results.put("Results","File Upload Error: Item: "+item_name+" in the Template has an invalid Family/Sub-Family.");
							Results.put("Status", "false");
							
							this.utils.mailNLogExceptions("Exception while updating Upload PriceList values :"+Results +" ", exception);
							Logger.logExceptionMessage(exception);		 
							//throw exception;
							status = false;
							break;
						}

					}

					//String inventory_item_id =this.priceListHelper.getInventroyItemId(item_name);
					//log("inventory_item_id.................................."+inventory_item_id);

					if(currentPriceStartDate.equalsIgnoreCase("")){
						currentPriceStartDate = null;
					}else{
						currentPriceStartDate = "TO_DATE('"+currentPriceStartDate+"','MM/DD/YYYY')";
					}								
					if(currentPriceEndDate.equalsIgnoreCase("")){
						currentPriceEndDate = null;
					}else{
						currentPriceEndDate = "TO_DATE('"+currentPriceEndDate+"','MM/DD/YYYY')";
					}					
					if(newPriceStartDate.equalsIgnoreCase("")){
						newPriceStartDate = null;
					}else{
						newPriceStartDate = "TO_DATE('"+newPriceStartDate+"','MM/DD/YYYY')";
					}					
					if(newPriceEndDate.equalsIgnoreCase("")){
						newPriceEndDate = null;
					}else{
						newPriceEndDate = "TO_DATE('"+newPriceEndDate+"','MM/DD/YYYY')";
					}
					if(lastDateEOL.equalsIgnoreCase("")){
						lastDateEOL = null;
					}else{
						lastDateEOL = "TO_DATE('"+lastDateEOL+"','MM/DD/YYYY')";
					}
					if(distribution.equalsIgnoreCase("")){
						distribution = null;
					}
					else{
						////Added By Narendar for distribution value round to two decimal points 
						try{
							/*double stringDistribution = Double.valueOf(distribution.trim()).doubleValue();
							//distribution=(new BigDecimal(stringDistribution)).setScale(2, BigDecimal.ROUND_DOWN).toString();
							DecimalFormat df = new DecimalFormat("#.##");
							distribution = df.format(stringDistribution);*/

							double stringDistribution = Double.valueOf(distribution.trim()).doubleValue();
							//DecimalFormat df = new DecimalFormat("#.##");
							//double value = Double.valueOf(df.format(stringDistribution)).doubleValue();
							//distribution=(new BigDecimal(value)).setScale(2, BigDecimal.ROUND_DOWN).toString();
							distribution = String.valueOf(stringDistribution);
						}catch (NumberFormatException exception){
							//Results = "Data Issue: For the "+item_name+ " ,the "+ distribution +" Distribution value  is invalied.</br> Please correct the data problem. If a formula is used, convert to actual value.";
							//Data Issue: For "+item_name+ ", the "+distribution +" Distribution value is invalid. Please correct the data problem. If a formula is used, convert it to the actual value.

							Results.put("Results","Data Issue: For "+item_name+ ", the "+distribution +" Distribution value is invalid.</br>Please correct the data problem. If a formula is used, convert it to the actual value.");
							Results.put("Status", "false");
							this.utils.mailNLogExceptions("Exception while updating Upload PriceList values :"+Results +" ", exception);
							Logger.logExceptionMessage(exception);		
							status = false;
							break;
							//throw exception;							
						}
					}
					char statusCode='X';
					//String insertUploadHistorySql = "insert into XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_DATA(SETUP_SEQ_ID,ITEM_EXISTS,INVENTORY_ITEM_ID,CURRENT_PRICE_START_DATE,CURRENT_PRICE_END_DATE,NEW_PRICE_START_DATE,NEW_PRICE_END_DATE,RECOMMEND_NEW_DESIGNS,IS_PRODUCT_EOL,LAST_DATE_TO_ORDER_EOL,DISTRIBUTION_PRICE,PRICELIST_ID,PRICELIST,TEMP_ID,BATCH_ID,CREATION_DATE,CREATED_BY,LAST_UPDATE_DATE,LAST_UPDATED_BY,STATUS_CODE,INCLUDE_CUSTOMER_PART,INVENTORY_ITEM) ";
					String insertUploadHistorySql ="";
					if(((String)insertMap.get("NEW_PRICE_START_DATE")).equalsIgnoreCase("") && ((String)insertMap.get("UPDATE_DFFS")).equalsIgnoreCase("X") && ((String)insertMap.get("ITEM_INFO")).equalsIgnoreCase("N") ){
						statusCode='E';						
						insertUploadHistorySql = "insert into XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_DATA(SETUP_SEQ_ID,ITEM_EXISTS,CURRENT_PRICE_START_DATE,CURRENT_PRICE_END_DATE,NEW_PRICE_START_DATE,NEW_PRICE_END_DATE,RECOMMEND_NEW_DESIGNS,IS_PRODUCT_EOL,LAST_DATE_TO_ORDER_EOL,DISTRIBUTION_PRICE,PRICELIST_ID,PRICELIST,TEMP_ID,BATCH_ID,CREATION_DATE,CREATED_BY,LAST_UPDATE_DATE,LAST_UPDATED_BY,STATUS_CODE,INCLUDE_CUSTOMER_PART,INVENTORY_ITEM,UPDATE_DFFS,COMMENTS,FILE_UPLOAD_ID,PRICE_LINE_DFF) ";
						insertUploadHistorySql += "values("+setUpSeqId+",'Y',"+currentPriceStartDate+","+currentPriceEndDate+","+newPriceStartDate+","+newPriceEndDate+",'"+insertMap.get("NOTRECOMENDED")+"','"+insertMap.get("IS_PRODUCT_EOL")+"',"+lastDateEOL+",ROUND("+distribution+",2),"+priceListId+",'"+priceListName+"',"+tempId+","+batchId+",SYSDATE,"+userId+",SYSDATE,"+userId+",'"+statusCode+"','"+customerPart+"','"+item_name+"','','Please enter NewPricingStartDate to active Price List line','"+fileUploadId+"','"+price_line_dff+"')";
						log("insertUploadHistorySql Query :::::::"+insertUploadHistorySql);				
					}
					else
					{
						insertUploadHistorySql = "insert into XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_DATA(SETUP_SEQ_ID,ITEM_EXISTS,CURRENT_PRICE_START_DATE,CURRENT_PRICE_END_DATE,NEW_PRICE_START_DATE,NEW_PRICE_END_DATE,RECOMMEND_NEW_DESIGNS,IS_PRODUCT_EOL,LAST_DATE_TO_ORDER_EOL,DISTRIBUTION_PRICE,PRICELIST_ID,PRICELIST,TEMP_ID,BATCH_ID,CREATION_DATE,CREATED_BY,LAST_UPDATE_DATE,LAST_UPDATED_BY,STATUS_CODE,INCLUDE_CUSTOMER_PART,INVENTORY_ITEM,UPDATE_DFFS,FILE_UPLOAD_ID,PRICE_LINE_DFF) ";
						insertUploadHistorySql += "values("+setUpSeqId+",'Y',"+currentPriceStartDate+","+currentPriceEndDate+","+newPriceStartDate+","+newPriceEndDate+",'"+insertMap.get("NOTRECOMENDED")+"','"+insertMap.get("IS_PRODUCT_EOL")+"',"+lastDateEOL+",ROUND("+distribution+",2),"+priceListId+",'"+priceListName+"',"+tempId+","+batchId+",SYSDATE,"+userId+",SYSDATE,"+userId+",'"+statusCode+"','"+customerPart+"','"+item_name+"','"+updateDffs+"','"+fileUploadId+"','"+price_line_dff+"')";
						log("insertUploadHistorySql Query :::::::"+insertUploadHistorySql);
					}
					for(int index=1;index<=rangeCount;index++){
						String insertPriceListValues = "";
						/*String rangeSeqId = this.utils.getEmptyForNull((String)insertMap.get("RangeSeqId"+index));						
						//range seqId
						if(rangeSeqId.equalsIgnoreCase("")){
							 rangeSeqId = null;
						}else{
							rangeSeqId = (String)insertMap.get("RangeSeqId"+index);
						}*/
						//R1,R2,R3... 					
						String priceValue = (String)insertMap.get("R"+index),price="";						
						if(/*rangeSeqId != null &&*/ !priceValue.equalsIgnoreCase(""))
						{
							//Added By Narendar for price value round to two decimal points 
							Logger.logNormalMessage("PriceListUploadException ","PriceValue: "+priceValue);

							try{
								double stringPrice = Double.valueOf(priceValue.trim()).doubleValue();
								//price=(new BigDecimal(stringPrice)).setScale(2, BigDecimal.ROUND_DOWN).toString();
								price = String.valueOf(stringPrice);
							}catch (NumberFormatException exception){
								//  D16*1.15 price value is invalid. 
								//Results= "Data Issue: For the Item:"+item_name+" ,the "+priceValue+" price value is invalid. </br>Please correct the data problem. If a formula is used, convert to actual value.";
								//Data Issue: For Item:"+item_name+", the "+priceValue+" price value is invalid. Please correct the data problem. If a formula is used, convert it to the actual value.
							
								
								Results.put("Results","Data Issue: For Item:"+item_name+", the "+priceValue+" price value is invalid.</br>Please correct the data problem. If a formula is used, convert it to the actual value.");
								Results.put("Status", "false");
								
								Logger.logNormalMessage("PriceListUploadException", "MainException ,Results:" +Results);
								this.utils.mailNLogExceptions("Exception while updating Upload PriceList values :" +Results+ " " , exception);
								Logger.logExceptionMessage(exception);		 
								//throw exception;
								status = false;
								break;
							}
							insertPriceListValues = "insert into XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_PRICES(SETUP_SEQ_ID,RANGE_SEQ_ID,PRICE_VALUE,TEMP_ID) VALUES("+setUpSeqId+","+index+",ROUND("+price+",2),"+tempId+")";
							sqlQueries.add(insertPriceListValues);
							log("InsertPriceListValues Query :::::::"+insertPriceListValues);
						}						
					}		//For loop out		    
					//Status
					if(status == false)
					{
						break;
					}					
					log("InsertUploadHistorySql Query ::::::::"+insertUploadHistorySql);
					sqlQueries.add(insertUploadHistorySql);
				}	//while		
				log("While Out :");							
			}
			else{
				log("No Records to Insert :"+insertList.size());
				//Results = "File Upload Error: No Prices or Dffs were updated, since....... ";
				//"File Upload Error: No Prices or DFFs are selected for upload.
				Results.put("Results","File Upload Error: No Prices or DFFs are selected for upload.");
				Results.put("Status", "false");				
				status = false;				
			    }
			
			//Stus 
			
			log("Status ::::::::::::::::::::::::::::"+status);
			
			//if update record is not empty Narendar.Burada 
			/*if(updateList.size() > 0){
				Iterator updateIterator = updateList.iterator();
				while(updateIterator.hasNext()){
					HashMap updateMap = (HashMap)updateIterator.next();
					String newPriceStartDate = (String)updateMap.get("NEW_PRICE_START_DATE");
					String newPriceEndDate = (String)updateMap.get("NEW_PRICE_END_DATE");
					String lastDateEOL = (String)updateMap.get("LAST_ORDER_DATE");
					String distribution = (String)updateMap.get("Distribution");
					String setUpSeqId = (String)updateMap.get("SETUP_SEQ_ID");
					if(distribution.equalsIgnoreCase("")){
						distribution = null;
					}					
					if(newPriceStartDate.equalsIgnoreCase("")){
						newPriceStartDate = null;
					}else{
						newPriceStartDate = "TO_DATE('"+newPriceStartDate+"','MM/DD/YYYY')";
					}
					if(newPriceEndDate.equalsIgnoreCase("")){
						newPriceEndDate = null;
					}else{
						newPriceEndDate = "TO_DATE('"+newPriceEndDate+"','MM/DD/YYYY')";
					}
					if(lastDateEOL.equalsIgnoreCase("")){
						lastDateEOL = null;
					}else{
						lastDateEOL = "TO_DATE('"+lastDateEOL+"','MM/DD/YYYY')";
					}

					String updatePriceListQuery = "update XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_DATA set NEW_PRICE_START_DATE ="+newPriceStartDate+",NEW_PRICE_END_DATE = "+newPriceEndDate+",RECOMMEND_NEW_DESIGNS = '"+updateMap.get("NOTRECOMENDED")+"',IS_PRODUCT_EOL='"+updateMap.get("IS_PRODUCT_EOL")+"', BATCH_ID = "+batchId+",DISTRIBUTION_PRICE = "+distribution+",LAST_DATE_TO_ORDER_EOL ="+lastDateEOL+",LAST_UPDATE_DATE=SYSDATE,LAST_UPDATED_BY="+userId+",STATUS_CODE='X' WHERE TEMP_ID="+updateMap.get("TEMP_ID");
					for(int index=1;index<=rangeCount;index++){
						String rangeSeqId = this.utils.getEmptyForNull((String)updateMap.get("RangeSeqId"+index));
						if(rangeSeqId.equalsIgnoreCase("")){
							 rangeSeqId = null;
						}else{
							rangeSeqId = (String)updateMap.get("RangeSeqId"+index);
						}
						String updatePriceListValuesQuery = "";
						if(!((String)updateMap.get("ORIG_SYS_LINE_REF_"+index)).equalsIgnoreCase(""))
							updatePriceListValuesQuery = "update XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_PRICES set price_value ="+updateMap.get("R"+index)+" where ORIG_SYS_LINE_REF='"+updateMap.get("ORIG_SYS_LINE_REF_"+index)+"' and ORIG_SYS_PRICING_ATTR_REF ='"+updateMap.get("ORIG_SYS_PRICING_ATTR_REF_"+index)+"' and temp_id="+updateMap.get("TEMP_ID");
						else
							updatePriceListValuesQuery = "insert into XXCAVM_CUST.CAVIUM_FC_PLIST_TEMP_PRICES(SETUP_SEQ_ID,RANGE_SEQ_ID,PRICE_VALUE,TEMP_ID) VALUES("+setUpSeqId+","+rangeSeqId+",'"+updateMap.get("R"+index)+"',"+updateMap.get("TEMP_ID")+")";
						sqlQueries.add(updatePriceListValuesQuery);
						log("UpdatePriceListValuesQuery Query :::::::"+updatePriceListValuesQuery);
					}
					sqlQueries.add(updatePriceListQuery);
					log("updatePriceListQuery Query :::::::"+updatePriceListQuery);
				}
			}*/
			
			
			if(status == true)
			{			
			String stringQueries[] = new String[sqlQueries.size()];
			System.arraycopy(sqlQueries.toArray(), 0, stringQueries, 0, sqlQueries.size());
			//status = true;
						
				if(utils.executeUpdate(stringQueries)){
					int requestId = 0;int noOfRecordsUploaded = 0;
					requestId = this.priceListHandler.executePriceListRequestHandler(batchId); //by for testing Narendar
					if(requestId!=0){						
						noOfRecordsUploaded=this.priceListHandler.updateRequestId(batchId,requestId,fileName);
						if(noOfRecordsUploaded == 0){
							//Results="with RequestId: "+requestId +" For Price list upload is un abule updated to Temp table.";
							//File uploaded successfully with Request-ID: "+requestId +". Warning:  The program was unable to update the temp-table with request-id or file-name although the upload was successful.
							Results.put("Results","File uploaded successfully with Request-ID:"+requestId +".<br>Warning:  The program was unable to update the temp-table with request-id or file-name although the upload was successful.");
							Results.put("Status", "true");
							
							log("with RequestId: "+requestId +" For Price list upload is un abule updated to Temp table.");
						}else{
							//Results="with RequestId: "+requestId +" For Price list upload is updated to Temp table.";
							//File uploaded successfully with Request-ID: "+requestId +"
							Results.put("Results","File uploaded successfully with Request-ID: "+requestId+".");
							Results.put("Status", "true");
							log("with RequestId: "+requestId +" For Price list upload is updated to Temp table.");
						}
					}else{
						//Results="File Upload Error: Please check with the Oracle Setups.(Request Id: "+requestId +" )";
						//File Upload Error: Unable to generate a Request-ID. Please check the Oracle setups.Request ID:"+requestId +"."
					
						String results="";
						if(this.priceListHandler.updateStatus(batchId,fileName)==0){
							results="File Upload Error: Unable to generate a Request-ID. Please check the Oracle setups.Request ID:"+requestId +".";
							results+="<br>Warning:  The program was unable to \"Skip\" the Request";
							
						}else{
							results="File Upload Error: Unable to generate a Request-ID. Please check the Oracle setups.Request ID:"+requestId +".";
						}
						Results.put("Results",results);
						Results.put("Status", "false");	
						//-----------------------Additional change:  records in temp-table need to be updated with status “CANCELLED”.------------------
						log("File Upload Error: Please check with the Oracle Setups.(Request Id: "+requestId +" )");
					}					
				}			
		}//status if
		else{//status false
			return Results;
		    }				

		}//try
		 catch (Exception exception ) {
			 Logger.logNormalMessage("PriceListUploadException", "MainException");			
			 this.utils.mailNLogExceptions("Exception while updating Upload PriceList values :" , exception);
			 Logger.logExceptionMessage(exception);	
			 Results.put("Results","Exception "+exception.getMessage());
			 Results.put("Status", "false");
			 //throw exception;
		}
		/*finally{
			    return Results;
		       }*/
		return Results;
	}
	public void log(String message){
		Logger.logDebugMessage(className, message);
	}
	
	
}

