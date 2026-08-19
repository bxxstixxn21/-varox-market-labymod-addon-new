package net.varoxcraft.addons.market.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import net.varoxcraft.addons.market.model.VaroxMarketItem;

/**
 * Liest die öffentliche Marktquelle im Hintergrund und hält eine lokale, immutable Kopie vor.
 */
public final class MarketService {

  public static final URI DATA_URI = URI.create("https://varoxcraft.net/markt/data");
  private static final long REFRESH_INTERVAL_MILLIS = Duration.ofSeconds(60).toMillis();

  private final HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();
  private final Gson gson = new Gson();
  private final AtomicBoolean requestInFlight = new AtomicBoolean(false);
  private final AtomicLong lastRequestMillis = new AtomicLong(0L);

  private volatile List<VaroxMarketItem> items = List.of();
  private volatile Instant lastUpdated;
  private volatile String lastError;

  public void refreshIfDue() {
    long now = System.currentTimeMillis();
    if (now - this.lastRequestMillis.get() < REFRESH_INTERVAL_MILLIS) {
      return;
    }

    this.refresh();
  }

  public void refresh() {
    if (!this.requestInFlight.compareAndSet(false, true)) {
      return;
    }

    this.lastRequestMillis.set(System.currentTimeMillis());
    HttpRequest request = HttpRequest.newBuilder(DATA_URI)
        .timeout(Duration.ofSeconds(15))
        .header("Accept", "application/json")
        .header("User-Agent", "VaroxMarket-LabyMod4/1.0")
        .GET()
        .build();

    this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(this::parseResponse)
        .whenComplete((loadedItems, throwable) -> {
          this.requestInFlight.set(false);
          if (throwable != null) {
            this.lastError = "Marktdaten konnten nicht aktualisiert werden.";
            return;
          }

          this.items = loadedItems;
          this.lastUpdated = Instant.now();
          this.lastError = null;
        });
  }

  private List<VaroxMarketItem> parseResponse(HttpResponse<String> response) {
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException("HTTP-Status " + response.statusCode());
    }

    try {
      MarketResponse payload = this.gson.fromJson(response.body(), MarketResponse.class);
      if (payload == null || payload.items == null) {
        throw new IllegalStateException("Die Marktantwort enthält keine Itemliste.");
      }
      return List.copyOf(payload.items);
    } catch (JsonParseException exception) {
      throw new IllegalStateException("Die Marktantwort ist kein gültiges JSON.", exception);
    }
  }

  public Optional<VaroxMarketItem> findItem(String query) {
    String normalizedQuery = normalize(query);
    if (normalizedQuery.isBlank()) {
      return Optional.empty();
    }

    Optional<VaroxMarketItem> exactMatch = this.items.stream()
        .filter(item -> normalize(item.name()).equals(normalizedQuery))
        .findFirst();
    if (exactMatch.isPresent()) {
      return exactMatch;
    }

    return this.items.stream()
        .filter(item -> normalize(item.name()).contains(normalizedQuery))
        .findFirst();
  }

  public List<VaroxMarketItem> topMovers(int limit) {
    return this.items.stream()
        .filter(item -> item.name() != null)
        .filter(item -> Math.abs(item.change()) > 0.0D)
        .sorted(Comparator.comparingDouble((VaroxMarketItem item) -> Math.abs(item.change())).reversed())
        .limit(Math.max(0, limit))
        .toList();
  }

  public boolean isLoading() {
    return this.requestInFlight.get();
  }

  public boolean hasData() {
    return !this.items.isEmpty();
  }

  public String lastError() {
    return this.lastError;
  }

  public Instant lastUpdated() {
    return this.lastUpdated;
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
  }

  private static final class MarketResponse {
    private List<VaroxMarketItem> items;
  }
}
