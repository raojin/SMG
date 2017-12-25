package dsoap.web.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.data.DBManager;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.log.SystemLog;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class GetNodeUsersAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;

	@Override
	public String execute() throws Exception {
		super.execute();
		boolean isEnd = false;
		try {
			// 获取发送参数，创建发送对象
			if (request.getParameter("flowParms") != null) {
				String flowParms = URLDecoder.decode(request
						.getParameter("flowParms"), "utf-8");
				//发送参数格式：<Root><Flow><Type>0</Type><Key>1346727307282000</Key><Objclass>651059</Objclass><UserID>737911</UserID><Pid>199592208</Pid><Pnid>2</Pnid><WfID>844</WfID></Flow></Root>
				// 要发送到的节点ID
				String toNodeID = URLDecoder.decode(request
						.getParameter("toNodeID"), "utf-8");
						dsFlow = new DS_FLOWClass(flowParms);
						//System.out.println("提交类型:" + dsFlow.iSendType);
						// 设置要发送到的节点信息
						if (!"".equals(toNodeID) && !"0".equals(toNodeID)) {
							//选中传入的节点，生成该节点下的人员
							dsFlow.setSelectNodeIDIsTS(Long.parseLong(toNodeID));
							//节点下人员的xml
							System.out.println(dsFlow.NextNodeInfoXml.asXML());
							request.setAttribute("usersXML", dsFlow.NextNodeInfoXml.asXML());
						}
						
			} else {
					errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
			}
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
		}
		request.setAttribute("errStr", errStr);
		return "RESULT";
	}
}
