package com.sls.uniparser;

import android.content.Intent;
import android.support.annotation.UiThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Serg on 09.12.2015.
 */
public class ParsingWork {

    private CustomUrl mURL;
    private final int BUFFER_SIZE = 100000;
    private Pattern PATTERN_URL_DOT;
    private Pattern PATTERN_EMAIL;
    private LinkedHashSet <CustomUrl> foundLinks;
    public static final String REG_EX_URL = "^*((https?|ftp)\\:\\/\\/)?(\\w{1})((\\.\\w)|(\\w))*\\.([a-z]{2,6})(\\/[a-z0-9_/]*)$*";
    public static final String REG_EX_TEL =  "^*((\\+38)?\\(?0\\d{2}?\\)?(\\d{7}|\\d{3}.\\d{2}.\\d{2}))\\+*";
    public static final String REG_EX_EMAIL = "^*([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$*";
    public static final String REG_EX_URL_DOT = "href=\"^?([a-z0-9_@?^=%&/~+#-]S*)*$*";


    public ParsingWork(CustomUrl url)
    {
        mURL = new CustomUrl(url.getHome(),url.getPath());
        PATTERN_URL_DOT = Pattern.compile(REG_EX_URL_DOT);
        PATTERN_EMAIL = Pattern.compile(REG_EX_EMAIL);
        foundLinks = new LinkedHashSet<>();
    }

    public LinkedHashSet<CustomUrl> goAhead()
    {
        BufferedReader buffer;
         try {
        StringBuffer mStringBuffer = new StringBuffer();
             System.out.println(mURL);
        if((buffer = getBufferFromUrl(mURL.toString()))==null) {
            return new LinkedHashSet<>();
        }

        int count =0;
        while (true) {
            char [] nextLine= new char [BUFFER_SIZE];

                if (buffer.read(nextLine)== -1)
                    break;

                mStringBuffer.append(nextLine);
                count++;

                // System.out.println(mStringBuffer.toString());
                checkStringByPatterns(mStringBuffer.toString());

        }

        buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return foundLinks;
    }



    private void checkStringByPatterns(String s)
    {

        Matcher matcher_dot = PATTERN_URL_DOT.matcher(s);
        while (matcher_dot.find())
        {
            String found = s.substring((matcher_dot.start()+6),matcher_dot.end());
            //System.out.println(s.substring((matcher_dot.start()),matcher_dot.end()));
            String tmp = mURL.getHome()+found;

            if(tmp.contains(mURL.toString())&& !tmp.equals(mURL.toString()))    //проверяем является ли ссылка дочерней относительно исходной
                //if (checkUrl(tmp))
                //System.out.println(tmp);
            //System.out.println(mURL.getHome()+ " "+ found);
                if(foundLinks.add(new CustomUrl(mURL.getHome(),found)))
                    Log.d("pasrser", mURL.getHome()+found);
           // Log.d("parser", tmp);

        }
        matcher_dot = PATTERN_EMAIL.matcher(s);

        while(matcher_dot.find())        {

           if (  addToList(s.substring(matcher_dot.start(), matcher_dot.end())))
           {
               Log.d("parser", s.substring(matcher_dot.start(), matcher_dot.end()));
           }

               //System.out.println(s.substring(matcher_dot.start(), matcher_dot.end()));
          ;
        }
    }

    @UiThread
    private boolean addToList(String s)
    {
        return MainActivity.mSynchronSet.add(s);
    }


    private BufferedReader getBufferFromUrl(String link)
    {
        URL url = null;
        HttpURLConnection httpcon = null;
        InputStreamReader ISR = null;

        try {
            url = new URL(link);
            httpcon = (HttpURLConnection)url.openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");
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

/*
    private boolean checkUrl(String link)  {
        URL url = null;
        try {
            url = new URL(link);

            HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
            httpcon.setConnectTimeout(0);
            httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");

            if (httpcon.getResponseCode() == 200)
                return true;
        }

        catch (UnknownHostException e)
        {
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
*/


}
