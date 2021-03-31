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

public class ReasonArgumentType implements ArgumentType<String>, HelpfulArgumentType {

    private String tooltip = "Reason of your report. Quickly explain the misbehaviour.";

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String o =  reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return o;
    }

    @Override
    public Collection<String> getExamples() {
        return Collections.singletonList("used inappropriate language");
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        if(tooltip != null) {
            builder.suggest("Explain the inappropriate behaviour of " + context.getArgument("player", String.class),
                            new LiteralMessage(tooltip));
        } else {
            builder.suggest("Explain the inappropriate behaviour of " + context.getArgument("player", String.class));
        }
        return builder.buildFuture();
    }

    @Override
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }
}
