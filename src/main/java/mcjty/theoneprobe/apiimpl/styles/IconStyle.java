package mcjty.theoneprobe.apiimpl.styles;

import mcjty.theoneprobe.api.IIconStyle;

public class IconStyle implements IIconStyle {
    private int width = 16;
    private int height = 16;
    private int txtw = 256;
    private int txth = 256;
    private int color = -1;
    
    @Override
    public IIconStyle copy() {
    	return new IconStyle().bounds(width, height).textureBounds(txtw, txth).color(color);
    }
    
    @Override
    public IIconStyle width(int w) {
        width = w;
        return this;
    }

    @Override
    public IIconStyle height(int h) {
        height = h;
        return this;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public IIconStyle textureWidth(int w) {
        txtw = w;
        return this;
    }

    @Override
    public IIconStyle textureHeight(int h) {
        txth = h;
        return this;
    }

    @Override
    public int getTextureWidth() {
        return txtw;
    }

    @Override
    public int getTextureHeight() {
        return txth;
    }
    
    @Override
    public IIconStyle color(int color) {
    	this.color = color;
    	return this;
    }
    
    @Override
    public int getColor() {
    	return color;
    }
}
