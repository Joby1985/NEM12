package simplenem12;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class SimpleNem12ParserImplTest {

    private SimpleNem12Parser parser;

    @Before
    public void setUp() throws Exception {
        parser = new SimpleNem12ParserImpl();
    }

    @Test
    public void testParseSimpleNem12() throws Exception {
        File file = Path.of("src/test/resources/SimpleNem12.csv")
                        .toFile();
        Collection<MeterRead> parsed = parser.parseSimpleNem12(file);
        assertNotNull(parsed);
        assertEquals(2, parsed.size());
        MeterRead[] listOfMeterRads = parsed.toArray(new MeterRead[0]);
        MeterRead reading1 = listOfMeterRads[0];
        assertEquals("6123456789", reading1.getNmi());
        BigDecimal expectedTotVol = new BigDecimal(-36.84);
        expectedTotVol = expectedTotVol.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        assertEquals(expectedTotVol, reading1.getTotalVolume());
    }
}
