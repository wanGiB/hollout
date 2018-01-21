package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.gravity;


import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.SpanLayoutChildGravity;

public interface IGravityModifiersFactory {
    IGravityModifier getGravityModifier(@SpanLayoutChildGravity int gravity);
}
