package inveno.spider.common.dao.impl;

import org.junit.Test;

import inveno.spider.common.dao.ContentDao;
import inveno.spider.common.dao.test.LinkPublisherDao;
import inveno.spider.common.model.Content;
import inveno.spider.common.model.test.LinkPublisher;

public class SplitTableContentTest {

	@Test
	public void testLinkPublisherInsert() {
		LinkPublisherDao linkPublisherDao = new LinkPublisherDaoImplTest();
		LinkPublisher linkPublisher = new LinkPublisher();
		linkPublisher.setLink("http://www.womenshealthlatam.com/amor-y-sexo/17/04/21/razones-autocomplacerte/5");
		linkPublisher.setPublisher("Womens' Health");
		linkPublisher.setSourceFeedsUrl("http://www.womenshealthlatam.com/amor-y-sexo/");
		linkPublisher.setTableName("t_link_publisher");
		linkPublisherDao.insert(linkPublisher);
	}

	@Test
	public void testInsert() {
		ContentDao contentDao = new SplitTableContentDaoImpl();
		Content content = new Content();
		content.setContentId("423456789");
		content.setSource("Womens' Health");
		content.setLink("http://www.womenshealthl'atam.com/amor-y-sexo/3");
		content.setPublisher("Womens' Health");
		content.setSourceType("web");
		content.setAuthor("");
		content.setLanguage("");
		content.setCountry("");
		content.setSourceItemId("");
		content.setHasCopyright(1);
		content.setBodyImagesCount(1);
		content.setPublisherPagerankScore(1.1);
		content.setListImagesCount(1);
		content.setSourceCommentCount(1);
		content.setCpVersion("");
		content.setTitle("tit'le");
		content.setContent("sadkljaskdjasl'sakdljlsakjdlsj");
		content.setType("1");
		contentDao.insert2CurrentMonth(content, "t_content_2017_05");
	}

}
