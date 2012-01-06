package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

public abstract class FatGammaTransaction_softResetTest<T extends GammaTransaction> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();
}
