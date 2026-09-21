package pcd.poool;

import java.util.ArrayDeque;
import java.util.Queue;

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
