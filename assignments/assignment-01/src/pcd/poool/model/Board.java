package pcd.poool.model;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import pcd.poool.concurrent.CollisionPair;
import pcd.poool.concurrent.WorkerThread;

public class Board {

    public record PerformanceStats(long positionNanos, long collisionDetectionNanos,
            long collisionApplicationNanos, int detectedCollisions) {}

    private List<Ball> balls;    
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    
    private Hole playerHole;
    private Hole botHole;
    private int playerScore;
    private int botScore;
    private boolean playerBallInHole;
    private boolean botBallInHole;
    private WorkerThread[] workers;
    private PerformanceStats performanceStats = new PerformanceStats(0, 0, 0, 0);
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall(); 
    	botBall = conf.getBotBall();
        playerBallInHole = false;
        botBallInHole = false;
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

    	resolveCollisionsAndUpdateScores();
    }

    public void updateStateWithThreads(long dt, int threadCount) throws InterruptedException {
        long positionStart = System.nanoTime();
        playerBall.updateState(dt, this);
        botBall.updateState(dt, this);

        int workerCount = Math.max(1, Math.min(threadCount, balls.size()));
        ensureWorkers(workerCount);
        runInParallel(balls.size(), workerCount, (workerIndex, from, to) -> {
            for (int i = from; i < to; i++) {
                balls.get(i).updateState(dt, this);
            }
        });

        long positionNanos = System.nanoTime() - positionStart;
        resolveCollisionsAndUpdateScores(workerCount, positionNanos);
    }

    private void ensureWorkers(int workerCount) {
        if (workers != null && workers.length == workerCount) {
            return;
        }
        stopBallWorkers();
        workers = new WorkerThread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new WorkerThread(i);
            workers[i].start();
        }
    }

    public void stopBallWorkers() {
        if (workers != null) {
            for (WorkerThread worker : workers) {
                worker.stopWorker();
            }
            workers = null;
        }
    }

    private void runInParallel(int itemCount, int workerCount, RangeTask task)
            throws InterruptedException {
        int chunkSize = (itemCount + workerCount - 1) / workerCount;
        CountDownLatch completed = new CountDownLatch(workerCount);
        for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
            int taskIndex = workerIndex;
            int from = workerIndex * chunkSize;
            int to = Math.min(from + chunkSize, itemCount);
            workers[taskIndex].submit(() -> {
                try {
                    task.run(taskIndex, from, to);
                } finally {
                    completed.countDown();
                }
            });
        }
        completed.await();
    }

    @FunctionalInterface
    private interface RangeTask {
        void run(int workerIndex, int from, int to);
    }

    private void resolveCollisionsAndUpdateScores() {
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
        	if (isInHole(playerBall, playerHole) || isInHole(playerBall, botHole)) {
        		playerBallInHole = true;
        	}
            if (isInHole(botBall, playerHole) || isInHole(botBall, botHole)) {
                botBallInHole = true;
            }
    }

    private void resolveCollisionsAndUpdateScores(int workerCount, long positionNanos)
            throws InterruptedException {
        long detectionStart = System.nanoTime();
        List<List<CollisionPair>> collisionsByWorker = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
            collisionsByWorker.add(new ArrayList<>());
        }
        runInParallel(Math.max(0, balls.size() - 1), workerCount, (workerIndex, from, to) -> {
            List<CollisionPair> collisions = collisionsByWorker.get(workerIndex);
            for (int i = from; i < to; i++) {
                Ball first = balls.get(i);
                for (int j = i + 1; j < balls.size(); j++) {
                    Ball second = balls.get(j);
                    double dx = second.getPos().x() - first.getPos().x();
                    double dy = second.getPos().y() - first.getPos().y();
                    double distance = Math.hypot(dx, dy);
                    double minDistance = first.getRadius() + second.getRadius();
                    if (distance < minDistance && distance > 1e-6) {
                        collisions.add(new CollisionPair(first, second));
                    }
                }
            }
        });
        List<CollisionPair> detectedCollisions = new ArrayList<>();
        collisionsByWorker.forEach(detectedCollisions::addAll);
        long collisionDetectionNanos = System.nanoTime() - detectionStart;

        long applicationStart = System.nanoTime();
        for (CollisionPair pair : detectedCollisions) {
            Ball.resolveCollision(pair.first(), pair.second());
        }
        for (var ball : balls) {
            Ball.resolveCollision(playerBall, ball);
            Ball.resolveCollision(botBall, ball);
        }
        Ball.resolveCollision(botBall, playerBall);
        updateScoresAndHoleState();
        long collisionApplicationNanos = System.nanoTime() - applicationStart;
        performanceStats = new PerformanceStats(positionNanos, collisionDetectionNanos,
            collisionApplicationNanos, detectedCollisions.size());
    }

    private void updateScoresAndHoleState() {
        updateScores();
        if (isInHole(playerBall, playerHole) || isInHole(playerBall, botHole)) {
            playerBallInHole = true;
        }
        if (isInHole(botBall, playerHole) || isInHole(botBall, botHole)) {
            botBallInHole = true;
        }
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

    public boolean isPlayerBallInHole() {
        return playerBallInHole;
    }

    public boolean isBotBallInHole() {
        return botBallInHole;
    }

    public boolean areSmallBallsFinished() {
        return balls.isEmpty();
    }

    public PerformanceStats getPerformanceStats() {
        return performanceStats;
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
