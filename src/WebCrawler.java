import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.security.validator.ValidatorException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebCrawler extends Thread{
    //public HashMap<String, HashSet<String>> searchIndex;
    //public HashMap<String, HashSet<String>> referenceIndex;
    public Storage st;
    private int numURL;
    private ArrayList<String> indexedUrls;


    public WebCrawler(Storage st){
        this.numURL = 0;
        this.st = st;
        this.start();
    }


    public void run(){

        this.indexLinks();

    }


    //A função indexLinks vai ser iterativa
    private void indexLinks() {

        while(this.numURL <3000){

            try {
                String ws = st.getLink();
                if (!ws.startsWith("http://") && !ws.startsWith("https://"))
                    ws = "http://".concat(ws);

                Document doc = Jsoup.connect(ws).get();
                // Title
                System.out.println(doc.title() + "\n");

                // Get all links
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    // Ignore bookmarks within the page

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

                    st.addReferenceToHash(ws, s);

                    //Add links to a queue
                    st.addLinkToQueue(s);

                    System.out.println(s + " is indexed " + numURL);

                    // Get website text
                    String text = doc.text(); // We can use doc.body().text() if we only want to get text from <body></body>
                    indexWords(text, ws);
                    this.numURL++;

                }




            } catch (UnsupportedMimeTypeException e){
                continue;
            } catch (HttpStatusException e){
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        st.linkList.clear();
        this.numURL = 0;
        System.out.println(st.getReferenceHash());
        System.out.println(st.getSearchHash());
    }


    private void indexWords(String text, String ws){
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

                st.addWordToHash(word, ws);


            }
        }
    }


}
