package test.custom.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CustomAdapterExample extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
    	final Context context = CustomAdapterExample.this;
    	
        /*ListView ls1 = new ListView(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
       		 context,
       		 android.R.layout.simple_list_item_1,
       		 new String[]{"item1","item2","item3","item4","item5","item6","item7"});
         ls1.setAdapter(adapter);*/
    	
    	ListView ls2 = new ListView(context);
     	/*m_lv1.setClickable(true);
     	m_lv1.setFastScrollEnabled(true);
     	m_lv1.setItemsCanFocus(true);*/
     	
     	// clear previous results in the LV
    	ls2.setAdapter(null);        
   		// populate
    	ArrayList<Device> m_Devices = new ArrayList<Device>();
   		Device device;
	        for (int i=0;i<10;i++) {
	        device = new Device("Network Device "+i,"13:B4:5C:0D:AE:67", i%2,0, 100 + i);
	        m_Devices.add(device);
	        }
        CustomAdapter lvAdapter =  new CustomAdapter(context, m_Devices);
        ls2.setAdapter(lvAdapter);
        ls2.setOnItemClickListener(new OnItemClickListener() 
        {
        	public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
        	{
        		Toast.makeText(getBaseContext(), "You clicked on "+arg2, Toast.LENGTH_LONG).show();
    		}
    	});
         
         setContentView(ls2);
    }
}