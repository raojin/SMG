package message;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import xsf.ID;
import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataTable;
import xsf.data.Parameter;
import xsf.data.Sql;
import xsf.message.Message;
import xsf.message.Messager;

/**
 * 解析来自流程中的消息，插入到平台的msg_message表和msg_message_user表中
 * @author liuy
 *
 */
public class MessageParse {

	public static boolean sendMessage(String infoId,String pid,String pnid) throws DocumentException {
		
		String bt = "";
		String moduleId = "";
		DataTable table = null;
		
		//从g_infos表里获取bt和moduleId
		//DataTable table = messageCenterDao.getGinfosInfo(infoId);
		Sql sql_0 = new Sql("select bt,module_id from g_infos where id=?");
		
		sql_0.getParameters().add(new Parameter("id",infoId));
		
		table = DBManager.getDataTable(sql_0); 
		
		if(table!=null && table.getRows().size()>0 ){
			 bt = table.getRow(0).getString("bt");
			 moduleId = table.getRow(0).getString("module_id");
		}
		
		//拼接url
		String url = "modules/system/formControl.jsp?moduleId="+moduleId+"&v=1&Info_ID="+infoId+"&pid="+pid+"&pnid="+pnid;
		
		String senderId = "";
		Date sendDate = new Date();
		
		//获取发送者
		//table = messageCenterDao.getSender(pid,pnid);
		Sql sql_1 = new Sql("select t.user_id,t.sdate from g_pnodes t where t.pid=? and t.id=? ");
		
		sql_1.getParameters().add(new Parameter("pid",pid));
		
		sql_1.getParameters().add(new Parameter("id",pnid));
		
		table = DBManager.getDataTable(sql_1); 
		
		if(table!=null && table.getRows().size()>0){
			senderId = table.getRow(0).getString("user_id");
			sendDate = table.getRow(0).getDate("sdate");
		}
		
		//获取接收者
		List<Long> recipientList = new ArrayList<Long>();
		//DataTable recipientTable = messageCenterDao.getRecipient(pid, pnid);
		Sql sql_2 = new Sql("select t.user_id from g_pnodes t where t.pid=? and t.fid = (select fid from g_pnodes where id=? and pid=? )");
		
		sql_2.getParameters().add(new Parameter("pid",pid));
		
		sql_2.getParameters().add(new Parameter("id",pnid));
		
		sql_2.getParameters().add(new Parameter("pid",pid));
			
		 table = DBManager.getDataTable(sql_2);
		
		if(table!=null && table.getRows().size()>0){
			for(DataRow row : table.getRows()){
				 long userId = row.getLong("user_id");
				 recipientList.add(userId);
			}
		}
		  
		//将获取的信息封装到消息体中
		long mid = ID.get16bID();
		Message message = new Message();
		message.setId(mid);
		message.setInfo(infoId);
		message.setFinishType("1");
		message.setFrom(Long.parseLong(senderId));
		message.setCreateTime(new Date());
		
		
		if(recipientList!=null && recipientList.size()>0){
			for(Long id : recipientList){
				message.addMessageProvider(id,Messager.SAMPLE_PROVIDER);
			}
		}
		 	
		//从流程定义表wfdefinition的wf_xml字段中取出配置的消息
		//String xml = messageCenterDao.getWfXMl(pid,pnid); 
		Sql sql_3 = new Sql("select wf_xml from wfdefinition where wf_id = (select wf_id from g_pnodes where pid=? and id=?)");
		
		sql_3.getParameters().add(new Parameter("pid",pid));
		
		sql_3.getParameters().add(new Parameter("id",pnid));
		
		String xml = DBManager.getFieldStringValue(sql_3);
		
		String body = "";
		if(xml!=null && !"".equals(xml)){
			body = getMsgBody(xml,infoId,senderId);
		}
		
		message.setBody(body);    
		message.setTitle(bt); 
		message.setUrl(url);
		message.setSendTime(sendDate);
		
		return Messager.send(message);
		
	}
 
	public static String getMsgBody(String xmlContent,String info_id,String user_id) throws DocumentException{

		//分割字符串，取出具有xml格式的字符串
		String[] xmlArray = xmlContent.split("</LimitTimeType>");
		
		//业务需要的信息存储在<SMS></SMS>中,故取xmlArray[1]来解析
		Document docXml = DocumentHelper.parseText(xmlArray[1]);
		
		//取出SMS标签下的Content标签里的内容
		Node node = docXml.selectSingleNode("/SMS/Content");
		
		String content = node.getText();
		
		//取出SMS标签下的Sql标签里的内容
		Node nodeSql = docXml.selectSingleNode("/SMS/Sql");
		
		String sqlStr = nodeSql.getText();
		
		String[] sqls = sqlStr.split(";");
		
		//取出sql语句中的占位符，如[INFO_ID]
		String param_0 = getParamsFromSql(sqls[0]);
		
		//将占位符替换成现有的参数
		String sql_0 = sqls[0].replace(param_0, info_id);
		
		String param_1 = getParamsFromSql(sqls[1]);
		
		String sql_1 = sqls[1].replace(param_1, user_id);
		
		String mname = "";
		String bt = "";
		
		DataTable table = DBManager.getDataTable(sql_0);
		
		if(table != null && table.getRowCount()>0){
			mname = table.getRow(0).getString("MNAME");
			bt = table.getRow(0).getString("BT");
		}
		
		String name = DBManager.getFieldStringValue(sql_1);
		
		String content_0 = content.replace("[标题]", bt);
		String content_1 = content_0.replace("[类型]", mname);
		String content_2 = content_1.replace("[发送人]", name);
		
		return content_2;
		
	}
	
	public static  String getParamsFromSql(String sql){
		
		int beginIndex = sql.indexOf("[");
		
		int endIndex = sql.indexOf("]");
		
		String param = sql.substring(beginIndex, endIndex+1);//subString取出的内容为 [...]
		
		return param;
	}
	
	 //从流程定义表wfdefinition的wf_xml字段中取出配置的消息
	 public static String getXmlFromWfdefinitionTable(String wf_id){
		
		Sql sql_ = new Sql("select wf_xml from wfdefinition where wf_id=? ");
		
		sql_.getParameters().add(new Parameter("wf_id",wf_id));
		 
		String xml = DBManager.getFieldStringValue(sql_);
		
		return xml;
	 }
	
	 
	 //从读取的xml里获取sql语句
	 public static String getSqlFromXml(String xml) throws DocumentException{
		 
		//分割字符串，取出具有xml格式的字符串
		String[] xmlArray = xml.split("</LimitTimeType>");
		
		//业务需要的信息存储在<SMS></SMS>中,故取xmlArray[1]来解析
		Document docXml = DocumentHelper.parseText(xmlArray[1]);
		
		//取出SMS标签下的Sql标签里的内容
		Node nodeSql = docXml.selectSingleNode("/SMS/Sql");
		
		String sqlStr = nodeSql.getText();
		
		 return sqlStr;
	 }
	 
	//从读取的xml里获取消息内容
	public static String getContentFromXml(String xml) throws DocumentException{
	 
		//分割字符串，取出具有xml格式的字符串
		String[] xmlArray = xml.split("</LimitTimeType>");
		
		//业务需要的信息存储在<SMS></SMS>中,故取xmlArray[1]来解析
		Document docXml = DocumentHelper.parseText(xmlArray[1]);
		
		//取出SMS标签下的Sql标签里的内容
		Node nodeSql = docXml.selectSingleNode("/SMS/Content");
		
		String content = nodeSql.getText();
		
		 return content;
	}
		 
	 
	 //获取已处理好的sql,查询并拼接消息内容
	 public static String getMsgContent(String sqls,String content) throws Exception{
		
		 String bt = "";
		 
		 String type = "";
		 
		 String name = "";
		 
		 List<DataTable> tableList = doCommandSql(sqls);
		 
		 for(DataTable table : tableList){
			 
			 if(table!=null && table.getRowCount()>0){
				
				 if(table.containsColumn("BT")){
					 
					 bt = table.getRow(0).getString("BT");
					 
				 }
				 
				if(table.containsColumn("MNAME")){
					 
					type = table.getRow(0).getString("MNAME");
					 
				}
				
				if(table.containsColumn("NAME")){
					 
					 name = table.getRow(0).getString("NAME");
					 
				}
				 
			 }
		 }
		
		
		int keyCount = 3;
		
	    String[] sKey = new String[keyCount];
	    
	    String[] sValue = new String[keyCount];
	    
	    // 标题
	    sKey[0] = "[标题]";
	    
	    sValue[0] = bt;
	    
	    // 类型
	    sKey[1] = "[类型]";
	    
	    sValue[1] = type;
	    
	    // 发送人
	    sKey[2] = "[发送人]";
	    
	    sValue[2] = name;
	    
	    String con = content;
	    
	    for (int i = 0; i < keyCount; i++) {
	    	
	    	con = con.replace(sKey[i], sValue[i]);
	      
	    }
	    
	    String btContent = bt + "," + con;
	    
	    return btContent;
		
	 }
	 
	 //批量的执行Sql
	 public static List<DataTable> doCommandSql(String sqls) {
		 
		 DataTable table = null;
		 
		 List<DataTable> list = new ArrayList<DataTable>();
		 
		 if (sqls != null && !"".equals(sqls)) {
		
		     String[] sqlArray = sqls.split(";");
		     
		     for (String subSql : sqlArray) { 	  
		    	 
		    	 table = DBManager.getDataTable(subSql);
		    	 
		    	 list.add(table);
		    	 
		    }
	   }
		return list;
   } 
	 

	 //从wfnodelist表中取出配置的提醒方式，如短信通知、邮件提醒、托盘提醒
	 public static String getXmlFromWfnodelist(String wf_id,String wfnode_id){
		 
		Sql sql_ = new Sql("select wfnode_xml from wfnodelist where  wf_id=? and wfnode_id=? ");
			
		sql_.getParameters().add(new Parameter("wf_id",wf_id));
		
		sql_.getParameters().add(new Parameter("wfnode_id",wfnode_id));
			 
		String xml = DBManager.getFieldStringValue(sql_);
			
		return xml;
		
	 }
	 
	//从读取的xml里获取消息内容
	public static String getTypeFromXml(String xml) throws DocumentException{
	 
		int beginIndex = xml.indexOf("<BaseInfo>");
		
		int endIndex = xml.indexOf("</BaseInfo>");
		
		String baseInfo = xml.substring(beginIndex, endIndex+11);//subString取出的内容为<BaseInfo>...</BaseInfo>
		
		Document docXml = DocumentHelper.parseText(baseInfo);
		
		Node textNode = docXml.selectSingleNode("/BaseInfo/IsForword");
		
		Node emailNode = docXml.selectSingleNode("/BaseInfo/IsEMail");
		
		Node trayNode = docXml.selectSingleNode("/BaseInfo/IsTray");
		
		String textContent = textNode.getText();
		
		String emailContent = emailNode.getText();
		
		String trayContent = trayNode.getText();
		
		String type = textContent + "," + emailContent + "," + trayContent;
		
		 return type;
	}
	
	//发送消息
	public static boolean sendMsg(String infoId,String sId,String rId,String body,String bt,Date sendDate,String pid,String pnid){
		
		//将获取的信息封装到消息体中
		long mid = ID.get16bID();
		
		Message message = new Message();
		
		message.setId(mid);
		
		message.setInfo(infoId);
		
		message.setFinishType("1");
		
		message.setFrom(Long.parseLong(sId));
		
		message.setCreateTime(new Date());
		
		message.addMessageProvider(Long.parseLong(rId),Messager.SAMPLE_PROVIDER);   
		
		message.setTitle(bt); 
		
		message.setBody(body);
		
		String moduleId = DBManager.getFieldStringValue("select module_id from g_infos where id="+ infoId);
		
		//拼接url
		String url = "modules/system/formControl.jsp?moduleId="+moduleId+"&v=1&Info_ID="+infoId+"&pid="+pid+"&pnid="+pnid;
		
		message.setUrl(url);
		
		message.setSendTime(sendDate);
		
		return Messager.send(message);
	} 
	
	public static void main(String[] args) {
		
		String xml = "<Position><X>199</X><Y>111</Y></Position>" +
				"<BaseInfo><Index>1</Index><Caption>拟稿</Caption><Order>1</Order><Desc>拟稿</Desc><Type>1</Type><SubFlowID/><SendMethod>0</SendMethod><IsWait>0</IsWait><IsForword>0</IsForword><IsEMail>0</IsEMail><IsTray>0</IsTray><IsExpiry>0</IsExpiry><Expiry>0</Expiry><TimeType>0</TimeType><InLineFlag>0</InLineFlag><OutLineFlag>0</OutLineFlag><Rate>0</Rate></BaseInfo>" +
				"<ACL><IsRoleSelect>0</IsRoleSelect><IsSelected>0</IsSelected><IsOnlyOneUser>0</IsOnlyOneUser><IsAutoSend>0</IsAutoSend><IsAutoExpand>0</IsAutoExpand><IsMultiUser>0</IsMultiUser><type>0</type></ACL><ButtonInfo/><ADSet><HZNode/></ADSet><OtherControl><TSNodeList>,</TSNodeList><BeforeSendSql/><AfterSendSql/><BackSendSql/><AutoSelectUserSql/></OtherControl>";
		int beginIndex = xml.indexOf("<BaseInfo>");
		
		int endIndex = xml.indexOf("</BaseInfo>");
		
		String param = xml.substring(beginIndex, endIndex+11);//subString取出的内容为<BaseInfo>...</BaseInfo>
		
		System.out.println(param); 
		
	}
}

