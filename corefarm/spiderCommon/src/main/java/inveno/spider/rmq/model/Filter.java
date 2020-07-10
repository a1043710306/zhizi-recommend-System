package inveno.spider.rmq.model;

public interface Filter<T,E> {
    boolean isMatched(T object, E prefixText);
    boolean isMatched(T object, E... prefixText);
}