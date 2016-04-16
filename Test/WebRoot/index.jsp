<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<!DOCTYPE html5>
<head>
<title>My JSP 'index.jsp' starting page</title>
<meta http-equiv="pragma" content="no-cache">
<script type="text/javascript" src="./ui/jquery-1.8.3.min.js"></script>
<script type="text/javascript">
	function test(){
		$.ajax({
			url:"http://ahwap.musicd.cc/musicanywhere-api/api/log/queryOrderLog",
			method:"post",
			data:encodeURI({"portalNo":"9001","progNo":"10000028","pageIndex":2,"pageSize":20,"goodsId":"10000055","resultCode":"00000000","uid":"a168977f-294c-4092-9c4d-0d308b39","sort":"createTime","order":"desc","beginTime":"2016-02-11 00:00:00","endTime":"2016-02-11 23:59:59"}),
			dataType:"json",
			success:function(res){
				console.log("1111111111111111111");
				console.log(res);
			}
		});
	
	}
</script>
</head>

<body>
	<form action="http://ahwap.musicd.cc/musicanywhere-api/api/log/queryOrderLog" method="post">
		<input name="portalNo" value="9001">
		<input name="progNo" value="10000028">
		<input name="pageIndex" value="2">
		<input name="pageSize" value="20">
		<input name="goodsId" value="10000055">
		<input name="resultCode" value="00000000">
		<input name="uid" value="a168977f-294c-4092-9c4d-0d308b39">
		<input name="sort" value="createTime">
		<input name="order" value="desc">
		<input name="beginTime" value="2016-02-11 00:00:00">
		<input name="endTime" value="2016-02-11 23:59:59">
		<br>
		<input type="button" value="点我" onclick="test()">
	</form>	
</body>
</html>
