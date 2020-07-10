<html>
<meta charset="UTF-8" />
<style type="text/css">
table, th, td {
   border: 1px solid black;
}
</style>
<%HeaderTemplate%><%/HeaderTemplate%>
<%DirectoryTemplate%><%/DirectoryTemplate%>
统计时间：<%statisticTime%>
<br/>
<table>
	<tr bgcolor="FAC08C">
		<td colspan="2" rowspan="2" align="center">&nbsp;<%fetchDate%></td>
		<td colspan="3" align="center">t_content</td>
		<td colspan="4" align="center">s_channel_content_audit</td>
	</tr>
	<tr>
		<td align="center">公众号数量</td>
		<td align="center">有发布资讯公众号数量</td>
		<td align="center">资讯量</td>
		<td align="center">公众号数量</td>
		<td align="center">有发布资讯公众号数量</td>
		<td align="center">过滤量</td>
		<td align="center">资讯量</td>
	</tr>
	<tr>
		<td colspan="2">原始爬取量</td>
		<td align="right">&nbsp;<%crawler.t_content.totalBizCount%></td>
		<td align="right">&nbsp;<%crawler.t_content.activeBizCount%></td>
		<td align="right">&nbsp;<%crawler.t_content.totalArticleCount%></td>
		<td align="right">&nbsp;<%crawler.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%crawler.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%crawler.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%crawler.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td colspan="2">运营筛选后的量*</td>
		<td colspan="3" align="center">N/A</td>
		<td align="right">19,363</td>
		<td align="right">&nbsp;<%filter.operation.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.operation.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.operation.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td colspan="2">入库量(Gap原因清洗程序过滤)</td>
		<td align="right">&nbsp;<%filter.cleaner.t_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.t_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.t_content.totalArticleCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.cleaner.s_content.totalArticleCount%></td>
	</tr>
	<%ContentTemplate%>
	<tr>
		<td rowspan="7">&nbsp;<%filter.name%></td>
		<td>渠道过滤条件过滤</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%filter.channel.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.channel.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.channel.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.channel.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td>未自动发佈</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%filter.manualrelease.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.manualrelease.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.manualrelease.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.manualrelease.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td>配置公众号并开自动发佈</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%filter.autorelease.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.autorelease.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.autorelease.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.autorelease.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td>原运营平台过滤</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%filter.platform.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%filter.platform.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%filter.platform.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%filter.platform.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td>发佈量</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%publish.s_content.totalBizCount%></td>
		<td align="right">&nbsp;<%publish.s_content.activeBizCount%></td>
		<td align="right">&nbsp;<%publish.s_content.filterArticleCount%></td>
		<td align="right">&nbsp;<%publish.s_content.totalArticleCount%></td>
	</tr>
	<tr>
		<td>推送失败</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%failed.delivery.totalBizCount%></td>
		<td align="right">&nbsp;<%failed.delivery.activeBizCount%></td>
		<td align="right">&nbsp;<%failed.delivery.totalArticleCount%></td>
		<td align="right">&nbsp;<%failed.delivery.totalArticleCount%></td>
	</tr>
	<tr>
		<td>有展示资讯量</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;</td>
		<td align="right">&nbsp;<%success.delivery.totalBizCount%></td>
		<td align="right">&nbsp;<%success.delivery.activeBizCount%></td>
		<td align="right">&nbsp;<%success.delivery.totalArticleCount%></td>
		<td align="right">&nbsp;<%success.delivery.totalArticleCount%></td>
	</tr>
	<%/ContentTemplate%>
</table>

<p>附注:
<li>截至5/25, 运营筛选量 19,942 无法抓取 579</li>
</html>
