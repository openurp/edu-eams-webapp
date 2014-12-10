[#ftl]
<script>
  function SetPrintSettings(){}
  function exportToExcel(){alert("Not Support!");}
  function exportToWord(){alert("Not Support!");}
  function setTopMargin(margin){}
  function setLeftMargin(margin){}
  function setRightMargin(margin){}
  function setBottomMargin(margin){}
  //flag:true纵向;flase横向;
  function setPortrait(flag){}
</script>
[#assign useragent=request.getHeader("USER-AGENT")]
[#if useragent?contains('Firefox')]
<div class="notprint" style="display:none;text-align:center" id="jssetupprint_tip">
  <a href="https://addons.mozilla.org/firefox/downloads/latest/8966/addon-8966-latest.xpi?src=dp-btn-primary">
    <span></span>
    </a>
</div>
<script>
  if(typeof jsPrintSetup=="undefined"){
    jQuery("#jssetupprint_tip").find("span").text("下载并安装火狐打印插件");
    document.getElementById('jssetupprint_tip').style.display="";
    if(confirm('为了提高打印效果,请火狐用户安装打印插件,点击确定开始下载')){
        window.open('https://addons.mozilla.org/firefox/downloads/latest/8966/addon-8966-latest.xpi?src=dp-btn-primary');
      }
  }else{
    SetPrintSettings=function(){
      jsPrintSetup.setOption('headerStrLeft', ' ');
      jsPrintSetup.setOption('headerStrCenter', ' ');
      jsPrintSetup.setOption('headerStrRight', ' ');
      jsPrintSetup.setOption('footerStrLeft', ' ');
      jsPrintSetup.setOption('footerStrCenter', ' ');
      jsPrintSetup.setOption('footerStrRight', ' ');
    }
    function print(){jsPrintSetup.print();}
    setTopMargin =function(margin){jsPrintSetup.setOption('marginTop',margin);}
    setLeftMargin=function(margin){jsPrintSetup.setOption('marginLeft',margin);}
    setRightMargin=function (margin){jsPrintSetup.setOption('marginRight',margin);}
    setBottomMargin=function (margin){jsPrintSetup.setOption('marginBottom',margin);}
    setPortrait=function(flag){
      if(flag){
        jsPrintSetup.setOption('orientation', jsPrintSetup.kPortraitOrientation);
      }else{
        jsPrintSetup.setOption('orientation', jsPrintSetup.kLandscapeOrientation);
      }
    }
  }
</script>
[#elseif useragent?contains('Chrome') ||useragent?contains('Opera')]
<style type="text/css" media="print">
@page{
  size: auto; /* auto is the initial value */
  margin: 0mm; /* this affects the margin in the printer settings */
}
body {
  background-color:#FFFFFF;
  margin: 0px; /* this affects the margin on the content before sending to printer */
}
</style> 
[#elseif useragent?contains('MSIE')]
<object id="factory" style="display:none" viewastext classid="clsid:1663ed61-23eb-11d2-b92f-008048fdd814" codebase="${request.getContextPath()}/static/themes/default/css/smsx.cab#Version=7.1.0.60"></object>
<script type="text/javascript">
  SetPrintSettings =function () { 
     try{
       if(typeof factory.printing != 'undefined'){ 
       factory.printing.header = ""; 
       factory.printing.footer = "";
       }
     }catch(e){}
  }
  setTopMargin =function(margin){factory.printing.topMargin=margin;}
  setLeftMargin=function(margin){factory.printing.leftMargin=margin;}
  setRightMargin=function (margin){factory.printing.rightMargin=margin;}
  setBottomMargin=function (margin){factory.printing.bottomMargin=margin;}
  setPortrait=function(flag){
    factory.printing.portrait = flag;
  }
  
  function newActiveX(name){
      try{
       return  new ActiveXObject(name); 
      }catch(e){
        alert("1.导出程序需要你本机安装Microsoft办公程序及其组件."+
        "\n2.在IE浏览器菜单项中:[工具]->[internet选项..]->[安全]选项卡中选择[自定义级别..]按钮,之后将其中activeX部分的设置改为'启用'.");
        return;
      }
  }
    
    //指定页面区域内容导入Excel
  function exportToExcel(elemId)  {
    var oXL = newActiveX("Excel.Application");
    if(null==oXL) return;
    var oWB = oXL.Workbooks.Add();
    var oSheet = oWB.ActiveSheet;
    var sel=document.body.createTextRange();
    sel.moveToElementText(document.getElementById(elemId));
    sel.select();
    sel.execCommand("Copy");
    oSheet.Paste();
    oXL.Visible = true;
  }

  //指定页面区域内容导入Word
  function exportToWord(elemId) {
    var oWD= newActiveX("Word.Application");
    if(null==oWD) return;
    var oDC = oWD.Documents.Add("",0,1);
    var oRange =oDC.Range(0,1);
    var sel = document.body.createTextRange();
    sel.moveToElementText(document.getElementById(elemId));
    sel.select();
    sel.execCommand("Copy");
    oRange.Paste();
    oWD.Application.Visible = true;
    //window.close();
  }
</script>
[/#if]
<script>
  SetPrintSettings();
</script>