package com.sls.uniparser;


import android.os.AsyncTask;
import android.util.Log;
import java.util.*;
import java.util.concurrent.*;


public class Parser  extends AsyncTask<Void,Void,Boolean>{

    private CustomUrl mURL;
    private int mTaskDepth;
    private LinkedHashSet<CustomUrl> firstStep;
    private int mAvailableThreads;
    private ExecutorService mExecutor;
    private ArrayList<Thread>mThreadList;

    public Parser(CustomUrl URL, int depth)
    {
        mURL = new CustomUrl(URL.getHome(),URL.getPath());
        mTaskDepth =depth;
        firstStep = new LinkedHashSet<>();


        mAvailableThreads = Runtime.getRuntime().availableProcessors();//return amount of processes which OS could give
        if (mAvailableThreads <=1)
            mAvailableThreads = 2;                                     //2 threads better than 1
        mExecutor = Executors.newFixedThreadPool(mAvailableThreads);
        System.out.println("Available threads:  "+ mAvailableThreads);
        mThreadList = new ArrayList<>();

    }

   private void runTask() {

       ParsingWork mParsing = new ParsingWork(mURL);
       firstStep = mParsing.goAhead();      //First walk of parser

        int tmp_count= 0;                   // counter of threads

        if (mTaskDepth <= 1)                //1 depth = 1 walk
            return;

       if(mAvailableThreads ==1)
       {
           initThread(new ThreadParser(firstStep, mTaskDepth, ++tmp_count));
           firstStep.clear();
           this.cancel(true);
           return;
       }


       int coef = firstStep.size() / mAvailableThreads;    // distribution links between threads
       LinkedHashSet<CustomUrl> prepareTask = new LinkedHashSet<>();

       while(!firstStep.isEmpty()) {

            prepareTask.clear();
            if (firstStep.size() < coef*2)              //if list of links less than for 2 threads - load all links in 1
            {
                prepareTask.addAll(firstStep);
                initThread(new ThreadParser(prepareTask, mTaskDepth, ++tmp_count));
                firstStep.clear();
                continue;
            }

            int counter =0;
            for (CustomUrl tmp: firstStep) {
                prepareTask.add(tmp);
                counter++;
                if (counter>=coef)
                    break;
            }

           initThread(new ThreadParser(prepareTask, mTaskDepth, ++tmp_count));
            firstStep.removeAll(prepareTask);
        }

       }

       private void  initThread(Thread thread)
    {
        thread.setDaemon(true);
        mThreadList.add(thread);
        mExecutor.submit(thread);
    }



    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        for(Thread tmp : mThreadList)
        {
            Log.d("PARSER3", tmp.getName() + " should be interrupt");
            tmp.interrupt();
        }
        setThisNull();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        runTask();
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        this.cancel(true);

    }

    private void setThisNull()      //try to kill this object
    {
        mExecutor = null;
        mURL = null;
        firstStep = null;
        mThreadList = null;
        Thread.currentThread().interrupt();
    }

}
