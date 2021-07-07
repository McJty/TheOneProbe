package mcjty.theoneprobe.apiimpl.elements;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IEntityStyle;
import mcjty.theoneprobe.api.IIconStyle;
import mcjty.theoneprobe.api.IItemStyle;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITextStyle;
import mcjty.theoneprobe.api.TankReference;
import mcjty.theoneprobe.apiimpl.ProbeInfo;
import mcjty.theoneprobe.apiimpl.styles.EntityStyle;
import mcjty.theoneprobe.apiimpl.styles.IconStyle;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import mcjty.theoneprobe.apiimpl.styles.TextStyle;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractElementPanel implements IElement, IProbeInfo {

    protected List<IElement> children = new ArrayList<>();
    protected ILayoutStyle layout;
    protected IProbeConfig overriddenConfig;

    @Override
    public void render(MatrixStack matrixStack, int x, int y) {
        Integer borderColor = layout.getBorderColor();
        if (borderColor != null) {
            int w = getWidth();
            int h = getHeight();
            RenderHelper.drawHorizontalLine(matrixStack, x, y, x + w - 1, borderColor);
            RenderHelper.drawHorizontalLine(matrixStack, x, y + h - 1, x + w - 1, borderColor);
            RenderHelper.drawVerticalLine(matrixStack, x, y, y + h - 1, borderColor);
            RenderHelper.drawVerticalLine(matrixStack, x + w - 1, y, y + h, borderColor);
        }
    }

    public AbstractElementPanel(ILayoutStyle style) {
        this.layout = style;
    }

    @Deprecated
    public AbstractElementPanel(Integer borderColor, int spacing, ElementAlignment alignment) {
        this(new LayoutStyle().borderColor(borderColor).spacing(spacing).alignment(alignment));
    }

    public AbstractElementPanel(PacketBuffer buf) {
        children = ProbeInfo.createElements(buf);
        this.layout = new LayoutStyle();
        layout.alignment(buf.readEnum(ElementAlignment.class));
        if (buf.readBoolean()) {
            layout.borderColor(buf.readInt());
        }
        layout.spacing(buf.readInt()).topPadding(buf.readInt()).bottomPadding(buf.readInt()).leftPadding(buf.readInt()).rightPadding(buf.readInt());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        ProbeInfo.writeElements(children, buf);
        buf.writeEnum(layout.getAlignment()).writeBoolean(layout.getBorderColor() != null);
        if (layout.getBorderColor() != null) {
            buf.writeInt(layout.getBorderColor());
        }
        buf.writeInt(layout.getSpacing()).writeInt(layout.getTopPadding()).writeInt(layout.getBottomPadding()).writeInt(layout.getLeftPadding()).writeInt(layout.getRightPadding());
    }

    public ILayoutStyle getStyle() {
        return layout;
    }

    public List<IElement> getElements() {
        return children;
    }

    protected int getYPadding() {
        return layout.getBottomPadding() + layout.getTopPadding();
    }

    protected int getXPadding() {
        return layout.getLeftPadding() + layout.getRightPadding();
    }

    @Override
    public IProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h) {
        return icon(icon, u, v, w, h, new IconStyle());
    }

    @Override
    public IProbeInfo icon(ResourceLocation icon, int u, int v, int w, int h, IIconStyle style) {
        children.add(new ElementIcon(icon, u, v, w, h, style));
        return this;
    }

    @Override
    public IProbeInfo text(ITextComponent text) {
        children.add(new ElementText(text).setLegacy());
        return this;
    }

    @Override
    public IProbeInfo text(CompoundText text, ITextStyle style) {
        children.add(new ElementText(text.get(), style).setLegacy());
        return this;
    }

    @Override
    public IProbeInfo text(CompoundText text) {
        children.add(new ElementText(text.get()).setLegacy());
        return this;
    }

    @Override
    public IProbeInfo text(ITextComponent text, ITextStyle style) {
        children.add(new ElementText(text, style).setLegacy());
        return this;
    }
    
    @Override
	public IProbeInfo mcText(ITextComponent text) {
        children.add(new ElementText(text));
		return this;
	}

	@Override
	public IProbeInfo mcText(ITextComponent text, ITextStyle style) {
        children.add(new ElementText(text, style));
		return this;
	}
	
	@Override
    public IProbeInfo itemLabel(ItemStack stack, ITextStyle style) {
        children.add(new ElementItemLabel(stack));
        return this;
    }

    @Override
    public IProbeInfo itemLabel(ItemStack stack) {
        children.add(new ElementItemLabel(stack));
        return this;
    }

    @Override
    public IProbeInfo entity(String entityName, IEntityStyle style) {
        children.add(new ElementEntity(entityName, style));
        return this;
    }

    @Override
    public IProbeInfo entity(String entityName) {
        return entity(entityName, new EntityStyle());
    }

    @Override
    public IProbeInfo entity(Entity entity, IEntityStyle style) {
        children.add(new ElementEntity(entity, style));
        return this;
    }

    @Override
    public IProbeInfo entity(Entity entity) {
        return entity(entity, new EntityStyle());
    }

    @Override
    public IProbeInfo item(ItemStack stack, IItemStyle style) {
        children.add(new ElementItemStack(stack, style));
        return this;
    }

    @Override
    public IProbeInfo item(ItemStack stack) {
        return item(stack, new ItemStyle());
    }

    @Override
    public IProbeInfo progress(int current, int max) {
        return progress(current, max, new ProgressStyle());
    }

    @Override
    public IProbeInfo progress(int current, int max, IProgressStyle style) {
        children.add(new ElementProgress(current, max, style));
        return this;
    }

    @Override
    public IProbeInfo progress(long current, long max) {
        return progress(current, max, new ProgressStyle());
    }

    @Override
    public IProbeInfo progress(long current, long max, IProgressStyle style) {
        children.add(new ElementProgress(current, max, style));
        return this;
    }
    
    @Override
	  public IProbeInfo tank(TankReference tank) {
    	children.add(new ElementTank(tank));
	  	return this;
	  }
    
	  @Override
	  public IProbeInfo tank(TankReference tank, IProgressStyle style) {
    	children.add(new ElementTank(tank, style));
	  	return this;
	  }
	
	   @Override
	   public IProbeInfo padding(int width, int height) {
		   children.add(new ElementPadding(width, height));
		   return this;
	   }
	
  	@Override
    public IProbeInfo horizontal(ILayoutStyle style) {
        ElementHorizontal e = new ElementHorizontal(style);
        children.add(e);
        return e;
    }

    @Override
    public IProbeInfo horizontal() {
        ElementHorizontal e = new ElementHorizontal(new LayoutStyle().spacing(layout.getSpacing()).alignment(ElementAlignment.ALIGN_TOPLEFT));
        children.add(e);
        return e;
    }

    @Override
    public IProbeInfo vertical(ILayoutStyle style) {
        ElementVertical e = new ElementVertical(style);
        children.add(e);
        return e;
    }

    @Override
    public IProbeInfo vertical() {
        ElementVertical e = new ElementVertical(new LayoutStyle().spacing(ElementVertical.SPACING).alignment(ElementAlignment.ALIGN_TOPLEFT));
        children.add(e);
        return e;
    }

    @Override
    public IProbeInfo element(IElement element) {
        children.add(element);
        return this;
    }

    @Override
    public ILayoutStyle defaultLayoutStyle() {
        return new LayoutStyle();
    }

    @Override
    public IProgressStyle defaultProgressStyle() {
        return new ProgressStyle();
    }

    @Override
    public ITextStyle defaultTextStyle() {
        return new TextStyle();
    }

    @Override
    public IItemStyle defaultItemStyle() {
        return new ItemStyle();
    }

    @Override
    public IEntityStyle defaultEntityStyle() {
        return new EntityStyle();
    }

    @Override
    public IIconStyle defaultIconStyle() {
        return new IconStyle();
    }
    
	@Override
	public IProbeInfo createVerticalPanel() {
		return new ElementVertical(new LayoutStyle().spacing(ElementVertical.SPACING).alignment(ElementAlignment.ALIGN_TOPLEFT));
	}

	@Override
	public IProbeInfo createVerticalPanel(ILayoutStyle style) {
		return new ElementVertical(style);
	}

	@Override
	public IProbeInfo createHorizontalPanel() {
		return new ElementHorizontal(new LayoutStyle().spacing(layout.getSpacing()).alignment(ElementAlignment.ALIGN_TOPLEFT));
	}

	@Override
	public IProbeInfo createHorizontalPanel(ILayoutStyle style) {
		return new ElementHorizontal(style);
	}
	
	@Override
	public IElement asElement() {
		return this;
	}
}