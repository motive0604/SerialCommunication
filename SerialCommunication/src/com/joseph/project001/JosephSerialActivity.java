package com.joseph.project001;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class JosephSerialActivity extends Activity {
	private static final String TAG = "JosephSerialActivity";
	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	//serial sending thread
	private SendingThread mSendingThread;
	//serial receiving thread
	private ReadingThread mReadingThread;
	//sendingλ¬Έμ?΄
	private byte[] mBuffer;
	//receive? λ¬Έμ?΄? ?λ©΄μ ???  textview
	private TextView mReception;
	
	private class SendingThread extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					if (mFileOutputStream != null) {
						mFileOutputStream.write(mBuffer);
					} else {
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	private class ReadingThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mFileInputStream == null) return;
					size = mFileInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
		System.loadLibrary("serial_port");
		mFd = open("/dev/s3c2410_serial2",115200,0);
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
		
		//?Ή?  λ¬Έμλ‘? μ±μΈ κ²½μ° ?? λ°©λ² ?¬?©
		//mBuffer = new byte[1024];
		//Arrays.fill(mBuffer, (byte) 0x58);	  
		//λ¬Έμ?΄λ‘? λ§λ€ κ²½μ° String? ??±?΄? byte arrayλ‘? λ³?κ²½ν?κ²? ?Έλ¦¬ν¨.
        String stringToConvert = "MangoV210 Serial Test - ZETA\r\n";
        mBuffer = stringToConvert.getBytes();

        mReception = (TextView)findViewById(R.id.ReceiveText);
        Button buttonReceiveText = (Button)findViewById(R.id.ButtonReceiveText);
        
        final Button buttonSendText = (Button)findViewById(R.id.ButtonSendText);
        buttonSendText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//send threadκ°? ??±?μ§? ???μ§? ??Έ??€
				//start button? ?λ₯? ? stop button? ?λ₯? κ²½μ°? ?΄?Ή??€.
				if (mSendingThread == null) {
					mSendingThread = null;	
				}
				mSendingThread = new SendingThread();
				mSendingThread.start();					
			}
		});

        final Button buttonStopSendText = (Button)findViewById(R.id.ButtonStopSendText);
        buttonStopSendText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mSendingThread != null)
					mSendingThread.interrupt();					
			}
		});

        buttonReceiveText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mReadingThread == null) {
					mReadingThread = null;	
				}
				mReadingThread = new ReadingThread();
				mReadingThread.start();				
		    }
		});

        final Button buttonClearRx = (Button)findViewById(R.id.ButtonClearRxText);
        buttonClearRx.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mReception.setText(" ");				
		    }
		});
        
        final Button buttonAbout = (Button)findViewById(R.id.ButtonAbout);
        buttonAbout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(JosephSerialActivity.this);
				builder.setTitle("About");
				builder.setMessage(R.string.about_msg);
				builder.show();
			}
		});

        final Button buttonQuit = (Button)findViewById(R.id.ButtonQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				JosephSerialActivity.this.finish();
			}
		});
    }
           
	// JNI
	public native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
        
}