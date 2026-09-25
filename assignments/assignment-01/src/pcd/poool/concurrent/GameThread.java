package pcd.poool.concurrent;

import java.util.Random;

import pcd.poool.command.GameCommand;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class GameThread extends Thread {

	private final Board board;
	private final CommandMonitor commandMonitor;
	private final ViewModel viewModel;
	private final View view;
	private volatile boolean running = true;
	private final Random botRandom = new Random(2);
	
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
		long lastBotKickTime = startTime;
		int nFrames = 0;
		
		while(running) {
			GameCommand command;
			
			while((command = commandMonitor.poll()) != null) {
				command.execute(board);
			}
			
			long botKickTime = System.currentTimeMillis();
			var botBall = board.getBotBall();
			if (botBall.getVel().abs() < 0.05 && botKickTime - lastBotKickTime > 2000) {
				double angle = botRandom.nextDouble() * Math.PI * 0.25;
				V2d velocity = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
				botBall.kick(velocity);
				lastBotKickTime = botKickTime;
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
