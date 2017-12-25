package dsoap.tools.tree;

import java.util.ArrayList;
import java.util.List;

public class Tree {

    public List<TreeNode> Nodes = new ArrayList<TreeNode>();
    private String url = "/ds_flow_wsj11/ds_Flow/SelectUserList.jsp";
    private String target = "";

    /**
     * { attributes: { id : "pjson4_1" }, data: "Root node 1", children : [ { attributes: { id : "pjson4_2" ,type:"ss",}, data: { title : "Custom icon" } }, { attributes: { id : "pjson4_3" }, data: "Child node 2" }, { attributes: { id : "pjson4_4" }, data: "Some other child node" } ]}, { attributes: { id : "pjson4_5" }, data: "Root node 2" }
     */
    public String getJqueryTreeStr() {
        StringBuffer sb = new StringBuffer();
        // ---吴红亮 添加 开始
        if (Nodes.size() > 1) {
            sb.append("{attributes:{nodetype:'Root',datalist:''},data:{title:'',icon :''},state:'open',children:[");
        }
        // ---吴红亮 添加 结束
        for (TreeNode node : Nodes) {
            sb.append("{");
            // 添加的属性
            sb.append("attributes:{nodetype:'" + node.getType() + "',datalist:'" + node.getNodeData() + "'},");
            // 基础属性
            sb.append("data:{title:'" + node.getText() + "',icon :'" + node.getImageUrl() + "'}");
            if (node.isExpanded()) {
                sb.append(",state:'open'");
            }
            // 子节点属性
            if (!node.Nodes.isEmpty()) {
                sb.append(",children:[");
                getSubJsTreeStr(sb, node);
                sb.append("]");
            }
            sb.append("}");
            // ---吴红亮 添加 开始
            if (Nodes.size() > 1) {
                sb.append(",");
            }
            // ---吴红亮 添加 结束
        }
        // ---吴红亮 添加 开始
        if (Nodes.size() > 1) {
            sb.append("]}");
        }
        // ---吴红亮 添加 结束
        // System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 
     * @param sb
     * @param node
     */
    public void getSubJsTreeStr(StringBuffer sb, TreeNode node) {
        for (TreeNode subnode : node.Nodes) {
            sb.append("{attributes:{nodetype:'" + subnode.getType() + "',datalist:'" + subnode.getNodeData() + "'},data:{title:'" + subnode.getText() + "',icon:'" + subnode.getImageUrl() + "'}");
            // sb.append("{ attributes:{nodetype:'"+subnode.getType()+"',datalist:'"+subnode.getNodeData()+"'},data:{title:'"+subnode.getText()+"'}");
            // ---吴红亮 添加 开始
            if (subnode.isExpanded()) {
                sb.append(",state:'open'");
            }
            // ---吴红亮 添加 结束
            if (!subnode.getType().equals("User")) {
                sb.append(",children:[");
                getSubJsTreeStr(sb, subnode);
                sb.append("]");
            }
            sb.append("},");
        }
    }

    public String getDtreeStr(String name) {
        StringBuffer sb = new StringBuffer();
        for (TreeNode node : Nodes) {
            sb.append("d.add(" + node.getId() + "," + node.getFid() + ",'" + node.getText() + "','" + url + "','" + node.getText() + "','" + target + "','" + node.getImageUrl() + "','" + node.getExpandedImageUrl() + "','false');");
            getSubDtreeStr(sb, node);
        }
        // System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * Name Type Description id Number Unique identity number. pid Number Number refering to the parent node. The value for the root node has to be -1. name String Text label for the node. url String Url for the node. title String Title for the node. target String Target for the node. icon String Image file to use as the icon. Uses default if not specified. iconOpen String Image file to use as the open icon. Uses default if not specified. open Boolean Is the node open.
     */

    private void getSubDtreeStr(StringBuffer sb, TreeNode node) {
        for (TreeNode subnode : node.Nodes) {
            sb.append("d.add(" + subnode.getId() + "," + subnode.getFid() + ",'" + subnode.getText() + "',\"javascript:wer('" + subnode.getNodeData() + "')\",'" + subnode.getText() + "','" + target + "','" + subnode.getImageUrl() + "','" + subnode.getExpandedImageUrl() + "','false');");
            if (!subnode.getType().equals("User")) {
                getSubDtreeStr(sb, subnode);
            }
        }
    }

    // UL-LI 树----------------------------------------------------------------------------------------------
    public String getTreeHTML() {
        StringBuffer sb = new StringBuffer();
        for (TreeNode node : Nodes) {
            String nodeType = node.getType();
            String nodeData = node.getNodeData();
            sb.append("<ul><li class='leaf' datalist='" + nodeData + "' nodetype='" + nodeType + "'>");
            processNode(sb, node);
            getSubTreeHTML(sb, node);
            sb.append("</li></ul>");
        }
        return sb.toString();
    }

    private void getSubTreeHTML(StringBuffer sb, TreeNode node) {
        int count = 0;
        int size = node.Nodes.size();
        for (TreeNode subnode : node.Nodes) {
            String nodeType = subnode.getType();
            String nodeData = subnode.getNodeData();
            System.out.println(nodeType);
            if (!nodeType.equals("User")) {
                if (count == 0) {// 父子
                    sb.append("<ul><li class='leaf' datalist='" + nodeData + "' nodetype='" + nodeType + "'>");
                } else {// 兄弟
                    sb.append("</li><li class='leaf' datalist='" + nodeData + "' nodetype='" + nodeType + "'>");
                }
                processNode(sb, subnode);// 非人员节点
                getSubTreeHTML(sb, subnode);
                if (count == size - 1) {
                    sb.append("</li></ul>");
                }
            } else {
                if (count == 0) {// 父子
                    sb.append("<ul><li class='leaf' datalist='" + nodeData + "' nodetype='" + nodeType + "' onclick='javascript:checkUser(this);'>");
                } else {// 兄弟
                    sb.append("</li><li class='leaf' datalist='" + nodeData + "' nodetype='" + nodeType + "' onclick='javascript:checkUser(this);'>");
                }
                processNode(sb, subnode);
                if (count == size - 1) {
                    sb.append("</li></ul>");
                }
            }
            count++;
        }
    }

    private void processNode(StringBuffer sb, TreeNode node) {
        String nodeText = node.getText();
        String nodeImg = node.getImageUrl();
        sb.append("<a style='background:url(" + nodeImg + ") no-repeat;' href='#'>" + nodeText + "</a>");
    }

    // DIV 树----------------------------------------------------------------------------------------------
    public String getTreeHTML1() {
        StringBuffer sb = new StringBuffer();
        int N = Nodes.size();
        int i = 0;
        for (TreeNode node : Nodes) {
            String nodeType = node.getType();
            String nodeData = node.getNodeData();
            sb.append("<div class='tree_node' datalist='" + nodeData + "' nodetype='" + nodeType + "'>");
            sb.append("<div class='tree_node_1'>");
            processNode1(sb, node, node.isExpanded());
            sb.append("</div>");
            sb.append("<div class='tree_node_" + (i == N - 1 ? "3" : "2") + "'>");
            getSubTreeHTML1(sb, node);
            sb.append("</div>");
            sb.append("</div>");
            i++;
        }
        return sb.toString();
    }

    private void processNode1(StringBuffer sb, TreeNode node, boolean isExpanded) {
        String nodeText = node.getText();
        String nodeImg = node.getImageUrl();
        String nodeType = node.getType();
        String nodeData = node.getNodeData();
        if (nodeType.equals("User")) {
            sb.append("<span class='tree_node_img_u_1'></span><span class='tree_node_img' style='background:url(" + nodeImg + ") no-repeat center;'/></span><a style='cursor:pointer;' onclick='javascript:checkUser(this);' datalist='" + nodeData + "' nodetype='" + nodeType + "'>" + nodeText + "</a>");
        } else {
            sb.append("<span class='tree_node_img_d_" + (isExpanded ? "1" : "2") + "' onclick='javascript:expanded(this);'></span><span class='tree_node_img' style='background:url(" + nodeImg + ") no-repeat center;'/></span><a style='cursor:pointer;' onclick='javascript:checkDept(this);' datalist='" + nodeData + "' nodetype='" + nodeType + "'>" + nodeText + "</a>");
        }
    }

    private void getSubTreeHTML1(StringBuffer sb, TreeNode node) {
        int N = node.Nodes.size();
        int i = 0;
        for (TreeNode subnode : node.Nodes) {
            String nodeType = subnode.getType();
            if (nodeType.equals("User")) {
                sb.append("<div class='tree_node'>");
                sb.append("<div class='tree_node_" + (i == N - 1 ? "4" : "2") + "'>");
                processNode1(sb, subnode, node.isExpanded());
                sb.append("</div>");
                sb.append("</div>");
            } else {
                sb.append("<div class='tree_node'>");
                sb.append("<div class='tree_node_1'>");
                processNode1(sb, subnode, node.isExpanded());
                sb.append("</div>");
                if (subnode.Nodes.size() > 0) {
                    sb.append("<div class='tree_node_" + (i == N - 1 ? "3" : "2") + "' style='display:" + (node.isExpanded() ? "block" : "none") + "'>");
                    getSubTreeHTML1(sb, subnode);
                    sb.append("</div>");
                }
                sb.append("</div>");
            }
            i++;
        }
    }

}
