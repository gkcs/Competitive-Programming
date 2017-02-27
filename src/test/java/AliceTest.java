import main.java.videos.AntColony;
import org.junit.Test;

import java.util.Random;

public class AliceTest {

    private final Random random = new Random();

    @Test
    public void test() {
        final int DIMENSIONS = 100, sqrt = (int) Math.sqrt(DIMENSIONS);
        final int[][] originalFlowers = new int[DIMENSIONS][DIMENSIONS];
        final int[][] regrow = new int[DIMENSIONS][DIMENSIONS];
        final int[][] limits = new int[DIMENSIONS][DIMENSIONS];
        final int[][][] fences = new int[DIMENSIONS][][];
        final int startX = random.nextInt(DIMENSIONS), startY = random.nextInt(DIMENSIONS);
        int count = 0;
        for (int i = 0; i < sqrt; i++) {
            for (int j = 0; j < sqrt; j++) {
                int height = random.nextInt(sqrt);
                int width = random.nextInt(sqrt);
                int upperOffset = random.nextInt(sqrt - height);
                int leftOffset = random.nextInt(sqrt - width);
                fences[count++] = new int[][]{{i + upperOffset, j + leftOffset}, {i + upperOffset + height, j + leftOffset + width}};
            }
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                originalFlowers[i][j] = random.nextInt(100000) + 1;
                limits[i][j] = random.nextInt(15) + 1;
                regrow[i][j] = random.nextInt(200) + 1;
            }
        }
        final int penalty = random.nextInt(2000000);
        final AntColony antColony = new AntColony(originalFlowers, regrow, limits, DIMENSIONS, fences, startX, startY,
                                                  penalty);
        long bestReward = 0;
        for (double alpha = 0; alpha <= 2; alpha += 0.05) {
            //for (double beta = 1; beta <= 1.5; beta += 0.133) {
            //  for (double evaporation = 0.05; evaporation <= 0.2; evaporation += 0.067) {
            antColony.setALPHA(alpha);
            double beta = 1.0;
            double evaporation = 0.117;
            antColony.setBETA(beta);
            antColony.setEvaporationCoefficient(evaporation);
            antColony.setStartTime(System.currentTimeMillis());
            final long reward = antColony.findBestTour().getReward();
            System.out.println(alpha + " " + beta + " " + evaporation);
            System.out.println(reward);
            if (bestReward < reward) {
                System.out.println("GO GO GO!!!");
                bestReward = reward;
            }
            //  }
            //}
        }

        //System.out.println(antColony.bruteForceTour().getReward());
    }
}
