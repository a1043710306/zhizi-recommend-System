<%HeaderTemplate%><%/HeaderTemplate%>
<%DirectoryTemplate%><%/DirectoryTemplate%>
<p><%filter.name%> 运营平台过滤統計
<table>
	<tr bgcolor="FAC08C">
			<td align="center">过滤序号</td>
			<td align="center">描述</td>
			<td align="center">數量</td>
	</tr>
	<%ContentTemplate%>
	<tr>
			<td><%code%></td>
			<td><%type_name%></td>
			<td align="right"><%filtered_count%></td>
	</tr>
	<%/ContentTemplate%>
</table>
