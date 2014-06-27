package com.rtsffm.ertd.proto;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

//~--- JDK imports ------------------------------------------------------------



import com.rtsffm.ertd.proto.gen.GenInetProtoMojo;

import java.io.File;

//~--- classes ----------------------------------------------------------------

public class GenInetProtoMojoTest extends AbstractMojoTestCase {
    protected void setUp() throws Exception {
        // required
        super.setUp();
    }

    //~--- methods ------------------------------------------------------------

    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }

    /**
     * @throws Exception if any
     */
    public void testSomething() throws Exception {
        File pom = getTestFile("src/test/resources/unit/project-to-test/pom.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());


        GenInetProtoMojo myMojo = (GenInetProtoMojo) lookupMojo("generate-protocol", pom);

        assertNotNull(myMojo);
        myMojo.execute();
    }
}
