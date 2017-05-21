package main.java.codingame.code4life;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static main.java.codingame.code4life.MoleculeType.*;


class Referee {

    static class ScienceProject {
        int[] cost;
        int index;

        public ScienceProject(int[] cost) {
            this.cost = cost;
        }
    }

    private List<LinkedList<SampleMaterial>> samplePool = Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>());

    private void initScienceProjects() {
        LinkedList<ScienceProject> scienceProjectPool;
        scienceProjectPool = new LinkedList<>();
        scienceProjectPool.add(new ScienceProject(new int[]{3, 3, 0, 0, 3}));
        scienceProjectPool.add(new ScienceProject(new int[]{0, 3, 3, 3, 0}));
        scienceProjectPool.add(new ScienceProject(new int[]{3, 0, 0, 3, 3}));
        scienceProjectPool.add(new ScienceProject(new int[]{0, 0, 4, 4, 0}));
        scienceProjectPool.add(new ScienceProject(new int[]{0, 4, 4, 0, 0}));
        scienceProjectPool.add(new ScienceProject(new int[]{0, 0, 0, 4, 4}));
        scienceProjectPool.add(new ScienceProject(new int[]{4, 0, 0, 0, 4}));
        scienceProjectPool.add(new ScienceProject(new int[]{3, 3, 3, 0, 0}));
        scienceProjectPool.add(new ScienceProject(new int[]{0, 0, 3, 3, 3}));
        scienceProjectPool.add(new ScienceProject(new int[]{4, 4, 0, 0, 0}));
    }

    private void initSamplePool() {
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 3, 0, 0, 0}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 2, 1}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 1, 1, 1, 1}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 2, 0, 0, 2}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 4, 0, 0}, 10, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 1, 2, 1, 1}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 2, 2, 0, 1}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{3, 1, 0, 0, 1}, 1, A));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 0, 0, 0, 2}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 0, 3}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 0, 1, 1, 1}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 2, 0, 2}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 4, 0}, 10, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 0, 1, 2, 1}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 0, 2, 2, 0}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 1, 3, 1, 0}, 1, B));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 1, 0, 0, 0}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 3, 0}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 1, 0, 1, 1}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 2, 0, 2, 0}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 0, 4}, 10, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 1, 0, 1, 2}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 1, 0, 2, 2}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 3, 1, 0, 0}, 1, C));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 2, 1, 0, 0}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{3, 0, 0, 0, 0}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 1, 1, 0, 1}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 0, 0, 2, 0}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{4, 0, 0, 0, 0}, 10, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 1, 1, 0, 1}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 0, 1, 0, 2}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 0, 0, 1, 3}, 1, D));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 2, 1, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 3, 0, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 1, 1, 1, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 0, 2, 0, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 4, 0, 0, 0}, 10, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{1, 2, 1, 1, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{2, 2, 0, 1, 0}, 1, E));
        samplePool.get(0).add(new SampleMaterial(new int[]{0, 0, 1, 3, 1}, 1, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 5, 0}, 20, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{6, 0, 0, 0, 0}, 30, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 3, 2, 2}, 10, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 1, 4, 2}, 20, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{2, 3, 0, 3, 0}, 10, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 5, 3}, 20, A));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 5, 0, 0, 0}, 20, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 6, 0, 0, 0}, 30, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 2, 2, 3, 0}, 10, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{2, 0, 0, 1, 4}, 20, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 2, 3, 0, 3}, 20, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{5, 3, 0, 0, 0}, 20, B));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 5, 0, 0}, 20, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 6, 0, 0}, 30, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{2, 3, 0, 0, 2}, 10, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{3, 0, 2, 3, 0}, 10, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{4, 2, 0, 0, 1}, 20, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 5, 3, 0, 0}, 20, C));
        samplePool.get(1).add(new SampleMaterial(new int[]{5, 0, 0, 0, 0}, 20, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 6, 0}, 30, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{2, 0, 0, 2, 3}, 10, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{1, 4, 2, 0, 0}, 20, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 3, 0, 2, 3}, 10, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{3, 0, 0, 0, 5}, 20, D));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 0, 5}, 20, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 0, 6}, 30, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{3, 2, 2, 0, 0}, 10, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 1, 4, 2, 0}, 20, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{3, 0, 3, 0, 2}, 10, E));
        samplePool.get(1).add(new SampleMaterial(new int[]{0, 0, 5, 3, 0}, 20, E));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 0, 7}, 40, A));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 0, 0, 0, 7}, 50, A));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 0, 0, 3, 6}, 40, A));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 3, 3, 5, 3}, 30, A));
        samplePool.get(2).add(new SampleMaterial(new int[]{7, 0, 0, 0, 0}, 40, B));
        samplePool.get(2).add(new SampleMaterial(new int[]{7, 3, 0, 0, 0}, 50, B));
        samplePool.get(2).add(new SampleMaterial(new int[]{6, 3, 0, 0, 3}, 40, B));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 0, 3, 3, 5}, 30, B));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 7, 0, 0, 0}, 40, C));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 7, 3, 0, 0}, 50, C));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 6, 3, 0, 0}, 40, C));
        samplePool.get(2).add(new SampleMaterial(new int[]{5, 3, 0, 3, 3}, 30, C));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 7, 0, 0}, 40, D));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 7, 3, 0}, 50, D));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 3, 6, 3, 0}, 40, D));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 5, 3, 0, 3}, 30, D));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 7, 0}, 40, E));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 7, 3}, 50, E));
        samplePool.get(2).add(new SampleMaterial(new int[]{0, 0, 3, 6, 3}, 40, E));
        samplePool.get(2).add(new SampleMaterial(new int[]{3, 3, 5, 3, 0}, 30, E));
    }
}

