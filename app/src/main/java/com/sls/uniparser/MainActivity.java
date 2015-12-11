package com.sls.uniparser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    private final int TIMER_QUANT = 1000; //ms
    private final int TIMER_DELAY = 100;
    private Button mButtonControl;
    private ProgressBar mProgress;
    private ListView mListViewEmails;
    private TextView mProgressText;
    private Spinner mSpinner;
    private EditText mEditUrl;
    private static CustomAdapter mCustomAdapter;
    private static HashSet<String> mSetEmails;
    public static Set<String> mSynchronSet;
    private Parser mParser;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    //private TextView mTextFoundEmail;


    private boolean isStart =false;
    private final int MIN_LEN_URL=11;
    private final int LEN_URL_PROTOCOL = 8;
    private int mDepthParsing = 1;

    private Integer [] mListDepth = {1,2,3,4,5};
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
                startParsing();
            }
        });
    }
    private void initAdapter(){

        mSetEmails = new HashSet<String>();
        mSynchronSet = Collections.synchronizedSet(mSetEmails);

        ListView lvEmail = (ListView)findViewById(R.id.listEmail);
        mCustomAdapter = new CustomAdapter(this,mSynchronSet);

        lvEmail.setAdapter(mCustomAdapter);
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


        if (URL.contains("http"))
        {
            if (mTimer != null) {
                mTimer.cancel();
            }

            String URL_path = URL.substring(URL.indexOf("/",LEN_URL_PROTOCOL));
            URL = URL.replace(URL_path,"");
            Log.d("PARSER3",""+mDepthParsing);
            mParser = new Parser(new CustomUrl(URL, URL_path), mDepthParsing);
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


    public void startParsView()
    {
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText(getResources().getString(R.string.text_progress));
        mProgress.setVisibility(View.VISIBLE);
        mEditUrl.setEnabled(isStart);
        mButtonControl.setText(getResources().getText(R.string.btnStop));
        mSynchronSet.clear();
        mCustomAdapter.notifyDataSetChanged();
        mButtonControl.setEnabled(false);
    }

    public void stopPars()
    {
        mProgressText.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
        mEditUrl.setEnabled(isStart);
        mButtonControl.setText(getResources().getText(R.string.btnStart));
                if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

                mButtonControl.setEnabled(true);
                isStart = false;
        }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }



    class MyTimerTask extends TimerTask {

        @Override
        public void run() {


            runOnUiThread(new Runnable() {
                @UiThread
                @Override
                public void run() {
                   if ( mCustomAdapter!= null) mCustomAdapter.notifyDataSetChanged();
                    if (mParser!=null)
                        if(mParser.isCancelled())
                        {
                            mParser = null;
                            stopPars();
                            Thread.currentThread().interrupt();

                        }
                }
            });
        }
    }


}
