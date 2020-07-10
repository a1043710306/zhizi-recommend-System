<%HeaderTemplate%><%/HeaderTemplate%>
<%DirectoryTemplate%><%/DirectoryTemplate%>
<p>
<table>
	<tr bgcolor="FAC08C">
			<td align="center">渠道</td>
			<td align="center">过滤条件</td>
			<td align="center">时效性（高/中/低）</td>
			<td align="center">质量（高/中/低）</td>
			<td align="center">三俗评分（0-5）</td>
			<td align="center">广告评分（0-5）</td>
			<td align="center">受众面（0-5）</td>
			<td align="center">公众号数量</td>
			<td align="center">有发布资讯公众号数量</td>
			<td align="center">资讯量</td>
			<td align="center">未自动发佈量</td>
			<td align="center">过滤量</td>
			<td align="center">发佈量</td>
			<td align="center">备注</td>
	</tr>
	<%ContentTemplate%>
	<tr>
			<td align="center"><%channel%></td>
			<td align="center"><%name%></td>
			<td align="center"><%effectiveness%></td>
			<td align="center"><%quality%></td>
			<td align="center"><%adult_score%></td>
			<td align="center"><%advertisement_score%></td>
			<td align="center"><%audience_scale%></td>
			<td align="right"><%totalBizCount%></td>
			<td align="right"><%activeBizCount%></td>
			<td align="right"><%totalArticleCount%></td>
			<td align="right"><%nonPublishArticleCount%></td>
			<td align="right"><%filteredArticleCount%></td>
			<td align="right"><%publishArticleCount%></td>
			<td><%description%></td>
	</tr>
	<%/ContentTemplate%>
</table>
