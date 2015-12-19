package com.sls.uniparser;


import android.os.AsyncTask;
import java.util.*;
import java.util.concurrent.*;


public class Parser  extends AsyncTask<Void,Void,Boolean>{

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private CustomUrl mURL;
    private int mTaskDepth;
    private LinkedHashSet<CustomUrl> firstStep;
    private ExecutorService mExecutor;
    private ArrayList<ThreadParser>mThreadList;

    public Parser(CustomUrl URL, int depth)
    {
        mURL = new CustomUrl(URL.getHome(),URL.getPath());
        mTaskDepth =depth;
        firstStep = new LinkedHashSet<>();
                                     //2 threads better than 1
        mExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        System.out.println("Available threads:  "+ CORE_POOL_SIZE);
        mThreadList = new ArrayList<>();
        //Thread.currentThread().setDaemon(true);

    }

   private void runTask() {

       ParsingWork mParsing = new ParsingWork(mURL);
       firstStep = mParsing.goAhead();      //First walk of parser

        int tmp_count= 0;                   // counter of threads

        if (mTaskDepth <= 1)                //1 depth = 1 walk
            return;


       int coef = firstStep.size() / CORE_POOL_SIZE;    // distribution links between threads
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

       private void  initThread(ThreadParser thread)
    {
        thread.setDaemon(true);
        mThreadList.add(thread);
        mExecutor.submit(thread);
    }



    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        for(ThreadParser tmp : mThreadList)
              tmp.stopIt();


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
