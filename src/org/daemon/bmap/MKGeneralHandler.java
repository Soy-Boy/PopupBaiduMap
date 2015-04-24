package org.daemon.bmap;

import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class MKGeneralHandler implements MKGeneralListener {
	
	Context context = null;
	public MKGeneralHandler(Context context){
		this.context = context;
	}

	@Override
	public void onGetNetworkState(int arg0) {
		// TODO Auto-generated method stub
		if (arg0 == MKEvent.ERROR_NETWORK_CONNECT) {
            Toast.makeText(context, "", Toast.LENGTH_LONG).show();
        }
        else if (arg0 == MKEvent.ERROR_NETWORK_DATA) {
            Toast.makeText(context, "", Toast.LENGTH_LONG).show();
        }
	}

	@Override
	public void onGetPermissionState(int arg0) {
		// TODO Auto-generated method stub
		if (arg0 ==  MKEvent.ERROR_PERMISSION_DENIED) {
            Toast.makeText(context, "", Toast.LENGTH_LONG).show();
        }
	}

}
