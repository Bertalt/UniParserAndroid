package com.sls.uniparser;

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
                blockingTask = new ArrayBlockingQueue<>(task.size());
                blockingTask.addAll(task);
                mSetForNextStep.clear();
            }
    }

    private boolean startParsing()
    {
        while (!blockingTask.isEmpty())
        {
            ParsingWork mParsing = new ParsingWork(blockingTask.poll());
            mSetForNextStep.addAll( mParsing.goAhead());//ParsingWork.goAhead() return list of links for next step
            if (Thread.currentThread().isInterrupted())
                return true;
        }

        return true;
    }

    @Override
    public void run() {


        while(mTaskDepth > mCurrentDepth && !Thread.currentThread().isInterrupted()) {

            System.out.println("Thread #"+mThreadId+" on the "+mCurrentDepth+" step");
            startParsing();
            System.out.println("Thread #"+mThreadId+ " done step #"+mCurrentDepth);
            mCurrentDepth++;
            setQueueTask(mSetForNextStep);
        }



    }


}
