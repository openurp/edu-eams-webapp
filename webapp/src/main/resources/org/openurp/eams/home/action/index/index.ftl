[#ftl]
[#assign base = request.getContextPath()/]
[@b.head theme="admin"/]
  <body>
    <div class="header">
       <div class="" style="padding:0px;">
          <div class="pull-left switch_system_div">
            <img class="cmd5_1" src="${base}/static/themes/admin/images/invis.gif"/>
          </div>
          <ul class="nav nav-tabs pull-left top-menu" role="tablist">
          </ul>
          <div class="navbar navbar-inverse pull-right" role="banner" style="margin-bottom:0px;">
              <form action="${b.url("!index")}" class="form-inline pull-left" role="form" style="padding:8px" method="post">
                <div class="form-group">
                  <select name="semesterId" class="form-control">
                    <option value="">学年学期</option>
                    [#list semesters?sort_by('code')?reverse as s]
                    <option value="${s.id}" [#if semesterId?? && semesterId == s.id?string]selected="selected"[/#if]>${s.code}</option>
                    [/#list]
                  </select>
                </div>
                <div class="form-group">
                  <select name="projectId" class="form-control">
                    <option value="">项目</option>
                    [#list projects as p]
                    <option value="${p.id}" [#if projectId?? && projectId == p.id?string]selected="selected"[/#if]>${p.name}</option>
                    [/#list]
                  </select>
                </div>
                <button type="submit" class="btn btn-default">切换</button>
              </form>
              <nav class="collapse navbar-collapse bs-navbar-collapse navbar-right" role="navigation">
                <ul class="nav navbar-nav">
                  <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">欢迎您，张三 <b class="caret"></b></a>
                    <ul class="dropdown-menu animated fadeInUp">
                      <li><a href="#">退出</a></li>
                    </ul>
                  </li>
                </ul>
              </nav>
          </div>
       </div>
  </div>

    <div class="page-content">
      <div class="row">
      <div class="col-md-2">
        <div class="sidebar content-box" style="display: block;">
                <ul class="nav submenus">
                    <!-- Main menu -->
                    <li class="current"><a href="#"><i class="glyphicon glyphicon-home"></i> Dashboard</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-calendar"></i> Calendar</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-stats"></i> Statistics (Charts)</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-list"></i> Tables</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-record"></i> Buttons</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-pencil"></i> Editors</a></li>
                    <li><a href="#"><i class="glyphicon glyphicon-tasks"></i> Forms</a></li>
                    <li class="submenu">
                         <a href="#">
                            <i class="glyphicon glyphicon-list"></i> Pages
                            <span class="caret pull-right"></span>
                         </a>
                         <!-- Sub menu -->
                         <ul>
                            <li><a href="#">Login</a></li>
                            <li class="submenu2">
                               <a href="#">
                                  <i class="glyphicon glyphicon-list"></i> Pages
                                  <span class="caret pull-right"></span>
                               </a>
                               <!-- Sub menu -->
                               <ul>
                                  <li><a href="#">Login</a></li>
                                  <li><a href="#">Signup</a></li>
                              </ul>
                          </li>
                        </ul>
                    </li>
                    [#--
                    --]
                </ul>
             </div>
      </div>
      <div class="col-md-10">
      [#--
      <iframe name="page_right" width="100%" height="600"></iframe>
      --]
        <div id="page_right" class="ajax_container content-box-large"style="min-height:600px;">Welcome!</div>
      </div>
    </div>
    </div>

    <footer>
       <div class="container">
          <div class="copy text-center">
             Copyright 2014 <a href='#'>Website</a>
          </div>
       </div>
    </footer>
    <script>
    $.getJSON('http://data.urp.sfu.edu.cn/app/openurp-eams-webapp/func/menus/2.json?top=true', function (data){
      var topMenu = $('.top-menu')
      for(var i in data){
        var menu = data[i];
        console.log(menu);
        var li = $('<li role="presentation"><a href="#">'+menu.name+'</a></li>');
        (function (li, menu){
          li.click(function (){
            showSubmenus(menu.indexno);
            return false;
          });
        })(li, menu);
        topMenu.append(li);
      }
      var lis = topMenu.find("li");
      lis.first().addClass('active').click();
      lis.click(function (){
        lis.removeClass('active');
        $(this).addClass('active');
      });
    });
    
    function showSubmenus(indexno){
      $.getJSON('http://data.urp.sfu.edu.cn/app/openurp-eams-webapp/func/menus/2.json?indexno=' + indexno, function (data){
        var nav = $('.submenus').empty();
        for(var i in data){
          var menu = data[i], li;
          console.log(menu);
          if(menu.children && menu.children.length > 0){
            li = $('<li class="submenu"><a href="#">'+menu.name+'<span class="caret pull-right"></span></a></li>');
            var ul2 = $('<ul></ul>');
            li.append(ul2);
            for(var j in menu.children){
              var menu2 = menu.children[j];
              var li2 = $('<li><a href="http://webapp.urp.sfu.edu.cn/party'+menu2.entry+' onclick="bg.Go(this, \'page_right\');return false;" target="page_right">'+menu2.name+'</a></li>');
              ul2.append(li2);
            }
          }else{
            li = $('<li><a href="'+"/eams-grade"+menu.entry+'" onclick="bg.Go(this, \'page_right\');return false;" target="page_right"><i class="glyphicon glyphicon-stats"></i> '+menu.name+'</a></li>');
          }
          nav.append(li);
        }
        nav.find(".submenu > a").click(function (){
          nav.find('.submenu ul').slideUp();
          var a = $(this), li = a.parent();
          if(!li.hasClass('open')){
            li.find('ul').slideDown(function (){
              li.addClass('open');
            });
          }
          nav.find('.submenu').removeClass('open');
        });
      });
    }
    </script>
[@b.foot theme="admin"/]