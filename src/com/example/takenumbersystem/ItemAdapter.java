package com.example.takenumbersystem;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ItemAdapter extends BaseAdapter
	{	
		private Context context;
		private ArrayList<HashMap<String,String>> ItemList;
		
		public ItemAdapter(Context mcontext,ArrayList<HashMap<String,String>> mItemList)
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
			
			View ItemView=layoutinflater.inflate(R.layout.waitingitemistview, null);
			LinearLayout lLayout=(LinearLayout)ItemView.findViewById(R.id.WaitingItemListViewLinearLayout);
			if(ItemList.get(arg0).get("ChangeNumberCheck")=="0")
			{
				lLayout.setBackgroundColor(Color.GREEN);
			}
			else if(ItemList.get(arg0).get("ChangeNumberCheck").equals("1"))
			{
				lLayout.setBackgroundColor(Color.YELLOW);
			}
			else if(ItemList.get(arg0).get("ChangeNumberCheck").equals("2"))
			{
				lLayout.setBackgroundColor(Color.YELLOW);
			}
			else
			{
				
			}
			
			
			
			
			
			TextView StoreName=(TextView)ItemView.findViewById(R.id.StoreName);
			StoreName.setText(ItemList.get(arg0).get("StoreName"));
			
			TextView MyValue=(TextView)ItemView.findViewById(R.id.MyValue);
			MyValue.setText(ItemList.get(arg0).get("number"));
			
			TextView ItemName=(TextView)ItemView.findViewById(R.id.ItemName);
			ItemName.setText(ItemList.get(arg0).get("ItemName"));
			
			TextView NowValue=(TextView)ItemView.findViewById(R.id.NowValue);
			NowValue.setText(ItemList.get(arg0).get("Now_Value"));
			
			
			TextView DistanceValue=(TextView)ItemView.findViewById(R.id.DistanceValue);
			DistanceValue.setText(ItemList.get(arg0).get("Distance"));
			
			TextView AlertText=(TextView)ItemView.findViewById(R.id.alert);
			AlertText.setText(ItemList.get(arg0).get("alert_text"));
		
		
			
			return ItemView;
		}
		
	}
