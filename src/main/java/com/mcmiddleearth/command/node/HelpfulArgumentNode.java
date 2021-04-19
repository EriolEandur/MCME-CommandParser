package com.mcmiddleearth.command.node;

import com.mcmiddleearth.command.McmeCommandSender;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class HelpfulArgumentNode<T> extends ArgumentCommandNode<McmeCommandSender, T> implements HelpfulNode {

    private String helpText;
    private final String tooltip;

    public HelpfulArgumentNode(String name, ArgumentType<T> type, Command<McmeCommandSender> command, Predicate<McmeCommandSender> requirement,
                               CommandNode<McmeCommandSender> redirect, RedirectModifier<McmeCommandSender> modifier, boolean forks,
                               SuggestionProvider<McmeCommandSender> customSuggestions, String helpText, String tooltip) {
        super(name, type, command, requirement, redirect, modifier, forks, customSuggestions);
        this.helpText = helpText;
        this.tooltip = tooltip;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String getHelpText() {
        return helpText;
    }

    @Override
    public void setHelpText(String helpText) {
        this.helpText = helpText;
        for(CommandNode<McmeCommandSender> child: getChildren()) {
            if(child instanceof HelpfulNode && ((HelpfulNode)child).getHelpText().equals("")) {
                ((HelpfulNode)child).setHelpText(helpText);
            }
        }
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(final CommandContext<McmeCommandSender> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        if(canUse(context.getSource())) {
            if (getCustomSuggestions() == null) {
                /*if (getType() instanceof HelpfulArgumentType) {
                    return ((HelpfulArgumentType) getType()).listSuggestions(context, builder, tooltip);
                } else {*/
                    return getType().listSuggestions(context, builder);
                //}
            } else {
                return getCustomSuggestions().getSuggestions(context, builder);
            }
        }
        return Suggestions.empty();
    }

    @Override
    public void addChild(CommandNode<McmeCommandSender> node) {
        super.addChild(node);
        CommandNode<McmeCommandSender> child = getChildren().stream().filter(search -> search.getName().equals(node.getName()))
                .findFirst().orElse(null);
        if(child instanceof HelpfulNode && ((HelpfulNode)child).getHelpText().equals("")) {
            ((HelpfulNode)child).setHelpText(helpText);
        }
    }

}
