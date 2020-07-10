package inveno.spider.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import inveno.spider.common.RabbitmqHelper;
import inveno.spider.parser.base.Html;
import inveno.spider.parser.base.Page;
import inveno.spider.parser.extractor.Extractor.Type;
import inveno.spider.parser.model.Profile;
import inveno.spider.parser.processor.CrawlWorker;
import inveno.spider.parser.processor.CrawlWorkerImpl;
import inveno.spider.rmq.model.QueuePrefix;

public class BatchParser
{

    public static void main(String[] args)
    {
            //Profile profile=Profile.readFromFile(profileName);
        Page page = RabbitmqHelper.getInstance().getMessageNonblocking(QueuePrefix.SYS_HTMLSOURCE_QUEUE.name(),Page.class);//null;//
        //page = new Page(0,"http://finance.chinanews.com/life/2014/06-20/6303845.shtml",Type.Content,1,"http://finance.chinanews.com/life/gd.shtml");

//        StringBuilder sb = new StringBuilder();
//        try
//        {
//            FileInputStream fis = new FileInputStream(new File("c:/html.txt"));
//            byte[] buffer=new byte[2048];
//            while(fis.read(buffer)>-1)
//            {
//                sb.append(new String(buffer,"utf-8"));
//            }
//            fis.close();
//            System.out.println(sb.toString());
//        } catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        Html html = new Html(sb.toString());
//        page.setHtml(html);
        Profile profile = Profile.readFromCache(page.getProfileName());//Profile.readFromFile("");//
        if(null==page || null==profile)
        {
            System.out.println("Profile or page is empty.");
            return;
        }

        CrawlWorker worker = null;
        if ("news".equalsIgnoreCase(profile.getType()) ||
                "blog".equalsIgnoreCase(profile.getType()) )
            worker = new CrawlWorkerImpl(profile,page);
        
        if(null==worker)
        {
            System.out.println("Unknow media type.");
            return;
        }

        final CrawlWorker w = worker;
        final Thread t = new Thread() {
            @Override
            public void run() {
                w.execute();
                RabbitmqHelper.getInstance().close();
            }
        };
        t.start();

    }

}
