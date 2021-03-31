/*
 * Copyright (C) 2020 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Eriol_Eandur
 */

public abstract class AbstractPlayerArgumentType implements ArgumentType<String>, HelpfulArgumentType {

    private String tooltip;

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public Collection<String> getExamples() {
        return Collections.singletonList("any_player_name");
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (String option : getPlayerSuggestions()) {
            if (option.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                if(tooltip == null) {
                    builder.suggest(option);
                } else {
                    builder.suggest(option, new LiteralMessage(tooltip));
                }
            }
        }
        return builder.buildFuture();
    }

    protected abstract Collection<String> getPlayerSuggestions();

    @Override
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }
}
