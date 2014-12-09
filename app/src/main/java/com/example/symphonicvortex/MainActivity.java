package com.example.symphonicvortex;

import android.app.Dialog;
import android.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

	private Button mButton;
	private EditText mEdit;
	private TextView mTextView;
	private TextView mTextViewDisplayInfo;
	private TextView mLog;
    private TextView mBluetooth;
	private MediaPlayer mp;
    private BluetoothSender bt;
    private SeekBar mSeeker;
    private SeekBar sCR1;
    private SeekBar sCG1;
    private SeekBar sCB1;
    private SeekBar sCR2;
    private SeekBar sCG2;
    private SeekBar sCB2;
    private SeekBar sCR3;
    private SeekBar sCG3;
    private SeekBar sCB3;
    private SeekBar sCR4;
    private SeekBar sCG4;
    private SeekBar sCB4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.mButton = (Button)findViewById(R.id.button1);
		this.mEdit = (EditText)findViewById(R.id.editText1);
		this.mTextView = (TextView)findViewById(R.id.textView1);
        this.mBluetooth = (TextView)findViewById(R.id.bluetooth);
        this.mSeeker = (SeekBar)findViewById(R.id.seek);
        this.sCR1 = (SeekBar)findViewById(R.id.R1);
        this.sCG1 = (SeekBar)findViewById(R.id.G1);
        this.sCB1 = (SeekBar)findViewById(R.id.B1);
        this.sCR2 = (SeekBar)findViewById(R.id.R2);
        this.sCG2 = (SeekBar)findViewById(R.id.G2);
        this.sCB2 = (SeekBar)findViewById(R.id.B2);
        this.sCR3 = (SeekBar)findViewById(R.id.R3);
        this.sCG3 = (SeekBar)findViewById(R.id.G3);
        this.sCB3 = (SeekBar)findViewById(R.id.B3);
        this.sCR4 = (SeekBar)findViewById(R.id.R4);
        this.sCG4 = (SeekBar)findViewById(R.id.G4);
        this.sCB4 = (SeekBar)findViewById(R.id.B4);
        this.mTextViewDisplayInfo = (TextView)findViewById(R.id.textView2);
		this.mLog = (TextView)findViewById(R.id.textView3);
		this.mEdit.setText("SANDSTORM.wav");
		this.mp = new MediaPlayer();
	}

    protected void onStart() {
        super.onStart();
        connect();
    }
	
	@Override
	protected void onDestroy()
	{
		try 
		{
			this.bt.close();
		} 
		catch (Exception e) {}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private class Process implements Runnable {

        private MainActivity mActivity;

        public Process(MainActivity m) {
            mActivity = m;
        }

        @Override
        public void run() {
            try {
                mp.pause();
                mp.stop();
                mp.release();
            }
            catch (Exception e) {}
            try {
                AudioProcessing ap = new AudioProcessing(mActivity.getFile(),bt);
                mp = MediaPlayer.create(mActivity, Uri.parse("/storage/extSdCard/" + mActivity.getFile()));
                mp.start();
                //mActivity.mTextViewDisplayInfo.setText(ap.getInfo());
                do
                {
                    bt.send(ap.getAudio());
                    Thread.sleep(2000);
                }
                while (ap.getFramesRead() != 0);

                //mActivity.mTextView.setText(ap.status);
                ap.close();
            }
            catch (Exception e) {
                //mLog.append(e.toString());
                System.err.println(e);
            }

        }
    }


    private Thread mPThread;
	public void processAudio(View view) {
        //mActivity.mTextViewDisplayInfo.setText(ap.getInfo());

        mPThread = (new Thread(new Process(this)));
        mPThread.start();
	}

    private String getTime() {
        return Integer.toString(mSeeker.getProgress()*20);

    }

    public class Color {
        private int r,g,b;
        public Color(int rr, int gg, int bb) {
            r = rr;
            g = gg;
            b = bb;
        }

        public String toString() {
            return "custom(" + Integer.toString(r) + ","
                             + Integer.toString(g) + ","
                             + Integer.toString(b) + ")";
        }

    }

    public Color getColor1() {
        int r = (int) (sCR1.getProgress() * 2.55);
        int g = (int) (sCG1.getProgress() * 2.55);
        int b = (int) (sCB1.getProgress() * 2.55);
        return new Color(r,g,b);
    }

    public Color getColor2() {
        int r = (int) (sCR2.getProgress() * 2.55);
        int g = (int) (sCG2.getProgress() * 2.55);
        int b = (int) (sCB2.getProgress() * 2.55);
        return new Color(r,g,b);
    }

    public Color getColor3() {
        int r = (int) (sCR3.getProgress() * 2.55);
        int g = (int) (sCG3.getProgress() * 2.55);
        int b = (int) (sCB3.getProgress() * 2.55);
        return new Color(r,g,b);
    }

    public Color getColor4() {
        int r = (int) (sCR4.getProgress() * 2.55);
        int g = (int) (sCG4.getProgress() * 2.55);
        int b = (int) (sCB4.getProgress() * 2.55);
        return new Color(r,g,b);
    }
	
	public void sendPulse(View view)
	{
		try {

			bt.send("pulse " + getTime() + " " + getColor1().toString());
            Toast.makeText(this, "sent pulse", Toast.LENGTH_SHORT);
		}
		catch (Exception e) {
			mLog.append(e.toString());
			System.err.println(e);
		}
	}
	
	public void sendSpiral(View view)
	{
		try {
			bt.send("repeat 4\n" +
                    "spiral " + (Integer.parseInt(getTime())/4) + " " + getColor1().toString()
                    + " " + getColor2().toString()
                    + " " + getColor3().toString()
                    + " " + getColor4().toString() +
                    "\nrepeat end");
		}
		catch (Exception e) {
			mLog.append(e.toString());
			System.err.println(e);
		}
	}

    public void sendRevSpiral(View view)
    {
        try {
            bt.send("reverse spiral " + getTime() + " " + getColor1().toString()
                    + " " + getColor2().toString()
                    + " " + getColor3().toString()
                    + " " + getColor4().toString());
        }
        catch (Exception e) {
            mLog.append(e.toString());
            System.err.println(e);
        }
    }

	public void sendSquare(View view)
	{
		try {
			bt.send("squares " + getTime() + " " + getColor1().toString()
                    + " " + getColor2().toString()
                    + " " + getColor3().toString()
                    + " " + getColor4().toString());
		}
		catch (Exception e) {
			mLog.append(e.toString());
			System.err.println(e);
		}
	}
	
	public void clearAll(View view)
	{
        if (mPThread != null) mPThread.interrupt();
		mLog.setText("Log");
		mTextViewDisplayInfo.setText("Display Audio Info");
		mTextView.setText("Choose audio file (.wav)...");
		mEdit.setText("SANDSTORM.wav");
		try {
            mp.pause();
            mp.stop();
            mp.release();
        }
        catch (Exception e) {}


        // for testing
        if (bt != null) {

            bt.send("pulse 1000 blue");
        }
	}


    public void connect() {
        try {
            bt.close();
        }
        catch(Exception e) {}
        mBluetooth.setText("Connecting to bluetooth");
        bt = new BluetoothSender();
        if (bt != null && bt.isConnected())
            mBluetooth.setText("Connected");
        else
            mBluetooth.setText("Not connected");
        if (bt!=null && bt.isConnected())
            bt.send("nop");
    }
    public void connect(View v) {
        mBluetooth.setText("Connecting to bluetooth");
        connect();
    }
	
	private String getFile()
	{
		return mEdit.getText().toString();
	}
}
