package com.mcmiddleearth.command;

import java.util.List;

public interface WrappedTabCompleteEvent {

    public void setCancelled(boolean cancelled);

    public boolean isCancelled();

    public CommandSender getSender();

    public List<String> getSuggestions();

    public String getCursor();
}
