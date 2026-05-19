package xyz.aerii.nebulune.mixin.mixins.athen;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.athen.api.dungeon.terminals.TerminalType;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click;
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.ITerminal;
import xyz.aerii.nebulune.accessors.ITerminalAccessor;
import xyz.aerii.nebulune.modules.impl.dungeons.AutoTerms;
import xyz.aerii.nebulune.modules.impl.dungeons.HoverTerms;
import xyz.aerii.nebulune.modules.impl.dungeons.QueueTerms;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ITerminal.class)
public abstract class ITerminalMixin implements ITerminalAccessor {
    @Final
    @Shadow
    private CopyOnWriteArrayList<Click> list;

    @Final
    @Shadow
    private TerminalType terminalType;

    @Shadow
    public abstract TerminalType getTerminalType();

    @Shadow
    protected abstract int getInt0();

    @Shadow
    protected abstract int getInt1();

    @Shadow
    protected abstract float getFloat();

    @Shadow
    protected abstract Click forSlot(int slot);

    @Shadow
    protected abstract boolean valid(Click click);

    @Override
    public CopyOnWriteArrayList<Click> nebulune$getList() {
        return list;
    }

    @Override
    public int nebulune$int0() {
        return getInt0();
    }

    @Override
    public int nebulune$int1() {
        return getInt1();
    }

    @Override
    public float nebulune$float() {
        return getFloat();
    }

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void nebulune$onOpen(CallbackInfo ci) {
        QueueTerms.INSTANCE.setYearning(false);
        HoverTerms.INSTANCE.reset();
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void nebulune$onClose(CallbackInfo ci) {
        QueueTerms.INSTANCE.getClicks().clear();
        QueueTerms.INSTANCE.getList().clear();
        HoverTerms.INSTANCE.reset();
    }

    @Inject(method = "click*", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;forSlot(I)Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/Click;", shift = At.Shift.AFTER), cancellable = true)
    private void nebulune$click(float mx, float my, float width, float height, int mouseButton, CallbackInfo ci) {
        if (!QueueTerms.INSTANCE.getEnabled()) return;

        float sp = getFloat();
        float pad = TerminalSolver.INSTANCE.getUi$padding();
        int slots = getTerminalType().getSlots();
        float gridW = getInt0() * sp + 2 * pad;
        float gridH = ((float) slots / 9 - 2) * sp + 2 * pad;
        float headerH = TerminalSolver.INSTANCE.getUi$hideHeader() ? 0f : 20f;
        float padding = TerminalSolver.INSTANCE.getUi$hideHeader() ? 0f : 6f;

        float ox = width / 2 - gridW / 2;
        float oy = height / 2 - (gridH + headerH + padding) / 2;

        int x = (int) ((mx - ox - pad) / sp) + getInt1();
        int y = (int) ((my - (oy + headerH + padding) - pad) / sp) + 1;
        if (x < getInt1() || x >= getInt1() + getInt0() || y < 1) return;

        int slot = x + y * 9;
        if (slot >= slots) return;

        Click c = forSlot(slot);
        if (c == null) return;
        if (c.getButton() != mouseButton && !(terminalType == TerminalType.RUBIX && TerminalSolver.INSTANCE.getRubix$left())) return;

        nebulune$adjust(c);
        ci.cancel();

        if (QueueTerms.INSTANCE.getYearning()) {
            QueueTerms.INSTANCE.getClicks().add(c);
            return;
        }

        QueueTerms.INSTANCE.setYearning(true);
        QueueTerms.INSTANCE.getList().add(c);
    }

    @ModifyVariable(method = "main(FFFFF)V", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private String nebulune$modifyTitleText(String titleText) {
        if (!QueueTerms.INSTANCE.getEnabled()) return titleText;
        return titleText + " - " + QueueTerms.INSTANCE.getClicks().size() + QueueTerms.INSTANCE.getList().size();
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lxyz/aerii/athen/modules/impl/dungeon/terminals/solver/base/ITerminal;compute(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void nebulune$update(List<ItemStack> items, CallbackInfo ci) {
        QueueTerms.INSTANCE.setYearning(false);
        AutoTerms.onUpdate();

        if (!QueueTerms.INSTANCE.getEnabled()) return;
        if (QueueTerms.INSTANCE.getClicks().isEmpty()) return;

        Click next = QueueTerms.INSTANCE.getClicks().getFirst();
        if (!valid(next)) {
            QueueTerms.INSTANCE.getClicks().clear();
            return;
        }

        for (Click c : QueueTerms.INSTANCE.getClicks()) nebulune$adjust(c);
        QueueTerms.INSTANCE.getClicks().removeFirst();
        QueueTerms.INSTANCE.getList().add(next);
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