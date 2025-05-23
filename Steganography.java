import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

public class Steganography {

    public static void wipeLowBits(Pixel px) {
        int red = (px.getRed() / 4) * 4;
        int green = (px.getGreen() / 4) * 4;
        int blue = (px.getBlue() / 4) * 4;
        px.setColor(new Color(red, green, blue));
    }

    public static Picture previewWipeLowBits(Picture img) {
        Picture output = new Picture(img);
        Pixel[][] grid = output.getPixels2D();
        for (Pixel[] row : grid) {
            for (Pixel cell : row) {
                wipeLowBits(cell);
            }
        }
        return output;
    }

    public static void embedLowBits(Pixel px, Color msgColor) {
        int red = (px.getRed() & 0b11111100) | (msgColor.getRed() >> 6);
        int green = (px.getGreen() & 0b11111100) | (msgColor.getGreen() >> 6);
        int blue = (px.getBlue() & 0b11111100) | (msgColor.getBlue() >> 6);
        px.setColor(new Color(red, green, blue));
    }

    public static Picture previewEmbedLowBits(Picture img, Color msgColor) {
        Picture copy = new Picture(img);
        Pixel[][] grid = copy.getPixels2D();
        for (Pixel[] row : grid) {
            for (Pixel cell : row) {
                embedLowBits(cell, msgColor);
            }
        }
        return copy;
    }

    public static Picture revealHiddenImage(Picture codedImage) {
        Picture revealed = new Picture(codedImage);
        Pixel[][] result = revealed.getPixels2D();
        Pixel[][] original = codedImage.getPixels2D();
        for (int r = 0; r < result.length; r++) {
            for (int c = 0; c < result[0].length; c++) {
                Color current = original[r][c].getColor();
                result[r][c].setColor(new Color(
                        ((result[r][c].getRed() & 0b11) << 6),
                        ((result[r][c].getGreen() & 0b11) << 6),
                        ((result[r][c].getBlue() & 0b11) << 6)));
            }
        }
        return revealed;
    }

    public static boolean fitsInside(Picture base, Picture hidden) {
        return base.getWidth() >= hidden.getWidth() && base.getHeight() >= hidden.getHeight();
    }

    public static Picture embedPicture(Picture container, Picture toHide, int offsetY, int offsetX) {
        Picture combined = new Picture(container);
        Pixel[][] baseGrid = combined.getPixels2D();
        Pixel[][] hideGrid = toHide.getPixels2D();

        for (int y = 0; y < hideGrid.length; y++) {
            for (int x = 0; x < hideGrid[0].length; x++) {
                int newY = offsetY + y;
                int newX = offsetX + x;

                if (newY < baseGrid.length && newX < baseGrid[0].length) {
                    Color hideColor = hideGrid[y][x].getColor();
                    int newRed = (baseGrid[newY][newX].getRed() & 0b11111100) | (hideColor.getRed() >> 6);
                    int newGreen = (baseGrid[newY][newX].getGreen() & 0b11111100) | (hideColor.getGreen() >> 6);
                    int newBlue = (baseGrid[newY][newX].getBlue() & 0b11111100) | (hideColor.getBlue() >> 6);
                    baseGrid[newY][newX].setColor(new Color(newRed, newGreen, newBlue));
                }
            }
        }

        return combined;
    }

    public static boolean areEqual(Picture img1, Picture img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }

        Pixel[][] pixelsA = img1.getPixels2D();
        Pixel[][] pixelsB = img2.getPixels2D();

        for (int y = 0; y < pixelsA.length; y++) {
            for (int x = 0; x < pixelsA[0].length; x++) {
                if (!pixelsA[y][x].getColor().equals(pixelsB[y][x].getColor())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static ArrayList<Point> locateChanges(Picture original, Picture modified) {
        ArrayList<Point> diffs = new ArrayList<>();
        if (original.getWidth() != modified.getWidth() || original.getHeight() != modified.getHeight()) {
            return diffs;
        }

        Pixel[][] base = original.getPixels2D();
        Pixel[][] compare = modified.getPixels2D();

        for (int y = 0; y < base.length; y++) {
            for (int x = 0; x < base[0].length; x++) {
                if (!base[y][x].getColor().equals(compare[y][x].getColor())) {
                    diffs.add(new Point(x, y));
                }
            }
        }

        return diffs;
    }

    public static Picture highlightDiffs(Picture img, ArrayList<Point> changes) {
        Picture outlined = new Picture(img);
        if (changes.isEmpty()) {
            return outlined;
        }

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;

        for (Point pt : changes) {
            int y = pt.y;
            int x = pt.x;
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        Graphics2D g = outlined.createGraphics();
        g.setColor(Color.RED);
        g.drawRect(minX, minY, maxX - minX, maxY - minY);
        g.dispose();

        return outlined;
    }

    public static ArrayList<Integer> textToCode(String msg) {
        msg = msg.toUpperCase();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> encoding = new ArrayList<>();
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == ' ') {
                encoding.add(27);
            } else {
                encoding.add(alphabet.indexOf(msg.charAt(i)) + 1);
            }
        }
        encoding.add(0); // end marker
        return encoding;
    }

    public static String codeToText(ArrayList<Integer> data) {
        String result = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) == 27) {
                result += " ";
            } else {
                result += alpha.substring(data.get(i) - 1, data.get(i));
            }
        }
        return result;
    }

    private static int[] extractTripletBits(int value) {
        int[] out = new int[3];
        int temp = value;
        for (int i = 0; i < 3; i++) {
            out[i] = temp % 4;
            temp = temp / 4;
        }
        return out;
    }

    public static void embedText(Picture baseImage, String message) {
        ArrayList<Integer> encoded = textToCode(message);
        Pixel[][] pixels = baseImage.getPixels2D();
        int idx = 0;

        for (int y = 0; y < pixels.length && idx < encoded.size(); y++) {
            for (int x = 0; x < pixels[0].length && idx < encoded.size(); x++) {
                int value = encoded.get(idx);
                int[] bits = extractTripletBits(value);
                Pixel px = pixels[y][x];
                int red = (px.getRed() & 0b11111100) | bits[0];
                int green = (px.getGreen() & 0b11111100) | bits[1];
                int blue = (px.getBlue() & 0b11111100) | bits[2];
                px.setColor(new Color(red, green, blue));
                idx++;
            }
        }
    }

    public static String decodeText(Picture img) {
        ArrayList<Integer> result = new ArrayList<>();
        Pixel[][] pixels = img.getPixels2D();

        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[0].length; x++) {
                Pixel px = pixels[y][x];
                int red = px.getRed() & 0b11;
                int green = px.getGreen() & 0b11;
                int blue = px.getBlue() & 0b11;
                int val = (blue << 4) | (green << 2) | red;
                if (val == 0) {
                    return codeToText(result);
                }
                result.add(val);
            }
        }
        return codeToText(result);
    }
}
