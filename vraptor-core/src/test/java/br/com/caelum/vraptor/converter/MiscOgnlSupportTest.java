package br.com.caelum.vraptor.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.converter.OgnlGenericTypesSupportTest.Cat;
import br.com.caelum.vraptor.converter.OgnlGenericTypesSupportTest.Leg;
import br.com.caelum.vraptor.core.Converters;
import br.com.caelum.vraptor.http.ognl.ListAccessor;
import br.com.caelum.vraptor.http.ognl.ReflectionBasedNullHandler;

/**
 * Unfortunately OGNL sucks so bad in its design that we had to create a "unit"
 * test which accesses more than a single class to test the ognl funcionality.
 * Even worse, OGNL sucks with its static configuration methods in such a way
 * that tests are not thread safe. Summing up: OGNL api sucks, OGNL idea rulez.
 * Tests written here are "acceptance tests" for the Ognl support on http
 * parameters.
 * 
 * @author Guilherme Silveira
 * 
 */
public class MiscOgnlSupportTest {

    private Mockery mockery;
    private Cat myCat;
    private Converters converters;
    private OgnlContext context;

    @Before
    public void setup() {
        this.mockery = new Mockery();
        this.converters = mockery.mock(Converters.class);
        this.myCat = new Cat();
        OgnlRuntime.setNullHandler(Object.class, new ReflectionBasedNullHandler());
        OgnlRuntime.setPropertyAccessor(List.class, new ListAccessor());
        this.context = (OgnlContext) Ognl.createDefaultContext(myCat);
        context.setTraceEvaluations(true);
        // OgnlRuntime.setPropertyAccessor(Set.class, new SetAccessor());
        // OgnlRuntime.setPropertyAccessor(Array.class, new ArrayAccessor());
        // OgnlRuntime.setPropertyAccessor(Map.class, new MapAccessor());
        Ognl.setTypeConverter(context, new OgnlToConvertersController(converters));
    }

    public static class Cat {
        private Leg firstLeg;

        public void setFirstLeg(Leg firstLeg) {
            this.firstLeg = firstLeg;
        }

        public Leg getFirstLeg() {
            return firstLeg;
        }
    }

    public static class Leg {
        private Integer id;

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }

    @Test
    public void isCapableOfDealingWithEmptyParameterForInternalValue() throws OgnlException {
        mockery.checking(new Expectations() {{
            one(converters).to(Integer.class, null); will(returnValue(new IntegerConverter()));
        }});
        Ognl.setValue("firstLeg.id", context, myCat, "");
        assertThat(myCat.firstLeg.id, is(equalTo(null)));
        mockery.assertIsSatisfied();
    }

}
