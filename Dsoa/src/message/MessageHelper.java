package message;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import message.vo.MessageProvider;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;




public class MessageHelper {
	public static List<Message> getMessages(long user,String content)
			throws DocumentException {
		List<Message> messages = new ArrayList<Message>();

		Document doc = DocumentHelper.parseText(content);
		Element rootElement = doc.getRootElement();
		List elements = rootElement.elements();
		int size = elements.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				Element element = (Element) elements.get(i);

				//基本信息............................
				String title = element.attributeValue("title"); // 标题
				String sendTime = element.attributeValue("sendTime"); // 发送时间
				String urgent = element.attributeValue("urgent"); // 紧急程度
				String finishType = element.attributeValue("finishType"); // 办结类型
				String finishTime = element.attributeValue("finishTime"); // 办结时间
				String url = element.attributeValue("url"); // 标题
				String info = element.attributeValue("infoId"); // 基本信息
				
				String body = element.element("body").getTextTrim() ; //内容
				
				//发送信息............................
				Message message = new Message();
				
				message.setInfo(info) ;
				message.setFrom(user) ;
				message.setTitle(title);
				message.setBody(body);
				
			
				if(!StringUtils.isEmpty(sendTime)) {
					message.setSendTime(parseDate(sendTime));
				}
				
				message.setUrgent(urgent);
				message.setFinishType(finishType);
				
				if(!StringUtils.isEmpty(finishTime)) {
					message.setFinishTime(parseDate(finishTime));
				}
				message.setUrl(url) ;
				
				//发送点
				message.setMessageProviders(getMessageProviderByXml(element.element("senders"))) ;
				
				
				messages.add(message);
			}
		}
		return messages;
	}
	
	private static List<MessageProvider> getMessageProviderByXml(Element senders){
		
		List<MessageProvider> messageProviders = null ;
		List senderElements = senders.elements();
		if (senderElements != null && senderElements.size() > 0) {
			int size = senderElements.size() ;
			messageProviders = new ArrayList<MessageProvider>();
			MessageProvider messageProvider = null ;
			for (int i = 0; i < size; i++) {
				Element sendElement = (Element) senderElements.get(i); //获取sender信息
				
				long user = Long.parseLong(sendElement.attributeValue("id")) ;  //发送人id信息
				
				List providerElements =  sendElement.elements() ;
				for(int j=0; j<providerElements.size(); j++){
					Element providerElement = (Element) providerElements.get(j) ;
					
					messageProvider = new MessageProvider(user,providerElement.getName(),providerElement.getTextTrim()) ;
					messageProviders.add(messageProvider);
				}
			}
		}
		return messageProviders ;
	}

	public static Date parseDate(String dateString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static boolean compareTo(Date date){
		if(date == null){
			return true ;
		}
		
		if(new Date().compareTo(date) >= 0){
			return true ;
		}
		return false ;
	}
	
 

}
