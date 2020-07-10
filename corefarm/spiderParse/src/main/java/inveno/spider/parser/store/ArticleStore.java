package inveno.spider.parser.store;

public interface ArticleStore {

    public boolean contains(String url);

    public void save(Article article);

    public void close();
}
