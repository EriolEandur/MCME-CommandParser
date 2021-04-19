package com.mcmiddleearth.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.command.node.HelpfulNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractCommandHandler {

    private final CommandDispatcher<McmeCommandSender> commandDispatcher = new CommandDispatcher<>();

    String command;

    public AbstractCommandHandler(String command)
    {
        this.command = command;
        commandDispatcher.register(createCommandTree( HelpfulLiteralBuilder.literal(command)));
    }

    /**
     * This method is called by the constructor to create a command tree based on the command passed to the constructor
     * Additional command trees may be added directly to the command dispatcher.
     * @param baseCommandNodeBuilder the base command node builder
     * @return same builder as passed in parameter
     */
    protected abstract HelpfulLiteralBuilder createCommandTree(HelpfulLiteralBuilder baseCommandNodeBuilder);

    public CommandDispatcher<McmeCommandSender> getCommandDispatcher() { return commandDispatcher; }

    public void execute(McmeCommandSender sender, String[] args) {
        execute(sender, command, args);
    }

    public void execute(McmeCommandSender sender, String command, String[] args) {
        try {
//CommandNode<CommandSender> node = dispatcher.getRoot();
//printTree(node);
            String message = String.format("%s %s", command, Joiner.on(' ').join(args)).trim();
            ParseResults<McmeCommandSender> result = commandDispatcher.parse(message, sender);
//Logger.getGlobal().info("nodes "+result.getContext().getNodes().size());
//Logger.getGlobal().info("Parsed");
            result.getExceptions().entrySet().stream()
                    .findFirst().ifPresent(error -> sender.sendMessage(new ComponentBuilder(error.getValue().getMessage())
                    .color(Style.ERROR).create()));
            if(result.getExceptions().isEmpty()) {
                if(result.getContext().getNodes().size() > 0
                        && (result.getContext().getCommand()==null
                            || result.getContext().getRange().getEnd() < result.getReader().getString().length())) {
                    //check for possible child nodes to collect suggestions and bake better error message
                    ComponentBuilder helpMessage;
                    boolean help = false;
                    String parsedCommand = "/" + result.getReader().getString()
                            .substring(0, result.getContext().getRange().getEnd());
                    if(result.getReader().getRemaining().trim().equals("help")){
                        helpMessage = new ComponentBuilder("Help for command "+parsedCommand+":").color(Style.INFO);
                        help = true;
                    } else {
                        helpMessage = new ComponentBuilder("Invalid command syntax.").color(Style.ERROR);
                    }
                    CommandNode<McmeCommandSender> parsedNode = result.getContext().getNodes().get(result.getContext().getNodes().size() - 1).getNode();
//Logger.getGlobal().info("Parsed Node:");
//printTree(parsedNode);
                    Collection<CommandNode<McmeCommandSender>> children = (result.getContext().getNodes().isEmpty()?new ArrayList<>():parsedNode.getChildren()
                            .stream().filter(node -> node.canUse(result.getContext().getSource())).collect(Collectors.toList()));
                    Map<CommandNode<McmeCommandSender>,String> use = commandDispatcher.getSmartUsage(parsedNode,result.getContext().getSource());
                    if (children.isEmpty()) {
                        if (result.getContext().getCommand() == null) {
                            helpMessage.append(" Maybe you don't have permission.");
                        } else if(!help) {
                            helpMessage.append(" Maybe you want to do:\n").append(parsedCommand).color(Style.INFO);
                        }
                    } else {
                        if(!help) {
                            helpMessage.append(" Maybe you want to do:");
                        }
                        for(Map.Entry<CommandNode<McmeCommandSender>,String> entry: use.entrySet()) {
                            String usageMessage = "";
                            helpMessage.append("\n").color(Style.INFO);
                            String[] visitedNodes = parsedCommand.split(" ");
                            Iterator<ParsedCommandNode<McmeCommandSender>> iterator = result.getContext().getNodes().listIterator();
                            for (String visitedNode : visitedNodes) {
                                helpMessage.append(" "+visitedNode);
                                ParsedCommandNode<McmeCommandSender> node = iterator.next();
//Logger.getGlobal().info("Visited Node:");
//printTree(node.getNode());
                                helpMessage.color((node.getNode() instanceof LiteralCommandNode?Style.LITERAL:Style.ARGUMENT));
                                if ((node.getNode() instanceof HelpfulNode)) {
                                    helpMessage.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new Text(new ComponentBuilder(((HelpfulNode) node.getNode()).getTooltip())
                                                    .color(Style.TOOLTIP).create())));
                                    if(!((HelpfulNode) node.getNode()).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node.getNode()).getHelpText();
                                    }
                                } else {
                                    helpMessage.event((HoverEvent)null);
                                }
                            }
                            String[] possibleNodes = entry.getValue().replace('|', ' ').split(" ");
                            CommandNode<McmeCommandSender> node = parsedNode;
                            CommandNode<McmeCommandSender> lastNode = parsedNode;
                            for(String possibleNode: possibleNodes) {
//Logger.getLogger(ModerationPluginCommand.class.getSimpleName()).info("possible node "+possibleNode);
                                helpMessage.append(" "+possibleNode);
                                CommandNode<McmeCommandSender> temp = node;
                                node = findDirectChild(node, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                if(node==null) {
                                    node = findDirectChild(lastNode, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                } else {
                                    lastNode = temp;
                                }
//Logger.getGlobal().info("possible Node:");
//printTree(node);
                                helpMessage.color((node instanceof LiteralCommandNode?Style.LITERAL:Style.ARGUMENT));
                                if ((node instanceof HelpfulNode)) {
                                    helpMessage.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new Text(new ComponentBuilder(((HelpfulNode) node).getTooltip())
                                                    .color(Style.TOOLTIP).create())));
                                    if(!((HelpfulNode) node).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node).getHelpText();
                                    }
                                } else {
                                    helpMessage.event((HoverEvent) null);
                                }
                            }
                            if(!usageMessage.equals("")) {
                                helpMessage.append(" : "+usageMessage).color(Style.HELP)
                                           .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(new ComponentBuilder().create())));
                            }
                        }
                    }
                    sender.sendMessage(helpMessage.create());
                } else if(result.getContext().getCommand() == null) {
                    sender.sendMessage(new ComponentBuilder("Invalid command. Maybe you don't have permission.")
                            .color(ChatColor.RED).create());
                } else {
                    commandDispatcher.execute(result);
                }
            }
        } catch (CommandSyntaxException e) {
            sender.sendMessage(new ComponentBuilder("Internal command parser exception!")
                    .color(ChatColor.RED).create());
        }
    }

    public void onTabComplete(TabCompleteRequest event) {
        try {
//Logger.getGlobal().info("onTabComplete cursor: "+event.getCursor().substring(1));
            ParseResults<McmeCommandSender> result = commandDispatcher.parse(event.getCursor().substring(1), event.getSender());
            if(result.getContext().getNodes().isEmpty()) {
                return;
            }
//Logger.getGlobal().info("onTabComplete nodes found");
            List<Suggestion> completionSuggestions
                    = commandDispatcher.getCompletionSuggestions(result).get().getList();
            if(completionSuggestions.isEmpty()) {
                event.setCancelled(true);
            } else {
                event.getSuggestions().addAll(completionSuggestions.stream().map(Suggestion::getText).collect(Collectors.toList()));
            }
//Logger.getGlobal().info("onTabComplete cancelled: "+event.isCancelled());
        } catch (InterruptedException | ExecutionException e) {
            event.getSender().sendMessage(new ComponentBuilder("Command tab complete error."+e).color(Style.ERROR).create());
        }
    }

    private CommandNode<McmeCommandSender> findDirectChild(CommandNode<McmeCommandSender> root, String name) {
//Logger.getLogger(ModerationPluginCommand.class.getSimpleName()).info("find node "+name);
        //if(root.getName().equals(name)) {
        //    return root;
        //} else {
            for(CommandNode<McmeCommandSender> node: root.getChildren()) {
                //CommandNode<CommandSender> found = findNode(node,name);
                if(node.getName().equals(name)) {//found != null) {
                    return node;//found;
                }
            }
        //}
        return null;
    }

    private void printTree(CommandNode<McmeCommandSender> node) {
        Logger log = Logger.getLogger(this.getClass().getSimpleName());
        log.info(printNode(node, "", "  "));
    }

    private String printNode(CommandNode<McmeCommandSender> node, String message, String indentation) {
        message = message + indentation+node.getClass().getSimpleName()+" "+node.getName()
                +"\n"+indentation+"-use: "+node.getUsageText();
        if(node instanceof HelpfulNode) {
            message = message
                    +"\n"+indentation+"-help: "+((HelpfulNode)node).getHelpText()
                    +"\n"+indentation+"-tool: "+((HelpfulNode)node).getTooltip();
        }
        for(CommandNode<McmeCommandSender> child: node.getChildren()) {
            message = printNode(child,message+"\n",indentation+"    ");
        }
        return message;
    }
}
