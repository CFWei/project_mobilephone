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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
	
public class LookUpChangeNumberPage extends Activity {
	private String ItemID;
	private String StoreID;
	private Handler threadhandler;
	ArrayList<HashMap<String,String>> ChangeNumberList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_up_change_number_page);
        Intent thisIntent = this.getIntent();
        Bundle bundle = thisIntent.getExtras();
        ItemID=bundle.getString("ItemID");
        StoreID=bundle.getString("StoreID");
        
        threadhandler=new Handler()
        {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what)
				{
					case 1:
						ListView ChangeNumberListView=(ListView)findViewById(R.id.ChangeNumberListView);
						ChangeNumberListView.setAdapter(new SimpleAdapter(
														LookUpChangeNumberPage.this,
														ChangeNumberList, 
														R.layout.changenumberlist, 
														new String[] { "number","CustomID" }, 
														new int []{ R.id.NumberValue, R.id.OwnerValue}));
						
						ChangeNumberListView.setOnItemClickListener(new OnItemClickListener() {

							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) 
							{
								
								AlertDialog.Builder builder=new AlertDialog.Builder(LookUpChangeNumberPage.this);
								builder.setTitle("換號確認");
								builder.setMessage("即將送出對 "+ChangeNumberList.get(arg2).get("number")+" 號的換號申請");
								final int position=arg2;
								builder.setPositiveButton("確認", new OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) 
									{
										SendChangeNumberRequest SCNR=new SendChangeNumberRequest();
										SCNR.setData(position);
										Thread SendChangeNumberRequestThread=new Thread(SCNR);
										SendChangeNumberRequestThread.start();
										
									}
								});
								builder.setNegativeButton("取消", new OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										
									}
								});
								AlertDialog alert = builder.create();
					    		alert.show();
								
							}
							
						});
						break;
					case 2:
						Toast.makeText(LookUpChangeNumberPage.this, "成功發送換號請求", Toast.LENGTH_SHORT).show();
						LookUpChangeNumberPage.this.finish();
						break;
				
				}
			}
        	
        	
        	
        	
        };
        
        
        
    }
    
    @Override
	protected void onResume() 
    {
		// TODO Auto-generated method stub
		super.onResume();
		Thread GetChangeNumberListThread=new Thread(GetChangeNumberList);
		GetChangeNumberListThread.start();
	}
    
    private class SendChangeNumberRequest implements Runnable
    {
    	int position;
    	public void setData(int position)
    	{
    		this.position=position;
    	}
    	
		public void run() 
		{
			try {
				String MatchID=ChangeNumberList.get(position).get("CustomID");
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
				nameValuePairs.add(new BasicNameValuePair("Store",StoreID));
				nameValuePairs.add(new BasicNameValuePair("MatchID",MatchID));
				nameValuePairs.add(new BasicNameValuePair("CustomID",MainActivity.UserIMEI));
				String result=connect_to_server("/project/mobilephone/SendChangeNumberRequest.php",nameValuePairs);
				if(result.equals("1"))
				{
					Log.v("debug", result);
					Message m=threadhandler.obtainMessage(2);
					threadhandler.sendMessage(m);
					
				}
				
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    	
    	
    }
    
    private Runnable GetChangeNumberList=new Runnable() 
    {	
		public void run() 
		{

	    	try {
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
				nameValuePairs.add(new BasicNameValuePair("StoreID",StoreID));
		    	nameValuePairs.add(new BasicNameValuePair("CustomID",MainActivity.UserIMEI));

				String result=connect_to_server("/project/mobilephone/LookUpChangeNumberList.php",nameValuePairs);
				//Log.v("debug", result);
				if(result.equals("-1"))
				{
					
					
				}
				else if(!result.equals("null"))
				{
					Log.v("debug", result);
					String[] key={"CustomID","number"};
					ChangeNumberList=json_deconde(result, key);
					Message m=threadhandler.obtainMessage(1);
					threadhandler.sendMessage(m);
				}
				else
				{
					
					
					
				}
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
			
		}
	};

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_look_up_change_number_page, menu);
        return true;
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
    
    
}
