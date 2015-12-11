package com.sls.uniparser;

import android.content.Intent;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ThreadParser extends Thread  {

    private BlockingQueue<CustomUrl> blockingTask;
    private LinkedHashSet<CustomUrl> mSetForNextStep;
    private int mTaskDepth;
    private int mCurrentDepth;
    private int mThreadId;
    public ThreadParser( LinkedHashSet<CustomUrl> task, int depth, int thread_id)
    {
        mSetForNextStep = new LinkedHashSet<>();
        mSetForNextStep.addAll(task);
        setQueueTask(mSetForNextStep);
        mTaskDepth = depth;
        mCurrentDepth =0;
        mThreadId = thread_id;
    }

    private void setQueueTask(LinkedHashSet<CustomUrl>task)
    {
        if (task!= null)
            if(task.size()>0) {
                blockingTask = new ArrayBlockingQueue<CustomUrl>(task.size());
                blockingTask.addAll(task);
                mSetForNextStep.clear();
            }
    }

    private boolean isDone()
    {
        while (!blockingTask.isEmpty())
        {
            ParsingWork mParsing = new ParsingWork(blockingTask.poll());
            mSetForNextStep.addAll( mParsing.goAhead());
        }

        return true;
    }

    @Override
    public void run() {

        System.out.println("Thread #"+ mThreadId+" starting");

        while(mTaskDepth > mCurrentDepth) {
            System.out.println("Thread #"+mThreadId+" on the "+mCurrentDepth+" step");
            while (!isDone()) {

                System.out.println("LAG");
            }
            System.out.println("Thread #"+mThreadId+ " done step #"+mCurrentDepth);
                mCurrentDepth++;

        }

        Thread.currentThread().interrupt();

    }


}
