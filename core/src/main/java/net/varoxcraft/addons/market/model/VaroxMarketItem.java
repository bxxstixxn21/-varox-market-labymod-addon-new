package net.varoxcraft.addons.market.model;

import java.util.List;
import java.util.Locale;

/**
 * Ein Datensatz aus dem öffentlichen VaroxCraft-Marktdatendienst.
 */
public record VaroxMarketItem(
    String name,
    String cat,
    String catName,
    double buy,
    double sell,
    String icon,
    double change,
    List<Double> spark,
    long ts
) {

  public String normalizedName() {
    return this.name == null ? "" : this.name.toLowerCase(Locale.ROOT);
  }

  public boolean hasMeaningfulHistory() {
    if (this.spark == null || this.spark.size() < 2) {
      return false;
    }

    double first = this.spark.getFirst();
    return this.spark.stream().anyMatch(value -> Double.compare(value, first) != 0);
  }
}
