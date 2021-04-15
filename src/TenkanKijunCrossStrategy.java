import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

/**
 * Tenkan-Sen / Kijun-Sen Cross Strategy. This is based of the Ichimoku study and adds the ability to trade.
 */
@StudyHeader(
        namespace = "com.mycompany",
        id = "TENKAN_KIJUN_CROSS_STRATEGY",
        rb="ichimoku_extensions.nls.strings",
        name = "Tenkan/Kijun Cross Strategy",
        desc = "Buys when the Tenkan-Sen crosses above the Kijun-Sen and sells when it crosses below.",
        menu = "ICHIMOKU_EXTENSIONS",
        overlay = true,
        signals = true,
        strategy = true,
        autoEntry = true,
        manualEntry = false,
        supportsUnrealizedPL = true,
        supportsRealizedPL = true,
        supportsTotalPL = true)

public class TenkanKijunCrossStrategy extends IchimokuKinkoHyo {
    @Override
    public void onActivate(OrderContext ctx) {
        if (getSettings().isEnterOnActivate()) {
            var series = ctx.getDataContext().getDataSeries();
            int ind = series.isLastBarComplete() ? series.size() - 1 : series.size() - 2;

            Double tenkan = series.getDouble(ind, Values.TS);
            Double kijun = series.getDouble(ind, Values.KS);

            if (tenkan == null || kijun == null) return;

            int tradeLots = getSettings().getTradeLots();
            int qty = tradeLots *= ctx.getInstrument().getDefaultQuantity();

            // Create a long or short position if we are above or below the signal line
            if (tenkan > kijun) {
                ctx.buy(qty);
            } else {
                ctx.sell(qty);
            }
        }
    }

    @Override
    public void onSignal(OrderContext ctx, Object signal) {
        var instr = ctx.getInstrument();
        int position = ctx.getPosition();
        int qty = (getSettings().getTradeLots() * instr.getDefaultQuantity());

        // Stop and Reverse if there is an open position
        qty += Math.abs(position);

        if (position <= 0 && signal == Signals.TENKAN_CROSS_ABOVE_KIJUN) {
            ctx.buy(qty); // Open Long Position
        }
        if (position >= 0 && signal == Signals.TENKAN_CROSS_BELOW_KIJUN) {
            ctx.sell(qty); // Open Short Position
        }
    }
}
