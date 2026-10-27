import com.logismart.DeliveryOptimizer;
import java.nio.file.Path;

/** Dependency-free regression checks for the sample dataset. Run with -ea. */
public class SelfTest {
    public static void main(String[] args) throws Exception {
        var result = new DeliveryOptimizer().run(Path.of("data"));
        assert result.orders() == 6 : "Expected six sample orders";
        assert result.allocated() == 6 : "All sample orders should be allocated";
        assert result.baselineKm() == 48 : "Unexpected baseline route distance: " + result.baselineKm();
        assert result.optimizedKm() == 43 : "Unexpected optimized route distance: " + result.optimizedKm();
        assert Math.abs(result.reductionPercent() - 10.4166667) < 0.01 : "Unexpected reduction";
        for (var p : result.plans()) assert p.load() <= p.capacity() : "Capacity exceeded for " + p.vehicleId();
        assert result.warnings().isEmpty() : "Unexpected validation warnings: " + result.warnings();
        System.out.println("PASS: sample order count, allocation, capacity, route totals, reduction and warnings.");
        System.out.println("Baseline=" + result.baselineKm() + " km; optimized=" + result.optimizedKm() + " km; reduction=" + String.format(java.util.Locale.US,"%.2f",result.reductionPercent()) + "%");
    }
}
