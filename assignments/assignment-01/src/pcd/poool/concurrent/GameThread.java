package pcd.poool.concurrent;

import java.util.Random;

import pcd.poool.command.GameCommand;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class GameThread extends Thread {

	private static final int BALL_WORKER_COUNT = Math.max(1,
			Runtime.getRuntime().availableProcessors() - 1);

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
		long lastReportTime = startTime;
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
				//botBall.kick(velocity);
				lastBotKickTime = botKickTime;
			}
			
			long now = System.currentTimeMillis();
			long elapsed = now - lastUpdateTime;
			lastUpdateTime = now;
			
			try {
				board.updateStateWithThreads(elapsed, BALL_WORKER_COUNT);
			} catch (InterruptedException ex) {
				if (!running) {
					break;
				}
				Thread.currentThread().interrupt();
				break;
			}
			
			nFrames++;
			long totalTime = now - startTime;
			int framesPerSecond = totalTime > 0 ?
					(int)(nFrames * 1000 / totalTime) :
						0;
			
			viewModel.update(board,  framesPerSecond);
			view.render();
			long reportTime = System.currentTimeMillis();
			if (reportTime - lastReportTime >= 1000) {
				var stats = board.getPerformanceStats();
				System.out.printf(
						"fps=%d, positions=%.2f ms, collision detection=%.2f ms, "
								+ "collision application=%.2f ms, pairs=%d%n",
						nFrames * 1000 / Math.max(1, reportTime - startTime),
						stats.positionNanos() / 1_000_000.0,
						stats.collisionDetectionNanos() / 1_000_000.0,
						stats.collisionApplicationNanos() / 1_000_000.0,
						stats.detectedCollisions());
				lastReportTime = reportTime;
			}
			if (board.isPlayerBallInHole()) {
				running = false;
				board.stopBallWorkers();
				view.showGameOverMessageAndClose(
						"Sconfitta",
						"La palla del giocatore è entrata in buca.");
				break;
			} else if (board.isBotBallInHole()) {
				running = false;
				board.stopBallWorkers();
				view.showGameOverMessageAndClose(
						"Vittoria",
						"La palla del bot è entrata in buca.");
				break;
			} else if (board.areSmallBallsFinished()) {
				running = false;
				board.stopBallWorkers();
				String title;
				String message;
				if (board.getPlayerScore() > board.getBotScore()) {
					title = "Vittoria";
					message = "Hai vinto: non ci sono più palline."
							+ " Punteggio " + board.getPlayerScore()
							+ " - " + board.getBotScore() + ".";
				} else if (board.getPlayerScore() < board.getBotScore()) {
					title = "Sconfitta";
					message = "Il bot ha vinto: non ci sono più palline."
							+ " Punteggio " + board.getPlayerScore()
							+ " - " + board.getBotScore() + ".";
				} else {
					title = "Pareggio";
					message = "La partita è finita in pareggio: non ci sono più palline."
							+ " Punteggio " + board.getPlayerScore()
							+ " - " + board.getBotScore() + ".";
				}
				view.showGameOverMessageAndClose(title, message);
				break;
			}
		}
	}
	
	public void stopGame() {
		running = false;
		board.stopBallWorkers();
		interrupt();
	}
}
