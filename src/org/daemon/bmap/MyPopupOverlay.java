package org.daemon.bmap;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapView.LayoutParams;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.platform.comapi.map.Projection;


public class MyPopupOverlay extends ItemizedOverlay<OverlayItem> {

	private Context context = null;
	// 
	private LinearLayout popupLinear = null;
	// 
	private View popupView = null;
	private MapView mapView = null;
	private Projection projection = null;

	// 
	private int layoutId = 0;
	//
	private boolean useDefaultMarker = false;
	private int[] defaultMarkerIds = { R.drawable.icon_marka,
			R.drawable.icon_markb, R.drawable.icon_markc,
			R.drawable.icon_markd, R.drawable.icon_marke,
			R.drawable.icon_markf, R.drawable.icon_markg,
			R.drawable.icon_markh, R.drawable.icon_marki,
			R.drawable.icon_markj, };

	// 
	private OnTapListener onTapListener = null;

	public MyPopupOverlay(Context context, Drawable marker, MapView mMapView) {
		super(marker, mMapView);
		this.context = context;
		this.popupLinear = new LinearLayout(context);
		this.mapView = mMapView;
		popupLinear.setOrientation(LinearLayout.VERTICAL);
		popupLinear.setVisibility(View.GONE);
		projection = mapView.getProjection();
	}

	@Override
	public boolean onTap(GeoPoint pt, MapView mMapView) {
		// 
		if (popupLinear != null && popupLinear.getVisibility() == View.VISIBLE) {
			LayoutParams lp = (LayoutParams) popupLinear.getLayoutParams();
			Point tapP = new Point();
			projection.toPixels(pt, tapP);
			Point popP = new Point();
			projection.toPixels(lp.point, popP);
			int xMin = popP.x - lp.width / 2 + lp.x;
			int yMin = popP.y - lp.height + lp.y;
			int xMax = popP.x + lp.width / 2 + lp.x;
			int yMax = popP.y + lp.y;
			if (tapP.x < xMin || tapP.y < yMin || tapP.x > xMax
					|| tapP.y > yMax)
				popupLinear.setVisibility(View.GONE);
		}
		return false;
	}

	@Override
	protected boolean onTap(int i) {
		// 
		OverlayItem item = getItem(i);
		if (popupView == null) {
			// 
			if (!createPopupView()){
				return true;
			}
		}
		if (onTapListener == null)
			return true;
		popupLinear.setVisibility(View.VISIBLE);
		onTapListener.onTap(i, popupView);

		popupLinear.measure(0, 0);
		int viewWidth = popupLinear.getMeasuredWidth();
		int viewHeight = popupLinear.getMeasuredHeight();

		LayoutParams layoutParams = new LayoutParams(viewWidth, viewHeight,
				item.getPoint(), 0, -60, LayoutParams.BOTTOM_CENTER);
		layoutParams.mode = LayoutParams.MODE_MAP;

		popupLinear.setLayoutParams(layoutParams);
		Point p = new Point();
		projection.toPixels(item.getPoint(), p);
		p.y = p.y - viewHeight / 2;
		GeoPoint point = projection.fromPixels(p.x, p.y);

		mapView.getController().animateTo(point);
		return true;
	}

	private boolean createPopupView() {
		// TODO Auto-generated method stub
		if (layoutId == 0)
			return false;
		popupView = LayoutInflater.from(context).inflate(layoutId, null);
		popupView.setBackgroundResource(R.drawable.popupborder);
		ImageView dialogStyle = new ImageView(context);
		dialogStyle.setImageDrawable(context.getResources().getDrawable(
				R.drawable.iw_tail));
		popupLinear.addView(popupView);
		android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.topMargin = -2;
		lp.leftMargin = 60;
		popupLinear.addView(dialogStyle, lp);
		mapView.addView(popupLinear);
		return true;
	}

	@Override
	public void addItem(List<OverlayItem> items) {
		// TODO Auto-generated method stub
		int startIndex = getAllItem().size();
		for (OverlayItem item : items){
			if (startIndex >= defaultMarkerIds.length)
				startIndex = defaultMarkerIds.length - 1;
			if (useDefaultMarker && item.getMarker() == null){
				item.setMarker(context.getResources().getDrawable(
						defaultMarkerIds[startIndex++]));
			}
		}
		super.addItem(items);
	}

	@Override
	public void addItem(OverlayItem item) {
		// TODO Auto-generated method stub
		//
		int index = getAllItem().size();
		if (index >= defaultMarkerIds.length)
			index = defaultMarkerIds.length - 1;
		if (useDefaultMarker && item.getMarker() == null){
			item.setMarker(context.getResources().getDrawable(
					defaultMarkerIds[getAllItem().size()]));
		}
		super.addItem(item);
	}

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	public void setUseDefaultMarker(boolean useDefaultMarker) {
		this.useDefaultMarker = useDefaultMarker;
	}

	public void setOnTapListener(OnTapListener onTapListener) {
		this.onTapListener = onTapListener;
	}

	public interface OnTapListener {
		public void onTap(int index, View popupView);
	}
}
