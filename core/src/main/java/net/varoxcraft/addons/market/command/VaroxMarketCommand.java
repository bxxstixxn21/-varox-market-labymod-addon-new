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
    this.displayMessage(Component.text("Varox Markt", NamedTextColor.GOLD));
    this.displayMessage(Component.text("/varoxmarkt <Itemname> – Preis und Verlauf anzeigen", NamedTextColor.GRAY));
    this.displayMessage(Component.text("/varoxmarkt top – stärkste Preisbewegungen", NamedTextColor.GRAY));
    this.displayMessage(Component.text("/varoxmarkt refresh – Daten sofort aktualisieren", NamedTextColor.GRAY));
  }

  private void displayTopMovers() {
    if (!this.marketService.hasData()) {
      this.marketService.refresh();
      this.displayMessage(Component.text("Varox Markt: Daten werden geladen. Bitte führe den Befehl gleich erneut aus.", NamedTextColor.YELLOW));
      return;
    }

    List<VaroxMarketItem> movers = this.marketService.topMovers(5);
    this.displayMessage(Component.text("Varox Markt – stärkste Bewegungen", NamedTextColor.GOLD));
    if (movers.isEmpty()) {
      this.displayMessage(Component.text("Zurzeit liegen keine Preisänderungen vor.", NamedTextColor.GRAY));
      return;
    }

    for (VaroxMarketItem item : movers) {
      TextColor color = item.change() >= 0.0D ? NamedTextColor.GREEN : NamedTextColor.RED;
      this.displayMessage(Component.text(item.name() + ": " + signedPercent(item.change()) + "% · "
          + PRICE_FORMAT.format(item.buy()) + " Voxis", color));
    }
  }

  private void displayItem(VaroxMarketItem item) {
    this.displayMessage(Component.text("Varox Markt – " + item.name(), NamedTextColor.GOLD));
    this.displayMessage(Component.text("Kaufpreis: " + PRICE_FORMAT.format(item.buy())
        + " Voxis | Verkaufspreis: " + (item.sell() > 0 ? PRICE_FORMAT.format(item.sell()) + " Voxis" : "—"),
        NamedTextColor.YELLOW));

    TextColor trendColor = item.change() > 0.0D ? NamedTextColor.GREEN
        : item.change() < 0.0D ? NamedTextColor.RED : NamedTextColor.GRAY;
    this.displayMessage(Component.text("Trend: " + signedPercent(item.change()) + "%", trendColor));
    this.displayMessage(Component.text("Verlauf: " + sparkline(item), NamedTextColor.AQUA));
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
