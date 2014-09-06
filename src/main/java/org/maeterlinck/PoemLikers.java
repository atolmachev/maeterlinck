package org.maeterlinck;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Hello world!
 */
public class PoemLikers {
    static int i = 0;

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("http://slova.org.ru/").get();
            Elements authors = doc.select("div#letter_box > a");
            ArrayList<PoemInfo> poemInfos = new ArrayList<PoemInfo>();
            for (Element eAuthor : authors) {
                String href = eAuthor.attr("href");
                String author = eAuthor.ownText();
                poemInfos.addAll(getPoemInfos(author, href));
            }
            System.out.printf("Total poems: " + poemInfos.size());

            //printTop(poemInfos);
            writeAsDataset(poemInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeAsDataset(ArrayList<PoemInfo> poemInfos) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("poem_likers"), "UTF-8"));
        try {
            for (PoemInfo poemInfo : poemInfos) {
                writer.write(poemInfo.toString());
            }
        } catch (IOException e) {
            writer.close();
        }
    }

    private static void printTop(ArrayList<PoemInfo> poemInfos) {
        Collections.sort(poemInfos, new Comparator<PoemInfo>() {
            @Override
            public int compare(PoemInfo poemInfo, PoemInfo poemInfo2) {
                return new Integer(poemInfo2.likes).compareTo(poemInfo.likes);
            }
        });
        for (int j = 0; j < 10; j++) {
            PoemInfo poemInfo = poemInfos.get(j);
            System.out.printf("%d. (%d likes) %s, %s %s\n", j, poemInfo.likes, poemInfo.author, poemInfo.name, poemInfo.url);
        }
    }

    private static List<PoemInfo> getPoemInfos(String author, String authorUrl) throws Exception {
        Document doc = Jsoup.connect("http://slova.org.ru/" + authorUrl).get();
        Elements ePoems = doc.select("div#stihi_list > a");
        ArrayList<PoemInfo> poemInfos = new ArrayList<PoemInfo>();
        for (Element ePoem : ePoems) {
            String href = ePoem.attr("href");
            String poemName = ePoem.ownText();
            poemInfos.add(getPoemInfo(poemName, href, author));
        }
        return poemInfos;
    }

    private static PoemInfo getPoemInfo(String poemName, String poemUrl, String author) throws Exception {
        i++;
        if (i % 100 == 0) System.out.printf("Parsed %d poems\n", i);
        Element doc = null;
        try {
            doc = Jsoup.connect("https://api.vk.com/method/likes.getList?type=sitepage&owner_id=1&item_id=1" +
                    "&filter=likes&offset=1&count=10000&extended=1&page_url=" + poemUrl).ignoreContentType(true).get().body();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 503) {
                System.out.printf("Sleeping 1s, i=%d\n", i);
                Thread.sleep(1000);
                return getPoemInfo(poemName, poemUrl, author);
            }
        }
        final JSONObject obj = new JSONObject(doc.text());
        final JSONObject resp = obj.getJSONObject("response");
        final int likes = resp.getInt("count");
        ArrayList<Long> likerUids = new ArrayList<Long>();
        if (likes > 0) {
            JSONArray items = resp.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                likerUids.add(item.getLong("uid"));
            }
        }
        return new PoemInfo(poemName, "http://slova.org.ru"+poemUrl, author, likes, likerUids.toArray(new Long[likerUids.size()]));
    }
}
