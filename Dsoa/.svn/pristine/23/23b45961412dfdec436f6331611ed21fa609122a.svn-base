package message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xsf.ID;
import message.vo.MessageProvider;

public class Message {
	private long id;
	private String title;
	private String body;
	private long from;
	
	private Date createTime;
	private Date sendTime;
	
	/**
	 * 前台传入唯一标识
	 */
	private String info ;
	
	/**
	 * 0一般；1紧急；2特急；3特提
	 */
	private String urgent ; 
	
	/**
	 * 办结机制0:时限 1:点击 2:手动
	 */
	private String finishType ;
	
	/**
	 * 办结时限
	 */
	private Date finishTime;
	
	/**
	 * 目标链接
	 */
	private String url ;
	
	
	/**
	 * 应用系统
	 */
	private String appSystem ;
	
	private String info_id ;
	
	
	
	
	
	public String getInfo_id() {
		return info_id;
	}

	public void setInfo_id(String info_id) {
		this.info_id = info_id;
	}

	public String getAppSystem() {
		return appSystem;
	}

	public void setAppSystem(String appSystem) {
		this.appSystem = appSystem;
	}

	


	private List<MessageProvider> messageProviders ;
	
	public Message(){
		this.id = ID.get16bID() ;
		messageProviders = new ArrayList<MessageProvider>();
	}
	
	public List<MessageProvider> getMessageProviders() {
		return messageProviders;
	}
	public void setMessageProviders(List<MessageProvider> messageProviders) {
		this.messageProviders.addAll(messageProviders) ;
	}
	
	public void addMessageProvider(MessageProvider provider){
		messageProviders.add(provider);
	}
	
	public void addMessageProvider(long user,String provider,String target){
		addMessageProvider(new MessageProvider(user, provider, target)) ;
	}
	
	public void addMessageProvider(long user,String provider){
		addMessageProvider(user, provider, "") ;
	}
	
	public MessageProvider getMessageProvider(){
		if(messageProviders.size() > 0){
			return messageProviders.get(0) ;
		}
		return null;
	}
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
		
	public String getUrgent() {
		return urgent;
	}
	public void setUrgent(String urgent) {
		this.urgent = urgent;
	}
	public String getFinishType() {
		return finishType;
	}
	public void setFinishType(String finishType) {
		this.finishType = finishType;
	}
	public Date getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setId(long id){
		this.id = id ;
	}
	public long getId() {
		return this.id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}

	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder() ;
		sb.append(" from -> ").append(from) 
		  .append(" \nbody -> ").append(body)
		  .append(" \n\tmessageProviders -> ").append(messageProviders.toString()) ;
		  
		return sb.toString() ;
	}
	
}
