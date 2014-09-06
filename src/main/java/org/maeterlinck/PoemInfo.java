package org.maeterlinck;

import com.google.common.base.Joiner;

/**
 * Created by IntelliJ IDEA.
 * User: andrey
 * Date: 2/09/14, 11:55 PM
 */
public class PoemInfo {
    final String name;
    final String url;
    final String author;
    final int likes;
    final Long[] likerUids;

    public PoemInfo(String name, String url, String author, int likes, Long[] likerUids) {
        this.name = name;
        this.url = url;
        this.author = author;
        this.likes = likes;
        this.likerUids = likerUids;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%d,%s\n", name, url, author, likes, Joiner.on(";").join(likerUids));
    }
}
