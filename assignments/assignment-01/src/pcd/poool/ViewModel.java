package pcd.poool;

import java.util.ArrayList;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
	private BallViewInfo bot;
	private int framePerSec;
	
	private Hole playerHole;
	private Hole botHole;
	private int playerScore;
	private int botScore;
	
	public ViewModel() {
		balls = new ArrayList<BallViewInfo>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getPlayerBall();
		player = new BallViewInfo(p.getPos(), p.getRadius());
		
		var b = board.getBotBall();
		bot = new BallViewInfo(b.getPos(), b.getRadius());
		
		playerHole = board.getPlayerHole();
		botHole = board.getBotHole();
		playerScore = board.getPlayerScore();
		botScore = board.getBotScore();
	}
	
	public synchronized ArrayList<BallViewInfo> getBalls(){
		var copy = new ArrayList<BallViewInfo>();
		copy.addAll(balls);
		return copy;
		
	}

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	public synchronized BallViewInfo getPlayerBall() {
		return player;
	}
	
	public synchronized BallViewInfo getBotBall() {
		return bot;
	}
	
	public synchronized Hole getPlayerHole() {
		return playerHole;
	}

	public synchronized Hole getBotHole() {
		return botHole;
	}

	public synchronized int getPlayerScore() {
		return playerScore;
	}

	public synchronized int getBotScore() {
		return botScore;
	}
	
}
