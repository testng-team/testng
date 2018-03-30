package test.listeners.github1735;

import org.testng.IExecutionListener;
import org.testng.collections.Lists;

import java.util.List;

public class LocalExecutionListener implements IExecutionListener {
    private static final List<String> start = Lists.newArrayList();
    private static final List<String>  finish = Lists.newArrayList();

    @Override
    public void onExecutionStart() {
        start.add("start");
    }

    @Override
    public void onExecutionFinish() {
        finish.add("finish");
    }

    public static List<String> getFinish() {
        return finish;
    }

    public static List<String> getStart() {
        return start;
    }
}
