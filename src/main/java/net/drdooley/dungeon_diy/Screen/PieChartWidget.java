package net.drdooley.dungeon_diy.Screen;

import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class PieChartWidget {

    private final List<Slice> slices = new ArrayList<>();

    public void rebuild(List<ReplacementEntry> entries) {
        slices.clear();

        int totalWeight = entries.stream()
          .mapToInt(ReplacementEntry::getWeight)
          .sum();

        if (totalWeight <= 0) {
            return;
        }

        double currentAngle = 0.0;

        for (int i = 0; i < entries.size(); i++) {
            ReplacementEntry entry = entries.get(i);

            double sweep = entry.getWeight() / (double) totalWeight * Math.PI * 2.0;

            slices.add(new Slice(
              currentAngle,
              currentAngle + sweep,
              getColor(i)
            ));

            currentAngle += sweep;
        }
    }

    private int getColor(int index) {
        return switch (index % 8) {
            case 0 -> 0xFFFF5555;
            case 1 -> 0xFF55FF55;
            case 2 -> 0xFF5555FF;
            case 3 -> 0xFFFFFF55;
            case 4 -> 0xFFFF55FF;
            case 5 -> 0xFF55FFFF;
            case 6 -> 0xFFFFAA55;
            default -> 0xFFAAAAAA;
        };
    }

    private record Slice(double start, double end, int color) {}

    public void render(GuiGraphics graphics, int x, int y, int size) {

        int half = size / 2;

        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {

                double dx = (px + 0.5) - half;
                double dy = (py + 0.5) - half;

                double angle = Math.atan2(dy, dx);
                if (angle < 0) {
                    angle += Math.PI * 2.0;
                }

                int color = 0xFF000000;

                for (Slice slice : slices) {
                    if (angle >= slice.start && angle < slice.end) {
                        color = slice.color;
                        break;
                    }
                }

                graphics.fill(x + px, y + py, x + px + 1, y + py + 1, color);
            }
        }
    }
}
