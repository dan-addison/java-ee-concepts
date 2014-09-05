package com.martinandersson.javaee.cdi.resolution;

import com.martinandersson.javaee.utils.Deployments;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.LongStream;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A common myth about CDI is that CDI can only inject interface types. Which
 * isn't true. Using the real class type at the injection point work flawlessly.
 * In fact, type is the number one "bean qualifier" that makes type safe
 * resolution possible.<p>
 * 
 * Note that in this deployment, there exists only one {@code SimpleCalculator}
 * which is {@code final}. Had there also existed a subclass thereof, then the
 * injection point in {@code BeanTypeResolutionDriver} will represent an
 * ambiguous dependency: two bean targets match. Such deployment will almost
 * always fail.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
@RunWith(Arquillian.class)
public class BeanTypeResolutionTest {
    
    @Deployment
    public static WebArchive buildDeployment() {
        return Deployments.buildCDIBeanArchive(
                BeanTypeResolutionTest.class,
                BeanTypeResolutionDriver.class,
                SimpleCalculator.class);
    }
    
    /**
     * WIll make a HTTP POST request to {@code TypeResolutionDriver}-servlet
     * which use a calculator that do not implement an interface. The calculator
     * is a POJO as much as a POJO can be.
     * 
     * @param url application url, provided by Arquillian
     * 
     * @throws MalformedURLException if things go to hell
     * @throws IOException if things go to hell
     */
    @Test
    @RunAsClient
    public void useSimpleCalculator(@ArquillianResource URL url) throws MalformedURLException, IOException {
        final URL testDriver = new URL(url, BeanTypeResolutionDriver.class.getSimpleName());
        final HttpURLConnection conn = (HttpURLConnection) testDriver.openConnection();
        
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "UTF-8");
        
        final long sum;
        
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8), false)) {
            
            // Try sum 5 + 5
            out.println(5);
            out.println(5);
            
            out.flush();
            sum = Long.parseLong(conn.getHeaderField("sum"));
        }
        
        assertEquals("Expected that 5 + 5 equals 10", 10, sum);
    }
}

/**
 * The {@code SimpleCalculator} does not implement an interface.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
class SimpleCalculator {
    public long sum(long... values) {
        return LongStream.of(values).sum();
    }
}