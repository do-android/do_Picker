package doext.implement;

import java.util.Map;

import org.json.JSONObject;
import android.content.Context;
import android.widget.FrameLayout;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIListData;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoProperty;
import core.object.DoUIModule;
import doext.define.do_Picker_IMethod;
import doext.define.do_Picker_MAbstract;
import doext.implement.DoPickerView.OnSelectChangedListener;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_Picker_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_Picker_View extends FrameLayout implements DoIUIModuleView, do_Picker_IMethod, OnSelectChangedListener {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_Picker_MAbstract model;
	private Context ctx;
	private DoPickerView dpView;

	public do_Picker_View(Context context) {
		super(context);
		this.ctx = context;
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_Picker_MAbstract) _doUIModule;
		DoProperty _fontSizeProperty = _doUIModule.getProperty("fontSize");
		int _fontSize = 20;
		if (null != _fontSizeProperty) {
			_fontSize = DoTextHelper.strToInt(_fontSizeProperty.getValue(), 20);
		}
		dpView = new DoPickerView(ctx, null, _doUIModule, _fontSize);
		setPickerViewSize((int) _doUIModule.getRealWidth(), (int) _doUIModule.getRealHeight());
		this.addView(dpView);
		dpView.setOnSelectChangedListener(this);
	}

	private void setPickerViewSize(int width, int height) {
		dpView.setSize(width, height, 0);
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		if (_changedValues.containsKey("index")) {
			int _index = DoTextHelper.strToInt(_changedValues.get("index"), 0);
			dpView.setIndex(_index);
			fireSelectChanged(_index);
		}
		if (_changedValues.containsKey("fontColor")) {
			dpView.setFontColor(_changedValues.get("fontColor"));
		}
		if (_changedValues.containsKey("fontStyle")) {
			dpView.setFontStyle(_changedValues.get("fontStyle"));
		}
		if (_changedValues.containsKey("selectedFontColor")) {
			dpView.setSelectFontColor(_changedValues.get("selectedFontColor"));
		}
		if (_changedValues.containsKey("selectedFontStyle")) {
			dpView.setSelectFontStyle(_changedValues.get("selectedFontStyle"));
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("bindItems".equals(_methodName)) {
			bindItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("refreshItems".equals(_methodName)) {
			refreshItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	/**
	 * 绑定item的数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void bindItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _address = DoJsonHelper.getString(_dictParas, "data", "");
		if (_address == null || _address.length() <= 0)
			throw new Exception("do_Picker_View 未指定相关的listview data参数！");
		DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _address);
		if (_multitonModule == null)
			throw new Exception("do_Picker_View data参数无效！");
		if (_multitonModule instanceof DoIListData) {
			DoIListData _data = (DoIListData) _multitonModule;
			dpView.bindData(_data);
		}
	}

	/**
	 * 刷新item数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void refreshItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		dpView.refreshData();
	}

	@Override
	public void onChanged(int index) {
		try {
			if (!this.model.getPropertyValue("index").equals(index + "")) {
				this.model.setPropertyValue("index", index + "");
				fireSelectChanged(index);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fireSelectChanged(int index) {
		DoInvokeResult _result = new DoInvokeResult(this.model.getUniqueKey());
		_result.setResultInteger(index);
		this.model.getEventCenter().fireEvent("selectChanged", _result);
	}
}