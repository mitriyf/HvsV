package ru.mitriyf.hvsv.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VectorData {
    private double x, y, z;

    public VectorData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
