
(function () {
	if (typeof (com) == "undefined") {
		com = {};
	}
	if (typeof (com.syc) == "undefined") {
		com.syc = {};
	}
	if (typeof (com.syc.oa) == "undefined") {
		com.syc.oa = {};
	}
	if (typeof (com.syc.oa.formStatus) == "undefined") {
		com.syc.oa.formStatus = {nodeInfoDiv:null, mode:null, loadFlow:function (divId) {
			var url = m_strFlow_Machine + "/ds_Flow/ShowFlow2.action?id=" + this.getQuery("Info_ID") + "&wf_id=0&formStatus=1";
			var data = null;
			var result = $.ajax({url:url, type:"POST", data:data, dataType:null, timeout:2000, async:false}).responseText;
			var picture = document.getElementById(divId);
			picture.innerHTML = result;
		}, loadFlow1:function (obj) {
			var url = m_strFlow_Machine + "/ds_Flow/formStatus1.jsp?id=" + this.getQuery("Info_ID") + "&wf_id=0";
			var data = null;
			var result = $.ajax({url:url, type:"POST", data:data, dataType:null, timeout:2000, async:false}).responseText;
			obj.innerHTML = result;
		}, loadFlow2:function (obj) {
			var url = m_strFlow_Machine + "/ds_Flow/FormStatus.action?id=" + this.getQuery("Info_ID") + "&wf_id=0";
			var data = null;
			var result = $.ajax({url:url, type:"POST", data:data, dataType:null, timeout:2000, async:false}).responseText;
			obj.innerHTML = result;
		}, showFlow:function (obj) {
			var temp = obj.parentNode.parentNode.lastChild;
			while (temp.tagName != "DIV") {
				temp = temp.previousSibling;
			}
			var current = temp.style.display;
			obj.innerHTML = current == "block" ? "\u67e5\u770b\u8be6\u60c5" : "\u9690\u85cf";
			obj.className = current == "block" ? "form_status_view" : "form_status_view1";
			temp.style.display = current == "block" ? "none" : "block";
			if (temp.hasLoaded == null) {
				this.mode = 1;
				var url = m_strFlow_Machine + "/ds_Flow/FormStatus.action?id=" + this.getQuery("Info_ID") + "&wf_id=0";
				var data = null;
				var result = $.ajax({url:url, type:"POST", data:data, dataType:null, timeout:2000, async:false}).responseText;
				temp.innerHTML = result;
				temp.hasLoaded = {};
			}
			if (this.nodeInfoDiv != null) {
				this.nodeInfoDiv.style.display = "none";
				this.nodeInfoDiv.obj.className = "form_status_node_info";
			}
		}, showInfo:function (sender, infos) {
			var ob = $(sender).offset();
			var left = ob.left;
			var top = ob.top;
			var w = sender.clientWidth;
			var sContent = Math.floor(left + (w / 2));
			var str = "";
			if (!$("#div_show_alt")[0]) {
				var div = com.syc.oa.formStatus.getDiv();
				$(document.body).append($(div));
			}
			var objDiv = $("#div_show_alt")[0];
			var h = 0;
			objDiv.style.display = "";
			h = objDiv.clientHeight;
			top = top - h;
			w = $(".form_status_user_info4")[0].offsetLeft;
			var llf = $(".form_status_user_info4").width();
			var width_ = $(".form_status_user_info4").offset().left - $("#div_show_alt").offset().left + llf;
			left = sContent - width_;
			$(".form_status_user_info_div2").html(infos);
			objDiv.style.left = left + "px";
			objDiv.style.top = top + "px";
		}, showInfo2:function (sender, infos) {
			alert("\u8bf7\u4f7f\u7528showInfo");
		}, infoHide:function () {
			if ($("#div_show_alt")[0]) {
				$("#div_show_alt").hide();
			}
		}, getDiv:function () {
			var str = "<div id='div_show_alt' style=\"position:absolute;display:none;z-index:1000;\" >" + "<table  border=\"0\" cellSpacing=\"0\" cellPadding=\"0\" >" + "<TBODY>" + "<TR>" + "<TD class=\"form_status_user_info1\"></TD>" + "<TD class=\"form_status_user_info2\" vAlign=top >" + "<DIV class=\"form_status_user_info_div1\"><img src='../css/blue/images/alert.gif'/></DIV></TD>" + "<TD class=\"form_status_user_info4\"></TD>" + "<TD class=\"form_status_user_info2\" vAlign=top>" + "<DIV class=\"form_status_user_info_div2\"></DIV></TD>" + "<TD class=\"form_status_user_info3\" onclick=javascript:com.syc.oa.formStatus.infoHide(this);></TD></TR></TBODY>" + "</table>" + "</div>";
			return str;
		}, showUserInfo:function (obj, mode) {
			if (mode != null) {
				return;
			}
			var temp = obj.parentNode.nextSibling;
			while (temp.tagName != "DIV") {
				temp = temp.nextSibling;
			}
			temp.style.display = "block";
			var a = 0;
			for (var i = 0; i < 2; i++) {
				a += $(temp.firstChild.rows[0].cells[i]).width();
			}
			a += $(temp.firstChild.rows[0].cells[2]).width() / 2;
			temp.style.top = $(obj).offset().top - 50;
			temp.style.left = $(obj).offset().left - a + $(obj).width() / 2;
			temp.style.display = "block";
		}, hideUserInfo:function (obj, mode) {
			if (mode != null) {
				return;
			}
			var temp = obj.parentNode.nextSibling;
			temp.style.display = "none";
		}, getQuery:function (name) {
			var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
			var r = window.location.search.substr(1).match(reg);
			if (r != null) {
				return unescape(r[2]);
			}
			return null;
		}, error:function (code) {
			if (code == 0) {
				alert("\u8bf7\u767b\u5f55\uff01");
			}
		}, createYJDIV:function (obj, id) {
			var tab = obj;
			while (tab.tagName != "TABLE") {
				tab = tab.parentNode;
			}
			tab = tab.parentNode;
			while (tab.tagName != "TABLE") {
				tab = tab.parentNode;
			}
			tab = tab.parentNode;
			while (tab.tagName != "TABLE") {
				tab = tab.parentNode;
			}
			var div_yj = document.getElementById("nodesInfo_" + id);
			if (this.nodeInfoDiv == null) {
				this.nodeInfoDiv = div_yj;
				this.nodeInfoDiv.obj = obj;
			} else {
				if (div_yj != this.nodeInfoDiv) {
					this.nodeInfoDiv.style.display = "none";
					this.nodeInfoDiv.obj.className = "form_status_node_info";
					this.nodeInfoDiv = div_yj;
					this.nodeInfoDiv.obj = obj;
				}
			}
			var tab1 = $(tab);
			var td = tab.parentNode;
			var tr = td.parentNode;
			var alignFlag = td.cellIndex > Math.ceil(tr.childNodes.length / 2) ? true : false;
			var x = 0;
			if (alignFlag) {
				x = tab1.offset().left + tab1.width() - $(this.nodeInfoDiv).width() + 2;
			} else {
				x = tab1.offset().left;
			}
			var y = tab1.offset().top + tab.clientHeight + 5;
			var s = this.nodeInfoDiv.style.display;
			this.nodeInfoDiv.style.top = y;
			this.nodeInfoDiv.style.left = x;
			this.nodeInfoDiv.style.display = (s == "block" ? "none" : "block");
			obj.className = "form_status_node_info" + (s == "block" ? "" : "1");
			this.nodeInfoDiv.style.zIndex = 1000;
		 	//alert(this.nodeInfoDiv.firstChild.outerHTML);
		}, popup:function (msg, bak) {
			var posb = msg.indexOf("[BR]");
			while (posb != -1) {
				msg = msg.substring(0, posb) + "<br>" + msg.substring(posb + 4, msg.length);
				posb = msg.indexOf("[BR]");
			}
			var posb = msg.indexOf("[br]");
			while (posb != -1) {
				msg = msg.substring(0, posb) + "<br>" + msg.substring(posb + 4, msg.length);
				posb = msg.indexOf("[br]");
			}
			var content = "<div class='form_status_node_YJ1' onmouseout='javascript:com.syc.oa.formStatus.kill();'  style='background-color:" + bak + "'>" + msg + "</div>";
			var e = window.event;
			var x = event.x + document.body.scrollLeft;
			var y = event.y + document.body.scrollTop;
			var yj = document.getElementById("form_status_node_YJ");
			yj.innerHTML = content;
			yj.style.left = x + 10;
			yj.style.top = y + 10;
			if (this.mode == null) {
				if (x > 600) {
					yj.style.left = x - 140;
				}
				if (y + $(yj).height() + 10 > $(document.body).height()) {
					if($(yj).text().length>48)
						{
						yj.style.top = y-$(yj).height();
						}
					else
						{
						yj.style.top = y-$(yj).height()/3;
						}
					
				}
			}
			yj.style.display = "block";
			yj.style.zIndex = 1001;
		}, kill:function () {
			document.getElementById("form_status_node_YJ").style.display = "none";
		}};
	}
	window.com.syc.oa.formStatus = com.syc.oa.formStatus;
})();

