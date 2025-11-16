import java.util.Map;
import java.util.Set;

/**
 * Utility class for converting monetary values between currencies.
 * <p>
 * Currency conversion is performed using predefined, static exchange rates.
 * These values do not reflect real market fluctuations or banking commissions.
 * 
 *
 * <p><b>Supported currencies:</b>
 * <ul>
 *     <li>RON</li>
 *     <li>EUR</li>
 *     <li>USD</li>
 * </ul>
 *
 * <p><b>Supported exchange pairs and rates:</b>
 * <pre>
 * RON → EUR = ÷ 5.0
 * EUR → RON = × 5.0
 *
 * RON → USD = ÷ 4.7
 * USD → RON = × 4.7
 *
 * EUR → USD = × 1.07
 * USD → EUR = ÷ 1.07
 * </pre>
 *
 * <p>Example:
 * <pre>
 * double eur = ExchangeRate.convert(100, "RON", "EUR");
 * double usd = ExchangeRate.convert(50, "EUR", "USD");
 * </pre>
 *
 * @author Ilinca Rusescu
 * @version 1.2
 */
public class ExchangeRate {

    // =============================
    //        CONFIGURATION
    // =============================

    /**
     * All supported currencies in ISO format.
     */
    public static final Set<String> SUPPORTED_CURRENCIES = Set.of("RON", "EUR", "USD");

    /**
     * Predefined conversion rates used for static lookup.
     * <p>Format of the key: {@code "FROM->TO"}
     */
    private static final Map<String, Double> RATE_MAP = Map.of(
            "RON->EUR", 1 / 5.0,
            "EUR->RON", 5.0,

            "RON->USD", 1 / 4.7,
            "USD->RON", 4.7,

            "EUR->USD", 1.07,
            "USD->EUR", 1 / 1.07
    );

    // =============================
    //        MAIN FUNCTION
    // =============================

    /**
     * Converts a given monetary {@code amount} from one currency to another
     * using predefined static rates.
     *
     * @param amount the amount to convert (must be ≥ 0)
     * @param from   source currency (e.g., "RON")
     * @param to     target currency (e.g., "EUR")
     * @return the converted value in the target currency
     *
     * @throws IllegalArgumentException if {@code amount < 0} or either currency code is invalid
     * @throws RuntimeException if the conversion pair is unsupported
     */

    public static double convert(double amount, String from, String to) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount to convert cannot be negative.");

        from = from.toUpperCase();
        to = to.toUpperCase();

        if (!SUPPORTED_CURRENCIES.contains(from) || !SUPPORTED_CURRENCIES.contains(to))
            throw new IllegalArgumentException("Currency must be one of: " + SUPPORTED_CURRENCIES);

        if (from.equals(to))
            return amount;

        String key = from + "->" + to;

        Double rate = RATE_MAP.get(key);
        if (rate == null)
            throw new RuntimeException("Unsupported currency conversion: " + from + " → " + to);

        return amount * rate;
    }
}