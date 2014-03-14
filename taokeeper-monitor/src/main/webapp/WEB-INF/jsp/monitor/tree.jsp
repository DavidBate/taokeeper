<%@ page language="java" pageEncoding="UTF-8"%>
<script type="text/javascript"
	src="js/jquery.js" >"></script>
<script type="text/javascript"
	src="js/jquery.tree.js"></script>
<script type="text/javascript" language="javascript">
  <!--
        $(function () {
            $.ajaxSetup({cache:false});
            $("#zkTree").tree({
                  data : {
                    type : "json",
                    async : true,
                    opts : {
                        method : "get",
                        url : "children.do?clusterId=${clusterId}"
                    }
                 },//end data
                 ui:{
                     theme_name : "default"
                 },
                 lang:{
                 loading : "loading......"
             },
                 types :{
                    "default" : {
                        draggable : false
                    }
                 },
             callback : {
                 beforedata : function(node,tree_obj){
                        var rootPath= '${path}';
                        $.ajax({
                        	type: 'get',
                        	url: 'content.do?clusterId=${clusterId}&path=${path}',
                        	success : function (result){
                        		$("#content").html(result);
                        	}
                         });
                        return {path :  $(node).attr("path") || rootPath,rel : $(node).attr("rel")};
                },
                onselect : function(node,tree_obj){
                         var test = $(node).children("a").attr("href");
                         $.ajax({
                        	type: 'get',
                        	url: test,
                        	success : function (result){
                        		$("#content").html(result);
                        	}
                         });
                },
                onsearch : function(node, tree_obj) {
                    tree_obj.container.find(".search").removeClass("search");
                    node.addClass("search");
                }
            } //end callback
            });
        });
         function searchnodes(){
            var searchPath=$('#search_path').val();
            window.location.href='tree.do?clusterId=${clusterId}&path='+searchPath;
            $(parent.document.body).find('#content').attr('src', "/node-zk/get?path="+searchPath);
         }
    //-->
</script>
<div id="container">
	<h2>
		Node-ZK-Browser
	</h2>
	<div>
		<a href="create.do?clusterId=${clusterId }" target="content">create path</a>
	</div>
	<div>
		<input type="text" id="search_path" value="${path}"/> 
		<input type="button"
			id="search_op" onclick="searchnodes()" value="Search" />
	</div>
	<div id="zkTree" style="width:30%;float:left;overflow:hidden"></div>
	<div id="content" style="width:50%;position: fixed;right:5%;top: 13%"></div>
</div>
