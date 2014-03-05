<%@ page language="java" pageEncoding="UTF-8"%><%@ include
	file="/WEB-INF/common/taglibs.jsp"%>
<script type="text/javascript">
var editing=false;
	function edit() {
		if(!editing){
			var value = $("#data").val();
			$("#data_container").html(
				"<form id='edit_form' action='edit.do'>"
						+ "<input type='hidden' name='path' value='${path}'/>"
						+ "<input type='hidden' name='clusterId' value='${clusterId}'/>"
						+ "<input type='hidden' name='version' value='${node.stat.version}'/>"
						+ "<table><tr><td colspan='2'><textarea cols='90' rows='10' name='data' id='new_data'>"
						+ value
						+ "</textarea></td></tr>"
						+ "<tr><td colspan='2'><input type='submit' value='Save' id='save_data'/></td></tr>"
						+ "</table></form>");
			editing = true;
		}
		
	}
</script>
<c:if test="${empty login }">
	<form action="login.do">
		username: <input name="username" value=""><br> password:
		<input name="password" value="" type="password"> <input
			type="submit" value="login">
	</form>
</c:if>
<c:if test="${!empty login }">
	<h2>Welcome admin!</h2>
</c:if>
<br>
<br>
<div>
	path:<strong>${path} </strong> &nbsp;&nbsp;
	<c:if test="${!empty login }">
		<a href="delete.do?clusterId=${clusterId}&path=${path}"
			onclick="return confirm('delete path : ${path}  ?')"
			style="text-decoration: none">delete</a>
	</c:if>
</div>
<div>stat:</div>
<div>
	<table border="1">
		<tr>
			<td>czxid</td>
			<td>${node.stat.czxid }</td>
		</tr>

		<tr>
			<td>mzxid</td>
			<td>${node.stat.mzxid }</td>
		</tr>

		<tr>
			<td>pzxid</td>
			<td>${node.stat.pzxid }</td>
		</tr>

		<tr>
			<td>dataLength</td>
			<td>${node.stat.dataLength }</td>
		</tr>

		<tr>
			<td>numChildren</td>
			<td>${node.stat.numChildren }</td>
		</tr>

		<tr>
			<td>version</td>
			<td>${node.stat.version }</td>
		</tr>

		<tr>
			<td>cversion</td>
			<td>${node.stat.cversion }</td>
		</tr>

		<tr>
			<td>aversion</td>
			<td>${node.stat.aversion }</td>
		</tr>

		<tr>
			<td>ctime</td>
			<td>${node.stat.ctime }</td>
		</tr>

		<tr>
			<td>mtime</td>
			<td>${node.stat.mtime }</td>
		</tr>

		<tr>
			<td>ephemeralOwner</td>
			<td>${node.stat.ephemeralOwner }</td>
		</tr>

	</table>
</div>
</br>
</br>
<div>data:</div>
<input type='hidden' value="${node.data }" id='data' />
<c:if test="${empty login }">
	<div id='data_container'>${node.data }</div>
</c:if>
<c:if test="${!empty login }">
	<div id='data_container' onclick='edit();'>${node.data }</div>
</c:if>