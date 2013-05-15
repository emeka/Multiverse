package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.BenchmarkUtils;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class BoxingOverheadDriver extends BenchmarkDriver implements GammaConstants {

    private GammaStm stm;
    private boolean withBoxing;
    private long transactionsPerThread;
    private int threadCount;
    private UpdateThread[] threads;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Transactions/thread %s \n", BenchmarkUtils.format(transactionsPerThread));
        System.out.printf("Multiverse > ThreadCount %s \n", threadCount);
        System.out.printf("Multiverse > With Boxing %s \n", withBoxing);

        stm = new GammaStm();
        threads = new UpdateThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new UpdateThread(k);
        }
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        startAll(threads);
        joinAll(threads);
    }

    @Override
    public void processResults(TestCaseResult testCaseResult) {
        long totalDurationMs = 0;
        for (UpdateThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double transactionsPerSecondPerThread = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionsPerThread, totalDurationMs, threadCount);

        double transactionsPerSecond = BenchmarkUtils.transactionsPerSecond(
                transactionsPerThread, totalDurationMs, threadCount);

        System.out.printf("Multiverse > Performance %s transactions/second/thread\n",
                BenchmarkUtils.format(transactionsPerSecondPerThread));
        System.out.printf("Multiverse > Performance %s transactions/second\n",
                BenchmarkUtils.format(transactionsPerSecond));

        testCaseResult.put("transactionsPerThreadPerSecond", transactionsPerSecondPerThread);
        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    class UpdateThread extends TestThread {
        private long durationMs;

        public UpdateThread(int id) {
            super("UpdateThread-" + id);
        }

        public void doRun() {
            FatMonoGammaTxn tx = new FatMonoGammaTxn(
                    new GammaTxnConfig(stm)
                            .setReadLockMode(LockMode.Exclusive)
                            .setDirtyCheckEnabled(false));

            long startMs = System.currentTimeMillis();

            final long _transactionCount = transactionsPerThread;
            if (withBoxing) {
                GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, new Long(0));
                for (long k = 0; k < _transactionCount; k++) {
                    ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
                    tx.commit();
                    tx.hardReset();
                }
                assertEquals(_transactionCount, (long) ref.atomicGet());
            } else {
                GammaTxnLong ref = new GammaTxnLong(stm);
                for (long k = 0; k < _transactionCount; k++) {
                    ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
                    tx.commit();
                    tx.hardReset();
                }
                assertEquals(_transactionCount, ref.atomicGet());
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse > %s is finished in %s ms\n", getName(), durationMs);
        }
    }
}
