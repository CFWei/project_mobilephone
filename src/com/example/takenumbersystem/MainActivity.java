package com.example.takenumbersystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {
	static String ServerURL="http://192.168.20.161/";
	public static String UserIMEI;
	public ArrayList<HashMap<String,String>> item_list=null;
	private Handler main_thread_handler=new Handler();
	private Handler threadhandler;
	private HandlerThread mthread;
	public boolean connect_status=false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(connect_status=check_connect_status())
        	UserIMEI=getIMEI();
        
        Button takenumber=(Button)findViewById(R.id.takenumber);
        takenumber.setOnClickListener(this);
        
    }
    
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(connect_status)
			{
				mthread=new HandlerThread("name");
		        mthread.start();
		        
		        threadhandler=new Handler(mthread.getLooper());
		        threadhandler.post(load_item);
			}
		
		
	}
    


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(threadhandler!=null)
				threadhandler.removeCallbacks(load_item);
		if(mthread!=null)
				mthread.quit();
		if(main_thread_handler!=null)
			main_thread_handler.removeCallbacks(update_value);
		

	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    

    public void onClick(View arg0) 
    {
    	if(arg0.getId()==R.id.takenumber)
    		{
    			Intent intent = new Intent();
    			intent.setClass(this,TakeNumberActivity.class);
    			startActivity(intent);	
    		
    		}
    		
		
	}
    
	private Runnable load_item=new Runnable()
    {	
    	private SimpleAdapter adapter;
    	public void run()
    	{	
           
			try {
			    	ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
			    	nameValuePairs.add(new BasicNameValuePair("custom_id",UserIMEI));
			    	String get_item_list;
					get_item_list = connect_to_server("/project/mobilephone/check_item.php",nameValuePairs);
					String key[]={"custom_id","store","item","number","StoreName","ItemName"};
					item_list=json_deconde(get_item_list,key);
			    } catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    			
			 if(item_list!=null)
			 {
				 ListView list = (ListView) findViewById(R.id.listView1);
			     list.post(new Runnable()
			     			{
			    	 			public void run() 
			    	 			{
			    	 				ListView list = (ListView) findViewById(R.id.listView1);
			    	 				
			    	 				list.setAdapter(new SimpleAdapter( 
			    	 						 MainActivity.this, 
			    	 			    		 item_list,
			    	 			    		 R.layout.waitingitemistview,
			    	 			    		 new String[] { "StoreName","number" },
			    	 			    		 new int[] { R.id.textView1, R.id.textView3 } ));
			    	 			}
			     			});
			     
			     main_thread_handler.postDelayed(update_value, 2000);
			     
			 }
    		
    	}
    	
    };
    
    private Runnable update_value=new Runnable()
    {

		public void run() 
		{	
			try {
				 	for(int i=0;i<item_list.size();i++)
				 	{
						ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("store",item_list.get(0).get("store")));
						nameValuePairs.add(new BasicNameValuePair("item",item_list.get(0).get("item")));
						String result=connect_to_server("/project/mobilephone/update_value.php",nameValuePairs);
						Log.v("TEST", result);
						
						ListView list=(ListView) findViewById(R.id.listView1);
						int first_position=list.getFirstVisiblePosition();
						View v=list.getChildAt(i-first_position);
						TextView text=(TextView)v.findViewById(R.id.textView2);
						text.setText(result);
				 	}
					
					main_thread_handler.postDelayed(this, 2000);
			} 
			catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    	
    	
    };
    
    
    
    public String getIMEI()
    {
			
    	TelephonyManager telManager  = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    	
    	return telManager.getDeviceId();
    	
    }
    public boolean check_connect_status()
    {	
    	try {
    			ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    			NetworkInfo network_info=cm.getActiveNetworkInfo();
    			if(network_info==null||network_info.isConnected()==false)
    				{
						
						Toast.makeText(this, "網路狀態：關閉", Toast.LENGTH_LONG).show();
    					return false;
					
    				}
    			else
    				{
    					Toast.makeText(this, "網路狀態：開啟", Toast.LENGTH_LONG).show();
    				}
    		
    		
				if(connect_to_server("/project/mobilephone/check_connect.php",null).equals("1"))
					{
						Toast.makeText(this, "可連接至主機", Toast.LENGTH_LONG).show();
						return true;
					}
				else
					{
						Toast.makeText(this, "not connect", Toast.LENGTH_SHORT).show();
						return false;
					}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return false;
    	
		
    	
    	
    	
    }
    
    public String connect_to_server(String program,ArrayList<NameValuePair> nameValuePairs) throws ClientProtocolException, IOException
    {	
    	//建立一個httpclient
    	HttpClient httpclient = new DefaultHttpClient();
    	//設定httppost的網址
    	HttpPost httppost = new HttpPost(ServerURL+program);
    	
    	//加入參數
    	if(nameValuePairs!=null)
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    	
		//發出httppost要求並接收回傳轉成字串
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"),8);
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) 
		{
			sb.append(line);
		}
		is.close();
		String result = sb.toString();
		
		return result;
    }
    
    public ArrayList<HashMap<String,String>> json_deconde(String jsonString,String[] key) throws JSONException
    {	
    	ArrayList<HashMap<String,String>> item = new ArrayList<HashMap<String,String>>();
    	JSONArray jArray = new JSONArray(jsonString);
    	for(int i = 0;i<jArray.length();i++)
		{	
    		 HashMap<String,String> temp = new HashMap<String,String>();
	     	 JSONObject json_data = jArray.getJSONObject(i); 
	     	 for(int j=0;j<key.length;j++)
	     	 	temp.put(key[j], json_data.getString(key[j]));
	     	 item.add(temp);
	     	 //Toast.makeText(this, json_data.getString(key[2]), Toast.LENGTH_SHORT).show();
		}
		
	
		return item;
    	
    }
    
}
