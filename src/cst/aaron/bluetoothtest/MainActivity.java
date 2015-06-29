package cst.aaron.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	private static BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID=UUID.fromString("66841278-c3d1-11df-ab31-001de000a903");
//	private static OutputStream mOutputStream;
//	private static InputStream mInputStream;
	private final static int 	REQUEST_CODE_BT=1;
	private static final String MY_NAME="Bluetooth_Test_Aaron";
	private AcceptThread acceptThread;
	private ConnectedThread mConnectedThread;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_CONNECTED=1;
	public static final int MESSAGE_DISCONNECTE=3;
	private final static int DISCURABLE_TIME=300;
	
	public static TextView count_TextView;
	public static ImageView signal_ImageView;
	public static TextView connecttion_TextView;
	public static ImageView left_ImageView;
	public static ImageView straight_ImageView;
	public static ImageView right_ImageView;
	public static TextToSpeech textToSpeech;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // setup UI view;
        count_TextView=(TextView)findViewById(R.id.signal_count);
        signal_ImageView=(ImageView)findViewById(R.id.signal_img);
        connecttion_TextView=(TextView)findViewById(R.id.connect_label);
        left_ImageView=(ImageView)findViewById(R.id.direction_left);
        straight_ImageView=(ImageView)findViewById(R.id.direction_straight);
        right_ImageView=(ImageView)findViewById(R.id.direction_right);
        
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {			
			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
			
				if (status==TextToSpeech.SUCCESS) {
					textToSpeech.setLanguage(Locale.UK);	
					speakToText("Hello, Welcome to CST Connected Vehicle Lab!");
				}
			}
		});
        
        
        // setup the BlueTooth 
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter==null) {
			Toast.makeText(getApplicationContext(), "Your device doesn't support bluetooth~", Toast.LENGTH_SHORT).show();		
		}
        
       /* if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_CODE_BT);
		}*/
        
        
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCURABLE_TIME);
                startActivityForResult(discoverableIntent,REQUEST_CODE_BT);
         }else{
            if(acceptThread==null){
               acceptThread=new AcceptThread();
           	   acceptThread.start();
             }
         }
    }

    public static void speakToText(String string){
    	
    	textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }
    protected void onActivityResult( int requestCode,  int resultCode, Intent data){
    	
    	switch (requestCode) {
		case REQUEST_CODE_BT:
			if (resultCode==DISCURABLE_TIME) {
				if (acceptThread==null) {
					acceptThread=new AcceptThread();
					acceptThread.start();
				}
			}
			break;

		default:
			break;
		}
    }
    
    private class AcceptThread extends Thread{
    	private BluetoothServerSocket mServerSocket=null;
    	
    	public AcceptThread(){
    		
    		BluetoothServerSocket tmp=null;
    		try {
				
    			tmp=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(MY_NAME, MY_UUID);
    			Log.v("bluetooth", "serversocket create success");
			} catch (Exception e) {
				// TODO: handle exception	
				Log.v("bluetooth", "serversocket create fail");
			}
    		mServerSocket=tmp;
    	}
    	
    	public void run(){
    		BluetoothSocket mBluetoothSocket=null;
    		while(true){
    			try {
					mBluetoothSocket=mServerSocket.accept();
					
				} catch (Exception e) {
					// TODO: handle exception
			//		Toast.makeText(getApplicationContext(), "unable to connect!", Toast.LENGTH_SHORT).show();
					Log.v("bluetooth", "unable to connect");
					break;
				}
    			if (mBluetoothSocket!=null) {
					// do something about the socket;
    			
    			
    			connected(mBluetoothSocket);
    				Log.v("bluetooth", "bluetooth connected");
    				try {
						mServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				break;
				}
    		}
    	}
    	public void cancel(){
    		try {
				mServerSocket.close();
				mHandler.obtainMessage(MESSAGE_DISCONNECTE).sendToTarget();
			} catch (Exception e) {
				// TODO: handle exception
			}
    	}
    }
    public synchronized void connected(BluetoothSocket socket){
    	if (acceptThread!=null) {
			acceptThread.cancel();acceptThread=null;
		}
    	if (mConnectedThread!=null) {
			mConnectedThread.cancel();mConnectedThread=null;
		}
    	mConnectedThread=new ConnectedThread(socket);
    	mConnectedThread.start();
    }
    
    private class ConnectedThread extends Thread{
    	private final BluetoothSocket mmSocket;
    	private  final InputStream mmInputStream;
    	//private  final OutputStream mmOutputStream;
    	
    	public ConnectedThread(BluetoothSocket socket){
    		
    		mmSocket=socket;
    		InputStream tmpInputStream=null;
    		//OutputStream tmpOutputStream=null;
    		try {
				tmpInputStream=socket.getInputStream();
				//tmpOutputStream=socket.getOutputStream();
			} catch (Exception e) {
				// TODO: handle exception
				disconnected_UI();
				Log.v("bluetooth", "tmp sockets not created");
				
			}
    		mmInputStream=tmpInputStream;
    	//	mmOutputStream=tmpOutputStream;
    		
    	}
    	public void run(){
    		byte[] buffer=new byte[1024];
    		int bytes;
    		//String messageString="Hello, I am Android";
    	//	byte[] sent_message=messageString.getBytes();
    		while (true) {
				try {
					
					bytes=mmInputStream.read(buffer);
					//mmOutputStream.write(sent_message);
				//	Log.v("bluetooth", "Send Message:"+messageString);
					mHandler.obtainMessage(MESSAGE_CONNECTED).sendToTarget();
					
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (Exception e) {
					// TODO: handle exception
					disconnected_UI();
					Log.v("bluetooth", "disconnected");
				}
			}
    	}
    	public void cancel(){
    		try {
				mmSocket.close();
			} catch (Exception e) {
				// TODO: handle exception
				Log.v("bluetooth", "close socket failed");
			}
    	}
    	
    }
    
    private static final Handler mHandler=new Handler(){
    	public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ:
				
				byte[] readbuf=(byte[]) msg.obj;
				
				String readMessageString=new String(readbuf,0,msg.arg1);
			//	String message_getString=new BigInteger(0,readbuf).toString(16);
			//	String message_getString=byteArrayToHex(readbuf);
				/*int size= readbuf.length;
				int dataint[]= new int[size];
				StringBuilder bStringBuilder=new StringBuilder();
				for (int i = 0; i < msg.arg1; i++) {
					dataint[i]=readbuf[i];
					bStringBuilder.append(Integer.toHexString(dataint[i]));
				}
				String message_getString=bStringBuilder.toString();*/
				readMessageString=readMessageString.substring(5);
				Log.v("bluetooth", "read:"+readMessageString);
			//	Log.v("bluetooth", "read:"+message_getString);
				
				UpdateUI(MessageParse(readMessageString)); 
				break;
			case MESSAGE_CONNECTED:
				connecttion_TextView.setText("Connected");
				break;
			case MESSAGE_DISCONNECTE:
				connecttion_TextView.setText("not Connected");
				 break;
			default:
				break;
			}
		}
    };

    public static InfoEntity MessageParse(String msg){
    	
    	InfoEntity infoEntity=new InfoEntity();
    	
    	//int  count=Character.getNumericValue(msg.charAt(4));
    		int count=Integer.parseInt(msg.substring(3, 5));
    		Log.v("bluetooth", msg.substring(3, 5));
    	infoEntity.setSignal_time(count);
    	
    	if (msg.contains("GSB")) {
			infoEntity.setDirection_code(InfoEntity.SIGNAL_DIRECTION_RL);
			infoEntity.setSignal_color_code(InfoEntity.SINGAL_GREEN);
		}
    	if (msg.contains("RSB")) {
			infoEntity.setDirection_code(InfoEntity.SIGNAL_DIRECTION_LEFT);
			infoEntity.setSignal_color_code(InfoEntity.SIGNAL_RED);
		}
    	if (msg.contains("YSB")) {
			infoEntity.setDirection_code(InfoEntity.SIGNAL_DIRECTION_STRAIGHT);
			infoEntity.setSignal_color_code(InfoEntity.SIGNAL_YELLOW);
		}
    	
    	return infoEntity;
    	
    }
   private static void disconnected_UI(){
	    signal_ImageView.setImageResource(R.drawable.none);
		count_TextView.setVisibility(TextView.GONE);
		connecttion_TextView.setText("not Connected");
   }
    private static void UpdateUI(InfoEntity infoentity){
    	
    	switch (infoentity.getSignal_color_code()) {
		case InfoEntity.SIGNAL_RED:
			signal_ImageView.setImageResource(R.drawable.red);
			count_TextView.setTextColor(Color.RED);
			break;
		case InfoEntity.SIGNAL_YELLOW:
			signal_ImageView.setImageResource(R.drawable.yellow);
			count_TextView.setTextColor(Color.YELLOW);
			break;
		case InfoEntity.SINGAL_GREEN:
			signal_ImageView.setImageResource(R.drawable.green);
			count_TextView.setTextColor(Color.GREEN);
			break;
		default: 
			signal_ImageView.setImageResource(R.drawable.none);
			count_TextView.setVisibility(TextView.GONE);
			connecttion_TextView.setText("not Connected");
			break;
		}
    	
    	if (infoentity.getDirection_code()==InfoEntity.SIGNAL_DIRECTION_RL) {
			left_ImageView.setVisibility(ImageView.VISIBLE);
			right_ImageView.setVisibility(ImageView.VISIBLE);
			straight_ImageView.setVisibility(ImageView.VISIBLE);
		}else if (infoentity.getDirection_code()==InfoEntity.SIGNAL_DIRECTION_LEFT)  {
			left_ImageView.setVisibility(ImageView.GONE);
			right_ImageView.setVisibility(ImageView.GONE);
			straight_ImageView.setVisibility(ImageView.GONE);
		} else {
			left_ImageView.setVisibility(ImageView.GONE);
			right_ImageView.setVisibility(ImageView.GONE);
			straight_ImageView.setVisibility(ImageView.VISIBLE);
		}
    	
    	count_TextView.setText(""+infoentity.getSignal_time());
    	
    }
    @Override
    public void onPause(){
    	if (textToSpeech!=null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
		}
    	super.onPause();
    }
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (acceptThread!=null) {
			acceptThread.cancel();
		}

		if (mConnectedThread!=null) {
			mConnectedThread.cancel();
		}
		
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
}
