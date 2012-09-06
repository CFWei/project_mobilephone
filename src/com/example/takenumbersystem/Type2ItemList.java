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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class Type2ItemList extends Activity {
	
	String SerialNumbers;
	private Handler MainThreadHandler;
	public ArrayList<HashMap<String,String>> ItemList=null;
	ItemListAdapter ILA;
   
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type2_item_list);
        
        Intent thisIntent = this.getIntent();
        Bundle bundle = thisIntent.getExtras();
        SerialNumbers=bundle.getString("SerialNumbers");
        
        MainThreadHandler=new Handler(){

			@Override
			public void handleMessage(Message msg) 
			{
				
				super.handleMessage(msg);
				String MsgString = (String)msg.obj;
				switch(msg.what)
				{
					case 1:
						Toast.makeText(Type2ItemList.this, MsgString, Toast.LENGTH_SHORT).show();
						break;
					case 2:
						ListView List = (ListView) findViewById(R.id.Type2ItemListView);
						ILA=new ItemListAdapter(Type2ItemList.this,ItemList);
						List.setAdapter(ILA);
						
						List.setOnItemClickListener(new OnItemClickListener() {

							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) 
							{
								final int position=arg2;
								AlertDialog.Builder alert = new AlertDialog.Builder(Type2ItemList.this);
								alert.setTitle("商品:"+ItemList.get(position).get("ItemName"));
								alert.setMessage("請輸入需要的個數");
								final EditText input = new EditText(Type2ItemList.this);
								input.setInputType(0x00000002);
								alert.setView(input);
							
								alert.setPositiveButton("確認", new OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) 
									{
										 String value = input.getText().toString();
										 ItemList.get(position).put("NeedValue",value);
										 ILA.notifyDataSetChanged();
										 
									}
								
								});
								alert.show();		
									
							}
							
							
						});
						break;
					case 3:
						Type2ItemList.this.finish();
						break;
				
				}
			}
        	
        	
        	
        };
        
        Button Submit=(Button)findViewById(R.id.SubmitButton);
        
        Submit.setOnClickListener(new Button.OnClickListener() {
			
			public void onClick(View v) 
			{	
				Thread SubmitDataThread=new Thread(SubmitData);
				SubmitDataThread.start();
				
			}
		});
        
        Thread GILThread=new Thread(GetItemList);
        GILThread.start();
        
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_type2_item_list, menu);
        return true;
    }
    
    Runnable SubmitData=new Runnable() 
    {
		
		public void run() 
		{	
			ArrayList<HashMap<String,String>> SendList=new ArrayList<HashMap<String,String>>();
			JSONObject jsonList=new JSONObject();
			for(int i=0;i<ItemList.size();i++)
			{
				if(!ItemList.get(i).get("NeedValue").equals("0"))
				{
					JSONObject temp = new JSONObject();
					try {
						temp.put("ItemName", ItemList.get(i).get("ItemName"));
						temp.put("NeedValue", ItemList.get(i).get("NeedValue"));
						temp.put("TakenItemID", ItemList.get(i).get("TakenItemID"));
						temp.put("Price", ItemList.get(i).get("Price"));
						jsonList.put(String.valueOf(i), temp);
					} catch (JSONException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			}
		
			ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("SerialNumbers",SerialNumbers));
			nameValuePairs.add(new BasicNameValuePair("CustomItemList",jsonList.toString()));
			nameValuePairs.add(new BasicNameValuePair("UserIMEI",MainActivity.UserIMEI));
			try {
				String result=connect_to_server("/project/mobilephone/Type2TakeNumber.php",nameValuePairs);
				Message m=MainThreadHandler.obtainMessage(1,"抽號成功");
				MainThreadHandler.sendMessage(m);
				
				m=MainThreadHandler.obtainMessage(3);
				MainThreadHandler.sendMessage(m);
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
	};
    
    
    
    public class ItemListAdapter extends BaseAdapter 
    {
    	private Context context;
		private ArrayList<HashMap<String,String>> ItemList;
		
		public ItemListAdapter( Context context,ArrayList<HashMap<String,String>> ItemList)
		{
			this.context=context;
			this.ItemList=ItemList;
			
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return ItemList.size();
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			LayoutInflater layoutinflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View ItemView=layoutinflater.inflate(R.layout.type2item, null);
			
			TextView ItemName=(TextView)ItemView.findViewById(R.id.ItemName);
			ItemName.setText(ItemList.get(arg0).get("ItemName"));
			
			TextView Price=(TextView)ItemView.findViewById(R.id.EachPriceValue);
			Price.setText(ItemList.get(arg0).get("Price"));
			
			TextView NeedValue=(TextView)ItemView.findViewById(R.id.NeedValue);
			NeedValue.setText(ItemList.get(arg0).get("NeedValue"));
			
			if(!ItemList.get(arg0).get("NeedValue").equals("0"))
			{
				
				ItemView.setBackgroundColor(Color.YELLOW);
				
			}
			
			String TotalDollar=String.valueOf((Integer.parseInt((ItemList.get(arg0).get("Price")))*Integer.parseInt(ItemList.get(arg0).get("NeedValue"))));
			TextView TotalDollarValue=(TextView)ItemView.findViewById(R.id.TotalDollar);
			TotalDollarValue.setText(TotalDollar);
			
			return ItemView;
		}
    	
    	
    	
    }
    
    Runnable GetItemList=new Runnable() {
		
		public void run() {
			
			try {
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("SerialNumbers",SerialNumbers));
				String result=connect_to_server("/project/mobilephone/Type2GetItemList.php",nameValuePairs);
				
				String key[]={"ItemName","Price","TakenItemID"};
				ItemList=json_deconde(result,key);
				
				for(int i=0;i<ItemList.size();i++)
				{
					ItemList.get(i).put("NeedValue","0");
				}
				
				Message m=MainThreadHandler.obtainMessage(2);
				MainThreadHandler.sendMessage(m);
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
