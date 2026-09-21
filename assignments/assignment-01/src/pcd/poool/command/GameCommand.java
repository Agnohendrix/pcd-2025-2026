package pcd.poool.command;

import pcd.poool.model.Board;

public interface GameCommand {

    void execute(Board board);
}