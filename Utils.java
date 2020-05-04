package com.cavium.forecast.util;

import java.io.PrintWriter; 
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cavium.forecast.db.ConnectionPool;
import com.cavium.forecast.logger.Logger;

/**
 * @author jgsoedl
 *  
 */
public class Utils {
	
	/**
	 * Public message property
	 */
	public String message = null;
	
	private MailClient mailClient = null;
	/**
	 * Uils class constructor
	 */
	public Utils(){
      // Nothing to do.
		try
		{
			
			this.mailClient = new MailClient();
		}
		catch(Exception exception)
		{
			Logger.logExceptionMessage(exception);
		}
	}

	/**
	 * Returns string with \r\n replaced by "<BR>"
	 * @param InString 
	 * @return : Returns string with \r\n replaced by "<BR>"
	 */
	public String replaceCRLF(String InString) {
       return(InString.replaceAll("[\n]", "<BR>"));
	}
	
	/**
	 * @param value
	 * @return escaped textfield value string.
	 */
	public String replaceQuote(String value){
		String retVal;
		if (!value.equals("")){
			retVal=value.replaceAll("\"","&quot;");
			retVal=value.replaceAll("\'","&#39;");
		}else{
			retVal=value;
		}
		return(retVal);
	}
	
	/**
	 * @return Message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param strMessage
	 */
	public void setMessage(String strMessage) {
		this.message = strMessage;
	}
	
	 public long compareTo( java.util.Date date1, java.util.Date date2 )  
	 {  
	 //returns negative value if date1 is before date2  
	 //returns 0 if dates are even  
	 //returns positive value if date1 is after date2  
	   return date1.getTime() - date2.getTime();  
	 }  
	
	
	
	/**
	 * @param InString 
	 * @return String
	 * Escapes ' with '' for SQL strings.
	 */
	public String dbTize(String InString) {
		String source=InString.trim();
		if (source.indexOf('\'') == -1) {
			return (source);
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < source.length(); i++) {
			sb.append(source.charAt(i));
			if (source.charAt(i) == '\'')
				sb.append('\'');
		}
		return sb.toString();
	}
	
	/**
	 * @param PlainPassword
	 * @return encrypted password.
	 */
	public String getEncryptedPassword(String PlainPassword){
		REPencrypt encr = new REPencrypt();
		String encrpw="";
		try {
			encrpw=encr.getHash(PlainPassword);
			return(encrpw);
		}catch (Exception e){
			return("User.getEncryptedPassword: " + e.toString());
		}finally{
			encr = null;
		}
	}
	
	
	/**
	 * @param str
	 * @return empty string if null; otherwise input string.
	 */
	public String getEmptyForNull(String str)
	{
		if(str == null)
			return "";
		return str.trim();
	
	}
	
	public String getNullForEmpty(String str)
	{
		if(str == ""){}
			return null;
		//return null;	
	}
	
	/**
	 * @param str
	 * @return 0 if null; otherwise the input string.
	 */
	public String getZeroForNull(String str)
	{
		if(str == null)
			return "0";
		return str;
	
	}
	
	
	
	/**
	 * executes an update/insert/delete query against DB
	 * @param queries , an arryay of queries
	 * @throws SQLException
	 */
	public boolean executeUpdate(String[] queries) throws Exception
	{
		Connection connection = null;
		Statement statement = null;
		boolean status = false;
		
		try {
			connection = ConnectionPool.getInstance().getConnection();
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			
			for (int index =0; index < queries.length; index++)
			{
				if(!queries[index].equalsIgnoreCase(""))
					logError("SQL_test >>> ::: " + queries[index]);
			}
			
			
			//adding queries to batch
			for (int index =0; index < queries.length; index++)
			{
				if(!queries[index].equalsIgnoreCase(""))
					statement.addBatch(queries[index]);
			}
			statement.executeBatch();
			statement.close();
			connection.commit();
			status = true;
		}
		catch(Exception sqlException)
		{
			status = false;
			logError("Exception in executing following queries");
			for (int index =0; index < queries.length; index++)
			{
				if(!queries[index].equalsIgnoreCase(""))
					logError("SQL>>> ::: " + queries[index]);
			}
			Logger.logExceptionMessage(sqlException);
			connection.rollback();
			throw sqlException;
		}
		finally
		{
			connection.setAutoCommit(true);
			ConnectionPool.getInstance().returnConnection(connection);
		}
		return status;
	}
	
	/**
	 * @param from
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param messageBody
	 * @param exception
	 * @throws Exception
	 */
	public void sendMail(String from, String to, String cc, String bcc,
			String subject, String messageBody, Exception exception) throws Exception
	{
		if(MailBundle.getProperty("sendErrorMails").equalsIgnoreCase("true"))
		{
			java.io.StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			messageBody += "</br> " + sw.toString();
			String instanceName = AppsBundle.getProperty("instanceName");
			if(instanceName != null && instanceName.equalsIgnoreCase(""))
			{
				instanceName = "PROD";
			}
			mailClient.sendMail(from, to, cc, bcc, subject + " - " + instanceName, messageBody);
		}
	}
	
	/**
	 * Mails and Logs exception generated.
	 * @param methodName
	 * @param className
	 * @param message
	 * @param exception
	 * @throws Exception
	 */
	public void mailNLogExceptions(String message, Exception exception) throws Exception {
		Logger.logErrorMessage(exception.getStackTrace()[0].getMethodName(), message);
		Logger.logExceptionMessage(exception);
		String errorMessage = "Error occurred in " + exception.getStackTrace()[0].getMethodName() + 
			" method of " + exception.getStackTrace()[0].getMethodName() + ", </br>";
		errorMessage += message;
		errorMessage += "</br>" + exception.getMessage();
		this.sendMail(MailBundle.getProperty("ForecastAdminMail"), MailBundle.getProperty("MailErrorsTo"), 
				null, null, MailBundle.getProperty("ErrorSubject"), errorMessage, exception);
	}
	
	public String renderSelectOptions(ArrayList list,String ID, String selectedValue,String strClass,String strEvent, String attribute) throws Exception{
		StringBuffer strDL=null;
	    strDL=new StringBuffer("<select id='" + ID + "' name='" + ID + "' class='" + strClass + "'" + strEvent + attribute + ">");
	    strDL.append("<option value=''></option>");
	    try{
	    if (list != null){
		    Iterator listIterator = list.iterator();
		    Hashtable weeklyStatus = null;
		    while (listIterator.hasNext())
		    { 
		    	weeklyStatus = (Hashtable)listIterator.next();
		    	strDL.append("<option value='" + (String)weeklyStatus.get("code") + "'");
			   if (weeklyStatus!=null && selectedValue!=null && weeklyStatus.get("code").equals(selectedValue) ){
				   strDL.append(" SELECTED");
			   }
			   strDL.append(">" + (String)weeklyStatus.get("meaning") + "</option>");
			}
		    strDL.append("</select>");
			Logger.logDebugMessage("Utils", strDL.toString());
		 }
	    return strDL.toString();
	    }catch(Exception exception) {
			throw exception;
		}
	}
	
	/**
	 * Returns Textbox String
	 * @param 
	 * @return : String
	 */
	public String renderTB(String ID, String strValue, String strClass, String strSize,String strMaxLength, String autoComplete, String readOnly, String strEvent) throws Exception {
		try {
			return( "<input type='text' id='"+ ID + "' name='" + ID + "' value='" + replaceQuote(strValue) + "' class='" + strClass +"' size='" + strSize +"'  maxlength='" + strMaxLength + "' autocomplete='" + autoComplete +"'" + readOnly + strEvent + ">");
		}
		catch(Exception exception) {
			throw exception;
		} 
	}
	
	/**
     * converts lov Data into JSON Format
     * @param lovData
     * @return
     * @throws Exception
     */
    public JSONArray getLOVDataInJSONFormat(ArrayList lovData) throws Exception {
        JSONArray lovRows = null; 
        try{
            if(lovData != null && lovData.size() > 0) {
                logError("Got lov data converting into LOV JSON Format");
                lovRows = new JSONArray();
                Iterator lovIterator = lovData.iterator();
                while(lovIterator.hasNext()) {
                    Hashtable dataMap = (Hashtable)lovIterator.next();
                    JSONObject lovRow = new JSONObject();
                    lovRow.putAll(dataMap);
                    lovRows.add(lovRow);
                }
            }
            return lovRows;
        }
        catch(Exception except) {
            throw except;
        }         
    }
	
    /**
     * Rends a LOV of a semi-colon separated property file list
     * @param Property-File Key, LOV ID, LOV selected value, LOV class, LOV event.
     * @returns LOV string
     * @throws Exception
    **/
	public String getWeeklyIncludeStatus(String propertyFileKey, String ID, String selectedValue,String strClass,String strEvent) throws Exception {
		try {
			String strDL=""; 
			if(propertyFileKey!=null && !"".equals(propertyFileKey)){
				String strList=AppsBundle.getProperty(propertyFileKey);
				String valueList[]=strList.split(";");
				strDL+="<select id='" + ID + "' name='" + ID + "' class='" + strClass + "'" + strEvent + ">";
				strDL+="<option value=''></option>";
				for(int i=0;i<valueList.length;i++){
					strDL+="<option value=\"" + valueList[i] + "\"";                                
					if(selectedValue!=null && !"".equals(selectedValue)){
						if (valueList[i].equalsIgnoreCase(selectedValue))
							strDL+=" SELECTED";
					}                                
					strDL+=">" + valueList[i] + "</option>";
				}
				strDL+="</select>";
				Logger.logDebugMessage("Utils :getWeeklyIncludeStatus", strDL);
			}
			return strDL;
		}catch(Exception exception) {
			throw exception;
		}	
	}
	
	public String renderDwProjection(ArrayList dwRevList,String SectionType){
		StringBuffer str = null;
		DecimalFormat money = new DecimalFormat("#,###,##0.00"); 
		double total,totalDwQtr01,totalDwQtr02,totalDwQtr03,totalDwQtr04,grandTotal;
		total=totalDwQtr01=totalDwQtr02=totalDwQtr03=totalDwQtr04=grandTotal=0.0;
		Hashtable ht=null;	 
		if(dwRevList!=null){
			Iterator itr= dwRevList.iterator();
			str=new StringBuffer("<table id='tblDWRev"+SectionType+"' width='100%' cellspacing='0' border='0' cellpadding='0' align='left'>");
			str.append("<tr><td class='TBLHD1011C' width='15%'>&nbsp;</td>");
			str.append("<td class='TBLHD1011C' width='15%'>Qtr-1</td>");
			str.append("<td class='TBLHD1011C' width='15%'>Qtr-2</td>");
			str.append("<td class='TBLHD1011C' width='15%'>Qtr-3</td>");
			str.append("<td class='TBLHD1011C' width='15%'>Qtr-4</td>");
			str.append("<td class='TBLHD1111C' width='25%'>Total</td></tr>");
			while(itr.hasNext()){
				ht=(Hashtable)itr.next();
				total=Double.valueOf((String)ht.get("dwQtr01")).doubleValue()+Double.valueOf((String)ht.get("dwQtr02")).doubleValue()+Double.valueOf((String)ht.get("dwQtr03")).doubleValue()+Double.valueOf((String)ht.get("dwQtr04")).doubleValue();
				totalDwQtr01+=Double.valueOf((String)ht.get("dwQtr01")).doubleValue();
				totalDwQtr02+=Double.valueOf((String)ht.get("dwQtr02")).doubleValue();
				totalDwQtr03+=Double.valueOf((String)ht.get("dwQtr03")).doubleValue();
				totalDwQtr04+=Double.valueOf((String)ht.get("dwQtr04")).doubleValue();
				grandTotal+=total;

				str.append("<tr><td class='TBLDT0011L' style='background-color:#eeeeee;' width='15%' align='center'>"+ht.get("dwYear")+"</td>");
				str.append("<td class='TBLDT0011L'  id='dwQtr01' width='15%' align='right'>$"+money.format(Double.valueOf((String)ht.get("dwQtr01")).doubleValue())+"</td>"); 
				str.append("<td class='TBLDT0011L'  id='dwQtr02' width='15%' align='right'>$"+money.format(Double.valueOf((String)ht.get("dwQtr02")).doubleValue())+"</td>");  
				str.append("<td class='TBLDT0011L'  id='dwQtr03' width='15%' align='right'>$"+money.format(Double.valueOf((String)ht.get("dwQtr03")).doubleValue())+"</td>");  
				str.append("<td class='TBLDT0011L'  id='dwQtr04' width='15%' align='right'>$"+money.format(Double.valueOf((String)ht.get("dwQtr04")).doubleValue())+"</td>");  
				str.append("<td class='TBLDT0111L' id='dwQtrTotal' width='25%' align='right'>$"+money.format(total)+"</td></tr>");
			}
			str.append("<tr><td class='TBLDT0011L' style='background-color:#eeeeee;' width='15%'>&nbsp;</td>");
			str.append("<td class='TBLDT0011L'  id='dwQtr01' width='15%' align='right'>$"+money.format(totalDwQtr01)+"</td>"); 
			str.append("<td class='TBLDT0011L'  id='dwQtr02' width='15%' align='right'>$"+money.format(totalDwQtr02)+"</td>"); 
			str.append("<td class='TBLDT0011L'  id='dwQtr03' width='15%' align='right'>$"+money.format(totalDwQtr03)+"</td>");  
			str.append("<td class='TBLDT0011L'  id='dwQtr04' width='15%' align='right'>$"+money.format(totalDwQtr04)+"</td>");  
			str.append("<td class='TBLDT0111L' id='dwQtrTotal' width='25%' align='right'>$"+money.format(grandTotal)+"</td></tr>");
			str.append("</table>");
		}
		return (str!=null)?str.toString():"";
}	
	
	
	
	
	private static void logError(String message)
	{
		Logger.logErrorMessage("Utils", message);
	}
}
