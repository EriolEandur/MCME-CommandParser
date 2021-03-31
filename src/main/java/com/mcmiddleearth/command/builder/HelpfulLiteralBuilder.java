package com.mcmiddleearth.command.builder;

import com.mcmiddleearth.command.CommandSender;
import com.mcmiddleearth.command.node.HelpfulLiteralNode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

public class HelpfulLiteralBuilder extends LiteralArgumentBuilder<CommandSender> {

    private String helpText;
    private String tooltip;

    protected HelpfulLiteralBuilder(String literal) {
        super(literal);
        helpText = "";
        tooltip = "";
    }

    /**
     * Create a new literal command node with help text.
     * @param name name of the node
     * @return a HelpfulLiteralBuilder object to build the node.
     */
    public static HelpfulLiteralBuilder literal(final String name) {
        return new HelpfulLiteralBuilder(name);
    }

    /**
     * Add a help text to a command node. It will be displayed in command help messages. Help texts are inherited
     * to child nodes.
     * Example: /report <player> <reason> : >Here the help text is displayed<
     * @param helpText text to display in help messages.
     * @return this HelpfulLiteralBuilder
     */
    public HelpfulLiteralBuilder withHelpText(String helpText) {
        this.helpText = helpText;
        return getThis();
    }

    /**
     * Add a tooltip to a command node. It will be displayed when a user hovers over a command help message.
     * @param tooltip tooltip to display
     * @return  this HelpfulLiteralBuilder
     */
    public HelpfulLiteralBuilder withTooltip(String tooltip) {
        this.tooltip = tooltip;
        return getThis();
    }

    @Override
    public HelpfulLiteralBuilder getThis() {
        return this;
    }

    @Override
    public HelpfulLiteralNode build() {
        final HelpfulLiteralNode result = new HelpfulLiteralNode(getLiteral(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(),
                                                   isFork(), helpText, tooltip);

        for (final CommandNode<CommandSender> argument : getArguments()) {
            result.addChild(argument);
        }

        return result;
    }
}
