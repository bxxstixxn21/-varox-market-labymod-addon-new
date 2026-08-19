package net.varoxcraft.addons.market;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.varoxcraft.addons.market.command.VaroxMarketCommand;
import net.varoxcraft.addons.market.hud.VaroxMarketHudWidget;
import net.varoxcraft.addons.market.service.MarketService;

@AddonMain
public final class VaroxMarketAddon extends LabyAddon<VaroxMarketConfiguration> {

  private final MarketService marketService = new MarketService();

  @Override
  protected void enable() {
    this.registerSettingCategory();

    HudWidgetCategory category = new HudWidgetCategory("varox_market");
    this.labyAPI().hudWidgetRegistry().categoryRegistry().register(category);
    this.labyAPI().hudWidgetRegistry().register(new VaroxMarketHudWidget(category, this.marketService));
    this.registerCommand(new VaroxMarketCommand(this.marketService));

    this.marketService.refresh();
    this.logger().info("Varox Markt Addon wurde aktiviert.");
  }

  @Override
  protected Class<VaroxMarketConfiguration> configurationClass() {
    return VaroxMarketConfiguration.class;
  }
}
