package org.multiverse.stms.gamma.benchmarks.orec;

import org.benchy.BenchmarkDriver;
import org.benchy.BenchyUtils;
import org.benchy.TestCaseResult;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GlobalConflictCounter;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

public class OrecReadBiasedReadDriver extends BenchmarkDriver implements GammaConstants {
    private GammaTxnLong ref;
    private GlobalConflictCounter globalConflictCounter;
    private GammaStm stm;

    private long operationCount = 1000 * 1000 * 1000;
    private GammaTxnLong orec;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Operation count is %s\n", operationCount);

        stm = new GammaStm();
        ref = new GammaTxnLong(stm);
        globalConflictCounter = stm.getGlobalConflictCounter();
        orec = new GammaTxnLong(stm);
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        final long _cycles = operationCount;
        final GammaTxnLong _orec = orec;

        for (long k = 0; k < _cycles; k++) {
            int arriveStatus = _orec.arrive(0);
            if ((arriveStatus & MASK_UNREGISTERED) == 0) {
                _orec.departAfterReading();
            }
        }
    }

    @Override
    public void processResults(TestCaseResult result) {
        long durationMs = result.getDurationMs();
        double transactionsPerSecond = BenchyUtils.operationsPerSecond(operationCount, durationMs, 1);

        result.put("transactionsPerSecond", transactionsPerSecond);
        System.out.printf("Performance : %s update-cycles/second\n", transactionsPerSecond);
        System.out.printf("Orec        : %s\n", orec.___toOrecString());
    }

}
