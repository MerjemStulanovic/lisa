package it.unive.lisa.cron.numeric;

import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.AnalysisTestExecutor;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.impl.numeric.ProductDomain;
import org.junit.Test;

import static it.unive.lisa.LiSAFactory.getDefaultFor;

public class ProductDomainTest extends AnalysisTestExecutor {

    @Test
    public void testProductDomain() throws AnalysisSetupException {
        LiSAConfiguration conf = new LiSAConfiguration().setDumpAnalysis(true).setAbstractState(
                getDefaultFor(AbstractState.class, getDefaultFor(HeapDomain.class), new ProductDomain()));
        perform("product-domain", "product-domain.imp", conf);
    }
}