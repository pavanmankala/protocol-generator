package com.rtsffm.ertd.proto;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.InvocationHandler;

import org.junit.Assert;
import org.junit.Test;

//~--- JDK imports ------------------------------------------------------------


import java.lang.reflect.Method;
import java.util.Arrays;

//~--- classes ----------------------------------------------------------------

class SampleClass {
    public String test(String input) {
        return "Hello world!";
    }
}


public class TestCGLIb {
    @Test
    public void testFixedValue() throws Exception {
        System.out.println(Arrays.asList(" int  , clff, dddd  , dfmf ".split("\\s*,\\s*")));
        System.out.println(int.class.getClass());
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new FixedValue() {
            @Override
            public Object loadObject() throws Exception {
                return "Hello cglib!";
            }
        });

        SampleClass proxy = (SampleClass) enhancer.create();

        Assert.assertEquals("Hello cglib!", proxy.test(null));
    }

    @Test
    public void testInvocationHandler() throws Exception {
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ((method.getDeclaringClass() != Object.class) && (method.getReturnType() == String.class)) {
                    return "Hello cglib!";
                } else {
                    throw new RuntimeException("Do not know what to do.");
                }
            }
        });

        SampleClass proxy = (SampleClass) enhancer.create();

        Assert.assertEquals("Hello cglib!", proxy.test(null));
        // Assert.assertEquals("Hello cglib!", proxy.toString());
    }

    @Test
    public void testBeanGenerator() throws Exception {
        BeanGenerator beanGenerator = new BeanGenerator();

        beanGenerator.addProperty("value", String.class);

        Object myBean = beanGenerator.create();

        Method setter = myBean.getClass().getMethod("setValue", String.class);

        setter.invoke(myBean, "Hello cglib!");

        Method getter = myBean.getClass().getMethod("getValue");

        Assert.assertEquals("Hello cglib!", getter.invoke(myBean));
    }
}
