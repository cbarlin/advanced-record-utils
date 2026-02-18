package io.github.cbarlin.aru.tests.c_hkj;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LensTests {

    @Test
    void testLensGenerationWorks() {
        // We aren't really testing that the lenses *work* (since it's a library), but
        //   we are testing that our settings has caused them to be generated
        final var numberExtractor = RootElementLenses.otherRecord().andThen(Rec2Lenses.someNumber());
        final RootElement toCheck = RootElementUtils.builder()
                .otherRecord(b -> b.someNumber(42).addListOfStrings("A").addListOfStrings("B"))
                .build();
        assertEquals(42, numberExtractor.get(toCheck));
        final var sealedImpl = new SealedInterfaceImplA(69);
        assertTrue(SealedInterfaceTestPrisms.sealedInterfaceImplA().getOptional(sealedImpl).isPresent());
        assertFalse(SealedInterfaceTestPrisms.sealedInterfaceImplB().getOptional(sealedImpl).isPresent());
    }
}
