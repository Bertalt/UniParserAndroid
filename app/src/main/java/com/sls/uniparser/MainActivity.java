package com.sls.uniparser;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    final int TIMER_QUANT = 1000;    // for timer task (ms)
    final int TIMER_DELAY = 100;     //  .... (ms)
    final int MIN_LEN_URL=11;
    final int LEN_URL_PROTOCOL = 8;

    private Button mButtonControl;
    private ProgressBar mProgress;
    private TextView mProgressText;
    private Spinner mSpinner;
    private EditText mEditUrl;

    private static CustomAdapter mCustomAdapter;
    private HashSet<String> mSetEmails;
    private HashSet<String> mSetForList;
    public static Set<String> mSynchronSet;
    private Parser mParser;

    private Timer mTimer;
    private MyTimerTask mMyTimerTask;       //update list of emails by timer



    private boolean isStart =false;         // indicator of parser
    private int mDepthParsing = 1;

    private Integer [] mListDepth = {1,2,3,4,5}; //available depth [use in spinner]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initEditUrl();
        initProgress();
        initButton();
        initAdapter();
        initSpinner();
    }

    private void initEditUrl()
    {
        mEditUrl = (EditText)findViewById(R.id.editURL);
    }

    private void initProgress()
    {
        mProgress = (ProgressBar)findViewById(R.id.progressBar);
        mProgressText = (TextView)findViewById(R.id.tvProgress);
    }



    private void initButton()
    {
        mButtonControl = (Button)findViewById(R.id.btnStart);
        mButtonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mParser == null) {
                    startParsing();
                    return;
                }
                if (!isStart)
                    startParsing();
                else
                    stopPars();
            }
        });
    }
    private void initAdapter(){

        mSetEmails = new HashSet<String>();
        mSynchronSet = Collections.synchronizedSet(mSetEmails);

        ListView lvEmail = (ListView)findViewById(R.id.listEmail);
        mSetForList = new HashSet<>();
        mCustomAdapter = new CustomAdapter(this,mSetForList);

        lvEmail.setAdapter(mCustomAdapter);

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
    }

    private void initSpinner()
    {
        mSpinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, mListDepth);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setPrompt(getResources().getString(R.string.spinnerPromt));
        mSpinner.setSelection(mDepthParsing);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDepthParsing = position+1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void startParsing()
    {
        String URL = mEditUrl.getText().toString();
        if (URL.length()<MIN_LEN_URL)
        {
            Toast.makeText(this,getResources().getString(R.string.toastToShortUrl),Toast.LENGTH_LONG).show();
            return;
        }
        if (!isNetworkConnected())
        {
            Toast.makeText(this,getResources().getString(R.string.toastDisconnect),Toast.LENGTH_SHORT).show();
            return;
        }
        if (mDepthParsing <=0)
        {
            Toast.makeText(this,getResources().getString(R.string.toastDepthError),Toast.LENGTH_SHORT).show();
            return;
        }
        if (URL.contains("http") || URL.contains("https"))
        {
            if (mTimer != null) {
                mTimer.cancel();
            }



            int tmp = URL.lastIndexOf("/");

            if  (tmp == URL.length()-1)
                    URL = URL.substring(0,URL.length()-1);

            tmp = URL.indexOf("/",LEN_URL_PROTOCOL);
            String URL_sub="";
            if (tmp != -1) {
                URL_sub = URL.substring(tmp,URL.length());
                URL = URL.substring(0, tmp);
            }
            URL_sub = URL_sub.concat("/");


           // Log.d("TAG", URL+" "+URL_sub);
            mParser = new Parser(new CustomUrl(URL, URL_sub), mDepthParsing);   // (URL for first run, maximal depth for parsing)
            mParser.execute();
            startParsView();
            
            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, TIMER_DELAY, TIMER_QUANT);
            isStart = true;

        }
        else
        {
           Toast.makeText(this,getResources().getString(R.string.toastIncorrectProtocol),Toast.LENGTH_LONG).show();
        }

    }


    public void stopPars()              //prepare UI & AsyncTask after parsing
    {
        mProgressText.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        mEditUrl.setEnabled(isStart);
        if (mSynchronSet.isEmpty())
            Toast.makeText(this,getResources().getString(R.string.toastNothingFound),Toast.LENGTH_LONG).show();
        mButtonControl.setText(getResources().getText(R.string.btnStart));
        mParser.cancel(true);
        mParser = null;
                if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
                isStart = false;
        }

    public void startParsView()         //prepare UI elements before parsing
    {
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText(getResources().getString(R.string.text_progress));
        mProgress.setVisibility(View.VISIBLE);
        mEditUrl.setEnabled(isStart);
        mButtonControl.setText(getResources().getText(R.string.btnStop));
        mSynchronSet.clear();
        mCustomAdapter.notifyDataSetChanged();
        //  mButtonControl.setEnabled(false);
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {         //Run every TIMER_QUANT & update ListView

            runOnUiThread(new Runnable() {
                @UiThread
                @Override
                public void run() {
                   if ( mCustomAdapter!= null) {
                       mSetForList.clear();
                       mSetForList.addAll(mSynchronSet);
                       mCustomAdapter.notifyDataSetChanged();
                   }
                    if (mParser!=null)
                        if(mParser.isCancelled())
                        {
                            stopPars();
                            Thread.currentThread().interrupt();
                        }
                }
            });
        }
    }


}
