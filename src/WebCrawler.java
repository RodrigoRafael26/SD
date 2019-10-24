import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class WebCrawler extends Thread{
    public Storage st;

    public WebCrawler(Storage st){
        this.st = st;
        this.start();
    }


    public void run(){

        this.indexLinks();
    }


    //A função indexLinks vai ser iterativa
    private void indexLinks() {

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


                    st.addReferenceToHash(ws, s);

                    //Add links to a queue
                    st.addLinkToQueue(s);


                    // Get website text
                    String text = doc.text();
                    indexWords(text, ws);

                }




            } catch (UnsupportedMimeTypeException e){

                //remove link from reference index (vai criar problemas com a partilha de urls)
                indexLinks();
            } catch (HttpStatusException e){
                //remove link from reference index (vai criar problemas com a partilha de urls)
                System.out.println("404");
                indexLinks();
            } catch (IOException e) {
                indexLinks();
                e.printStackTrace();
            }
            indexLinks();
        System.out.println(st.getLinkList().size());
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
