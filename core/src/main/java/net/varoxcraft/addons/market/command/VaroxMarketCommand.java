package net.varoxcraft.addons.market.command;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.varoxcraft.addons.market.model.VaroxMarketItem;
import net.varoxcraft.addons.market.service.MarketService;

/**
 * Liefert Marktdaten im Chat. Der Befehl wird vollständig vom Client verarbeitet.
 */
public final class VaroxMarketCommand extends Command {

  private static final NumberFormat PRICE_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);
  private static final char[] SPARK_CHARS = {'▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

  private final MarketService marketService;

  public VaroxMarketCommand(MarketService marketService) {
    super("varoxmarkt", "vmarkt");
    this.marketService = marketService;
    PRICE_FORMAT.setMaximumFractionDigits(2);
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayHelp();
      return true;
    }

    if (arguments[0].equalsIgnoreCase("refresh")) {
      this.marketService.refresh();
      this.displayMessage(Component.text("Varox Markt: Aktualisierung gestartet.", NamedTextColor.YELLOW));
      return true;
    }

    if (arguments[0].equalsIgnoreCase("top")) {
      this.displayTopMovers();
      return true;
    }

    if (!this.marketService.hasData()) {
      this.marketService.refresh();
      this.displayMessage(Component.text("Varox Markt: Daten werden geladen. Bitte führe den Befehl gleich erneut aus.", NamedTextColor.YELLOW));
      return true;
    }

    String query = String.join(" ", arguments);
    Optional<VaroxMarketItem> result = this.marketService.findItem(query);
    if (result.isEmpty()) {
      this.displayMessage(Component.text("Varox Markt: Kein Item passend zu \"" + query + "\" gefunden.", NamedTextColor.RED));
      return true;
    }

    this.displayItem(result.get());
    return true;
  }

  private void displayHelp() {
    Component help = Component.text("◆ Varox Markt", NamedTextColor.GOLD)
        .append(Component.newline())
        .append(Component.text("  /varoxmarkt <Itemname>  ", NamedTextColor.WHITE))
        .append(Component.text("Preis und Verlauf", NamedTextColor.GRAY))
        .append(Component.newline())
        .append(Component.text("  /varoxmarkt top  ", NamedTextColor.WHITE))
        .append(Component.text("stärkste Bewegungen", NamedTextColor.GRAY))
        .append(Component.newline())
        .append(Component.text("  /varoxmarkt refresh  ", NamedTextColor.WHITE))
        .append(Component.text("Daten aktualisieren", NamedTextColor.GRAY));
    this.displayMessage(help);
  }

  private void displayTopMovers() {
    if (!this.marketService.hasData()) {
      this.marketService.refresh();
      this.displayMessage(Component.text("Varox Markt: Daten werden geladen. Bitte führe den Befehl gleich erneut aus.", NamedTextColor.YELLOW));
      return;
    }

    List<VaroxMarketItem> movers = this.marketService.topMovers(5);
    Component output = Component.text("◆ Varox Markt", NamedTextColor.GOLD)
        .append(Component.text("  Top-Bewegungen", NamedTextColor.GRAY));
    if (movers.isEmpty()) {
      output.append(Component.newline()).append(Component.text("  Keine Preisänderungen vorhanden.", NamedTextColor.GRAY));
      this.displayMessage(output);
      return;
    }

    for (int index = 0; index < movers.size(); index++) {
      output.append(Component.newline()).append(this.formatTopMover(movers.get(index), index + 1));
    }
    output.append(Component.newline()).append(Component.text("  /vmarkt <Item>", NamedTextColor.DARK_GRAY));
    this.displayMessage(output);
  }

  private void displayItem(VaroxMarketItem item) {
    TextColor trendColor = item.change() > 0.0D ? NamedTextColor.GREEN
        : item.change() < 0.0D ? NamedTextColor.RED : NamedTextColor.GRAY;
    String direction = item.change() > 0.0D ? "▲" : item.change() < 0.0D ? "▼" : "•";
    String sellPrice = item.sell() > 0.0D ? PRICE_FORMAT.format(item.sell()) + " V" : "—";

    Component output = Component.text("◆ ", NamedTextColor.GOLD)
        .append(Component.text(item.name(), NamedTextColor.WHITE))
        .append(Component.newline())
        .append(Component.text("  Kauf  ", NamedTextColor.GRAY))
        .append(Component.text(PRICE_FORMAT.format(item.buy()) + " V", NamedTextColor.YELLOW))
        .append(Component.text("   │   Verkauf  ", NamedTextColor.GRAY))
        .append(Component.text(sellPrice, NamedTextColor.YELLOW))
        .append(Component.newline())
        .append(Component.text("  " + direction + " " + signedPercent(item.change()) + "%", trendColor))
        .append(Component.text("   │   Verlauf  ", NamedTextColor.GRAY))
        .append(Component.text(sparkline(item), NamedTextColor.AQUA));
    this.displayMessage(output);
  }

  private Component formatTopMover(VaroxMarketItem item, int position) {
    TextColor trendColor = item.change() >= 0.0D ? NamedTextColor.GREEN : NamedTextColor.RED;
    String direction = item.change() >= 0.0D ? "▲" : "▼";
    return Component.text("  " + position + ". ", NamedTextColor.DARK_GRAY)
        .append(Component.text(item.name(), NamedTextColor.WHITE))
        .append(Component.text("  " + direction + " " + signedPercent(item.change()) + "%", trendColor))
        .append(Component.text("  •  " + PRICE_FORMAT.format(item.buy()) + " V", NamedTextColor.YELLOW));
  }

  private static String sparkline(VaroxMarketItem item) {
    if (item.spark() == null || item.spark().size() < 2) {
      return "keine Verlaufsdaten";
    }

    double minimum = item.spark().stream().mapToDouble(Double::doubleValue).min().orElse(0.0D);
    double maximum = item.spark().stream().mapToDouble(Double::doubleValue).max().orElse(0.0D);
    if (Double.compare(minimum, maximum) == 0) {
      return "──────── (unverändert)";
    }

    StringBuilder output = new StringBuilder(item.spark().size());
    for (double point : item.spark()) {
      double normalized = (point - minimum) / (maximum - minimum);
      int index = Math.min(SPARK_CHARS.length - 1, (int) Math.round(normalized * (SPARK_CHARS.length - 1)));
      output.append(SPARK_CHARS[index]);
    }
    return output.toString();
  }

  private static String signedPercent(double value) {
    return String.format(Locale.GERMANY, "%+.1f", value);
  }
}
