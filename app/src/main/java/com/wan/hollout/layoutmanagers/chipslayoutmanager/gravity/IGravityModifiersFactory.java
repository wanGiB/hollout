package com.wan.hollout.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.layoutmanagers.chipslayoutmanager.SpanLayoutChildGravity;

public interface IGravityModifiersFactory {
    IGravityModifier getGravityModifier(@SpanLayoutChildGravity int gravity);
}
