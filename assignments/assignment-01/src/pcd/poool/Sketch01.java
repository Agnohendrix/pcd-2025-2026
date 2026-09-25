package pcd.poool;

import pcd.poool.concurrent.CommandMonitor;
import pcd.poool.concurrent.GameThread;
import pcd.poool.model.Board;
import pcd.poool.model.LargeBoardConf;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class Sketch01 {

	
	public static void main(String[] argv) {

		/* 
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls 
		 */
		
		//var boardConf = new MinimalBoardConf();
		 var boardConf = new LargeBoardConf();
		// var boardConf = new MassiveBoardConf();
		
		Board board = new Board();
		board.init(boardConf);
		
		CommandMonitor commandMonitor = new CommandMonitor();
		
		ViewModel viewModel = new ViewModel();
		viewModel.update(board, 0);
		View view = new View(viewModel, commandMonitor, 1200, 800);
		
		GameThread gameThread = new GameThread(board, commandMonitor, viewModel, view);
		view.setCloseHandler(gameThread::stopGame);
		view.show();
		gameThread.start();
	}
}
