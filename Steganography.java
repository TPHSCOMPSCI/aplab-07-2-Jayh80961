
import java.awt.*;
import java.util.*;

public class Steganography {

    
    //clearlow
    public static void clearLow(Pixel p) {
        int red = (p.getRed() / 4) * 4;
        int green = (p.getGreen() / 4) * 4;
        int blue = (p.getBlue() / 4) * 4;
        p.setColor(new Color(red, green, blue));
    }









    //main
    public static PictureMain testClearLow(PictureMain p) {
        PictureMain copyelement = new PictureMain(p);
        Pixel[][] pixels = copyelement.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel pixel : row) {
                clearLow(pixel);
            }
        }
        return copyelement;
    }

    public static void setLow(Pixel p, Color c) {
        int red = (p.getRed() & 0b11111100) | (c.getRed() >> 6);
        int green = (p.getGreen() & 0b11111100) | (c.getGreen() >> 6);
        int blue = (p.getBlue() & 0b11111100) | (c.getBlue() >> 6);
        p.setColor(new Color(red, green, blue));
    }



    

    public static PictureMain testSetLow(PictureMain p, Color c) {
        PictureMain copyelement = new PictureMain(p);
        Pixel[][] pixels = copyelement.getPixels2D();
        for (Pixel[] row : pixels) {
            for (Pixel pixel : row) {
                setLow(pixel, c);
            }
        }
        return copyelement;
    }

    public static PictureMain revealPictureMain(PictureMain hidden) {
        PictureMain copyelement = new PictureMain(hidden);
        Pixel[][] pixels = copyelement.getPixels2D();
        Pixel[][] source = hidden.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                Color col = source[r][c].getColor();
                pixels[r][c].setColor(new Color(((pixels[r][c].getRed() & 0b00000011) << 6),
                        ((pixels[r][c].getGreen() & 0b00000011) << 6), ((pixels[r][c].getBlue() & 0b00000011) << 6)));
            }
        }
        return copyelement;
    }

    public static boolean canHide(PictureMain source, PictureMain secret) {
        return source.getWidth() >= secret.getWidth() && source.getHeight() >= secret.getHeight();
    }

    public static PictureMain hidePictureMain(PictureMain source, PictureMain secret, int startRow, int startCol) {
        PictureMain combined = new PictureMain(source);
        Pixel[][] sourcePixels = combined.getPixels2D();
        Pixel[][] secretPixels = secret.getPixels2D();

        for (int r = 0; r < secretPixels.length; r++) {
            for (int c = 0; c < secretPixels[0].length; c++) {
                int sourceR = startRow + r;
                int sourceC = startCol + c;

                if (sourceR < sourcePixels.length && sourceC < sourcePixels[0].length) {
                    Color secretColor = secretPixels[r][c].getColor();
                    int rNew = (sourcePixels[sourceR][sourceC].getRed() & 0b11111100) | (secretColor.getRed() >> 6);
                    int gNew = (sourcePixels[sourceR][sourceC].getGreen() & 0b11111100) | (secretColor.getGreen() >> 6);
                    int bNew = (sourcePixels[sourceR][sourceC].getBlue() & 0b11111100) | (secretColor.getBlue() >> 6);

                    sourcePixels[sourceR][sourceC].setColor(new Color(rNew, gNew, bNew));
                }
            }
        }
        return combined;
    }

    public static boolean isSame(PictureMain pic1, PictureMain pic2) {
        if (pic1.getWidth() != pic2.getWidth() || pic1.getHeight() != pic2.getHeight()) {
            return false;
        }
        Pixel[][] p1 = pic1.getPixels2D();
        Pixel[][] p2 = pic2.getPixels2D();
        for (int r = 0; r < p1.length; r++) {
            for (int c = 0; c < p1[0].length; c++) {
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<Point> findDifferences(PictureMain pic1, PictureMain pic2) {
        ArrayList<Point> diffPoints = new ArrayList<>();
        if (pic1.getWidth() != pic2.getWidth() || pic1.getHeight() != pic2.getHeight()) {
            return diffPoints;
        }
        Pixel[][] p1 = pic1.getPixels2D();
        Pixel[][] p2 = pic2.getPixels2D();
        for (int r = 0; r < p1.length; r++) {
            for (int c = 0; c < p1[0].length; c++) {
                if (!p1[r][c].getColor().equals(p2[r][c].getColor())) {
                    diffPoints.add(new Point(c, r));
                }
            }
        }
        return diffPoints;
    }

    public static PictureMain showDifferentArea(PictureMain pic, ArrayList<Point> differences) {
        PictureMain highlighted = new PictureMain(pic);
        if (differences.isEmpty()) {
            return highlighted;
        }

        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;

        for (Point p : differences) {
            int row = p.y;
            int col = p.x;
            minRow = Math.min(minRow, row);
            maxRow = Math.max(maxRow, row);
            minCol = Math.min(minCol, col);
            maxCol = Math.max(maxCol, col);
        }

        Graphics2D g = highlighted.createGraphics();
        g.setColor(Color.BLUE);
        g.drawRect(minCol, minRow, maxCol - minCol, maxRow - minRow);
        g.dispose();

        return highlighted;
    }

    public static ArrayList<Integer> encodeString(String s) {
        s = s.toUpperCase();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < s.length(); i++) {
            if (s.substring(i, i + 1).equals(" ")) {
                result.add(27);
            } else {
                result.add(alpha.indexOf(s.substring(i, i + 1)) + 1);
            }
        }
        result.add(0);
        return result;
    }

    public static String decodeString(ArrayList<Integer> codes) {
        String result = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < codes.size(); i++) {
            if (codes.get(i) == 27) {
                result = result + " ";
            } else {
                result = result
                        + alpha.substring(codes.get(i) - 1, codes.get(i));
            }
        }
        return result;
    }

    private static int[] getBitPairs(int num) {
        int[] bits = new int[3];
        int code = num;
        for (int i = 0; i < 3; i++) {
            bits[i] = code % 4;
            code = code / 4;
        }
        return bits;
    }

    public static void hideText(PictureMain source, String s) {
        ArrayList<Integer> encoded = encodeString(s);
        Pixel[][] pixels = source.getPixels2D();
        int i = 0;
        for (int r = 0; r < pixels.length && i < encoded.size(); r++) {
            for (int c = 0; c < pixels[0].length && i < encoded.size(); c++) {
                int num = encoded.get(i);
                int[] bitPairs = getBitPairs(num);
                Pixel p = pixels[r][c];
                int red = (p.getRed() & 0b11111100) | bitPairs[0];
                int green = (p.getGreen() & 0b11111100) | bitPairs[1];
                int blue = (p.getBlue() & 0b11111100) | bitPairs[2];
                p.setColor(new Color(red, green, blue));
                i++;
            }
        }
    }

    public static String revealText(PictureMain source) {
        ArrayList<Integer> words = new ArrayList<>();
        Pixel[][] pixels = source.getPixels2D();
        for (int r = 0; r < pixels.length; r++) {
            for (int c = 0; c < pixels[0].length; c++) {
                Pixel p = pixels[r][c];
                int red = p.getRed() & 0b00000011;
                int green = p.getGreen() & 0b00000011;
                int blue = p.getBlue() & 0b00000011;
                int letter = (blue << 4) | (green << 2) | red;
                if (letter == 0) {
                    return decodeString(words);
                }
                words.add(letter);
            }
        }
        return decodeString(words);
    }

    //ACTIVITY 5 CODE
    public static void randomGrey(PictureMain source, int width, int height) {
        Pixel[][] pixels = source.getPixels2D();
        int maxRow = pixels.length - width;
        int maxCol = pixels[0].length - height;
        if (maxRow < 0 || maxCol < 0) {
            System.out.println("Region too large for the image!");
            return;
        }
        // Generate random starting position
        int startRow = (int) (Math.random() * maxRow);
        int startCol = (int) (Math.random() * maxCol);
        // Traverse the selected region
        for (int r = startRow; r < startRow + height; r++) {
            for (int c = startCol; c < startCol + width; c++) {
                if (r < pixels.length && c < pixels[0].length) {
                    Pixel p = pixels[r][c];
                    int gray = (p.getRed() + p.getGreen() + p.getBlue()) / 3;
                    p.setColor(new Color(gray, gray, gray));
                }
            }
        }
    }

    public static void main(String[] args) {
        //test runs for activity one
        PictureMain beach = new PictureMain("beach.jpg");
        PictureMain arch = new PictureMain("arch.jpg");
        beach.explore();
        PictureMain copyelement2 = testSetLow(beach, Color.PINK);
        copyelement2.explore();
        PictureMain copyelement3 = revealPictureMain(copyelement2);
        copyelement3.explore();

        //test runs for activity two
        System.out.println(canHide(beach, arch));
        if (canHide(beach, arch)) {
            PictureMain hidden = hidePictureMain(beach, arch, 0, 0);
            hidden.explore();
            PictureMain revealed = revealPictureMain(hidden);
            revealed.explore();
        }
        // test runs for activity three
        PictureMain swan = new PictureMain("swan.jpg");
        PictureMain swan2 = new PictureMain("swan.jpg");
        System.out.println("Swan and swan2 are the same: "
                + isSame(swan, swan2));
        swan = testClearLow(swan);
        System.out.println("Swan and swan2 are the same (after clearLow run on swan): "
                + isSame(swan, swan2));
        PictureMain arch1 = new PictureMain("arch.jpg");
        PictureMain arch2 = new PictureMain("arch.jpg");
        PictureMain koala = new PictureMain("koala.jpg");
        PictureMain robot1 = new PictureMain("robot.jpg");
        ArrayList<Point> pointList = findDifferences(arch1, arch2);
        System.out.println("PointList after comparing two identical PictureMains has a size of " + pointList.size());
        pointList = findDifferences(arch1, koala);
        System.out.println("PointList after comparing two different sized PictureMains has a size of " + pointList.size());
        arch2 = hidePictureMain(arch1, robot1, 65, 102);
        pointList = findDifferences(arch1, arch2);
        System.out.println("Pointlist after hiding a PictureMain has a size of " + pointList.size());
        arch1.show();
        arch2.show();
        PictureMain hall = new PictureMain("femaleLionAndHall.jpg");
        PictureMain robot2 = new PictureMain("robot.jpg");
        PictureMain flower2 = new PictureMain("flower1.jpg");
        PictureMain hall2 = hidePictureMain(hall, robot2, 50, 300);
        PictureMain hall3 = hidePictureMain(hall2, flower2, 115, 275);
        hall3.explore();
        if (!isSame(hall, hall3)) {
            PictureMain hall4 = showDifferentArea(hall, findDifferences(hall, hall3));
            hall4.show();
            PictureMain unhiddenHall3 = revealPictureMain(hall3);
            unhiddenHall3.show();
        }
        //test runs for activity four 
        PictureMain beach1 = new PictureMain("beach.jpg");
        hideText(beach1, "HELLO WORLD");
        String revealed = revealText(beach1);
        System.out.println("Hidden message: " + revealed);

        //activity 5: applying grey filters to random regions, the region size is specified
        PictureMain motorcyle = new PictureMain("blueMotorcycle.jpg");
        motorcyle.explore(); //original image
        randomGrey(motorcyle, 300, 400);
        motorcyle.explore(); //grey image

    }
}
