package pcd.poool;

import java.util.Random;

import pcd.poool.concurrent.CommandMonitor;
import pcd.poool.concurrent.GameThread;
import pcd.poool.model.Board;
import pcd.poool.model.LargeBoardConf;
import pcd.poool.model.V2d;
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
		View view = new View(viewModel, commandMonitor, 1200, 800);
		
		GameThread gameThread = new GameThread(board, commandMonitor, viewModel, view);
		gameThread.start();
						
		viewModel.update(board, 0);			
		view.render();
		waitAbit();

		int nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();
			
		var pb = board.getBotBall();
		var rand = new Random(2);
		var lastKickTime = t0;
				
		/* main simulation loop */
		
		while (true){			
		
			/* if the player ball is stopped and 5 secs have elapsed, then kick the player ball */

			if (pb.getVel().abs() < 0.05 && System.currentTimeMillis() - lastKickTime > 2000) {
				var angle = rand.nextDouble()*Math.PI*0.25;
				var v = new V2d(Math.cos(angle),Math.sin(angle)).mul(1.5);
				pb.kick(v);
				lastKickTime = System.currentTimeMillis();
			}
			
			/* update board state */
			
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();			
			//board.updateState(elapsed);
			
			/* render */
			
			nFrames++;
			int framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = (int)(nFrames*1000/dt);
			}

			//viewModel.update(board, framePerSec);			
			//view.render();
			
		}
	}
	
	private static void waitAbit() {
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {}
	}
	
}
