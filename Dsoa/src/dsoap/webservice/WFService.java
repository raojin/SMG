package dsoap.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public interface WFService {

	@WebMethod
	public @WebResult(name="addWorkDaysWFResult")String addWorkDays(@WebParam(name="nowTime")String nowTime, @WebParam(name="iDays")int iDays) throws Exception;

	@WebMethod
	public @WebResult(name="GetWFResult")String GetWF(@WebParam(name="sWFID")String sWFID);

	@WebMethod
	public @WebResult(name="GetSysDateResult")String GetSysDate() throws Exception ;
	
	@WebMethod
	public @WebResult(name="GetRegCodeResult")String GetRegCode();
	
	@WebMethod
	public @WebResult(name="GetCompNameCodeResult")String GetCompNameCode() throws Exception;
	
	@WebMethod
	public @WebResult(name="GetCompNameCodeFromNAMEResult")String GetCompNameCodeFromNAME(@WebParam(name="sName")String sName);
	
	@WebMethod
	public @WebResult(name="SetRegCodeResult")String SetRegCode(@WebParam(name="sCode")String sCode);
	
	@WebMethod
	public @WebResult(name="SaveWFResult")String SaveWF(@WebParam(name="xml")String xml);
	
	@WebMethod
	public @WebResult(name="GetAllUserListResult")String GetAllUserList(@WebParam(name="sObjClass")String sObjClass);
	
	@WebMethod
	public @WebResult(name="GetButtonDicResult")String GetButtonDic();
	
	@WebMethod
	public @WebResult(name="GetSubFLowResult")String GetSubFLow(@WebParam(name="sObjclass")String sObjclass);
	
	@WebMethod
	public @WebResult(name="DeleteWFResult")String DeleteWF(@WebParam(name="WF_ID")String WF_ID);
	
	@WebMethod
	public @WebResult(name="GetTreeUsersResult")String GetTreeUsers();
	
	@WebMethod
	public @WebResult(name="GetRemoteTreeUsersResult")String GetRemoteTreeUsers();
	
//	@WebMethod
//	public String GetFileTranServiceXML();
	
	@WebMethod
	public @WebResult(name="GetWFSetInfoResult")String GetWFSetInfo();
	
	@WebMethod
	public @WebResult(name="GetWfTypeDicResult")String GetWfTypeDic();
	
	@WebMethod
	public @WebResult(name="GetG_PNodesInfoResult")String GetG_PNodesInfo(@WebParam(name="info_id")String info_id, @WebParam(name="wf_id")String wf_id);
	
	@WebMethod
	public @WebResult(name="getDiffTimeResult")String getDiffTime(@WebParam(name="sNowWorkTime")String sNowWorkTime, @WebParam(name="sEndWorkTime")String sEndWorkTime) throws Exception;
	
}
