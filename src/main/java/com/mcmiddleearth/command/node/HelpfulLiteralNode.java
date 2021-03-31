package com.mcmiddleearth.command.node;

import com.mcmiddleearth.command.CommandSender;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class HelpfulLiteralNode extends LiteralCommandNode<CommandSender> implements HelpfulNode {

    private String helpText;
    private final String tooltip;

    public HelpfulLiteralNode(String literal, Command<CommandSender> command, Predicate<CommandSender> requirement,
                              CommandNode<CommandSender> redirect, RedirectModifier<CommandSender> modifier,
                              boolean forks, String helpText, String tooltip) {
        super(literal, command, requirement, redirect, modifier, forks);
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
        for(CommandNode<CommandSender> child: getChildren()) {
            if(child instanceof HelpfulNode && ((HelpfulNode)child).getHelpText().equals("")) {
                ((HelpfulNode)child).setHelpText(helpText);
            }
        }
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSender> context, SuggestionsBuilder builder) {
        if (canUse(context.getSource()) && getLiteral().toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
            return builder.suggest(getLiteral(),new LiteralMessage(getHelpText())).buildFuture();
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    public void addChild(CommandNode<CommandSender> node) {
        super.addChild(node);
        CommandNode<CommandSender> child = getChildren().stream().filter(search -> search.getName().equals(node.getName()))
                .findFirst().orElse(null);
        if(node instanceof HelpfulNode && child instanceof HelpfulNode && ((HelpfulNode)child).getHelpText().equals("")) {
             ((HelpfulNode)child).setHelpText(helpText);
        }
    }
}
