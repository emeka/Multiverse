package org.multiverse.stms.gamma.benchmarks;

import org.multiverse.TestThread;
import org.multiverse.stms.gamma.BenchmarkUtils;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Result;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;

/**
 * @author Peter Veentjer
 */
public class UncontendedMultipleReadBenchmark implements GammaConstants {
    private GammaStm stm;
    private final long transactionCount = 100 * 1000 * 1000;


    public static void main(String[] args) {
        //should be a power of two.
        int refCount = Integer.parseInt(args[0]);
        UncontendedMultipleReadBenchmark test = new UncontendedMultipleReadBenchmark();

        test.start(refCount);
    }

    public void start(int refCount) {
        int[] processors = BenchmarkUtils.generateProcessorRange();

        System.out.printf("Multiverse> Uncontended multiple read transaction benchmark\n");
        System.out.printf("Multiverse> Running with the following processor range %s\n", Arrays.toString(processors));
        System.out.printf("Multiverse> Running with %s transactionalobjects per transaction\n", refCount);
        System.out.printf("Multiverse> %s Transactions per thread\n", format(transactionCount));
        Result[] result = new Result[processors.length];

        System.out.printf("Multiverse> Starting warmup run\n");
        test(1, 1);
        System.out.printf("Multiverse> Finished warmup run\n");

        long startNs = System.nanoTime();

        for (int k = 0; k < processors.length; k++) {
            int processorCount = processors[k];
            double performance = test(processorCount, refCount);
            result[k] = new Result(processorCount, performance);
        }

        long durationNs = System.nanoTime() - startNs;
        System.out.printf("Benchmark took %s seconds\n", TimeUnit.NANOSECONDS.toSeconds(durationNs));

        BenchmarkUtils.toGnuplot(result);
    }


    private double test(int threadCount, int refCount) {
        System.out.printf("Multiverse> Running with %s processors\n", threadCount);

        stm = new GammaStm();

        ReadThread[] threads = new ReadThread[threadCount];

        for (int k = 0; k < threads.length; k++) {
            threads[k] = new ReadThread(k, refCount);
        }

        startAll(threads);
        joinAll(threads);

        long totalDurationMs = 0;
        for (ReadThread t : threads) {
            totalDurationMs += t.durationMs;
        }

        double readsPerSecond = BenchmarkUtils.transactionsPerSecondPerThread(
                transactionCount * refCount, totalDurationMs, threadCount);

        System.out.printf("Multiverse> Performance %s reads/second with %s threads\n",
                format(readsPerSecond), threadCount);
        return readsPerSecond;
    }

    class ReadThread extends TestThread {
        private final int refCount;
        private long durationMs;

        public ReadThread(int id, int refCount) {
            super("ReadThread-" + id);
            setPriority(Thread.MAX_PRIORITY);
            this.refCount = refCount;
        }

        public void doRun() {
            switch (refCount) {
                case 1:
                    run1();
                    break;
                case 2:
                    run2();
                    break;
                case 4:
                    run4();
                    break;
                case 8:
                    run8();
                    break;
                case 16:
                    run16();
                    break;
                case 32:
                    run32();
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        public void run1() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);

            GammaTxnConfig config = new GammaTxnConfig(stm, 1)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                tx.commit();
                tx.hardReset();
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }

        public void run2() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);
            GammaTxnLong ref2 = newReadBiasedLongRef(stm);

            GammaTxnConfig config = new GammaTxnConfig(stm, 2)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                ref2.openForRead(tx, LOCKMODE_NONE);
                tx.commit();
                tx.hardReset();

            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }

        public void run4() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);
            GammaTxnLong ref2 = newReadBiasedLongRef(stm);
            GammaTxnLong ref3 = newReadBiasedLongRef(stm);
            GammaTxnLong ref4 = newReadBiasedLongRef(stm);

            GammaTxnConfig config = new GammaTxnConfig(stm, 4)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                ref2.openForRead(tx, LOCKMODE_NONE);
                ref3.openForRead(tx, LOCKMODE_NONE);
                ref4.openForRead(tx, LOCKMODE_NONE);
                tx.commit();
                tx.hardReset();
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }

        public void run8() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);
            GammaTxnLong ref2 = newReadBiasedLongRef(stm);
            GammaTxnLong ref3 = newReadBiasedLongRef(stm);
            GammaTxnLong ref4 = newReadBiasedLongRef(stm);
            GammaTxnLong ref5 = newReadBiasedLongRef(stm);
            GammaTxnLong ref6 = newReadBiasedLongRef(stm);
            GammaTxnLong ref7 = newReadBiasedLongRef(stm);
            GammaTxnLong ref8 = newReadBiasedLongRef(stm);

            GammaTxnConfig config = new GammaTxnConfig(stm, 8)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                ref2.openForRead(tx, LOCKMODE_NONE);
                ref3.openForRead(tx, LOCKMODE_NONE);
                ref4.openForRead(tx, LOCKMODE_NONE);
                ref5.openForRead(tx, LOCKMODE_NONE);
                ref6.openForRead(tx, LOCKMODE_NONE);
                ref7.openForRead(tx, LOCKMODE_NONE);
                ref8.openForRead(tx, LOCKMODE_NONE);

                tx.commit();
                tx.hardReset();

            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }

        public void run16() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);
            GammaTxnLong ref2 = newReadBiasedLongRef(stm);
            GammaTxnLong ref3 = newReadBiasedLongRef(stm);
            GammaTxnLong ref4 = newReadBiasedLongRef(stm);
            GammaTxnLong ref5 = newReadBiasedLongRef(stm);
            GammaTxnLong ref6 = newReadBiasedLongRef(stm);
            GammaTxnLong ref7 = newReadBiasedLongRef(stm);
            GammaTxnLong ref8 = newReadBiasedLongRef(stm);
            GammaTxnLong ref9 = newReadBiasedLongRef(stm);
            GammaTxnLong ref10 = newReadBiasedLongRef(stm);
            GammaTxnLong ref11 = newReadBiasedLongRef(stm);
            GammaTxnLong ref12 = newReadBiasedLongRef(stm);
            GammaTxnLong ref13 = newReadBiasedLongRef(stm);
            GammaTxnLong ref14 = newReadBiasedLongRef(stm);
            GammaTxnLong ref15 = newReadBiasedLongRef(stm);
            GammaTxnLong ref16 = newReadBiasedLongRef(stm);

            GammaTxnConfig config = new GammaTxnConfig(stm, 16)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                ref2.openForRead(tx, LOCKMODE_NONE);
                ref3.openForRead(tx, LOCKMODE_NONE);
                ref4.openForRead(tx, LOCKMODE_NONE);
                ref5.openForRead(tx, LOCKMODE_NONE);
                ref6.openForRead(tx, LOCKMODE_NONE);
                ref7.openForRead(tx, LOCKMODE_NONE);
                ref8.openForRead(tx, LOCKMODE_NONE);
                ref9.openForRead(tx, LOCKMODE_NONE);
                ref10.openForRead(tx, LOCKMODE_NONE);
                ref11.openForRead(tx, LOCKMODE_NONE);
                ref12.openForRead(tx, LOCKMODE_NONE);
                ref13.openForRead(tx, LOCKMODE_NONE);
                ref14.openForRead(tx, LOCKMODE_NONE);
                ref15.openForRead(tx, LOCKMODE_NONE);
                ref15.openForRead(tx, LOCKMODE_NONE);
                ref16.openForRead(tx, LOCKMODE_NONE);

                tx.commit();
                tx.hardReset();

            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }

        public void run32() {
            GammaTxnLong ref1 = newReadBiasedLongRef(stm);
            GammaTxnLong ref2 = newReadBiasedLongRef(stm);
            GammaTxnLong ref3 = newReadBiasedLongRef(stm);
            GammaTxnLong ref4 = newReadBiasedLongRef(stm);
            GammaTxnLong ref5 = newReadBiasedLongRef(stm);
            GammaTxnLong ref6 = newReadBiasedLongRef(stm);
            GammaTxnLong ref7 = newReadBiasedLongRef(stm);
            GammaTxnLong ref8 = newReadBiasedLongRef(stm);
            GammaTxnLong ref9 = newReadBiasedLongRef(stm);
            GammaTxnLong ref10 = newReadBiasedLongRef(stm);
            GammaTxnLong ref11 = newReadBiasedLongRef(stm);
            GammaTxnLong ref12 = newReadBiasedLongRef(stm);
            GammaTxnLong ref13 = newReadBiasedLongRef(stm);
            GammaTxnLong ref14 = newReadBiasedLongRef(stm);
            GammaTxnLong ref15 = newReadBiasedLongRef(stm);
            GammaTxnLong ref16 = newReadBiasedLongRef(stm);
            GammaTxnLong ref17 = newReadBiasedLongRef(stm);
            GammaTxnLong ref18 = newReadBiasedLongRef(stm);
            GammaTxnLong ref19 = newReadBiasedLongRef(stm);
            GammaTxnLong ref20 = newReadBiasedLongRef(stm);
            GammaTxnLong ref21 = newReadBiasedLongRef(stm);
            GammaTxnLong ref22 = newReadBiasedLongRef(stm);
            GammaTxnLong ref23 = newReadBiasedLongRef(stm);
            GammaTxnLong ref24 = newReadBiasedLongRef(stm);
            GammaTxnLong ref25 = newReadBiasedLongRef(stm);
            GammaTxnLong ref26 = newReadBiasedLongRef(stm);
            GammaTxnLong ref27 = newReadBiasedLongRef(stm);
            GammaTxnLong ref28 = newReadBiasedLongRef(stm);
            GammaTxnLong ref29 = newReadBiasedLongRef(stm);
            GammaTxnLong ref30 = newReadBiasedLongRef(stm);
            GammaTxnLong ref31 = newReadBiasedLongRef(stm);
            GammaTxnLong ref32 = newReadBiasedLongRef(stm);


            GammaTxnConfig config = new GammaTxnConfig(stm, 32)
                    .setReadonly(true);
            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

            long startMs = System.currentTimeMillis();

            for (long iteration = 0; iteration < transactionCount; iteration++) {
                ref1.openForRead(tx, LOCKMODE_NONE);
                ref2.openForRead(tx, LOCKMODE_NONE);
                ref3.openForRead(tx, LOCKMODE_NONE);
                ref4.openForRead(tx, LOCKMODE_NONE);
                ref5.openForRead(tx, LOCKMODE_NONE);
                ref6.openForRead(tx, LOCKMODE_NONE);
                ref7.openForRead(tx, LOCKMODE_NONE);
                ref8.openForRead(tx, LOCKMODE_NONE);
                ref9.openForRead(tx, LOCKMODE_NONE);
                ref10.openForRead(tx, LOCKMODE_NONE);
                ref11.openForRead(tx, LOCKMODE_NONE);
                ref12.openForRead(tx, LOCKMODE_NONE);
                ref13.openForRead(tx, LOCKMODE_NONE);
                ref14.openForRead(tx, LOCKMODE_NONE);
                ref15.openForRead(tx, LOCKMODE_NONE);
                ref15.openForRead(tx, LOCKMODE_NONE);
                ref16.openForRead(tx, LOCKMODE_NONE);
                ref17.openForRead(tx, LOCKMODE_NONE);
                ref18.openForRead(tx, LOCKMODE_NONE);
                ref19.openForRead(tx, LOCKMODE_NONE);
                ref20.openForRead(tx, LOCKMODE_NONE);
                ref21.openForRead(tx, LOCKMODE_NONE);
                ref22.openForRead(tx, LOCKMODE_NONE);
                ref23.openForRead(tx, LOCKMODE_NONE);
                ref24.openForRead(tx, LOCKMODE_NONE);
                ref25.openForRead(tx, LOCKMODE_NONE);
                ref26.openForRead(tx, LOCKMODE_NONE);
                ref27.openForRead(tx, LOCKMODE_NONE);
                ref28.openForRead(tx, LOCKMODE_NONE);
                ref29.openForRead(tx, LOCKMODE_NONE);
                ref30.openForRead(tx, LOCKMODE_NONE);
                ref31.openForRead(tx, LOCKMODE_NONE);
                ref32.openForRead(tx, LOCKMODE_NONE);
                tx.commit();
                tx.hardReset();
            }

            durationMs = System.currentTimeMillis() - startMs;
            System.out.printf("Multiverse> %s is finished in %s ms\n", getName(), durationMs);
        }
    }

    private GammaTxnLong newReadBiasedLongRef(GammaStm stm) {
        return makeReadBiased(new GammaTxnLong(stm));
    }
}
