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


import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

public class TakeNumberActivity extends Activity implements LocationListener {
	private LocationManager locationManager;
	public ArrayList<HashMap<String,String>> store_list=null;
	private static final int getresult=1;
	private Handler threadhandler;
	private HandlerThread mthread;
	ProgressDialog myDialog ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_number);
        
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        	{	
        		Criteria criteria = new Criteria();
    			criteria.setAccuracy(criteria.ACCURACY_MEDIUM);
    			
    			String bestProvider = locationManager.getBestProvider(criteria, true);
    			
        		locationManager.requestLocationUpdates(bestProvider,0,0,this);
        	}
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        	{
        		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        	}
        else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        	{
        		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        	}
        else {
				Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
			  }
        myDialog= ProgressDialog.show(this,"抽號系統","讀取位置中",true,true,new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(TakeNumberActivity.this, "取消抽號程序", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
        	
    }
    
   
    @Override
	protected void onDestroy() {
		super.onDestroy();
		
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_take_number, menu);
        return true;
    }
	
	
	
    class location_message
	{
    	Double longitude;
    	Double latitude;
	}
    
    public location_message SetLocation(Double longitude,Double latitude)
    {
		//設定return value
		location_message return_value=new location_message();
		return_value.longitude=longitude;
		return_value.latitude=latitude;
    	return return_value;
    }
    
    
	public void onLocationChanged(Location location) 
	{
		Double longitude = location.getLongitude();	//取得經度
		Double latitude = location.getLatitude();//取得緯度
		
		myDialog.dismiss();
		
		myDialog= ProgressDialog.show(this,"抽號系統","搜尋附近店家",true,true,new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(TakeNumberActivity.this, "取消抽號程序", Toast.LENGTH_SHORT).show();
				finish();
				
			}
		});
		mthread=new HandlerThread("name");
		mthread.start();
		       
		threadhandler=new Handler(mthread.getLooper());
		GetStoreListRunnable a=new GetStoreListRunnable();
		a.setData(longitude, latitude);
		threadhandler.post(a);
		locationManager.removeUpdates(this);
	}
	/*
	public void debug(location_message location)
	{
		ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("logitude",String.valueOf(location.longitude)));
		nameValuePairs.add(new BasicNameValuePair("latitude",String.valueOf(location.latitude)));
		try {
			String get_store_list=connect_to_server("/project/mobilephone/get_near_store.php",nameValuePairs);
			Log.v("debug",get_store_list);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(this, "hello",Toast.LENGTH_SHORT).show();
	}
	*/
	public class GetStoreListRunnable implements Runnable
    {
		private double longitude,latitude;
		public void setData(double get_longitude,double get_latitude)
		{
			longitude=get_longitude;
			latitude=get_latitude;
			
		}
		public void run() {
			location_message parameter=SetLocation(longitude,latitude);
			get_store_list(parameter);
		
		}
		
		
		
    };
	
	
	
	public void get_store_list(location_message location) 
	{	

		ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("logitude",String.valueOf(location.longitude)));
		nameValuePairs.add(new BasicNameValuePair("latitude",String.valueOf(location.latitude)));
		//從server上取得附近店家名單
		String get_store_list="";
		try {
				get_store_list=connect_to_server("/project/mobilephone/get_near_store.php",nameValuePairs);
				if(get_store_list.equals("null"))
				{
					Toast.makeText(this, "找不到符合條件的店家", Toast.LENGTH_SHORT).show();
					myDialog.dismiss();
					return;
					
				}
				String key[]={"StoreName","StoreAddress","StoreTelephone","GPS_Longitude","GPS_Latitude","SerialNumbers","distance"};
				
				store_list=json_deconde(get_store_list,key);
				for(int i=0;i<store_list.size();i++)
					{	
						for(int j=0;j<key.length;j++)
						{
						//	Log.v("item", store_list.get(i).get(key[j]));
						}
					}
				if(store_list!=null)
				{
					final ListView store_list_view=(ListView)findViewById(R.id.store_list_view);
					store_list_view.post(new Runnable()
					{

						public void run() 
						{	
							store_list_view.setAdapter(
									new SimpleAdapter( 
									TakeNumberActivity.this, 
									store_list,
	    	 			    		R.layout.storelistview,
	    	 			    		new String[] { "StoreName","StoreAddress","distance"},
	    	 			    		new int[] { R.id.textView1,  R.id.textView2,R.id.textView3} ));
							
						}
						
					});
					//設定onclick觸發事件
					store_list_view.setOnItemClickListener(new OnItemClickListener() 
					{
					public void onItemClick(AdapterView<?> parent,View view, int position, long id) 
						{	
							//設定intent轉換
							Intent intent = new Intent();
							intent.setClass(TakeNumberActivity.this, SelectItemActivity.class);
							
							//設定傳遞參數
							Bundle bundle = new Bundle();
							String passvalue=store_list.get(position).get("SerialNumbers");
							bundle.putString("SerialNumbers",passvalue);
							intent.putExtras(bundle);
							
							startActivityForResult(intent,getresult);
							
						}
					

					});
					
				}
				myDialog.dismiss();
			} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Toast.makeText(this, store_list, Toast.LENGTH_LONG).show();
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
			case getresult:
				finish();
			
		}
	}


	public String connect_to_server(String program,ArrayList<NameValuePair> nameValuePairs) throws ClientProtocolException, IOException
    {	
    	//建立一個httpclient
    	HttpClient httpclient = new DefaultHttpClient();
    	//設定httppost的網址
    	HttpPost httppost = new HttpPost(MainActivity.ServerURL+program);
    	
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
	
	
	

	
	
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
    
}
