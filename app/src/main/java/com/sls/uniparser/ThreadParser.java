package com.sls.uniparser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ThreadParser extends Thread  {

    private static final String TAG_THREAD = "PARSER3";
    private BlockingQueue<CustomUrl> blockingTask;
    private LinkedHashSet<CustomUrl> mSetForNextStep;
    private int mTaskDepth;
    private int mCurrentDepth;
    private int mThreadId;
    private boolean isInterrupt = false;
    public ThreadParser( LinkedHashSet<CustomUrl> task, int depth, int thread_id)
    {
        mSetForNextStep = new LinkedHashSet<>();
        blockingTask = new ArrayBlockingQueue<>(task.size());
        setQueueTask(task);
        mTaskDepth = depth;
        mCurrentDepth =0;
        mThreadId = thread_id;

    }

    private void setQueueTask(LinkedHashSet<CustomUrl>task)
    {
        if (task!= null)
            if(task.size()>0) {
                blockingTask.clear();
                blockingTask.addAll(task);
                mSetForNextStep.clear();
            }
    }

    private boolean startParsing()
    {
        while (!blockingTask.isEmpty())
        {
            if ( isInterrupt )
            return true;

            ParsingWork mParseingWork = new ParsingWork(blockingTask.poll());
            LinkedHashSet<CustomUrl> tmp =  new LinkedHashSet<>();
            tmp =  mParseingWork.goAhead();
            if(tmp != null)
            mSetForNextStep.addAll(tmp);//ParsingWork.goAhead() return list of links for next step

        }

        return true;
    }
    public void stopIt()
    {
        isInterrupt  = true;

        interrupt();
    }

    @Override
    public void run() {


        while(mTaskDepth > mCurrentDepth && !isInterrupt) {

            if(!isInterrupt) {
                System.out.println("Thread #" + mThreadId + " on the " + mCurrentDepth + " step");
                startParsing();
                System.out.println("Thread #" + mThreadId + " done step #" + mCurrentDepth);
                mCurrentDepth++;
                setQueueTask(mSetForNextStep);
            }
        }
        interrupt();


    }

    @Override
    public void interrupt()
    {
        if (!this.isInterrupted())
        {
            super.interrupt();
            setThisNull();
        }
    }

    private void setThisNull()
    {

        mSetForNextStep.clear();
        blockingTask.clear();
        mSetForNextStep = null;
        blockingTask = null;
        System.gc();

    }


}
