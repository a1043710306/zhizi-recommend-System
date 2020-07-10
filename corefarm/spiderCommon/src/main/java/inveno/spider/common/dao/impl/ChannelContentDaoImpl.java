package inveno.spider.common.dao.impl;

import inveno.spider.common.SessionOtherFactory;
import inveno.spider.common.dao.ChannelContentDao;
import inveno.spider.common.model.ChannelContent;

public class ChannelContentDaoImpl implements ChannelContentDao
{
    @Override
	public ChannelContent save(ChannelContent channelContent) {
		final String sqlId="inveno.spider.common.mapper.ChannelContentMapper.insert";
		SessionOtherFactory.getInstance().insert(sqlId, channelContent);
		return channelContent;
	}
}
