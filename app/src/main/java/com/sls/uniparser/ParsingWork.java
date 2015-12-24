package com.sls.uniparser;


import android.support.annotation.UiThread;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingWork {

    //TODO:Modify REG_EX_URL_DOT to remove <href=">

    final int LEN_HREF = 6;
    final String TAG_LINKS = "found_links";
    final String TAG_EMAILS = "found_emails";
    private CustomUrl mURL;
    private final int BUFFER_SIZE = 100000;     //Buffer for parts of site
    private Pattern PATTERN_URL_SUB;
    private Pattern PATTERN_EMAIL;
    private LinkedHashSet <CustomUrl> foundLinks;

    public static final String REG_EX_EMAIL = "^*([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$*";
    public static final String REG_EX_URL_SUB = "href=\"^?([a-z0-9_@?^=%&/~+#-]S*)*$*";


    public ParsingWork(CustomUrl url)
    {
        mURL = new CustomUrl(url.getHome(),url.getPath());
        PATTERN_URL_SUB = Pattern.compile(REG_EX_URL_SUB);
        PATTERN_EMAIL = Pattern.compile(REG_EX_EMAIL);
        foundLinks = new LinkedHashSet<>();
    }

    public LinkedHashSet<CustomUrl> goAhead(){
        BufferedReader buffer = null;
         try {
        StringBuilder mStringBuffer = new StringBuilder();
        if((buffer = getBufferFromUrl(mURL.toString()))==null) { //Cannot connect to links
            return null;// return empty list
        }
             char [] nextPart;
        while (true) {                  //Parsing html on several parts
             nextPart= new char [BUFFER_SIZE];
                if (buffer.read(nextPart)== -1)
                    break;
                mStringBuffer.append(nextPart);
                checkStringByPatterns(mStringBuffer.toString());

        }
            nextPart = null;
             buffer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
             if (buffer != null)
                 try {
                     buffer.close();
                     System.gc();
                 } catch (IOException ignored) {}
         }


        return foundLinks;
    }

    private void checkStringByPatterns(String s)
    {

        Matcher matcher_dot = PATTERN_URL_SUB.matcher(s);     //find subsidiaries links
        while (matcher_dot.find())
        {
            String found = s.substring((matcher_dot.start()+LEN_HREF),matcher_dot.end());
            String tmp = mURL.getHome()+found;

            if(tmp.contains(mURL.toString())&& !tmp.equals(mURL.toString()))    //проверяем является ли ссылка дочерней относительно исходной
                if(foundLinks.add(new CustomUrl(mURL.getHome(),found)))
                    ;  //  Log.d(TAG_LINKS, mURL.getHome()+found)

        }

        matcher_dot = PATTERN_EMAIL.matcher(s);             //find emails

        while(matcher_dot.find())        {
           if (addToList(s.substring(matcher_dot.start(), matcher_dot.end())))
                ;// Log.d(TAG_EMAILS, s.substring(matcher_dot.start(), matcher_dot.end()));
        }
    }

    @UiThread
    private boolean addToList(String s)    {     return MainActivity.mSynchronSet.add(s);    }

    private BufferedReader getBufferFromUrl(String link) {

        URL url;
        HttpURLConnection httpcon;
        InputStreamReader ISR = null;
        try {
            url = new URL(link);
            httpcon = (HttpURLConnection)url.openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");       //connect to site like browser
            ISR = new InputStreamReader(httpcon.getInputStream(), "utf-8");

        }
        catch (FileNotFoundException e)
        {            return null;        }

        catch (IOException e) {
            e.printStackTrace();
            return null;
        }



        return new BufferedReader(ISR);
    }

}
