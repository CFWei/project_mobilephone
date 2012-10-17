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


import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint({ "NewApi", "NewApi" })
public class MainActivity extends Activity implements OnClickListener,LocationListener{
	static String ServerURL="http://takeanumber.no-ip.info:800";
	private static final double EARTH_RADIUS = 6378137;
	public static String UserIMEI;
	public ArrayList<HashMap<String,String>> item_list=null;
	private Handler main_thread_handler;
	private Handler threadhandler;
	private HandlerThread mthread;
	private ItemAdapter itemadapter;
	private boolean ListViewSettingCheck=false;
	public boolean connect_status=false,location_status=false;
	ProgressDialog myDialog ;
	ArrayList<HashMap<String,String>> MyItemList;
	private LocationManager locationManager;
	
	//以下為音效參數
	private SoundPool soundPool;
	private int AlarmWave;
	private static final int SOUND_COUNT = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        soundPool = new SoundPool(SOUND_COUNT, AudioManager.STREAM_MUSIC,0);
        //參數:最大音效數,聲音類型,來源品質
        AlarmWave = this.soundPool.load(this,R.raw.doorbell,1);
        //將音樂讀取進來
        
        UserIMEI=getIMEI();
        main_thread_handler=new Handler()
        {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String MsgString = (String)msg.obj;
				switch(msg.what)
				{
					case 1:
						Toast.makeText(MainActivity.this,MsgString , Toast.LENGTH_SHORT).show();
						break;
					case 2: 
						ListView list=(ListView) findViewById(R.id.listView1);
						if(ListViewSettingCheck)
							((ItemAdapter)list.getAdapter()).notifyDataSetChanged();
						
				    	break;
					case 3:
						ProgressBar MainActivityProgressBar=(ProgressBar)findViewById(R.id.MainActivityProgressBar);
						MainActivityProgressBar.setVisibility(ProgressBar.INVISIBLE);
						break;
					case 4:
						TextView Message=(TextView)findViewById(R.id.MainActivityMessage);
						Message.setText(MsgString);
						break;
					case 5:
						Bundle bundle = new Bundle();
						int ItemPosition=Integer.parseInt(MsgString);
						bundle.putString("ItemID",item_list.get(ItemPosition).get("item"));
						bundle.putString("StoreID",item_list.get(ItemPosition).get("store")); 
						Intent intent = new Intent();
						intent.setClass(MainActivity.this,LookUpChangeNumberPage.class);
						intent.putExtras(bundle);
						startActivity(intent);
						break;
					case 6:
						ListView List = (ListView) findViewById(R.id.listView1);
    	 				itemadapter=new ItemAdapter(MainActivity.this,item_list);
    	 				List.setAdapter(itemadapter);
    	 				ListViewSettingCheck=true;
						break;
					case 7:
						
						int position=Integer.parseInt(MsgString);
						AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
						builder.setTitle("換號成功");
						builder.setMessage("商品: "+item_list.get(position).get("ItemName")+" 換號成功");
						builder.setPositiveButton("確認", new AlertDialog.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) 
							{
								
								
							}
						});
						AlertDialog alert = builder.create();
			    		//alert.show();
			    		 
			    		
						break;
					case 8:
						AlertDialog.Builder Dialog=new AlertDialog.Builder(MainActivity.this);
						
						LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
						View layout = inflater.inflate(R.layout.getmyitemlayout, null);
						
						ListView GetMyItemListView=(ListView)layout.findViewById(R.id.GetMyItemListView);
						GetMyItemAdapter GMIA=new GetMyItemAdapter(MainActivity.this,MyItemList);
						GetMyItemListView.setAdapter(GMIA);
						
						Dialog.setView(layout);
						Dialog.show();
						
						
						break;
				}
			}
        };
        
        ListView list=(ListView) findViewById(R.id.listView1);
        registerForContextMenu(list);
        
    }
    
    @Override
	protected void onResume() 
    {
    	
		// TODO Auto-generated method stub
		super.onResume();
		
		//myDialog= ProgressDialog.show(this,"抽號系統","與server聯繫中 請稍候",true);
		mthread=new HandlerThread("name");
		mthread.start();
		       
		threadhandler=new Handler(mthread.getLooper());
	
		//threadhandler.post(check_connect_status_runnable);
		
		Thread CheckConnectThread=new Thread(check_connect_status_runnable);
		CheckConnectThread.start();
		
		TurnOnLocationListener();
		
		Message m=main_thread_handler.obtainMessage(4,"");
 		main_thread_handler.sendMessage(m);
		
	}
    
	@Override
	protected void onPause() 
	{	
		super.onPause();
	
		if(threadhandler!=null)
			{
				threadhandler.removeCallbacks(load_item);
				threadhandler.removeCallbacks(update_value);
			}
		if(mthread!=null)
			mthread.quit();
		
		if(location_status==true)
			{
				locationManager.removeUpdates(this);
				location_status=false;
			}

		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		
		if(!connect_status)
				threadhandler.removeCallbacks(check_connect_status_runnable);
		if(threadhandler!=null)	
			{
				threadhandler.removeCallbacks(load_item);
				threadhandler.removeCallbacks(update_value);
				
			}
		/*
		if(mthread!=null)
				mthread.quit();
				*/
		/*
		if(location_status==true)
		{
			locationManager.removeUpdates(this);
			location_status=false;
		}
		*/

	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		int ItemPosition = info.position;
		
		if(item_list.get(ItemPosition).get("StoreType").equals("2"))
			menu.add(0, 3, 0, "顯示我的商品");
		
		if(item_list.get(ItemPosition).get("ChangeNumberCheck").equals("0"))
			menu.add(0, 0, 0, "顯示換號清單");
		else if(item_list.get(ItemPosition).get("ChangeNumberCheck").equals("1"))
			{
				menu.add(0, 1, 0, "取消換號配對");
			
			}
		else if(item_list.get(ItemPosition).get("ChangeNumberCheck").equals("2"))
			{
				menu.add(0, 1, 1, "取消換號配對");
				menu.add(0, 2, 0, "確認換號配對");
			}	
		else if(item_list.get(ItemPosition).get("ChangeNumberCheck").equals("3"))
		{}
		else
			menu.add(0, 0, 0, "進入換號系統");
		
		

	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		 
		 //擷取是哪個位置的Item被select
		 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		 final int ItemPosition = info.position;
		 
		
		switch(item.getItemId())
		{
			case 0:
				CheckChangeNumberState CCNS=new CheckChangeNumberState();
				CCNS.setdata(ItemPosition);
				Thread CheckChangeNumberStateThread=new Thread(CCNS);
				CheckChangeNumberStateThread.start();
				break;
			case 1:
				ImplementChangeNumber ICN=new ImplementChangeNumber();
				ICN.setData(1,ItemPosition);
				Thread ICNThread=new Thread(ICN);
				ICNThread.start();
				break;
			case 2:
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("換號確認");
				builder.setMessage("你確定要換號?");
				builder.setPositiveButton("確認", new AlertDialog.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) 
					{
						ImplementChangeNumber ICN=new ImplementChangeNumber();
						ICN.setData(2,ItemPosition);
						Thread ICNThread=new Thread(ICN);
						ICNThread.start();
						
					}
				});
				builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
				AlertDialog alert = builder.create();
	    		alert.show();
				
				break;
			case 3:
				GetMyItem GMI=new GetMyItem(ItemPosition);
				Thread GMIThread=new Thread(GMI);
				GMIThread.start();
				break;
		}
		
		
		return super.onContextItemSelected(item);
	}
	
	private class GetMyItem implements Runnable
	{	
		private int ItemPosition;
		
		public GetMyItem(int ItemPosition)
		{
			this.ItemPosition=ItemPosition;
			
		}
		public void run() 
		{
			
			try {
				String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
				
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
				nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
				nameValuePairs.add(new BasicNameValuePair("Store",Store));
		
				String result=connect_to_server("/project/mobilephone/GetMyItem.php",nameValuePairs);
				
				String key[]={"NeedValue","TakenItemID","ItemName","Price"};
				MyItemList=json_deconde(result,key);
				
				Message m=main_thread_handler.obtainMessage(8);
				main_thread_handler.sendMessage(m);
				
				
				
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
		
	}
	
	
	
	private class ImplementChangeNumber implements Runnable
	{	
		
		//1：取消配對 //2:接受配對 //3:完成換號並移除
		private int choose;
		private int ItemPosition;
		
		public void setData(int choose,int ItemPosition)
		{
			this.choose=choose;
			this.ItemPosition=ItemPosition;
		}
		
		public void run() 
		{
			if(choose==1)
			{
				CancelChangeNumber();
			}
			if(choose==2)
			{
				AcceptChangeNumber();
				
			}
			if(choose==3)
			{
				DeleteChangeNumber();
				
			}
			
		}
		public void DeleteChangeNumber()
		{
			try {
				String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
						
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
				nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
				nameValuePairs.add(new BasicNameValuePair("Store",Store));
				String result=connect_to_server("/project/mobilephone/DeleteChangeNumber.php",nameValuePairs);
				
				
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		public void AcceptChangeNumber()
		{
			
	    	
	    	try {
	    		String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
				
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
		       	nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
		    	nameValuePairs.add(new BasicNameValuePair("Store",Store));
				String result=connect_to_server("/project/mobilephone/AcceptChangeNumber.php",nameValuePairs);
				if(!result.equals("-1"))
				{
					item_list.get(ItemPosition).put("number", result);
					
				}
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void CancelChangeNumber()
		{
			
	    	
	    	try {
	    		String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
				
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
		       	nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
		    	nameValuePairs.add(new BasicNameValuePair("Store",Store));
		    	
				String result=connect_to_server("/project/mobilephone/CancelChangeNumber.php",nameValuePairs);
				
				if(result.equals("1"))
				{
					Message m=main_thread_handler.obtainMessage(1,"取消換號配對成功");
					main_thread_handler.sendMessage(m);
				}
				else
				{
					Message m=main_thread_handler.obtainMessage(1,"取消換號配對失敗");
					main_thread_handler.sendMessage(m);
					
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
	private class CheckChangeNumberState implements Runnable
	{	
		private int ItemPosition;
		public void setdata(int ItemPosition)
		{
			this.ItemPosition=ItemPosition;
			
		}
		public void run() 
		{	
	    	try {
				String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
				
				ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
		       	nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
		    	nameValuePairs.add(new BasicNameValuePair("Store",Store));
				String result=connect_to_server("/project/mobilephone/CheckChangeNumberState.php",nameValuePairs);
				if(result.equals("0"))
				{
					MainActivity.this.runOnUiThread(new Runnable() 
					{
						public void run() 
						{
							ChangeNumber(ItemPosition);
							
						}
					});
					
				}
				else
				{
					if(item_list.get(ItemPosition).get("ChangeNumberCheck").equals("1")||item_list.get(ItemPosition).get("ChangeNumberCheck").equals("2"))
					{
						Message m=main_thread_handler.obtainMessage(1,"已進行配對,請稍候");
						main_thread_handler.sendMessage(m);
						
					}
					else	
					{	
						Message m=main_thread_handler.obtainMessage(5,Integer.toString(ItemPosition));
						main_thread_handler.sendMessage(m);
					}
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
	
	
	
	
	public void ChangeNumber(int ItemPosition)
	{
		final int Position=ItemPosition;
		Builder MyAlertDialog = new AlertDialog.Builder(this);
		MyAlertDialog.setTitle("換號系統");
		MyAlertDialog.setMessage("請選擇你要向前換號/向後換號");
		
		MyAlertDialog.setPositiveButton("向前",new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) 
			{	
				SendChangeNumberRequest SCNR=new SendChangeNumberRequest();
				SCNR.setdata(1, Position);
				Thread SendChangeNumberRequestThread=new Thread(SCNR);
				SendChangeNumberRequestThread.start();
				
			}
		} );
		
		MyAlertDialog.setNeutralButton("向後",new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) 
			{
				SendChangeNumberRequest SCNR=new SendChangeNumberRequest();
				SCNR.setdata(2, Position);
				Thread SendChangeNumberRequestThread=new Thread(SCNR);
				SendChangeNumberRequestThread.start();
				
			}
		} );
		
		MyAlertDialog.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
			
		
			}
		});
		MyAlertDialog.show();
		
	}
	
	
	private class SendChangeNumberRequest implements Runnable
	{
		private int Choose;
		private int ItemPosition;
		
		public void setdata(int Choose,int ItemPosition)
		{
			this.Choose=Choose;//1:向前 2:向後
			this.ItemPosition=ItemPosition;
			
		}
		public void run() 
		{
	    	try {
	    			
	    		String ItemID=item_list.get(ItemPosition).get("item");
				String Store=item_list.get(ItemPosition).get("store");
				
	    	 	ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
		    	nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
		    	nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
		    	nameValuePairs.add(new BasicNameValuePair("Store",Store));
		    	nameValuePairs.add(new BasicNameValuePair("Choose",Integer.toString(Choose)));
				String result=connect_to_server("/project/mobilephone/ChangeNumberRequest.php",nameValuePairs);
			
				Message m=main_thread_handler.obtainMessage(5,Integer.toString(ItemPosition));

				main_thread_handler.sendMessage(m);

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
	    	
		}
		
		
		
	} 
	
	//檢查連線狀態的Runnable
	private Runnable check_connect_status_runnable=new Runnable()
    {

		public void run() 
		{
			if(check_connect_status())
				{
					connect_status=true;
					
			        Button takenumber=(Button)findViewById(R.id.takenumber);
			        takenumber.setOnClickListener(MainActivity.this);
					
					Thread LoadItemThread=new Thread(load_item);
					LoadItemThread.start();
				}
			else
				{
					
				 	Message m=main_thread_handler.obtainMessage(3);
				 	main_thread_handler.sendMessage(m);
				 	/*
				 MainActivity.this.runOnUiThread(new Runnable(){

						public void run() {
							//myDialog.dismiss();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setTitle("抽號系統");
							
				    		builder.setMessage("無法連上server");
				    		DialogInterface.OnClickListener okclick=new DialogInterface.OnClickListener()
				    		  {

								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									
								}
				    			  
				    		  };
				    		  builder.setNeutralButton("確認", okclick);
				    		  AlertDialog alert = builder.create();
				    		  alert.show();
				    		
						}});
				 */
				}
		}		
    };

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
    //讀取個人物品清單
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
					//json decode 
					String key[]={"custom_id","store","item","number","StoreName","ItemName","GPS_Longitude","GPS_Latitude","StoreType"};
					if(get_item_list!=null && !get_item_list.equals("null"))
					{
						item_list=json_deconde(get_item_list,key);
					
						for(int i=0;i<item_list.size();i++)
						{
							item_list.get(i).put("Now_Value","");
							item_list.get(i).put("alert_text", "");
							item_list.get(i).put("Distance", "定位中");
							item_list.get(i).put("ChangeNumberCheck", "");
							
						}
						
						Message m=main_thread_handler.obtainMessage(6);
				 		main_thread_handler.sendMessage(m);
						/*
						 ListView list = (ListView) findViewById(R.id.listView1);
					     list.post(new Runnable()
					     			{
					    	 			public void run() 
					    	 			{	
					    	 				ListView list = (ListView) findViewById(R.id.listView1);
					    	 				itemadapter=new ItemAdapter(item_list);
					    	 				list.setAdapter(itemadapter);
					    	 				
					    	 				//list.setAdapter(new SimpleAdapter( 
					    	 				//		 MainActivity.this, 
					    	 			    //		 item_list,
					    	 			    //		 R.layout.waitingitemistview,
					    	 			    //		 new String[] { "StoreName","number","ItemName","Now_Value","alert_text","Distance" },
					    	 			    //		 new int[] { R.id.StoreName, R.id.MyValue,R.id.ItemName,R.id.NowValue,R.id.alert,R.id.DistanceValue} ));
					    	 			
					    	 				
					    	 				
					    	 				ListViewSettingCheck=true;
					    	 			}
					    	 			
					     			});
					     			*/
					    m=main_thread_handler.obtainMessage(3);
						main_thread_handler.sendMessage(m);
						 
					    threadhandler.postDelayed(update_value, 1000);
					}
					else
					{
						Message m=main_thread_handler.obtainMessage(4,"目前沒有商品");
				 		main_thread_handler.sendMessage(m);
				 		m=main_thread_handler.obtainMessage(3);
						 main_thread_handler.sendMessage(m);
					}
					
					
			    } 
			catch (ClientProtocolException e) 
			 {
				 Message m=main_thread_handler.obtainMessage(1,"ClientProtocolException");
				 main_thread_handler.sendMessage(m);
				e.printStackTrace();
			} catch (IOException e) {
				 Message m=main_thread_handler.obtainMessage(1,"IOException");
				 main_thread_handler.sendMessage(m);
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    };
    
    private Runnable update_value=new Runnable()
    {
		public void run() 
		{	
			try {	
					int delete_list[]=new int[item_list.size()];
					int delete_list_sp=0;
			
				 	for(int i=0;i<item_list.size();i++)
				 	{	
				 		//連結到Server做更新
						ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("store",item_list.get(i).get("store")));
						nameValuePairs.add(new BasicNameValuePair("item",item_list.get(i).get("item")));
						nameValuePairs.add(new BasicNameValuePair("number",item_list.get(i).get("number")));
						nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
						String result=connect_to_server("/project/mobilephone/update_value.php",nameValuePairs);
						//json decode
						String[] key={"NowValue","ItemLife","ChangeNumberCheck","ChangeNumber"};
						ArrayList<HashMap<String,String>> temp;
						temp=json_deconde(result, key);
						
						//擷取相關資料
						String NowValue=temp.get(0).get("NowValue");
						String ItemLife=temp.get(0).get("ItemLife");
						String ChangeNumber=temp.get(0).get("ChangeNumber");
						String ChangeNumberCheck=temp.get(0).get("ChangeNumberCheck");
						
						if(!ChangeNumber.equals("-1"))
						{
							item_list.get(i).put("ChangeNumber",ChangeNumber);
							
						}
						
						
						//若到號則發出提醒
						if(item_list.get(i).get("number").equals(NowValue)&&!item_list.get(i).containsKey("alert"))
							{	
								item_list.get(i).put("alert","1");	
								alert a=new alert();
								a.setData(i,item_list.get(i).get("StoreName"),item_list.get(i).get("ItemName"));
								MainActivity.this.runOnUiThread(a);
								
								item_list.get(i).put("alert_text","到號");
								
								soundPool.play(AlarmWave, 1, 1, 0, 0, (float) 1.5);
								//播放通知音效
								// 第二、三參數分別為左右喇叭的音量，可用 0 到 1
					            // 第四參數固定用 0
					            // 第五個參數為播放次數，0 為不重複，-1 為無限重複
					            // 第六個參數為播放速度，可用 0.5 到 2 
							}
						
						//若商品狀態不等於未服務 則刪除此商品
						if(!ItemLife.equals("0"))
						{
								delete_list[delete_list_sp]=i;
								delete_list_sp++;	
						}
						 
						//沒有要求換號 不做任何事
						if(ChangeNumberCheck.equals("-1"))
						{}
						//有要求換號 但是還在等待
						else if(ChangeNumberCheck.equals("0"))
						{
							item_list.get(i).put("ChangeNumberCheck","0");
						}
						else if(ChangeNumberCheck.equals("1"))
						{
							item_list.get(i).put("ChangeNumberCheck","1");
							
						}
						//收到別人的換號要求
						else if(ChangeNumberCheck.equals("2"))
						{	
							//如果還沒發出過換號提醒 則發出
							if(!item_list.get(i).containsKey("ChangeNumberAlert"))
								{	
									
									ChangeNumberAlert CNA=new ChangeNumberAlert();
									CNA.setDate(item_list.get(i).get("item"), item_list.get(i).get("store"),i);
									Thread ChangeNumberAlertThread = new Thread(CNA);
									ChangeNumberAlertThread.start();
									
								}
						
							item_list.get(i).put("ChangeNumberAlert","1");
							item_list.get(i).put("ChangeNumberCheck","2");
						}
						else if(ChangeNumberCheck.equals("3"))
						{
							Message m=main_thread_handler.obtainMessage(7,String.valueOf(i));
					 		main_thread_handler.sendMessage(m);
					 		
					 		Log.v("debug", ChangeNumber);
					 		
					 		item_list.get(i).put("ChangeNumberCheck","");
					 		item_list.get(i).put("ChangeNumber","");
					 		ImplementChangeNumber ICN=new ImplementChangeNumber();
					 		ICN.setData(3, i);
					 		Thread ICNThread=new Thread(ICN);
					 		ICNThread.start();
						}
						
						else
						{}
						
						//更新NowValue
						item_list.get(i).put("Now_Value", NowValue);
				
				 	}
				 	
				 	
				 	for(int i=delete_list_sp-1;i>=0;i--)
				 		{	
				 			
				 			int delete_item_num=delete_list[i]; 
				 			/*
				 			ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
							nameValuePairs.add(new BasicNameValuePair("Store",item_list.get(delete_item_num).get("store")));
							nameValuePairs.add(new BasicNameValuePair("Item",item_list.get(delete_item_num).get("item")));
							nameValuePairs.add(new BasicNameValuePair("Number",item_list.get(delete_item_num).get("number")));
							nameValuePairs.add(new BasicNameValuePair("UserIMEI",UserIMEI));
							connect_to_server("/project/mobilephone/del_item.php",nameValuePairs);
							*/
				 			item_list.remove(delete_item_num);
				 		
				 		}
					
				 	
				 	delete_list_sp=0;
				 	
				 	Message m=main_thread_handler.obtainMessage(2);
				 	main_thread_handler.sendMessage(m);
				 	
					threadhandler.removeCallbacks(update_value);
					if(item_list.size()!=0)
						threadhandler.postDelayed(update_value, 3000);
				 	
			}
			
			catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    };
    
    
    private class ChangeNumberAlert implements Runnable
    {
    	String ItemID;
    	String Store;
    	int position;
    	public void setDate (String ItemID,String Store,int position)
    	{
    		this.ItemID=ItemID;
    		this.Store=Store;
    		this.position=position;
    	}
    	
		public void run() {
			/*
			ArrayList<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("ItemID",ItemID));
			nameValuePairs.add(new BasicNameValuePair("Store",Store));
			nameValuePairs.add(new BasicNameValuePair("CustomID",UserIMEI));
			String result=connect_to_server("/project/mobilephone/GetChangeNumberMatch.php",nameValuePairs);
			Log.v("DEBUG", result);
			
			String[] key={"number","MateID"};
			ArrayList<HashMap<String,String>> temp;
			temp=json_deconde(result,key);
			final String Number=temp.get(0).get("number");
			*/
			MainActivity.this.runOnUiThread(new Runnable() {
				
				public void run() 
				{	
					String Number=item_list.get(position).get("ChangeNumber");
					AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("商品: "+item_list.get(position).get("ItemName"));
					builder.setMessage("收到換號要求 號碼為: "+Number+" 號");
					builder.setPositiveButton("確認", new AlertDialog.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) 
						{
							ImplementChangeNumber ICN=new ImplementChangeNumber();
							ICN.setData(2,position);
							Thread ICNThread=new Thread(ICN);
							ICNThread.start();
							
						}
					});
					builder.setNegativeButton("拒絕", new AlertDialog.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							ImplementChangeNumber ICN=new ImplementChangeNumber();
							ICN.setData(1,position);
							Thread ICNThread=new Thread(ICN);
							ICNThread.start();
							
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					
					
					
				}
			});
			
		}
    	
    	
    	
    }
    
    
    
    
    public void TurnOnLocationListener()
    {
    	
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        

        
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        	{	
        		Criteria criteria = new Criteria();
    			criteria.setAccuracy(criteria.ACCURACY_COARSE);
    			
    			String bestProvider = locationManager.getBestProvider(criteria, true);
        		locationManager.requestLocationUpdates(bestProvider,0,0,this);
        		location_status=true;
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
    	
    }
   
    public class alert implements Runnable {
    	  private int num;
    	  private String StoreName,ItemName;
    	  public void setData(int data,String getStoreName,String getItemName) 
    	  {
	    	    num=data;
	    	    StoreName=getStoreName;
	    	    ItemName=getItemName;
    	  }

    	  public void run() 
    	  {
    		  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    		  builder.setMessage(StoreName+":"+ItemName);
    		  builder.setTitle("到號通知");
    		  DialogInterface.OnClickListener okclick=new DialogInterface.OnClickListener()
    		  {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
    		  };
    		  builder.setNeutralButton("確認", okclick);
    		  AlertDialog alert = builder.create();
    		  alert.show();
    	  }
    	}

    public String getIMEI()
    {
			
    	TelephonyManager telManager  = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    	return telManager.getDeviceId();
    }
    
    
    public void update_list(int position)
    {
    	item_list.remove(position);
    	ListView list=(ListView) findViewById(R.id.listView1);
    	((ItemAdapter)list.getAdapter()).notifyDataSetChanged();
    }
    
    public boolean check_connect_status()
    {	
    	try {	
		 		//Message ma=main_thread_handler.obtainMessage(1,"try!");
		 		//main_thread_handler.sendMessage(ma);
    			ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    			NetworkInfo network_info=cm.getActiveNetworkInfo();
    			if(network_info==null||network_info.isConnected()==false)
    				{
						//Toast.makeText(this, "網路狀態：關閉", Toast.LENGTH_SHORT).show();
					 	Message m=main_thread_handler.obtainMessage(4,"請開啟網路");
					 	main_thread_handler.sendMessage(m);
    					return false;
					
    				}
    			else
    				{	
    					//Toast.makeText(this, "網路狀態：開啟", Toast.LENGTH_SHORT).show();
    				}
    		
    		
				if(connect_to_server("/project/mobilephone/check_connect.php",null).equals("1"))
					{
						//Toast.makeText(this, "可連接至主機", Toast.LENGTH_SHORT).show();
						return true;
					}
				else
					{
						//Toast.makeText(this, "not connect", Toast.LENGTH_SHORT).show();

						return false;
					}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
	 		Message m=main_thread_handler.obtainMessage(4,"無法連接至主機");
	 		main_thread_handler.sendMessage(m);
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

	public void onLocationChanged(Location arg0) 
	{	
		if(item_list!=null&&!item_list.isEmpty())
			{
				for(int i=0;i<item_list.size();i++)
				{
					double lat=Double.parseDouble(item_list.get(i).get("GPS_Latitude"));
					double lng=Double.parseDouble(item_list.get(i).get("GPS_Longitude"));
					double dis=getDistance(arg0.getLatitude(),lat,arg0.getLongitude(),lng);
					int RoundDistance=(int)Math.round(dis);
					item_list.get(i).put("Distance",String.valueOf(RoundDistance));
					
				}
			}
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
    
	public static double rad(double d)
	{
		return d*Math.PI/180.0;
	}
	
	public double getDistance(double lat1,double lat2,double lng1,double lng2)
	{
		double radlat1=rad(lat1);
		double radlat2=rad(lat2);
		double radlng1=rad(lng1);
		double radlng2=rad(lng2);
		
		double a=radlat1-radlat2;
		double b=radlng1-radlng2;
		
		double s=2*Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) + 
			        Math.cos(radlat1)*Math.cos(radlat2)*Math.pow(Math.sin(b/2),2)));
		s=s*EARTH_RADIUS;
		
		return s;
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
			ItemCount.setText(ItemList.get(arg0).get("NeedValue"));
			
			return ItemView;
		}
		
		
	}
	
	

}






