package pcd.poool.view;

import pcd.poool.concurrent.CommandMonitor;

public class View {

	private ViewFrame frame;
	private ViewModel viewModel;
	
	public View(ViewModel model, CommandMonitor commandMonitor, int w, int h) {
		frame = new ViewFrame(model, commandMonitor, w, h);	
		this.viewModel = model;
	}

	public void show() {
		frame.setVisible(true);
		frame.requestFocusInWindow();
	}
		
	public void render() {
		frame.render();
	}

	public void setCloseHandler(Runnable closeHandler) {
		frame.setCloseHandler(closeHandler);
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}
}
