package doext.implement;

import org.json.JSONException;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import core.DoServiceContainer;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIListData;
import core.object.DoUIModule;

public class DoPickerView extends FrameLayout implements OnScrollListener, OnTouchListener {

//	public static int MAX_FONT_COLOR = 255;
//	public static int MIN_FONT_COLOR = 3;

	private int cellHeight = 100;
	private int maxFontSize = 20;
	private int minFontSize = 8;
	private int increase = 4;

	private int[] fontSizes;
	private int[] fontColors;

	private String fontStyle;

	private ListView mListView;
	private MyAdapter mAdapter;
	private Context mContext;

	private int visibleCount;
	private int firstVisibleItemPos = 0;

	private Vibrator mVibrator;

	private String selectFontStyle = "normal";
	private String selectFontColor = "000000FF";

	private OnSelectChangedListener changedListener;

	public void setOnSelectChangedListener(OnSelectChangedListener listener) {
		this.changedListener = listener;
	}

	public DoPickerView(Context context, DoIListData data, DoUIModule uiModule, int _fontSize) {
		super(context);
		this.mContext = context;
		if (_fontSize < 12) {
			_fontSize = 12;
		}
		this.maxFontSize = _fontSize;
		this.minFontSize = _fontSize - 8;
//		this.cellHeight = (int) (cellHeight * uiModule.getYZoom());

		this.maxFontSize = getDeviceFontSize(uiModule, maxFontSize + "");
		this.minFontSize = getDeviceFontSize(uiModule, minFontSize + "");
		this.increase = getDeviceFontSize(uiModule, increase + "");

		this.cellHeight = getFontHeight(context, this.maxFontSize, uiModule.getYZoom());
		init(data);
	}

	public static int getFontHeight(Context context, int fontSize, double yZoom) {
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		FontMetrics fm = paint.getFontMetrics();
		double fontHeight = Math.ceil(fm.bottom - fm.top) + (10 * yZoom);
		return (int) fontHeight;
	}

	public static int getDeviceFontSize(DoUIModule _uiModule, String _fontSize) {
		int _convertFontSize = DoTextHelper.strToInt(_fontSize, 17);
		int _convertSize = (int) Math.round(_convertFontSize * _uiModule.getYZoom());
		if (_convertSize <= 0)
			_convertSize = 1;
		if (_convertSize > 32767)
			_convertSize = 32767;
		return _convertSize;
	}

	public void setFontStyle(String _fontStyle) {
		this.fontStyle = _fontStyle;
		refreshData();
	}

	public void setFontColor(String _color) {
		convertFontColors(_color);
		refreshData();
	}

	public void setSelectFontStyle(String _fontStyle) {
		this.selectFontStyle = _fontStyle;
		refreshData();
	}

	public void setSelectFontColor(String _color) {
		this.selectFontColor = _color;
		refreshData();
	}

	public void bindData(DoIListData data) {
		if (null != mAdapter) {
			int count = this.getVisibleCount() / 2;
			mAdapter.bindData(data, count);
			setSelection(this.firstVisibleItemPos, false);
		}
	}

	public void setIndex(int i) {
		this.firstVisibleItemPos = i;
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
			setSelection(this.firstVisibleItemPos, false);
		}
	}

	public void refreshData() {
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void init(DoIListData data) {
		mListView = new ListView(mContext);
		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mListView.setDividerHeight(0);
		mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

		mListView.setOnScrollListener(this);
		mListView.setOnTouchListener(this);
		FrameLayout.LayoutParams lv_lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mListView, lv_lp);

		mVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);

	}

	public void setSize(int width, int height, int left) {

		this.visibleCount = height / cellHeight;
		this.visibleCount = visibleCount % 2 == 0 ? --visibleCount : visibleCount;

		fillData();

		FrameLayout.LayoutParams mpv_lp = new LayoutParams(width, visibleCount * cellHeight);
		mpv_lp.gravity = Gravity.CENTER_VERTICAL;
		mpv_lp.leftMargin = left;
		this.setLayoutParams(mpv_lp);

		View topView = new View(mContext);
		FrameLayout.LayoutParams view_lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		view_lp.topMargin = (mpv_lp.height - cellHeight) / 2;
		topView.setBackgroundColor(Color.parseColor("#C7C7C7"));

		View topView2 = new View(mContext);
		FrameLayout.LayoutParams view_lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		view_lp2.topMargin = view_lp.topMargin + cellHeight;
		topView2.setBackgroundColor(Color.parseColor("#C7C7C7"));

		this.addView(topView, view_lp);
		this.addView(topView2, view_lp2);

	}

	private class MyAdapter extends BaseAdapter {

		private DoIListData mData;
		private int invisibleCount;
		private int totalCount;

		protected void bindData(DoIListData _listData, int _topCount) {
			this.mData = _listData;
			this.invisibleCount = _topCount;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mData == null) {
				totalCount = 0;
			} else {
				totalCount = mData.getCount() + (invisibleCount * 2);
			}
			return totalCount;
		}

		@Override
		public Object getItem(int position) {
			try {
				if (position < invisibleCount || position > (totalCount - invisibleCount - 1)) {
					return "";
				}
				return mData.getData(position - invisibleCount);
			} catch (JSONException e) {
				DoServiceContainer.getLogEngine().writeError("do_Picker_View getItem \n\t", e);
			}
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(mContext);
			tv.setText(getItem(position) + "");
			int pos = getRelePosition(position);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizes[pos]);
			if (centerCount == pos) {
				tv.setTextColor(DoUIModuleHelper.getColorFromString(selectFontColor, Color.BLACK));
				DoUIModuleHelper.setFontStyle(tv, selectFontStyle);
			} else {
				tv.setTextColor(fontColors[pos]);
				DoUIModuleHelper.setFontStyle(tv, fontStyle);
			}

			tv.setGravity(Gravity.CENTER);
			tv.setHeight(cellHeight);

			return tv;
		}

	}

	private int getRelePosition(int position) {
		for (int i = 0; i < this.visibleCount; i++) {
			if (position == (firstVisibleItemPos + i)) {
				return i;
			}
		}
		return 0;
	}

	public int getVisibleCount() {
		return this.visibleCount;
	}

	int centerCount;

	private void fillData() {
		fontSizes = new int[visibleCount];
		fontColors = new int[visibleCount];
		// 根据显示的个数求两边的个数
		centerCount = visibleCount / 2;
		fontSizes[centerCount] = maxFontSize;

		for (int i = centerCount - 1; i >= 0; i--) {
			fontSizes[i] = fontSizes[i + 1] - increase;
		}

		for (int i = centerCount + 1; i <= visibleCount - 1; i++) {
			fontSizes[i] = fontSizes[centerCount - i + centerCount];
		}
		convertFontColors("000000FF");
	}

	private void convertFontColors(String _color) {
		int _mColor = DoUIModuleHelper.getColorFromString(_color, Color.BLACK);
		// 根据显示的个数求两边的个数
		fontColors[centerCount] = Color.alpha(_mColor);

		for (int i = centerCount - 1; i >= 0; i--) {
			fontColors[i] = fontColors[i + 1] - 20;
		}

		for (int i = centerCount + 1; i <= visibleCount - 1; i++) {
			fontColors[i] = fontColors[centerCount - i + centerCount];
		}

		for (int i = 0; i < fontColors.length; i++) {
			fontColors[i] = getColor(_mColor, fontColors[i]);
		}
	}

	private int getColor(int _color, int _i) {
		int _r = Color.red(_color);
		int _g = Color.green(_color);
		int _b = Color.blue(_color);
		return Color.argb(_i, _r, _g, _b);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 当屏幕停止时候，自动滚动，调整距离
		if (SCROLL_STATE_IDLE == scrollState) {
			this.mAdapter.notifyDataSetChanged();
			mListView.setSelection(this.firstVisibleItemPos);
			if (null != changedListener) {
				changedListener.onChanged(this.firstVisibleItemPos);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		double scrollY = getScrollDistance();
		double distance = scrollY % cellHeight;
		int v = (int) (scrollY / cellHeight);
		int pos = v;
		if (distance >= (cellHeight / 2)) {
			pos = v + 1;
		}
		this.firstVisibleItemPos = pos;
		setSelection(pos, true);
		// this.mAdapter.notifyDataSetChanged();
	}

	private int oldPos;

	private void setSelection(int pos, boolean isVibrator) {
		if (pos == oldPos) {
			return;
		}
		if (pos < 0) {
			pos = 0;
		}
		if (pos > mListView.getCount()) {
			pos = mListView.getCount() - 1;
		}
		mListView.setSelection(pos);
		if (null != mVibrator && isVibrator) {
			mVibrator.vibrate(5);
		}
		oldPos = pos;
	}

	private double getScrollDistance() {
		View c = mListView.getChildAt(0);
		if (c == null) {
			return 0;
		}
		int firstVisiblePosition = mListView.getFirstVisiblePosition();
		int top = c.getTop();
		return -top + firstVisiblePosition * c.getHeight();
	}

	public interface OnSelectChangedListener {
		void onChanged(int index);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		if (arg1.getAction() == MotionEvent.ACTION_OUTSIDE || arg1.getAction() == MotionEvent.ACTION_CANCEL || arg1.getAction() == MotionEvent.ACTION_UP) {
			this.mAdapter.notifyDataSetChanged();
			mListView.setSelection(this.firstVisibleItemPos);
			if (null != changedListener) {
				changedListener.onChanged(this.firstVisibleItemPos);
			}
		}
		return false;
	}

}
