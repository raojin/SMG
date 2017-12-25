<%@ page language="java" pageEncoding="UTF-8"%>
<%
	String strMj = request.getAttribute("strMj").toString();
	String sms = (String) request.getAttribute("sms");
	
	boolean isForword = (Boolean) request.getAttribute("isForword");
	boolean isEMail = (Boolean) request.getAttribute("isEMail");
	boolean isTray = (Boolean) request.getAttribute("isTray");
	boolean msg_lock=(Boolean) request.getAttribute("msg_lock");
	boolean chIsHideYJ = (Boolean) request.getAttribute("chIsHideYJ");
	boolean showSearch = dsoap.tools.ConfigurationSettings.showSearch;
	boolean showClose = dsoap.tools.ConfigurationSettings.showClose;
	boolean cascade = dsoap.tools.ConfigurationSettings.cascade;
	String  sendBatch = request.getAttribute("sendBatch") == null ? "" : request.getAttribute("sendBatch").toString();
	String  isCS = request.getAttribute("isCS") == null ? "" : request.getAttribute("isCS").toString();	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>人员列表</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
		<link type="text/css" rel="stylesheet"
			href="../js/jquery/tree_component.css" />
		<script type="text/javascript" src="../js/jquery/_lib.js"></script>
		<script type="text/javascript" src="../js/jquery/tree_component.js"></script>
		<script language="javascript">
//	written	by Tan Ling	Wee	on 2 Dec 2001
//	last updated 20 June 2003
//	email :	fuushikaden@yahoo.com
		var	fixedX = -1;			// x position (-1 if to appear below control)
		var	fixedY = -1;			// y position (-1 if to appear below control)
		var startAt = 1;			// 0 - sunday ; 1 - monday
		var showWeekNumber = 0;	// 0 - don't show; 1 - show
		var showToday = 1;		// 0 - don't show; 1 - show
		var imgDir = "../images/";		// directory for images ... e.g. var imgDir="/img/"
		var gotoString = "切换到本月";
		var todayString = "今天：";
		var weekString = "周";
		var scrollLeftMessage = "切换到上月，按住鼠标左键不放将会一直切换";
		var scrollRightMessage = "切换到下月，按住鼠标左键不放将会一直切换";
		var selectMonthMessage = "选择月份";
		var selectYearMessage = "选择年度";
		var selectDateMessage = "选择 [date]"; // do not replace [date], it will be replaced by date.
		var	crossobj, crossMonthObj, crossYearObj, monthSelected, yearSelected, dateSelected, omonthSelected, oyearSelected, odateSelected, monthConstructed, yearConstructed, intervalID1, intervalID2, timeoutID1, timeoutID2, ctlToPlaceValue, ctlNow, dateFormat, nStartingYear
		var	bPageLoaded=false;
		var	ie=document.all;
		var	dom=document.getElementById;
		var	ns4=document.layers;
		var	today =	new	Date();
		var	dateNow	 = today.getDate();
		var	monthNow = today.getMonth();
		var	yearNow	 = today.getYear();
		var	imgsrc = new Array("drop1.gif","drop2.gif","left1.gif","left2.gif","right1.gif","right2.gif");
		var	img	= new Array();
		var bShow = false;
	    /* hides <select> and <applet> objects (for IE only) */
		function hideElement( elmID, overDiv ){
			if(ie){
				for(i = 0; i < document.all.tags( elmID ).length; i++){
					obj = document.all.tags( elmID )[i];
					if(!obj || !obj.offsetParent){
						continue;
					}
					// Find the element's offsetTop and offsetLeft relative to the BODY tag.
					objLeft = obj.offsetLeft;
					objTop = obj.offsetTop;
					objParent = obj.offsetParent;
					while(objParent.tagName.toUpperCase() != "BODY"){
						objLeft += objParent.offsetLeft;
						objTop += objParent.offsetTop;
						objParent = objParent.offsetParent;
					}
					objHeight = obj.offsetHeight;
					objWidth = obj.offsetWidth;
					if(( overDiv.offsetLeft + overDiv.offsetWidth ) <= objLeft );
					else if(( overDiv.offsetTop + overDiv.offsetHeight ) <= objTop );
					else if( overDiv.offsetTop >= ( objTop + objHeight ));
					else if( overDiv.offsetLeft >= ( objLeft + objWidth ));
					else{
						obj.style.visibility = "hidden";
					}
				}
			}
		}
	     
	    /*
	    * unhides <select> and <applet> objects (for IE only)
	    */
		function showElement( elmID ){
	    	
			if(ie){
				for(i = 0; i < document.all.tags(elmID).length; i++){
					obj = document.all.tags(elmID)[i];
					if(!obj || !obj.offsetParent){
						continue;
					}
					obj.style.visibility = "";
				}
			}
		}
	
		function HolidayRec (d, m, y, desc){
			this.d = d;
			this.m = m;
			this.y = y;
			this.desc = desc;
		}
	
		var HolidaysCounter = 0;
		var Holidays = new Array();
	
		function addHoliday (d, m, y, desc){
			Holidays[HolidaysCounter++] = new HolidayRec(d, m, y, desc);
		}
	
		if (dom){
			for	(i=0;i<imgsrc.length;i++){
				img[i] = new Image;
				img[i].src = imgDir + imgsrc[i];
			}
			document.write ("<div onclick='bShow=true' id='calendar'	style='z-index:+999;position:absolute;visibility:hidden;'><table	width="+((showWeekNumber==1)?250:220)+" style='font-family:arial;font-size:11px;border-width:1;border-style:solid;border-color:#a0a0a0;font-family:arial; font-size:11px}' bgcolor='#ffffff'><tr bgcolor='#0000aa'><td><table width='"+((showWeekNumber==1)?248:218)+"'><tr><td style='padding:2px;font-family:arial; font-size:11px;'><font color='#ffffff'><B><span id='caption'></span></B></font></td><td align=right><a href='javascript:hideCalendar()'><IMG SRC='"+imgDir+"close.gif' WIDTH='15' HEIGHT='13' BORDER='0' ALT='Close the Calendar'></a></td></tr></table></td></tr><tr><td style='padding:5px' bgcolor=#ffffff><span id='content'></span></td></tr>")
			if (showToday==1){
				document.write ("<tr bgcolor=#f0f0f0><td style='padding:5px' align=center><span id='lblToday'></span></td></tr>")
			}	
			document.write ("</table></div><div id='selectMonth' style='z-index:+999;position:absolute;visibility:hidden;'></div><div id='selectYear' style='z-index:+999;position:absolute;visibility:hidden;'></div>");
		}
	
		var	monthName =	new	Array("一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月")
		var	monthName2 = new Array("1","2","3","4","5","6","7","8","9","10","11","12")
		if (startAt==0){
			dayName = new Array	("日","一","二","三","四","五","六")
		}else{
			dayName = new Array	("一","二","三","四","五","六","日")
		}
		var	styleAnchor="text-decoration:none;color:black;"
		var	styleLightBorder="border-style:solid;border-width:1px;border-color:#a0a0a0;"
		function swapImage(srcImg, destImg){
			if(ie){
				document.getElementById(srcImg).setAttribute("src",imgDir + destImg);
			}
		}
		function init()	{
			if (!ns4){
				if (!ie) { 
					yearNow += 1900;
				}
				crossobj=(dom)?document.getElementById("calendar").style : ie? document.all.calendar : document.calendar;
				hideCalendar();
				crossMonthObj=(dom)?document.getElementById("selectMonth").style : ie? document.all.selectMonth	: document.selectMonth;
				crossYearObj=(dom)?document.getElementById("selectYear").style : ie? document.all.selectYear : document.selectYear;
				monthConstructed=false;
				yearConstructed=false;
				if (showToday==1){
					document.getElementById("lblToday").innerHTML =	todayString + " <a onmousemove='window.status=\""+gotoString+"\"' onmouseout='window.status=\"\"' title='"+gotoString+"' style='"+styleAnchor+"' href='javascript:monthSelected=monthNow;yearSelected=yearNow;constructCalendar();'>星期"+dayName[(today.getDay()-startAt==-1)?6:(today.getDay()-startAt)]+"  " + yearNow +  "年" + monthName2[monthNow].substring(0,3) + "月" + dateNow + "日</a>"
				}
				sHTML1="<span id='spanLeft'	style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer' onmouseover='swapImage(\"changeLeft\",\"left2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+scrollLeftMessage+"\"' onclick='javascript:decMonth()' onmouseout='clearInterval(intervalID1);swapImage(\"changeLeft\",\"left1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onmousedown='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"StartDecMonth()\",500)'	onmouseup='clearTimeout(timeoutID1);clearInterval(intervalID1)'>&nbsp<IMG id='changeLeft' SRC='"+imgDir+"left1.gif' width=10 height=11 BORDER=0>&nbsp</span>&nbsp;"
				sHTML1+="<span id='spanRight' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer'	onmouseover='swapImage(\"changeRight\",\"right2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+scrollRightMessage+"\"' onmouseout='clearInterval(intervalID1);swapImage(\"changeRight\",\"right1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onclick='incMonth()' onmousedown='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"StartIncMonth()\",500)'	onmouseup='clearTimeout(timeoutID1);clearInterval(intervalID1)'>&nbsp<IMG id='changeRight' SRC='"+imgDir+"right1.gif'	width=10 height=11 BORDER=0>&nbsp</span>&nbsp"
				sHTML1+="<span id='spanMonth' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer'	onmouseover='swapImage(\"changeMonth\",\"drop2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+selectMonthMessage+"\"' onmouseout='swapImage(\"changeMonth\",\"drop1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"' onclick='popUpMonth()'></span>&nbsp;"
				sHTML1+="<span id='spanYear' style='border-style:solid;border-width:1;border-color:#3366FF;cursor:pointer' onmouseover='swapImage(\"changeYear\",\"drop2.gif\");this.style.borderColor=\"#88AAFF\";window.status=\""+selectYearMessage+"\"'	onmouseout='swapImage(\"changeYear\",\"drop1.gif\");this.style.borderColor=\"#3366FF\";window.status=\"\"'	onclick='popUpYear()'></span>&nbsp;"
				document.getElementById("caption").innerHTML  =	sHTML1;
				bPageLoaded=true;
			}
		}
	
		function hideCalendar()	{
			$(crossobj).attr("visibility", "hidden");
			if (crossMonthObj != null){
				$(crossMonthObj).attr("visibility", "hidden");
			}
			if (crossYearObj !=	null){
				$(crossYearObj).attr("visibility", "hidden");
			}
		    showElement( 'SELECT' );
			showElement( 'APPLET' );
		}
	
		function padZero(num) {
			return (num	< 10)? '0' + num : num ;
		}
	
		function constructDate(d,m,y){
			sTmp = dateFormat;
			sTmp = sTmp.replace	("dd","<e>");
			sTmp = sTmp.replace	("d","<d>");
			sTmp = sTmp.replace	("<e>",padZero(d));
			sTmp = sTmp.replace	("<d>",d);
			sTmp = sTmp.replace	("mmmm","<p>");
			sTmp = sTmp.replace	("mmm","<o>");
			sTmp = sTmp.replace	("mm","<n>");
			sTmp = sTmp.replace	("m","<m>");
			sTmp = sTmp.replace	("<m>",m+1);
			sTmp = sTmp.replace	("<n>",padZero(m+1));
			sTmp = sTmp.replace	("<o>",monthName[m]);
			sTmp = sTmp.replace	("<p>",monthName2[m]);
			sTmp = sTmp.replace	("yyyy",y);
			return sTmp.replace ("yy",padZero(y%100));
		}
	
		function closeCalendar() {
			var	sTmp;
			hideCalendar();
			ctlToPlaceValue.value =	constructDate(dateSelected,monthSelected,yearSelected);
		}
	
		/*** Month Pulldown	***/
		function StartDecMonth(){
			intervalID1=setInterval("decMonth()",80);
		}
	
		function StartIncMonth(){
			intervalID1=setInterval("incMonth()",80);
		}
	
		function incMonth () {
			monthSelected++;
			if (monthSelected>11) {
				monthSelected=0;
				yearSelected++;
			}
			constructCalendar();
		}
	
		function decMonth () {
			monthSelected--;
			if (monthSelected<0) {
				monthSelected=11;
				yearSelected--;
			}
			constructCalendar();
		}
	
		function constructMonth() {
			popDownYear();
			if (!monthConstructed) {
				sHTML =	"";
				for	(i=0; i<12;	i++) {
					sName =	monthName[i];
					if (i==monthSelected){
						sName =	"<B>" +	sName +	"</B>";
					}
					sHTML += "<tr><td id='m" + i + "' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='this.style.backgroundColor=\"\"' style='cursor:pointer' onclick='monthConstructed=false;monthSelected=" + i + ";constructCalendar();popDownMonth();event.cancelBubble=true'>&nbsp;" + sName + "&nbsp;</td></tr>";
				}
				document.getElementById("selectMonth").innerHTML = "<table width=70	style='font-family:arial; font-size:11px; border-width:1; border-style:solid; border-color:#a0a0a0;' bgcolor='#FFFFDD' cellspacing=0 onmouseover='clearTimeout(timeoutID1)'	onmouseout='clearTimeout(timeoutID1);timeoutID1=setTimeout(\"popDownMonth()\",100);event.cancelBubble=true'>" +	sHTML +	"</table>";
				monthConstructed=true;
			}
		}
	
		function popUpMonth() {
			constructMonth();
			crossMonthObj.visibility = (dom||ie)? "visible"	: "show";
			crossMonthObj.left = parseInt(crossobj.left) + 50;
			crossMonthObj.top =	parseInt(crossobj.top) + 26;
			hideElement( 'SELECT', document.getElementById("selectMonth") );
			hideElement( 'APPLET', document.getElementById("selectMonth") );			
		}
	
		function popDownMonth()	{
			crossMonthObj.visibility= "hidden";
		}
	
		/*** Year Pulldown ***/
		function incYear() {
			for	(i=0; i<7; i++){
				newYear	= (i+nStartingYear)+1;
				if (newYear==yearSelected){ 
					txtYear ="&nbsp;<B>"+ newYear +	"</B>&nbsp;";
				}else{ 
					txtYear ="&nbsp;" + newYear + "&nbsp;";
				}
				document.getElementById("y"+i).innerHTML = txtYear;
			}
			nStartingYear ++;
			bShow=true;
		}
	
		function decYear() {
			for	(i=0; i<7; i++){
				newYear	= (i+nStartingYear)-1
				if (newYear==yearSelected){ 
					txtYear ="&nbsp;<B>"+ newYear +	"</B>&nbsp;";
				}else{ 
					txtYear ="&nbsp;" + newYear + "&nbsp;";
				}
				document.getElementById("y"+i).innerHTML = txtYear;
			}
			nStartingYear --;
			bShow=true;
		}
	
		function selectYear(nYear) {
			yearSelected=parseInt(nYear+nStartingYear);
			yearConstructed=false;
			constructCalendar();
			popDownYear();
		}
	
		function constructYear() {
			popDownMonth();
			sHTML =	"";
			if (!yearConstructed) {
				sHTML =	"<tr><td align='center'	onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='clearInterval(intervalID1);this.style.backgroundColor=\"\"' style='cursor:pointer'	onmousedown='clearInterval(intervalID1);intervalID1=setInterval(\"decYear()\",30)' onmouseup='clearInterval(intervalID1)'>-</td></tr>"
				j =	0;
				nStartingYear =	yearSelected-3;
				for	(i=(yearSelected-3); i<=(yearSelected+3); i++) {
					sName =	i;
					if (i==yearSelected){
						sName =	"<B>" +	sName +	"</B>";
					}
					sHTML += "<tr><td id='y" + j + "' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='this.style.backgroundColor=\"\"' style='cursor:pointer' onclick='selectYear("+j+");event.cancelBubble=true'>&nbsp;" + sName + "&nbsp;</td></tr>"
					j ++;
				}
				sHTML += "<tr><td align='center' onmouseover='this.style.backgroundColor=\"#FFCC99\"' onmouseout='clearInterval(intervalID2);this.style.backgroundColor=\"\"' style='cursor:pointer' onmousedown='clearInterval(intervalID2);intervalID2=setInterval(\"incYear()\",30)'	onmouseup='clearInterval(intervalID2)'>+</td></tr>"
				document.getElementById("selectYear").innerHTML	= "<table width=44 style='font-family:arial; font-size:11px; border-width:1; border-style:solid; border-color:#a0a0a0;'	bgcolor='#FFFFDD' onmouseover='clearTimeout(timeoutID2)' onmouseout='clearTimeout(timeoutID2);timeoutID2=setTimeout(\"popDownYear()\",100)' cellspacing=0>"	+ sHTML	+ "</table>"
				yearConstructed	= true;
			}
		}
	
		function popDownYear() {
			clearInterval(intervalID1)
			clearTimeout(timeoutID1)
			clearInterval(intervalID2)
			clearTimeout(timeoutID2)
			crossYearObj.visibility= "hidden"
		}
	
		function popUpYear() {
			var	leftOffset
			constructYear()
			crossYearObj.visibility	= (dom||ie)? "visible" : "show"
			leftOffset = parseInt(crossobj.left) + document.getElementById("spanYear").offsetLeft
			if (ie){
				leftOffset += 6
			}
			crossYearObj.left =	leftOffset
			crossYearObj.top = parseInt(crossobj.top) +	26
		}
	
		/*** calendar ***/
		function WeekNbr(n) {
			// Algorithm used:
			// From Klaus Tondering's Calendar document (The Authority/Guru)
			// hhtp://www.tondering.dk/claus/calendar.html
			// a = (14-month) / 12
			// y = year + 4800 - a
			// m = month + 12a - 3
			// J = day + (153m + 2) / 5 + 365y + y / 4 - y / 100 + y / 400 - 32045
			// d4 = (J + 31741 - (J mod 7)) mod 146097 mod 36524 mod 1461
			// L = d4 / 1460
			// d1 = ((d4 - L) mod 365) + L
			// WeekNumber = d1 / 7 + 1
			year = n.getFullYear();
			month = n.getMonth() + 1;
			if (startAt == 0) {
				day = n.getDate() + 1;
			}else {
				day = n.getDate();
			}
			a = Math.floor((14-month) / 12);
			y = year + 4800 - a;
			m = month + 12 * a - 3;
			b = Math.floor(y/4) - Math.floor(y/100) + Math.floor(y/400);
			J = day + Math.floor((153 * m + 2) / 5) + 365 * y + b - 32045;
			d4 = (((J + 31741 - (J % 7)) % 146097) % 36524) % 1461;
			L = Math.floor(d4 / 1460);
			d1 = ((d4 - L) % 365) + L;
			week = Math.floor(d1/7) + 1;
			return week;
		}
	
		function constructCalendar () {
			var aNumDays = Array (31,0,31,30,31,30,31,31,30,31,30,31)
			var dateMessage
			var	startDate =	new	Date (yearSelected,monthSelected,1)
			var endDate
			if (monthSelected==1){
				endDate	= new Date (yearSelected,monthSelected+1,1);
				endDate	= new Date (endDate	- (24*60*60*1000));
				numDaysInMonth = endDate.getDate()
			}else{
				numDaysInMonth = aNumDays[monthSelected];
			}
			datePointer	= 0
			dayPointer = startDate.getDay() - startAt
			if (dayPointer<0){
				dayPointer = 6
			}
			sHTML =	"<table	 border=0 style='font-family:verdana;font-size:10px;'><tr>"
			if (showWeekNumber==1){
				sHTML += "<td width=27 style='font-family:宋体;font-size:12px;' align='center'><b>" + weekString + "</b></td><td width=1 rowspan=7 bgcolor='#d0d0d0' style='padding:0px'><img src='"+imgDir+"divider.gif' width=1></td>"
			}
			for	(i=0; i<7; i++)	{
				sHTML += "<td width='27' align='right' style='font-family:宋体;font-size:12px;'><B>"+ dayName[i]+"</B></td>"
			}
			sHTML +="</tr><tr>"
			if (showWeekNumber==1){
				sHTML += "<td align=right>" + WeekNbr(startDate) + "&nbsp;</td>"
			}
			for	( var i=1; i<=dayPointer;i++ ){
				sHTML += "<td>&nbsp;</td>"
			}
			for	( datePointer=1; datePointer<=numDaysInMonth; datePointer++ ){
				dayPointer++;
				sHTML += "<td align=right>"
				sStyle=styleAnchor
				if ((datePointer==odateSelected) &&	(monthSelected==omonthSelected)	&& (yearSelected==oyearSelected))
				{ sStyle+=styleLightBorder }
				sHint = ""
				for (k=0;k<HolidaysCounter;k++){
					if ((parseInt(Holidays[k].d)==datePointer)&&(parseInt(Holidays[k].m)==(monthSelected+1))){
						if ((parseInt(Holidays[k].y)==0)||((parseInt(Holidays[k].y)==yearSelected)&&(parseInt(Holidays[k].y)!=0))){
							sStyle+="background-color:#FFDDDD;"
							sHint+=sHint==""?Holidays[k].desc:"\n"+Holidays[k].desc
						}
					}
				}
				var regexp= /\"/g
				sHint=sHint.replace(regexp,"&quot;")
				dateMessage = "onmousemove='window.status=\""+selectDateMessage.replace("[date]",constructDate(datePointer,monthSelected,yearSelected))+"\"' onmouseout='window.status=\"\"' "
				if ((datePointer==dateNow)&&(monthSelected==monthNow)&&(yearSelected==yearNow)){ 
					sHTML += "<b><a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer+";closeCalendar();'><font color=#ff0000>&nbsp;" + datePointer + "</font>&nbsp;</a></b>"
				}else if	(dayPointer % 7 == (startAt * -1)+1){ 
					sHTML += "<a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer + ";closeCalendar();'>&nbsp;<font color=#909090>" + datePointer + "</font>&nbsp;</a>" 
				}else{ 
					sHTML += "<a "+dateMessage+" title=\"" + sHint + "\" style='"+sStyle+"' href='javascript:dateSelected="+datePointer + ";closeCalendar();'>&nbsp;" + datePointer + "&nbsp;</a>" 
				}
				sHTML += ""
				if ((dayPointer+startAt) % 7 == startAt) { 
					sHTML += "</tr><tr>" 
					if ((showWeekNumber==1)&&(datePointer<numDaysInMonth)){
						sHTML += "<td align=right>" + (WeekNbr(new Date(yearSelected,monthSelected,datePointer+1))) + "&nbsp;</td>"
					}
				}
			}
			document.getElementById("content").innerHTML   = sHTML
			document.getElementById("spanMonth").innerHTML = "&nbsp;" +	monthName[monthSelected] + "&nbsp;<IMG id='changeMonth' SRC='"+imgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>"
			document.getElementById("spanYear").innerHTML =	"&nbsp;" + yearSelected	+ "&nbsp;<IMG id='changeYear' SRC='"+imgDir+"drop1.gif' WIDTH='12' HEIGHT='10' BORDER=0>"
		}
	
		function popUpCalendar(ctl,	ctl2, format) {
			var	leftpos=0
			var	toppos=0
			if (bPageLoaded){
				if ( crossobj.visibility ==	"hidden" ) {
					ctlToPlaceValue	= ctl2;
					dateFormat=format;
					formatChar = " ";
					aFormat	= dateFormat.split(formatChar)
					if (aFormat.length<3){
						formatChar = "/";
						aFormat	= dateFormat.split(formatChar)
						if (aFormat.length<3){
							formatChar = ".";
							aFormat	= dateFormat.split(formatChar)
							if (aFormat.length<3){
								formatChar = "-";
								aFormat	= dateFormat.split(formatChar)
								if (aFormat.length<3){
									// invalid date	format
									formatChar="";
								}
							}
						}
					}
					tokensChanged =	0
					if ( formatChar	!= "" ){
						// use user's date
						aData =	ctl2.value.split(formatChar)
						for	(i=0;i<3;i++){
							if ((aFormat[i]=="d") || (aFormat[i]=="dd")){
								dateSelected = parseInt(aData[i], 10)
								tokensChanged ++
							}else if((aFormat[i]=="m") || (aFormat[i]=="mm")){
								monthSelected =	parseInt(aData[i], 10) - 1
								tokensChanged ++
							}else if(aFormat[i]=="yyyy"){
								yearSelected = parseInt(aData[i], 10)
								tokensChanged ++
							}else if(aFormat[i]=="mmm"){
								for	(j=0; j<12;	j++){
									if (aData[i]==monthName[j]){
										monthSelected=j
										tokensChanged ++
									}
								}
							}else if(aFormat[i]=="mmmm"){
								for	(j=0; j<12;	j++){
									if (aData[i]==monthName2[j]){
										monthSelected=j
										tokensChanged ++
									}
								}
							}
						}
					}
					if ((tokensChanged!=3)||isNaN(dateSelected)||isNaN(monthSelected)||isNaN(yearSelected)){
						dateSelected = dateNow
						monthSelected =	monthNow
						yearSelected = yearNow
					}
					odateSelected=dateSelected
					omonthSelected=monthSelected
					oyearSelected=yearSelected
					aTag = ctl
					do {
						aTag = aTag.offsetParent;
						leftpos	+= aTag.offsetLeft;
						toppos += aTag.offsetTop;
					} while(aTag.tagName!="BODY");
					crossobj.left =	fixedX==-1 ? ctl.offsetLeft	+ leftpos :	fixedX
					crossobj.top = fixedY==-1 ?	ctl.offsetTop +	toppos + ctl.offsetHeight +	2 :	fixedY
					constructCalendar (1, monthSelected, yearSelected);
					crossobj.visibility=(dom||ie)? "visible" : "show"
					hideElement( 'SELECT', document.getElementById("calendar") );
					hideElement( 'APPLET', document.getElementById("calendar") );			
					bShow = true;
				}else{
					hideCalendar()
					if (ctlNow!=ctl) {popUpCalendar(ctl, ctl2, format)}
				}
				ctlNow = ctl
			}
		}
		document.onkeypress = function hidecal1 () { 
			if (event.keyCode==27){
				hideCalendar()
			}
		}
		document.onclick = function hidecal2 () { 		
			if (!bShow){
				hideCalendar()
			}
			bShow = false
		}
		if(ie){
			init()
		}else{
			window.onload=init
		}
	</script>
		<script language="javascript"><!--
		function showSending() {
			sending.style.visibility="visible";
			cover.style.visibility="visible";
		}	
		function hideSending() {
			sending.style.visibility="hidden";
			cover.style.visibility="hidden";
		}	
		-->
	</script>
		<script language="javascript">
		var outLineFlag=<%=request.getAttribute("OutLineFlag").toString()%>;
		function back(){
			
			var url = outLineFlag == 0?"SelectNode.action":"SelectUser.action";
			window.location.href = url;
		}
		function clearUser(){
			document.all.UList.value = selectedData;
			document.all.List.src="SelectUserList.jsp";
		}
		function setSendMethod(nodeid,v){
			var temp=","+nodeid+":"
			if(v=="0"){
				temp+="1";
			}else{
				temp+="0";
			}
			document.all.SendMethod.value=document.all.SendMethod.value.replace(temp,","+nodeid+":"+v);	
		}
		function getSendMethod(nodeid){
			var s=document.all.SendMethod.value;
			var slist=s.split(","+nodeid+":");
			return slist[1].substr(0,1);
		}
	</script>
		<script language="javascript">
		var xmlHttp = false;
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		}catch(e){
			try {
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			}catch (e2) {
				xmlHttp = false;
			}
		}
		/**发送处理**/
		var sendLock = false;
		function SendSMS(){
			if($("#UList").val()==""){
				alert("请选择发送人员");
				//hideSending();
				return;
			}
			document.all.List.src="SelectUserList.jsp";
			document.all("TxtPriSend").value = "";
			if (<%=dsoap.tools.ConfigurationSettings.AppSettings("分级保护版本")%> == 1 && <%=strMj%> > 0){
				showSending();
				var sulist=document.all.UList.value.split(";");
	   			var strUid = "";
	   			for (var i=0;i<sulist.length;i++){
	   				if (sulist[i].length < 5){
	   					continue;
	   				}
	   				var stu = sulist[i].split(":")
	   				if (stu.length >= 2){
	   					strUid += stu[2] + ",";
	   				}
	   			}
	   			if (strUid.length > 1){
	   				strUid = strUid.substring(0,strUid.length-1)
	   			}
	    		document.all("TxtPriSend").value = strUid;
				var url = "CheckUserPri.aspx?users="+strUid+"&mj=<%=strMj%>";
				xmlHttp.open("GET", url, true);
				xmlHttp.onreadystatechange = ConfirmSend;
				xmlHttp.send(null);
			}else{			
				//alert(document.getElementById("UList").value);
				try{
					document.all.txtNode.value = document.all.SydateNode_DateControl.value;
					document.all.txtNode.value += " " + document.all.SydateNode_HourControl.value;
					document.all.txtNode.value += ":" + document.all.SydateNode_MinControl.value;
				}catch(e){
					document.all.txtNode.value="NULL";
				}
				//document.all.isSendSMS.value=document.all.ISSMS.checked;
				//document.all.isSendMAIL.value=document.all.ISMAIL.checked;
				//document.all.isSendTRAY.value=document.all.ISTRAY.checked;
				//document.all.SMSContent.value=escape(document.all.Content.value) + escape(document.all.Content1.value);
				var isSendMsg = document.getElementById("isSendMsg");
				var isSendmail = document.getElementById("isSendmail");
				var isSendOther = document.getElementById("isSendOther");
				if((isSendMsg && isSendMsg.checked)||(isSendmail && isSendmail.checked)||(isSendOther && isSendOther.checked)){
					var sms = document.getElementById("SMS");
					if(sms!=null&&$('#SMS').val()!=''){
						document.all.SMSContent.value = document.all.SMS.value;
						if(isSendMsg&&document.all.isSendMsg){
							document.all.isSendSMS.value=document.all.isSendMsg.checked;
						}
						if(isSendmail&&document.all.isSendmail){
							document.all.isSendMAIL.value=document.all.isSendmail.checked;
						}
						if(isSendOther&&document.all.isSendOther){
							document.all.isSendTRAY.value=document.all.isSendOther.checked;
						}
						
					}else{
						top.alert("\u8BF7\u586B\u5199\u53D1\u9001\u7684\u5185\u5BB9!");//请填写发送的内容
						return ;
					}
					
				}
				showSending();
				
				document.all.isHideYJ.value=document.all.chIsHideYJ.checked;
				if(!sendLock){
					sendLock = true;
					//增加批量发送判断逻辑 杨龙修改 2012/9/26 开始   modify by john.he 20121008
					 var sendBatch='<%=sendBatch%>';
					if(sendBatch!="")
					{
						//如果是批量发送则跳转到批量发送的action
						//document.SendList.action="SendBatch.action";
						$("#SendList").attr("action", "SendBatch.action");
					}
					//增加批量发送判断逻辑 杨龙修改 2012/9/26 结束	
					//document.SendList.submit();
					$("#SendList").submit();
				}
			}
		}
		function Send(){
			
			if($("#UList").val()==""){
				alert("请选择发送人员");
				//hideSending();
				return;
			}
			
			showSending();
			
			document.all("TxtPriSend").value = "";
			if (<%=dsoap.tools.ConfigurationSettings.AppSettings("分级保护版本")%> == 1 && <%=strMj%> > 0){
				showSending();
				var sulist=document.all.UList.value.split(";");
	   			var strUid = "";
	   			for (var i=0;i<sulist.length;i++){
	   				if (sulist[i].length < 5){
	   					continue;
	   				}
	   				var stu = sulist[i].split(":")
	   				if (stu.length >= 2){
	   					strUid += stu[2] + ",";
	   				}
	   			}
	   			if (strUid.length > 1){
	   				strUid = strUid.substring(0,strUid.length-1)
	   			}
	    		document.all("TxtPriSend").value = strUid;
				var url = "CheckUserPri.aspx?users="+strUid+"&mj=<%=strMj%>";
				xmlHttp.open("GET", url, true);
				xmlHttp.onreadystatechange = ConfirmSend;
				xmlHttp.send(null);
			}else{			
				//alert(document.getElementById("UList").value);
				try{
					document.all.txtNode.value = document.all.SydateNode_DateControl.value;
					document.all.txtNode.value += " " + document.all.SydateNode_HourControl.value;
					document.all.txtNode.value += ":" + document.all.SydateNode_MinControl.value;
				}catch(e){
					document.all.txtNode.value="NULL";
				}
				//document.all.isSendSMS.value=document.all.ISSMS.checked;
				//document.all.isSendMAIL.value=document.all.ISMAIL.checked;
				//document.all.isSendTRAY.value=document.all.ISTRAY.checked;
				//document.all.SMSContent.value=escape(document.all.Content.value) + escape(document.all.Content1.value);
				var isSendMsg = document.getElementById("isSendMsg");
				var isSendmail = document.getElementById("isSendmail");
				var isSendOther = document.getElementById("isSendOther");
				if((isSendMsg && isSendMsg.checked)||(isSendmail && isSendmail.checked)||(isSendOther && isSendOther.checked)){
					var sms = document.getElementById("SMS");
	//				if(sms!=null&&$('#SMS').val()!=''){
						document.all.SMSContent.value = document.all.SMS.value;
						if(isSendMsg&&document.all.isSendMsg){
							document.all.isSendSMS.value=document.all.isSendMsg.checked;
						}
						if(isSendmail&&document.all.isSendmail){
							document.all.isSendMAIL.value=document.all.isSendmail.checked;
						}
						if(isSendOther&&document.all.isSendOther){
							document.all.isSendTRAY.value=document.all.isSendOther.checked;
						}
						
//					}
					
				}
				//showSending();
				document.all.isHideYJ.value=document.all.chIsHideYJ.checked;
				if(!sendLock){
					sendLock = true;
					//增加批量发送判断逻辑 杨龙修改 2012/9/26 开始   modify by john.he 20121008
					 var sendBatch='<%=sendBatch%>';
					if(sendBatch!="")
					{
						//如果是批量发送则跳转到批量发送的action
						$("#SendList").attr("action", "SendBatch.action");
					}
					//增加批量发送判断逻辑 杨龙修改 2012/9/26 结束	
					$("#SendList").submit();
				}
			}
		}
		
		function ConfirmSend(){
			if (xmlHttp.readyState == 4){
				var response = xmlHttp.responseText;
				if (response != ""){
					if (!confirm(response)){
						hideSending();
						return;
					}
				}
				try{
					document.all.txtNode.value = document.all.SydateNode_DateControl.value;
					document.all.txtNode.value += " " + document.all.SydateNode_HourControl.value;
					document.all.txtNode.value += ":" + document.all.SydateNode_MinControl.value;
				}catch(e){
					document.all.txtNode.value="NULL";
				}	
				//document.all.isSendSMS.value=document.all.ISSMS.checked;
				//document.all.isSendMAIL.value=document.all.ISMAIL.checked;
				//document.all.isSendTRAY.value=document.all.ISTRAY.checked;
				//document.all.SMSContent.value=escape(document.all.Content.value) + escape(document.all.Content1.value);
				document.all.isHideYJ.value=document.all.chIsHideYJ.checked;
				document.SendList.submit();		
			}
		}
	
		function TextLengthTest(){
			var strlist=document.all.Content.value.split("");
			var num=0;
			for(var i=0;i<strlist.length;i++){
				var str=strlist[i];
				if(/([^\x00-\xff])/.test(str)){
					num+=2;
				}else{
					num+=1;
				}
			}
			if(num>140){
				L_NUM.innerHTML="<FONT face='宋体' color=red>"+num/2+"</FONT>";
			}else{
				L_NUM.innerHTML=num/2;
			}
		}
	</script>
	</head>
	<body background="../images/bg_body_right.gif" topMargin="0"
		style="border: 0px solid #00ff00; display: none;">
		<!-- <a href="ReturnNodesShow.action">任意退回</a> -->
		<!-- <a href="sendToEnd.jsp">办结</a> -->
		<form id="SendList" name="SendUserList" action="Sending.action"
			method="POST">
			<INPUT id="SendMethod" type="text" name="SendMethod"
				style="DISPLAY: none"
				value="<%=request.getAttribute("SendMethod").toString()%>">
			<INPUT id="UList" style="DISPLAY: none" type="text" name="UList"
				value="">
			<INPUT id="isSendSMS" style="DISPLAY: none" type="text"
				name="isSendSMS">
			<INPUT id="isSendMAIL" style="DISPLAY: none" type="text"
				name="isSendMAIL">
			<INPUT id="isSendTRAY" style="DISPLAY: none" type="text"
				name="isSendTRAY">
				
			<INPUT id="SMSContent" style="DISPLAY: none" type="text"
				name="SMSContent">
			<INPUT id="isHideYJ" style="DISPLAY: none" type="text"
				name="isHideYJ">
			<INPUT id="txtNode" style="DISPLAY: none" type="text" name="txtNode">
			<INPUT id="TxtPriSend" type="hidden" name="TxtPriSend">
		</form>
		<div id="sending" style="width: 100%; z-index: 10; padding: 0 20 0 20px; visibility: hidden; position: absolute; top: 220px;">
			<TABLE cellSpacing="0" cellPadding="0" width="100%" border="0">
				<TR>
					<td width="30%"></td>
					<TD bgColor="#ff9900">
						<TABLE height="70" cellSpacing="2" cellPadding="0" width="100%"
							border="0">
							<TR>
								<td align="center" bgColor="#eeeeee">
									正在发送文件, 请稍候...
								</td>
							</TR>
						</TABLE>
					</TD>
					<td width="30%"></td>
				</TR>
			</TABLE>
		</div>
		<div id="cover"
			style="z-index: 9; left: 0px; visibility: hidden; width: 100%; position: absolute; top: 0px;">
		</div>
		<table height="100%" cellSpacing="0" cellPadding="0" width="100%"
			align="center" border="0">
			<tr>
				<td>
					<form id="Form1" method="post">
					<!--增加线上条件提示   杨龙修改 2012/9/11 -->
					<%=request.getAttribute("wfTip") == null ? "" : request.getAttribute("wfTip").toString()%>
						<table height="100%" cellSpacing="0" cellPadding="0" width="100%"
							border="0">
							<tr vAlign="top">
								<td width='300px;'>
									<div id="jstree" class="select_user_tree"
										<%=" style='width:240px; height:"
					+ (showSearch ? "340px;" : "365px;") + "' "%>><%=((dsoap.tools.tree.Tree) request.getAttribute("TvUsers"))
					.getTreeHTML1()%></div>
									<%
										if (showSearch) {
									%>
									<table border="0" cellSpacing="0" cellPadding="0"
										bgColor="#a2c1dd" style="margin-top: 5px;">
										<tr>
											<td>
												<input class="user_tree_search" id="searchText" onkeydown='keyselectUser1()'/>
											</td>
											<td>
												<div class="user_tree_text" onclick="selectUser1()">
													查找
												</div>
											</td>
											<td>
												<div class="user_tree_text" onclick="next(1)">
													下一个
												</div>
											</td>
										</tr>
									</table>
									<%
										}
									%>
								</td>
								<td>
									<table cellSpacing="0" cellPadding="0" width="100%" border="0">
										<tr>
											<td>
												<iframe id="List"
													height='<%=msg_lock == false ? "363" : "277"%>px'
													style="width: 100%; border: 1px solid #DFDFDF;" name="List"
													align="right" src='SelectUserList.jsp' frameBorder="0"></iframe>
											</td>
										</tr>
										<%
											if (sms != null) {
										%>
										<tr>
											<td>
												<div style="height: 20px;">
													<% 
														if(msg_lock&&isForword){
													%>
													&nbsp;短信提醒<input type="checkbox" name="isSendMsg"  checked="checked"/>
													<%
														}else if(msg_lock&&!isForword){
															%>
															&nbsp;短信提醒<input type="checkbox" name="isSendMsg" />
													<%		
														}
													%>
													<% 
														if(msg_lock&&isEMail){
													%>
													&nbsp;邮件提醒<input type="checkbox" name="isSendmail"  checked="checked"/>
													<%
														}else if(msg_lock&&!isEMail){
															%>
															&nbsp;邮件提醒<input type="checkbox" name="isSendmail" />
															<%
														}
													%>
													<% 
														if(msg_lock&&isTray){
													%>
													&nbsp;消息提醒<input type="checkbox" name="isSendOther"  checked="checked"/>
													<%
														}else if(msg_lock&&!isTray){
															%>
															&nbsp;消息提醒<input type="checkbox" name="isSendOther" />
															<%
														}
													%>
												</div>
												<% if(msg_lock){
													
												%>
												<textarea id="SMS" style="width: 100%; height: 65px;font-size:12px;"><%=sms%></textarea>
												<%
													}
												%>
											</td>
										</tr>
										<%
											}
										%>
									</table>
								</td>
							</tr>
							<tr height="0">
								<td>
									<input id="chIsHideYJ" type="checkbox" value="1"
										style="DISPLAY: none" name="chIsHideYJ"
										<%=chIsHideYJ ? "checked=\"checked\"" : ""%>>
								</td>
							</tr>
						</table>
					</form>
				</td>
			</tr>
			<tr>
				<td>
					<table cellSpacing="0" cellPadding="0" width="100%" border="0"
						style="padding-top: 6px; border-top: 1px solid #DFDFDF; width: 100%;">
						<tr>
							<td align="right">
								<div id="subBu" onclick="SendSMS();" class="select_user_send">
									发送
								</div>
							</td>
							<td align="<%=showClose ? "center" : "left"%>">
								<div id="colBu" onclick="clearUser();" class="select_user_reselect">清空</div>
							</td>
							<td align="<%=showClose ? "center" : "left"%>">
								<div id="colBu" onclick="back();" class="select_user_reselect">重新选择</div>
							</td>
							<%
								if (showClose) {
							%>
							<td align="left">
								<div onclick="top.window.sendFormWin.hide();"
									class="select_user_reselect">
									关闭
								</div>
							</td>
							<%
								}
							%>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</body>
	<%=request.getAttribute("JavaScriptLab").toString()%>
	<script type='text/javascript'>
	var selectedData = document.all.UList.value;
	function checkUser(NODE){
		//alert(NODE.getAttribute("datalist"));
		//var DataList=NODE.datalist.split(",");
		//alert(NODE.getAttribute("datalist"));
		var DataList=NODE.getAttribute("datalist").split(",");
		//0:sUType,1:sUserID,2:sUName,
		//3:sNodeID,4:sSendMethod,5:sSelected,6:sMultiUser,7:sNodeID,8:sTimeType,9:sTimeSpan,10:sNodeCaption
		//11:fName,12:fID,13:topName
		var sNodeID=DataList[3];
		var sUserID=DataList[1];
		var sUserName=DataList[2];
		if (DataList[13] != "") {
			sUserName += "("+DataList[13]+")";
		}
		var sSendMethod=getSendMethod(sNodeID);
		var sMultiUser=DataList[6];
		var sNodeCaption=DataList[10];
		var sfName=DataList[11];
		var sFid=DataList[12];
		var sustr=document.all.UList.value;
		var sulist=document.all.UList.value.split(";");
		var iOutLineFlag='<%=request.getAttribute("iOutLineFlag") == null? "": request.getAttribute("iOutLineFlag").toString()%>';
		var isCS='<%=isCS%>';
		if(sMultiUser=="1"){
			var s=":"+sNodeID+":"+sUserID+":"+sNodeCaption+":"+sfName+":"+sUserName+":"+sFid + ":";
			//alert(";"+s);
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 开始
			//或分则且非抄送发送 才做多发限制
			if(0==iOutLineFlag&&1!=isCS)
				{
				var sulist=document.all.UList.value.split(";");
				if(sulist!="")
					{
						var sLeft=sNodeID;
						var sRight;
						var stu = sulist[1].split(":")
	   					if (stu.length >= 2){
								sRight=stu[1];
							if(sLeft!=sRight)
	   						{
	   							sustr="";
	   						}
	   						}
					}
				}
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 结束
			if(sustr.indexOf(";"+s)==-1){
				sustr+=";"+s;
			}
		}else{
			var s=":"+sNodeID+":"+sUserID+":"+sNodeCaption+":"+sfName+":"+sUserName+":"+sFid+":";
			var st=":"+sNodeID+":";
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 开始
			if(0==iOutLineFlag&&1!=isCS)
				{
					var sulist=document.all.UList.value.split(";");
						if(sulist!="")
							{
								var sLeft=sNodeID;
								var sRight;
								var stu = sulist[1].split(":")
	   							if (stu.length >= 2){
									sRight=stu[1];
	   								}
	   						if(sLeft!=sRight)
	   							{
	   								sustr="";
	   							}
							}
					}
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 结束
			for(var i=0;i<sulist.length;i++){
				if(sulist[i].indexOf(st)==-1){
					continue;
				}else{
				//alert(sulist[i]);
					if(sulist[i].split(":").length<9){
						sustr=sustr.replace(";"+sulist[i],"");
					}else{
						s="";
					}
					break;
				}
			}
			sustr+=(s=="")?"":(";"+s);
		}
		document.all.UList.value = sustr;
		document.all.List.src="SelectUserList.jsp";
	}
	function checkDept(NODE) {
		//$(NODE).find("li").each(function (index,_NODE) {
		//	if("User" == _NODE.nodetype){
		//		checkUser(_NODE);
		//	}else if("Dept" == _NODE.nodetype){
		//		//checkDept(_NODE);
		//	}
		//});
		<%=cascade ? "" : "return;"%>
		NODE = NODE.parentNode.nextSibling;
		$(NODE).find("A").each(function (index,_NODE) {
			if("User" == _NODE.getAttribute("nodetype")){
				checkUser(_NODE);
			}else{
				checkDept(_NODE);
			}
		});
	}
	function expanded(obj){
		var t = (obj.className == "tree_node_img_d_1" ? "block" : "none");
		if(obj.parentNode.nextSibling != null){
			obj.parentNode.nextSibling.style.display = (t == "block" ? "none" : "block");
		}
		obj.className = (t == "block" ? "tree_node_img_d_2" : "tree_node_img_d_1");
	}
	document.oncontextmenu = function(){};
	window.onload = function(){
		try{
			/* 暂时注释   这里要调整eui需要自动变化 矿高 by John He 20140122 */
			var win = top.window.sendFormWin;
			/**
			win.element.dialog('option', 'height', 500);
			win.element.dialog('option', 'width', 600);
			win.element.dialog('option', 'position', "center");
			*/
			
			//弹出窗口调整  2014.3.20 taolb
			win.setWH({
				height:500,
				width:780
			}); 
			document.body.style.display = "block";
			//默认选中和必选初始化选择  杨龙修改 2012/9/19 
			initSelectTree();
			
			
		}catch(e){}
		$(window.frameElement).css("height","100%");
	}
	//默认选中和必选初始化选择  杨龙修改 2012/9/19 开始
	function initSelectTree()
	{
		var treeDiv = document.getElementById('jstree');
		checkInit(treeDiv);
	}
	function checkInit(NODE) {
		$(NODE).find("A").each(function (index,_NODE) {
			if("User" == _NODE.getAttribute("nodetype")){
				var DataList=_NODE.getAttribute("datalist").split(",");
				var sfName=DataList[11];
				if(""!=sfName)
				{
					if(sfName.indexOf('默认')>-1||sfName.indexOf('必选')>-1)
					{
					checkInitUser(_NODE);
					}
				}
			}
		});
	}
	function checkInitUser(NODE){
		//alert(NODE.getAttribute("datalist"));
		//var DataList=NODE.datalist.split(",");
		var DataList=NODE.getAttribute("datalist").split(",");
		//0:sUType,1:sUserID,2:sUName,
		//3:sNodeID,4:sSendMethod,5:sSelected,6:sMultiUser,7:sNodeID,8:sTimeType,9:sTimeSpan,10:sNodeCaption
		//11:fName,12:fID,13:topName
		var sNodeID=DataList[3];
		var sUserID=DataList[1];
		var sUserName=DataList[2];
		if (DataList[13] != "") {
			sUserName += "("+DataList[13]+")";
		}
		var sSendMethod=getSendMethod(sNodeID);
		var sMultiUser=DataList[6];
		var sNodeCaption=DataList[10];
		var sfName=DataList[11];
		var sFid=DataList[12];
		var sustr=document.all.UList.value;
		var sulist=document.all.UList.value.split(";");
		if(sMultiUser=="1"){
			var s=":"+sNodeID+":"+sUserID+":"+sNodeCaption+":"+sfName+":"+sUserName+":"+sFid;
			//alert(";"+s);
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 开始
			var iOutLineFlag='<%=request.getAttribute("iOutLineFlag") == null
					? ""
					: request.getAttribute("iOutLineFlag").toString()%>';
			if(0==iOutLineFlag)
				{
				var sulist=document.all.UList.value.split(";");
				if(sulist!="")
					{
						var sLeft=sNodeID;
						var sRight;
						var stu = sulist[1].split(":")
	   					if (stu.length >= 2){
								sRight=stu[1];
	   						}
	   					if(sLeft!=sRight)
	   						{
	   							sustr="";
	   						}
					}
				}
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 结束
			if(sfName.indexOf('默认')>-1||sfName.indexOf('必选')>-1)
			{
			if(sustr.indexOf(";"+s)==-1){
				sustr+=";"+s;
			}
			}
		}else{
			var s=":"+sNodeID+":"+sUserID+":"+sNodeCaption+":"+sfName+":"+sUserName+":"+sFid;
			var st=":"+sNodeID+":";
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 开始
			var iOutLineFlag='<%=request.getAttribute("iOutLineFlag") == null
					? ""
					: request.getAttribute("iOutLineFlag").toString()%>';
			if(0==iOutLineFlag)
				{
					var sulist=document.all.UList.value.split(";");
						if(sulist!="")
							{
								var sLeft=sNodeID;
								var sRight;
								var stu = sulist[1].split(":")
	   							if (stu.length >= 2){
									sRight=stu[1];
	   								}
	   						if(sLeft!=sRight)
	   							{
	   								sustr="";
	   							}
							}
					}
			//如果是或分支则不能同时选多个节点的人 杨龙修改 2012/9/17 结束
			if(sfName.indexOf('默认')>-1||sfName.indexOf('必选')>-1)
			{
			for(var i=0;i<sulist.length;i++){
				if(sulist[i].indexOf(st)==-1){
					continue;
				}else{
				//alert(sulist[i]);
					if(sulist[i].split(":").length<8){
						sustr=sustr.replace(";"+sulist[i],"");
					}else{
						s="";
					}
					break;
				}
			}
			sustr+=(s=="")?"":(";"+s);
			}
		}
		document.all.UList.value = sustr;
		document.all.List.src="SelectUserList.jsp";
	}
	//默认选中和必选初始化选择  杨龙修改 2012/9/19 结束
	//--------------------------------------------------------------------------------------查询 定位
	var aMatch = [];
	var currentIndex=0;
	
	function keyselectUser1(){
		if (event.keyCode==13){  //回车键的键值为13 
			  if (event.srcElement.id == "searchText") //如果最后一个焦点在验证码上
	            {
	                event.returnValue = false;
	                selectUser1(); 
	            }
	     }  
	}
	
	function selectUser1(){
		var condition = document.getElementById('searchText').value;
		var treeDiv = document.getElementById('jstree');
		if(condition == null || condition == ''){
			clearAMatch();
			return;
		}
		var A = $(treeDiv).find("A");
		var N = A.length;
		clearAMatch();
		for(var i=0; i<N; i++){
			if(A[i].innerHTML.indexOf(condition)>-1){
				addAMatch(A[i]);
			}
		}
		currentIndex=0;
		position();
		
	}
	function clearAMatch(){

		for(var i=0; i<aMatch.length; i++){
			aMatch[i].style.color = "black";
			//var p = aMatch[i].parentNode.parentNode;
			//while(p.tagName != "DIV"){
			//	if(p.tagName=="LI"){
			//		p.className = p.className.replace("open","closed");
			//	}
			//	p = p.parentNode;
			//}
		//	var p = aMatch[i].parentNode.parentNode.parentNode;
		//	while(p.id != "jstree"){
		//		p.style.display = "none";
		//		p.previousSibling.firstChild.className = "tree_node_img_d_2";
		//		p = p.parentNode.parentNode;
		//	}
			aMatch[i] = null;
		}
		aMatch = null;
		aMatch = [];
	}
	function addAMatch(a){
		//var p = a.parentNode.parentNode;
		//while(p.tagName != "DIV"){
		//	if(p.tagName=="LI"){
		//		p.className = p.className.replace("closed","open");
		//	}
		//	p = p.parentNode;
		//}
		var p = a.parentNode.parentNode.parentNode;
		while(p.id != "jstree"){
			p.style.display = "block";
			p.previousSibling.firstChild.className = "tree_node_img_d_1";
			p = p.parentNode.parentNode;
		}
		aMatch[aMatch.length] = a;
	}
	function next(direct){
		if(aMatch.length == 0){
			return;
		}
		aMatch[currentIndex].style.color = "black";
		currentIndex++;
		if(currentIndex >= aMatch.length){
			currentIndex = 0;
		}
		position();
	}
	function position(){
		if(currentIndex<aMatch.length){
			var a = aMatch[currentIndex];
			a.style.color = "red";
			//document.getElementById("jstree").scrollTop=$(a).offset().top;
			var t = a.offsetTop;
			var p = a.parentNode;
			while(p.tagName != "DIV"){
				p = p.parentNode;
				t += p.offsetTop;
			}
			document.getElementById("jstree").scrollTop = t;
		}
	}
</script>
</html>
