package message;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;


import xsf.ID;
import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataTable;
import xsf.data.Parameter;
import xsf.data.Sql;
import xsf.log.LogManager;

public class SMSInterface {
	
	public static String smsurl = "http://10.15.208.70:81/";
	public static String smsuserid = "c0lqnr";
	public static String smsauthkey = "12DE715B0B4A7BCBD0490FDEB37056A0";
	
	public static String sendSMS1(String userId,String content,
			String sendTime,String mobile){
		/*String result = "false";
		String account = "";
		String pwd = "";
		DataRow dr = getSMSAccount(userId);
		if(dr!=null){
			account = dr.getString("SMSACCOUNT");
			pwd = dr.getString("SMSPWD");
			pwd = xsf.crypto.BASE64.decode(pwd);
		}else{
			return "NoPWD";
		}
		
		if(!isMobile(mobile)){
			return result;
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("Account", account);
		params.put("PWD", pwd);
		params.put("Msg1", content);
		params.put("Mobile1", mobile);
		result = sendPostMessage(params, "GB2312");
		System.out.println("-result->>" + result);
		return result;*/
		
		StringBuilder urlstr = new StringBuilder(smsurl)
		.append("sms.shtml?optype=sms_send&userid=c0lqnr&authkey=12DE715B0B4A7BCBD0490FDEB37056A0&content=")
		.append(URLEncoder.encode(toUtf8String(content, "UTF-8")))
		.append("&type=1&tel=").append(mobile).append("&time=&uuid=").append(ID.get6bID());
		System.out.println(urlstr);
		BufferedReader reader = null;
		//HTTP请求连接
		try {
			URL url=new URL(urlstr.toString().trim());  
			StringBuffer document = new StringBuffer();  
			URLConnection conn = url.openConnection();  
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));  
			String line = null;  
			
			while ((line = reader.readLine()) != null){  
			     document.append(line);  
			}
			System.out.println("send result:"+document);     
		
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try{
					reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return "OK";
	}
	
	/*
	 * 转码codestr
	 */
	public static  String toUtf8String(String s,String codestr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = Character.toString(c).getBytes(codestr);
				} catch (Exception ex) {
					LogManager.error(ex);
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}

	
	/*
	 * 通过userId得到sms用户名和密码
	 */
	public static DataRow getSMSAccount(String userId){
		Sql sql = new Sql("SELECT SMSACCOUNT,SMSPWD FROM MSG_MESSAGE_USER_ACCONT WHERE USERID=?ID");
		sql.getParameters().add(new Parameter("ID", userId));
		DataTable dt = DBManager.getDataTable(sql);
		if(dt!=null&&dt.getRows().size()>0){
			return dt.getRow(0);
		}else{
			return null;
		}
	}
	
	/*
	 * 判断是手机号码是否合法
	 */
	public static boolean isMobile(String mobiles){  
		  
		String regExp = "^0?(13[0-9]|15[012356789]|18[0236789]|14[57])[0-9]{8}$";  
		  
		Pattern p = Pattern.compile(regExp);  
		  
		Matcher m = p.matcher(mobiles);  
		  
		return m.find(); 
		  
	} 
	
	
	/*
	   * params 填写的URL的参数 encode 字节编码
	   */
	  public static String sendPostMessage(Map<String, String> params,
	      String encode) {
		
	    StringBuffer stringBuffer = new StringBuffer();
	    String result ="";
	    if (params != null && !params.isEmpty()) {
	      for (Map.Entry<String, String> entry : params.entrySet()) {
	        try {
	          stringBuffer
	              .append(entry.getKey())
	              .append("=")
	              .append(URLEncoder.encode(entry.getValue(), encode))
	              .append("&");

	        } catch (UnsupportedEncodingException e) {
	   
	          e.printStackTrace();
	          return "false";
	        }
	      }
	      // 删掉最后一个 & 字符
	      stringBuffer.deleteCharAt(stringBuffer.length() - 1);
	      System.out.println("-->>" + stringBuffer.toString());
	      URL url;
	      HttpURLConnection httpURLConnection = null;
	      try {
	    	 
				url = new URL("http://210.76.64.248/oasms/SMInterface.asp");
	        httpURLConnection = (HttpURLConnection) url
	            .openConnection();
	        httpURLConnection.setConnectTimeout(3000);
	        httpURLConnection.setDoInput(true);// 从服务器获取数据
	        httpURLConnection.setDoOutput(true);// 向服务器写入数据

	        // 获得上传信息的字节大小及长度
	        byte[] mydata = stringBuffer.toString().getBytes();
	        // 设置请求体的类型
	        httpURLConnection.setRequestProperty("Content-Type",
	            "application/x-www-form-urlencoded");
	        httpURLConnection.setRequestProperty("Content-Lenth",
	            String.valueOf(mydata.length));

	        // 获得输出流，向服务器输出数据
	        OutputStream outputStream = (OutputStream) httpURLConnection
	            .getOutputStream();
	        outputStream.write(mydata);

	        // 获得服务器响应的结果和状态码
	        int responseCode = httpURLConnection.getResponseCode();
	        if (responseCode == 200) {

	          // 获得输入流，从服务器端获得数据
	          InputStream inputStream = (InputStream) httpURLConnection
	              .getInputStream();
	          result = changeInputStream(inputStream, encode);

	        }

	      } catch (IOException e) {
	        e.printStackTrace();
	        result = "false";
	      }finally{
	    	  if(httpURLConnection!=null){
	    		  httpURLConnection.disconnect();
	    	  }
	      }
	    }

	    return result;
	  }
	
	
	/*
	   * // 把从输入流InputStream按指定编码格式encode变成字符串String
	   */
	  public static String changeInputStream(InputStream inputStream,
	      String encode) {

	    // ByteArrayOutputStream 一般叫做内存流
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    byte[] data = new byte[1024];
	    int len = 0;
	    String result = "";
	    if (inputStream != null) {

	      try {
	        while ((len = inputStream.read(data)) != -1) {
	          byteArrayOutputStream.write(data, 0, len);

	        }
	        result = new String(byteArrayOutputStream.toByteArray(), encode);

	      } catch (IOException e) {
	        e.printStackTrace();
	      }

	    }

	    return result;
	  }
}
