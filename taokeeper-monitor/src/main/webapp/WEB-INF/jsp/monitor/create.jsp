<%@ page language="java" pageEncoding="UTF-8"%>
<form action="create.do" method="post">
	<table>
		<tr>
			<td>Path</td>
			<td><input type='text' name="path"  style="width:50%"></td>
		</tr>
		<tr>
			<td>Data</td>
			<td><input type='text' name="data" style="width:50%"></td>
		</tr>
		<tr>
			<td>Type</td>
			<td><input type='radio' name='persistent' value="true"
				checked="true" />PERSISTENT&nbsp;<input type='radio' name='persistent'
				value="false" /> EPHEMERAL&nbsp; </br>
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type='submit' value="Create" /></td>
		</tr>
		<table>
</form>