package message;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import xsf.ID;
import xsf.data.CommandCollection;
import xsf.data.DBManager; 
import xsf.data.DataRow;
import xsf.data.DataTable;
import xsf.data.Parameter;
import xsf.data.Sql;
import xsf.util.StringHelper;
import message.Message;
import message.Messager;
import message.vo.MessageProvider;

/**
 * 解析来自流程中的消息，插入到平台的msg_message表和msg_message_user表中
 * @author liuy
 *
 */
public class MessageParser { 
	 
	/**
	 * 从流程定义表wfdefinition的wf_xml字段中取出配置的消息
	 * @author liuyang
	 * @param wf_id 流程ID
	 * @return 表wfdefinition的wf_xml字段值
	 * */
	 public static String getXmlFromWfdefinitionTable(String wf_id){
		
		Sql sql_ = new Sql("select wf_xml from wfdefinition where wf_id=? ");
		
		sql_.getParameters().add(new Parameter("wf_id",wf_id));
		 
		String xml = DBManager.getFieldStringValue(sql_);
		
		return xml;
	 }
	
	 /**
	  * 添加标签，将此处得到的xml格式化为标准的xml
	  * @param xml 
	  * @return 标准的xml
	  */
	 public static String formatXML(String xml){
		 
		 StringBuffer sb = new StringBuffer();
		 
		 sb.append("<Root>");
		 
		 sb.append(xml);
		 
		 sb.append("</Root>");
		 
		 String formatXML = sb.toString();
		 
		 return formatXML;
	 }
	 
	 /**
	  * 获取标签体内容
	  * @param xml
	  * @param tagName 标签名
	  * @return 标签体内容
	 * @throws DocumentException 
	  */
	 public static String getBodyFromXml(String xml,String tagName) throws DocumentException{
		 
		 //获取document对象 
		 Document document = DocumentHelper.parseText(xml);
		 
		 //获取节点 
		 Node node = document.selectSingleNode(tagName);
		 
		 //得到内容
		 String body = node.getText(); 
		 
		 return body; 
	 }
	 
	 /**
	  * 获取存放Field和Name标签内容的map,如<Field>NAME</Field><Name>发送人</Name>  map.put("NAME","发送人")
	  * @param xml 
	  * @return
	  */
	 public static Map<String,String> getfieldMap(String xml)  throws DocumentException{ 
		 
		Map<String, String> fieldMap =  new HashMap<String, String>();  
		
		Document docXml = DocumentHelper.parseText(xml);
		
		List<Object> nodes = docXml.selectNodes("/Root/SMS/Key");
		
		for(Object obj : nodes){
			
			 Node node = (Node) obj;
			 
			 String subXml = node.asXML();
			 
			 String field = getBodyFromXml(subXml,"/Key/Field");
			 
			 String name = getBodyFromXml(subXml,"/Key/Name");
			 
			 fieldMap.put(field, name); 
			 
		} 
  
		return fieldMap; 
	 }
	 
	 /**
	  * 获取消息内容
	  * @param sqls 配置的sql
	  * @param xml 配置的xml
	  * @return 
	 * @throws DocumentException 
	  */
	 public static String getMessage(String xml,String sqls) throws Exception{ 
		 
		 Map<String, String> nameMap =  new HashMap<String, String>(); 
		 
		 Map<String,String> fieldMap = getfieldMap(xml); 
		 
		 List<DataTable> list = doCommandSql(sqls);
		 
		 for(DataTable table : list){
			 
			 if(table!=null && table.getRowCount()>0){
				 
				 for(Map.Entry<String,String> entry : fieldMap.entrySet()){ 
					 
					 if(table.containsColumn(entry.getKey())){
						 
						 String reVal = table.getRow(0).getString(entry.getKey());
						 
						 //nameMap:key [Name标签内容]  value:数据库查出的结果  如 map.put("[标题]","关于xxx的通知"); 
						 nameMap.put("["+entry.getValue()+"]", reVal); 
						 
					 }
					 
				 }  
			 }
		 }
		 
		 //获取配置的消息内容
		 String text = getBodyFromXml(xml, "/Root/SMS/Content");
		 
		 //获取发送的消息内容
		 String content = getRealMsgContent(text,nameMap);
		 
		 return content; 
	 }
	 
	/**
	 * 执行sql,sql语句个数是不确定的
	 * @param sqls
	 * @return
	 */
	 public static List<DataTable> doCommandSql(String sqls) throws Exception{
		 
		 DataTable table = null;
		 
		 List<DataTable> list = new ArrayList<DataTable>();
		 
		 if (sqls != null && !"".equals(sqls)) {
		
		     String[] sqlArray = sqls.split(";");
		     
		     for (String subSql : sqlArray) { 	  
		    	 
		    	 //-------------------->try catch<--------------------
		    	 table = DBManager.getDataTable(subSql);
		    	 
		    	 list.add(table);
		    	 
		    }
	     }
		 
		 return list; 
	  } 
	 
	 /**
	  * 获取需要发送的消息的内容
	  * @param content 配置的消息内容
	  * @param keyCount 消息内容中替换参数的个数
	  * @return
	  */
	 public static String getRealMsgContent(String content, Map<String, String> nameMap){
		
		int count = 0;
		
		String[] sKey = new String[nameMap.size()];
		
		String[] sValue = new String[nameMap.size()];
		
		for(Map.Entry<String,String> entry : nameMap.entrySet()){
			
			sKey[count] = entry.getKey();
			
			sValue[count] = entry.getValue();
			
			count++;
		}
		
		for (int i = 0; i < count; i++) {
			
			content = content.replace(sKey[i], sValue[i]);
		  
		}
		
		return content; 
	 }  
	 
	 /**
	  * 从wfnodelist表中取出配置的提醒方式，如短信通知、邮件提醒、托盘提醒
	  * @param wf_id 流程ID
	  * @param wfnode_id 流程节点ID
	  * @return
	  */
	 public static String getXmlFromWfnodelist(String wf_id,String wfnode_id){
		 
		Sql sql_ = new Sql("select wfnode_xml from wfnodelist where  wf_id=? and wfnode_id=? ");
			
		sql_.getParameters().add(new Parameter("wf_id",wf_id));
		
		sql_.getParameters().add(new Parameter("wfnode_id",wfnode_id));
			 
		String xml = DBManager.getFieldStringValue(sql_);
			
		return xml; 
	 }
	 

	 /**
	  * 从读取的xml里获取消息内容
	  * @param xml
	  * @return
	  * @throws DocumentException
	  */
	public static String getTypeFromXml(String xml) throws DocumentException{
		
		String IsForwordType = getBodyFromXml(xml,"/Root/BaseInfo/IsForword");
		
		String IsEMailType = getBodyFromXml(xml,"/Root/BaseInfo/IsEMail");
		
		String IsTrayType = getBodyFromXml(xml,"/Root/BaseInfo/IsTray"); 
		
		String type = IsForwordType + "," + IsEMailType + "," + IsTrayType;
		
		 return type;
	}
	
	/**
	 * 将消息封装到平台的消息体中，发送消息
	 * @param map 参数集合
	 * @param sendTime 发送时间
	 * @param sendType 发送类型  如，短信提醒、邮件提醒、托盘提醒
	 * @return
	 */
	public static boolean sendMsg(Map<String,String> map,Date sendTime,String sendType,String content){
		
		//将获取的信息封装到消息体中
		long mid = ID.get16bID();
		
		Message message = new Message();
		
		message.setId(mid);
		
		message.setInfo(map.get("infoId"));
		
		message.setFinishType("1");
		
		message.setFrom(Long.parseLong(map.get("sUserId")));
		
		message.setCreateTime(new Date());
		
		if("isForword".equals(sendType)){
			
			message.addMessageProvider(Long.parseLong(map.get("rUserId")),Messager.SNS_PROVIDER); //短信提醒
			
		}else if("isMail".equals(sendType)){
			
			message.addMessageProvider(Long.parseLong(map.get("rUserId")),Messager.MAIL_PROVIDER); //邮件提醒
			
		}else if("isTray".equals(sendType)){
			
			message.addMessageProvider(Long.parseLong(map.get("rUserId")),Messager.SAMPLE_REPLY_PROVIDER); //即使通讯
		}else if("isTuoPan".equals(sendType)){
			
			message.addMessageProvider(Long.parseLong(map.get("rUserId")),Messager.SAMPLE_REPLY_TUOPAN); //托盘提醒
		}
		
		message.setTitle(content); 
		
		message.setBody(map.get("body"));
		
		//拼接url
		String url = "modules/system/formControl.jsp?moduleId="+map.get("moduleId")+"&Info_ID="+map.get("infoId")+"&pid="+map.get("pid")+"&pnid="+map.get("pnid");
		
		message.setUrl(url);
		
		message.setSendTime(sendTime);
		message.setInfo_id(map.get("infoId"));
		return Messager.send(message);
	} 
	
	/*
	 * 针对发送短信
	 */
	public static boolean sendMsg_user(Map<String, String> map, Date sendTime,
			String sendType, String content) {

		// 将获取的信息封装到消息体中
		long mid = ID.get16bID();

		Message message = new Message();

		message.setId(mid);

		message.setInfo(map.get("infoId"));

		message.setFinishType("1");

		message.setFrom(Long.parseLong(map.get("sUserId")));

		message.setCreateTime(new Date());

		message.addMessageProvider(Long.parseLong(map.get("rUserId")),
				Messager.SNS_PROVIDER); // 短信提醒

		message.setTitle(content);

		message.setBody(map.get("body"));

		// 拼接url
		String url = "modules/system/formControl.jsp?moduleId="
				+ map.get("moduleId") + "&Info_ID=" + map.get("infoId")
				+ "&pid=" + map.get("pid") + "&pnid=" + map.get("pnid");

		message.setUrl(url);

		message.setSendTime(sendTime);

		CommandCollection collection = new CommandCollection();
		String sUserId = map.get("sUserId");
		String rUserId = map.get("rUserId");
		Map<String,String> userMap = getUserMap(sUserId+","+rUserId);
		String sUserNameAndMobile = userMap.get(sUserId);
		String rUserNameAndMobile = userMap.get(rUserId);
		String sUserName = sUserNameAndMobile.split(";")[0];
		String sUserMobile = sUserNameAndMobile.split(";")[1];
		String rUserName = rUserNameAndMobile.split(";")[0];
		String rUserMobile = rUserNameAndMobile.split(";")[1];
		
		if(StringHelper.isNullOrEmpty(rUserMobile)||"null".equals(rUserMobile)){
			return false;
		}
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd");
		
		//判断是否还可以发送短信
//		boolean flag = isSendSMS(sUserId);
//		if(!flag){
//			return false;
//		}
		String result = SMSInterface.sendSMS1(sUserId,content,
				sdf1.format(sendTime),rUserMobile);
		if(!"OK".equals(result)){
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Sql insertSMS = new Sql(
				"INSERT INTO MSG_MESSAGE(ID,TITLE,BODY,USERID,CREATETIME,"
						+ "SENDTIME,ROWSTATE,PROVIDER,SENDUSERIDS,SENDUSER,USERNAME,USERMOBILE," +
								"SENDUSERNAMES,SENDUSERMOBILES) VALUES(?ID,?TITLE,?BODY,?USERID,to_date(?CREATETIME,'yyyy-mm-dd hh24:mi:ss'),"
						+ "to_date(?SENDTIME,'yyyy-mm-dd hh24:mi:ss'),?ROWSTATE,?PROVIDER,?ASENDUSERIDS,?SENDUSER,?USERNAME,?USERMOBILE," +
								"?SENDUSERNAMES,?SENDUSERMOBILES)");
		insertSMS.getParameters().add(new Parameter("ID", mid));
		insertSMS.getParameters().add(new Parameter("TITLE", content));
		insertSMS.getParameters().add(new Parameter("BODY", content));
		insertSMS.getParameters().add(
				new Parameter("USERID", Long.parseLong(sUserId)));
		insertSMS.getParameters().add(
				new Parameter("CREATETIME", sdf.format(new Date())));
		insertSMS.getParameters().add(new Parameter("SENDTIME", sdf.format(new Date())));
		insertSMS.getParameters().add(new Parameter("ROWSTATE", "1"));
		insertSMS.getParameters().add(new Parameter("PROVIDER", "sms"));
		insertSMS.getParameters()
				.add(new Parameter("ASENDUSERIDS", Long.parseLong(rUserId)));
		insertSMS.getParameters().add(new Parameter("SENDUSER", rUserName+"<"+rUserMobile+">;"));
		insertSMS.getParameters().add(new Parameter("USERNAME", sUserName));
		insertSMS.getParameters().add(new Parameter("USERMOBILE", sUserMobile));
		insertSMS.getParameters().add(new Parameter("SENDUSERNAMES", rUserName+";"));
		insertSMS.getParameters().add(new Parameter("SENDUSERMOBILES", rUserMobile+";"));
		collection.add(insertSMS);

		List<MessageProvider> messageProviders = message.getMessageProviders();

		if (messageProviders == null || messageProviders.size() == 0) {
			return false;
		}
		int i = 0;
		for (MessageProvider messageProvider : messageProviders) {

			Sql insertUser = new Sql(
					"INSERT INTO MSG_MESSAGE_USER(ID,MESSAGE_ID,"
							+ "PROVIDER,USER_ID,STATUS,TYPE,ROWSTATE,USERNAME,USERMOBILE,SENDORRECEIVETIME) "
							+ "VALUES(?ID,?MESSAGE_ID,?PROVIDER,?USER_ID,?STATUS,?TYPE,"
							+ "?ROWSTATE,?USERNAME,?USERMOBILE,sysdate)");
			insertUser.getParameters().add(new Parameter("ID", ID.get16bID()));
			insertUser.getParameters().add(new Parameter("MESSAGE_ID", mid));
			insertUser.getParameters().add(new Parameter("PROVIDER", "sms"));
			insertUser.getParameters()
					.add(new Parameter("USER_ID", Long.parseLong(map
							.get("rUserId"))));
			insertUser.getParameters().add(new Parameter("STATUS", "1"));
			insertUser.getParameters().add(new Parameter("TYPE", "1"));
			insertUser.getParameters().add(new Parameter("ROWSTATE", "0"));
			
			insertUser.getParameters().add(new Parameter("USERNAME", rUserName));
			insertUser.getParameters().add(new Parameter("USERMOBILE",rUserMobile));
			i++;
			collection.add(insertUser);
		}
		int size = content.length();
		if(size>=70){
			int num = size/65;
			int num1 =  size%65;
			if(num1>0){
				num = num+1;
			}
			i = i*num;
		}
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String currentMonth = "";
		String currentDay = "";
		if(month<10){
			currentMonth = "0"+month;
		}else{
			currentMonth = ""+month;
		}
		if(day<10){
			currentDay = "0"+day;
		}else{
			currentDay = ""+day;
		}
		Sql sql = new Sql("SELECT ID,SENDNUM FROM MSG_MESSAGE_NUM WHERE USERID=?ID AND SENDTIME=?DAY");
		sql.getParameters().add(new Parameter("ID", sUserId));
		sql.getParameters().add(new Parameter("DAY", year+currentMonth+currentDay));
		DataTable dt = DBManager.getDataTable(sql);
		if(dt!=null&&dt.getRows().size()>0){
			String id = dt.getRow(0).getString("ID");
			long sum =  dt.getRow(0).getLong("SENDNUM");
			sum = sum+i;
			sql = new Sql("UPDATE MSG_MESSAGE_NUM SET SENDNUM=?SENDNUM WHERE ID=?ID ");
			sql.getParameters().add(new Parameter("SENDNUM", sum));
			sql.getParameters().add(new Parameter("ID", id));
			
			collection.add(sql);
		}else{
			sql = new Sql("SELECT A.MAINUNIT FROM G_ORGUSER A WHERE A.USERINFOID=?ID");
			sql.getParameters().add(new Parameter("ID", sUserId));
			String mainunitId = DBManager.getFieldStringValue(sql);
			sql = new Sql("INSERT INTO MSG_MESSAGE_NUM(ID,USERID,SENDTIME,SENDNUM," +
					"MAINUNIT) VALUES (?ID,?USERID,?SENDTIME,?SENDNUM,?MAINUNIT)");
			sql.getParameters().add(new Parameter("ID", ID.get16bID()));
			sql.getParameters().add(new Parameter("USERID", sUserId));
			sql.getParameters().add(new Parameter("SENDTIME", year+currentMonth+currentDay));
			sql.getParameters().add(new Parameter("SENDNUM", i));
			sql.getParameters().add(new Parameter("MAINUNIT", mainunitId));
			collection.add(sql);
		}
		return DBManager.execute(collection);
	}

	
	public static boolean isSendSMS(String userId){
		Sql sql = new Sql("SELECT B.MSG_NUM AS NUM " +
				"FROM MSG_MESSAGE_SETTING_USER A " +
				"LEFT JOIN MSG_MESSAGE_SETTING B " +
				"ON A.SETTING_ID=B.ID WHERE A.USERID=?USERID");
		sql.getParameters().add(new Parameter("USERID", userId));
		long size = DBManager.getFieldLongValue(sql);
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		String currentMonth = "";
		if(month<10){
			currentMonth = "0"+month;
		}else{
			currentMonth = ""+month;
		}
		
		sql = new Sql("SELECT sum(SENDNUM) FROM MSG_MESSAGE_NUM " +
				"WHERE SENDTIME>=?ADAY and SENDTIME<=?BDAY AND USERID=?ID");
		sql.getParameters().add(new Parameter("ADAY", year+currentMonth+"00"));
		sql.getParameters().add(new Parameter("BDAY", year+currentMonth+"31"));
		sql.getParameters().add(new Parameter("ID", userId));
		long useNum = DBManager.getFieldLongValue(sql);
		if(size>useNum){
			return true;
		}
		return false;
	}
	
	public static Map<String,String> getUserMap(String ids){
		
		Map<String,String> map = new HashMap<String,String>();
		Sql sql = new Sql("SELECT ID,NAME,MOBILE FROM G_USERINFO where id in ("+ids+")");
		DataTable dt = DBManager.getDataTable(sql);
		if(dt==null||dt.getRows().size()<=0){
			return null;
		}else{
			for(DataRow dr:dt.getRows()){
				String mobile = dr.getString("MOBILE");
				if(StringHelper.isNullOrEmpty(mobile)){
					map.put(dr.getString("ID"),dr.getString("NAME")+";null");
				}else{
					map.put(dr.getString("ID"),dr.getString("NAME")+";"+dr.getString("MOBILE"));
				}
				
			}
			return map;
		}
	}
}

