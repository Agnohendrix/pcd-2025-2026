package pcd.poool.command;

import pcd.poool.model.Board;
import pcd.poool.model.V2d;

public class RightCommand implements GameCommand {

    @Override
    public void execute(Board board) {
        var player = board.getPlayerBall();
        player.kick(player.getVel().sum(new V2d(1, 0)));
    }
}