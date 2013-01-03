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
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

public class SelectItemActivity extends Activity {
	
	private Handler main_thread_handler=new Handler();
	private Handler threadhandler;
	private HandlerThread mthread;
	private String SerialNumbers="";
	public ArrayList<HashMap<String,String>> item_list=null;
    private Runnable set_item_list=new Runnable()
    {

		public void run() 
		{	
			ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("SerialNumbers",SerialNumbers));
			String get_item_list="";
			try {
					get_item_list=connect_to_server("/project/mobilephone/get_item_list.php",nameValuePairs);
					if(get_item_list.equals("fail"))
						return;
					String key[]={"ID","Name","State","Value","Now_Value"};
					item_list=json_deconde(get_item_list,key);
					//Log.v("getitem",get_item_list);
					
				} 
			catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final ListView showitem=(ListView)findViewById(R.id.showitem);
			showitem.post(new Runnable() 
			{
				
				public void run() 
				{
					showitem.setAdapter(
							new SimpleAdapter( 
							SelectItemActivity.this, 
							item_list,
	 			    		R.layout.selectitemlistview,
	 			    		new String[] {"Name","Value","Now_Value"},
	 			    		new int[] { R.id.textView1,R.id.textView2,R.id.textView3 } ));
					
				}
			});
			showitem.setOnItemClickListener(new OnItemClickListener() 
			{
				public void onItemClick(AdapterView<?> arg0, View arg1,int position, long id) 
				{	
					ItemClickRunnable a=new ItemClickRunnable();
					a.setDate(item_list.get(position).get("ID"),SerialNumbers,MainActivity.UserIMEI);
					threadhandler.post(a);
					/*
					ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("ItemId",item_list.get(position).get("ID")));
					nameValuePairs.add(new BasicNameValuePair("SerialNumbers",SerialNumbers));
					nameValuePairs.add(new BasicNameValuePair("UserIMEI",MainActivity.UserIMEI));
					try {
						String result=connect_to_server("/project/mobilephone/take_number.php",nameValuePairs);
						Log.v("add", result);
						
						if(!result.equals("fail")&&!result.equals("MySQL Query Error"))
							{
								toast("你現在抽到的號碼是:"+result);
								setResult(RESULT_OK);
								finish();
							}
						else
							{
								toast("抽號失敗");

							}
							
					} catch (ClientProtocolException e) {
						// TODO Auto-gednerated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
				}
				
			});
			
			
		}	
    	
		
		
		
    	
    };
	
	public void toast(String text)
	{
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

		
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);
        
        Intent thisIntent = this.getIntent();
        Bundle bundle = thisIntent.getExtras();
        SerialNumbers=bundle.getString("SerialNumbers");
        
        mthread=new HandlerThread("name");
        mthread.start();
        
        threadhandler=new Handler(mthread.getLooper());
        threadhandler.post(set_item_list);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_item, menu);
        return true;
    }
    
    public class ItemClickRunnable implements Runnable 
    {
    	private String ItemId,SerialNumbers,UserIMEI;
    	public void setDate(String getItemId,String getSerialNumbers,String getUserIMEI )
		{
    		ItemId=getItemId;
    		SerialNumbers=getSerialNumbers;
    		UserIMEI=getUserIMEI;
		}
		public void run() 
		{	
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SelectItemActivity.this);
			String phoneNubmer=preferences.getString("PhoneNumber", "");
			
			ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("ItemId",ItemId));
			nameValuePairs.add(new BasicNameValuePair("SerialNumbers",SerialNumbers));
			nameValuePairs.add(new BasicNameValuePair("UserIMEI",UserIMEI));
			nameValuePairs.add(new BasicNameValuePair("phoneNubmer",phoneNubmer));
			
			String result;
			try {
				result = connect_to_server("/project/mobilephone/take_number.php",nameValuePairs);
				
				if(result.equals("-1")){
					toast("抽號失敗,已有相同商品在抽號等待");
				}
				
				else if(!result.equals("fail")&&!result.equals("MySQL Query Error"))
					{
						toast("你現在抽到的號碼是:"+result);
						setResult(RESULT_OK);
						SelectItemActivity.this.finish();
					}
				else
					{
						toast("抽號失敗");

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
