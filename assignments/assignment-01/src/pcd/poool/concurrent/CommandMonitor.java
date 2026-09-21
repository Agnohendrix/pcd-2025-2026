package pcd.poool.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;

import pcd.poool.command.GameCommand;

public class CommandMonitor {
	
	private final Queue<GameCommand> commands = new ArrayDeque<>();
	
	public synchronized void submit(GameCommand command) {
		commands.add(command);
		notifyAll();
	}
	
	public synchronized GameCommand poll() {
		return commands.poll();
	}
}
