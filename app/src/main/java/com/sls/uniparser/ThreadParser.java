package com.sls.uniparser;

import android.util.Log;

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
    ParsingWork mParseingWork;
    private boolean isInterrupt = false;
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
            if ( isInterrupt )
        {
            Log.d(TAG_THREAD,getName()+" was interrupt");
            return true;
        }
            mParseingWork = new ParsingWork(blockingTask.poll());
            mSetForNextStep.addAll( mParseingWork.goAhead());//ParsingWork.goAhead() return list of links for next step

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


}
