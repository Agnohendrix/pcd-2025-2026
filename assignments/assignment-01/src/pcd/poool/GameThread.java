package pcd.poool;

public class GameThread extends Thread {

	private final Board board;
	private final CommandMonitor commandMonitor;
	private final ViewModel viewModel;
	private final View view;
	private volatile boolean running = true;
	
	public GameThread(Board board, CommandMonitor commandMonitor, ViewModel viewModel, View view) {
		this.board = board;
		this.commandMonitor = commandMonitor;
		this.viewModel = viewModel;
		this.view = view;
	}
	
	@Override
	public void run() {
		long lastUpdateTime = System.currentTimeMillis();
		long startTime = lastUpdateTime;
		int nFrames = 0;
		
		while(running) {
			GameCommand command;
			
			while((command = commandMonitor.poll()) != null) {
				command.execute(board);
			}
			
			long now = System.currentTimeMillis();
			long elapsed = now - lastUpdateTime;
			lastUpdateTime = now;
			
			board.updateState(elapsed);
			
			nFrames++;
			long totalTime = now - startTime;
			int framesPerSecond = totalTime > 0 ?
					(int)(nFrames * 1000 / totalTime) :
						0;
			
			viewModel.update(board,  framesPerSecond);
			view.render();
		}
	}
	
	public void stopGame() {
		running = false;
	}
}
