package com.ri;

import com.ri.meta.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;

import javax.swing.*;


// Todo Color thry
//  C0: Probability-Decision: No idea
//  C1: Energy-Will:          Being very strong, easy to use
//  C2: Stability-Wisdom:     Not a piece, Can swap the color of two (non special) pieces. (maybe)
//  C3: Space-perspective:    Swap  (X--C0--Z --> Z--C0--X or C0--X -> X--C0 in u,r,l dir only can't be blocked by other pieces)
//  C4: Information-Memories  Mimic (piece can mimic all movement of all the piece it touches, even special pieces)
//  C5: Time-Emotions         No idea

// Piece attributes
//  Colors:          C0 C1 C2 C3 C4 C5
//  Singe:           u l r
//  Double:          U L R
//  Mimic:           *

// Other attributes
//  Castle:          S1 S2
//  Outer-bound:     O
//  Empty:           _

// 1: You can block opponent from playing a piece of particular color on current turn.
// 2: Each piece move according to these rules:
//      - Single stroke move 1 block in that direction.
//      - Double stroke move any blocks in that direction.
//      - Single stroke can mimic a double stroke piece's all movements if it touches it from same direction as its single
//          stroke and double stroke piece also have same direction.
//      - Double stroke can pass through a double stroke piece if it touches it from same direction as its double stroke and
//          double stroke piece also have same direction.
//      - Mimic (circular) piece can mimic all movement of all the piece it touches but cannot move on its own.
//      - Mimic and pass-through mechanics still work even if the other pieces color is blocked.
// 3: You cannot capture a piece having same color as capturer piece even via pass-though or mimic mechanics.
// 4: You have to capture opponent's castle positioned at (0, 3) or (0, -3) to win, as opponent can block one color, you have
//      to attack it with two different colored pieces to win.
public class GameKyz32 {
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = GameKyz32.class.getSimpleName();
        ProjectType     type        = ProjectType.OTHER;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.CLASS_C;
        byte            id          = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.INCOMPLETE;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    private static void execute(ProjectName projectName) throws Exception {
        JFrame frame = new JFrame();

        frame.add(new _Panel());

        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class _Panel extends JPanel implements MouseListener {
        private static final int size = 864;
        private static final int margin = 8;
        private static final int colorChooseSize = 64;
        private static final int hSize = size / 27;
        private static final int hSizeS3 = (int) (hSize * Math.sqrt(3));
        private static final int boardSize = 6;
        private static final int fontSize = 16;

        private static final double pieceSize = 0.616;
        private static final int[] xHex = new int[6];
        private static final int[] yHex = new int[6];
        private static final Color[] boardColor = new Color[]{
                new Color(0xB0BDCB),
                new Color(0xC7B9AC),
                new Color(0xA7C1B4),
        };
        private static final Color[] pieceColor = new Color[]{
                new Color(0xff92f1),
                new Color(0xa89cff),
                new Color(0x6db6ff),
                new Color(0x47ffa3),
                new Color(0x84ff09),
                new Color(0xffa449),

                new Color(0x800040),
                new Color(0x5600AC),
                new Color(0x003E7D),
                new Color(0x004A09),
                new Color(0x3B4300),
                new Color(0x643200),

                new Color(0xC0C0C0),
                new Color(0x303030),
        };
        private static final AffineTransform mainTransform = new AffineTransform();
        private static final AffineTransform invMainTransform;

        static {
            mainTransform.translate(margin, margin + colorChooseSize);
            mainTransform.scale(1 - 1.0f * margin / size, 1 - 1.0f * margin / size);
            try {
                invMainTransform = mainTransform.createInverse();
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < 6; i++) {
                xHex[i] = (int) (Math.cos(i * Math.PI / 3) * hSize);
                yHex[i] = (int) (Math.sin(i * Math.PI / 3) * hSize);
            }
        }

        private final int[][] board = new int[boardSize * 2 + 1][boardSize * 2 + 1];
        private int[] selectedPiece = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        private ArrayList<int[]> moves = new ArrayList<>();
        private int selectedColor = -1;
        private boolean currentPlayer = true;
        private int phase = 0;

        public _Panel() {
            super();

            final double sqrt3 = Math.sqrt(3);
            for (int i = -boardSize; i <= boardSize; i++) {
                for (int j = -boardSize; j <= boardSize; j++) {
                    final double x = i * sqrt3;
                    final double y = j * 2 + ((i % 2 == 0) ? 0 : 1);
                    final double d = x * x + y * y;

                    if (i == -6 || i == 6) setPiece(i, j, Integer.MIN_VALUE);
                    else if (d > boardSize * boardSize * 4) setPiece(i, j, Integer.MIN_VALUE);
                    else {
                        setPiece(i, j, -1);
                    }
                }
            }
            // @formatter:off
            setPiece( 0,  3, -2);
            setPiece( 0, -3, -3);

            setPiece( 0,  6, 1, 0, 2, 2, 2);
            setPiece( 1,  5, 1, 1, 2, 1, 1);
            setPiece(-1,  5, 1, 1, 2, 1, 1);
            setPiece( 2,  5, 1, 2, 0, 2, 2);
            setPiece(-2,  5, 1, 2, 0, 2, 2);
            setPiece( 3,  4, 1, 3, 1, 1, 1);
            setPiece(-3,  4, 1, 3, 1, 1, 1);
            setPiece( 4,  4, 1, 5, 2, 0, 2);
            setPiece(-4,  4, 1, 5, 2, 2, 0);
            setPiece( 5,  3, 1, 4, 1, 2, 1);
            setPiece(-5,  3, 1, 4, 1, 1, 2);
            setPiece( 2,  3, 1, 2, 0, 0, 1);
            setPiece(-2,  3, 1, 2, 0, 1, 0);
            setPiece( 1,  4, 1, 0, 0, 1, 0);
            setPiece(-1,  4, 1, 0, 0, 0, 1);
            setPiece( 4,  2, 1, 1, 1, 0, 1);
            setPiece(-4,  2, 1, 1, 1, 1, 0);
            setPiece( 0,  5, 1, 3, 0, 0, 0);

            setPiece( 0, -6, 0, 0, 2, 2, 2);
            setPiece( 1, -6, 0, 1, 2, 1, 1);
            setPiece(-1, -6, 0, 1, 2, 1, 1);
            setPiece( 2, -5, 0, 2, 0, 2, 2);
            setPiece(-2, -5, 0, 2, 0, 2, 2);
            setPiece( 3, -5, 0, 3, 1, 1, 1);
            setPiece(-3, -5, 0, 3, 1, 1, 1);
            setPiece( 4, -4, 0, 5, 2, 2, 0);
            setPiece(-4, -4, 0, 5, 2, 0, 2);
            setPiece( 5, -4, 0, 4, 1, 1, 2);
            setPiece(-5, -4, 0, 4, 1, 2, 1);
            setPiece( 2, -3, 0, 2, 0, 1, 0);
            setPiece(-2, -3, 0, 2, 0, 0, 1);
            setPiece( 1, -5, 0, 0, 0, 0, 1);
            setPiece(-1, -5, 0, 0, 0, 1, 0);
            setPiece( 4, -2, 0, 1, 1, 1, 0);
            setPiece(-4, -2, 0, 1, 1, 0, 1);
            setPiece( 0, -5, 0, 3, 0, 0, 0);
            // @formatter:on


            setPreferredSize(new Dimension(size + margin, size + margin + colorChooseSize * 2));
            addMouseListener(this);
        }

        private static boolean getPiecePlayer(int piece) {
            if (piece < 0) throw new RuntimeException();
            return piece % 2 == 1;
        }

        private static int getPieceColor(int piece) {
            return piece / 2 % 6;
        }

        private static int getPieceUp(int piece) {
            return piece / (2 * 6) % 3;
        }

        private static int getPieceLeft(int piece) {
            return piece / (2 * 6 * 3) % 3;
        }

        private static int getPieceRight(int piece) {
            return piece / (2 * 6 * 3 * 3) % 3;
        }

        private static int getPieceDir(int piece, int i) {
            return switch (i) {
                case 0 -> getPieceUp(piece);
                case 1 -> getPieceLeft(piece);
                case 2 -> getPieceRight(piece);
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
        }

        public static int moveDirX(int x, int y, int dir, int i) {
            return switch (dir) {
                case 0 -> x;
                case 1 -> x + i;
                case 2 -> x - i;
                default -> throw new IllegalStateException("Unexpected value: " + dir);
            };
        }

        public static int moveDirY(int x, int y, int dir, int d) {
            return switch (dir) {
                case 0 -> y + d;
                case 1, 2 -> y + (x % 2 == 0 ? 0 : 1) - (1 + d) / 2;
                default -> throw new IllegalStateException("Unexpected value: " + dir);
            };
        }

        private int getPiece(int i, int j) {
            try {
                return board[i + boardSize][j + boardSize];
            } catch (ArrayIndexOutOfBoundsException _) {
                return Integer.MIN_VALUE;
            }
        }

        private void setPiece(int i, int j, int piece) {
            board[i + boardSize][j + boardSize] = piece;
        }

        private void setPiece(int i, int j, int player, int color, int u, int l, int r) {
            board[i + boardSize][j + boardSize] = player + 2 * (color + 6 * (u + 3 * (l + 3 * r)));
        }

        @Override
        public void paint(Graphics _g) {
            super.paint(_g);

            Graphics2D g = (Graphics2D) _g;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, size + margin, size + margin + colorChooseSize * 2);

            {
                g.setColor(Color.GRAY);
                g.setStroke(new BasicStroke(2.5f));
                g.drawLine(0, colorChooseSize, size + margin, colorChooseSize);
                g.drawLine(0, size + margin + colorChooseSize, size + margin, size + margin + colorChooseSize);

                for (int i = 0; i < 6; i++) {
                    g.setColor(pieceColor[i + (currentPlayer ? 0 : 6)]);

                    if (selectedColor != i) {
                        boolean k = (currentPlayer) ^ (phase == 0);
                        g.fillOval((size - margin * 2) / 6 * i + colorChooseSize / 2 + margin,
                                margin + (k ? size + margin + colorChooseSize : 0),
                                colorChooseSize - margin * 2, colorChooseSize - margin * 2);
                    }
                }
            }

            {
                g.setTransform(mainTransform);

                g.setColor(new Color(0x312A37));
                g.fillOval(3, 3, size, size);

                g.setColor(new Color(0x415157));
                g.fillOval(-3, -3, size, size);

                g.setPaint(new GradientPaint(0, size / 7f, new Color(0x2D3C3B), size, size * 6 / 7f, new Color(0x41373A)));
                g.fillOval(0, 0, size, size);
                g.setClip(new Ellipse2D.Float(0, 0, size, size));
            }


            {
                BasicStroke s1 = new BasicStroke(2.5f);
                BasicStroke s2 = new BasicStroke(5.75f);
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));

                int n = boardSize;
                for (int i = -n; i <= n; i++) {
                    for (int j = -n; j <= n; j++) {
                        final int x = i * hSizeS3;
                        final int y = j * hSize * 2 + ((i % 2 == 0) ? 0 : hSize);

                        final int d = x * x + y * y;
                        final int piece = getPiece(i, j);
                        if (piece == Integer.MIN_VALUE) continue;

                        if (d > hSize * hSize * (n - 1) * (n - 1) * 4) {
                            g.setColor((i % 2 == 0) ? boardColor[0] : boardColor[1]);
                        } else if (d > hSize * hSize * (n - 3) * (n - 3) * 4) {
                            g.setColor((i % 2 == 0) ? boardColor[1] : boardColor[2]);
                        } else {
                            g.setColor((i % 2 == 0) ? boardColor[2] : boardColor[0]);
                        }

                        g.setTransform(mainTransform);
                        g.translate(size / 2 + x, size / 2 + y);

                        g.setStroke(s1);
                        g.drawPolygon(xHex, yHex, 6);

                        g.drawString(i + " " + j, -12, 0);

                        if (piece == -1) continue;


                        g.scale(pieceSize, pieceSize);

                        if (piece == -2 || piece == -3) {
                            g.setColor(pieceColor[12 - 3 - piece + 1]);
                            g.fillOval(-hSize, -hSize, hSize * 2, hSize * 2);

                            g.setColor(pieceColor[12 + 3 + piece]);
                            g.drawOval(-hSize, -hSize, hSize * 2, hSize * 2);
                        } else {
                            boolean player = getPiecePlayer(piece);
                            g.setColor(pieceColor[getPieceColor(piece) + (player ? 0 : 6)]);
                            g.fillPolygon(xHex, yHex, 6);
                            g.setColor(player ? Color.WHITE : Color.BLACK);
                            g.setStroke(s2);
                            g.drawPolygon(xHex, yHex, 6);

                            g.setColor(player ? Color.BLACK : Color.WHITE);

                            if (getPieceDir(piece, 0) == 0 && getPieceDir(piece, 1) == 0 && getPieceDir(piece, 2) == 0) {
                                g.drawOval(-hSize / 2, -hSize / 2, hSize, hSize);
                            } else {
                                for (int k = 0; k < 3; k++) {
                                    int dir = getPieceDir(piece, k);
                                    if (dir != 0) {
                                        g.setStroke(dir == 1 ? s1 : s2);
                                        g.drawLine(0, -hSize / 2, 0, hSize / 2);

                                        if (dir == 1) {
                                            g.drawLine(-hSize / 9, -hSize / 2, hSize / 9, -hSize / 2);
                                            g.drawLine(-hSize / 9, hSize / 2, hSize / 9, hSize / 2);
                                        }
                                    }
                                    g.rotate(Math.PI / 3);
                                }
                            }

                            if (i == selectedPiece[0] && j == selectedPiece[1]) {
                                g.setStroke(s2);
                                g.setColor(new Color(0xeeded8));

                                g.drawOval(-hSize * 7 / 6, -hSize * 7 / 6, hSize * 2 * 7 / 6, hSize * 2 * 7 / 6);
                            }
                        }
                    }
                }

                for (int[] move : moves) {
                    final int x = move[0] * hSizeS3;
                    final int y = move[1] * hSize * 2 + ((move[0] % 2 == 0) ? 0 : hSize);

                    g.setTransform(mainTransform);
                    g.translate(size / 2 + x, size / 2 + y);
                    g.scale(pieceSize, pieceSize);

                    g.setColor(Color.GREEN);
                    g.setStroke(s1);
                    g.setColor(new Color(0xeeded8));

                    if (getPiece(move[0], move[1]) < 0) {
                        g.setStroke(s2);
                        g.drawOval(-hSize * 4 / 6, -hSize * 4 / 6, hSize * 2 * 4 / 6, hSize * 2 * 4 / 6);

                    } else if (getPiece(move[0], move[1]) >= 0) {
                        g.setStroke(s1);
                        g.drawOval(-hSize * 7 / 6, -hSize * 7 / 6, hSize * 2 * 7 / 6, hSize * 2 * 7 / 6);
                    }
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (phase == 0) {
                if ((!currentPlayer)) {
                    if (e.getY() < colorChooseSize + size + margin) return;
                } else if (e.getY() > colorChooseSize) return;

                int x = e.getX();

                int c = (x - margin) / ((size - margin * 2) / 6);

                if (c >= 0 && c < 6) {
                    selectedColor = c;
                    phase = 1;
                }
            } else if (phase == 1) {
                Point point = e.getPoint();
                invMainTransform.transform(point, point);
                point.translate(-size / 2, -size / 2);

                final int x = Math.round((float) point.x / hSizeS3);
                final int y = Math.round(((float) point.y - ((x % 2 == 0) ? 0 : hSize)) / hSize / 2);

                if (selectedPiece[0] == Integer.MAX_VALUE || selectedPiece[1] == Integer.MAX_VALUE) {
                    int piece = getPiece(x, y);
                    if (piece >= 0 && getPiecePlayer(piece) == currentPlayer && selectedColor != getPieceColor(piece)) {
                        selectedPiece[0] = x;
                        selectedPiece[1] = y;
                    }
                } else {
                    int piece = getPiece(selectedPiece[0], selectedPiece[1]);
                    for (int[] move : moves) {
                        if (move[0] == x && move[1] == y) {
                            if (getPiece(x, y) == -2 || getPiece(x, y) == -3) {
                                JDialog dialog = new JDialog();
                                dialog.setTitle("Game Concluded");
                                dialog.add(new JLabel("Player `" + currentPlayer + "` won"));
                                dialog.pack();
                                dialog.setVisible(true);
                            }

                            setPiece(x, y, piece);
                            setPiece(selectedPiece[0], selectedPiece[1], -1);
                            currentPlayer = !currentPlayer;
                            phase = 0;
                            selectedColor = -1;
                            break;
                        }
                    }

                    selectedPiece[0] = Integer.MAX_VALUE;
                    selectedPiece[1] = Integer.MAX_VALUE;
                }

                updateMoveLogic();
            }

            repaint();
        }

        private void updateMoveLogic() {
            moves.clear();

            int x = selectedPiece[0];
            int y = selectedPiece[1];

            if (x == Integer.MAX_VALUE || y == Integer.MAX_VALUE) return;

            movesFor(x, y, getPieceColor(getPiece(x, y)));
        }

        private void movesFor(int x, int y, int color) {
            int piece = getPiece(x, y);

            boolean all0 = getPieceDir(piece, 0) == 0 && getPieceDir(piece, 1) == 0 && getPieceDir(piece, 2) == 0;

            for (int d = -1; d <= 1; d += 2) {
                for (int i = 0; i < 3; i++) {
                    int k = getPieceDir(piece, i);

                    if (k == 1 || all0) {
                        int x0, y0;

                        x0 = moveDirX(x, y, i, d);
                        y0 = moveDirY(x, y, i, d);

                        final int targetPiece = getPiece(x0, y0);
                        if (k == 1 && (targetPiece == -1 || (targetPiece >= 0 && getPiecePlayer(targetPiece) != currentPlayer && getPieceColor(targetPiece) != color))) {
                            moves.add(new int[]{x0, y0});
                        } else if (targetPiece == -2 || targetPiece == -3) {
                            if (currentPlayer ^ targetPiece == -2) moves.add(new int[]{x0, y0});
                            break;
                        }

                        if ((targetPiece >= 0 && getPiecePlayer(targetPiece) == currentPlayer) && (getPieceDir(targetPiece, i) == 2 || all0)) {
                            movesFor(x0, y0, color);
                        }
                    }

                    if (k == 2) {
                        int tmpX, x0, y0;

                        tmpX = x;
                        y0 = y;

                        for (int j = 0; j < boardSize * 2; j++) {
                            x0 = moveDirX(tmpX, y0, i, d);
                            y0 = moveDirY(tmpX, y0, i, d);
                            tmpX = x0;

                            int targetPiece = getPiece(x0, y0);
                            if (targetPiece >= 0) {
                                if (getPiecePlayer(targetPiece) != currentPlayer && getPieceColor(targetPiece) != color) moves.add(new int[]{x0, y0});
                                else if (getPiecePlayer(targetPiece) == currentPlayer && getPieceDir(targetPiece, i) == 2) continue;
                                break;
                            } else if (targetPiece == -2 || targetPiece == -3) {
                                if (currentPlayer ^ targetPiece == -2) moves.add(new int[]{x0, y0});
                                break;
                            }
                            if (targetPiece == Integer.MIN_VALUE) break;

                            moves.add(new int[]{x0, y0});
                        }
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
