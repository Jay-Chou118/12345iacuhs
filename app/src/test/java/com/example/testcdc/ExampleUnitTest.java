package com.example.testcdc;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.testcdc.Utils.Utils;
import com.example.testcdc.entity.SignalInfo;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        byte[] data = {0,0,0,0,0,0,2,0};
        SignalInfo signalInfo = new SignalInfo();
        signalInfo.bitLength = 14;
        signalInfo.bitStart = 71;
        long signal = Utils.getSignal(71, 14, data);
        System.out.println(signal);
    }

    public void msgInfTest(){



    }
}