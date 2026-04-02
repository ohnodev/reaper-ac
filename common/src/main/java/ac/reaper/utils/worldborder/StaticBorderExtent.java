package ac.reaper.utils.worldborder;

import ac.reaper.utils.math.ReaperMath;

public record StaticBorderExtent(double size) implements BorderExtent {

    @Override
    public double getMinX(double centerX, double absoluteMaxSize) {
        return ReaperMath.clamp(centerX - size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMaxX(double centerX, double absoluteMaxSize) {
        return ReaperMath.clamp(centerX + size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMinZ(double centerZ, double absoluteMaxSize) {
        return ReaperMath.clamp(centerZ - size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMaxZ(double centerZ, double absoluteMaxSize) {
        return ReaperMath.clamp(centerZ + size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public BorderExtent tick() {
        return this;
    }

    @Override
    public BorderExtent update() {
        return this;
    }

}
