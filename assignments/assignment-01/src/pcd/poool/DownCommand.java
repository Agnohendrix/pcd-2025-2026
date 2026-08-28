package pcd.poool;

public class DownCommand implements GameCommand {

    @Override
    public void execute(Board board) {
        var player = board.getPlayerBall();
        player.kick(player.getVel().sum(new V2d(0, -1)));
    }
}