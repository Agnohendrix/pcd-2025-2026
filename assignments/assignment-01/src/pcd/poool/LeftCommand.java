package pcd.poool;

public class LeftCommand implements GameCommand {

    @Override
    public void execute(Board board) {
        var player = board.getPlayerBall();
        player.kick(player.getVel().sum(new V2d(-1, 0)));
    }
}