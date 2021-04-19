package com.mcmiddleearth.command;

import java.util.List;

public interface TabCompleteRequest {

    public void setCancelled(boolean cancelled);

    public boolean isCancelled();

    public McmeCommandSender getSender();

    public List<String> getSuggestions();

    public String getCursor();
}
