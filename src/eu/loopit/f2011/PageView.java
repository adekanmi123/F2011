package eu.loopit.f2011;

public interface PageView {
	
	void initialize();
	
	/**
	 * Ask the view to refresh its data
	 */
	void load();
	
	boolean isLoading();

	void onFocusChange(boolean hasFocus);
}
