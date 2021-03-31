package com.mcmiddleearth.command.argument;

public interface HelpfulArgumentType {

    public void setTooltip(String tooltip);

    default public String getTooltip() {
        return null;
    }
}
