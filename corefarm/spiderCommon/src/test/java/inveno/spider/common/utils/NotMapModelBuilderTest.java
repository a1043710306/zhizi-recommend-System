package inveno.spider.common.utils;

import java.lang.reflect.Modifier;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import inveno.spider.common.model.Article;

public class NotMapModelBuilderTest {
	
	GsonBuilder gsonBuilder = null;
	Gson gson = null;
	
	@Before
	public void init() {
		System.out.println("before");
		gsonBuilder = new GsonBuilder();

        gson = gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss")
                .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .create();
	}

	@Test
	public void testUnicode() {
		String testStr = "{\"publisherPagerankScore\": 0, \"commentCount\": 0, \"hasCopyright\": 0, \"bodyImagesCount\": 1, \"author\": \"\", \"isOpenComment\": 0, \"crawlerType\": \"webant\", \"listImages\": [{\"height\": 360, \"src\": \"http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg\", \"format\": \"jpeg\", \"width\": 600, \"desc\": \"\"}], \"content\": \"<p><img src='http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg' /></p><p>लंदन: तुर्की की लंबी दूरी की धाविका एल्वान एबेलेगेसी और गाब्जे बुलुत को डोपिंग में दोषी पाया गया है जिसके बाद अब उनसे 2008 और 2012 ओलिंपिक खेलों में जीते गये पदकों को वापिस लिया जाएगा। डोपिंग में दोषी पाई गई इन एथलीटों को ओलंपिक पदकों के अलावा वर्ष 2007 में ओसाका एथलेटिक्स विश्व चैंपियनशिप में जीते गये पदकों को भी अब वापिस लौटाना होगा। दोनों महिला एथलीट आईएएएफ के समक्ष पेश हुई थीं जिसके बाद उन्हें दंडित किया गया।</p><p>इथोपिया मूल की एबेलेगेसी के वर्ष 2007 में लिए गए नमूनों की पुन: जांच के बाद उन्हें डोपिंग नियमों के उल्लंघन का दोषी पाया गया है और अगस्त 26, 2007 से अगस्त 25, 2009 तक उनके सभी परिणामों को रद्द कर दिया गया है। ऐसे में अब उन्हें 2008 बीजिंग ओलंपिक में 5000 और 10000 मीटर रेस में मिले रजत पदक और 2007 विश्व चैंपियनशिप में मिले रजत को गंवाना होगा।</p><p>एबेलेगेसी और बुलुत से पदक वापिस लेने के बाद अब यह पदक ब्रिटेन की जो पावेय को कांस्य और अमेरिका की कारा गाउचर को रजत पदक दिया जायेगा। पावेय का यह पहला विश्व पदक होगा। पावेय ने कहा कि यह खबर सुनकर मैं खुश हूं लेकिन यह एक तरह का कष्ट पहुंचाने वाली बात है। इसके अलावा इथोपिया मूल की बहरीन की मरियम यूसुफ जमाल मरियम को स्वर्ण पदक प्रदान किया जाएगा।</p>\", \"type\": 0, \"fetchTime\": \"2017-03-31 10:22:24\", \"fallImage\": \"http://cloudimg.hotoday.in/v1/icon?id=17505034622774894696&size=210*210&fmt=.jpeg\", \"sourceType\": \"app\", \"imgFlag\": 1, \"discoveryTime\": \"2017-03-31 10:22:24\", \"sourceCommentCount\": 0, \"link\": \"http://www.punjabkesari.in/sports/news/elvan-abeylegesse-599535/test1\", \"sourceFeedsUrl\": \"http://n.m.ksmobile.net/news/fresh?count=10&mnc=260&regionid=-1&nmcc=460&mode=2&user_city=&display=0xE1f8f&ch=200000&ch_preinstall=unknown&pf=android&newuser=1&lan=hi_IN&v=5&nmnc=00&osv=4.2.2&act=1&action=0x72ef&server_city=&offset=&preload=0&ctype=0x11a67&model=vivo+X1St&scenario_param=49&mcc=310&scenario=0x00310101&brand=vivo&declared_lan=hi_IN&net=wifi&pid=14&appv=6.5.6&channelid=179166&aid=29fa6938b2b8f89a\", \"categories\": {}, \"bodyImages\": [{\"height\": 360, \"src\": \"http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg\", \"format\": \"jpeg\", \"width\": 600, \"desc\": \"\"}], \"publisher\": \"Punjab Kesari\", \"title\": \"तुर्की की धाविकओं से छिनेगा ओलिंपिक पदक\", \"publishTime\": \"2017-03-30 20:34:19\", \"summary\": \"तुर्की की लंबी दूरी की धाविका एल्वान एबेलेगेसी और गाब्जे बुलुत को डोपिंग में दोषी पाया गया है जिसके बाद अब उनसे 2008 और 2012 ओलिंपिक खेलों...\", \"listImagesCount\": 0}";
//		String testStr = "{\"publisherPagerankScore\": 0, \"commentCount\": 0, \"hasCopyright\": 0, \"bodyImagesCount\": 1, \"author\": \"\", \"isOpenComment\": 0, \"crawlerType\": \"webant\", \"listImages\": [{\"height\": 360, \"src\": \"http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg\", \"format\": \"jpeg\", \"width\": 600, \"desc\": \"\"}], \"content\": \"<p><img src='http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg' /></p><p>लंदन: तुर्की की लंबी दूरी की धाविका एल्वान एबेलेगेसी और गाब्जे बुलुत को डोपिंग में दोषी पाया गया है जिसके बाद अब उनसे 2008 और 2012 ओलिंपिक खेलों में जीते गये पदकों को वापिस लिया जाएगा। डोपिंग में दोषी पाई गई इन एथलीटों को ओलंपिक पदकों के अलावा वर्ष 2007 में ओसाका एथलेटिक्स विश्व चैंपियनशिप में जीते गये पदकों को भी अब वापिस लौटाना होगा। दोनों महिला एथलीट आईएएएफ के समक्ष पेश हुई थीं जिसके बाद उन्हें दंडित किया गया।</p><p>इथोपिया मूल की एबेलेगेसी के वर्ष 2007 में लिए गए नमूनों की पुन: जांच के बाद उन्हें डोपिंग नियमों के उल्लंघन का दोषी पाया गया है और अगस्त 26, 2007 से अगस्त 25, 2009 तक उनके सभी परिणामों को रद्द कर दिया गया है। ऐसे में अब उन्हें 2008 बीजिंग ओलंपिक में 5000 और 10000 मीटर रेस में मिले रजत पदक और 2007 विश्व चैंपियनशिप में मिले रजत को गंवाना होगा।</p><p>एबेलेगेसी और बुलुत से पदक वापिस लेने के बाद अब यह पदक ब्रिटेन की जो पावेय को कांस्य और अमेरिका की कारा गाउचर को रजत पदक दिया जायेगा। पावेय का यह पहला विश्व पदक होगा। पावेय ने कहा कि यह खबर सुनकर मैं खुश हूं लेकिन यह एक तरह का कष्ट पहुंचाने वाली बात है। इसके अलावा इथोपिया मूल की बहरीन की मरियम यूसुफ जमाल मरियम को स्वर्ण पदक प्रदान किया जाएगा।</p>\"}";
//		String testStr = "{\"publisherPagerankScore\": 0, \"commentCount\": 0, \"hasCopyright\": 0, \"bodyImagesCount\": 1, \"author\": \"\", \"isOpenComment\": 0, \"crawlerType\": \"webant\", \"listImages\": [{\"height\": 360, \"src\": \"http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg\", \"format\": \"jpeg\", \"width\": 600, \"desc\": \"\"}]}";
//		String testStr = "{\"listImages\": [{\"height\": 360, \"src\": \"http://cloudimg.hotoday.in/v1/icon?id=9421487877794417572&size=600*360&fmt=.jpeg\", \"format\": \"jpeg\", \"width\": 600, \"desc\": \"\"}]}";
		System.out.println(testStr);
		Article model = gson.fromJson(testStr, Article.class);
		System.out.println(model.toString());
	}
	
}
