package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class ReactionMessageEvent {

    private String reaction;

    public ReactionMessageEvent(String reaction) {
        this.reaction = reaction;
    }

    public String getReaction() {
        return reaction;
    }

}
