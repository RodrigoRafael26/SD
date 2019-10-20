import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class WebCrawler extends Thread{
    public HashMap<String, HashSet<String>> searchIndex;
    public HashMap<String, HashSet<String>> referenceIndex;
    private ArrayBlockingQueue<String> linkQueue;
    private String ws;
    private int recursionLevel;
    private ArrayList<String> indexedUrls;
    //define max threads!!!

    public WebCrawler(String ws, HashMap<String, HashSet<String>> searchIndex, HashMap<String, HashSet<String>> referenceIndex){
        this.ws = ws;
        this.searchIndex = searchIndex;
        this.referenceIndex = referenceIndex;
        this.recursionLevel = 0;
        this.indexedUrls = new ArrayList<>();
        this.linkQueue = new ArrayBlockingQueue<String>(30, true);
        this.start();
    }

    public void run(){

        indexLinks(ws);

    }


    //A função indexLinks vai ser recursiva
    private void indexLinks(String ws) {

        try {

            if (!ws.startsWith("http://") && !ws.startsWith("https://"))
                ws = "http://".concat(ws);

            Document doc = Jsoup.connect(ws).get();
            // Title
            System.out.println(doc.title() + "\n");

            // Get all links
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                // Ignore bookmarks within the page

                //System.out.println(s);
                if (link.attr("href").startsWith("#")) {
                    continue;
                }

                // Shall we ignore local links? Otherwise we have to rebuild them for future parsing
                if (!link.attr("href").startsWith("http")) {
                    continue;
                }

                //index links
                String s = link.attr("href");
                //System.out.println(s);

                if (referenceIndex.get(s) != null) {
                    referenceIndex.get(s).add(ws);
                }else {
                    referenceIndex.put(s, new HashSet<String>());
                    referenceIndex.get(s).add(ws);
                }

                //Add links to a queue

                //create other thread to go through that queue (recursividade)
                this.recursionLevel++;
                if(recursionLevel <= 5){
                    if(!indexedUrls.contains(s)) {
                        System.out.println(s + " " + this.recursionLevel);
                        indexedUrls.add(s);
                        indexLinks(s);

                    }
                    String text = doc.text(); // We can use doc.body().text() if we only want to get text from <body></body>
                    indexWords(text, searchIndex, ws);
                    this.recursionLevel--;
                }

            }

            // Get website text

        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void indexWords(String text,  HashMap<String,HashSet<String>> searchIndex, String ws){
        Map<String, Integer> countMap = new TreeMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
        String line;

        // Get words and respective count
        while (true) {
            try {
                if ((line = reader.readLine()) == null)
                    break;
                String[] words = line.split("[ ,;:.?!“”(){}\\[\\]<>']+");
                for (String word : words) {
                    word = word.toLowerCase();
                    if ("".equals(word)) {
                        continue;
                    }
                    if (!countMap.containsKey(word)) {
                        countMap.put(word, 1);
                    }
                    else {
                        countMap.put(word, countMap.get(word) + 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Close reader
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display words and counts
        for (String word : countMap.keySet()) {
            if (word.length() >= 3) { // Shall we ignore small words?
                //System.out.println(word + "\t" + countMap.get(word));
                if(searchIndex.get(word)!=null){
                    searchIndex.get(word).add(ws);

                }else{
                    searchIndex.put(word, new HashSet<String>());
                    searchIndex.get(word).add(ws);
                }


            }
        }
    }


}
