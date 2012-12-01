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

import com.example.takenumbersystem.MainActivity.GetMyItemAdapter;

import android.R.integer;
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
							
								AlertDialog.Builder alert = new AlertDialog.Builder(Type2ItemList.this);

								LayoutInflater inflater = LayoutInflater.from(Type2ItemList.this);
								View AlertView=inflater.inflate(R.layout.inputitemnum,null);
								
								TextView LimitQuantityNum=(TextView)AlertView.findViewById(R.id.LimitQuantityNum);
								LimitQuantityNum.setText("(數量上限:"+ItemList.get(arg2).get("LimitQuantity")+")");
								
								Button Num1=(Button)AlertView.findViewById(R.id.Num1);
								ImplementInput II =new ImplementInput("1",AlertView);
								Num1.setOnClickListener(II);
								
								Button Num2=(Button)AlertView.findViewById(R.id.Num2);
								II =new ImplementInput("2",AlertView);
								Num2.setOnClickListener(II);
								
								Button Num3=(Button)AlertView.findViewById(R.id.Num3);
								II =new ImplementInput("3",AlertView);
								Num3.setOnClickListener(II);
								
								Button Num4=(Button)AlertView.findViewById(R.id.Num4);
								II =new ImplementInput("4",AlertView);
								Num4.setOnClickListener(II);
								
								Button Num5=(Button)AlertView.findViewById(R.id.Num5);
								II =new ImplementInput("5",AlertView);
								Num5.setOnClickListener(II);
								
								
								Button Num6=(Button)AlertView.findViewById(R.id.Num6);
								II =new ImplementInput("6",AlertView);
								Num6.setOnClickListener(II);
								
								Button Num7=(Button)AlertView.findViewById(R.id.Num7);
								II =new ImplementInput("7",AlertView);
								Num7.setOnClickListener(II);
								
								Button Num8=(Button)AlertView.findViewById(R.id.Num8);
								II =new ImplementInput("8",AlertView);
								Num8.setOnClickListener(II);
								
								Button Num9=(Button)AlertView.findViewById(R.id.Num9);
								II =new ImplementInput("9",AlertView);
								Num9.setOnClickListener(II);
								
								Button Num0=(Button)AlertView.findViewById(R.id.Num0);
								II =new ImplementInput("0",AlertView);
								Num0.setOnClickListener(II);
								
								Button Clear=(Button)AlertView.findViewById(R.id.Clear);
								II=new ImplementInput("Clear",AlertView);
								Clear.setOnClickListener(II);
								
								Button Submit=(Button)AlertView.findViewById(R.id.Submit);
								alert.setView(AlertView);
								AlertDialog alertDialog = alert.create();

								II=new ImplementInput("Submit",AlertView);
								II.setdata(arg2,alertDialog);
								Submit.setOnClickListener(II);
								
								alertDialog.show();
								//alert.show();		
									
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
				AlertDialog.Builder Dialog=new AlertDialog.Builder(Type2ItemList.this);
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.getmyitemlayout, null);
				
				int totalcost=0;
				
				ArrayList<HashMap<String,String>> TempItemList=new ArrayList<HashMap<String,String>>();
				for(int i=0;i<ItemList.size();i++){
					if(!ItemList.get(i).get("NeedValue").equals("0"))
					{
						HashMap<String, String> temp = ItemList.get(i);
						//temp.put("ItemName", ItemList.get(i).get("ItemName"));
						//temp.put("NeedValue", ItemList.get(i).get("NeedValue"));
						//temp.put("TakenItemID", ItemList.get(i).get("TakenItemID"));
						//temp.put("Price", ItemList.get(i).get("Price"));
						TempItemList.add(temp);
						int eachcost=Integer.parseInt(ItemList.get(i).get("Price"));
						int needvalue=Integer.parseInt(ItemList.get(i).get("NeedValue"));
						totalcost+=eachcost*needvalue;
						
					}
					
				}
				
		
				ListView GetMyItemListView=(ListView)layout.findViewById(R.id.GetMyItemListView);
				GetMyItemAdapter GMIA=new GetMyItemAdapter(Type2ItemList.this,TempItemList);
				GetMyItemListView.setAdapter(GMIA);
		
				TextView TotalCost=(TextView)layout.findViewById(R.id.TotoalCost);
				TotalCost.setText(String.valueOf(totalcost));
				
				Dialog.setView(layout);
				Dialog.setPositiveButton("確認", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Thread SubmitDataThread=new Thread(SubmitData);
						SubmitDataThread.start();
						
					}
				});
				
				Dialog.setNegativeButton("取消", new OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
				
				Dialog.show();
				
	
				
			}
		});
        
        Thread GILThread=new Thread(GetItemList);
        GILThread.start();
        
    }
    
    class ImplementInput implements android.view.View.OnClickListener
    {	
    	
    	String Num;
    	View AlertView;
    	int position;
    	AlertDialog alertDialog;
    	public ImplementInput(String Num,View AlertView)
    	{
    		this.Num=Num;
    		this.AlertView=AlertView;
    		
    	}
    	public void setdata(int position,AlertDialog alertDialog)
    	{
    		this.position=position;
    		this.alertDialog=alertDialog;
    		
    	}
    	
    	
		public void onClick(View v) 
		{	
			if(Num.equals("Clear"))
				{
					Clear();
					return;
				}
			if(Num.equals("Submit"))
			{
				Submit();
				
				return;
				
			}
			
			TextView NumText=(TextView)AlertView.findViewById(R.id.NumberInputText);
			
			String Text=NumText.getText().toString();
			String FinalText;
			if(Text.equals("0"))
				FinalText=Num;
			else
				FinalText=Text+Num;
			
			NumText.setText(FinalText);
			
		}
    	public void Submit()
    	{	
    		TextView NumText=(TextView)AlertView.findViewById(R.id.NumberInputText);
    		String Num=NumText.getText().toString();
    		String LimitQuantity=ItemList.get(position).get("LimitQuantity");
    		
    		int IntLimitQuantity=Integer.parseInt(LimitQuantity);
    		int IntNum=Integer.parseInt(Num);
    		
    		
    		if(IntNum<=IntLimitQuantity){
    			ItemList.get(position).put("NeedValue", Num);
    			Type2ItemList.this.ILA.notifyDataSetChanged();
    			alertDialog.dismiss();
    		}
    		else{
    			Toast.makeText(Type2ItemList.this,"此商品數量上限:"+LimitQuantity,Toast.LENGTH_SHORT).show();
    			
    			
    		}
    		
    		
    		
    	}
		
		
		public void Clear()
		{
			TextView NumText=(TextView)AlertView.findViewById(R.id.NumberInputText);
			String Text=NumText.getText().toString();
			String FinalText=Text.substring(0,Text.length()-1);
			if(FinalText.equals(""))
			{
				FinalText="0";
				
			}
			
			NumText.setText(FinalText);
			
		}
		
		
    	
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
				String returnString="";
				if(result.equals("1")){
					returnString="抽號成功";
					
					
				}
				else if(result.equals("-2")){
					
					returnString="抽號失敗,請勿重複抽號";
					
				}
				else{
					returnString="抽號失敗";
				}
				Message m=MainThreadHandler.obtainMessage(1,returnString);
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
				
				String key[]={"ItemName","Price","TakenItemID","LimitQuantity"};
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
	
	class GetMyItemAdapter extends BaseAdapter
	{
		private Context context;
		private ArrayList<HashMap<String,String>> ItemList;
		
		
		public GetMyItemAdapter(Context mcontext,ArrayList<HashMap<String,String>> mItemList)
		{
			context=mcontext;
			ItemList=mItemList;

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
			View ItemView=layoutinflater.inflate(R.layout.myitemlayout, null);
			
			//String key[]={"NeedValue","TakenItemID","ItemName","Price"};
			
			TextView ItemName=(TextView)ItemView.findViewById(R.id.ItemName);
			ItemName.setText(ItemList.get(arg0).get("ItemName"));
			
			TextView ItemCount=(TextView)ItemView.findViewById(R.id.ItemCount);
			ItemCount.setText("共"+ItemList.get(arg0).get("NeedValue")+"個");
			
			TextView EachPrice=(TextView)ItemView.findViewById(R.id.EachPrice);
			EachPrice.setText(ItemList.get(arg0).get("Price"));
			
			return ItemView;
		}
		
		
	}
    
}
