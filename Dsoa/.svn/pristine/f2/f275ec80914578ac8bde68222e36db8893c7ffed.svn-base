package message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import message.dao.MessageDaoFactory;
import message.vo.MessageProvider;


public class Messager {
	/**
	 * 邮件提醒
	 */
	public static final String MAIL_PROVIDER = "mail";
	
	/**
	 * 短信提醒
	 */
	public static final String SNS_PROVIDER = "sms";
	
	/**
	 * 简单内部系统提醒，不插入系统接受者信息
	 */
	public static final String SAMPLE_PROVIDER = "sample" ;
	
	/**
	 * 简单内部系统提醒,插入系统接受者信息
	 */
	public static final String SAMPLE_REPLY_PROVIDER = "sampleReply" ;
	
	
	public static final String SAMPLE_REPLY_TUOPAN = "stuoPan" ;
	
	
	public void init(){
		//
	}
	
	/**
	 * 发送信息
	 * @param body 内容
	 * @param from 发送者
	 * @param to  接受者
	 * @param provider 发送方式 （如系统内部）
	 * @return
	 */
	public static boolean send(String body,long from,long to,String provider){
		return send(body,from,to,provider,"");
	}
	
	/**
	 * 发送信息
	 * @param body 内容 
	 * @param from 发送者
	 * @param to   接受者
	 * @param provider 发送方式  （如mail,sample,sns...）
	 * @param target  发送需要信息  （如邮箱、手机号...）
	 * @return
	 */
	public static boolean send(String body,long from,long to,String provider,String target){
		return send(body,from,new MessageProvider(to,provider,target));
	}
	
	/**
	 * 发送信息
	 * @param body 内容 
	 * @param from 发送者
	 * @param messageProvider  信息提供者
	 * @return
	 */
	public static boolean send(String body,long from,MessageProvider messageProvider){
		Message message = new Message();
		
		message.setFrom(from);
		message.setBody(body) ;
		message.addMessageProvider(messageProvider);
		return send(message);
	}
	
	/**
	 * 系统内部发送多人
	 * @param body
	 * @param from
	 * @param tos
	 * @return
	 */
	public static boolean send(String title, String body,long from, Long[] tos){
		List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
		MessageProvider messageProvider = null ;
		for(long to : tos){
			messageProvider = new MessageProvider(to, Messager.SAMPLE_PROVIDER) ;
			messageProviders.add(messageProvider);
		}
		return send(title, body,from,messageProviders);
	}
	
	
	
	
	/**
	 * 发送信息
	 * @param body 内容 
	 * @param from  发送者
	 * @param messageProviders  多种接受方式
	 * @return
	 */
	public static boolean send(String title, String body,long from,List<MessageProvider> messageProviders){
		Message message = new Message();
		message.setTitle(title);
		message.setBody(body) ;
		message.setFrom(from);
		message.setSendTime(new Date());
		message.setMessageProviders(messageProviders);
		return send(message);
	}
	
	
	/**
	 * 系统内部发送多人
	 * @param body
	 * @param from
	 * @param tos
	 * @return
	 */
	public static boolean send(String body,long from,Long[] tos){		
		return send(body,from,tos,Messager.SAMPLE_PROVIDER);
	}
	
	
	public static boolean send(String body,long from,Long[] tos,String provider){
		List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
		MessageProvider messageProvider = null ;
		for(long to : tos){
			messageProvider = new MessageProvider(to, provider) ;
			messageProviders.add(messageProvider);
		}
		return send(body,from,messageProviders);
	}
	
	
	
	/**
	 * 发送信息
	 * @param body 内容 
	 * @param from  发送者
	 * @param messageProviders  多种接受方式
	 * @return
	 */
	public static boolean send(String body,long from,List<MessageProvider> messageProviders){
		Message message = new Message();
		message.setBody(body) ;
		message.setFrom(from);
		message.setMessageProviders(messageProviders);
		return send(message);
	}
	
	/**
	 * 发送者 
	 * @param message  封装信息体 (最全的参数信息)
	 * @return
	 */
	public static boolean send(Message message){
		return MessageDaoFactory.getInstance().saveSendMessage(message) ;
	}
	
	/**
	 * 手动办结
	 * @param messages
	 * @return
	 */
	public static boolean stop(String messages){
		return MessageDaoFactory.getInstance().updateMessagesStatus(2,messages) ;
	}
	
	public void close(){
		
	}
	
	public static void main(String[] args) {
		
//		Message message = new Message();
//		message.setBody("内容xxx") ;
//		message.setTitle("标题xxx") ;
//		message.setFrom(11) ;
//		message.setSendTime(new Date()) ;
//		
//	
//		message.addMessageProvider(12, Messager.MAIL_PROVIDER, "545014805@qq.com") ;
//		message.addMessageProvider(12, Messager.SNS_PROVIDER, "18621287429") ;
//		message.addMessageProvider(12, Messager.SAMPLE_PROVIDER) ;
//		
//		message.addMessageProvider(11, Messager.MAIL_PROVIDER, "545014805@qq.com") ;
//		message.addMessageProvider(11, Messager.SNS_PROVIDER, "18621287429") ;
//		message.addMessageProvider(11, Messager.SAMPLE_PROVIDER) ;
////		
//		message.setFinishTime(new Date()) ;
//		message.setFinishType("2") ;
//		message.setUrgent("2") ;
//		message.setUrl("链接地址");
//		
//		Messager.send(message);
//		
//		Messager.send("body",11,12,Messager.SAMPLE_PROVIDER);
//		
//		
//		Messager.send("body",11,12,Messager.MAIL_PROVIDER,"545014805@qq.com");
		int len = 1000;
		Long[] users = new Long[len] ;
		for(int i=0; i<1000;i++){
			users[i] = (long) (len + i);
		}
		Messager.send("body",11,users,Messager.SAMPLE_REPLY_PROVIDER);
//		
//		
//		
//		Messager.send("body",11,new MessageProvider(12, Messager.MAIL_PROVIDER, "545014805@qq.comqq")) ;
//		
//		
//		List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
//		messageProviders.add(new MessageProvider(12, Messager.MAIL_PROVIDER, "545014805@qq.comqq"));
//		messageProviders.add(new MessageProvider(12, Messager.SAMPLE_PROVIDER));
//		
//		Messager.send("body2",11,messageProviders);
	}
}
