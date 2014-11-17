
function OnReturn(form){
   this.form=form;
   this.elemts=new Array();
   this.add=addElemement;
   this.clear=function(){this.elemts.length=0};
   this.focus=setFocus;
   this.select=true;
}

function addElemement(ele){
  this.elemts.push(ele);
  return this.elemts.length;
}
  // 设置焦点
  function setFocus(event){
     if(event.keyCode==13){
        var target = bg.event.getTarget(event).name;
        for(var i=0;i<this.elemts.length-1;i++){
          if(target==this.elemts[i]){
             if(this.form[this.elemts[i+1]] && this.form[this.elemts[i+1]].type!="hidden"){
                 this.form[this.elemts[i+1]].focus();
                 if(this.form[this.elemts[i+1]].type=="text"){
                    this.form[this.elemts[i+1]].select();
                 }
                 break;
             }else{
                 target=this.elemts[i+1];
                 continue;
             }
          }
        }
     }
  }