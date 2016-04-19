package Framework;

import Evaluators.*;
import GUI.MainFrame;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class Main {

    static Evaluator currentPlayer;
    static Evaluator blackPlayer;
    static Evaluator whitePlayer;
    static MainFrame frame;

    public static void main(String[] args) {
        botVersusBot();
        //manVersusBot();
    }

    public static void botVersusBot() {
        blackPlayer = new DecompiledOurEvaluator();
        whitePlayer = new YourEvaluator();

        frame = new MainFrame("botVersusBot");
        int depth = 5;

        Position p = new Position();
        p.setStartingPosition();
        p.addPositionToGUIpositionList(frame.c);
        blackPlayer.eval(p);

        int moveNumber = 0;
        for (; moveNumber < 150; moveNumber++) {
            Vector<Position> P = p.getNextPositions();

            if(p.winner == +1) {
                frame.c.gameOver("White won.");
                break;
            }

            if(p.winner == -1) {
                frame.c.gameOver("Black won.");
                break;
            }

            if(P.size() == 0) {
                frame.c.gameOver("No more available moves");
                break;
            }

            Position bestPosition = new Position();
            if(p.whiteToMove) {
                currentPlayer = whitePlayer;
                double max = -1./0.;
                for(int i = 0; i < P.size(); ++i) {
                    double val = minmax(P.elementAt(i),depth,1);
                    if(max < val) {
                        bestPosition = P.elementAt(i);
                        max = val;
                    }
                }
            } else {
                currentPlayer = blackPlayer;
                double min = 1./0.;
                for(int i = 0; i < P.size(); ++i) {
                    double val = minmax(P.elementAt(i),depth,0);
                    if(min > val) {
                        bestPosition = P.elementAt(i);
                        min = val;
                    }
                }
            }
            assert p.whiteToMove != bestPosition.whiteToMove;
            p = bestPosition;
            p.addPositionToGUIpositionList(frame.c);
        }
    }

    /** Manual play vs. "YourEvaluator" bot */
    public static void manVersusBot() {
        frame = new MainFrame("manVersusBot");
        Position p = new Position();
        p.setStartingPosition();
        p.addPositionToGUIpositionList(frame.c);
    }

    static double eval(Position p) {
        if (!Double.isNaN(p.cachedResult)) {
            return p.cachedResult;
        }

        double d = 0.0;
        if (currentPlayer == whitePlayer)
            d = currentPlayer.eval(p);
        else
        {
            Position mirrored = p.mirror();
            d = -currentPlayer.eval(mirrored);
        }
        p.cachedResult = d;
        return d;
    }

    static double alphabeta(Position p, int depth, double alpha, double beta, int player) {
        // 0 tries to maximize, 1 tries to minimize
        if (p.winner == -1) return -1E10-depth; // prefer to win sooner
        if (p.winner == +1) return +1E10+depth; // and lose later

        if(depth == 0) {
            return currentPlayer.eval(p);
        }
        Vector<Position> P = p.getNextPositions();
        Collections.sort(P, (new Main()).new PositionComparator());
        if(player == 0) Collections.reverse(P);

        if(player == 0) {
            for(int i = 0; i < P.size(); ++i) {
                alpha = Math.max(alpha, alphabeta(P.elementAt(i),depth-1,alpha,beta,1));
                if(beta <= alpha) break;
            }
            return alpha;
        }

        for(int i = 0; i < P.size(); ++i) {
            beta = Math.min(beta,alphabeta(P.elementAt(i),depth-1,alpha,beta,0));
            if(beta <= alpha) break;
        }

        return beta;
    }

    static double minmax(Position p, int depth, int player) {
        double alpha = -Double.MAX_VALUE, beta = Double.MAX_VALUE;
        return alphabeta(p,depth,alpha,beta,player);
    }

    public static void requestMove(Evaluator evaluator, Position p) {
        currentPlayer = evaluator;
        Vector<Position> P = p.getNextPositions();
        int depth = 5;
        if(p.winner == +1) {
            frame.c.gameOver("White won.");
            return;
        }

        if(p.winner == -1) {
            frame.c.gameOver("Black won.");
            return;
        }

        if(P.size() == 0) {
            frame.c.gameOver("No more available moves");
            return;
        }

        Position bestPosition = new Position();
        if(p.whiteToMove) {
            double max = -1./0.;
            for(int i = 0; i < P.size(); ++i) {
                double val = minmax(P.elementAt(i),depth,1);
                if(max < val) {
                    bestPosition = P.elementAt(i);
                    max = val;
                }
            }
        } else {
            double min = 1./0.;
            for(int i = 0; i < P.size(); ++i) {
                double val = minmax(P.elementAt(i),depth,0);
                if(min > val) {
                    bestPosition = P.elementAt(i);
                    min = val;
                }
            }
        }
        assert p.whiteToMove != bestPosition.whiteToMove;
        p = bestPosition;
        p.addPositionToGUIpositionList(frame.c);
    }


    class PositionComparator implements Comparator<Position> {
        public int compare(Position p1, Position p2) {
            return Double.compare(eval(p1), eval(p2));
        }
    }
}
