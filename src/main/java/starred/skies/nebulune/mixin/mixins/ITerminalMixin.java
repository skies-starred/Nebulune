package starred.skies.nebulune.mixin.mixins;

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
import starred.skies.athen.api.dungeon.terminals.TerminalAPI;
import starred.skies.athen.api.dungeon.terminals.TerminalType;
import starred.skies.athen.handlers.Smoothie;
import starred.skies.athen.modules.impl.dungeon.terminals.solver.base.Click;
import starred.skies.athen.modules.impl.dungeon.terminals.solver.base.ITerminal;
import starred.skies.nebulune.Nebulune;
import starred.skies.nebulune.modules.TerminalSolver;

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
        TerminalSolver.yearning = false;
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void nebulune$onClose(CallbackInfo ci) {
        TerminalSolver.clicks.clear();
    }

    @Inject(method = "click*", at = @At(value = "INVOKE", target = "Lstarred/skies/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;forSlot(I)Lstarred/skies/athen/modules/impl/dungeon/terminals/solver/base/Click;", shift = At.Shift.AFTER), cancellable = true)
    private void nebulune$click(float mx, float my, float width, float height, int mouseButton, CallbackInfo ci) {
        int mode = TerminalSolver.getMode();
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

        if (TerminalSolver.yearning) TerminalSolver.clicks.add(c);
        else nebulune$clickClick(c);
        ci.cancel();
    }

    @ModifyVariable(method = "renderHeader", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private String nebulune$modifyHeaderText(String titleText) {
        if (TerminalSolver.getMode() != 1) return titleText;

        String queueText = " | " + TerminalSolver.clicks.size();
        return titleText + queueText;
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lstarred/skies/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;compute(ILnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void nebulune$update(int slot, ItemStack item, CallbackInfo ci) {
        TerminalSolver.yearning = false;

        if (TerminalSolver.getMode() != 1 || TerminalSolver.clicks.isEmpty()) return;

        Click next = TerminalSolver.clicks.getFirst();
        if (!valid(next)) {
            TerminalSolver.clicks.clear();
            return;
        }

        for (Click c : TerminalSolver.clicks) nebulune$adjust(c);
        TerminalSolver.clicks.removeFirst();
        nebulune$clickClick(next);
    }

    @Unique
    private void nebulune$clickClick(Click click) {
        TerminalSolver.yearning = false;

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
        int timeout = TerminalSolver.getTimeout();

        Nebulune.after(timeout, () -> {
            if (!TerminalAPI.INSTANCE.getTerminalOpen().getValue() || id != TerminalAPI.INSTANCE.getLastId()) return Unit.INSTANCE;

            TerminalSolver.clicks.clear();
            compute(0, ItemStack.EMPTY);
            TerminalSolver.yearning = false;
            return Unit.INSTANCE;
        });
    }

    @Unique
    private void nebulune$adjust(Click click) {
        TerminalType type = getTerminalType();
        
        if (type == TerminalType.NUMBERS || type == TerminalType.PANES || 
            type == TerminalType.NAME || type == TerminalType.COLORS) {
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
            if (next == 0) list.remove(index);else list.set(index, new Click(click.getSlot(), next));
        }
    }
}