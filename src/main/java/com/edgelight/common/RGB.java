package com.edgelight.common;

import java.util.function.Function;

public class RGB {

	public double r;
	public double g;
	public double b;
	
	public RGB(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public double getColor(int colorIdx) {
		return colorIdx == 0 ? r : colorIdx == 1 ? g : b;
	}
	
	public void setColor(int colorIdx, double val) {
		switch (colorIdx) {
		case 0:
			r = val;
			break;
		case 1:
			g = val;
			break;
		default:
		case 2:
			b = val;
			break;
		}
	}
	
	public void forEachColor(Function<Double, Double> colorProcessor) {
		r = colorProcessor.apply(r);
		g = colorProcessor.apply(g);
		b = colorProcessor.apply(b);
	}
	
	public void multiply(double val) {
		forEachColor(color -> val*color);
	}
	
	public void checkBounds() {
		forEachColor(color -> Math.max(0, Math.min(255, color)));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RGB)) {
			return false;
		}
		return Math.abs(((RGB) obj).r - this.r) < 0.1
				&& Math.abs(((RGB) obj).g - this.g) < 0.1
				&& Math.abs(((RGB) obj).b - this.b) < 0.1;
	}
	
	@Override
	public int hashCode() {
		return (int) (this.r * 10235231 + this.g * 364301 + this.b * 1625);
	}
}
