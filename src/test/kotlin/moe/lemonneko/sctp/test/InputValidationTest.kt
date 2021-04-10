package moe.lemonneko.sctp.test

import moe.lemonneko.sctp.validateOutputPathInput
import moe.lemonneko.sctp.validateSourceCodePathInput
import org.junit.jupiter.api.Test
import kotlin.test.expect

class InputValidationTest {
    @Test
    fun sourceCodePathValidationTest() {
        expect(true) {
            validateSourceCodePathInput("").first
        }
        expect(true) {
            validateSourceCodePathInput("path/not/exists").first
        }
        expect(false) {
            validateSourceCodePathInput(".").first
        }
    }

    @Test
    fun outputPathValidationTest() {
        expect(true) {
            validateOutputPathInput("").first
        }
        expect(true) {
            validateOutputPathInput(".").first
        }
        expect(false) {
            validateOutputPathInput("path/not/exists").first
        }
    }
}
