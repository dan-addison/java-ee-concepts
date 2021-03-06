package com.martinandersson.javaee.cdi.qualifiers;

import com.martinandersson.javaee.cdi.qualifiers.QualifierDriver.Report;
import com.martinandersson.javaee.cdi.qualifiers.lib.Broccoli;
import com.martinandersson.javaee.cdi.qualifiers.lib.Caloric;
import com.martinandersson.javaee.cdi.qualifiers.lib.Healthy;
import com.martinandersson.javaee.cdi.qualifiers.lib.Meat;
import com.martinandersson.javaee.cdi.qualifiers.lib.Unhealthy;
import com.martinandersson.javaee.cdi.qualifiers.lib.Water;
import com.martinandersson.javaee.utils.DeploymentBuilder;
import com.martinandersson.javaee.utils.HttpRequests;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TODO: Write something.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
@RunWith(Arquillian.class)
public class QualifierTest
{    
    @Deployment
    public static WebArchive buildDeployment() {
        return new DeploymentBuilder(QualifierTest.class)
                .addEmptyBeansXMLFile()
                .add(
                  // Driver
                  QualifierDriver.class,
                
                  // Qualifiers
                  Healthy.class,
                  Unhealthy.class,
                
                  // Common bean type
                  Caloric.class,
                
                  // Caloric implementations
                  Water.class,
                  Broccoli.class,
                  Meat.class)
                .build();
    }
    
    @Test
    @RunAsClient
    public void qualifierTest(@ArquillianResource URL url) {
        Report<Class<? extends Caloric>> report = HttpRequests.getObject(url);
        
        assertEquals("Expected that Water is the only @Default Caloric bean.",
                Water.class, report.defaultType);
        
        assertEquals("Expected that Broccoli is the only @Healthy Caloric bean.",
                Broccoli.class, report.healthyType);
        
        assertEquals("Expected that Meat is the only @Unhealthy Caloric bean.",
                Meat.class, report.unhealthyType);
    }
}