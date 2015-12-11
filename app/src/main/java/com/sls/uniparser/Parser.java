package com.sls.uniparser;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Serg on 09.12.2015.
 */
public class Parser  extends AsyncTask<CustomUrl,String,Boolean>{

    private CustomUrl mURL;
    private int mTaskDepth;
    private LinkedHashSet<CustomUrl> firstStep;
    private int mAvailableThreads;
    private ExecutorService mExecutor;
    private ArrayList<Future> mFutureList;

    public Parser(CustomUrl URL, int depth)
    {
        mURL = new CustomUrl(URL.getHome(),URL.getPath());
        mTaskDepth =depth;
        firstStep = new LinkedHashSet<>();


        mAvailableThreads = Runtime.getRuntime().availableProcessors();
        mExecutor = Executors.newFixedThreadPool(mAvailableThreads);
        mFutureList = new ArrayList<>();

    }

   private void run() {

       ParsingWork mParsing = new ParsingWork(mURL);
       firstStep = mParsing.goAhead();

        int tmp_count= 0;

        if (mTaskDepth <= 1)
            return;


        int coef = firstStep.size() / mAvailableThreads;
        System.out.println("Available threads:  "+ mAvailableThreads);
        LinkedHashSet<CustomUrl> prepareTask = new LinkedHashSet<>();
       while(!firstStep.isEmpty()) {


           if(mAvailableThreads ==1)
           {
               mExecutor.submit(new ThreadParser(firstStep,mTaskDepth, ++tmp_count));
               firstStep.clear();
               break;
           }

            prepareTask.clear();

            if (firstStep.size() < coef*2)
            {
                prepareTask.addAll(firstStep);
                Thread prepareThread = new ThreadParser(prepareTask, mTaskDepth, ++tmp_count);
                prepareThread.setDaemon(true);
               mFutureList.add(mExecutor.submit(prepareThread));
                firstStep.clear();
                break;
            }

            int counter =0;
            for (CustomUrl tmp: firstStep) {
                prepareTask.add(tmp);
                counter++;
                if (counter>=coef)
                    break;
            }

           Thread prepareThread = new ThreadParser(prepareTask, mTaskDepth, ++tmp_count);
           prepareThread.setDaemon(true);
           mFutureList.add(mExecutor.submit(prepareThread));
            firstStep.removeAll(prepareTask);

        }



    }

    @Override
    protected Boolean doInBackground(CustomUrl... params) {


        run();
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.currentThread().interrupt();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mExecutor = null;
        mURL = null;
        mFutureList = null;
        firstStep = null;
        this.cancel(true);


    }

}
