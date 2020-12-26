package ua.edu.sumdu.elit.in71.birintsev.services.criteria;

import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;

@Service("ShannonCriteria")
public class ShannonCriteria extends AbstractCriteriaMethod {

    private final MathService mathService;

    public ShannonCriteria(
        ClassBitmapService classBitmapService,
        MathService mathService
    ) {
        super(classBitmapService);
        this.mathService = mathService;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public double calc(double d1, double d2) {
        double alpha = 1 - d1;
        double betta = 1 - d2;
        
        double A  = alpha / (alpha + d2);
        double B  = betta / (betta + d1);
        double D1 = d1    / (betta + d1);
        double D2 = d2    / (alpha + d2);

        return  1 + 0.5 * (
                  zeroIfNotFinite(A  * mathService.log2(A))
                + zeroIfNotFinite(D1 * mathService.log2(D1))
                + zeroIfNotFinite(B  * mathService.log2(B))
                + zeroIfNotFinite(D2 * mathService.log2(D2))
            );
    }

    // returns non-NaN value (or 0 if value=Double.NaN)
    private double zeroIfNotFinite(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    @Override
    public String getMethodName() {
        return "Shannon criteria";
    }
}
