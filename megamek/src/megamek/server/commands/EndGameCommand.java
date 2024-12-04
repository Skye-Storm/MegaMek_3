/*
 * MegaMek - Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.server.commands;

import megamek.client.ui.Messages;
import megamek.common.options.OptionsConstants;
import megamek.server.Server;
import megamek.server.commands.arguments.Argument;
import megamek.server.commands.arguments.Arguments;
import megamek.server.commands.arguments.BooleanArgument;
import megamek.server.commands.arguments.IntegerArgument;
import megamek.server.totalwarfare.TWGameManager;

import java.util.List;

/**
 * The Server Command "/end" that will finish a game immediately declaring forced victory for a player or their team.
 *
 * @author Luana Coppio
 */
public class EndGameCommand extends GamemasterServerCommand {

    public static final String PLAYER_ID = "playerID";
    public static final String FORCE = "force";

    public EndGameCommand(Server server, TWGameManager gameManager) {
        super(server,
            gameManager,
            "end",
            Messages.getString("Gamemaster.cmd.endgame.help"),
            Messages.getString("Gamemaster.cmd.endgame.longName"));
    }

    @Override
    public List<Argument<?>> defineArguments() {
        return List.of(
            new IntegerArgument(PLAYER_ID, Messages.getString("Gamemaster.cmd.endgame.playerID")),
            new BooleanArgument(FORCE, Messages.getString("Gamemaster.cmd.endgame.force"), false));
    }

    @Override
    protected void runCommand(int connId, Arguments args) {
        int playerID = ((IntegerArgument) args.get(PLAYER_ID)).getValue();
        boolean force = ((BooleanArgument) args.get(FORCE)).getValue();

        var player = server.getGame().getPlayer(playerID);
        if (player == null) {
            server.sendServerChat(connId, Messages.getString("Gamemaster.cmd.endgame.playerNotFound"));
            return;
        }

        gameManager.forceVictory(player, force, true);
        server.sendServerChat(connId, Messages.getString("Gamemaster.cmd.endgame.success"));
    }
}
