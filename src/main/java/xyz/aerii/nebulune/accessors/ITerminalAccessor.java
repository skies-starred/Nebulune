package xyz.aerii.nebulune.accessors;

import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click;

import java.util.concurrent.CopyOnWriteArrayList;

public interface ITerminalAccessor {
    CopyOnWriteArrayList<Click> nebulune$getList();
}