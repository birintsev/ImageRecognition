package ua.edu.sumdu.elit.in71.birintsev.services.criteria;

import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;

@Service("CulbacCriteria")
public class CulbacCriteria extends AbstractCriteriaMethod {

	private final MathService mathService;

	public CulbacCriteria(
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
		double eps = 0.001;
		double alpha = 1 - d1;
		double betta = 1 - d2;
		return 0.5
			* mathService.log2(
				(d1 + d2 + eps) / (alpha + betta + eps)
			)
			* (d1 + d2 - alpha - betta);
	}

	@Override
	public String getMethodName() {
		return "Culbac criteria";
	}
}
