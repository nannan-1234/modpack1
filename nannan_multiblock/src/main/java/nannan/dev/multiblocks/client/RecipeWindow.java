package nannan.dev.multiblocks.client;

/**
 * 主页面要显示的温度窗口（由屏幕每 tick 从同步字段刷新，温度条与温度曲线共用同一个实例）。
 *
 * <p>决策 16 说刻度要"跟着配方走"：这里显示的是**机器当前正在跑（或正在等温度）的那条配方**的窗口。
 * 换配方时窗口会跟着动，所以温度条上不会再有写死的 773 K 刻度。</p>
 */
public class RecipeWindow {

    public double min;
    public double max;
    /** 当前温度是否已经在窗口里（决定色带是亮的还是暗的）。 */
    public boolean inWindow;

    public boolean hasWindow() {
        return max > min;
    }

    public void update(double min, double max, boolean inWindow) {
        this.min = min;
        this.max = max;
        this.inWindow = inWindow;
    }
}
