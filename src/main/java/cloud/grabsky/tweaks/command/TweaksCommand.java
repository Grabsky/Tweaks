/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
