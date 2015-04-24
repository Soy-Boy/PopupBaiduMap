package org.daemon.bmap;

import java.util.ArrayList;
import java.util.List;

import org.daemon.bmap.MyPopupOverlay.OnTapListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
 
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;
	private String keyString = "uHZf8R4yflDHGSIFoP4rfysk";
	
	private MapController mMapController = null;
	private Toast mToast;
	private LocationClient mLocClient;
	private LocationData mLocData;
	//定位图层
	private	LocationOverlay myLocationOverlay = null;
	
	private boolean isRequest = false;//是否手动触发请求定位
	private boolean isFirstLoc = true;//是否首次定位
	
	private PopupOverlay mPopupOverlay  = null;//弹出泡泡图层，浏览节点时使用
	private View viewCache;
	private BDLocation location;
	
	EditText editText;
	Button button;
	
	MKSearch mkSearch = null;
	MKSearchListener mkSearchListener = new MKSearchListener() {
		
		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		  
		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			// 首先判断是否搜索到结果
			if(arg2 != 0 || arg0 == null)
			{
				Toast.makeText(MainActivity.this, "没有找到结果！", Toast.LENGTH_SHORT).show();
				return;
			}
			// 将结果绘制到地图上
			if(arg0.getCurrentNumPois() > 0)
			{
				PoiOverlay poiOverlay = new PoiOverlay(MainActivity.this, mMapView);
                poiOverlay.setData(arg0.getAllPoi());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(poiOverlay);
                mMapView.refresh();
                //当arg1为2（公交线路）或4（地铁线路）时， poi坐标为空
                for( MKPoiInfo info : arg0.getAllPoi() )
                {
                	if ( info.pt != null ){
                		mMapView.getController().animateTo(info.pt);
                		break;
                	}
                }
			}
		}
		
		@Override
		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
				int arg2) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(keyString, new MKGeneralHandler(MainActivity.this));
        setContentView(R.layout.activity_main);
		
      //点击按钮手动请求定位
      		((Button) findViewById(R.id.request)).setOnClickListener(new OnClickListener() {
      			
      			@Override
      			public void onClick(View v) {
      				requestLocation();
      			}
      		});
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		mMapController = mMapView.getController();
		mMapController.setZoom(16);
		 mMapView.setBuiltInZoomControls(true);   //显示内置缩放控件
	        editText = (EditText) findViewById(R.id.editText1);//JIA
			button = (Button) findViewById(R.id.button1);//JIA
			// 初始化Poi搜索对象
			mkSearch = new MKSearch();
			mkSearch.init(mBMapMan, mkSearchListener);
			
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String key = editText.getText().toString();
					// 如果关键字为空则不进入搜索
					if(key.equals(""))
					{
						Toast.makeText(MainActivity.this, "请输入搜索关键词！", Toast.LENGTH_SHORT).show();
					}
					else      
					{
						// 这里Poi搜索以城市内Poi检索为例，开发者可根据自己的实际需求，灵活使用
						mkSearch.poiSearchInCity("成都", key);
					}
				}
			});

	        mLocData = new LocationData();
	        
	        
	        //实例化定位服务，LocationClient类必须在主线程中声明
	        mLocClient = new LocationClient(getApplicationContext());
			mLocClient.registerLocationListener(new BDLocationListenerImpl());//注册定位监听接口
			
			/**
			 * 设置定位参数
			 */
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true); //打开GPRS
			option.setAddrType("all");//返回的定位结果包含地址信息
			option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
			option.setScanSpan(5000); //设置发起定位请求的间隔时间为5000ms
			option.disableCache(false);//禁止启用缓存定位
//			option.setPoiNumber(5);    //最多返回POI个数   
//			option.setPoiDistance(1000); //poi查询距离        
//			option.setPoiExtraInfo(true);  //是否需要POI的电话和地址等详细信息        
			
			mLocClient.setLocOption(option);
			mLocClient.start();  //	调用此方法开始定位
			
			//定位图层初始化
			myLocationOverlay = new LocationOverlay(mMapView);
			//设置定位数据
		    myLocationOverlay.setData(mLocData);
		    
		    myLocationOverlay.setMarker(getResources().getDrawable(R.drawable.me));
		    
		    //添加定位图层
		    mMapView.getOverlays().add(myLocationOverlay);
		    myLocationOverlay.enableCompass();
		    
		    //修改定位数据后刷新图层生效
		    mMapView.refresh();
		//meiyou p38
		GeoPoint p1 = new GeoPoint(30635379, 104096137);//经济
		GeoPoint p2 = new GeoPoint(30639403, 104097471);//法学院
		GeoPoint p3 = new GeoPoint(30641127, 104098000);//文学与新闻
		GeoPoint p4 = new GeoPoint(30637872, 104094363);//外国语学院
		GeoPoint p5 = new GeoPoint(30639737, 104094162);//艺术学院
		GeoPoint p6 = new GeoPoint(30636994, 104093731);//数学学院
		GeoPoint p7 = new GeoPoint(30636909, 104096889);//　物理科学与技术学院
		GeoPoint p8 = new GeoPoint(30638292, 104097511);//化学学院
		GeoPoint p9 = new GeoPoint(30646914, 104076145);//生命科学学院
		GeoPoint p10= new GeoPoint(30639185, 104088357);//电子信息学院
		GeoPoint p11= new GeoPoint(30637406, 104097701);//材料学院
		GeoPoint p12= new GeoPoint(30639651, 104085239);//制造科学与工程学院
		GeoPoint p13= new GeoPoint(30638587, 104088171);//电气信息学院
		GeoPoint p14= new GeoPoint(30637080, 104087569);//计算机学院
		GeoPoint p15= new GeoPoint(30637950, 104085340);//建筑与环境学院
		GeoPoint p16= new GeoPoint(30638012, 104084512);//空天科学与工程学院
		GeoPoint p17= new GeoPoint(30632232, 104084920);//水利水电学院
		GeoPoint p18= new GeoPoint(30638905, 104083753);//化学工程学院
		GeoPoint p19= new GeoPoint(30633204, 104085480);//轻纺与食品学院
		GeoPoint p20= new GeoPoint(30635285, 104085393);//高分子科学与工程学院
		GeoPoint p21= new GeoPoint(30647807, 104073793);//华西基础医学与法医学院
		GeoPoint p22= new GeoPoint(30649236, 104067935);//华西临床医学院(华西医院)
		GeoPoint p23= new GeoPoint(30648770, 104072134);//　华西口腔医学院
		GeoPoint p24= new GeoPoint(30647330, 104069868);//华西公共卫生学院(华西第四医院)
		GeoPoint p25= new GeoPoint(30643745, 104076799);//华西药学院
		GeoPoint p26= new GeoPoint(30635379, 104096137);//公共管理学院
		GeoPoint p27= new GeoPoint(30637903, 104093340);//商学院
		GeoPoint p28= new GeoPoint(30639706, 104098086);//政治学院
		GeoPoint p29= new GeoPoint(30634788, 104090470);//体育学院
		GeoPoint p30= new GeoPoint(30637080, 104087569);//软件学院
		GeoPoint p31= new GeoPoint(30639923, 104084135);//灾后重建与管理学院
		
		//江安
		GeoPoint p32= new GeoPoint(30566030, 104015090);//江安灾后重建与管理学院
		GeoPoint p33= new GeoPoint(30564980, 104006988);//江安建筑与环境学院
		GeoPoint p34= new GeoPoint(30559453, 104003047);//江安体育学院
		GeoPoint p35= new GeoPoint(30561482, 104001869);//江安艺术学院
		GeoPoint p36= new GeoPoint(30564234, 104009367);//江安计算机学院		
		GeoPoint p37= new GeoPoint(30558535, 104008293);//江安法学院
		//教学楼
		GeoPoint p39= new GeoPoint(30635798, 104086676);//研究生教学楼
		GeoPoint p40= new GeoPoint(30636606, 104093792);//第三教学楼
		GeoPoint p41= new GeoPoint(30639628, 104086174);//西五教学楼
		GeoPoint p42= new GeoPoint(30637336, 104095400);//第一教学楼
		GeoPoint p43= new GeoPoint(30639597, 104085357);//西三教学楼
		GeoPoint p44= new GeoPoint(30636645, 104083794);//西四教学楼
		GeoPoint p45= new GeoPoint(30636715, 104084014);//第四教学楼
		GeoPoint p46= new GeoPoint(30639410, 104086294);//第五教学楼
		GeoPoint p47= new GeoPoint(30635915, 104094694);//第二教学楼
		GeoPoint p48= new GeoPoint(30638882, 104083668);//西二教学楼
		GeoPoint p49= new GeoPoint(30638082, 104087360);//基础教学楼
		GeoPoint p50= new GeoPoint(30647799, 104069121);//华西临床教学楼
		GeoPoint p51= new GeoPoint(30560697, 104007150);//江安第一教学楼
		GeoPoint p52= new GeoPoint(30645586, 104076315);//华西东区第六教学楼
		GeoPoint p53= new GeoPoint(30646137, 104075602);//华西东区第二教学楼
		GeoPoint p54= new GeoPoint(30647613, 104071904);//华西西区口腔教学楼
		GeoPoint p55= new GeoPoint(30646898, 104074326);//华西东区第四教学楼
		GeoPoint p56= new GeoPoint(30646914, 104076145);//华西东区第三教学楼
		GeoPoint p57= new GeoPoint(30648195, 104074003);//华西东区第五教学楼
		GeoPoint p58= new GeoPoint(30646797, 104075602);//华西东区第九教学楼
		GeoPoint p59= new GeoPoint(30646743, 104070378);//华西西区第七教学楼
		GeoPoint p60= new GeoPoint(30646114, 104076215);//华西东区第一教学楼
		GeoPoint p61= new GeoPoint(30645609, 104075441);//华西东区第十教学楼
		GeoPoint p62= new GeoPoint(30647893, 104069503);//华西西区第八教学楼
		mMapController.animateTo(p1);

		//
		MyPopupOverlay myOverlay = new MyPopupOverlay(
				MainActivity.this,
				getResources().getDrawable(R.drawable.icon_gcoding),
				mMapView);
		myOverlay.setLayoutId(R.layout.popup_content);
		//myOverlay.setUseDefaultMarker(true);
				
		OverlayItem item1 = new OverlayItem(p1, "", "");
		OverlayItem item2 = new OverlayItem(p2, "", "");
		OverlayItem item3 = new OverlayItem(p3, "", "");
		OverlayItem item4 = new OverlayItem(p4, "", "");
		OverlayItem item5 = new OverlayItem(p5, "", "");
		OverlayItem item6 = new OverlayItem(p6, "", "");
		OverlayItem item7 = new OverlayItem(p7, "", "");
		OverlayItem item8 = new OverlayItem(p8, "", "");
		OverlayItem item9 = new OverlayItem(p9, "", "");
		OverlayItem item10 = new OverlayItem(p10, "", "");
		OverlayItem item11= new OverlayItem(p11, "", "");
		OverlayItem item12= new OverlayItem(p12, "", "");
		OverlayItem item13= new OverlayItem(p13, "", "");
		OverlayItem item14= new OverlayItem(p14, "", "");
		OverlayItem item15= new OverlayItem(p15, "", "");
		OverlayItem item16= new OverlayItem(p16, "", "");
		OverlayItem item17= new OverlayItem(p17, "", "");
		OverlayItem item18= new OverlayItem(p18, "", "");
		OverlayItem item19= new OverlayItem(p19, "", "");
		OverlayItem item20= new OverlayItem(p20, "", "");
		OverlayItem item21= new OverlayItem(p21, "", "");
		OverlayItem item22= new OverlayItem(p22, "", "");
		OverlayItem item23= new OverlayItem(p23, "", "");
		OverlayItem item24= new OverlayItem(p24, "", "");
		OverlayItem item25= new OverlayItem(p25, "", "");
		OverlayItem item26= new OverlayItem(p26, "", "");
		OverlayItem item27= new OverlayItem(p27, "", "");
		OverlayItem item28= new OverlayItem(p28, "", "");
		OverlayItem item29= new OverlayItem(p29, "", "");
		OverlayItem item30= new OverlayItem(p30, "", "");
		OverlayItem item31= new OverlayItem(p31, "", "");
		OverlayItem item32= new OverlayItem(p32, "", "");
		OverlayItem item33= new OverlayItem(p33, "", "");
		OverlayItem item34= new OverlayItem(p34, "", "");
		OverlayItem item35= new OverlayItem(p35, "", "");
		OverlayItem item36= new OverlayItem(p36, "", "");
		OverlayItem item37= new OverlayItem(p37, "", "");
		OverlayItem item39= new OverlayItem(p39, "", "");
		item39.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item40= new OverlayItem(p40, "", "");
		item40.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item41= new OverlayItem(p41, "", "");item41.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item42= new OverlayItem(p42, "", "");item42.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item43= new OverlayItem(p43, "", "");item43.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item44= new OverlayItem(p44, "", "");item44.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item45= new OverlayItem(p45, "", "");item45.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item46= new OverlayItem(p46, "", "");item46.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item47= new OverlayItem(p47, "", "");item47.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item48= new OverlayItem(p48, "", "");item48.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item49= new OverlayItem(p49, "", "");item49.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item50= new OverlayItem(p50, "", "");item50.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item51= new OverlayItem(p51, "", "");item51.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item52= new OverlayItem(p52, "", "");item52.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item53= new OverlayItem(p53, "", "");item53.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item54= new OverlayItem(p54, "", "");item54.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item55= new OverlayItem(p55, "", "");item55.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item56= new OverlayItem(p56, "", "");item56.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item57= new OverlayItem(p57, "", "");item57.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item58= new OverlayItem(p58, "", "");item58.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item59= new OverlayItem(p59, "", "");item59.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item60= new OverlayItem(p60, "", "");item60.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item61= new OverlayItem(p61, "", "");item61.setMarker(getResources().getDrawable(R.drawable.lvse));
		OverlayItem item62= new OverlayItem(p62, "", "");item62.setMarker(getResources().getDrawable(R.drawable.lvse));
		
		
		List<OverlayItem> items = new ArrayList<OverlayItem>();
		items.add(item1);
		items.add(item2);
		items.add(item3);
		items.add(item4);
		items.add(item5);
		items.add(item6);
		items.add(item7);
		items.add(item8);
		items.add(item9);
		items.add(item10);
		items.add(item11);
		items.add(item12);
		items.add(item13);
		items.add(item14);
		items.add(item15);
		items.add(item16);
		items.add(item17);
		items.add(item18);
		items.add(item19);
		items.add(item20);
		items.add(item21);
		items.add(item22);
		items.add(item23);
		items.add(item24);
		items.add(item25);
		items.add(item26);
		items.add(item27);
		items.add(item28);
		items.add(item29);
		items.add(item30);
		items.add(item31);
		items.add(item32);
		items.add(item33);
		items.add(item34);
		items.add(item35);
		items.add(item36);
		items.add(item37);
		
		items.add(item39);
		items.add(item40);
		items.add(item41);
		items.add(item42);
		items.add(item43);
		items.add(item44);
		items.add(item45);
		items.add(item46);
		items.add(item47);
		items.add(item48);
		items.add(item49);
		items.add(item50);
		items.add(item51);
		items.add(item52);
		items.add(item53);
		items.add(item54);
		items.add(item55);
		items.add(item56);
		items.add(item57);
		items.add(item58);
		items.add(item59);
		items.add(item60);
		items.add(item61);
		items.add(item62);
		
		myOverlay.addItem(items);
//		myOverlay.addItem(item2);
		
		final List<MapPopupItem> mItems = new ArrayList<MapPopupItem>();
		MapPopupItem mItem = new MapPopupItem();
		mItem.setTitle("望江经济学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("028-85412504 85418561");
		mItem.setDrawableId(R.drawable.jingji);
		mItem.setDetail("经济学院经国家教委批准成立于1985年11月");
		mItems.add(mItem);
		mItem = new MapPopupItem();//1
		mItem.setTitle("望江法学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("028-85990911");
		mItem.setDrawableId(R.drawable.fa);
		mItem.setDetail("法学院有着悠久的历史。她创始于1903年，初称“课吏馆”，又称仕学馆。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//2
		mItem.setTitle("望江文学与新闻学院");
		mItem.setAddress("四川省成都市武侯区望江路29号文科楼3楼");
		mItem.setTelNumber("028-85412309 85412710");
		mItem.setDrawableId(R.drawable.wenxin);
		mItem.setDetail("文学与新闻学院具有悠久的历史。文学院历任院长有向楚.");
		mItems.add(mItem);
		mItem = new MapPopupItem();//3
		mItem.setTitle("望江外国语学院");
		mItem.setAddress("成都市望江路29#  ");
		mItem.setTelNumber("86-028-85412023 ");
		mItem.setDrawableId(R.drawable.waiguoyu);
		mItem.setDetail("外国语学院由原四川大学外文系、原成都科技大学外语系和原华西医科大学外语系调整组建而成");
		mItems.add(mItem);
		mItem = new MapPopupItem();//4
		mItem.setTitle("望江艺术学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("028-85991681 028-86991679");
		mItem.setDrawableId(R.drawable.yishu);
		mItem.setDetail("先后经历了文化艺术学院、艺术学院（1996年）、哲学与艺术学院（1998年）等建制形式后，于2001年6月恢复了艺术学院建制。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//5
		mItem.setTitle("望江数学学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("028-85412720");
		mItem.setDrawableId(R.drawable.shuxue);
		mItem.setDetail("数学学院是四川大学历史最悠久的院系之一。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//6
		mItem.setTitle("望江物理学院");
		mItem.setAddress(" 四川省成都市九眼桥望江路29号");
		mItem.setTelNumber(" 028-85412322(行政)85412323(党办)85415561(教务)85410030(学工)");
		mItem.setDrawableId(R.drawable.wuli);
		mItem.setDetail("物理科学与技术学院是四川大学规模最大和办学历史最悠久的学院之一");
		mItems.add(mItem);
		mItem = new MapPopupItem();//7
		mItem.setTitle("望江化学学院");
		mItem.setAddress("四川大学化学学院（610064）");
		mItem.setTelNumber("（028）85412290 ");
		mItem.setDrawableId(R.drawable.huaxue);
		mItem.setDetail("化学学院的办学可上溯至1907年四川高等学堂化学门与应用化学门的开办");
		mItems.add(mItem);
		mItem = new MapPopupItem();//8
		mItem.setTitle("望江生命科学学院");
		mItem.setAddress("四川省成都市武侯区望江路29号");
		mItem.setTelNumber("scu_life@163.com");
		mItem.setDrawableId(R.drawable.life);
		mItem.setDetail("生命科学学院历史悠久，其前身生物系始建于1924年");
		mItems.add(mItem);
		mItem = new MapPopupItem();//9
		mItem.setTitle("望江电子信息学院");
		mItem.setAddress("四川大学基础实验教学楼A座一楼");
		mItem.setTelNumber("86-028-85463873 85463872");
		mItem.setDrawableId(R.drawable.dianzi);
		mItem.setDetail("电子信息学院组建于1998年，学院的变革和发展可追溯到1954年，是一个蓬勃发展的学科型学院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//10
		mItem.setTitle("望江材料学院");
		mItem.setAddress("四川大学第一理科楼附三楼");
		mItem.setTelNumber("86-28-85416050");
		mItem.setDrawableId(R.drawable.cailiao);
		mItem.setDetail("材料科学与工程学院于2001年7月，由原材料科学系、金属材料系和无机材料系等三个实体系组建而成");
		mItems.add(mItem);
		mItem = new MapPopupItem();//11
		mItem.setTitle("望江制造学院");
		mItem.setAddress("四川省成都市武侯区望江路29号");
		mItem.setTelNumber("028-85405822");
		mItem.setDrawableId(R.drawable.zhizao);
		mItem.setDetail("制造科学与工程学院，由始建于1945年3月的国立四川大学机械电机工程学系发展变革而来");
		mItems.add(mItem);
		mItem = new MapPopupItem();//12
		mItem.setTitle("望江电气学院");
		mItem.setAddress("四川大学望江校区基础教学大楼 A座二楼");
		mItem.setTelNumber("028-85405614");
		mItem.setDrawableId(R.drawable.dianqi);
		mItem.setDetail("电气信息学院组建于1998年，由原成都科技大学电力工程系、自动化系、应用电子技术系合并组建而成");
		mItems.add(mItem);
		mItem = new MapPopupItem();//13
		mItem.setTitle("望江计算机学院");
		mItem.setAddress("成都市一环路南一段24号基础教学楼B座三楼");
		mItem.setTelNumber("86-028-85469688 ");
		mItem.setDrawableId(R.drawable.jisuanji);
		mItem.setDetail("计算机学院1958年设立计算机专业，1981年建立计算机科学系，1998年设立计算机学院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//14
		mItem.setTitle("望江建环学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("404");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("404");
		mItems.add(mItem);
		mItem = new MapPopupItem();//15
		mItem.setTitle("望江空天学院");
		mItem.setAddress("成都市一环路南一段24号四川大学行政楼401A");
		mItem.setTelNumber("028-85402654 028-85402657");
		mItem.setDrawableId(R.drawable.kt);
		mItem.setDetail("航空宇航科学与技术学科在2012年全国学科评估中名列第八，2013年正式对外招收硕士、博士研究生");
		mItems.add(mItem);
		mItem = new MapPopupItem();//16
		mItem.setTitle("望江水利水电学院");
		mItem.setAddress("中国.四川.成都市一环路南一段24号");
		mItem.setTelNumber("028—85401154");
		mItem.setDrawableId(R.drawable.shuili);
		mItem.setDetail("四川大学水利教育之先河始于1944年建立的四川大学理工学院土木水利系");
		mItems.add(mItem);
		mItem = new MapPopupItem();//17
		mItem.setTitle("望江化工学院");
		mItem.setAddress("四川省成都市一环路南一段24号");
		mItem.setTelNumber("028-85405222");
		mItem.setDrawableId(R.drawable.huagong);
		mItem.setDetail("化学工程学院办学历史悠久，学术沉淀深厚");
		mItems.add(mItem);
		mItem = new MapPopupItem();//18
		mItem.setTitle("望江轻纺学院");
		mItem.setAddress("成都市一环路南一段24号");
		mItem.setTelNumber("028-85405836 028-85405840（院办) 028-85461730（党办）");
		mItem.setDrawableId(R.drawable.qf);
		mItem.setDetail("轻纺与食品学院成立于2001年7月，由原轻工食品学院和纺织学院合并组建");
		mItems.add(mItem);
		mItem = new MapPopupItem();//19
		mItem.setTitle("望江高分子学院");
		mItem.setAddress("四川省成都市一环路南一段24号");
		mItem.setTelNumber("86-28-85461786 86-28-85405401");
		mItem.setDrawableId(R.drawable.gfz);
		mItem.setDetail("高分子科学与工程学院是教育部直属重点高校中第一个以高分子学科为主体的学科型学院。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//20
		mItem.setTitle("华西基法学院");
		mItem.setAddress("成都人民南路3段17号");
		mItem.setTelNumber("(028) 85501243; 85501550");
		mItem.setDrawableId(R.drawable.jifa);
		mItem.setDetail("华西基础医学与法医学院是四川大学重要的教学、科研基地，建院于2001年");
		mItems.add(mItem);
		mItem = new MapPopupItem();//21
		mItem.setTitle("华西临床医学院");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("028-85422114 ");
		mItem.setDrawableId(R.drawable.lc);
		mItem.setDetail("华西医院起源于由美国、英国、加拿大等国的基督教会1892年在成都创建的仁济、存仁医院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//22
		mItem.setTitle("华西口腔医学院");
		mItem.setAddress("四川省成都市人民南路三段14号");
		mItem.setTelNumber("028-85501437");
		mItem.setDrawableId(R.drawable.kouqiang);
		mItem.setDetail("华西口腔医学院具有悠久的历史和深厚的底蕴,始建于1917年");
		mItems.add(mItem);
		mItem = new MapPopupItem();//23
		mItem.setTitle("华西公共卫生学院");
		mItem.setAddress("四川省成都市人民南路三段14号");
		mItem.setTelNumber("028-85501272");
		mItem.setDrawableId(R.drawable.gw);
		mItem.setDetail("华西公共卫生学院是中国最著名的公共卫生学院之一，历史可追溯到 1914 年");
		mItems.add(mItem);
		mItem = new MapPopupItem();//24
		mItem.setTitle("华西药学院");
		mItem.setAddress("四川省成都市人民南路三段17号");
		mItem.setTelNumber("028-85501628");
		mItem.setDrawableId(R.drawable.yao);
		mItem.setDetail("华西药学院前身为华西协合大学理学院药学系，创建于1918年");
		mItems.add(mItem);
		mItem = new MapPopupItem();//25
		mItem.setTitle("望江公共管理学院");
		mItem.setAddress("四川大学望江校区东区公共管理学院大楼");
		mItem.setTelNumber("028-85418790");
		mItem.setDrawableId(R.drawable.gg);
		mItem.setDetail("四川大学公共管理学院成立于2001年6月");
		mItems.add(mItem);
		mItem = new MapPopupItem();//26
		mItem.setTitle("望江商学院");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("028-85470029");
		mItem.setDrawableId(R.drawable.shang);
		mItem.setDetail("四川大学商学教育始于1905年四川省城高等学堂所附设的半日学堂");
		mItems.add(mItem);
		mItem = new MapPopupItem();//27
		mItem.setTitle("望江政治学院");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("028-85996283 ");
		mItem.setDrawableId(R.drawable.zz);
		mItem.setDetail("马克思主义学院（政治学院）成立于2001年，系由原四川大学马列部、思政部和华西医科大学人文社会科学部合并组建而成");
		mItems.add(mItem);
		mItem = new MapPopupItem();//28
		mItem.setTitle("望江体育学院");
		mItem.setAddress("404");
		mItem.setTelNumber("404");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("404");
		mItems.add(mItem);
		mItem = new MapPopupItem();//29
		mItem.setTitle("望江软件学院");
		mItem.setAddress("成都市一环路南一段24号基础教学楼B座三楼");
		mItem.setTelNumber("86-028-85469688 ");
		mItem.setDrawableId(R.drawable.rj);
		mItem.setDetail("四川大学国家示范性软件学院是教育部、原国家计委批准的35所国家级示范性软件学院之一，成立于2001年12月");
		mItems.add(mItem);
		mItem = new MapPopupItem();//30
		mItem.setTitle("望江灾后重建学院");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("028)85996595");
		mItem.setDrawableId(R.drawable.zaihou);
		mItem.setDetail(" 四川大学-香港理工大学灾共建后重建与管理学院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//31
		mItem.setTitle("江安灾后重建学院");
		mItem.setAddress("四川省成都市双流县黄河中路");
		mItem.setTelNumber("(028)85992211");
		mItem.setDrawableId(R.drawable.zaihou);
		mItem.setDetail("四川大学-香港理工大学灾共建后重建与管理学院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//32
		mItem.setTitle("江安建环学院");
		mItem.setAddress("404");
		mItem.setTelNumber("404");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("404");
		mItems.add(mItem);
		mItem = new MapPopupItem();//33
		mItem.setTitle("江安体育学院");
		mItem.setAddress("404");
		mItem.setTelNumber("404");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("404");
		mItems.add(mItem);
		mItem = new MapPopupItem();//34
		mItem.setTitle("江安艺术学院");
		mItem.setAddress("四川成都双流航空港四川大学艺术学院大楼");
		mItem.setTelNumber("028-85991681 028-86991679");
		mItem.setDrawableId(R.drawable.yishu);
		mItem.setDetail("先后经历了文化艺术学院、艺术学院（1996年）、哲学与艺术学院（1998年）等建制形式后，于2001年6月恢复了艺术学院建制。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//35
		mItem.setTitle("江安计算机学院");
		mItem.setAddress("成都市双流县川大路第二基础教学楼B座五楼");
		mItem.setTelNumber("86-028-8599097");
		mItem.setDrawableId(R.drawable.jisuanji);
		mItem.setDetail("计算机学院1958年设立计算机专业，1981年建立计算机科学系，1998年设立计算机学院");
		mItems.add(mItem);
		mItem = new MapPopupItem();//36
		mItem.setTitle("江安法学院");
		mItem.setAddress("成都市双流县川大路二段");
		mItem.setTelNumber("028-85990911");
		mItem.setDrawableId(R.drawable.fa);
		mItem.setDetail("法学院有着悠久的历史。她创始于1903年，初称“课吏馆”，又称仕学馆。");
		mItems.add(mItem);
		mItem = new MapPopupItem();//39
		mItem.setTitle("研究生教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//40
		mItem.setTitle("望江第三教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//41
		mItem.setTitle("望江西五教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		
		mItem = new MapPopupItem();//42
		mItem.setTitle("望江第一教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//43
		mItem.setTitle("望江西三教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//44
		mItem.setTitle("望江西四教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//45
		mItem.setTitle("望江第四教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//46
		mItem.setTitle("望江第五教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//47
		mItem.setTitle("望江第二教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//48
		mItem.setTitle("望江西二教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//49
		mItem.setTitle("望江基础教学楼");
		mItem.setAddress("成都市望江路29号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//50
		mItem.setTitle("华西临床教学楼");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);

		mItem = new MapPopupItem();//51
		mItem.setTitle("江安第一教学楼");
		mItem.setAddress("成都市双流县川大路二段");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//52
		mItem.setTitle("华西东区六教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//53
		mItem.setTitle("华西东区二教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//54
		mItem.setTitle("华西西区口腔教学楼");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//55
		mItem.setTitle("华西东区四教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//56
		mItem.setTitle("华西东区三教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//57
		mItem.setTitle("华西东区五教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//58
		mItem.setTitle("华西东区九教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//59
		mItem.setTitle("华西西区七教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);

		mItem = new MapPopupItem();//60
		mItem.setTitle("华西东区第一教学楼");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//61
		mItem.setTitle("华西东区十教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		mItem = new MapPopupItem();//62
		mItem.setTitle("华西西区八教");
		mItem.setAddress("四川省成都市武侯区国学巷37号");
		mItem.setTelNumber("");
		mItem.setDrawableId(R.drawable.demo_dahuo);
		mItem.setDetail("教学楼");
		mItems.add(mItem);
		
		
		myOverlay.setOnTapListener(new OnTapListener() {
			
			@Override
			public void onTap(int index, View popupView) {
				// TODO Auto-generated method stub
				MapPopupItem mItem = mItems.get(index);
	//			Button button = (Button) popupView.findViewById(R.id.enter);
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
					}
				});
				TextView shopName = (TextView) popupView.findViewById(R.id.name);
				TextView shopAdress = (TextView) popupView.findViewById(R.id.address);
				TextView shopTel = (TextView) popupView.findViewById(R.id.tel);
				TextView shopDetail = (TextView) popupView.findViewById(R.id.detail);
				ImageView image = (ImageView) popupView.findViewById(R.id.popup_image);
				
				shopName.setText(mItem.getTitle());
				shopAdress.setText(mItem.getAddress());
				shopTel.setText(mItem.getTelNumber());
				shopDetail.setText(mItem.getDetail());
				image.setImageResource(mItem.getDrawableId());
			}
		});
		
		mMapView.getOverlays().add(myOverlay);
		mMapView.refresh();
	}
	
	
	
	public class BDLocationListenerImpl implements BDLocationListener {

		/**
		 * 接收异步返回的定位结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			
			MainActivity.this.location = location;
			
			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			//如果不显示定位精度圈，将accuracy赋值为0即可
			mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();
			
			//将定位数据设置到定位图层里
            myLocationOverlay.setData(mLocData);
            //更新图层数据执行刷新后生效
            mMapView.refresh();
            
            
			
            if(isFirstLoc || isRequest){
				mMapController.animateTo(new GeoPoint(
						(int) (location.getLatitude() * 1e6), (int) (location
								.getLongitude() * 1e6)));
				
	//			showPopupOverlay(location);
				
				isRequest = false;
            }
            
            isFirstLoc = false;
		}

		/**
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			
		}

	}
	/**
	 * 常用事件监听，用来处理通常的网络错误，授权验证错误等
	 * @author xiaanming
	 *
	 */
	public class MKGeneralListenerImpl implements MKGeneralListener{

		/**
		 * 一些网络状态的错误处理回调函数
		 */
		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				showToast("您的网络出错啦！");
            }
		}

		/**
		 * 授权错误的时候调用的回调函数
		 */
		@Override
		public void onGetPermissionState(int iError) {
			if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
				showToast("API KEY错误, 请检查！");
            }
		}
		
	}

	//
	private class LocationOverlay extends MyLocationOverlay{

		public LocationOverlay(MapView arg0) {
			super(arg0);
		}

		@Override
		protected boolean dispatchTap() {
//			showPopupOverlay(location);
			return super.dispatchTap();
		}

		@Override
		public void setMarker(Drawable arg0) {
			super.setMarker(arg0);
		}
		
		
		
	}
	/**
	 * 手动请求定位的方法
	 */
	public void requestLocation() {
		isRequest = true;
		
		if(mLocClient != null && mLocClient.isStarted()){
			showToast("正在定位......");
			mLocClient.requestLocation();
		}else{
			Log.d("LocSDK3", "locClient is null or not started");
		}
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}

		super.onResume();
	}
	 /** 
     * 显示Toast消息 
     * @param msg 
     */  
    private void showToast(String msg){  
        if(mToast == null){  
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);  
        }else{  
            mToast.setText(msg);  
            mToast.setDuration(Toast.LENGTH_SHORT);
        }  
        mToast.show();  
    } 
}
