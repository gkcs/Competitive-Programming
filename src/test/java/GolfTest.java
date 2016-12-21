import main.java.codingame.GolfCourse;
import org.junit.Test;

public class GolfTest {
    @Test
    public void test() {
        char[][] grid = {"3..H.2".toCharArray(),
                ".2..H.".toCharArray(),
                "..H..H".toCharArray(),
                ".X.2.X".toCharArray(),
                "......".toCharArray(),
                "3..H..".toCharArray()};
        System.out.println(GolfCourse.printRaw(grid));
        final String run = GolfCourse.run(grid);
        System.out.println(run);
    }
}
