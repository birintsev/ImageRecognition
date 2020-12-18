package ua.edu.sumdu.elit.in71.birintsev.services.criteria;

import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;

@Service("ShannonCriteria")
public class ShannonCriteria extends AbstractCriteriaMethod {

	public ShannonCriteria(ClassBitmapService classBitmapService) {
		super(classBitmapService);
	}

	@Override
	public double calc(double d1, double d2) {
		double alpha = 1 - d1;
		double betta = 1 - d2;
		return 1 + 0.5
			* (
			(alpha/(alpha + d2)) * log2(alpha/(alpha + d2))
				+ (betta/(betta + d1)) * log2(betta/(betta + d1))
				+ (d1/(d1 + betta)) * log2(d1/(d1 + betta))
				+ (d2/(alpha + d2)) * log2(d2/(alpha + d2))
		);
	}

	@Override
	public String getMethodName() {
		return "Shannon criteria";
	}
}
