package message.vo;

/**
 * 信息提供者
 */
public class MessageProvider {
	/**
	 * 用户ID
	 */
	private long user;
	
	/**
	 * 操作方式
	 */
	private String provider ;
	
	/**
	 * 目标
	 */
	private String target ;
	
	/**
	 * 关联ID 2014.1.7 taolb 
	 */
	private String relation ;

	
	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public MessageProvider(){
		
	}
	
	public MessageProvider(long user, String provider){
		this(user,provider,"") ;
	}
	
	public MessageProvider(long user, String provider, String target) {
		this.user = user;
		this.provider = provider;
		this.target = target;
	}

	
	public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public String toString(){
		return "user -> " + user + " , provider -> " + provider + " , target -> " + target ; 
	}
}
