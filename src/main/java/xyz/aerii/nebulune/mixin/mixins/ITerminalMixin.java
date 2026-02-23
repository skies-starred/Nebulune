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
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.TerminalSimulator;
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.base.ITerminalSim;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.ITerminal;
import xyz.aerii.nebulune.Nebulune;
import xyz.aerii.nebulune.modules.TerminalHelper;

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
        TerminalHelper.INSTANCE.setYearning(false);
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void nebulune$onClose(CallbackInfo ci) {
        TerminalHelper.INSTANCE.getClicks().clear();
    }

    @Inject(method = "click*", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;forSlot(I)Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/Click;", shift = At.Shift.AFTER), cancellable = true)
    private void nebulune$click(float mx, float my, float width, float height, int mouseButton, CallbackInfo ci) {
        if (TerminalHelper.INSTANCE.getMode() != 1) return;

        int slots = getTerminalType().getSlots();
        float gridW = 9 * 18f;
        float gridH = ((float) slots / 9) * 18f;
        float headerH = 26f;

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

        if (TerminalHelper.INSTANCE.getYearning()) TerminalHelper.INSTANCE.getClicks().add(c);
        else nebulune$clickClick(c);
        ci.cancel();
    }

    @Inject(method = "main(FFFFF)V", at = @At("TAIL"))
    private void nebulune$hover(float ox, float oy, float gridW, float headerH, float uiScale, CallbackInfo ci) {
        if (TerminalHelper.INSTANCE.getMode() != 1) return;
        if (!TerminalHelper.INSTANCE.getHoverTerms()) return;
        if (System.currentTimeMillis() - TerminalAPI.INSTANCE.getOpenTime() < TerminalSolver.INSTANCE.getFcDelay()) return;

        var client = Smoothie.getClient();
        float mx = (float) (client.mouseHandler.xpos() / uiScale);
        float my = (float) (client.mouseHandler.ypos() / uiScale);

        int xSlot = (int) ((mx - ox) / 18);
        int ySlot = (int) ((my - (oy + headerH + 6f)) / 18);
        if (xSlot < 0 || xSlot > 8 || ySlot < 0) return;

        int slot = xSlot + ySlot * 9;
        if (slot >= getTerminalType().getSlots()) return;

        Click c = forSlot(slot);
        if (c == null) return;

        var clicks = TerminalHelper.INSTANCE.getClicks();
        if (clicks.stream().anyMatch(it -> it.getSlot() == c.getSlot())) return;

        nebulune$adjust(c);
        if (TerminalHelper.INSTANCE.getYearning()) clicks.add(c);
        else nebulune$clickClick(c);
    }

    @ModifyVariable(method = "main(FFFFF)V", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private String nebulune$modifyTitleText(String titleText) {
        if (TerminalHelper.INSTANCE.getMode() != 1) return titleText;
        return titleText + " | " + TerminalHelper.INSTANCE.getClicks().size();
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;compute(ILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void nebulune$update(int slot, ItemStack item, CallbackInfo ci) {
        TerminalHelper.INSTANCE.setYearning(false);

        if (TerminalHelper.INSTANCE.getMode() != 1) return;
        if (TerminalHelper.INSTANCE.getClicks().isEmpty()) return;

        Click next = TerminalHelper.INSTANCE.getClicks().getFirst();
        if (!valid(next)) {
            TerminalHelper.INSTANCE.getClicks().clear();
            return;
        }

        for (Click c : TerminalHelper.INSTANCE.getClicks()) nebulune$adjust(c);
        TerminalHelper.INSTANCE.getClicks().removeFirst();
        nebulune$clickClick(next);
    }

    @Unique
    private void nebulune$clickClick(Click click) {
        TerminalHelper.INSTANCE.setYearning(true);

        if (TerminalSimulator.INSTANCE.getS().getValue()) {
            var client = Smoothie.getClient();
            var screen = client.screen;
            if (!(screen instanceof ITerminalSim sim)) return;

            var slots = sim.getMenu().slots;
            int slotIndex = click.getSlot();
            if (slotIndex >= slots.size()) return;

            var slot = slots.get(slotIndex);
            sim.slotClicked(slot, slotIndex, click.getButton(), click.getButton() == 0 ? ClickType.CLONE : ClickType.PICKUP);

            if (TerminalSolver.INSTANCE.getSound$enabled()) {
                var sound = TerminalSolver.INSTANCE.getSound$click();
                if (sound != null) Smoothie.play(sound, TerminalSolver.INSTANCE.getSound$volume(), TerminalSolver.INSTANCE.getSound$pitch());
            }

            return;
        }

        var client = Smoothie.getClient();
        var gameMode = client.gameMode;
        var player = client.player;
        if (gameMode == null || player == null) return;

        if (TerminalSolver.INSTANCE.getSound$enabled()) {
            var sound = TerminalSolver.INSTANCE.getSound$click();
            if (sound != null) Smoothie.play(sound, TerminalSolver.INSTANCE.getSound$volume(), TerminalSolver.INSTANCE.getSound$pitch());
        }

        gameMode.handleInventoryMouseClick(
                TerminalAPI.INSTANCE.getLastId(),
                click.getSlot(),
                click.getButton() == 0 ? 2 : click.getButton(),
                click.getButton() == 0 ? ClickType.CLONE : ClickType.PICKUP,
                player
        );

        int id = TerminalAPI.INSTANCE.getLastId();
        int timeout = TerminalHelper.INSTANCE.getTimeout();

        Nebulune.after(timeout, () -> {
            if (!TerminalAPI.INSTANCE.getTerminalOpen().getValue()) return Unit.INSTANCE;
            if (id != TerminalAPI.INSTANCE.getLastId()) return Unit.INSTANCE;

            TerminalHelper.INSTANCE.getClicks().clear();
            compute(0, ItemStack.EMPTY);
            TerminalHelper.INSTANCE.setYearning(false);
            return Unit.INSTANCE;
        });
    }

    @Unique
    private void nebulune$adjust(Click click) {
        TerminalType type = getTerminalType();

        if (type == TerminalType.NUMBERS || type == TerminalType.PANES || type == TerminalType.NAME || type == TerminalType.COLORS) {
            list.remove(click);
            return;
        }

        if (type == TerminalType.RUBIX) {
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