package xyz.aerii.nebulune.mixin.mixins;

import kotlin.Unit;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.athen.api.dungeon.terminals.TerminalAPI;
import xyz.aerii.athen.api.dungeon.terminals.TerminalType;
import xyz.aerii.athen.handlers.Smoothie;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.ITerminal;
import xyz.aerii.nebulune.Nebulune;
import xyz.aerii.nebulune.modules.TerminalSolver;

import java.util.List;

@Mixin(ITerminal.class)
public abstract class ITerminalMixin {
    @Final
    @Shadow
    private List<Click> list;

    @Shadow
    public abstract TerminalType getTerminalType();

    @Shadow
    protected abstract Click forSlot(int slot);

    @Shadow
    protected abstract boolean valid(Click click);

    @Shadow
    protected abstract void compute(int slot, ItemStack item);

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void nebulune$onOpen(CallbackInfo ci) {
        TerminalSolver.INSTANCE.setYearning(false);
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void nebulune$onClose(CallbackInfo ci) {
        TerminalSolver.INSTANCE.getClicks().clear();
    }

    @Inject(method = "click*", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;forSlot(I)Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/Click;", shift = At.Shift.AFTER), cancellable = true)
    private void nebulune$click(float mx, float my, float width, float height, int mouseButton, CallbackInfo ci) {
        int mode = TerminalSolver.INSTANCE.getMode();
        if (mode == 0) return;

        int slots = getTerminalType().getSlots();
        float gridW = 9 * 18f;
        float gridH = ((float) slots / 9) * 18f;
        float headerH = 24f;

        float ox = width / 2 - gridW / 2;
        float oy = height / 2 - (gridH + headerH) / 2;

        int x = (int) ((mx - ox) / 18);
        int y = (int) ((my - (oy + headerH)) / 18);
        if (x < 0 || x > 8 || y < 0) return;

        int slot = x + y * 9;
        if (slot >= slots) return;

        Click c = forSlot(slot);
        if (c == null || c.getButton() != mouseButton) return;

        nebulune$adjust(c);

        if (TerminalSolver.INSTANCE.getYearning()) TerminalSolver.INSTANCE.getClicks().add(c);
        else nebulune$clickClick(c);
        ci.cancel();
    }

    @ModifyVariable(method = "renderHeader", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private String nebulune$modifyHeaderText(String titleText) {
        if (TerminalSolver.INSTANCE.getMode() != 1) return titleText;

        String queueText = " | " + TerminalSolver.INSTANCE.getClicks().size();
        return titleText + queueText;
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;compute(ILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void nebulune$update(int slot, ItemStack item, CallbackInfo ci) {
        TerminalSolver.INSTANCE.setYearning(false);

        if (TerminalSolver.INSTANCE.getMode() != 1 || TerminalSolver.INSTANCE.getClicks().isEmpty()) return;

        Click next = TerminalSolver.INSTANCE.getClicks().getFirst();
        if (!valid(next)) {
            TerminalSolver.INSTANCE.getClicks().clear();
            return;
        }

        for (Click c : TerminalSolver.INSTANCE.getClicks()) nebulune$adjust(c);
        TerminalSolver.INSTANCE.getClicks().removeFirst();
        nebulune$clickClick(next);
    }

    @Unique
    private void nebulune$clickClick(Click click) {
        TerminalSolver.INSTANCE.setYearning(true);

        var client = Smoothie.getClient();
        var gameMode = client.gameMode;
        var player = client.player;
        if (gameMode == null || player == null) return;

        gameMode.handleInventoryMouseClick(
                TerminalAPI.INSTANCE.getLastId(),
                click.getSlot(),
                click.getButton() == 0 ? 2 : click.getButton(),
                click.getButton() == 0 ? ClickType.CLONE : ClickType.PICKUP,
                player
        );

        int id = TerminalAPI.INSTANCE.getLastId();
        int timeout = TerminalSolver.INSTANCE.getTimeout();

        Nebulune.after(timeout, () -> {
            if (!TerminalAPI.INSTANCE.getTerminalOpen().getValue() || id != TerminalAPI.INSTANCE.getLastId()) return Unit.INSTANCE;

            TerminalSolver.INSTANCE.getClicks().clear();
            compute(0, ItemStack.EMPTY);
            TerminalSolver.INSTANCE.setYearning(false);
            return Unit.INSTANCE;
        });
    }

    @Unique
    private void nebulune$adjust(Click click) {
        TerminalType type = getTerminalType();

        if (type == TerminalType.NUMBERS || type == TerminalType.PANES || type == TerminalType.NAME || type == TerminalType.COLORS) {
            list.remove(click);
        } else if (type == TerminalType.RUBIX) {
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getSlot() == click.getSlot()) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return;

            int next = list.get(index).getButton() + (click.getButton() == 0 ? -1 : 1);
            if (next == 0) list.remove(index);
            else list.set(index, new Click(click.getSlot(), next));
        }
    }
}