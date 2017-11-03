package doext.define;

import core.object.DoUIModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;

public abstract class do_Picker_MAbstract extends DoUIModule {

	protected do_Picker_MAbstract() throws Exception {
		super();
	}

	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception {
		super.onInit();
		// 注册属性
		this.registProperty(new DoProperty("index", PropertyDataType.Number, "", false));
		this.registProperty(new DoProperty("fontSize", PropertyDataType.Number, "20", true));
		this.registProperty(new DoProperty("fontColor", PropertyDataType.String, "000000FF", false));
		this.registProperty(new DoProperty("fontStyle", PropertyDataType.String, "normal", false));
		this.registProperty(new DoProperty("selectedFontColor", PropertyDataType.String, "000000FF", false));
		this.registProperty(new DoProperty("selectedFontStyle", PropertyDataType.String, "normal", false));
	}
}