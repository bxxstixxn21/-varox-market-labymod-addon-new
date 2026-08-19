package net.varoxcraft.addons.market.hud;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.varoxcraft.addons.market.model.VaroxMarketItem;
import net.varoxcraft.addons.market.service.MarketService;

/**
 * Ein konfigurierbares LabyMod-HUD für die markantesten Preisbewegungen.
 */
public final class VaroxMarketHudWidget extends TextHudWidget<TextHudWidgetConfig> {

  private static final NumberFormat PRICE_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);

  private final MarketService marketService;
  private TextLine header;
  private TextLine firstMover;
  private TextLine secondMover;
  private TextLine thirdMover;
  private TextLine status;

  public VaroxMarketHudWidget(HudWidgetCategory category, MarketService marketService) {
    super("varox_market", TextHudWidgetConfig.class);
    this.marketService = marketService;
    this.bindCategory(category);
    PRICE_FORMAT.setMaximumFractionDigits(2);
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);
    this.header = this.createLine("Varox Markt", "Lädt...");
    this.firstMover = this.createLine("", "");
    this.secondMover = this.createLine("", "");
    this.thirdMover = this.createLine("", "");
    this.status = this.createLine("", "");
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    this.marketService.refreshIfDue();

    if (!this.marketService.hasData()) {
      this.header.updateAndFlush(this.marketService.isLoading() ? "Daten werden geladen..." : "Noch keine Marktdaten");
      this.header.setState(TextLine.State.VISIBLE);
      this.hideMovers();
      String error = this.marketService.lastError();
      if (error != null) {
        this.status.updateAndFlush(error);
        this.status.setState(TextLine.State.VISIBLE);
      } else {
        this.status.setState(TextLine.State.HIDDEN);
      }
      return;
    }

    this.header.updateAndFlush("Top-Bewegungen");
    this.header.setState(TextLine.State.VISIBLE);

    List<VaroxMarketItem> movers = this.marketService.topMovers(3);
    this.updateMover(this.firstMover, movers, 0);
    this.updateMover(this.secondMover, movers, 1);
    this.updateMover(this.thirdMover, movers, 2);

    this.status.updateAndFlush("/varoxmarkt <Item>");
    this.status.setState(TextLine.State.VISIBLE);
  }

  private void updateMover(TextLine line, List<VaroxMarketItem> movers, int index) {
    if (index >= movers.size()) {
      line.setState(TextLine.State.HIDDEN);
      return;
    }

    VaroxMarketItem item = movers.get(index);
    String direction = item.change() >= 0.0D ? "▲" : "▼";
    String label = direction + " " + item.name();
    String value = signedPercent(item.change()) + "%  " + PRICE_FORMAT.format(item.buy()) + " V";
    line.updateAndFlush(value);
    line.setState(TextLine.State.VISIBLE);
  }

  private void hideMovers() {
    this.firstMover.setState(TextLine.State.HIDDEN);
    this.secondMover.setState(TextLine.State.HIDDEN);
    this.thirdMover.setState(TextLine.State.HIDDEN);
  }

  private static String signedPercent(double value) {
    return String.format(Locale.GERMANY, "%+.1f", value);
  }
}
