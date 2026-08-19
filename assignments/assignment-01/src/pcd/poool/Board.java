package pcd.poool;

import java.util.*;

record Hole(P2d pos, Double radius) {}

public class Board {

    private List<Ball> balls;    
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    
    private Hole playerHole;
    private Hole botHole;
    private int playerScore;
    private int botScore;
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall(); 
    	botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
    	playerHole = new Hole(new P2d(bounds.x0() , bounds.y1() ), 0.3);
        botHole = new Hole(new P2d(bounds.x1() , bounds.y1() ), 0.3);
    }
    
    public void updateState(long dt) {

    	playerBall.updateState(dt, this);
    	botBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}       	
    	
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
    	for (var b: balls) {
    		Ball.resolveCollision(playerBall, b);
            Ball.resolveCollision(botBall, b);
    	} 
    	Ball.resolveCollision(botBall, playerBall);
    	
    	updateScores();
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() {
    	return playerBall;
    }
    
    public Ball getBotBall() {
    	return botBall;
    }
    
    public  Boundary getBounds(){
        return bounds;
    }
    
    public Hole getPlayerHole() {
        return playerHole;
    }

    public Hole getBotHole() {
        return botHole;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getBotScore() {
        return botScore;
    }
    
    private void updateScores() {
        var iterator = balls.iterator();
        while (iterator.hasNext()) {
            var ball = iterator.next();
            if (isInHole(ball, playerHole)) {
                playerScore++;
                iterator.remove();
            } else if (isInHole(ball, botHole)) {
                botScore++;
                iterator.remove();
            }
        }
    }

    private boolean isInHole(Ball ball, Hole hole) {
        return ball.getPos().sub(hole.pos()).abs() <= hole.radius();
    }
}
