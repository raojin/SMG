package dsoap.tools;

import org.dom4j.Node;

public class Dom4jTools {

    public static String getSingleNodeInnerXml(Node node, String xpath) {
        // System.out.println(node.asXML());
        Node singleNode = null;
        if (xpath.equals("/")) {
            singleNode = node;
        } else {
            singleNode = node.selectSingleNode(xpath);
        }

        if (singleNode.getName() != null && !"".equals(singleNode.getName())) {
            String nodename = singleNode.getName();
            String xml = singleNode.asXML();
            String temp = "<" + nodename;
            if (xml.startsWith(temp)) {
                temp = xml.substring(xml.indexOf(">") + 1);
            }
            if (temp.length() < nodename.length() + 3) {
                return "";
            }
            String result = temp.substring(0, temp.length() - nodename.length() - 3);
            return result;
        }
        return "";
    }
    
}
