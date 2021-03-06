package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnStack_containsTest {

    private Stm stm;
    private NaiveTxnStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        stack = new NaiveTxnStack<String>(stm);
    }

    @Test
    public void whenNullItem() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");
                boolean result = stack.contains(null);
                assertFalse(result);
                assertEquals("[2, 1]", stack.toString());
            }
        });
    }

    @Test
    public void whenEmptyStack() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                boolean result = stack.contains("foo");

                assertFalse(result);
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenStackDoesntContainItem() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");
                stack.push("3");
                stack.push("4");

                boolean result = stack.contains("b");

                assertFalse(result);
                assertEquals("[4, 3, 2, 1]", stack.toString());
            }
        });
    }

    @Test
    public void whenContainsItem() {
         StmUtils.atomic(new TxnVoidCallable() {
             @Override
             public void call(Txn tx) throws Exception {
                 stack.push("1");
                 stack.push("2");
                 stack.push("3");
                 stack.push("4");

                 boolean result = stack.contains("3");

                 assertTrue(result);
                 assertEquals("[4, 3, 2, 1]", stack.toString());
             }
         });
    }
}
