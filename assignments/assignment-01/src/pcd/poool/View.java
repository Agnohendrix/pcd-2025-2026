package pcd.poool;


public class View {

	private ViewFrame frame;
	private ViewModel viewModel;
	
	public View(ViewModel model, Board board, int w, int h) {
		frame = new ViewFrame(model, board, w, h);	
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}
}
