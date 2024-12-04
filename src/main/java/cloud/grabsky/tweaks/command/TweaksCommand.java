/*
 * Tweaks (https://github.com/Grabsky/Tweaks)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.tweaks.command;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommand;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.annotation.Command;
import cloud.grabsky.commands.annotation.Dependency;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.tweaks.Tweaks;
import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@Command(name = "tweaks", permission = "tweaks.command.tweaks", usage = "/tweaks (...)")
public final class TweaksCommand extends RootCommand {

    @Dependency
    private @UnknownNullability Tweaks plugin;

    @Override
    public @NotNull CompletionsProvider onTabComplete(@NotNull final RootCommandContext context, final int index) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Returning list of sub-commands when no argument was specified in the input.
        if (index == 0 && sender.hasPermission(this.getPermission() + ".reload") == true)
            return CompletionsProvider.of("reload");
        // Returning empty completions provider when missing permission for that literal.
        return CompletionsProvider.EMPTY;
    }


    @Override
    public void onCommand(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws CommandLogicException {
        final CommandSender sender = context.getExecutor().asCommandSender();
        // Showing usage when no argument has been provided.
        if (arguments.hasNext() == false) {
            Message.of("<dark_gray>› <gray>Usage: <gold>/tweaks reload").send(sender);
            return;
        }
        // Getting first argument as String.
        final String argument = arguments.next(String.class).asRequired().toLowerCase();
        // Handling "/dialogs reload" command...
        if (argument.equalsIgnoreCase("reload") == true) {
            if (sender.hasPermission(this.getPermission() + ".reload") == true) {
                final boolean isSuccess = plugin.onReload();
                // Sending message to the sender.
                Message.of(isSuccess == true ? "<dark_gray>› <gray>Plugin <gold>Tweaks<gray> has been reloaded." : "<dark_gray>› <red>An error occurred while trying to reload <gold>Tweaks<red> plugin. See console for more details.").send(sender);
                return;
            }
            // Sending error message to the sender.
            Message.of("<dark_gray>› <red>Insufficient permissions.").send(sender);
            // Showing usage when invalid/unexpected argument has been provided.
        } else {
            Message.of("<dark_gray>› <gray>Usage: <gold>/tweaks reload").send(sender);
        }
    }
}
