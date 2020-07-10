package inveno.spider.rmq.model;
/**
 * 枚举
 * @author yanxiaobo
 *
 */
public enum QueuePrefix
{
    SPIDER("normal_"),
    SYS_HTMLSOURCE_QUEUE("sys_htmlsource_queue"),
    SYS_MESSAGE_QUEUE("sys_message_queue"),
    SYS_JS_PAGE_QUEUE("sys_js_page_queue"),
    SYS_NEW_MESSAGE_QUEUE("sys_new_message_queue");
    
    
    private String value;
    private QueuePrefix(String value)
    {
        this.value = value;
    }
    
    public String getValue()
    {
        return this.value;
    }
}
