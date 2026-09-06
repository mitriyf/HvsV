package ru.mitriyf.hvsv.model;

import lombok.Getter;

@Getter
public class OffsetData {
    private final double[] offsetX, offsetZ;
    private final float[] offsetPitch;
    private final double offsetY;

    public OffsetData(double[] offsetX, double offsetY, double[] offsetZ, float[] offsetPitch) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.offsetPitch = offsetPitch;
    }
}
